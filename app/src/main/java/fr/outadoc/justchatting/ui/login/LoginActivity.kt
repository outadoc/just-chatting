package fr.outadoc.justchatting.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.ActivityLoginBinding
import fr.outadoc.justchatting.util.convertDpToPixels
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.shortToast
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
        viewHolder.webViewContainer.isVisible = true

        viewHolder.havingTrouble.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.login_problem_solution))
                .setPositiveButton(R.string.log_in) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, helixAuthUrl.toString().toUri())
                    if (intent.resolveActivity(packageManager) != null) {
                        viewHolder.webView.reload()
                        startActivity(intent)
                    } else {
                        toast(R.string.no_browser_found)
                    }
                }
                .setNeutralButton(R.string.to_enter_url) { _, _ ->
                    val editText = EditText(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            val margin = convertDpToPixels(10f)
                            setMargins(margin, 0, margin, 0)
                        }
                    }

                    val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.enter_url)
                        .setView(editText)
                        .setPositiveButton(R.string.log_in) { _, _ ->
                            val text = editText.text
                            if (text.isNotEmpty()) {
                                if (!viewModel.onNavigateToUrl(text.toString())) {
                                    shortToast(R.string.invalid_url)
                                }
                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()

                    dialog.window?.setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

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
