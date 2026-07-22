package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.ui.components.InterviewDashboardComponent
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.I18nHelper
import com.example.data.JobEntity
import com.example.data.QuizResult

@Composable
fun Phase1JobTrackerScreen(
    jobs: List<JobEntity>,
    currentTenant: String,
    currentLanguage: String,
    recentQuizResults: List<QuizResult> = emptyList(),
    onAddJob: (JobEntity) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteJob: (String) -> Unit,
    onScheduleReminder: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Interview Dashboard, 1: Pipeline
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isAddJobDialogOpen by remember { mutableStateOf(false) }

    val statusCategories = listOf("All", "Saved", "Applied", "Incoming Interview", "Offered", "Cancelled", "Rejected")

    val tenantJobs = remember(jobs, currentTenant) {
        jobs.filter { currentTenant == "platform" || it.tenantId == currentTenant || it.tenantId == "platform" }
    }

    val filteredJobs = remember(tenantJobs, selectedStatusFilter, searchQuery) {
        tenantJobs.filter { job ->
            val matchesStatus = selectedStatusFilter == "All" ||
                    job.status.equals(selectedStatusFilter, ignoreCase = true) ||
                    (selectedStatusFilter == "Incoming Interview" && job.status.equals("Interviewing", ignoreCase = true))
            val matchesSearch = job.companyName.contains(searchQuery, ignoreCase = true) ||
                    job.jobTitle.contains(searchQuery, ignoreCase = true) ||
                    job.notes.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    // Dashboard Statistics
    val totalApps = tenantJobs.size
    val incomingInterviews = tenantJobs.filter { it.status.equals("Incoming Interview", ignoreCase = true) || it.status.equals("Interviewing", ignoreCase = true) }
    val offeredCount = tenantJobs.count { it.status.equals("Offered", ignoreCase = true) }
    val cancelledCount = tenantJobs.count { it.status.equals("Cancelled", ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title & Add Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = I18nHelper.getString("job_tracker", currentLanguage),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = if (selectedSubTab == 0) "REST API /api/v1/interviews Summary" else "${filteredJobs.size} Active Pipeline Applications",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Button(
                    onClick = { isAddJobDialogOpen = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("add_job_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Job",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = I18nHelper.getString("add_job", currentLanguage), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Tab Switcher
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedSubTab = 0 }
                            .testTag("subtab_interview_dashboard"),
                        color = if (selectedSubTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = if (selectedSubTab == 0) 2.dp else 0.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Interview Dashboard",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSubTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedSubTab = 1 }
                            .testTag("subtab_pipeline_board"),
                        color = if (selectedSubTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = if (selectedSubTab == 1) 2.dp else 0.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pipeline Board",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSubTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedSubTab == 0) {
                InterviewDashboardComponent(
                    recentQuizResults = recentQuizResults,
                    onShowToast = { msg -> onScheduleReminder(msg) }
                )
            } else {

            // Dashboard Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Apps",
                    value = "$totalApps",
                    icon = Icons.Default.Work,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    isSelected = selectedStatusFilter == "All",
                    onClick = { selectedStatusFilter = "All" },
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Incoming",
                    value = "${incomingInterviews.size}",
                    icon = Icons.Default.CalendarToday,
                    containerColor = Color(0xFFFFEDD5),
                    contentColor = Color(0xFFC2410C),
                    isSelected = selectedStatusFilter == "Incoming Interview",
                    onClick = { selectedStatusFilter = "Incoming Interview" },
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Offers",
                    value = "$offeredCount",
                    icon = Icons.Default.CheckCircle,
                    containerColor = Color(0xFFDCFCE7),
                    contentColor = Color(0xFF15803D),
                    isSelected = selectedStatusFilter == "Offered",
                    onClick = { selectedStatusFilter = "Offered" },
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Cancelled",
                    value = "$cancelledCount",
                    icon = Icons.Default.EventBusy,
                    containerColor = Color(0xFFF3E8FF),
                    contentColor = Color(0xFF7E22CE),
                    isSelected = selectedStatusFilter == "Cancelled",
                    onClick = { selectedStatusFilter = "Cancelled" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_search_input"),
                placeholder = { Text("Search by company, title or keywords...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusCategories) { status ->
                    val isSelected = selectedStatusFilter == status
                    val badgeColor = when (status) {
                        "Saved" -> Color(0xFF6B7280)
                        "Applied" -> Color(0xFF2563EB)
                        "Incoming Interview" -> Color(0xFFEA580C)
                        "Offered" -> Color(0xFF059669)
                        "Cancelled" -> Color(0xFF7E22CE)
                        "Rejected" -> Color(0xFFDC2626)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedStatusFilter = status }
                            .testTag("filter_chip_$status"),
                        color = if (isSelected) badgeColor else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Job List View
            if (filteredJobs.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No job applications found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Tap 'Add Job' to create a new application entry.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredJobs, key = { it.id }) { job ->
                        JobCardItem(
                            job = job,
                            onUpdateStatus = onUpdateStatus,
                            onDeleteJob = onDeleteJob,
                            onScheduleReminder = onScheduleReminder
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
            }
        }

        // Add Job Dialog Modal
        if (isAddJobDialogOpen) {
            AddJobDialog(
                currentTenant = currentTenant,
                onDismiss = { isAddJobDialogOpen = false },
                onSave = { newJob ->
                    onAddJob(newJob)
                    isAddJobDialogOpen = false
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("stat_card_${title.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) containerColor else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
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
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun JobCardItem(
    job: JobEntity,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteJob: (String) -> Unit,
    onScheduleReminder: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (job.status) {
        "Saved" -> Color(0xFF6B7280)
        "Applied" -> Color(0xFF2563EB)
        "Incoming Interview", "Interviewing" -> Color(0xFFEA580C)
        "Offered" -> Color(0xFF059669)
        "Cancelled" -> Color(0xFF7E22CE)
        "Rejected" -> Color(0xFFDC2626)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.jobTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = job.companyName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Status Change Dropdown Badge
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { menuExpanded = true }
                            .testTag("job_status_chip_${job.id}"),
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${job.status} ▾",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        listOf("Saved", "Applied", "Incoming Interview", "Offered", "Cancelled", "Rejected").forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(statusOption) },
                                onClick = {
                                    onUpdateStatus(job.id, statusOption)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Salary Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (job.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.location,
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                if (job.salary.isNotBlank()) {
                    Text(
                        text = job.salary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            if (job.interviewDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val isCancelled = job.status.equals("Cancelled", ignoreCase = true)
                val isIncoming = job.status.equals("Incoming Interview", ignoreCase = true) || job.status.equals("Interviewing", ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCancelled) Color(0xFFF3E8FF) else if (isIncoming) Color(0xFFFFF7ED) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCancelled) Icons.Default.EventBusy else Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (isCancelled) Color(0xFF7E22CE) else if (isIncoming) Color(0xFFEA580C) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCancelled) "Cancelled: ${job.interviewDate}" else "Interview: ${job.interviewDate}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCancelled) Color(0xFF6B21A8) else if (isIncoming) Color(0xFFC2410C) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (!isCancelled) {
                            IconButton(
                                onClick = { onScheduleReminder(job.companyName) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Schedule Reminder",
                                    tint = if (isIncoming) Color(0xFFEA580C) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (job.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = job.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onDeleteJob(job.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Job",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddJobDialog(
    currentTenant: String,
    onDismiss: () -> Unit,
    onSave: (JobEntity) -> Unit
) {
    var company by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Applied") }
    var salary by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var interviewDate by remember { mutableStateOf("") }

    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_job_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Job Application",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_company_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Dropdown
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { statusDropdownExpanded = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status: $status", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("▼", fontSize = 12.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        listOf("Saved", "Applied", "Incoming Interview", "Offered", "Cancelled", "Rejected").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    status = s
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary / Compensation") },
                    placeholder = { Text("$120,000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("Remote / New York") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = interviewDate,
                    onValueChange = { interviewDate = it },
                    label = { Text("Interview Schedule / Notes") },
                    placeholder = { Text("e.g., Fri, July 25 at 2:00 PM") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Next Steps") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (company.isNotBlank() && title.isNotBlank()) {
                                onSave(
                                    JobEntity(
                                        id = "job-${System.currentTimeMillis()}",
                                        companyName = company,
                                        jobTitle = title,
                                        status = status,
                                        salary = salary,
                                        location = location,
                                        notes = notes,
                                        interviewDate = interviewDate,
                                        tenantId = currentTenant
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_job_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Application")
                    }
                }
            }
        }
    }
}

