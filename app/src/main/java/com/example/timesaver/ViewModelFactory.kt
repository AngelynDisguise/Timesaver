package com.example.timesaver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.timesaver.database.TimesaverRepository
import com.example.timesaver.fragments.activity.logs.LogsViewModel

class ViewModelFactory(private val repository: TimesaverRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LogsViewModel::class.java) -> {
                LogsViewModel(repository) as T
            }
            // Add more ViewModel types as needed
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}