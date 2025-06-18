package com.example.learnui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learnui.DataClass.NavItem
import com.example.learnui.LocalModelsDao
import com.example.learnui.R
import com.example.learnui.ui.theme.Black


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier, dao: LocalModelsDao) {

    val uiColor =  if (isSystemInDarkTheme()) Color.White else Black


    val navItemList = listOf(
        NavItem("Home", rememberVectorPainter(Icons.Default.Home)),
        NavItem("Download", icon = painterResource(R.drawable.download)),
        NavItem("Settings", rememberVectorPainter(Icons.Default.Settings))
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }
    var showDialog by remember { mutableStateOf(false) }


    // Intercept back gesture or back press
    BackHandler {
        showDialog = true
    }

    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false }
        ){
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Do you want to exit?")
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("No")
                        }
                        TextButton(onClick = {
                            exitApp()
                        }) {
                            Text("Yes")
                        }
                    }
                }
            }
        }
    }

    //Ui
    Scaffold(
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(painter = navItem.icon, contentDescription = "icon", modifier = Modifier.size(22.dp))
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

        ContentScreen(selectedIndex, navController, dao, innerPadding)
    }
}

private fun exitApp() {
    android.os.Process.killProcess(android.os.Process.myPid())
}

@Composable
fun ContentScreen(
    selectedIndex: Int,
    navController: NavController,
    dao: LocalModelsDao,
    innerPadding: PaddingValues
) {
    Box(
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        when (selectedIndex) {
            0 -> HomePage(navController)
            1 -> DownloadPage(dao, navController)
            2 -> SettingsPage()
        }
    }

}
