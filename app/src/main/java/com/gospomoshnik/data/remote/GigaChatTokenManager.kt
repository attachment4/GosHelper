package com.gospomoshnik.data.remote

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GigaChatTokenManager @Inject constructor(
    private val oAuthApi: GigaChatOAuthApi,
    @Named("gigachat_auth") private val basicAuth: String
) {
    private var accessToken: String = ""
    private var expiresAt: Long = 0L
    private val mutex = Mutex()

    suspend fun getBearer(): String {
        if (isValid()) return "Bearer $accessToken"
        // Один обновляющий запрос за раз — параллельные сообщения не дёргают OAuth
        mutex.withLock {
            if (!isValid()) refresh()
        }
        return "Bearer $accessToken"
    }

    private fun isValid() = accessToken.isNotEmpty() &&
        System.currentTimeMillis() < expiresAt - 60_000

    private suspend fun refresh() {
        val response = oAuthApi.getToken(
            basicAuth = "Basic $basicAuth",
            rqUid     = UUID.randomUUID().toString()
        )
        accessToken = response.access_token
        expiresAt   = response.expires_at
    }
}
