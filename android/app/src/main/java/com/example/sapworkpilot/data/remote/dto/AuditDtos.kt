package com.example.sapworkpilot.data.remote.dto

data class AuditEntryDto(
    val timestamp: Double,
    val user: String,
    val action: String,
    val detail: String
)