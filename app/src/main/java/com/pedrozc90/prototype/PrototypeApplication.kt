package com.pedrozc90.prototype

import android.app.Application
import com.pedrozc90.prototype.core.di.AppContainer
import com.pedrozc90.prototype.core.di.DefaultAppContainer

class PrototypeApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

}
