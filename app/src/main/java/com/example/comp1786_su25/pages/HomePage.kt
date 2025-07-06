package com.example.comp1786_su25.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comp1786_su25.AuthState
import com.example.comp1786_su25.AuthViewModel
import com.example.comp1786_su25.Site_pages.ClassPage
import com.example.comp1786_su25.Site_pages.TeacherPage
import com.example.comp1786_su25.Site_pages.UserPage
import com.example.comp1786_su25.tabview.TabBarItem
import com.example.comp1786_su25.tabview.TabView

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("intro")
            else -> Unit
        }
    }

    // Create a nested NavController for the tab navigation
    val tabNavController = rememberNavController()

    val tabItems = listOf(
        TabBarItem(
            title = "class",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        ),
        TabBarItem(
            title = "teacher",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        ),
        TabBarItem(
            title = "user",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        )
    )

    Scaffold(
        bottomBar = {
            TabView(tabItems = tabItems, navController = tabNavController)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Nested NavHost for tab navigation
            NavHost(
                navController = tabNavController,
                startDestination = "class"
            ) {
                composable("class") {
                    ClassPage(modifier, navController)
                }
                composable("teacher") {
                    TeacherPage(modifier, navController)
                }
                composable("user") {
                    UserPage(modifier, navController)
                }
            }

            // Sign out button can be in a common location or moved to the user page
            TextButton(onClick = {
                authViewModel.logout()
            }) {
                Text("Sign Out")
            }
        }
    }
}
