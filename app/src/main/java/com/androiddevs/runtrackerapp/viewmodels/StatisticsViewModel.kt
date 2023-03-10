package com.androiddevs.runtrackerapp.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.androiddevs.runtrackerapp.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
}