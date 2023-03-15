package com.androiddevs.runtrackerapp.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.androiddevs.runtrackerapp.utils.Constants.KEY_NAME
import com.androiddevs.runtrackerapp.utils.Constants.KEY_WEIGHT
import com.example.runtrackerapp.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences : SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        loadFieldsFromSharedPref()

        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success){
                Snackbar.make(view , "Changes saved" , Snackbar.LENGTH_SHORT).show()
            } else{
                Snackbar.make(view , "Please fill out all the fields" , Snackbar.LENGTH_LONG).show()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
    private fun loadFieldsFromSharedPref(){
        val name = sharedPreferences.getString(KEY_NAME , "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT , 80f)

        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean{

        val nameText = etName.text.toString()
        val weightText = etWeight.text.toString()

        if (nameText.isEmpty() || weightText.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_NAME , nameText)
            .putFloat(KEY_WEIGHT , weightText.toFloat())
            .apply()

        val toolbartext = "Let's go $nameText"
        requireActivity().tvToolbarTitle.text = toolbartext
        return true
    }
}