package com.iustinmoraru.prtracker.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.iustinmoraru.prtracker.BuildConfig
import com.iustinmoraru.prtracker.R
import com.iustinmoraru.prtracker.data.workout.Workout
import com.iustinmoraru.prtracker.domain.stats.PersonalRecord
import com.iustinmoraru.prtracker.ui.components.IconSquareButton
import com.iustinmoraru.prtracker.ui.theme.Lime
import com.iustinmoraru.prtracker.ui.theme.OnLime
import com.iustinmoraru.prtracker.ui.theme.PrTheme
import kotlinx.coroutines.launch

/** Tabs of the bottom navigation (the "+" button opens a separate activity). */
enum class MainTab { HOME, PROGRESS, TIMER, PROFILE }

/** Navigation callbacks that leave the main screen. Implemented by [MainActivity]. */
data class MainNavigation(
    val addWorkout: () -> Unit,
    val openWorkout: (Workout) -> Unit,
    val editWorkout: (Workout) -> Unit,
    val openRecord: (PersonalRecord) -> Unit,
    val openSettings: () -> Unit,
)

@Composable
fun MainScreen(
    state: MainUiState,
    navigation: MainNavigation,
    onToggleStatus: (Workout) -> Unit,
    onDelete: (Workout) -> Unit,
    onSignOut: () -> Unit,
    onMessageShown: () -> Unit,
    timerContent: @Composable () -> Unit,
) {
    val colors = PrTheme.colors
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var pendingDelete by remember { mutableStateOf<Workout?>(null) }
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        val text = when (message) {
            is MainMessage.NewRecords -> context.getString(R.string.toast_new_record, message.exercises.joinToString { it.name })
            is MainMessage.StatusChanged ->
                context.getString(if (message.completed) R.string.detail_marked_done else R.string.detail_marked_planned)
            is MainMessage.Deleted -> context.getString(R.string.main_deleted, message.name)
            MainMessage.ActionFailed -> context.getString(R.string.detail_action_failed)
        }
        onMessageShown()
        snackbarHostState.showSnackbar(text)
    }

    val actions = WorkoutActions(
        open = navigation.openWorkout,
        edit = navigation.editWorkout,
        toggleStatus = onToggleStatus,
        delete = { pendingDelete = it },
    )

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            MainTopBar(
                onSettings = navigation.openSettings,
                onAdd = navigation.addWorkout,
                onAbout = { showAbout = true },
                onSignOut = { confirmSignOut = true },
                onNotifications = {
                    val next = state.dashboard.nextPlanned
                    val text = if (next != null) {
                        context.getString(R.string.main_next_workout, next.name)
                    } else {
                        context.getString(R.string.main_no_notifications)
                    }
                    snackbarHostState.currentSnackbarData?.dismiss()
                    scope.launch { snackbarHostState.showSnackbar(text) }
                },
            )
            AnimatedContent(
                targetState = tab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab",
                modifier = Modifier.weight(1f),
            ) { current ->
                if (current == MainTab.TIMER) {
                    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { timerContent() }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 120.dp),
                    ) {
                        when (current) {
                            MainTab.HOME -> HomeTab(state, onAdd = navigation.addWorkout, actions = actions)
                            MainTab.PROGRESS -> ProgressTab(state, onRecordClick = navigation.openRecord)
                            MainTab.PROFILE -> ProfileTab(
                                state = state,
                                onOpenSettings = navigation.openSettings,
                                onAbout = { showAbout = true },
                                onSignOut = { confirmSignOut = true },
                            )
                            MainTab.TIMER -> Unit
                        }
                    }
                }
            }
        }

        SnackbarHost(
            snackbarHostState,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 92.dp, start = 16.dp, end = 16.dp),
        )

        BottomNavBar(
            selected = tab,
            onSelect = { tab = it },
            onAdd = navigation.addWorkout,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
        )
    }

    pendingDelete?.let { workout ->
        ConfirmDialog(
            title = stringResource(R.string.detail_delete_title),
            message = stringResource(R.string.detail_delete_message, workout.name),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                pendingDelete = null
                onDelete(workout)
            },
            onDismiss = { pendingDelete = null },
        )
    }
    if (confirmSignOut) {
        ConfirmDialog(
            title = stringResource(R.string.main_sign_out_title),
            message = stringResource(R.string.main_sign_out_message),
            confirmLabel = stringResource(R.string.profile_sign_out),
            onConfirm = {
                confirmSignOut = false
                onSignOut()
            },
            onDismiss = { confirmSignOut = false },
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(stringResource(R.string.main_about_text, BuildConfig.VERSION_NAME)) },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text(stringResource(R.string.action_ok), color = colors.accentText) } },
            containerColor = colors.sheet,
        )
    }
}

/** Header from the design: menu button (options menu), logo and notifications bell. */
@Composable
private fun MainTopBar(
    onSettings: () -> Unit,
    onAdd: () -> Unit,
    onAbout: () -> Unit,
    onSignOut: () -> Unit,
    onNotifications: () -> Unit,
) {
    val colors = PrTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            IconSquareButton(Icons.Rounded.Menu, stringResource(R.string.cd_open_menu), { menuOpen = true })
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                MenuItem(Icons.Rounded.Add, stringResource(R.string.menu_add_workout)) { menuOpen = false; onAdd() }
                MenuItem(Icons.Rounded.Settings, stringResource(R.string.settings_title)) { menuOpen = false; onSettings() }
                MenuItem(Icons.Rounded.Info, stringResource(R.string.profile_about)) { menuOpen = false; onAbout() }
                HorizontalDivider(color = colors.border)
                MenuItem(Icons.AutoMirrored.Rounded.Logout, stringResource(R.string.profile_sign_out)) { menuOpen = false; onSignOut() }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Lime),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = OnLime, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.brand_title), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
        IconSquareButton(Icons.Rounded.Notifications, stringResource(R.string.cd_notifications), onNotifications, badge = true)
    }
}

@Composable
private fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
    )
}

/** Floating bottom navigation with the raised lime "+" button in the middle. */
@Composable
private fun BottomNavBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PrTheme.colors
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, shape)
            .clip(shape)
            .background(if (colors.isDark) colors.sheet else colors.card)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 5.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavItem(Icons.Rounded.Home, stringResource(R.string.nav_home), selected == MainTab.HOME, { onSelect(MainTab.HOME) }, Modifier.weight(1f))
        NavItem(Icons.Rounded.BarChart, stringResource(R.string.nav_progress), selected == MainTab.PROGRESS, { onSelect(MainTab.PROGRESS) }, Modifier.weight(1f))
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Lime)
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.menu_add_workout), onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.menu_add_workout), tint = OnLime, modifier = Modifier.size(26.dp))
            }
        }
        NavItem(Icons.Rounded.Timer, stringResource(R.string.nav_timer), selected == MainTab.TIMER, { onSelect(MainTab.TIMER) }, Modifier.weight(1f))
        NavItem(Icons.Rounded.AccountCircle, stringResource(R.string.nav_profile), selected == MainTab.PROFILE, { onSelect(MainTab.PROFILE) }, Modifier.weight(1f))
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = PrTheme.colors
    val tint = if (isSelected) colors.accentText else colors.textMuted
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing), color = tint, maxLines = 1)
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = PrTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel, color = colors.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = colors.textPrimary) } },
        containerColor = colors.sheet,
    )
}
