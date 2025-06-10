package com.example.learnui.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnui.DataClass.NavItem
import com.example.learnui.LocalModelsDao

@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier, dao: LocalModelsDao) {

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Download", Icons.Default.Favorite),
        NavItem("Settings", Icons.Default.Settings)
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }
    Scaffold(
        topBar = {
            Text(text = "Subjects", style = MaterialTheme.typography.headlineLarge)
        },
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = "icon")
                        },
                        label = {
                            Text(navItem.label)
                        }
                    )
                }
            }
        }
    )
    { innerPadding ->

        ContentScreen(modifier = Modifier.padding(innerPadding), selectedIndex, navController, dao)
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    navController: NavController,
    dao: LocalModelsDao
) {
    when(selectedIndex){
        0 -> HomePage(navController)
        1 -> DownloadPage(modifier,dao, navController)
        2 -> SettingsPage()
    }

}
