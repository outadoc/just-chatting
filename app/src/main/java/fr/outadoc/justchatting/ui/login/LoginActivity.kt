package fr.outadoc.justchatting.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.di.Injectable
import fr.outadoc.justchatting.util.convertDpToPixels
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.shortToast
import fr.outadoc.justchatting.util.toast
import kotlinx.android.synthetic.main.activity_login.havingTrouble
import kotlinx.android.synthetic.main.activity_login.progressBar
import kotlinx.android.synthetic.main.activity_login.webView
import kotlinx.android.synthetic.main.activity_login.webViewContainer
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<LoginViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel.state.observe(this) { state ->
            when (state) {
                LoginViewModel.State.Initial -> {}
                is LoginViewModel.State.LoadWebView -> {
                    state.exception?.let {
                        toast(R.string.connection_error)
                    }

                    initWebView(state.clientId, state.redirect)
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
    private fun initWebView(helixClientId: String, helixRedirect: String) {
        webViewContainer.isVisible = true

        val helixScopes = listOf(
            "chat:read",
            "chat:edit",
            "channel:moderate",
            "channel_editor",
            "whispers:edit",
            "user:read:follows"
        )

        val helixAuthUrl =
            "https://id.twitch.tv/oauth2/authorize".toHttpUrl()
                .newBuilder()
                .addQueryParameter("response_type", "token")
                .addQueryParameter("client_id", helixClientId)
                .addQueryParameter("redirect_uri", helixRedirect)
                .addQueryParameter("scope", helixScopes.joinToString(" "))
                .build()
                .toString()

        havingTrouble.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.login_problem_solution))
                .setPositiveButton(R.string.log_in) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, helixAuthUrl.toUri())
                    if (intent.resolveActivity(packageManager) != null) {
                        webView.reload()
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
                                if (!loginIfValidUrl(text.toString())) {
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

        with(webView) {
            if (isDarkMode) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
                }

                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                    WebSettingsCompat.setForceDarkStrategy(
                        this.settings,
                        WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY
                    )
                }
            }

            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    loginIfValidUrl(url)
                    return false
                }

                override fun onReceivedError(
                    view: WebView,
                    errorCode: Int,
                    description: String,
                    failingUrl: String
                ) {
                    val errorMessage = if (errorCode == -11) {
                        getString(R.string.browser_workaround)
                    } else {
                        getString(R.string.error, "$errorCode $description")
                    }

                    val html = "<html><body><div align=\"center\">$errorMessage</div></body>"
                    loadUrl("about:blank")
                    loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            }

            loadUrl(helixAuthUrl)
        }
    }

    private fun loginIfValidUrl(url: String): Boolean {
        val matcher = "token=(.+?)(?=&)".toPattern().matcher(url)
        if (!matcher.find()) return false

        webViewContainer.isVisible = false
        progressBar.isVisible = true

        viewModel.onTokenReceived(token = matcher.group(1)!!)
        return true
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }
}
