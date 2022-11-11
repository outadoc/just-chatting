package fr.outadoc.justchatting.ui.login

import android.webkit.CookieManager
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.util.toast
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onDone: () -> Unit
) {
    val viewModel: LoginViewModel = getViewModel()
    val state by viewModel.state.collectAsState()

    Crossfade(targetState = state) { currentState ->
        when (currentState) {
            LoginViewModel.State.Initial -> {}
            is LoginViewModel.State.LoadWebView -> {
                val webViewState = rememberWebViewState(currentState.url.toString())
                val navigator = rememberWebViewNavigator()

                val url: String? = webViewState.content.getCurrentUrl()

                LaunchedEffect(url) {
                    if (url != null) {
                        viewModel.onNavigateToUrl(url)
                    }
                }

                val context = LocalContext.current
                LaunchedEffect(currentState.exception) {
                    currentState.exception?.let {
                        context.toast(R.string.connection_error)
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(
                                    onClick = { viewModel.onCloseClick() }
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Cancel login"
                                    )
                                }
                            },
                            actions = {
                                when (webViewState.loadingState) {
                                    LoadingState.Initializing -> {}
                                    LoadingState.Finished -> {
                                        IconButton(
                                            onClick = { navigator.reload() }
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Refresh current page"
                                            )
                                        }
                                    }

                                    is LoadingState.Loading -> {
                                        IconButton(
                                            onClick = { navigator.stopLoading() }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Cancel page load"
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) { insets ->
                    WebView(
                        modifier = Modifier.padding(insets),
                        state = webViewState,
                        navigator = navigator,
                        onCreated = { webView ->
                            // noinspection SetJavascriptEnabled
                            webView.settings.javaScriptEnabled = true
                        },
                        onDispose = {
                            // If we don't clear cookies, user won't be able to switch accounts.
                            CookieManager.getInstance().removeAllCookies(null)
                        }
                    )
                }
            }

            LoginViewModel.State.Done -> {
                onDone()
            }
        }
    }
}
