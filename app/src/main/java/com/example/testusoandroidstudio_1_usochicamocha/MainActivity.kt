package com.example.testusoandroidstudio_1_usochicamocha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.testusoandroidstudio_1_usochicamocha.ui.home.HomeScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.home.HomeViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.imprevisto.ImprevistoScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.form.FormScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.log.LogScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.login.LoginScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.login.LoginViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.main.MainScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.maquinaria.MaquinariaCambioAceiteScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta.MotocicletaScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta.MotoHubScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.motocicleta.MotoCambioAceiteScreen

import com.example.testusoandroidstudio_1_usochicamocha.ui.shared.ConnectionStatusTopBar
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.captura.CapturaScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.cola.ColaScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.cronograma.CronogramaScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.detalle.DetalleScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.home.SubestacionHomeScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.subestacion.pendientes.PendientesScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.splash.SplashScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.splash.SplashViewModel
import com.example.testusoandroidstudio_1_usochicamocha.ui.theme.AppUsoChicamochaTheme
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoMainScreen
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoCambioAceiteScreen
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
        // Sin esto, el sistema pinta la barra de estado/navegación con un color propio
        // (no transparente) por encima del contenido de Compose, aunque el layout ya
        // dibuje edge-to-edge (forzado desde Android 15 / targetSdk 35). Con esto, el
        // fondo de cada pantalla se ve continuo hasta el borde real de la pantalla.
        enableEdgeToEdge()

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
                                navController.navigate("motocicleta")
                            },
                            onNavigateToCambioAceiteMaquinaria = {
                                navController.navigate("maquinaria_cambio_aceite")
                            },
                            onNavigateToCambioAceiteVehicular = {
                                navController.navigate("vehiculo_cambio_aceite")
                            },
                            onNavigateToSubestaciones = {
                                navController.navigate("subestaciones_home")
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
                                    popUpTo("home") { inclusive = true }
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
                            onNavigateToMaquinariaCambioAceite = { machineOilChangeId ->
                                val route = if (machineOilChangeId != null) "maquinaria_cambio_aceite?machineOilChangeId=$machineOilChangeId" else "maquinaria_cambio_aceite"
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
                            },
                            onNavigateToCambioAceite = {
                                navController.navigate("motocicleta_cambio_aceite")
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
                    composable("motocicleta_cambio_aceite") {
                        MotoCambioAceiteScreen(
                            onNavigateBack = {
                                navController.popBackStack()
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
                            },
                            onNavigateToCambioAceite = {
                                navController.navigate("vehiculo_cambio_aceite")
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
                    composable("vehiculo_cambio_aceite") {
                        VehiculoCambioAceiteScreen(
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
                        route = "maquinaria_cambio_aceite?machineOilChangeId={machineOilChangeId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("machineOilChangeId") {
                                type = androidx.navigation.NavType.IntType
                                defaultValue = -1 // Use -1 to indicate no ID
                            }
                        )
                    ) {
                        MaquinariaCambioAceiteScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("subestaciones_home") {
                        SubestacionHomeScreen(
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCaptura = {
                                navController.navigate("subestaciones_captura?libre=true")
                            },
                            onNavigateToCronograma = {
                                navController.navigate("subestaciones_cronograma")
                            },
                            onNavigateToPendientes = {
                                navController.navigate("subestaciones_pendientes")
                            },
                            onNavigateToRealizadas = {
                                navController.navigate("subestaciones_pendientes?filtroInicial=Realizadas")
                            },
                            onNavigateToCola = {
                                navController.navigate("subestaciones_cola")
                            }
                        )
                    }
                    composable(
                        route = "subestaciones_captura?programacionId={programacionId}&estacionId={estacionId}&actividadId={actividadId}&esInspeccion={esInspeccion}&vencida={vencida}&editandoId={editandoId}&motivoEdicion={motivoEdicion}&libre={libre}",
                        arguments = listOf(
                            androidx.navigation.navArgument("programacionId") {
                                type = androidx.navigation.NavType.LongType
                                defaultValue = -1L
                            },
                            androidx.navigation.navArgument("estacionId") {
                                type = androidx.navigation.NavType.LongType
                                defaultValue = -1L
                            },
                            androidx.navigation.navArgument("actividadId") {
                                type = androidx.navigation.NavType.LongType
                                defaultValue = -1L
                            },
                            androidx.navigation.navArgument("esInspeccion") {
                                type = androidx.navigation.NavType.BoolType
                                defaultValue = false
                            },
                            androidx.navigation.navArgument("vencida") {
                                type = androidx.navigation.NavType.BoolType
                                defaultValue = false
                            },
                            androidx.navigation.navArgument("editandoId") {
                                type = androidx.navigation.NavType.LongType
                                defaultValue = -1L
                            },
                            androidx.navigation.navArgument("motivoEdicion") {
                                type = androidx.navigation.NavType.StringType
                                defaultValue = ""
                            },
                            androidx.navigation.navArgument("libre") {
                                type = androidx.navigation.NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        CapturaScreen(
                            networkStatus = networkStatus,
                            programacionId = backStackEntry.arguments?.getLong("programacionId") ?: -1L,
                            estacionId = backStackEntry.arguments?.getLong("estacionId") ?: -1L,
                            actividadId = backStackEntry.arguments?.getLong("actividadId") ?: -1L,
                            esInspeccion = backStackEntry.arguments?.getBoolean("esInspeccion") ?: false,
                            vencida = backStackEntry.arguments?.getBoolean("vencida") ?: false,
                            editandoId = backStackEntry.arguments?.getLong("editandoId") ?: -1L,
                            motivoEdicionInicial = (backStackEntry.arguments?.getString("motivoEdicion") ?: "")
                                .let { runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrDefault(it) },
                            libre = backStackEntry.arguments?.getBoolean("libre") ?: false,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCola = {
                                navController.navigate("subestaciones_cola")
                            },
                            onNavigateToPendientes = {
                                navController.popBackStack("subestaciones_home", inclusive = false)
                                navController.navigate("subestaciones_pendientes")
                            },
                            onNavigateToHome = {
                                navController.popBackStack("subestaciones_home", inclusive = false)
                            }
                        )
                    }
                    composable("subestaciones_cronograma") {
                        CronogramaScreen(
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCaptura = { programacionId, estacionId, actividadId, esInspeccion, vencida ->
                                navController.navigate(
                                    "subestaciones_captura?programacionId=$programacionId&estacionId=$estacionId&actividadId=$actividadId&esInspeccion=$esInspeccion&vencida=$vencida"
                                )
                            },
                            onNavigateToDetalle = { ejecucionId ->
                                navController.navigate("subestaciones_detalle/$ejecucionId")
                            },
                            onNavigateToCapturaLibre = {
                                navController.navigate("subestaciones_captura?libre=true")
                            },
                            onNavigateToCola = {
                                navController.navigate("subestaciones_cola")
                            }
                        )
                    }
                    composable(
                        route = "subestaciones_pendientes?filtroInicial={filtroInicial}",
                        arguments = listOf(
                            androidx.navigation.navArgument("filtroInicial") {
                                type = androidx.navigation.NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        PendientesScreen(
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCaptura = { programacionId, estacionId, actividadId, esInspeccion, vencida ->
                                navController.navigate(
                                    "subestaciones_captura?programacionId=$programacionId&estacionId=$estacionId&actividadId=$actividadId&esInspeccion=$esInspeccion&vencida=$vencida"
                                )
                            },
                            onNavigateToDetalle = { ejecucionId ->
                                navController.navigate("subestaciones_detalle/$ejecucionId")
                            },
                            onNavigateToCola = {
                                navController.navigate("subestaciones_cola")
                            }
                        )
                    }
                    composable(
                        route = "subestaciones_detalle/{ejecucionId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("ejecucionId") {
                                type = androidx.navigation.NavType.LongType
                            }
                        )
                    ) { backStackEntry ->
                        DetalleScreen(
                            ejecucionId = backStackEntry.arguments?.getLong("ejecucionId") ?: -1L,
                            networkStatus = networkStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToEditar = { ejecucionId, motivo ->
                                val motivoCodificado = java.net.URLEncoder.encode(motivo, "UTF-8")
                                navController.navigate("subestaciones_captura?editandoId=$ejecucionId&motivoEdicion=$motivoCodificado")
                            },
                            onNavigateToCola = {
                                navController.navigate("subestaciones_cola")
                            }
                        )
                    }
                    composable("subestaciones_cola") {
                        ColaScreen(
                            networkStatus = networkStatus,
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
