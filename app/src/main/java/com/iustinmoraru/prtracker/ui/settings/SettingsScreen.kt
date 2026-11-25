package com.iustinmoraru.prtracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iustinmoraru.prtracker.BuildConfig
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.settings.ThemeMode
import com.iustinmoraru.prtracker.ui.components.Eyebrow
import com.iustinmoraru.prtracker.ui.components.IconTile
import com.iustinmoraru.prtracker.ui.components.PrCard
import com.iustinmoraru.prtracker.ui.components.ScreenHeader
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.OnViolet
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import com.iustinmoraru.prtracker.ui.theme.Violet

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.settings_eyebrow),
            title = stringResource(R.string.settings_title),
            onBack = onBack,
        )

        Spacer(Modifier.height(20.dp))
        Eyebrow(stringResource(R.string.settings_section_appearance))
        Spacer(Modifier.height(10.dp))
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeOption(
                mode = ThemeMode.SYSTEM,
                icon = Icons.Rounded.BrightnessAuto,
                title = stringResource(R.string.theme_system),
                subtitle = stringResource(R.string.theme_system_desc),
                selected = themeMode == ThemeMode.SYSTEM,
                onSelect = viewModel::onThemeSelected,
            )
            ThemeOption(
                mode = ThemeMode.LIGHT,
                icon = Icons.Rounded.LightMode,
                title = stringResource(R.string.theme_light),
                subtitle = stringResource(R.string.theme_light_desc),
                selected = themeMode == ThemeMode.LIGHT,
                onSelect = viewModel::onThemeSelected,
            )
            ThemeOption(
                mode = ThemeMode.DARK,
                icon = Icons.Rounded.DarkMode,
                title = stringResource(R.string.theme_dark),
                subtitle = stringResource(R.string.theme_dark_desc),
                selected = themeMode == ThemeMode.DARK,
                onSelect = viewModel::onThemeSelected,
            )
        }

        Spacer(Modifier.height(26.dp))
        Eyebrow(stringResource(R.string.settings_section_notifications))
        Spacer(Modifier.height(10.dp))
        PrCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Rounded.Notifications, background = Violet, tint = OnViolet)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(stringResource(R.string.settings_notifications_title), style = MaterialTheme.typography.titleSmall, color = PrTheme.colors.textPrimary)
                    Text(stringResource(R.string.settings_notifications_desc), style = MaterialTheme.typography.bodySmall, color = PrTheme.colors.textMuted)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = viewModel::onNotificationsToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnLime,
                        checkedTrackColor = Lime,
                    ),
                )
            }
        }

        Spacer(Modifier.height(26.dp))
        Eyebrow(stringResource(R.string.settings_section_about))
        Spacer(Modifier.height(10.dp))
        PrCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Rounded.Info, background = PrTheme.colors.cardStrong, tint = PrTheme.colors.textSecondary)
                Column(Modifier.padding(horizontal = 12.dp)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleSmall, color = PrTheme.colors.textPrimary)
                    Text(
                        stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall,
                        color = PrTheme.colors.textMuted,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** One selectable theme row; the whole row is a radio button for accessibility. */
@Composable
private fun ThemeOption(
    mode: ThemeMode,
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: (ThemeMode) -> Unit,
) {
    val colors = PrTheme.colors
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.accent.copy(alpha = 0.08f) else colors.card)
            .border(1.dp, if (selected) colors.accentText.copy(alpha = 0.6f) else colors.border, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(mode) })
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, background = if (selected) Lime else colors.cardStrong, tint = if (selected) OnLime else colors.textSecondary)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        if (selected) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(22.dp))
        }
    }
}
