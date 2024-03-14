package com.geeksville.mesh.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import com.geeksville.mesh.model.AuthViewModel

@Composable
fun PinRegistrationScreen(authViewModel: AuthViewModel, onRegistrationComplete: () -> Unit) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register your PIN", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = pin,
            onValueChange = { pin = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Enter a PIN") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            authViewModel.savePinCode(pin)
            onRegistrationComplete()
        }) {
            Text("Register")
        }
    }
}

@Composable
fun PinLoginScreen(authViewModel: AuthViewModel, onLoginSuccess: () -> Unit) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome Back!", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = pin,
            onValueChange = { pin = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Enter your PIN") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (authViewModel.isPinValid(pin)) {
                onLoginSuccess()
            } else {
                // Show error message
            }
        }) {
            Text("Login")
        }
    }
}
