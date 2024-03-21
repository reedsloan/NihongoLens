package com.reedsloan.nihongolens.di

import android.app.Application
import androidx.room.Room
import com.github.wanasit.kotori.Tokenizer
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import com.reedsloan.nihongolens.data.data_source.AppDataDatabase
import com.reedsloan.nihongolens.data.repository.AppDataRepositoryImpl
import com.reedsloan.nihongolens.domain.repository.AppDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDataDatabase(app: Application): AppDataDatabase {
        return Room.databaseBuilder(
            app,
            AppDataDatabase::class.java,
            "app_data_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAppDataRepository(appDataDatabase: AppDataDatabase): AppDataRepository {
        return AppDataRepositoryImpl(appDataDatabase.dao)
    }

    @Provides
    @Singleton
    fun provideTokenizer() = Tokenizer.createDefaultTokenizer()
}