package com.example.dashcarr.di.modules

import android.content.Context
import com.example.dashcarr.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
class DBModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = AppDatabase.getInstance(app)

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase) = db.pointsDao()

}