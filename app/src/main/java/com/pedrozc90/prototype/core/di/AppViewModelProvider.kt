package com.pedrozc90.prototype.core.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pedrozc90.prototype.PrototypeApplication
import com.pedrozc90.prototype.ui.screens.home.HomeViewModel
import com.pedrozc90.prototype.ui.screens.settings.SettingsViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        // home
        initializer {
            HomeViewModel()
        }

        // settings
        initializer {
            SettingsViewModel()
        }
    }

}

fun CreationExtras.application(): PrototypeApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PrototypeApplication)

fun CreationExtras.container(): AppContainer = application().container
