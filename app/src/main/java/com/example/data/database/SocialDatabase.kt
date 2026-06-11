package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PostEntity::class, 
        CommentEntity::class, 
        WithdrawalEntity::class, 
        CreatorProfileEntity::class
    ], 
    version = 1, 
    exportSchema = false
)
abstract class SocialDatabase : RoomDatabase() {
    abstract fun socialDao(): SocialDao

    companion object {
        @Volatile
        private var INSTANCE: SocialDatabase? = null

        fun getDatabase(context: Context): SocialDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SocialDatabase::class.java,
                    "instaearn_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
