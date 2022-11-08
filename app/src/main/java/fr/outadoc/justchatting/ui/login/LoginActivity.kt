package fr.outadoc.justchatting.ui.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.ActivityLoginBinding
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.toast
import okhttp3.HttpUrl
import org.intellij.lang.annotations.Language
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModel()
    private lateinit var viewHolder: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewHolder = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewHolder.root)

        viewModel.state.observe(this) { state ->
            when (state) {
                LoginViewModel.State.Initial -> {}
                is LoginViewModel.State.LoadWebView -> {
                    state.exception?.let {
                        toast(R.string.connection_error)
                    }

                    initWebView(state.url)
                }

                LoginViewModel.State.Done -> {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        viewModel.onStart()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(helixAuthUrl: HttpUrl) {
        clearCookies()

        with(viewHolder.webView) {
            if (isDarkMode) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
                }

                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                    WebSettingsCompat.setForceDarkStrategy(
                        settings,
                        WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY
                    )
                }
            }

            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {

                override fun onLoadResource(view: WebView, url: String) {
                    viewModel.onNavigateToUrl(url)
                }

                override fun onReceivedError(
                    view: WebView,
                    errorCode: Int,
                    description: String,
                    failingUrl: String
                ) {
                    val errorMessage = when (errorCode) {
                        ERROR_FAILED_SSL_HANDSHAKE -> getString(R.string.browser_workaround)
                        else -> getString(R.string.error, "$errorCode $description")
                    }

                    @Language("HTML")
                    val html = """<html><body><div align="center">$errorMessage</div></body>"""

                    loadUrl("about:blank")
                    loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            }

            loadUrl(helixAuthUrl.toString())
        }
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }
}
