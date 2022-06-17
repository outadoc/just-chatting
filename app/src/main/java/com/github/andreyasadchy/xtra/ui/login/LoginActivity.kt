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
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.convertDpToPixels
import com.github.andreyasadchy.xtra.util.isDarkMode
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.shortToast
import com.github.andreyasadchy.xtra.util.toast
import kotlinx.android.synthetic.main.activity_login.havingTrouble
import kotlinx.android.synthetic.main.activity_login.progressBar
import kotlinx.android.synthetic.main.activity_login.webView
import kotlinx.android.synthetic.main.activity_login.webViewContainer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Injectable {

    @Inject
    lateinit var repository: AuthRepository

    private val tokenPattern = Pattern.compile("token=(.+?)(?=&)")

    private var tokens = 0
    private var userId = ""
    private var userLogin = ""
    private var helixToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val helixClientId = prefs().getString(C.HELIX_CLIENT_ID, "") ?: ""
        val user = User.get(this)

        if (user !is NotLoggedIn) {
            TwitchApiHelper.checkedValidation = false
            User.set(this, null)

            GlobalScope.launch {
                try {
                    if (!user.helixToken.isNullOrBlank()) {
                        repository.revoke(helixClientId, user.helixToken)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        initWebView(helixClientId)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(helixClientId: String) {
        webViewContainer.isVisible = true

        val helixScopes = listOf(
            "chat:read",
            "chat:edit",
            "channel:moderate",
            "channel_editor",
            "whispers:edit",
            "user:read:follows"
        )

        val helixRedirect = prefs().getString(C.HELIX_REDIRECT, "https://localhost")
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
        val matcher = tokenPattern.matcher(url)
        return if (matcher.find() && tokens < 2) {
            webViewContainer.isVisible = false
            progressBar.isVisible = true

            val token = matcher.group(1)!!
            if (tokens == 0) {
                lifecycleScope.launch {
                    try {
                        val response = repository.validate(
                            TwitchApiHelper.addTokenPrefixHelix(token)
                        )

                        if (response != null) {
                            userId = response.userId
                            userLogin = response.login
                            helixToken = token
                        } else {
                            throw IOException()
                        }
                    } catch (e: Exception) {
                        toast(R.string.connection_error)
                    }
                }
            } else {
                lifecycleScope.launch {
                    try {
                        val response = repository.validate(TwitchApiHelper.addTokenPrefixGQL(token))
                        if (response != null) {
                            userId = response.userId
                            userLogin = response.login

                            if (helixToken.isNotBlank()) {
                                TwitchApiHelper.checkedValidation = true
                                User.set(
                                    this@LoginActivity,
                                    LoggedIn(userId, userLogin, helixToken)
                                )
                                setResult(RESULT_OK)
                                finish()
                            }
                        } else {
                            throw IOException()
                        }
                    } catch (e: Exception) {
                        toast(R.string.connection_error)
                    }
                }
            }

            tokens++
            true
        } else {
            false
        }
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }
}
