package com.example.learnui.pages

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    {
        ContentScreen(selectedIndex, navController, dao)
    }
}

@Composable
fun ContentScreen(
    selectedIndex: Int,
    navController: NavController,
    dao: LocalModelsDao
) {
    Box() {
        when (selectedIndex) {
            0 -> HomePage(navController)
            1 -> DownloadPage(dao, navController)
            2 -> SettingsPage()
        }
    }

}
