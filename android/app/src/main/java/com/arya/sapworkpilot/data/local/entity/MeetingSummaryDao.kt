package com.arya.sapworkpilot.data.local.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingSummaryDao {

    @Query("SELECT * FROM meeting_summaries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MeetingSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MeetingSummaryEntity)

    @Query("DELETE FROM meeting_summaries WHERE id = :id")
    suspend fun delete(id: String)
}