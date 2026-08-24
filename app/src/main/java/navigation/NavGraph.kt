package com.app.habitus.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.habitus.ui.screens.AddHabitScreen
import com.app.habitus.ui.screens.HabitDetailScreen
import com.app.habitus.ui.screens.HomeScreen
import com.app.habitus.ui.screens.StatsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddHabit : Screen("add_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Int) = "edit_habit/$habitId"
    }
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Int) = "habit_detail/$habitId"
    }
    object Stats : Screen("stats")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        // Antes este padding (que reserva espacio para la barra de
        // navegación inferior del Scaffold en MainActivity) no se aplicaba
        // a ningún destino, por lo que el contenido de varias pantallas
        // podía quedar oculto detrás de la barra inferior.
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddHabitClick = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onHabitClick = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }

        composable(Screen.AddHabit.route) {
            AddHabitScreen(
                onHabitSaved = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditHabit.route) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull()
            AddHabitScreen(
                habitId = habitId,
                onHabitSaved = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HabitDetail.route) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: 1

            HabitDetailScreen(
                habitId = habitId,
                onEditClick = {
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }


        composable(Screen.Stats.route) {
            StatsScreen()
        }
    }
}