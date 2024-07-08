package com.julianczaja.stations.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.julianczaja.stations.data.StationsFileReaderImpl
import com.julianczaja.stations.data.local.database.AppDatabase
import com.julianczaja.stations.data.local.database.dao.StationDao
import com.julianczaja.stations.data.local.database.dao.StationKeywordDao
import com.julianczaja.stations.data.remote.KoleoApi
import com.julianczaja.stations.data.remote.VersionHeaderInterceptor
import com.julianczaja.stations.data.repository.StationKeywordRepositoryImpl
import com.julianczaja.stations.data.repository.StationRepositoryImpl
import com.julianczaja.stations.domain.StationsFileReader
import com.julianczaja.stations.domain.repository.StationKeywordRepository
import com.julianczaja.stations.domain.repository.StationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import javax.inject.Singleton

private const val BASE_URL = "https://koleo.pl/"
private const val DATABASE_NAME = "app_database"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideKoleoApi(json: Json): KoleoApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(
            OkHttpClient().newBuilder()
                // Fix for PROTOCOL_ERROR (https://github.com/square/okhttp/issues/3955)
                .protocols(listOf(Protocol.HTTP_1_1))
                .addInterceptor(VersionHeaderInterceptor())
                .build()
        )
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(KoleoApi::class.java)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideStationDao(appDatabase: AppDatabase) = appDatabase.stationDao()

    @Provides
    @Singleton
    fun provideStationKeywordDao(appDatabase: AppDatabase) = appDatabase.stationKeywordDao()

    @Provides
    @Singleton
    fun provideStationsFileReader(
        @ApplicationContext context: Context,
        json: Json,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): StationsFileReader = StationsFileReaderImpl(context, json, ioDispatcher)

    @Provides
    @Singleton
    fun provideStationRepository(
        api: KoleoApi,
        dao: StationDao
    ): StationRepository = StationRepositoryImpl(api, dao)

    @Provides
    @Singleton
    fun provideStationKeywordRepository(
        api: KoleoApi,
        dao: StationKeywordDao
    ): StationKeywordRepository = StationKeywordRepositoryImpl(api, dao)
}
