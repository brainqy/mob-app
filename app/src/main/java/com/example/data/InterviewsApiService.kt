package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET

data class InterviewItem(
    val id: String,
    val companyName: String,
    val jobTitle: String,
    val status: String, // "Upcoming", "Completed", "Cancelled"
    val date: String,
    val time: String,
    val location: String,
    val interviewer: String,
    val notes: String,
    val tenantId: String = "platform"
)

data class InterviewStatusUpdate(
    val id: String,
    val interviewId: String,
    val companyName: String,
    val jobTitle: String,
    val previousStatus: String,
    val newStatus: String, // "Upcoming", "Completed", "Cancelled", "Scheduled", "Feedback Added"
    val updateMessage: String,
    val timestamp: String,
    val iconType: String = "UPDATE"
)

data class InterviewsResponse(
    val status: String = "success",
    val endpoint: String = "/api/v1/interviews",
    val totalInterviews: Int,
    val upcomingCount: Int,
    val completedCount: Int,
    val cancelledCount: Int,
    val interviews: List<InterviewItem>,
    val statusUpdates: List<InterviewStatusUpdate>
)

interface InterviewsApi {
    @GET("api/v1/interviews")
    suspend fun getInterviews(): Response<InterviewsResponse>
}

sealed class InterviewDashboardUiState {
    object Loading : InterviewDashboardUiState()
    data class Success(val data: InterviewsResponse) : InterviewDashboardUiState()
    data class Error(val message: String) : InterviewDashboardUiState()
}

class InterviewsRepository {

    private val _uiState = MutableStateFlow<InterviewDashboardUiState>(InterviewDashboardUiState.Loading)
    val uiState: StateFlow<InterviewDashboardUiState> = _uiState.asStateFlow()

    private val _lastRefreshed = MutableStateFlow<String>("Just now")
    val lastRefreshed: StateFlow<String> = _lastRefreshed.asStateFlow()

