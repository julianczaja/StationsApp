package com.julianczaja.stations.data.local.database.dao

import androidx.room.Insert
import androidx.room.Transaction

interface BaseDao<T> {

    @Insert
    suspend fun insertAll(items: List<T>)

    @Transaction
    suspend fun withTransaction(tx: suspend () -> Unit) = tx()
}
