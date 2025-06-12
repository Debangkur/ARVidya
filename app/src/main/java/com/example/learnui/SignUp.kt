
package com.example.learnui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.learnui.loginAndsignup.SignUpScreen
import com.example.learnui.ui.theme.LearnUiTheme
import com.google.firebase.auth.FirebaseAuth

class SignUp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnUiTheme {
                SignUpScreen {email,password ->
                    signUpUser(email,password)
                }
            }
        }
    }

    private fun signUpUser(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainPage::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "SignUp failed failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_LONG).show()
        }
    }
}

