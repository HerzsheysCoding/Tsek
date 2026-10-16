package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckDao {
    @Query("SELECT * FROM check_history ORDER BY timestamp DESC")
    fun getAllChecks(): Flow<List<CheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: CheckEntity)

    @Query("DELETE FROM check_history WHERE id = :id")
    suspend fun deleteCheckById(id: String)

    @Query("DELETE FROM check_history")
    suspend fun clearAllChecks()

    @Query("SELECT * FROM scam_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<LocalReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: LocalReportEntity)

    @Query("SELECT * FROM user_account LIMIT 1")
    fun getUserAccount(): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccountEntity)

    @Query("DELETE FROM user_account")
    suspend fun clearUserAccount()
}
