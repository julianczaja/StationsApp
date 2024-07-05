package com.julianczaja.stations.di

import android.content.Context
import androidx.room.Room
import com.julianczaja.stations.data.StationsFileReaderImpl
import com.julianczaja.stations.data.local.database.AppDatabase
import com.julianczaja.stations.domain.StationsFileReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "app_database"
    ).build()

    @Provides
    @Singleton
    fun provideStationDao(appDatabase: AppDatabase) = appDatabase.stationDao()

    @Provides
    @Singleton
    fun provideStationKeywordDao(appDatabase: AppDatabase) = appDatabase.stationKeywordDao()

    @Provides
    @Singleton
    fun proviceStationsFileReader(
        @ApplicationContext context: Context,
        json: Json,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): StationsFileReader = StationsFileReaderImpl(context, json, ioDispatcher)
}
