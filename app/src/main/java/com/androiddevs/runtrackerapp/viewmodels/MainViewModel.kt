package com.androiddevs.runtrackerapp.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runtrackerapp.db.Run
import com.androiddevs.runtrackerapp.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}