package com.samcod3.alldebrid.ui.screens.login

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.samcod3.alldebrid.data.model.AllDebridError

/**
 * WebView screen for authorizing VPN/new IP addresses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpAuthorizationScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authorize IP Address") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            IpAuthWebView(
                url = AllDebridError.VPN_AUTHORIZATION_URL,
                onLoadingChange = { isLoading = it },
                onAuthorizationComplete = onComplete
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun IpAuthWebView(
    url: String,
    onLoadingChange: (Boolean) -> Unit,
    onAuthorizationComplete: () -> Unit
) {
    var hasCompletedAuth by remember { mutableStateOf(false) }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChange(false)
                        
                        // Check if authorization was completed
                        // The VPN page typically shows a success message or redirects
                        url?.let {
                            // If user navigates away from /vpn page, they likely completed auth
                            if (!it.contains("/vpn") && hasCompletedAuth) {
                                onAuthorizationComplete()
                            }
                            // Mark that user has visited the page
                            if (it.contains("/vpn")) {
                                hasCompletedAuth = true
                            }
                        }
                    }
                }
                
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
