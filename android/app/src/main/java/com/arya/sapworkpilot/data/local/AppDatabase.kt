package com.arya.sapworkpilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arya.sapworkpilot.data.local.entity.CachedDashboardDao
import com.arya.sapworkpilot.data.local.entity.CachedDashboardEntity
import com.arya.sapworkpilot.data.local.entity.ChatMessageDao
import com.arya.sapworkpilot.data.local.entity.ChatMessageEntity
import com.arya.sapworkpilot.data.local.entity.MeetingSummaryDao
import com.arya.sapworkpilot.data.local.entity.MeetingSummaryEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        CachedDashboardEntity::class,
        MeetingSummaryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun cachedDashboardDao(): CachedDashboardDao
    abstract fun meetingSummaryDao(): MeetingSummaryDao
}