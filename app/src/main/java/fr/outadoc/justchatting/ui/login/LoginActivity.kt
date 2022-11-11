package fr.outadoc.justchatting.ui.login

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import fr.outadoc.justchatting.ui.theme.AppTheme

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                LoginScreen(
                    onDone = { finish() }
                )
            }
        }
    }
}
