package com.gospomoshnik.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gospomoshnik.domain.model.SubscriptionStatus
import com.gospomoshnik.domain.repository.SubscriptionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subscription")

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SubscriptionRepository {

    private object Keys {
        val IS_PRO          = booleanPreferencesKey("is_pro")
        val REQUESTS_USED   = intPreferencesKey("requests_used")
        val EXPIRES_AT      = longPreferencesKey("expires_at")
        val USAGE_MONTH     = stringPreferencesKey("usage_month")   // "2026-06"
    }

    private fun currentMonth(): String =
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    override fun getStatus(): Flow<SubscriptionStatus> =
        context.dataStore.data.map { prefs ->
            val isPro       = prefs[Keys.IS_PRO]        ?: false
            val savedMonth  = prefs[Keys.USAGE_MONTH]   ?: currentMonth()
            // Новый месяц — счётчик логически обнуляется (физически — при первом запросе)
            val used        = if (savedMonth == currentMonth()) prefs[Keys.REQUESTS_USED] ?: 0 else 0
            val expiresAt   = prefs[Keys.EXPIRES_AT]

            // Автоматически снять Pro если истёк срок
            val proActive = isPro && (expiresAt == null || expiresAt > System.currentTimeMillis())

            SubscriptionStatus(
                isPro          = proActive,
                requestsUsed   = used,
                requestsLimit  = if (proActive) Int.MAX_VALUE else 10,
                expiresAt      = expiresAt
            )
        }

    override suspend fun getStatusOnce(): SubscriptionStatus = getStatus().first()

    override suspend fun incrementUsage() {
        context.dataStore.edit { prefs ->
            val month = currentMonth()
            if (prefs[Keys.USAGE_MONTH] != month) {
                // Первый запрос в новом месяце — сброс счётчика
                prefs[Keys.USAGE_MONTH]   = month
                prefs[Keys.REQUESTS_USED] = 1
            } else {
                prefs[Keys.REQUESTS_USED] = (prefs[Keys.REQUESTS_USED] ?: 0) + 1
            }
        }
    }

    override suspend fun resetMonthlyUsage() {
        context.dataStore.edit { prefs ->
            prefs[Keys.USAGE_MONTH]   = currentMonth()
            prefs[Keys.REQUESTS_USED] = 0
        }
    }

    override suspend fun activatePro(expiresAt: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PRO]      = true
            prefs[Keys.EXPIRES_AT]  = expiresAt
            prefs[Keys.REQUESTS_USED] = 0
        }
    }

    override suspend fun deactivatePro() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PRO] = false
            prefs.remove(Keys.EXPIRES_AT)
        }
    }
}
