package com.reedsloan.nihongolens.di

import android.app.Application
import androidx.room.Room
import com.atilika.kuromoji.ipadic.Tokenizer
import com.reedsloan.nihongolens.data.data_source.AppConfigurationDatabase
import com.reedsloan.nihongolens.data.repository.AppConfigurationRepositoryImpl
import com.reedsloan.nihongolens.data.repository.JMDictRepositoryImpl
import com.reedsloan.nihongolens.domain.repository.AppConfigurationRepository
import com.reedsloan.nihongolens.domain.repository.JMDictRepository
import com.reedsloan.nihongolens.domain.use_case.GetAppConfiguration
import com.reedsloan.nihongolens.domain.use_case.GetDictionary
import com.reedsloan.nihongolens.domain.use_case.UpdateAppConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object AppModule {
    @Provides
    @Singleton
    fun provideAppDataDatabase(app: Application): AppConfigurationDatabase {
        return Room.databaseBuilder(
            app,
            AppConfigurationDatabase::class.java,
            "app_configuration_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAppConfigurationRepository(appConfigurationDatabase: AppConfigurationDatabase): AppConfigurationRepository {
        return AppConfigurationRepositoryImpl(appConfigurationDatabase.dao)
    }

    @Provides
    @Singleton
    fun provideJmdictRepo(app: Application): JMDictRepository = JMDictRepositoryImpl(app.applicationContext)


    @Provides
    @Singleton
    fun provideUpdateAppConfigurationUseCase(appConfigurationRepository: AppConfigurationRepository) = UpdateAppConfiguration(appConfigurationRepository)

    @Provides
    @Singleton
    fun provideGetAppConfigurationUseCase(appConfigurationRepository: AppConfigurationRepository) = GetAppConfiguration(appConfigurationRepository)

    @Provides
    @Singleton
    fun getDictionaryUseCase(jmdictRepo: JMDictRepositoryImpl) = GetDictionary(jmdictRepo)

    @Provides
    @Singleton
    fun provideContext(app: Application) = app.applicationContext!!
}