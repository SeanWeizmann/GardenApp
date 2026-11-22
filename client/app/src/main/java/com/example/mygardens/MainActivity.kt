package com.example.mygardens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mygardens.data.GardenersApi
import com.example.mygardens.data.GoogleCodeRequest
import com.example.mygardens.network.NetworkConfig
import com.example.mygardens.ui.MissionsScreen
import com.example.mygardens.ui.MissionsViewModel
import com.example.mygardens.ui.theme.MyGardensTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class MainActivity : ComponentActivity() {
    private val activityScope = MainScope()
    private var sessionToken by mutableStateOf<String?>(null)
    private var authMessage by mutableStateOf<String?>(null)
    private val sessionPreferences by lazy { getSharedPreferences("gardeners_auth", MODE_PRIVATE) }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val code = account.serverAuthCode
                    ?: error("Google did not return a server authorization code.")
                activityScope.launch {
                    try {
                        val session = GardenersApi.instance.exchangeGoogleCode(
                            request = GoogleCodeRequest(code)
                        )
                        sessionPreferences.edit().putString(SESSION_TOKEN_KEY, session.sessionToken)
                            .apply()
                        GardenersApi.sessionToken = session.sessionToken
                        sessionToken = session.sessionToken
                        authMessage = null
                    } catch (e: Exception) {
                        val serverMessage =
                            (e as? HttpException)?.response()?.errorBody()?.string()?.let { body ->
                                runCatching {
                                    val json = JsonParser.parseString(body).asJsonObject
                                    json.get("error")?.asString ?: json.get("message")?.asString
                                }.getOrNull()
                            }
                        val msg = serverMessage ?: e.localizedMessage ?: "Network connection error"
                        authMessage =
                            "Backend sign-in failed: $msg (Server: ${NetworkConfig.baseUrl})"
                    }
                }
            } catch (e: Exception) {
                val apiError = e as? ApiException
                authMessage = when (apiError?.statusCode) {
                    12501 -> "Google sign-in was canceled."
                    10 -> "Developer Error (Code 10): Please ensure the SHA-1 fingerprint (6A:FC:3D:20:AE:38:9A:F1:DA:6D:FE:E3:E2:C9:5A:6E:D4:9F:05:9C) for com.example.mygardens is added to your Google Cloud Console Android client."
                    12500 -> "Sign-in Failed (Code 12500): Check OAuth consent screen and Web Client ID configuration in Google Cloud Console."
                    else -> {
                        if (result.resultCode == RESULT_CANCELED && result.data == null) {
                            "Google sign-in was canceled."
                        } else if (apiError != null) {
                            "Google sign-in failed (code ${apiError.statusCode}): ${apiError.status.statusMessage ?: apiError.message ?: "check OAuth client configuration"}"
                        } else {
                            "Google sign-in failed: ${e.localizedMessage ?: "unknown error"}"
                        }
                    }
                }
            }
        }

    private fun startGoogleSignIn() {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (webClientId.isBlank()) {
            authMessage = "Google Web Client ID is not set in gradle.properties."
            return
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestServerAuthCode(webClientId)
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.events"))
            .build()
        val client = GoogleSignIn.getClient(this, options)
        signInLauncher.launch(client.signInIntent)
    }

    private fun restoreSession() {
        val savedToken = sessionPreferences.getString(SESSION_TOKEN_KEY, null) ?: return
        if (savedToken.isBlank()) return
        sessionToken = savedToken
        GardenersApi.sessionToken = savedToken
        activityScope.launch {
            try {
                withContext(Dispatchers.IO) { GardenersApi.instance.tasks() }
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    sessionPreferences.edit().remove(SESSION_TOKEN_KEY).apply()
                    GardenersApi.sessionToken = null
                    sessionToken = null
                }
            } catch (_: Exception) {
                // Preserve the session during temporary connectivity failures.
            }
        }
    }

    private fun logout() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestServerAuthCode(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.events"))
            .build()
        activityScope.launch {
            try {
                if (!sessionToken.isNullOrBlank()) withContext(Dispatchers.IO) { GardenersApi.instance.logout() }
            } catch (_: Exception) {
                // Complete local sign-out even when offline.
            } finally {
                sessionPreferences.edit().remove(SESSION_TOKEN_KEY).apply()
                GardenersApi.sessionToken = null
                sessionToken = null
                GoogleSignIn.getClient(this@MainActivity, options).signOut()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSession()
        enableEdgeToEdge()
        setContent {
            val viewModel: MissionsViewModel = viewModel()
            LaunchedEffect(sessionToken) {
                GardenersApi.sessionToken = sessionToken
                viewModel.onSessionChanged(sessionToken)
            }
            MyGardensTheme {
                val isDarkMode by viewModel.isDarkMode.collectAsState()
                MyGardensTheme(darkTheme = isDarkMode) {
                    MissionsScreen(
                        viewModel = viewModel,
                        authMessage = authMessage,
                        onSignInClick = {
                            authMessage = null
                            startGoogleSignIn()
                        },
                        onLogoutClick = { logout() },
                        isSignedIn = sessionToken != null
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        activityScope.cancel(); super.onDestroy()
    }

}

private const val SESSION_TOKEN_KEY = "server_session_token"
