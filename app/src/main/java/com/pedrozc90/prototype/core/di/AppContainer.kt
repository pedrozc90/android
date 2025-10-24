package com.pedrozc90.prototype.core.di

import android.content.Context

interface AppContainer {
    // define your singletons here

}

class DefaultAppContainer(context: Context) : AppContainer {
    // initialize your singletons here
}
