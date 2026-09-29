package com.example.sapworkpilot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_dashboard")
data class CachedDashboardEntity(
    @PrimaryKey val id: Int = 1, // single row, always overwritten
    val json: String,
    val cachedAt: Long
)