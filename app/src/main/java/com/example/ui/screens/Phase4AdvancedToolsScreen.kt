package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AlumniUser
import com.example.data.ResumeEntity
import kotlinx.coroutines.launch

@Composable
fun Phase4AdvancedToolsScreen(
    resumes: List<ResumeEntity>,
    onAddResume: (String, String, String) -> Unit,
    onAnalyzeResume: suspend (String, String) -> ResumeEntity,
    onShowToast: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Resumes & AI ATS, 1: Alumni Connect, 2: Activity Log
    val subTabs = listOf("Resumes & AI ATS", "Alumni Connect", "Activity Log")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title, fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tools_subtab_$index")
                )
            }
        }

        when (selectedSubTab) {
            0 -> ResumesAndAiSection(
                resumes = resumes,
                onAddResume = onAddResume,
                onAnalyzeResume = onAnalyzeResume,
                onShowToast = onShowToast
            )
            1 -> AlumniConnectSection(onShowToast = onShowToast)
            2 -> ActivityLogSection()
        }
    }
}

@Composable
private fun ResumesAndAiSection(
    resumes: List<ResumeEntity>,
    onAddResume: (String, String, String) -> Unit,
    onAnalyzeResume: suspend (String, String) -> ResumeEntity,
    onShowToast: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isAddModalOpen by remember { mutableStateOf(false) }

    var selectedResumeForAnalysis by remember { mutableStateOf<ResumeEntity?>(resumes.firstOrNull()) }
    var jobDescriptionInput by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<ResumeEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // AI ATS Match Analyzer Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ats_analyzer_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini AI ATS Match Analyzer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select a saved resume profile and paste a job description to calculate match score & missing keywords.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Resume Dropdown
                var resumeDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { resumeDropdownExpanded = true }
                            .testTag("select_resume_dropdown"),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedResumeForAnalysis?.title ?: "Select Resume Profile",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text("▼", fontSize = 12.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = resumeDropdownExpanded,
                        onDismissRequest = { resumeDropdownExpanded = false }
                    ) {
                        resumes.forEach { res ->
                            DropdownMenuItem(
                                text = { Text(res.title) },
                                onClick = {
                                    selectedResumeForAnalysis = res
                                    resumeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = jobDescriptionInput,
                    onValueChange = { jobDescriptionInput = it },
                    placeholder = { Text("Paste Job Description here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jd_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (selectedResumeForAnalysis != null && jobDescriptionInput.isNotBlank()) {
                            isAnalyzing = true
                            coroutineScope.launch {
                                val result = onAnalyzeResume(selectedResumeForAnalysis!!.id, jobDescriptionInput)
                                analysisResult = result
                                isAnalyzing = false
                                onShowToast("AI Analysis Complete!")
                            }
                        } else {
                            onShowToast("Please select a resume and enter job description")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_ats_analysis_button"),
                    enabled = !isAnalyzing,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing with Gemini...")
                    } else {
                        Text("Run ATS Match Analysis")
                    }
                }
            }
        }

        // Analysis Results Output Card
        if (analysisResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Match Score: ${analysisResult!!.matchScore}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = analysisResult!!.feedback,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saved Resumes List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Saved Resumes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Button(
                onClick = { isAddModalOpen = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_resume_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Resume")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        resumes.forEach { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(res.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Target Role: ${res.targetRole}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res.content.take(120) + "...",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }

    if (isAddModalOpen) {
        var title by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { isAddModalOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Add New Resume Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Target Role") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Resume Content") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isAddModalOpen = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onAddResume(title, role, content)
                                    isAddModalOpen = false
                                }
                            }
                        ) { Text("Save Resume") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlumniConnectSection(onShowToast: (String) -> Unit) {
    val alumniList = listOf(
        AlumniUser("Siddharth Patel", "Google", "Staff Software Engineer", "2021"),
        AlumniUser("Neha Kulkarni", "Amazon", "Senior PM", "2022"),
        AlumniUser("Vikram Singh", "Microsoft", "Principal Engineer", "2020")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Alumni Directory", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(alumniList) { alumni ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(alumni.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("${alumni.role} @ ${alumni.company}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary))
                        Text("Class of ${alumni.graduationYear}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onShowToast("Connect request sent to ${alumni.name}!") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Request Mentoring")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLogSection() {
    val logs = listOf(
        "Logged in via Secure JWT Auth",
        "Submitted Daily Interview Challenge (+50 XP)",
        "Added new Job Application: 'TechCorp Inc'",
        "Updated Job Status to 'Interviewing'",
        "Ran Gemini AI ATS Resume Analysis"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Activity Log", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(log, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
