package com.pobvol.mychecks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.repository.UserSettings

@Composable
fun FtpSettingsDialog(
    userSettings: UserSettings,
    onFtpServerChanged: (String) -> Unit,
    onFtpPortChanged: (Int) -> Unit,
    onFtpUseTlsChanged: (Boolean) -> Unit,
    onFtpUserChanged: (String) -> Unit,
    onFtpPasswordChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.ftp_settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }

                // 1. Server / Host
                var serverText by remember(userSettings.ftpServer) { mutableStateOf(userSettings.ftpServer) }
                OutlinedTextField(
                    value = serverText,
                    onValueChange = { newText ->
                        serverText = newText
                        onFtpServerChanged(newText)
                    },
                    label = { Text(stringResource(R.string.ftp_server_label)) },
                    placeholder = { Text("ftp.example.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                // 2. Port
                var portText by remember(userSettings.ftpPort) { mutableStateOf(userSettings.ftpPort.toString()) }
                OutlinedTextField(
                    value = portText,
                    onValueChange = { newText ->
                        portText = newText
                        val portInt = newText.toIntOrNull() ?: 21
                        onFtpPortChanged(portInt)
                    },
                    label = { Text(stringResource(R.string.ftp_port_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                // 3. FTP over TLS (FTPS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.ftp_use_tls_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = userSettings.ftpUseTls,
                        onCheckedChange = onFtpUseTlsChanged,
                    )
                }

                // 4. Username
                var userText by remember(userSettings.ftpUser) { mutableStateOf(userSettings.ftpUser) }
                OutlinedTextField(
                    value = userText,
                    onValueChange = { newText ->
                        userText = newText
                        onFtpUserChanged(newText)
                    },
                    label = { Text(stringResource(R.string.ftp_user_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                // 5. Password
                var passwordText by remember(userSettings.ftpPassword) { mutableStateOf(userSettings.ftpPassword) }
                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { newText ->
                        passwordText = newText
                        onFtpPasswordChanged(newText)
                    },
                    label = { Text(stringResource(R.string.ftp_password_label)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}
