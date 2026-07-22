package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.AuthScreenMode
import com.example.ui.auth.AuthViewModel
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.JobTraqMainContainer
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileDashboardScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.TermsAndConditionsDialog
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.AuthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuthApp()
        }
    }
}

@Composable
fun AuthApp(viewModel: AuthViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val systemDark = isSystemInDarkTheme()
    val isDark = systemDark || uiState.darkThemeOverride

    LaunchedEffect(uiState.messageSnackbar) {
        uiState.messageSnackbar?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSnackbar()
        }
    }

    AuthTheme(darkTheme = isDark) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = uiState.currentMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "AuthScreenTransitions"
                ) { mode ->
                    when (mode) {
                        AuthScreenMode.WELCOME -> {
                            WelcomeScreen(
                                onNavigateToLogin = { viewModel.navigateTo(AuthScreenMode.LOGIN) },
                                onNavigateToSignup = { viewModel.navigateTo(AuthScreenMode.SIGNUP) },
                                onSocialLogin = { provider -> viewModel.triggerSocialAuth(provider) }
                            )
                        }

                        AuthScreenMode.LOGIN -> {
                            LoginScreen(
                                state = uiState,
                                onEmailChanged = viewModel::onLoginEmailChanged,
                                onPasswordChanged = viewModel::onLoginPasswordChanged,
                                onTogglePasswordVisibility = viewModel::toggleLoginPasswordVisibility,
                                onToggleRememberMe = viewModel::toggleRememberMe,
                                onSubmitLogin = viewModel::submitLogin,
                                onTriggerBiometrics = viewModel::triggerBiometricAuth,
                                onNavigateToSignup = { viewModel.navigateTo(AuthScreenMode.SIGNUP) },
                                onNavigateToForgotPassword = { viewModel.navigateTo(AuthScreenMode.FORGOT_PASSWORD) },
                                onNavigateBack = { viewModel.navigateTo(AuthScreenMode.WELCOME) }
                            )
                        }

                        AuthScreenMode.SIGNUP -> {
                            SignupScreen(
                                state = uiState,
                                onNameChanged = viewModel::onSignupNameChanged,
                                onEmailChanged = viewModel::onSignupEmailChanged,
                                onPhoneChanged = viewModel::onSignupPhoneChanged,
                                onPasswordChanged = viewModel::onSignupPasswordChanged,
                                onConfirmPasswordChanged = viewModel::onSignupConfirmPasswordChanged,
                                onTogglePasswordVisibility = viewModel::toggleSignupPasswordVisibility,
                                onToggleConfirmPasswordVisibility = viewModel::toggleSignupConfirmPasswordVisibility,
                                onToggleTermsAccepted = viewModel::toggleTermsAccepted,
                                onAvatarSelected = viewModel::selectAvatarIndex,
                                onSubmitSignup = viewModel::submitSignup,
                                onOpenTermsModal = { viewModel.setTermsModalOpen(true) },
                                onNavigateToLogin = { viewModel.navigateTo(AuthScreenMode.LOGIN) },
                                onNavigateBack = { viewModel.navigateTo(AuthScreenMode.WELCOME) }
                            )
                        }

                        AuthScreenMode.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(
                                state = uiState,
                                onEmailChanged = viewModel::onResetEmailChanged,
                                onOtpChanged = viewModel::onResetOtpChanged,
                                onNewPasswordChanged = viewModel::onResetNewPasswordChanged,
                                onConfirmPasswordChanged = viewModel::onResetConfirmPasswordChanged,
                                onSubmitEmail = viewModel::submitResetEmail,
                                onSubmitOtp = viewModel::submitResetOtp,
                                onSubmitNewPassword = viewModel::submitNewPassword,
                                onNavigateToLogin = { viewModel.navigateTo(AuthScreenMode.LOGIN) },
                                onNavigateBack = { viewModel.navigateTo(AuthScreenMode.LOGIN) }
                            )
                        }

                        AuthScreenMode.LOGGED_IN -> {
                            uiState.loggedInUser?.let { user ->
                                JobTraqMainContainer(
                                    user = user,
                                    darkThemeOverride = uiState.darkThemeOverride,
                                    isEditModalOpen = uiState.isEditProfileModalOpen,
                                    onLogout = viewModel::logout,
                                    onToggleTheme = viewModel::toggleThemeOverride,
                                    onOpenEditModal = { viewModel.setEditProfileModalOpen(true) },
                                    onCloseEditModal = { viewModel.setEditProfileModalOpen(false) },
                                    onSaveProfile = viewModel::updateProfile
                                )
                            }
                        }
                    }
                }

                // Global Dialogs
                if (uiState.isTermsModalOpen) {
                    TermsAndConditionsDialog(
                        onDismiss = { viewModel.setTermsModalOpen(false) },
                        onAccept = { viewModel.toggleTermsAccepted(true) }
                    )
                }
            }
        }
    }
}
