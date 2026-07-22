package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.InterviewDashboardUiState
import com.example.data.QuizResult
import com.example.data.InterviewItem
import com.example.data.InterviewStatusUpdate
import com.example.data.InterviewsRepository
import kotlinx.coroutines.launch

data class LocalPushNotification(
    val id: String,
    val companyName: String,
    val jobTitle: String,
    val date: String,
    val time: String,
    val interviewer: String,
    val timestamp: String = "Just Now"
)

@Composable
fun LocalPushNotificationBanner(
    notification: LocalPushNotification,
    onSnooze: () -> Unit,
    onViewDetails: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .testTag("local_push_notification_banner"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Push Notification Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JobTraq • System Push Notification",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Notification",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title & Content
            Text(
                text = "Upcoming Interview Reminder: ${notification.companyName}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${notification.jobTitle} with ${notification.interviewer} on ${notification.date} @ ${notification.time}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notification Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onSnooze,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snooze 15m", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onViewDetails,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Interview", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun InterviewDashboardComponent(
    repository: InterviewsRepository = remember { InterviewsRepository() },
    recentQuizResults: List<QuizResult> = emptyList(),
    modifier: Modifier = Modifier,
    onShowToast: (String) -> Unit = {}
) {
    var uiState by remember { mutableStateOf<InterviewDashboardUiState>(InterviewDashboardUiState.Loading) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Upcoming", "Completed", "Cancelled"
    var dashboardViewMode by remember { mutableIntStateOf(0) } // 0: Interactive Calendar, 1: Updates Stream
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedUpdateDetail by remember { mutableStateOf<InterviewStatusUpdate?>(null) }
    var selectedCalendarDay by remember { mutableStateOf<Int?>(25) } // Default selected day July 25
    val coroutineScope = rememberCoroutineScope()

    // Remind Me Toggle state map for interviews
    var remindMeStateMap by remember { mutableStateOf<Map<String, Boolean>>(mapOf("int-1" to true, "int-2" to true)) }
    
    // Active Push Notification Mock-up Banner
    var activePushNotification by remember {
        mutableStateOf<LocalPushNotification?>(
            LocalPushNotification(
                id = "int-1",
                companyName = "Acme Corp",
                jobTitle = "Senior Android Engineer",
                date = "Fri, July 25, 2026",
                time = "10:00 AM PST",
                interviewer = "Sarah Jenkins (Tech Lead)"
            )
        )
    }

    fun handleToggleRemindMe(interview: InterviewItem, enabled: Boolean) {
        remindMeStateMap = remindMeStateMap + (interview.id to enabled)
        if (enabled) {
            activePushNotification = LocalPushNotification(
                id = interview.id,
                companyName = interview.companyName,
                jobTitle = interview.jobTitle,
                date = interview.date,
                time = interview.time,
                interviewer = interview.interviewer
            )
            onShowToast("Local push notification active for ${interview.companyName}")
        } else {
            if (activePushNotification?.id == interview.id) {
                activePushNotification = null
            }
            onShowToast("Reminder disabled for ${interview.companyName}")
        }
    }

    fun loadData() {
        coroutineScope.launch {
            isRefreshing = true
            try {
                repository.fetchInterviewsFromApi()
                uiState = repository.uiState.value
            } catch (e: Exception) {
                uiState = InterviewDashboardUiState.Error(e.message ?: "Failed to fetch /api/v1/interviews")
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("interview_dashboard_root")
    ) {
        // Animated Push Notification Banner Mock-up
        AnimatedVisibility(
            visible = activePushNotification != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            activePushNotification?.let { notification ->
                LocalPushNotificationBanner(
                    notification = notification,
                    onSnooze = {
                        onShowToast("Snoozed push notification for 15 minutes.")
                        activePushNotification = null
                    },
                    onViewDetails = {
                        selectedCalendarDay = 25
                        dashboardViewMode = 0
                        onShowToast("Viewing interview details for ${notification.companyName}")
                    },
                    onDismiss = {
                        activePushNotification = null
                        onShowToast("Push notification banner dismissed")
                    }
                )
            }
        }

        // API Header Endpoint Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "GET /api/v1/interviews",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "Live REST Endpoint • 200 OK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(
                            onClick = {
                                loadData()
                                onShowToast("Refreshed /api/v1/interviews payload")
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("refresh_dashboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh REST Endpoint",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = uiState) {
            is InterviewDashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Fetching /api/v1/interviews...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            is InterviewDashboardUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error fetching interview data", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { loadData() }) {
                            Text("Retry Call")
                        }
                    }
                }
            }

            is InterviewDashboardUiState.Success -> {
                val data = state.data

                // Summary Stat Cards Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Upcoming Stat Card
                    DashboardStatCard(
                        title = "Upcoming",
                        count = "${data.upcomingCount}",
                        icon = Icons.Default.Event,
                        bgColor = Color(0xFFFFF7ED),
                        contentColor = Color(0xFFC2410C),
                        isSelected = selectedFilter == "Upcoming",
                        onClick = {
                            selectedFilter = if (selectedFilter == "Upcoming") "All" else "Upcoming"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_upcoming")
                    )

                    // Completed Stat Card
                    DashboardStatCard(
                        title = "Completed",
                        count = "${data.completedCount}",
                        icon = Icons.Default.CheckCircle,
                        bgColor = Color(0xFFECFDF5),
                        contentColor = Color(0xFF047857),
                        isSelected = selectedFilter == "Completed",
                        onClick = {
                            selectedFilter = if (selectedFilter == "Completed") "All" else "Completed"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_completed")
                    )

                    // Cancelled Stat Card
                    DashboardStatCard(
                        title = "Cancelled",
                        count = "${data.cancelledCount}",
                        icon = Icons.Default.Cancel,
                        bgColor = Color(0xFFF3E8FF),
                        contentColor = Color(0xFF7E22CE),
                        isSelected = selectedFilter == "Cancelled",
                        onClick = {
                            selectedFilter = if (selectedFilter == "Cancelled") "All" else "Cancelled"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_cancelled")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // RECENT QUIZZES SECTION
                RecentQuizzesSection(
                    recentQuizzes = recentQuizResults,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dashboard Mode Toggle Switcher (Calendar View vs Visual Trends vs Updates Stream)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { dashboardViewMode = 0 }
                                .testTag("view_mode_calendar"),
                            color = if (dashboardViewMode == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            tonalElevation = if (dashboardViewMode == 0) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (dashboardViewMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Calendar",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (dashboardViewMode == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (dashboardViewMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { dashboardViewMode = 1 }
                                .testTag("view_mode_trends"),
                            color = if (dashboardViewMode == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            tonalElevation = if (dashboardViewMode == 1) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = if (dashboardViewMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Trends Chart",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (dashboardViewMode == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (dashboardViewMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { dashboardViewMode = 2 }
                                .testTag("view_mode_stream"),
                            color = if (dashboardViewMode == 2) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            tonalElevation = if (dashboardViewMode == 2) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = if (dashboardViewMode == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Stream",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (dashboardViewMode == 2) FontWeight.Bold else FontWeight.Normal,
                                        color = if (dashboardViewMode == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (dashboardViewMode == 0) {
                    // INTERACTIVE CALENDAR VIEW
                    InteractiveInterviewCalendarView(
                        interviews = data.interviews,
                        selectedDay = selectedCalendarDay,
                        selectedStatusFilter = selectedFilter,
                        remindMeStateMap = remindMeStateMap,
                        onToggleRemindMe = { interview, enabled -> handleToggleRemindMe(interview, enabled) },
                        onSelectDay = { day -> selectedCalendarDay = day },
                        onShowToast = onShowToast
                    )
                } else if (dashboardViewMode == 1) {
                    // VISUAL TRENDS CHART VIEW (Recharts-Style Canvas)
                    InterviewVolumeTrendsChartCard(
                        interviews = data.interviews,
                        onNavigateToCalendarWeek = { weekDay ->
                            selectedCalendarDay = weekDay
                            dashboardViewMode = 0
                        },
                        onShowToast = onShowToast
                    )
                } else {
                    // RECENT STATUS UPDATES STREAM VIEW
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recent Status Updates",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (selectedFilter != "All") {
                                TextButton(onClick = { selectedFilter = "All" }) {
                                    Text("Reset Filter ($selectedFilter)", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredUpdates = remember(data.statusUpdates, selectedFilter) {
                            if (selectedFilter == "All") {
                                data.statusUpdates
                            } else {
                                data.statusUpdates.filter { update ->
                                    update.newStatus.equals(selectedFilter, ignoreCase = true) ||
                                            (selectedFilter == "Upcoming" && update.newStatus.equals("Scheduled", ignoreCase = true))
                                }
                            }
                        }

                        if (filteredUpdates.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No status updates found for filter '$selectedFilter'.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("recent_updates_list"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredUpdates, key = { it.id }) { update ->
                                    StatusUpdateCardItem(
                                        update = update,
                                        onClick = { selectedUpdateDetail = update }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Status Update Details Dialog Modal
        if (selectedUpdateDetail != null) {
            val detail = selectedUpdateDetail!!
            Dialog(onDismissRequest = { selectedUpdateDetail = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Interview Status Update",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = { selectedUpdateDetail = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${detail.jobTitle} @ ${detail.companyName}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(detail.previousStatus)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Transitions to",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusChip(detail.newStatus)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Timestamp: ${detail.timestamp}",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = detail.updateMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                onShowToast("Reminders synchronized for ${detail.companyName}")
                                selectedUpdateDetail = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Acknowledge & Sync Calendar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveInterviewCalendarView(
    interviews: List<InterviewItem>,
    selectedDay: Int?,
    selectedStatusFilter: String,
    remindMeStateMap: Map<String, Boolean>,
    onToggleRemindMe: (InterviewItem, Boolean) -> Unit,
    onSelectDay: (Int?) -> Unit,
    onShowToast: (String) -> Unit
) {
    var monthName by remember { mutableStateOf("July 2026") }

    // Map day numbers (1..31) to interview items
    val dayToInterviewsMap = remember(interviews) {
        val map = mutableMapOf<Int, MutableList<InterviewItem>>()
        interviews.forEach { item ->
            // Extract day from date strings like "Fri, July 25, 2026" or "July 25, 2026"
            val dayRegex = Regex("""\b(\d{1,2})\b""")
            val match = dayRegex.find(item.date)
            if (match != null) {
                val dayNum = match.groupValues[1].toIntOrNull()
                if (dayNum != null && dayNum in 1..31) {
                    map.getOrPut(dayNum) { mutableListOf() }.add(item)
                }
            }
        }
        map
    }

    // Days grid for July 2026 (July 1st 2026 is Wednesday -> 3 blank leading cells Sun, Mon, Tue)
    val leadingOffset = 3
    val totalDaysInJuly = 31
    val calendarGridCells = remember {
        val list = mutableListOf<Int?>()
        repeat(leadingOffset) { list.add(null) }
        for (d in 1..totalDaysInJuly) { list.add(d) }
        while (list.size % 7 != 0) { list.add(null) }
        list
    }

    // Selected day interviews list filtered by status filter if active
    val interviewsOnSelectedDay = remember(selectedDay, dayToInterviewsMap, selectedStatusFilter) {
        if (selectedDay == null) emptyList()
        else {
            val list = dayToInterviewsMap[selectedDay] ?: emptyList()
            if (selectedStatusFilter == "All") list
            else list.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_calendar_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Calendar Header & Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onShowToast("Navigated to June 2026") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                            }
                            IconButton(
                                onClick = { onShowToast("Navigated to August 2026") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days of week row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid (31 Days)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val rows = calendarGridCells.chunked(7)
                        rows.forEach { weekRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weekRow.forEach { dayNum ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayNum != null) {
                                            val dayInterviews = dayToInterviewsMap[dayNum] ?: emptyList()
                                            val isSelected = selectedDay == dayNum
                                            val hasUpcoming = dayInterviews.any { it.status.equals("Upcoming", ignoreCase = true) }
                                            val hasCompleted = dayInterviews.any { it.status.equals("Completed", ignoreCase = true) }
                                            val hasCancelled = dayInterviews.any { it.status.equals("Cancelled", ignoreCase = true) }

                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .clickable { onSelectDay(if (isSelected) null else dayNum) }
                                                    .testTag("calendar_day_$dayNum"),
                                                shape = CircleShape,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    dayInterviews.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                    else -> Color.Transparent
                                                },
                                                border = if (isSelected) null else if (dayInterviews.isNotEmpty()) {
                                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                                } else null
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        text = "$dayNum",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = if (dayInterviews.isNotEmpty() || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = when {
                                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                                dayInterviews.isNotEmpty() -> MaterialTheme.colorScheme.primary
                                                                else -> MaterialTheme.colorScheme.onSurface
                                                            },
                                                            fontSize = 12.sp
                                                        )
                                                    )

                                                    // Visual Highlight Indicator Dots for Scheduled Interviews
                                                    if (dayInterviews.isNotEmpty()) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(top = 1.dp)
                                                        ) {
                                                            if (hasUpcoming) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(5.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) Color.White else Color(0xFFEA580C))
                                                                )
                                                            }
                                                            if (hasCompleted) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(5.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) Color.White else Color(0xFF059669))
                                                                )
                                                            }
                                                            if (hasCancelled) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(5.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) Color.White else Color(0xFF7E22CE))
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEA580C)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upcoming", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF059669)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Completed", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF7E22CE)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancelled", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Details Panel for Selected Calendar Date
        item {
            if (selectedDay == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap any date highlighted with an indicator dot to view scheduled interview details.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calendar_selected_day_details"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Interviews for July $selectedDay, 2026",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            IconButton(
                                onClick = { onSelectDay(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (interviewsOnSelectedDay.isEmpty()) {
                            Text(
                                text = "No interviews scheduled on July $selectedDay, 2026.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                interviewsOnSelectedDay.forEach { interview ->
                                    CalendarInterviewDetailCard(
                                        interview = interview,
                                        isRemindMeEnabled = remindMeStateMap[interview.id] ?: false,
                                        onToggleRemindMe = { enabled -> onToggleRemindMe(interview, enabled) },
                                        onSyncReminder = {
                                            onShowToast("Calendar reminder synced for ${interview.companyName}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarInterviewDetailCard(
    interview: InterviewItem,
    isRemindMeEnabled: Boolean,
    onToggleRemindMe: (Boolean) -> Unit,
    onSyncReminder: () -> Unit
) {
    val statusColor = when (interview.status.lowercase()) {
        "upcoming", "scheduled" -> Color(0xFFEA580C)
        "completed" -> Color(0xFF059669)
        "cancelled" -> Color(0xFF7E22CE)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = interview.companyName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = interview.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = interview.jobTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${interview.date} @ ${interview.time}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = interview.location,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = interview.interviewer,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (interview.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${interview.notes}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Remind Me Toggle Switch Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isRemindMeEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isRemindMeEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = if (isRemindMeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Remind Me",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isRemindMeEnabled) "Push notification enabled" else "Toggle for notification mock-up",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isRemindMeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isRemindMeEnabled,
                        onCheckedChange = onToggleRemindMe,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("remind_me_switch_${interview.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSyncReminder,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync External Calendar", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    count: String,
    icon: ImageVector,
    bgColor: Color,
    contentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) bgColor else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )

                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = contentColor
                    ) {
                        Box(modifier = Modifier.size(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun StatusUpdateCardItem(
    update: InterviewStatusUpdate,
    onClick: () -> Unit
) {
    val statusColor = when (update.newStatus.lowercase()) {
        "upcoming", "scheduled" -> Color(0xFFEA580C)
        "completed" -> Color(0xFF059669)
        "cancelled" -> Color(0xFF7E22CE)
        else -> MaterialTheme.colorScheme.primary
    }

    val iconVector = when (update.newStatus.lowercase()) {
        "upcoming", "scheduled" -> Icons.Default.Event
        "completed" -> Icons.Default.CheckCircle
        "cancelled" -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("update_card_${update.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = update.newStatus,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = update.companyName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = update.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }

                Text(
                    text = update.jobTitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(update.previousStatus)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "➔",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusChip(update.newStatus)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = update.updateMessage,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun StatusChip(statusText: String) {
    val (chipBg, chipFg) = when (statusText.lowercase()) {
        "upcoming", "scheduled" -> Color(0xFFFFF7ED) to Color(0xFFC2410C)
        "completed" -> Color(0xFFECFDF5) to Color(0xFF047857)
        "cancelled" -> Color(0xFFF3E8FF) to Color(0xFF7E22CE)
        "applied" -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = chipBg
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = chipFg,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun InterviewVolumeTrendsChartCard(
    interviews: List<InterviewItem>,
    onNavigateToCalendarWeek: (Int) -> Unit,
    onShowToast: (String) -> Unit
) {
    var chartType by remember { mutableIntStateOf(0) } // 0: Bar Chart, 1: Area Curve
    var selectedWeekIndex by remember { mutableIntStateOf(2) } // Default select Week 3 (Peak)

    val weeklyData = remember(interviews) {
        listOf(
            WeeklyTrendData("Week 1", "July 1 - 7", 3, 2, 1, 0, 4),
            WeeklyTrendData("Week 2", "July 8 - 14", 5, 3, 2, 0, 11),
            WeeklyTrendData("Week 3", "July 15 - 21", 8, 5, 2, 1, 18),
            WeeklyTrendData("Week 4", "July 22 - 28", 4, 3, 1, 0, 25),
            WeeklyTrendData("Week 5", "July 29 - 31", 2, 1, 1, 0, 29)
        )
    }

    val totalMonthVolume = weeklyData.sumOf { it.total }
    val avgPerWeek = String.format("%.1f", totalMonthVolume.toDouble() / weeklyData.size)
    val peakWeek = weeklyData.maxByOrNull { it.total }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visual_trends_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart Header & Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interview Volume Trends",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                    Text(
                        text = "Past Month • Recharts Canvas Engine",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }

                // Switch Chart Type (Bar / Curve)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        IconButton(
                            onClick = { chartType = 0 },
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (chartType == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Bar Chart View",
                                tint = if (chartType == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { chartType = 1 },
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (chartType == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Area Curve View",
                                tint = if (chartType == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Monthly Total", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text("$totalMonthVolume", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Weekly Avg", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text(avgPerWeek, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary))
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7ED)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Peak Week", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text("${peakWeek?.total ?: 0} (${peakWeek?.weekName?.take(2)})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEA580C)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Native Jetpack Compose Canvas Chart (Recharts-Style)
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = Color(0xFF0284C7)
            val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            val maxVal = 10f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("canvas_chart_area")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val widthPerBar = size.width / weeklyData.size
                                val tappedIndex = (offset.x / widthPerBar).toInt().coerceIn(0, weeklyData.size - 1)
                                selectedWeekIndex = tappedIndex
                                onShowToast("Selected ${weeklyData[tappedIndex].weekName}: ${weeklyData[tappedIndex].total} interviews")
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomPadding = 30f
                    val topPadding = 20f
                    val chartHeight = canvasHeight - bottomPadding - topPadding

                    // Draw Horizontal Gridlines
                    val ySteps = 4
                    for (i in 0..ySteps) {
                        val y = topPadding + (chartHeight / ySteps) * i

                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    val numBars = weeklyData.size
                    val barWidth = (canvasWidth / numBars) * 0.45f
                    val stepX = canvasWidth / numBars

                    if (chartType == 0) {
                        // Render Bar Chart
                        weeklyData.forEachIndexed { index, data ->
                            val isSelected = index == selectedWeekIndex
                            val barHeight = (data.total / maxVal) * chartHeight
                            val x = (stepX * index) + (stepX / 2) - (barWidth / 2)
                            val y = topPadding + (chartHeight - barHeight)

                            // Bar Shadow / Gradient
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = if (isSelected) listOf(
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.7f)
                                    ) else listOf(
                                        secondaryColor.copy(alpha = 0.6f),
                                        secondaryColor.copy(alpha = 0.3f)
                                    )
                                ),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(12f, 12f)
                            )

                            if (isSelected) {
                                // Draw top cap dot indicator
                                drawCircle(
                                    color = primaryColor,
                                    radius = 6f,
                                    center = Offset(x + barWidth / 2, y - 10f)
                                )
                            }
                        }
                    } else {
                        // Render Smooth Area Curve
                        val path = Path()
                        val fillPath = Path()

                        weeklyData.forEachIndexed { index, data ->
                            val x = (stepX * index) + (stepX / 2)
                            val y = topPadding + chartHeight - ((data.total / maxVal) * chartHeight)

                            if (index == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, canvasHeight - bottomPadding)
                                fillPath.lineTo(x, y)
                            } else {
                                val prevX = (stepX * (index - 1)) + (stepX / 2)
                                val prevY = topPadding + chartHeight - ((weeklyData[index - 1].total / maxVal) * chartHeight)
                                val controlX1 = prevX + (x - prevX) / 2
                                val controlX2 = prevX + (x - prevX) / 2
                                path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                                fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                            }

                            if (index == weeklyData.size - 1) {
                                fillPath.lineTo(x, canvasHeight - bottomPadding)
                                fillPath.close()
                            }
                        }

                        // Gradient Area Fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        // Spline Curve Line
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 6f)
                        )

                        // Data Point Circles
                        weeklyData.forEachIndexed { index, data ->
                            val isSelected = index == selectedWeekIndex
                            val x = (stepX * index) + (stepX / 2)
                            val y = topPadding + chartHeight - ((data.total / maxVal) * chartHeight)

                            drawCircle(
                                color = if (isSelected) primaryColor else secondaryColor,
                                radius = if (isSelected) 10f else 6f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            // X-Axis Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEachIndexed { index, data ->
                    val isSelected = index == selectedWeekIndex
                    Text(
                        text = data.weekName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedWeekIndex = index
                                onShowToast("Selected ${data.weekName}")
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Week Detail Tooltip Breakdown Panel
            val currentSelectedData = weeklyData.getOrNull(selectedWeekIndex) ?: weeklyData[0]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentSelectedData.weekName} (${currentSelectedData.dateRange})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${currentSelectedData.total} Total",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status breakdown chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEA580C)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${currentSelectedData.upcoming} Upcoming", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF059669)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${currentSelectedData.completed} Completed", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF7E22CE)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${currentSelectedData.cancelled} Cancelled", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            onNavigateToCalendarWeek(currentSelectedData.representativeDay)
                            onShowToast("Navigated to calendar date July ${currentSelectedData.representativeDay}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Jump to Calendar for this Week", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

data class WeeklyTrendData(
    val weekName: String,
    val dateRange: String,
    val total: Int,
    val upcoming: Int,
    val completed: Int,
    val cancelled: Int,
    val representativeDay: Int
)
