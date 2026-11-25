package com.iustinmoraru.prtracker.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.ui.auth.LoginActivity
import com.iustinmoraru.prtracker.ui.common.ThemedActivity
import com.iustinmoraru.prtracker.ui.components.PrimaryButton
import com.iustinmoraru.prtracker.ui.components.SecondaryButton
import com.iustinmoraru.prtracker.ui.settings.SettingsActivity
import com.iustinmoraru.prtracker.ui.theme.PrTheme

/** Main screen of the app (placeholder until the workout features are implemented). */
class MainActivity : ThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = container.authRepository.currentUser
        if (user == null) {
            startActivity(LoginActivity.intent(this))
            return
        }
        setThemedContent {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp, androidx.compose.ui.Alignment.CenterVertically)) {
                Text("Salut, ${user.firstName}!", color = PrTheme.colors.textPrimary)
                PrimaryButton(text = "Setări", onClick = { startActivity(SettingsActivity.intent(this@MainActivity)) })
                SecondaryButton(text = "Ieși din cont", onClick = {
                    container.authRepository.signOut()
                    startActivity(LoginActivity.intent(this@MainActivity))
                })
            }
        }
    }

    companion object {
        fun intent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
