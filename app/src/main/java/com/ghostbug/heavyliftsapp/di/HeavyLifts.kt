package com.ghostbug.heavyliftsapp.di

import android.app.Application

class HeavyLifts : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}