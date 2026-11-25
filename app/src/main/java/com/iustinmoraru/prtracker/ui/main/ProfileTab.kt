package com.iustinmoraru.prtracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.ui.components.SecondaryButton
import com.iustinmoraru.prtracker.ui.theme.PrTheme

/** "Profil" tab: avatar, account info, shortcuts to settings and logout. */
@Composable
fun ProfileTab(
    state: MainUiState,
    onOpenSettings: () -> Unit,
    onAbout: () -> Unit,
    onSignOut: () -> Unit,
) {
    val colors = PrTheme.colors
    val user = state.user

    Column(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF563C88), Color(0xFF201B2F))))
                .border(2.dp, colors.accent, RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(user?.initials ?: "?", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            user?.displayName?.ifBlank { null } ?: user?.firstName.orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
        Text(
            stringResource(if (user?.isGoogleAccount == true) R.string.profile_google_account else R.string.profile_email_account),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(20.dp))
        SecondaryButton(
            text = stringResource(R.string.profile_sign_out),
            onClick = onSignOut,
            leadingIcon = Icons.AutoMirrored.Rounded.Logout,
        )
    }

    Spacer(Modifier.height(28.dp))
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
    ) {
        ProfileRow(Icons.Rounded.Palette, stringResource(R.string.profile_settings_theme), onOpenSettings)
        HorizontalDivider(color = colors.border)
        ProfileRow(Icons.Rounded.Notifications, stringResource(R.string.profile_notifications), onOpenSettings)
        HorizontalDivider(color = colors.border)
        ProfileRow(Icons.Rounded.Info, stringResource(R.string.profile_about), onAbout)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    val colors = PrTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.textMuted)
    }
}
