package com.example.learnui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

                    composable(
                        route = "model?location={encodedLocation}&tts={encodedTts}",
                        arguments = listOf(
                            navArgument("location") {
                                type = NavType.StringType
                                nullable = true },
                            navArgument("tts") {
                                type = NavType.StringType
                                nullable = true }
                        )
                    ){ backStackEntry ->
                        val location = backStackEntry.arguments?.getString("location") ?: ""
                        val tts = backStackEntry.arguments?.getString("tts") ?: ""
                        ModelViewer(location,tts)
                    }
                }
            }
        }
    }


}
