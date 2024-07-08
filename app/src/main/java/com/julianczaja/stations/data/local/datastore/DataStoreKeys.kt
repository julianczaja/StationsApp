package com.julianczaja.stations.data.local.datastore

import androidx.datastore.preferences.core.longPreferencesKey

object DataStoreKeys {
    val LAST_DATA_UPDATE_KEY = longPreferencesKey("last_data_update_key")
}
