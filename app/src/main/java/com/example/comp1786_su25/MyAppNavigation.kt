package com.example.comp1786_su25

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.comp1786_su25.functionPages.Class.AddClassScreen
import com.example.comp1786_su25.functionPages.Class.UpdateClassScreen
import com.example.comp1786_su25.functionPages.Teacher.AddTeacherScreen
import com.example.comp1786_su25.functionPages.Teacher.UpdateTeacherScreen
import com.example.comp1786_su25.functionPages.User.UpdateUserScreen
import com.example.comp1786_su25.pages.HomePage
import com.example.comp1786_su25.pages.IntroPage
import com.example.comp1786_su25.pages.LoginPage
import com.example.comp1786_su25.pages.SigninPage

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") {
            IntroPage(modifier, navController, authViewModel)
        }
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SigninPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("addclass") {
            AddClassScreen(modifier, navController)
        }
        composable(
            route = "updateclass/{classId}",
            arguments = listOf(
                navArgument("classId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId")
            UpdateClassScreen(modifier, navController, classId)
        }
        composable("addteacher") {
             AddTeacherScreen(modifier, navController) // Uncomment when AddTeacherScreen is implemented
        }
        composable(
            route = "updateteacher/{teacherId}",
            arguments = listOf(
                navArgument("teacherId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId")
            UpdateTeacherScreen( navController, teacherId)
        }
        composable(
            route = "updateuser/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UpdateUserScreen(modifier ,navController, userId)
        }
    }
}