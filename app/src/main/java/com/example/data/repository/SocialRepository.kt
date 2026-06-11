package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class SocialRepository(private val socialDao: SocialDao) {

    val allPosts: Flow<List<PostEntity>> = socialDao.getAllPosts()
    
    val profile: Flow<CreatorProfileEntity?> = socialDao.getProfileFlow()
    
    val allWithdrawals: Flow<List<WithdrawalEntity>> = socialDao.getAllWithdrawals()

    suspend fun getProfileSync(): CreatorProfileEntity? {
        return socialDao.getProfile()
    }

    suspend fun insertProfile(profile: CreatorProfileEntity) {
        socialDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: CreatorProfileEntity) {
        socialDao.updateProfile(profile)
    }

    suspend fun createPost(post: PostEntity): Long {
        return socialDao.insertPost(post)
    }

    suspend fun updatePost(post: PostEntity) {
        socialDao.updatePost(post)
    }

    fun getCommentsForPost(postId: Int): Flow<List<CommentEntity>> {
        return socialDao.getCommentsForPost(postId)
    }

    suspend fun addComment(comment: CommentEntity) {
        socialDao.insertComment(comment)
    }

    suspend fun requestWithdrawal(withdrawal: WithdrawalEntity) {
        socialDao.insertWithdrawal(withdrawal)
    }
}
