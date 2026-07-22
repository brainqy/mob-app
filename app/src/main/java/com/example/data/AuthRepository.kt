package com.example.data

import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class AuthRepository(private val userDao: UserDao) {

    // Simple SHA-256 password hashing helper
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        passwordRaw: String,
        phone: String = "",
        avatarBadgeIndex: Int = 0
    ): Result<Long> {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        val newUser = UserEntity(
            fullName = fullName.trim(),
            email = trimmedEmail,
            passwordHash = hashPassword(passwordRaw),
            phone = phone.trim(),
            avatarBadgeIndex = avatarBadgeIndex
        )

        return try {
            val id = userDao.insertUser(newUser)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to register account: ${e.localizedMessage}"))
        }
    }

    suspend fun loginUser(email: String, passwordRaw: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("No account found with this email."))

        val hashedInput = hashPassword(passwordRaw)
        return if (user.passwordHash == hashedInput) {
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid password. Please try again."))
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email.trim().lowercase())
    }

    fun getUserFlow(userId: Long): Flow<UserEntity?> {
        return userDao.getUserByIdFlow(userId)
    }

    suspend fun resetPassword(email: String, newPasswordRaw: String): Result<Unit> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("No user found with email $email"))

        userDao.updatePassword(trimmedEmail, hashPassword(newPasswordRaw))
        return Result.success(Unit)
    }

    suspend fun updateUserProfile(user: UserEntity): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedDemoUserIfEmpty() {
        if (userDao.getUserCount() == 0) {
            registerUser(
                fullName = "Alex Morgan",
                email = "alex@example.com",
                passwordRaw = "Password123!",
                phone = "+1 (555) 019-2834",
                avatarBadgeIndex = 1
            )
        }
    }
}
