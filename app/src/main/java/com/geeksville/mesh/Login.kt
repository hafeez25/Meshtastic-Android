package com.geeksville.mesh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.geeksville.mesh.databinding.ActivityLoginBinding
import com.geeksville.mesh.model.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Automatically open the keyboard for pinEditText
        binding.pinEditText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.pinEditText, InputMethodManager.SHOW_IMPLICIT)

        binding.loginButton.setOnClickListener {
            val pin = binding.pinEditText.text.toString()
            if (authViewModel.isPinValid(pin)) {
                // Correct PIN
                navigateToMainContent()
                finish() // Optionally finish the Login Activity if no need to return here
            } else {
                // Incorrect PIN
                binding.pinEditText.error = "Invalid PIN"
                // Optionally, clear the input field for re-entry
                binding.pinEditText.text = null
            }
        }
    }

    private fun navigateToMainContent() {
        // Assuming MainActivity is the "main content" of your app
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        // Hide the keyboard before navigating away
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        // Optional: provide feedback or close the app
    }
}