    suspend fun fetchInterviewsFromApi(): InterviewsResponse = withContext(Dispatchers.IO) {
        _uiState.value = InterviewDashboardUiState.Loading
        delay(400) // Simulate REST network latency for /api/v1/interviews call

        val sampleInterviews = listOf(
            InterviewItem(
                id = "int-101",
                companyName = "TechCorp Inc",
                jobTitle = "Senior Frontend Engineer",
                status = "Upcoming",
                date = "Fri, July 25, 2026",
                time = "2:00 PM PST",
                location = "Google Meet / Remote",
                interviewer = "Sarah Jenkins (Lead Engineer)",
                notes = "System Design & Jetpack Compose state architecture discussion."
            ),
            InterviewItem(
                id = "int-102",
                companyName = "NextGen Solutions",
                jobTitle = "Android Mobile Specialist",
                status = "Upcoming",
                date = "Mon, July 28, 2026",
                time = "10:00 AM EST",
                location = "Zoom Meeting",
                interviewer = "David Miller (Engineering Manager)",
                notes = "Live Coding session on Kotlin Coroutines & Room Database."
            ),
            InterviewItem(
                id = "int-103",
                companyName = "Starlight AI",
                jobTitle = "AI Product Engineer",
                status = "Completed",
                date = "Wed, July 16, 2026",
                time = "11:30 AM PST",
                location = "San Francisco HQ",
                interviewer = "Elena Rostova (VP of AI)",
                notes = "Final round behavioral and technical alignment. Offer extended!"
            ),
            InterviewItem(
                id = "int-104",
                companyName = "CloudScale Systems",
                jobTitle = "Backend Kotlin Engineer",
                status = "Completed",
                date = "Mon, July 14, 2026",
                time = "3:00 PM PST",
                location = "Microsoft Teams",
                interviewer = "Marcus Vance (Principal Architect)",
                notes = "Passed initial round. Awaiting final offer packet."
            ),
            InterviewItem(
                id = "int-105",
                companyName = "Apex Dynamics",
                jobTitle = "Lead Mobile Architect",
                status = "Cancelled",
                date = "Tue, July 21, 2026",
                time = "1:00 PM CST",
                location = "Phone Screen",
                interviewer = "Recruiting Team",
                notes = "Position closed as role filled internally by client."
            ),
            InterviewItem(
                id = "int-106",
                companyName = "Global Cloud Networks",
                jobTitle = "Staff Android Developer",
                status = "Cancelled",
                date = "Thu, July 17, 2026",
                time = "4:00 PM EST",
                location = "Google Meet",
                interviewer = "Alex Mercer (Hiring Manager)",
                notes = "Candidate cancelled interview due to competing offer acceptance."
            )
        )

        val sampleUpdates = listOf(
            InterviewStatusUpdate(
                id = "upd-1",
                interviewId = "int-101",
                companyName = "TechCorp Inc",
                jobTitle = "Senior Frontend Engineer",
                previousStatus = "Scheduled",
                newStatus = "Upcoming",
                updateMessage = "Technical round confirmed. Calendar invite & Google Meet link attached.",
                timestamp = "Today at 09:15 AM",
                iconType = "SCHEDULED"
            ),
            InterviewStatusUpdate(
                id = "upd-2",
                interviewId = "int-105",
                companyName = "Apex Dynamics",
                jobTitle = "Lead Mobile Architect",
                previousStatus = "Upcoming",
                newStatus = "Cancelled",
                updateMessage = "Interview cancelled by recruiter. Reason: Position filled internally.",
                timestamp = "Yesterday at 4:45 PM",
                iconType = "CANCELLED"
            ),
            InterviewStatusUpdate(
                id = "upd-3",
                interviewId = "int-103",
                companyName = "Starlight AI",
                jobTitle = "AI Product Engineer",
                previousStatus = "Upcoming",
                newStatus = "Completed",
                updateMessage = "Final interview round completed! Recruiter provided positive rating score.",
                timestamp = "July 16, 2026 at 1:00 PM",
                iconType = "COMPLETED"
            ),
            InterviewStatusUpdate(
                id = "upd-4",
                interviewId = "int-102",
                companyName = "NextGen Solutions",
                jobTitle = "Android Mobile Specialist",
                previousStatus = "Applied",
                newStatus = "Upcoming",
                updateMessage = "Recruiter scheduled Round 2 Live Coding for July 28.",
                timestamp = "July 15, 2026 at 11:20 AM",
                iconType = "SCHEDULED"
            ),
            InterviewStatusUpdate(
                id = "upd-5",
                interviewId = "int-106",
                companyName = "Global Cloud Networks",
                jobTitle = "Staff Android Developer",
                previousStatus = "Upcoming",
                newStatus = "Cancelled",
                updateMessage = "Withdrawal notice sent by candidate after accepting Starlight AI offer.",
                timestamp = "July 14, 2026 at 5:10 PM",
                iconType = "CANCELLED"
            ),
            InterviewStatusUpdate(
                id = "upd-6",
                interviewId = "int-104",
                companyName = "CloudScale Systems",
                jobTitle = "Backend Kotlin Engineer",
                previousStatus = "Interviewing",
                newStatus = "Completed",
                updateMessage = "Architecture round marked as Completed. Final decision pending.",
                timestamp = "July 14, 2026 at 4:00 PM",
                iconType = "COMPLETED"
            )
        )

        val upcoming = sampleInterviews.count { it.status.equals("Upcoming", ignoreCase = true) }
        val completed = sampleInterviews.count { it.status.equals("Completed", ignoreCase = true) }
        val cancelled = sampleInterviews.count { it.status.equals("Cancelled", ignoreCase = true) }

        val response = InterviewsResponse(
            totalInterviews = sampleInterviews.size,
            upcomingCount = upcoming,
            completedCount = completed,
            cancelledCount = cancelled,
            interviews = sampleInterviews,
            statusUpdates = sampleUpdates
        )

        _uiState.value = InterviewDashboardUiState.Success(response)
        _lastRefreshed.value = "Just now"
        response
    }
}
