package com.iustinmoraru.prtracker.ui.main

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.ui.common.ThemedActivity
import com.iustinmoraru.prtracker.ui.components.PrimaryButton
import com.iustinmoraru.prtracker.ui.settings.SettingsActivity

/** Main screen of the app (placeholder until the features are implemented). */
class MainActivity : ThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setThemedContent {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                PrimaryButton(text = "Setări", onClick = { startActivity(SettingsActivity.intent(this@MainActivity)) })
            }
        }
    }
}
