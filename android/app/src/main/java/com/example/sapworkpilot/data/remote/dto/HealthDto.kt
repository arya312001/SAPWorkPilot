package com.example.sapworkpilot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HealthDto(
    val status: String,
    val service: String,
    val version: String,
    @SerializedName("jira_configured") val jiraConfigured: Boolean = false,
    @SerializedName("llm_configured") val llmConfigured: Boolean = false
)