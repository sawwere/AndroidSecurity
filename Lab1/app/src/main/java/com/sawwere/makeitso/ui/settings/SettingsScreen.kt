package com.sawwere.makeitso.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sawwere.makeitso.R
import com.sawwere.makeitso.data.repository.AuthProvider
import com.sawwere.makeitso.ui.shared.CenterTopAppBar
import com.sawwere.makeitso.ui.shared.StandardButton
import com.sawwere.makeitso.ui.theme.DarkBlue
import com.sawwere.makeitso.ui.theme.DarkGrey
import com.sawwere.makeitso.ui.theme.LightRed
import com.sawwere.makeitso.ui.theme.MakeItSoTheme
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

@Composable
fun SettingsScreen(
    openHomeScreen: () -> Unit,
    openSignInScreen: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val shouldRestartApp by viewModel.shouldRestartApp.collectAsStateWithLifecycle()

    if (shouldRestartApp) {
        openHomeScreen()
    } else {
        val isAnonymous by viewModel.isAnonymous.collectAsStateWithLifecycle()
        val signInState by viewModel.uiState.collectAsStateWithLifecycle()

        SettingsScreenContent(
            loadCurrentUser = viewModel::loadCurrentUser,
            openSignInScreen = openSignInScreen,
            signOut = viewModel::signOut,
            deleteAccount = viewModel::deleteAccount,
            signInState = signInState,
            isAnonymous = isAnonymous
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreenContent(
    loadCurrentUser: () -> Unit,
    openSignInScreen: () -> Unit,
    signOut: () -> Unit,
    deleteAccount: () -> Unit,
    signInState: SettingsViewModel.SignInUiState,
    isAnonymous: Boolean
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(true) {
        loadCurrentUser()
    }

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
        ) {
            Spacer(Modifier.size(24.dp))

            if (isAnonymous) {
                StandardButton(
                    label = R.string.sign_in,
                    onButtonClick = {
                        openSignInScreen()
                    }
                )
            } else {
                UserInfoCard(
                    userEmail = signInState.userEmail,
                    authProvider = signInState.authProvider,
                )
                StandardButton(
                    label = R.string.sign_out,
                    onButtonClick = {
                        signOut()
                    }
                )

                Spacer(Modifier.size(16.dp))

                DeleteAccountButton(deleteAccount)
            }
        }
    }
}

@Composable
fun DeleteAccountButton(deleteAccount: () -> Unit) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    StandardButton(
        label = R.string.delete_account,
        onButtonClick = {
            showDeleteAccountDialog = true
        }
    )

    if (showDeleteAccountDialog) {
        AlertDialog(
            containerColor = LightRed,
            textContentColor = DarkBlue,
            titleContentColor = DarkBlue,
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_description)) },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.cancel), fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccount()
                    },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.delete), fontSize = 16.sp)
                }
            },
            onDismissRequest = { showDeleteAccountDialog = false }
        )
    }
}

private fun getDialogButtonColors(): ButtonColors {
    return ButtonColors(
        containerColor = LightRed,
        contentColor = DarkBlue,
        disabledContainerColor = LightRed,
        disabledContentColor = DarkGrey
    )
}

@Composable
fun UserInfoCard(
    userEmail: String?,
    authProvider: AuthProvider?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Информация о входе",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Почта пользователя
            InfoRow(
                icon = Icons.Default.Email,
                title = "Электронная почта",
                value = userEmail ?: "Не указана"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Способ входа
            InfoRow(
                icon = Icons.Default.Settings,
                title = "Способ входа",
                value = authProvider?.displayName ?: "Неизвестно"
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun SettingsScreenPreview() {
    MakeItSoTheme(darkTheme = true) {
        SettingsScreenContent(
            loadCurrentUser = {},
            openSignInScreen = {},
            signOut = {},
            deleteAccount = {},
            signInState = SettingsViewModel.SignInUiState(
                userEmail = "example@example.com"
            ),
            isAnonymous = false
        )
    }
}