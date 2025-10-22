package com.pedrozc90.prototype.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pedrozc90.prototype.PrototypeApplication
import com.pedrozc90.prototype.data.AppContainer
import com.pedrozc90.prototype.ui.screens.devices.DevicesViewModel
import com.pedrozc90.prototype.ui.screens.home.HomeViewModel
import com.pedrozc90.prototype.ui.screens.reader.ReaderViewModel
import com.pedrozc90.prototype.ui.screens.readings.ReadDetailsViewModel
import com.pedrozc90.prototype.ui.screens.readings.ReadListViewModel
import com.pedrozc90.prototype.ui.screens.settings.SettingsViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        // home
        initializer {
            HomeViewModel()
        }

        // reader
        initializer {
            ReaderViewModel(
                tagRepository = container().tagRepository,
                readRepository = container().readRepository
            )
        }

        // readings
        initializer {
            ReadListViewModel(readRepository = container().readRepository)
        }

        initializer {
            ReadDetailsViewModel(
                readRepository = container().readRepository,
                savedStateHandle = this.createSavedStateHandle()
            )
        }

        // settings
        initializer {
            DevicesViewModel(
                bluetoothController = container().bluetoothController,
                preferencesRepository = container().preferencesRepository
            )
        }

        initializer {
            SettingsViewModel(
                tagRepository = container().tagRepository,
                readRepository = container().readRepository,
                preferencesRepository = container().preferencesRepository
            )
        }
    }

}

fun CreationExtras.application(): PrototypeApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PrototypeApplication)

fun CreationExtras.container(): AppContainer = application().container

// fun CreationExtras.tagRepository(): TagRepository = container().tagRepository
// fun CreationExtras.readRepository(): ReadRepository = container().readRepository
