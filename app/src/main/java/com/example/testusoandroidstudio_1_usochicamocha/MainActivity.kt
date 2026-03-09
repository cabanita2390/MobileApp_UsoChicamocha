// Archivo: /src/main/java/com/example/testusoandroidstudio_1_usochicamocha/MainActivity.kt

package com.example.testusoandroidstudio_1_usochicamocha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testusoandroidstudio_1_usochicamocha.ui.home.HomeScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.home.HomeViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.imprevisto.ImprevistoScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.form.FormScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.log.LogScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.login.LoginScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.login.LoginViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.main.MainScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.mantenimiento.MantenimientoScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.motos.MotosScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.splash.SplashScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.splash.SplashViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.theme.AppUsoChicamochaTheme
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoMainScreen
import com.example.testusoandroidstudio_1_usochicamocha.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Temporary fix for Compose clickable issue
        ComposeFoundationFlags.isNonComposedClickableEnabled = false

        setContent {
            AppUsoChicamochaTheme {
                val networkStatus by networkMonitor.networkStatus.collectAsState()

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
                                        navController.navigate("home") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                 )
                            }
                        }
                    }
                    composable("home") {
                        val homeViewModel: HomeViewModel = hiltViewModel()
                        HomeScreen(
                            networkStatus = networkStatus,
                            viewModel = homeViewModel,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToMaquinaria = {
                                navController.navigate("main")
                            },
                            onNavigateToVehicular = {
                                navController.navigate("vehiculo_main")
                            },
                            onNavigateToMotos = {
                                navController.navigate("motos")
                            }
                        )
                    }
                    composable("main") {
                        MainScreen(
                            networkStatus = networkStatus,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateBack = {
                                navController.popBackStack()
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
                    composable("vehiculo_main") {
                        VehiculoMainScreen(
                            networkStatus = networkStatus,
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToForm = {
                                navController.navigate("vehiculo")
                            }
                        )
                    }
                    composable("vehiculo") {
                        VehiculoScreen(
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("motos") {
                        MotosScreen(
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
