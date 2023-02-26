package com.androiddevs.runningappyt.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.runningappyt.viewmodels.MainViewModel
import com.androiddevs.runningappyt.viewmodels.StatisticsViewModel
import com.example.runtrackerapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel : StatisticsViewModel by viewModels()
}