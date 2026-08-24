package com.app.habitus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.habitus.R
import com.app.habitus.navigation.Screen

@Composable
fun HabitusBottomNavigation(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val navBackground = MaterialTheme.colorScheme.surfaceVariant
    val navIcon = MaterialTheme.colorScheme.onSurfaceVariant
    val navSelected = MaterialTheme.colorScheme.outline
    val labelHome = stringResource(R.string.nav_home)
    val labelAdd = stringResource(R.string.nav_add)
    val labelProgress = stringResource(R.string.nav_progress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(navBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavItem(
                label = labelHome,
                selected = currentRoute == Screen.Home.route,
                navBackground = navBackground,
                navIcon = navIcon,
                navSelected = navSelected,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = labelHome,
                        modifier = Modifier.size(18.dp),
                        tint = navIcon
                    )
                },
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            NavItem(
                label = labelAdd,
                selected = currentRoute == Screen.AddHabit.route,
                navBackground = navBackground,
                navIcon = navIcon,
                navSelected = navSelected,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = labelAdd,
                        modifier = Modifier.size(18.dp),
                        tint = navIcon
                    )
                },
                onClick = {
                    navController.navigate(Screen.AddHabit.route) {
                        launchSingleTop = true
                    }
                }
            )

            NavItem(
                label = labelProgress,
                selected = currentRoute == Screen.Stats.route,
                navBackground = navBackground,
                navIcon = navIcon,
                navSelected = navSelected,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = labelProgress,
                        modifier = Modifier.size(18.dp),
                        tint = navIcon
                    )
                },
                onClick = {
                    navController.navigate(Screen.Stats.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    navBackground: androidx.compose.ui.graphics.Color,
    navIcon: androidx.compose.ui.graphics.Color,
    navSelected: androidx.compose.ui.graphics.Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) navSelected else navBackground)
                .padding(horizontal = 16.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = navIcon,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}