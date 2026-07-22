package com.example.data

data class JobEntity(
    val id: String,
    val companyName: String,
    val jobTitle: String,
    val status: String, // Saved, Applied, Interviewing, Offered, Rejected
    val notes: String = "",
    val salary: String = "",
    val location: String = "",
    val interviewDate: String = "",
    val tenantId: String = "platform",
    val updatedAt: Long = System.currentTimeMillis()
)

data class QuestionEntity(
    val id: String,
    val questionText: String,
    val category: String, // Technical, Behavioral, HR, System Design
    val difficulty: String, // Easy, Medium, Hard
    val sampleAnswer: String,
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val isBookmarked: Boolean = false
)

data class QuizEntity(
    val id: String,
    val title: String,
    val description: String,
    val questionCount: Int,
    val questions: List<QuestionEntity>
)

data class QuizResult(
    val quizTitle: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Int,
    val userAnswers: Map<String, Int> // questionId -> chosenIndex
)

data class FeedPostEntity(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: String = "Just now",
    val tenantId: String = "platform",
    val comments: List<String> = emptyList()
)

data class ResumeEntity(
    val id: String,
    val title: String,
    val targetRole: String,
    val content: String,
    val matchScore: Int = 0,
    val feedback: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class WalletState(
    val coins: Int = 500,
    val flashCoins: Int = 50,
    val streakDays: Int = 7,
    val xp: Int = 1250,
    val level: Int = 5,
    val badges: List<String> = listOf("Early Adopter", "Profile Pro", "7-Day Streak", "Quiz Master"),
    val referralCode: String = "JOBTRAQ-ALEX-2025"
)

data class AlumniUser(
    val name: String,
    val company: String,
    val role: String,
    val graduationYear: String,
    val availableForMentoring: Boolean = true
)
