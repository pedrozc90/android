package com.pedrozc90.prototype

import android.app.Application
import com.pedrozc90.prototype.data.AppContainer
import com.pedrozc90.prototype.data.DefaultAppContainer

class PrototypeApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

}
