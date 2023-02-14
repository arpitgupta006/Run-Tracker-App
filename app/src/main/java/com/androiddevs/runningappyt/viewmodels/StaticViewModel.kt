package com.androiddevs.runningappyt.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.androiddevs.runningappyt.repositories.MainRepository

class StaticViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
}