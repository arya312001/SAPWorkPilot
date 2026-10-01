package com.arya.sapworkpilot.data.remote

import com.arya.sapworkpilot.data.local.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class DynamicBaseUrlInterceptor @Inject constructor(
    private val settingsStore: SettingsStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val savedUrl = runBlocking { settingsStore.baseUrl.first() }
        val savedHttpUrl = savedUrl.toHttpUrl()

        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .scheme(savedHttpUrl.scheme)
            .host(savedHttpUrl.host)
            .port(savedHttpUrl.port)
            .build()

        val newRequest = original.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}