package com.example.timesaver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.timesaver.database.TimesaverDao
import com.example.timesaver.database.TimesaverDatabase

class MainViewModelFactory(private val dao: TimesaverDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}