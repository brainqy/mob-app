package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val avatarBadgeIndex: Int = 0,
    val isBiometricsEnabled: Boolean = true,
    val securityPin: String = "1234"
)
