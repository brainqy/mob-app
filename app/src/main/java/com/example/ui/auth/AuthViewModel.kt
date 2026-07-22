package com.example.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthDatabase
import com.example.data.AuthRepository
import com.example.data.UserEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthScreenMode {
    WELCOME,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    LOGGED_IN
}

data class PasswordStrength(
    val score: Int = 0, // 0 to 4
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialChar: Boolean = false
) {
    val label: String
        get() = when (score) {
            0 -> "Very Weak"
            1 -> "Weak"
            2 -> "Fair"
            3 -> "Strong"
            4 -> "Very Strong"
            else -> "Weak"
        }
}

data class AuthUiState(
    val currentMode: AuthScreenMode = AuthScreenMode.WELCOME,
    val isLoading: Boolean = false,
    val loggedInUser: UserEntity? = null,
    val messageSnackbar: String? = null,

    // Login Form State
    val loginEmail: String = "alex@example.com",
    val loginPassword: String = "Password123!",
    val isLoginPasswordVisible: Boolean = false,
    val isRememberMeChecked: Boolean = true,
    val loginError: String? = null,

    // Signup Form State
    val signupName: String = "",
    val signupEmail: String = "",
    val signupPhone: String = "",
    val signupPassword: String = "",
    val signupConfirmPassword: String = "",
    val isSignupPasswordVisible: Boolean = false,
    val isSignupConfirmPasswordVisible: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val selectedAvatarIndex: Int = 0,
    val signupError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength(),

    // Forgot Password Flow State
    val resetEmail: String = "",
    val resetOtpCode: String = "",
    val resetNewPassword: String = "",
    val resetConfirmPassword: String = "",
    val resetStep: Int = 1, // 1: Email, 2: OTP, 3: New Password
    val resetError: String? = null,

    // Modals & UI Toggles
    val isTermsModalOpen: Boolean = false,
    val isBiometricModalOpen: Boolean = false,
    val isEditProfileModalOpen: Boolean = false,
    val darkThemeOverride: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val userDao = AuthDatabase.getDatabase(application).userDao()
        repository = AuthRepository(userDao)

        viewModelScope.launch {
            repository.seedDemoUserIfEmpty()
        }
    }

    fun navigateTo(mode: AuthScreenMode) {
        _uiState.update {
            it.copy(
                currentMode = mode,
                loginError = null,
                signupError = null,
                resetError = null,
                messageSnackbar = null
            )
        }
    }

    // Login Form Handlers
    fun onLoginEmailChanged(email: String) {
        _uiState.update { it.copy(loginEmail = email, loginError = null) }
    }

    fun onLoginPasswordChanged(password: String) {
        _uiState.update { it.copy(loginPassword = password, loginError = null) }
    }

    fun toggleLoginPasswordVisibility() {
        _uiState.update { it.copy(isLoginPasswordVisible = !it.isLoginPasswordVisible) }
    }

    fun toggleRememberMe(checked: Boolean) {
        _uiState.update { it.copy(isRememberMeChecked = checked) }
    }

    fun submitLogin() {
        val email = _uiState.value.loginEmail
        val password = _uiState.value.loginPassword

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(loginError = "Please enter both email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            delay(600) // Realistic authenticating delay

            val result = repository.loginUser(email, password)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loggedInUser = user,
                        currentMode = AuthScreenMode.LOGGED_IN,
                        messageSnackbar = "Welcome back, ${user.fullName}!"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = exception.message ?: "Authentication failed."
                    )
                }
            }
        }
    }

    // Signup Form Handlers
    fun onSignupNameChanged(name: String) {
        _uiState.update { it.copy(signupName = name, signupError = null) }
    }

    fun onSignupEmailChanged(email: String) {
        _uiState.update { it.copy(signupEmail = email, signupError = null) }
    }

    fun onSignupPhoneChanged(phone: String) {
        _uiState.update { it.copy(signupPhone = phone) }
    }

    fun onSignupPasswordChanged(password: String) {
        val strength = calculatePasswordStrength(password)
        _uiState.update {
            it.copy(
                signupPassword = password,
                passwordStrength = strength,
                signupError = null
            )
        }
    }

    fun onSignupConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(signupConfirmPassword = password, signupError = null) }
    }

    fun toggleSignupPasswordVisibility() {
        _uiState.update { it.copy(isSignupPasswordVisible = !it.isSignupPasswordVisible) }
    }

    fun toggleSignupConfirmPasswordVisibility() {
        _uiState.update { it.copy(isSignupConfirmPasswordVisible = !it.isSignupConfirmPasswordVisible) }
    }

    fun toggleTermsAccepted(accepted: Boolean) {
        _uiState.update { it.copy(isTermsAccepted = accepted) }
    }

    fun selectAvatarIndex(index: Int) {
        _uiState.update { it.copy(selectedAvatarIndex = index) }
    }

    fun submitSignup() {
        val state = _uiState.value
        if (state.signupName.isBlank()) {
            _uiState.update { it.copy(signupError = "Please enter your full name.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.signupEmail).matches()) {
            _uiState.update { it.copy(signupError = "Please enter a valid email address.") }
            return
        }
        if (state.signupPassword.length < 6) {
            _uiState.update { it.copy(signupError = "Password must be at least 6 characters.") }
            return
        }
        if (state.signupPassword != state.signupConfirmPassword) {
            _uiState.update { it.copy(signupError = "Passwords do not match.") }
            return
        }
        if (!state.isTermsAccepted) {
            _uiState.update { it.copy(signupError = "You must accept the Terms and Conditions.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, signupError = null) }
            delay(700)

            val result = repository.registerUser(
                fullName = state.signupName,
                email = state.signupEmail,
                passwordRaw = state.signupPassword,
                phone = state.signupPhone,
                avatarBadgeIndex = state.selectedAvatarIndex
            )

            result.onSuccess { userId ->
                val user = repository.getUserByEmail(state.signupEmail)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loggedInUser = user,
                        currentMode = AuthScreenMode.LOGGED_IN,
                        messageSnackbar = "Account created successfully!"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        signupError = exception.message ?: "Signup failed."
                    )
                }
            }
        }
    }

    // Forgot Password Handlers
    fun onResetEmailChanged(email: String) {
        _uiState.update { it.copy(resetEmail = email, resetError = null) }
    }

    fun onResetOtpChanged(otp: String) {
        _uiState.update { it.copy(resetOtpCode = otp, resetError = null) }
    }

    fun onResetNewPasswordChanged(password: String) {
        _uiState.update { it.copy(resetNewPassword = password, resetError = null) }
    }

    fun onResetConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(resetConfirmPassword = password, resetError = null) }
    }

    fun submitResetEmail() {
        val email = _uiState.value.resetEmail
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(resetError = "Please enter a valid email address.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resetError = null) }
            delay(600)
            val user = repository.getUserByEmail(email)
            if (user == null) {
                _uiState.update {
                    it.copy(isLoading = false, resetError = "No account registered with this email.")
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resetStep = 2,
                        messageSnackbar = "Verification code sent to $email"
                    )
                }
            }
        }
    }

    fun submitResetOtp() {
        val otp = _uiState.value.resetOtpCode
        if (otp.length < 4) {
            _uiState.update { it.copy(resetError = "Please enter the 4-digit code.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resetStep = 3,
                    messageSnackbar = "Identity verified! Set your new password."
                )
            }
        }
    }

    fun submitNewPassword() {
        val state = _uiState.value
        if (state.resetNewPassword.length < 6) {
            _uiState.update { it.copy(resetError = "Password must be at least 6 characters.") }
            return
        }
        if (state.resetNewPassword != state.resetConfirmPassword) {
            _uiState.update { it.copy(resetError = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(600)
            repository.resetPassword(state.resetEmail, state.resetNewPassword)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentMode = AuthScreenMode.LOGIN,
                    loginEmail = state.resetEmail,
                    messageSnackbar = "Password reset successfully! Please sign in with your new password."
                )
            }
        }
    }

    // Biometrics & Social Quick Auth Simulation
    fun triggerBiometricAuth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBiometricModalOpen = true) }
            delay(1200) // Biometric scanning simulation
            val user = repository.getUserByEmail("alex@example.com")
            if (user != null) {
                _uiState.update {
                    it.copy(
                        isBiometricModalOpen = false,
                        loggedInUser = user,
                        currentMode = AuthScreenMode.LOGGED_IN,
                        messageSnackbar = "Biometric unlock successful!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isBiometricModalOpen = false,
                        loginError = "Biometric setup required."
                    )
                }
            }
        }
    }

    fun triggerSocialAuth(providerName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(800)
            val demoEmail = "${providerName.lowercase()}user@auth.io"
            var user = repository.getUserByEmail(demoEmail)
            if (user == null) {
                repository.registerUser(
                    fullName = "$providerName User",
                    email = demoEmail,
                    passwordRaw = "SocialPass123!",
                    phone = "",
                    avatarBadgeIndex = 2
                )
                user = repository.getUserByEmail(demoEmail)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loggedInUser = user,
                    currentMode = AuthScreenMode.LOGGED_IN,
                    messageSnackbar = "Signed in with $providerName successfully!"
                )
            }
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                loggedInUser = null,
                currentMode = AuthScreenMode.LOGIN,
                messageSnackbar = "Logged out safely."
            )
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }

    fun setTermsModalOpen(open: Boolean) {
        _uiState.update { it.copy(isTermsModalOpen = open) }
    }

    fun setEditProfileModalOpen(open: Boolean) {
        _uiState.update { it.copy(isEditProfileModalOpen = open) }
    }

    fun toggleThemeOverride() {
        _uiState.update { it.copy(darkThemeOverride = !it.darkThemeOverride) }
    }

    fun updateProfile(name: String, phone: String, avatarIndex: Int) {
        val currentUser = _uiState.value.loggedInUser ?: return
        viewModelScope.launch {
            val updated = currentUser.copy(
                fullName = name,
                phone = phone,
                avatarBadgeIndex = avatarIndex
            )
            repository.updateUserProfile(updated)
            _uiState.update {
                it.copy(
                    loggedInUser = updated,
                    isEditProfileModalOpen = false,
                    messageSnackbar = "Profile updated!"
                )
            }
        }
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength()

        val minLen = password.length >= 8
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        var score = 0
        if (password.length >= 6) score++
        if (minLen) score++
        if (hasUpper && hasDigit) score++
        if (hasSpecial) score++

        return PasswordStrength(
            score = score.coerceIn(0, 4),
            hasMinLength = minLen,
            hasUppercase = hasUpper,
            hasDigit = hasDigit,
            hasSpecialChar = hasSpecial
        )
    }
}
