package com.example.dashcarr.presentation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: MultiDexApplication() {
    //val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alreadyLoggedIn")
}