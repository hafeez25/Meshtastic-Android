package com.geeksville.mesh

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.geeksville.mesh.databinding.ActivityRegisterBinding
import com.geeksville.mesh.model.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val pin = binding.registerPinEditText.text.toString()
            val confirmPin = binding.confirmPinEditText.text.toString()

            if (pin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(this, "PIN cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin == confirmPin) {
                authViewModel.savePinCode(pin)
                finish()
                // Navigate to the main part of the application
            } else {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                binding.confirmPinEditText.error = "PIN does not match"
            }
        }



    }

}
