package com.pedrozc90.prototype.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pedrozc90.prototype.PrototypeApplication
import com.pedrozc90.prototype.ui.screens.home.HomeViewModel
import com.pedrozc90.prototype.ui.screens.reader.ReaderViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        // home
        initializer {
            HomeViewModel()
        }

        // reader
        initializer {
            ReaderViewModel()
        }
    }

}

fun CreationExtras.application(): PrototypeApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PrototypeApplication)
