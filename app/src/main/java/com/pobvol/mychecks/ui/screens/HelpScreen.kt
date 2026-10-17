package com.pobvol.mychecks.ui.screens

import android.graphics.Color
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pobvol.mychecks.R
import com.pobvol.mychecks.data.repository.ThemeMode
import com.pobvol.mychecks.ui.ChecklistUiState
import com.pobvol.mychecks.ui.theme.mychecksTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    uiState: ChecklistUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    BackHandler(onBack = onBackClick)

    val darkTheme = when (uiState.userSettings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var textZoomPercent by remember { mutableStateOf(100) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val currentLang = uiState.userSettings.preferredLanguage.takeIf { it.isNotBlank() } ?: "en"
    val assetFileName = when (currentLang.lowercase()) {
        "de" -> "help/help_de.html"
        "es" -> "help/help_es.html"
        "fr" -> "help/help_fr.html"
        else -> "help/help_en.html"
    }

    val htmlContent = remember(darkTheme, currentLang) {
        try {
            context.assets.open(assetFileName).bufferedReader().use { it.readText() }
                .replace("<body>", if (darkTheme) "<body class=\"dark\">" else "<body class=\"light\">")
        } catch (_: Exception) {
            "<html><body><h1>Help</h1><p>Error loading help content.</p></body></html>"
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.help),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_dashboard),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        textZoomPercent = maxOf(70, textZoomPercent - 15)
                        webViewRef?.settings?.textZoom = textZoomPercent
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ZoomOut,
                            contentDescription = "Zoom Out",
                        )
                    }
                    IconButton(onClick = {
                        textZoomPercent = minOf(200, textZoomPercent + 15)
                        webViewRef?.settings?.textZoom = textZoomPercent
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ZoomIn,
                            contentDescription = "Zoom In",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.textZoom = textZoomPercent
                    setBackgroundColor(Color.TRANSPARENT)
                    loadDataWithBaseURL("file:///android_asset/help/", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { webView ->
                webView.settings.textZoom = textZoomPercent
                webView.loadDataWithBaseURL("file:///android_asset/help/", htmlContent, "text/html", "UTF-8", null)
                webViewRef = webView
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HelpScreenPreview() {
    mychecksTheme {
        HelpScreen(
            uiState = ChecklistUiState(),
            onBackClick = {},
        )
    }
}
