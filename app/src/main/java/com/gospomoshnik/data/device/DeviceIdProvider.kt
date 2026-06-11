package com.gospomoshnik.data.device

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.deviceStore: DataStore<Preferences> by preferencesDataStore(name = "device")

/**
 * Стабильный анонимный идентификатор устройства — привязывает подписку к
 * конкретному пользователю на бэкенде. Не содержит персональных данных:
 * случайный UUID, сгенерированный один раз при первом запуске.
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY = stringPreferencesKey("device_id")

    suspend fun get(): String {
        val existing = context.deviceStore.data.first()[KEY]
        if (existing != null) return existing
        val generated = UUID.randomUUID().toString()
        context.deviceStore.edit { it[KEY] = generated }
        return generated
    }
}
