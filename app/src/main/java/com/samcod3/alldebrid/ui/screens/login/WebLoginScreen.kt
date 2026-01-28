package com.samcod3.alldebrid.ui.screens.login

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebLoginScreen(
    onApiKeyExtracted: (String) -> Unit,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Loading AllDebrid...") }
    var isExtractingKey by remember { mutableStateOf(false) }
    var extractionFailed by remember { mutableStateOf(false) }
    var extractionStartTime by remember { mutableStateOf(0L) }
    var noKeysFound by remember { mutableStateOf(false) }

    // If no keys found, go back immediately to create one
    LaunchedEffect(noKeysFound) {
        if (noKeysFound) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login to AllDebrid") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // Progress indicator
            if (isLoading || isExtractingKey) {
                LinearProgressIndicator(
                    progress = { if (progress > 0) progress else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (isExtractingKey && !extractionFailed) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        
                        // Check for timeout
                        if (extractionStartTime > 0 && 
                            System.currentTimeMillis() - extractionStartTime > 10000) {
                            extractionFailed = true
                            statusText = "API key extraction timed out"
                        }
                    }
                }
            } else if (extractionFailed) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Failed to extract API key automatically",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Please go back and enter your API key manually from the API Keys Manager",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        androidx.compose.material3.Button(
                            onClick = onBack,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            } else {
                WebViewContent(
                    onProgress = { progress = it },
                    onLoadingChange = { isLoading = it },
                    onLoginDetected = {
                        isExtractingKey = true
                        extractionStartTime = System.currentTimeMillis()
                        statusText = "Extracting API Key..."
                    },
                    onApiKeyExtracted = onApiKeyExtracted,
                    onStatusChange = { statusText = it },
                    onExtractionFailed = { extractionFailed = true },
                    onNoKeysFound = { noKeysFound = true }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewContent(
    onProgress: (Float) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onLoginDetected: () -> Unit,
    onApiKeyExtracted: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onExtractionFailed: () -> Unit,
    onNoKeysFound: () -> Unit
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var hasDetectedLogin by remember { mutableStateOf(false) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                
                // Enable cookies
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)
                
                
                // JavaScript interface for extracting API key
                addJavascriptInterface(
                    ApiKeyExtractor { apiKey ->
                        when {
                            apiKey == "NO_KEYS_FOUND" -> {
                                // No keys exist, go back to API Keys screen to create one
                                onNoKeysFound()
                            }
                            apiKey.startsWith("ERROR:") -> {
                                onExtractionFailed()
                            }
                            else -> {
                                onApiKeyExtracted(apiKey)
                            }
                        }
                    },
                    "AndroidApp"
                )
                
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return false // Let the WebView handle all URLs
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChange(false)
                        
                        url?.let {
                            // Check if user is logged in by looking at the URL or cookies
                            val cookies = CookieManager.getInstance().getCookie("https://alldebrid.com")
                            val isLoggedIn = cookies?.contains("uid=") == true && 
                                             !it.contains("/login") && 
                                             !it.contains("/register")
                            
                            if (isLoggedIn && !hasDetectedLogin) {
                                hasDetectedLogin = true
                                onLoginDetected()
                                onStatusChange("Login detected! Fetching API keys...")
                                
                                // Navigate to apikeys page and extract keys
                                view?.loadUrl("https://alldebrid.com/apikeys/")
                            }
                            
                            // If we're on the apikeys page, extract the keys
                            if (it.contains("/apikeys") && hasDetectedLogin) {
                                onStatusChange("Extracting API key...")
                                
                                // Inject JavaScript to extract the API key
                                val extractScript = """
                                    (function() {
                                        try {
                                            // Look for the keys variable in the page
                                            var keysMatch = document.body.innerHTML.match(/var keys = (\[[\s\S]*?\]);/);
                                            if (keysMatch && keysMatch[1]) {
                                                var keys = JSON.parse(keysMatch[1]);
                                                if (keys.length > 0) {
                                                    // Return the first API key
                                                    AndroidApp.onApiKeyFound(keys[0].apikey);
                                                    return;
                                                }
                                            }
                                            
                                            // Alternative: look for apikey in any script tag
                                            var scripts = document.getElementsByTagName('script');
                                            for (var i = 0; i < scripts.length; i++) {
                                                var content = scripts[i].innerHTML;
                                                var match = content.match(/var keys = (\[[\s\S]*?\]);/);
                                                if (match && match[1]) {
                                                    var keys = JSON.parse(match[1]);
                                                    if (keys.length > 0) {
                                                        AndroidApp.onApiKeyFound(keys[0].apikey);
                                                        return;
                                                    }
                                                }
                                            }
                                            
                                            // No keys found, need to create one
                                            AndroidApp.onApiKeyFound("NO_KEYS_FOUND");
                                        } catch(e) {
                                            AndroidApp.onApiKeyFound("ERROR:" + e.message);
                                        }
                                    })();
                                """.trimIndent()
                                
                                view?.evaluateJavascript(extractScript, null)
                            }
                        }
                    }
                }
                
                // Load AllDebrid login page
                loadUrl("https://alldebrid.com/register/#login")
                webView = this
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * JavaScript interface for receiving the extracted API key
 */
class ApiKeyExtractor(private val onKeyFound: (String) -> Unit) {
    @JavascriptInterface
    fun onApiKeyFound(apiKey: String) {
        // JavascriptInterface runs on WebView thread, but navigation must be on main thread
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onKeyFound(apiKey)
        }
    }
}

