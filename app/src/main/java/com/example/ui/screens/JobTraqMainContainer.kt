package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JobEntity
import com.example.data.JobTraqRepository
import com.example.data.UserEntity
import com.example.ui.components.JobTraqBottomNav
import com.example.ui.components.JobTraqTab
import com.example.ui.components.JobTraqTopBar
import kotlinx.coroutines.launch

@Composable
fun JobTraqMainContainer(
    user: UserEntity,
    darkThemeOverride: Boolean,
    isEditModalOpen: Boolean,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenEditModal: () -> Unit,
    onCloseEditModal: () -> Unit,
    onSaveProfile: (String, String, Int) -> Unit,
    repository: JobTraqRepository = remember { JobTraqRepository() }
) {
    var selectedTab by remember { mutableStateOf(JobTraqTab.PIPELINE) }
    var isQuizActive by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val currentTenant by repository.currentTenant.collectAsStateWithLifecycle()
    val currentRole by repository.currentRole.collectAsStateWithLifecycle()
    val currentLanguage by repository.currentLanguage.collectAsStateWithLifecycle()

    val jobs by repository.jobs.collectAsStateWithLifecycle()
    val questions by repository.questions.collectAsStateWithLifecycle()
    val quizzes by repository.quizzes.collectAsStateWithLifecycle()
    val feedPosts by repository.feedPosts.collectAsStateWithLifecycle()
    val walletState by repository.walletState.collectAsStateWithLifecycle()
    val resumes by repository.resumes.collectAsStateWithLifecycle()

    LaunchedEffect(selectedTab) {
        if (selectedTab != JobTraqTab.PREP_HUB) {
            isQuizActive = false
        }
    }

    fun showToast(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isQuizActive) {
                JobTraqTopBar(
                    currentLanguage = currentLanguage,
                    onOpenSettings = { selectedTab = JobTraqTab.PROFILE }
                )
            }
        },
        bottomBar = {
            if (!isQuizActive) {
                JobTraqBottomNav(
                    selectedTab = selectedTab,
                    currentLanguage = currentLanguage,
                    onTabSelected = { selectedTab = it }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isQuizActive) PaddingValues(0.dp) else innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "JobTraqTabTransition"
            ) { tab ->
                when (tab) {
                    JobTraqTab.PIPELINE -> {
                        Phase1JobTrackerScreen(
                            jobs = jobs,
                            currentTenant = currentTenant,
                            currentLanguage = currentLanguage,
                            onAddJob = { newJob ->
                                repository.addJob(newJob)
                                showToast("Job application saved!")
                            },
                            onUpdateStatus = { id, status ->
                                repository.updateJobStatus(id, status)
                                showToast("Status updated to '$status'")
                            },
                            onDeleteJob = { id ->
                                repository.deleteJob(id)
                                showToast("Job deleted from pipeline.")
                            },
                            onScheduleReminder = { company ->
                                showToast("Reminder scheduled for $company interview!")
                            }
                        )
                    }

                    JobTraqTab.PREP_HUB -> {
                        Phase2InterviewPrepScreen(
                            questions = questions,
                            quizzes = quizzes,
                            currentLanguage = currentLanguage,
                            onToggleBookmark = { id -> repository.toggleBookmark(id) },
                            onCreateQuiz = { title, desc, qList ->
                                repository.createQuiz(title, desc, qList)
                                showToast("Custom quiz '$title' created!")
                            },
                            onQuizStateChanged = { active -> isQuizActive = active },
                            onShowToast = { showToast(it) }
                        )
                    }

                    JobTraqTab.COMMUNITY -> {
                        Phase3CommunityScreen(
                            feedPosts = feedPosts,
                            walletState = walletState,
                            currentTenant = currentTenant,
                            onAddPost = { text -> repository.addFeedPost(text, user.fullName) },
                            onToggleLike = { id -> repository.toggleLikePost(id) },
                            onAddComment = { id, comment -> repository.addCommentToPost(id, comment) },
                            onShowToast = { showToast(it) }
                        )
                    }

                    JobTraqTab.TOOLS -> {
                        Phase4AdvancedToolsScreen(
                            resumes = resumes,
                            onAddResume = { title, role, content ->
                                repository.addResume(title, role, content)
                                showToast("Resume '$title' saved!")
                            },
                            onAnalyzeResume = { resId, jd ->
                                repository.analyzeResumeWithAI(resId, jd)
                            },
                            onShowToast = { showToast(it) }
                        )
                    }

                    JobTraqTab.PROFILE -> {
                        ProfileDashboardScreen(
                            user = user,
                            darkThemeOverride = darkThemeOverride,
                            isEditModalOpen = isEditModalOpen,
                            onLogout = onLogout,
                            onToggleTheme = onToggleTheme,
                            onOpenEditModal = onOpenEditModal,
                            onCloseEditModal = onCloseEditModal,
                            onSaveProfile = onSaveProfile,
                            currentTenant = currentTenant,
                            currentRole = currentRole,
                            currentLanguage = currentLanguage,
                            onTenantSelected = { repository.setTenant(it) },
                            onRoleSelected = { repository.setRole(it) },
                            onLanguageSelected = { repository.setLanguage(it) }
                        )
                    }
                }
            }
        }
    }
}
