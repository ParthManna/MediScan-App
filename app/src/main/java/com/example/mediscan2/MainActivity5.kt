package com.example.mediscan2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ad_coding.supabasecourse.ui.theme.SupabaseCourseTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.jan.supabase.storage.Storage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject

class MainActivity5 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuthApp(this)
        }
    }
}

@Composable
fun AuthApp(activity: ComponentActivity) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            EmailAuthScreen(
                onAuthSuccess = {
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                },
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun EmailAuthScreen(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(true) }
    val authState by authViewModel.authState.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SUCCESS -> onAuthSuccess()
            else -> {}
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        GradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(
                title = if (isSignUpMode) "Create An Account" else "Sign In",
                subtitle = if (isSignUpMode) "Enter your email and password to register"
                else "Enter your credentials to sign in"
            )

            Spacer(modifier = Modifier.height(40.dp))

            EmailInputField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = "Email",
                placeholder = "john.doe@example.com",
                isError = authState is AuthState.ERROR,
                onClearError = { authViewModel.clearError() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            PasswordInputField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = "Password",
                placeholder = "Enter your password",
                isError = authState is AuthState.ERROR,
                errorMessage = (authState as? AuthState.ERROR)?.message,
                onClearError = { authViewModel.clearError() }
            )

            if (!isSignUpMode) {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            authViewModel.resetPassword(emailValue)
                        }
                    },
                    enabled = emailValue.isNotBlank(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.Gray // makes it visible when disabled
                    )

                ) {
                    Text("Forgot Password?")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AuthButton(
                text = if (isSignUpMode) "Sign up" else "Sign in",
                onClick = {
                    coroutineScope.launch {
                        if (isSignUpMode) {
                            authViewModel.signUp(emailValue, passwordValue)
                        } else {
                            authViewModel.signIn(emailValue, passwordValue)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = { isSignUpMode = !isSignUpMode },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account? Sign in"
                    else "Don't have an account? Sign up"
                )
            }

            when (authState) {
                is AuthState.LOADING -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))

                is AuthState.EMAIL_SENT -> {
                    Text(
                        text = (authState as AuthState.EMAIL_SENT).message,
                        color = Color.Green,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                is AuthState.ERROR -> {
                    Text(
                        text = (authState as AuthState.ERROR).message,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                else -> {}
            }



        }
    }
}

@Composable
fun GradientBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF264C6F),
                        Color.Black
                    )
                )
            )
    )
}

@Composable
fun AuthHeader(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
fun EmailInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    onClearError: (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            color = if (isError) Color.Red else Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (isError) {
                    onClearError?.invoke()
                }
            },
            textStyle = MaterialTheme.typography.titleMedium.copy( // Larger input text
                color = Color.White,
                fontWeight = FontWeight.Normal
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleMedium // Larger placeholder
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = if (isError) Color.Red else Color.Transparent,
                focusedIndicatorColor = if (isError) Color.Red else Color.Transparent,
                focusedContainerColor = if (isError) Color.Red.copy(alpha = 0.1f) else Color.DarkGray,
                unfocusedContainerColor = if (isError) Color.Red.copy(alpha = 0.1f) else Color.DarkGray,
                focusedTextColor = Color.White, // White text when focused
                unfocusedTextColor = Color.White // White text when not focused
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    onClearError: (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            color = if (isError) Color.Red else Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (isError) {
                    onClearError?.invoke()
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = MaterialTheme.typography.titleMedium.copy( // Larger input text
                color = Color.White,
                fontWeight = FontWeight.Normal
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleMedium )// Larger placeholder
            },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password"
                        else "Show password",
                        tint = Color.White
                    )
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = if (isError) Color.Red else Color.Transparent,
                focusedIndicatorColor = if (isError) Color.Red else Color.Transparent,
                focusedContainerColor = if (isError) Color.Red.copy(alpha = 0.1f) else Color.DarkGray,
                unfocusedContainerColor = if (isError) Color.Red.copy(alpha = 0.1f) else Color.DarkGray,
                focusedTextColor = Color.White, // White text when focused
                unfocusedTextColor = Color.White, // White text when not focused
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }


    }


}



@Composable
fun AuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableLiveData<AuthState>(AuthState.IDLE)
    val authState: LiveData<AuthState> = _authState

    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://bwbabpydaarigkibyanp.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8"
    ) {
        install(Auth)
        install(Storage)
    }

    suspend fun signUp(email: String, password: String) {
        _authState.value = AuthState.LOADING
        try {
            // New correct way to sign up with email verification
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password

                // Set verification options
                this.data = buildJsonObject {
                    put("redirect_to", "mediscan://callback?type=signup")
                }
            }

            _authState.value = AuthState.EMAIL_SENT("Verification email sent to $email")
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR(
                when {
                    e.message?.contains("Password should be at least") == true ->
                        "Password must be at least 6 characters"
                    e.message?.contains("User already registered") == true ->
                        "Email already registered. Please sign in."
                    else -> "Sign up failed: ${e.message}"
                }
            )
        }
    }


    suspend fun signIn(email: String, password: String) {
        _authState.value = AuthState.LOADING
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            _authState.value = AuthState.SUCCESS
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR(
                when {
                    e.message?.contains("Invalid login credentials") == true ->
                        "Invalid email or password"
                    e.message?.contains("Email not confirmed") == true ->
                        "Please verify your email first"
                    else -> e.message ?: "Sign in failed"
                }
            )
        }
    }

    suspend fun resetPassword(email: String) {
        if (!isValidEmail(email)) {
            _authState.value = AuthState.ERROR("Please enter a valid email address")
            return
        }
        _authState.value = AuthState.LOADING
        try {
            supabase.auth.resetPasswordForEmail(email)
            _authState.value = AuthState.EMAIL_SENT("Password reset link sent to $email")
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR("Failed to send reset email: ${e.message}")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }



    fun clearError() {
        _authState.value = AuthState.IDLE
    }
}

sealed class AuthState {
    object IDLE : AuthState()
    object LOADING : AuthState()
    object SUCCESS : AuthState()
    data class ERROR(val message: String) : AuthState()
    data class EMAIL_SENT(val message: String) : AuthState()  // New state
}