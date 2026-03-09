// Archivo: /src/main/java/com/example/testusoandroidstudio_1_usochicamocha/MainActivity.kt

package com.example.testusoandroidstudio_1_usochicamocha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan
import com.example.testusoandroidstudio_1_usochicamocha.ui.imprevisto.ImprevistoScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.form.FormScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.log.LogScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.login.LoginScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.login.LoginViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.main.MainScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.mantenimiento.MantenimientoScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta.MotocicletaScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta.MotoHubScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.selectionhub.SelectionHubScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.splash.SplashScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.splash.SplashViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.theme.AppUsoChicamochaTheme
import com.example.testusoandroidstudio_1_usochicamocha.util.NetworkMonitor
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.LocalSyncCoordinator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var localSyncCoordinator: LocalSyncCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppUsoChicamochaTheme {
                val networkStatus by networkMonitor.networkStatus.collectAsState()

                // Cuando la conexión se recupera, disparar sync de datos pendientes
                LaunchedEffect(Unit) {
                    localSyncCoordinator.schedulePeriodicMasterDataSync()
                    
                    snapshotFlow { networkStatus }
                        .distinctUntilChanged()
                        .scan(Pair(false, false)) { prev, curr -> Pair(prev.second, curr) }
                        .filter { (wasConnected, isConnected) -> !wasConnected && isConnected }
                        .collect {
                            localSyncCoordinator.coordinateSync(
                                LocalSyncCoordinator.SyncTrigger.FormSaved("reconnect")
                            )
                        }
                }

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        val splashViewModel: SplashViewModel = hiltViewModel()
                        SplashScreen(navController = navController, viewModel = splashViewModel)
                    }
                    composable("login") {
                        val loginViewModel: LoginViewModel = hiltViewModel()
                        Scaffold(
                            topBar = { ConnectionStatusTopBar(isConnected = networkStatus) }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = {
                                        navController.navigate("selection_hub") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                 )
                            }
                        }
                    }

                    composable("selection_hub") {
                        SelectionHubScreen(
                            networkStatus = networkStatus,
                            onNavigateToMaquinaria = {
                                navController.navigate("main")
                            },
                            onNavigateToMotocicletas = {
                                navController.navigate("motocicleta")
                            },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("selection_hub") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainScreen(
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("selection_hub") { inclusive = true }
                                }
                            },
                            onNavigateToLogs = {
                                navController.navigate("logs")
                            },
                            onNavigateToForm = {
                                navController.navigate("form")
                            },
                            onNavigateToImprevisto = {
                                navController.navigate("imprevisto")
                            },
                            onNavigateToMantenimiento = { maintenanceId ->
                                val route = if (maintenanceId != null) "mantenimiento?maintenanceId=$maintenanceId" else "mantenimiento"
                                navController.navigate(route)
                            }
                        )
                    }

                    composable("motocicleta") {
                        MotoHubScreen(
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToForm = {
                                navController.navigate("motocicleta_form")
                            }
                        )
                    }
                    composable("motocicleta_form") {
                        MotocicletaScreen(
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("form") {
                        FormScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("logs") {
                        LogScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("imprevisto") {
                        ImprevistoScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = "mantenimiento?maintenanceId={maintenanceId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("maintenanceId") {
                                type = androidx.navigation.NavType.IntType
                                defaultValue = -1 // Use -1 to indicate no ID
                            }
                        )
                    ) {
                        MantenimientoScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                }
            }
        }
    }
}
