package com.pedrozc90.prototype.core.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pedrozc90.prototype.PrototypeApplication
import com.pedrozc90.prototype.ui.screens.home.HomeViewModel
import com.pedrozc90.prototype.ui.screens.login.LoginViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductDetailsViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductEntryViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductListViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductRemoteViewModel
import com.pedrozc90.prototype.ui.screens.settings.SettingsViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        // home
        initializer {
            HomeViewModel()
        }

        // settings
        initializer {
            SettingsViewModel(
                preferences = container().preferences
            )
        }

        // Devices
        initializer {
            DevicesViewModel(
                preferences = container().preferences,
                bluetooth = container().bluetooth
            )
        }

        // login
        initializer {
            LoginViewModel(
                preferences = container().preferences,
                repository = container().remote
            )
        }

        // products
        initializer {
            ProductListViewModel(
                repository = container().productRepository
            )
        }

        initializer {
            ProductRemoteViewModel(
                repository = container().remote
            )
        }

        initializer {
            ProductEntryViewModel(
                repository = container().productRepository
            )
        }

        initializer {
            ProductDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                repository = container().productRepository
            )
        }
    }

}

fun CreationExtras.application(): PrototypeApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PrototypeApplication)

fun CreationExtras.container(): AppContainer = application().container
