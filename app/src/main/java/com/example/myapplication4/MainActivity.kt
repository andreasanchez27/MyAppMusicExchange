package com.example.myapplication4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication4.ui.theme.MyApplication4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication4Theme {
                // Eliminamos el Scaffold de aquí y dejamos solo el de ProgramaPrincipal
                ProgramaPrincipal()
            }
        }
    }
}

@Composable
fun ProgramaPrincipal() {
    val navController = rememberNavController()

    // Un solo Scaffold para toda la aplicación
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                appItems = listOf(Destination.Compare, Destination.History, Destination.Settings, Destination.ListAll)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AppNavigation(navController = navController)
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val compareHistory = remember { mutableStateListOf<String>() }
    val fontSize = rememberSaveable { mutableStateOf(18f) }
    val textColor = rememberSaveable { mutableStateOf("Black") }

    NavHost(navController, startDestination = Destination.Compare.route) {
        composable(Destination.Compare.route) {
            CompareScreen(
                history = compareHistory,
                fontSize = fontSize.value,
                textColor = textColor.value
            ) }
        composable(Destination.History.route) {
            HistoryScreen(
                history = compareHistory,
                fontSize = fontSize.value,
                textColor = textColor.value
        ) }
        composable(Destination.Settings.route) {
            SettingScreen(
                fontSize = fontSize,
                textColor = textColor
            ) }
        composable(Destination.ListAll.route) { ListAllScreen() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, appItems: List<Destination>) {
    // Cambiamos BottomNavigation por NavigationBar (Material 3)
    NavigationBar(
        containerColor = colorResource(id = R.color.purple_700)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        appItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = item.icon),
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title, fontSize = 12.sp) },
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                // En Material 3 usamos NavigationBarItemDefaults para los colores
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White.copy(alpha = 0.4f),
                    indicatorColor = Color.Transparent // O el color que desees para el fondo circular al seleccionar
                ),
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


