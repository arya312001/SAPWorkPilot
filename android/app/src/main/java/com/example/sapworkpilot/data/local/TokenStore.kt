package com.example.sapworkpilot.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("access_token")
    private val roleKey = stringPreferencesKey("role")

    val token: Flow<String?> = context.authDataStore.data.map { it[tokenKey] }
    val role: Flow<String?> = context.authDataStore.data.map { it[roleKey] }

    suspend fun save(token: String, role: String) {
        context.authDataStore.edit {
            it[tokenKey] = token
            it[roleKey] = role
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}