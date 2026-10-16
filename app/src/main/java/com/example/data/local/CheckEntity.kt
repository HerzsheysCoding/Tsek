package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_history")
data class CheckEntity(
    @PrimaryKey val id: String,
    val rawSnippet: String,
    val inputType: String,
    val verdict: String,
    val score: Int,
    val category: String,
    val explanationTl: String,
    val explanationEn: String,
    val redFlagCount: Int,
    val primaryDomain: String?,
    val primaryPhone: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scam_reports")
data class LocalReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: String, // "PHONE", "LINK", "MESSAGE"
    val targetValue: String,
    val category: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val supabaseSynced: Boolean,
    val syncMessage: String,
    val createdAt: Long = System.currentTimeMillis()
)
