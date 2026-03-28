package com.example.heavyLifts.di

import android.app.Application

class Demo103App : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}