package com.modern.btourist.Database

import android.app.Application
import android.content.Context

class Btourist : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: Btourist
            private set
    }
}