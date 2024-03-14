package com.geeksville.mesh.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val prefs by lazy { app.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE) }

    fun savePinCode(pin: String) {
        prefs.edit().putString("user_pin", pin).apply()
    }

    fun isPinValid(pin: String): Boolean {
        val savedPin = prefs.getString("user_pin", "")
        return pin == savedPin
    }

    fun isPinSet(): Boolean {
        return prefs.contains("user_pin")
    }
}
