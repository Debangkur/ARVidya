package com.example.learnui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.learnui.loginAndsignup.LoginScreen
import com.example.learnui.ui.theme.LearnUiTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()

        setContent {
            LearnUiTheme {
                LoginScreen{ email, password ->
                    loginUser(email,password)
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    startActivity(Intent(this, MainPage::class.java))
                    finish()
                }else{
                    Toast.makeText(this,"Login failed",Toast.LENGTH_SHORT).show()
                }
            }
    }
}


