package com.iustinmoraru.prtracker.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.iustinmoraru.prtracker.ui.common.ThemedActivity

/** Settings screen, opened from the Profile tab or from the main menu. */
class SettingsActivity : ThemedActivity() {

    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setThemedContent {
            SettingsScreen(viewModel = viewModel, onBack = ::finish)
        }
    }

    companion object {
        fun intent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}
