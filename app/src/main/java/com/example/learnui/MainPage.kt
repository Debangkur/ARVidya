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
import com.example.learnui.ARCode.ARViewer
import com.example.learnui.ARCode.ModelViewer
import com.example.learnui.pages.MainScreen
import com.example.learnui.pages.TopicPage
import com.example.learnui.ui.theme.LearnUiTheme

class MainPage : ComponentActivity() {
    private lateinit var database: LocalModelDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = LocalModelDatabase.getDatabase(this)

        setContent {
            LearnUiTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ){
                    composable("main") {
                        MainScreen(navController, dao = database.dao)
                    }

                    composable("topics/{subject}"){ backStackEntry ->
                        val subjectName = backStackEntry.arguments?.getString("subject") ?: ""
                        TopicPage(subjectName, navController, database.dao)
                    }

                    composable(
                        route = "model/{location}/{tts}/{name}",
                        arguments = listOf(
                            navArgument(name = "location"){
                                type = NavType.StringType
                            },
                            navArgument(name = "tts"){
                                type = NavType.StringType
                            },
                            navArgument(name = "name"){
                                type = NavType.StringType
                            }
                        )
                    ){ backStackEntry ->
                        val location = backStackEntry.arguments?.getString("location") ?: ""
                        val tts = backStackEntry.arguments?.getString("tts") ?: ""
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        ModelViewer(location,tts,name, navController)
                    }

                    composable(
                        route = "ar/{location}/{tts}/{name}",
                        arguments = listOf(
                            navArgument(name = "location"){
                                type = NavType.StringType
                            },
                            navArgument(name = "tts"){
                                type = NavType.StringType
                            },
                            navArgument(name = "name"){
                                type = NavType.StringType
                            }
                        )
                    ){ backStackEntry ->
                        val location = backStackEntry.arguments?.getString("location") ?: ""
                        val tts = backStackEntry.arguments?.getString("tts") ?: ""
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        ARViewer(location,tts,name, navController)
                    }

                }
            }
        }
    }


}
