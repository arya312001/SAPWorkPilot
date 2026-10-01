package com.arya.sapworkpilot.data.local.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedDashboardDao {

    @Query("SELECT * FROM cached_dashboard WHERE id = 1")
    suspend fun get(): CachedDashboardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: CachedDashboardEntity)
}