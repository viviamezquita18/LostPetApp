package com.app.lostpetapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.first

class LocationPreference(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = "location"
    )

    suspend fun save(key: String, value: Double) {
        val dataStoreKey = preferencesKey<Double>(key)
        dataStore.edit { location ->
            location[dataStoreKey] = value

        }
    }

    suspend fun readData(key: String): Double {
        val dataStoreKey = preferencesKey<Double>(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey] ?: 0.0

    }
}