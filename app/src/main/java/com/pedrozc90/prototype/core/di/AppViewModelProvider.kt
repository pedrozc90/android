package com.pedrozc90.prototype.core.di

import android.util.Log
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pedrozc90.prototype.PrototypeApplication
import com.pedrozc90.prototype.ui.screens.debug.DebugViewModel
import com.pedrozc90.prototype.ui.screens.devices.DevicesViewModel
import com.pedrozc90.prototype.ui.screens.home.HomeViewModel
import com.pedrozc90.prototype.ui.screens.inventory.InventoryBasicViewModel
import com.pedrozc90.prototype.ui.screens.inventory.InventoryBatchViewModel
import com.pedrozc90.prototype.ui.screens.login.LoginViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductDetailsViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductEntryViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductListViewModel
import com.pedrozc90.prototype.ui.screens.products.ProductRemoteViewModel
import com.pedrozc90.prototype.ui.screens.settings.SettingsViewModel
import com.pedrozc90.rfid.core.RfidDevice

private const val TAG = "AppViewModelProvider"

object AppViewModelProvider {

    val Factory = viewModelFactory {
        // home
        initializer {
            HomeViewModel()
        }

        // settings
        initializer {
            SettingsViewModel(
                preferences = container().preferences,
                bluetooth = container().bluetooth,
                factory = container().manager,
                context = application()
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

        // inventory
        initializer {
            InventoryBasicViewModel(
                manager = container().manager,
                preferences = container().preferences,
                tagRepository = container().tagRepository,
                inventoryRepository = container().inventoryRepository
            )
        }

        initializer {
            InventoryBatchViewModel(
                device = device(),
                preferences = container().preferences,
                inventoryRepository = container().inventoryRepository,
                tagRepository = container().tagRepository
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

        // Debug
        initializer {
            DebugViewModel(
                preferences = container().preferences,
                device = device()
            )
        }
    }

}

fun CreationExtras.application(): PrototypeApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PrototypeApplication)

fun CreationExtras.container(): AppContainer = application().container

fun CreationExtras.device(): RfidDevice {
    // read cached value (synchronous). If null, fallback to a default "none" or fallback device
    val type = container().preferences.getDeviceType()
    val factory = container().manager
    val device = factory.build(type = type)
    Log.d(TAG, "Creating RfidDevice '${ device.hashCode() }' of type '$type'")
    return device
}
