package com.example

import android.app.Application
import com.example.di.CafeContainer

class CafeApplication : Application() {
    lateinit var container: CafeContainer

    override fun onCreate() {
        super.onCreate()
        container = CafeContainer(this)
    }
}
