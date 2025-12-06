package com.sawwere.healthconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sawwere.healthconnect.data.HealthConnectProvider
import com.sawwere.healthconnect.data.HealthConnectRepository
import com.sawwere.healthconnect.ui.HealthConnectViewModel
import com.sawwere.healthconnect.ui.insert.InsertDataScreen
import com.sawwere.healthconnect.ui.main.MainScreen

class MainActivity : ComponentActivity() {

    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(HealthConnectProvider.PERMISSIONS)) {
            viewModel.onPermissionsGranted()
        } else {
            viewModel.onPermissionsDenied()
        }
    }

    private val viewModel: HealthConnectViewModel by viewModels {
        HealthConnectViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HealthConnectApp(viewModel = viewModel)
        }
    }

    fun launchPermissionsRequest() {
        requestPermissions.launch(HealthConnectProvider.PERMISSIONS.toSet())
    }
}

@Composable
fun HealthConnectApp(viewModel: HealthConnectViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val activity = context as? MainActivity

            NavHost(
                navController = navController,
                startDestination = "main"
            ) {
                composable("main") {
                    MainScreen(
                        viewModel = viewModel,
                        onNavigateToInsert = {
                            navController.navigate("insert")
                        },
                        onRequestPermissions = {
                            activity?.launchPermissionsRequest()
                        }
                    )
                }
                composable("insert") {
                    InsertDataScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}