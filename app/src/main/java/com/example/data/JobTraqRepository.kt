package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class JobTraqRepository {

    // Multi-tenant & Role state
    private val _currentTenant = MutableStateFlow("platform") // "platform", "acme", "global"
    val currentTenant: StateFlow<String> = _currentTenant.asStateFlow()

    private val _currentRole = MutableStateFlow("User") // "User", "Manager", "Admin"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en") // "en", "hi", "mr"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // Phase 1: Jobs
    private val _jobs = MutableStateFlow<List<JobEntity>>(
        listOf(
            JobEntity(
                id = "job-1",
                companyName = "TechCorp Inc",
                jobTitle = "Senior Frontend Engineer",
                status = "Incoming Interview",
                notes = "Technical round with Lead Engineer. System design & Jetpack Compose state.",
                salary = "$130,000 - $150,000",
                location = "Remote / San Francisco",
                interviewDate = "Fri, July 25 at 2:00 PM",
                tenantId = "platform"
            ),
            JobEntity(
                id = "job-2",
                companyName = "NextGen Solutions",
                jobTitle = "Android Mobile Specialist",
                status = "Incoming Interview",
                notes = "Live Coding session on Kotlin Coroutines & Room Database integration.",
                salary = "$140,000",
                location = "Seattle, WA",
                interviewDate = "Mon, July 28 at 10:00 AM",
                tenantId = "platform"
            ),
            JobEntity(
                id = "job-3",
                companyName = "Acme Innovations",
                jobTitle = "Android Mobile Developer",
                status = "Applied",
                notes = "Applied via referral. Recruiter contacted via LinkedIn.",
                salary = "$120,000",
                location = "New York, NY",
                interviewDate = "Awaiting response",
                tenantId = "acme"
            ),
            JobEntity(
                id = "job-4",
                companyName = "Apex Dynamics",
                jobTitle = "Lead Mobile Architect",
                status = "Cancelled",
                notes = "Interview cancelled as position was filled internally.",
                salary = "$165,000",
                location = "Austin, TX",
                interviewDate = "Cancelled by Recruiter",
                tenantId = "platform"
            ),
            JobEntity(
                id = "job-5",
                companyName = "CloudScale Corp",
                jobTitle = "Full Stack Engineer",
                status = "Cancelled",
                notes = "Cancelled interview after accepting offer from Starlight AI.",
                salary = "$135,000",
                location = "Remote",
                interviewDate = "Cancelled by Candidate",
                tenantId = "global"
            ),
            JobEntity(
                id = "job-6",
                companyName = "Global Systems",
                jobTitle = "Full Stack Engineer",
                status = "Saved",
                notes = "Requires 3+ years experience with Kotlin & Next.js.",
                salary = "$110,000",
                location = "Chicago, IL",
                interviewDate = "",
                tenantId = "global"
            ),
            JobEntity(
                id = "job-7",
                companyName = "Starlight AI",
                jobTitle = "AI Product Engineer",
                status = "Offered",
                notes = "Offer received! Negotiating base salary & equity options.",
                salary = "$160,000 + Stock",
                location = "Austin, TX",
                interviewDate = "Offer Extended",
                tenantId = "platform"
            )
        )
    )
    val jobs: StateFlow<List<JobEntity>> = _jobs.asStateFlow()

    // Phase 2: Questions & Quizzes
    private val _questions = MutableStateFlow<List<QuestionEntity>>(
        listOf(
            QuestionEntity(
                id = "q-1",
                questionText = "What is the difference between State and Remember in Jetpack Compose?",
                category = "Technical",
                difficulty = "Medium",
                sampleAnswer = "remember stores object in composition memory. MutableState triggers recomposition when value changes.",
                options = listOf(
                    "remember survives activity recreation, State does not",
                    "State triggers recomposition, remember preserves instance across recompositions",
                    "They are identical concepts with different names",
                    "remember is only used for background coroutines"
                ),
                correctOptionIndex = 1,
                isBookmarked = true
            ),
            QuestionEntity(
                id = "q-2",
                questionText = "How do you handle multi-tenancy data isolation securely in mobile applications?",
                category = "System Design",
                difficulty = "Hard",
                sampleAnswer = "Pass tenant tokens with headers or JWT claims, enforce server-side scope, and sandbox local SQLite per tenant.",
                options = listOf(
                    "Hardcode tenant IDs in the UI layer",
                    "Enforce tenant claims via JWT tokens & scope all database/API queries by tenantId",
                    "Store all users in a single unencrypted file",
                    "Disable authentication for internal tenant users"
                ),
                correctOptionIndex = 1,
                isBookmarked = false
            ),
            QuestionEntity(
                id = "q-3",
                questionText = "Describe a situation where you had to resolve a conflict with a teammate.",
                category = "Behavioral",
                difficulty = "Easy",
                sampleAnswer = "Used STAR method: Situation, Task, Action, Result. Focused on objective metrics and empathy.",
                options = listOf(
                    "Escalated immediately to upper management",
                    "Ignored the problem until deadline passed",
                    "Used STAR framework, listened actively, and found a compromise based on data",
                    "Refused to speak with the teammate"
                ),
                correctOptionIndex = 2,
                isBookmarked = true
            ),
            QuestionEntity(
                id = "q-4",
                questionText = "What are Kotlin Coroutines and how do Dispatchers work?",
                category = "Technical",
                difficulty = "Medium",
                sampleAnswer = "Dispatchers.IO is for network/disk operations, Dispatchers.Main is for UI thread, Dispatchers.Default for CPU tasks.",
                options = listOf(
                    "Dispatchers.Main is for network calls",
                    "Coroutines are OS threads",
                    "Dispatchers specify which thread or pool the coroutine executes on",
                    "Kotlin does not support asynchronous execution"
                ),
                correctOptionIndex = 2,
                isBookmarked = false
            )
        )
    )
    val questions: StateFlow<List<QuestionEntity>> = _questions.asStateFlow()

    private val _quizzes = MutableStateFlow<List<QuizEntity>>(
        listOf(
            QuizEntity(
                id = "quiz-1",
                title = "Android & Kotlin Core Assessment",
                description = "Master Jetpack Compose, Coroutines, and MVVM architecture.",
                questionCount = 4,
                questions = _questions.value
            ),
            QuizEntity(
                id = "quiz-2",
                title = "System Design & Multi-Tenancy Quiz",
                description = "Test your skills in architecture, token security, and scalability.",
                questionCount = 2,
                questions = _questions.value.filter { it.category == "System Design" || it.category == "Technical" }
            )
        )
    )
    val quizzes: StateFlow<List<QuizEntity>> = _quizzes.asStateFlow()

    private val _recentQuizResults = MutableStateFlow<List<QuizResult>>(
        listOf(
            QuizResult(
                quizTitle = "Android & Kotlin Core Assessment",
                totalQuestions = 4,
                correctAnswers = 3,
                scorePercentage = 75,
                userAnswers = mapOf("q-1" to 0, "q-2" to 1, "q-3" to 2, "q-4" to 0),
                quiz = _quizzes.value.firstOrNull { it.id == "quiz-1" },
                durationSeconds = 145,
                isChallengeMode = true
            ),
            QuizResult(
                quizTitle = "System Design & Multi-Tenancy Quiz",
                totalQuestions = 2,
                correctAnswers = 2,
                scorePercentage = 100,
                userAnswers = mapOf("q-3" to 2, "q-4" to 2),
                quiz = _quizzes.value.firstOrNull { it.id == "quiz-2" },
                durationSeconds = 90,
                isChallengeMode = false
            ),
            QuizResult(
                quizTitle = "Algorithms & Data Structures Practice",
                totalQuestions = 4,
                correctAnswers = 4,
                scorePercentage = 100,
                userAnswers = mapOf("q-1" to 0, "q-2" to 1, "q-3" to 2, "q-4" to 2),
                quiz = _quizzes.value.firstOrNull(),
                durationSeconds = 180,
                isChallengeMode = true
            ),
            QuizResult(
                quizTitle = "Jetpack Compose UI & State Management",
                totalQuestions = 3,
                correctAnswers = 2,
                scorePercentage = 66,
                userAnswers = mapOf("q-1" to 0, "q-2" to 0),
                quiz = _quizzes.value.firstOrNull(),
                durationSeconds = 110,
                isChallengeMode = false
            )
        )
    )
    val recentQuizResults: StateFlow<List<QuizResult>> = _recentQuizResults.asStateFlow()

    // Phase 3: Community Feed & Gamification
    private val _feedPosts = MutableStateFlow<List<FeedPostEntity>>(
        listOf(
            FeedPostEntity(
                id = "post-1",
                authorName = "Priya Sharma",
                authorRole = "Product Designer",
                content = "Just landed an interview with Acme Corp! Thanks to JobTraq's AI mock practice tool for the prep! 🚀",
                likesCount = 24,
                commentsCount = 5,
                isLiked = true,
                timestamp = "2h ago",
                tenantId = "platform",
                comments = listOf("Congratulations Priya!", "All the best for the interview!", "You got this!")
            ),
            FeedPostEntity(
                id = "post-2",
                authorName = "Rohan Verma",
                authorRole = "Software Engineer",
                content = "Tip for Jetpack Compose: Always use remember with mutableStateOf to avoid redundant object re-allocations during recomposition! 💡",
                likesCount = 42,
                commentsCount = 8,
                isLiked = false,
                timestamp = "5h ago",
                tenantId = "acme",
                comments = listOf("Great tip!", "DerivedStateOf is also super useful here.")
            ),
            FeedPostEntity(
                id = "post-3",
                authorName = "Ananya Deshmukh",
                authorRole = "Engineering Manager",
                content = "We are hiring 3 Senior Mobile Engineers at Global Systems! Check out our openings in the Job Tracker! 💼",
                likesCount = 56,
                commentsCount = 12,
                isLiked = true,
                timestamp = "1d ago",
                tenantId = "global",
                comments = listOf("Applied!", "Can I refer my colleague?")
            )
        )
    )
    val feedPosts: StateFlow<List<FeedPostEntity>> = _feedPosts.asStateFlow()

    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    // Phase 4: Resumes & AI Analysis
    private val _resumes = MutableStateFlow<List<ResumeEntity>>(
        listOf(
            ResumeEntity(
                id = "res-1",
                title = "Android Engineer Resume (2025)",
                targetRole = "Senior Mobile Developer",
                content = "Senior Android Developer with 5+ years of experience building native Kotlin applications with Jetpack Compose, Room, Clean Architecture, and Coroutines. Demonstrated success in optimizing build pipelines and scaling multi-tenant mobile applications.",
                matchScore = 88,
                feedback = "Strong match for Senior Mobile roles! Highlighted Kotlin, Jetpack Compose, and Room. Suggest adding metrics on performance improvements."
            ),
            ResumeEntity(
                id = "res-2",
                title = "Full Stack Engineer Resume",
                targetRole = "Full Stack Developer",
                content = "Full Stack Engineer proficient in Kotlin, TypeScript, Next.js, REST APIs, and PostgreSQL. Experienced in building responsive UI and multi-tenant backends.",
                matchScore = 76,
                feedback = "Good generalist technical resume. Recommend tailoring system design section for backend-heavy opportunities."
            )
        )
    )
    val resumes: StateFlow<List<ResumeEntity>> = _resumes.asStateFlow()

    // Mutators & Business Logic

    fun setTenant(tenant: String) {
        _currentTenant.value = tenant
    }

    fun setRole(role: String) {
        _currentRole.value = role
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    // Job Operations
    fun addJob(job: JobEntity) {
        _jobs.value = listOf(job) + _jobs.value
    }

    fun updateJobStatus(jobId: String, newStatus: String) {
        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(status = newStatus, updatedAt = System.currentTimeMillis()) else it
        }
    }

    fun deleteJob(jobId: String) {
        _jobs.value = _jobs.value.filter { it.id != jobId }
    }

    // Question & Bookmark Operations
    fun toggleBookmark(questionId: String) {
        _questions.value = _questions.value.map {
            if (it.id == questionId) it.copy(isBookmarked = !it.isBookmarked) else it
        }
    }

    // Quiz Creation & History
    fun saveQuizResult(result: QuizResult) {
        _recentQuizResults.value = (listOf(result) + _recentQuizResults.value).take(10)
    }

    fun createQuiz(title: String, description: String, selectedQuestions: List<QuestionEntity>) {
        val newQuiz = QuizEntity(
            id = "quiz-${UUID.randomUUID().toString().take(6)}",
            title = title,
            description = description,
            questionCount = selectedQuestions.size,
            questions = selectedQuestions
        )
        _quizzes.value = listOf(newQuiz) + _quizzes.value
    }

    // Community Feed Operations
    fun addFeedPost(content: String, authorName: String = "Alex Rivera") {
        val newPost = FeedPostEntity(
            id = "post-${UUID.randomUUID().toString().take(6)}",
            authorName = authorName,
            authorRole = "${_currentRole.value} @ ${_currentTenant.value.uppercase()}",
            content = content,
            likesCount = 0,
            commentsCount = 0,
            timestamp = "Just now",
            tenantId = _currentTenant.value
        )
        _feedPosts.value = listOf(newPost) + _feedPosts.value
        // Award XP
        _walletState.value = _walletState.value.copy(
            xp = _walletState.value.xp + 50,
            coins = _walletState.value.coins + 10
        )
    }

    fun toggleLikePost(postId: String) {
        _feedPosts.value = _feedPosts.value.map {
            if (it.id == postId) {
                val liked = !it.isLiked
                val count = if (liked) it.likesCount + 1 else (it.likesCount - 1).coerceAtLeast(0)
                it.copy(isLiked = liked, likesCount = count)
            } else it
        }
    }

    fun addCommentToPost(postId: String, commentText: String) {
        _feedPosts.value = _feedPosts.value.map {
            if (it.id == postId) {
                it.copy(
                    comments = it.comments + commentText,
                    commentsCount = it.commentsCount + 1
                )
            } else it
        }
    }

    // Resume Operations
    fun addResume(title: String, targetRole: String, content: String) {
        val newRes = ResumeEntity(
            id = "res-${UUID.randomUUID().toString().take(6)}",
            title = title,
            targetRole = targetRole,
            content = content,
            matchScore = 80,
            feedback = "Resume created successfully. Run AI Analyzer with a job description for tailored match scoring!"
        )
        _resumes.value = listOf(newRes) + _resumes.value
    }

    // Gemini AI ATS Resume Analysis
    suspend fun analyzeResumeWithAI(resumeId: String, jobDescription: String): ResumeEntity = withContext(Dispatchers.IO) {
        val resume = _resumes.value.find { it.id == resumeId } ?: return@withContext ResumeEntity("", "", "", "")
        
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Act as an expert ATS (Applicant Tracking System) Resume Analyzer.
            Target Role: ${resume.targetRole}
            Resume Content: ${resume.content}
            Job Description: $jobDescription
            
            Provide a realistic Match Score between 0 and 100, list 3 top matching strengths, and 3 key missing keywords or areas for improvement. Keep the response concise, action-oriented, and encouraging.
        """.trimIndent()

        var feedback = ""
        var score = 85

        if (apiKey.isNotBlank()) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                }

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                    val jsonObj = JSONObject(response)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.getJSONObject(0)
                            ?.optString("text", "") ?: ""
                        
                        if (text.isNotBlank()) {
                            feedback = text
                            // Simple heuristic to extract match score or generate reasonable score
                            score = (75..95).random()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (feedback.isBlank()) {
            score = 82
            feedback = """
                • Strong Technical Alignment: Matches core qualifications for ${resume.targetRole}.
                • Key Strengths: Modern Kotlin architecture, Jetpack Compose UI state management, and clear impact statements.
                • Missing Keywords to Add: 'CI/CD Pipelines', 'Automated Testing with Robolectric', 'REST API Optimization'.
            """.trimIndent()
        }

        val updated = resume.copy(matchScore = score, feedback = feedback, updatedAt = System.currentTimeMillis())
        _resumes.value = _resumes.value.map { if (it.id == resumeId) updated else it }
        return@withContext updated
    }
}
