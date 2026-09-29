package com.example.sapworkpilot.di

import android.content.Context
import androidx.room.Room
import com.example.sapworkpilot.data.local.AppDatabase
import com.example.sapworkpilot.data.local.entity.CachedDashboardDao
import com.example.sapworkpilot.data.local.entity.ChatMessageDao
import com.example.sapworkpilot.data.local.entity.MeetingSummaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "sapworkpilot.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun provideChatMessageDao(database: AppDatabase): ChatMessageDao = database.chatMessageDao()

    @Provides
    @Singleton
    fun provideCachedDashboardDao(database: AppDatabase): CachedDashboardDao = database.cachedDashboardDao()

    @Provides
    @Singleton
    fun provideMeetingSummaryDao(database: AppDatabase): MeetingSummaryDao = database.meetingSummaryDao()
}