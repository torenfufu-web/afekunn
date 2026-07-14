package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.CafeDatabase
import com.example.data.repository.CafeRepository

class CafeContainer(private val context: Context) {
    val database: CafeDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            CafeDatabase::class.java,
            "cafe_database.db"
        )
        .fallbackToDestructiveMigration() // Reset cleanly if schema changes in development
        .build()
    }

    val repository: CafeRepository by lazy {
        CafeRepository(
            memoDao = database.memoDao(),
            chatMessageDao = database.chatMessageDao()
        )
    }
}
