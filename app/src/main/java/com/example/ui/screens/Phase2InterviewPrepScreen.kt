package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.I18nHelper
import com.example.data.QuestionEntity
import com.example.data.QuizEntity
import com.example.data.QuizResult
import com.example.ui.components.RecentQuizzesSection

@Composable
fun Phase2InterviewPrepScreen(
    questions: List<QuestionEntity>,
    quizzes: List<QuizEntity>,
    currentLanguage: String,
    recentQuizResults: List<QuizResult> = emptyList(),
    onSaveQuizResult: (QuizResult) -> Unit = {},
    onToggleBookmark: (String) -> Unit,
    onCreateQuiz: (String, String, List<QuestionEntity>) -> Unit,
    onQuizStateChanged: (Boolean) -> Unit = {},
    onShowToast: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Quizzes, 2: Book Mock
    val subTabs = listOf("Question Bank", "Practice Quizzes", "Book Practice")

    var activeQuiz by remember { mutableStateOf<QuizEntity?>(null) }
    var activeQuizResult by remember { mutableStateOf<QuizResult?>(null) }

    LaunchedEffect(activeQuiz, activeQuizResult) {
        onQuizStateChanged(activeQuiz != null || activeQuizResult != null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header SubTab Navigation
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                subTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSubTab == index,
                        onClick = {
                            selectedSubTab = index
                            activeQuiz = null
                            activeQuizResult = null
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("prep_subtab_$index")
                    )
                }
            }

            // Main Content Area
            when (selectedSubTab) {
                0 -> QuestionBankSection(
                    questions = questions,
                    onToggleBookmark = onToggleBookmark,
                    onShowToast = onShowToast
                )
                1 -> QuizzesSection(
                    quizzes = quizzes,
                    questions = questions,
                    recentQuizResults = recentQuizResults,
                    onStartQuiz = { quiz -> activeQuiz = quiz },
                    onCreateQuiz = onCreateQuiz
                )
                2 -> BookMockInterviewSection(onShowToast = onShowToast)
            }
        }

        // Full Screen Practice Quiz Container (Absolute full-screen positioning in container)
        if (activeQuizResult != null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("full_screen_quiz_result"),
                color = MaterialTheme.colorScheme.background
            ) {
                QuizResultView(
                    result = activeQuizResult!!,
                    onDismiss = { activeQuizResult = null }
                )
            }
        } else if (activeQuiz != null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("full_screen_quiz_player"),
                color = MaterialTheme.colorScheme.background
            ) {
                QuizPlayerView(
                    quiz = activeQuiz!!,
                    onFinishQuiz = { result ->
                        onSaveQuizResult(result)
                        activeQuizResult = result
                        activeQuiz = null
                    },
                    onExit = { activeQuiz = null }
                )
            }
        }
    }
}

