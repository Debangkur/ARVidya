package com.example.learnui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learnui.ARCode.ModelViewer
import com.example.learnui.pages.MainScreen
import com.example.learnui.pages.TopicPage
import com.example.learnui.ui.theme.LearnUiTheme

class MainPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnUiTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ){
                    composable("main") {
                        MainScreen(navController)
                    }

                    composable("topics/{subject}"){ backStackEntry ->
                        val subjectName = backStackEntry.arguments?.getString("subject") ?: ""
                        TopicPage(subjectName, navController)
                    }

                    composable("model/{model}/{tts}") { backStackEntry ->
                        val model = backStackEntry.arguments?.getString("model") ?: ""
                        val tts = backStackEntry.arguments?.getString("tts") ?: ""
                        ModelViewer(model,tts)
                    }
                }
            }
        }
    }


}
