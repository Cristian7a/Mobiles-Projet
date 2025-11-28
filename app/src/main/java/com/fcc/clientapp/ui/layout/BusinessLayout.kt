package com.fcc.clientapp.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.fcc.clientapp.ui.components.AppNavbar
import com.fcc.clientapp.ui.components.BusinessDrawerContent
import com.fcc.clientapp.ui.theme.BackgroundLight
import kotlinx.coroutines.launch

@Composable
fun BusinessLayout(
    businessName: String,
    businessLogoUrl: String?, // <--- NUEVO
    userImageUrl: String?,
    currentRoute: String,
    onMenuOptionClick: (String) -> Unit,
    onNavigateDrawer: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BusinessDrawerContent(
                businessName = businessName,
                businessLogoUrl = businessLogoUrl, // <--- PASARLO AQUÍ
                currentRoute = currentRoute,
                onNavigate = onNavigateDrawer,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Scaffold(
            topBar = {
                AppNavbar(
                    profileImageUrl = userImageUrl,
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    onMenuOptionClick = onMenuOptionClick
                )
            },
            containerColor = BackgroundLight
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}