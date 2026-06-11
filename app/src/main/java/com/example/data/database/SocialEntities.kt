package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val userFullName: String,
    val avatarUrl: String,
    val imageUrl: String,
    val caption: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val viewsCount: Int = 0,
    val earningsEarned: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "India",
    val isUserPost: Boolean = false
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val username: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val upiId: String,
    val amount: Double,
    val status: String, // "Pending", "Approved"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "creator_profile")
data class CreatorProfileEntity(
    @PrimaryKey val username: String,
    val fullName: String,
    val bio: String,
    val avatarUrl: String,
    val walletBalance: Double,
    val totalEarnings: Double,
    val ratePerView: Double = 0.20 // ₹0.20 INR per view
)
