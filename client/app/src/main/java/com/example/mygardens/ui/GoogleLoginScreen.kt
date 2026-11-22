package com.example.mygardens.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun GoogleLoginScreen(
    clientConfigured: Boolean,
    message: String?,
    onSignIn: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("התחברות או הרשמה") },
        text = {
            androidx.compose.foundation.layout.Column {
                Text("התחברו עם חשבון Google כדי לשמור את הלקוחות והמשימות בחשבון הפרטי שלכם.")
                if (!clientConfigured) {
                    Text("Google sign-in is not configured in this build.")
                }
                if (message != null) {
                    Text(
                        message,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSignIn, enabled = clientConfigured) {
                Text("המשך עם Google", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ביטול") }
        }
    )
}
