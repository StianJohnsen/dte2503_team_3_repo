package com.example.dashcarr.di.modules

import com.example.dashcarr.data.datasource.Preferences
import com.example.dashcarr.domain.preferences.IPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface PreferencesModule {

    @Binds
    fun bindPreferences(preferences: Preferences): IPreferences
}