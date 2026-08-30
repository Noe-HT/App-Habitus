package com.app.habitus.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.habitus.R
import com.app.habitus.navigation.Screen
import com.app.habitus.ui.theme.Radius
import com.app.habitus.ui.theme.Spacing

@Composable
fun HabitusBottomNavigation(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val navBackground = MaterialTheme.colorScheme.surface
    val selectedPillColor = MaterialTheme.colorScheme.primaryContainer
    val selectedContentColor = MaterialTheme.colorScheme.primary
    val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    val labelHome = stringResource(R.string.nav_home)
    val labelAdd = stringResource(R.string.nav_add)
    val labelProgress = stringResource(R.string.nav_progress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(navBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavItem(
                label = labelHome,
                selected = currentRoute == Screen.Home.route,
                pillColor = selectedPillColor,
                selectedColor = selectedContentColor,
                unselectedColor = unselectedContentColor,
                icon = Icons.Filled.Home,
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
                pillColor = selectedPillColor,
                selectedColor = selectedContentColor,
                unselectedColor = unselectedContentColor,
                icon = Icons.Filled.Add,
                onClick = {
                    navController.navigate(Screen.AddHabit.route) {
                        launchSingleTop = true
                    }
                }
            )

            NavItem(
                label = labelProgress,
                selected = currentRoute == Screen.Stats.route,
                pillColor = selectedPillColor,
                selectedColor = selectedContentColor,
                unselectedColor = unselectedContentColor,
                icon = Icons.Filled.MoreVert,
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
    pillColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        label = "navItemColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) pillColor else Color.Transparent,
        label = "navItemBg"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.md))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.xs, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.md))
                .background(bgColor)
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm - 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}