@Composable
private fun QuestionBankSection(
    questions: List<QuestionEntity>,
    onToggleBookmark: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showOnlyBookmarked by remember { mutableStateOf(false) }

    val categories = listOf("All", "Technical", "Behavioral", "System Design", "HR")

    val filteredQuestions = remember(questions, searchQuery, selectedCategory, showOnlyBookmarked) {
        questions.filter { q ->
            val matchesCategory = selectedCategory == "All" || q.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = q.questionText.contains(searchQuery, ignoreCase = true) || q.category.contains(searchQuery, ignoreCase = true)
            val matchesBookmark = !showOnlyBookmarked || q.isBookmarked
            matchesCategory && matchesSearch && matchesBookmark
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Daily Challenge Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_challenge_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DAILY INTERVIEW CHALLENGE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Q: What is a closure in JavaScript or Kotlin, and how is state captured?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onShowToast("+50 XP Earned! Daily Challenge Completed! 🎉") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("submit_daily_challenge_button")
                ) {
                    Text("Submit Daily Answer (+50 XP)")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Bookmark Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("question_search_input"),
                placeholder = { Text("Search questions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showOnlyBookmarked = !showOnlyBookmarked }
                    .testTag("bookmarked_filter_toggle"),
                color = if (showOnlyBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (showOnlyBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmarked Only",
                    tint = if (showOnlyBookmarked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = category },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Questions List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredQuestions, key = { it.id }) { q ->
                QuestionCardItem(
                    question = q,
                    onToggleBookmark = onToggleBookmark
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun QuestionCardItem(
    question: QuestionEntity,
    onToggleBookmark: (String) -> Unit
) {
    var expandedAnswer by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = question.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = question.difficulty,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleBookmark(question.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("bookmark_button_${question.id}")
                ) {
                    Icon(
                        imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (question.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { expandedAnswer = !expandedAnswer },
                modifier = Modifier.testTag("toggle_answer_button_${question.id}")
            ) {
                Text(if (expandedAnswer) "Hide Sample Answer" else "Show Sample Answer")
            }

            if (expandedAnswer) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = question.sampleAnswer,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizzesSection(
    quizzes: List<QuizEntity>,
    questions: List<QuestionEntity>,
    recentQuizResults: List<QuizResult> = emptyList(),
    onStartQuiz: (QuizEntity) -> Unit,
    onCreateQuiz: (String, String, List<QuestionEntity>) -> Unit
) {
    var isCreateModalOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (recentQuizResults.isNotEmpty()) {
            RecentQuizzesSection(
                recentQuizzes = recentQuizResults,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Quizzes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Button(
                onClick = { isCreateModalOpen = true },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("create_quiz_button")
            ) {
                Text("Create Custom Quiz")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        quizzes.forEach { quiz ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("quiz_card_${quiz.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = quiz.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${quiz.questions.size} Questions",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Button(
                            onClick = { onStartQuiz(quiz) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("start_quiz_button_${quiz.id}")
                        ) {
                            Text("Start Practice")
                        }
                    }
                }
            }
        }
    }

    if (isCreateModalOpen) {
        CreateQuizDialog(
            availableQuestions = questions,
            onDismiss = { isCreateModalOpen = false },
            onCreate = { title, desc, selected ->
                onCreateQuiz(title, desc, selected)
                isCreateModalOpen = false
            }
        )
    }
}

// QuizPlayerView and QuizResultView are now rendered using QuizPlayerComponents.kt


@Composable
private fun BookMockInterviewSection(onShowToast: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Book Practice Interviews",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Prepare for live interviews with AI audio simulations, expert mentors, or peer friends.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // AI Mock Interview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("book_ai_mock_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Practice with Gemini AI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Instant audio-first interactive mock interview powered by Gemini 3.5 Flash.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onShowToast("Starting AI Mock Session with Gemini...") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Start Instant AI Session")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Expert Mentor Session Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Practice with Industry Experts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Schedule a 1-on-1 feedback session with verified tech lead mentors.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onShowToast("Mentor booking request submitted!") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Request Mentor Session")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Friend Session Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Practice with Friends",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Invite a classmate or peer via email to conduct a peer-to-peer interview.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onShowToast("Friend invitation link copied!") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Invite Friend via Link")
                }
            }
        }
    }
}

@Composable
private fun CreateQuizDialog(
    availableQuestions: List<QuestionEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, String, List<QuestionEntity>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateMapOf<String, Boolean>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create Custom Quiz",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quiz Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Questions:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                availableQuestions.forEach { q ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIds[q.id] = !(selectedIds[q.id] ?: false) }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedIds[q.id] ?: false,
                            onCheckedChange = { selectedIds[q.id] = it }
                        )
                        Text(text = q.questionText, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            val selectedList = availableQuestions.filter { selectedIds[it.id] == true }
                            if (title.isNotBlank() && selectedList.isNotEmpty()) {
                                onCreate(title, desc, selectedList)
                            }
                        }
                    ) { Text("Create") }
                }
            }
        }
    }
}
