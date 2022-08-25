package com.github.andreyasadchy.xtra.ui.login

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
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.repository.AuthPreferencesRepository
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.convertDpToPixels
import com.github.andreyasadchy.xtra.util.isDarkMode
import com.github.andreyasadchy.xtra.util.shortToast
import com.github.andreyasadchy.xtra.util.toast
import kotlinx.android.synthetic.main.activity_login.havingTrouble
import kotlinx.android.synthetic.main.activity_login.progressBar
import kotlinx.android.synthetic.main.activity_login.webView
import kotlinx.android.synthetic.main.activity_login.webViewContainer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Injectable {

    @Inject
    lateinit var repository: AuthRepository

    @Inject
    lateinit var authPreferencesRepository: AuthPreferencesRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lifecycleScope.launch {
            val user = userPreferencesRepository.user.first()
            if (user !is User.NotLoggedIn) {
                TwitchApiHelper.checkedValidation = false
                userPreferencesRepository.updateUser(null)

                try {
                    val token = user.helixToken
                    if (!token.isNullOrBlank()) {
                        repository.revokeToken()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        lifecycleScope.launch {
            combine(
                authPreferencesRepository.helixClientId,
                authPreferencesRepository.helixRedirect
            ) { helixClientId, helixRedirect ->
                helixClientId to helixRedirect
            }.collect { (clientId, redirect) ->
                initWebView(clientId, redirect)
            }
        }
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
                    failingUrl: String,
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

        val token = matcher.group(1)!!
        lifecycleScope.launch {
            try {
                val response = repository.validate(token) ?: throw IOException()
                userPreferencesRepository.updateUser(
                    user = User.LoggedIn(
                        id = response.userId,
                        login = response.login,
                        helixToken = token
                    )
                )

                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                toast(R.string.connection_error)
            }
        }

        return true
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }
}
