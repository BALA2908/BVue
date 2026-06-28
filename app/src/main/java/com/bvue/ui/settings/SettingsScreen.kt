package com.bvue.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bvue.BVueApplication
import com.bvue.domain.model.AudioLanguage
import com.bvue.domain.model.QualityPref
import com.bvue.domain.model.SponsorCategory
import com.bvue.domain.model.ThemeMode

@Composable
fun SettingsScreen(onBack: (() -> Unit)? = null) {
    val app = LocalContext.current.applicationContext as BVueApplication
    val vm: SettingsViewModel = viewModel(
        factory = viewModelFactory { initializer { SettingsViewModel(app.container.settingsRepository) } },
    )
    val settings by vm.settings.collectAsStateWithLifecycle()

    var showQuality by remember { mutableStateOf(false) }
    var showAudio by remember { mutableStateOf(false) }
    var showTheme by remember { mutableStateOf(false) }
    var showSponsorCats by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(Modifier.width(16.dp))
            }
            Text("Settings", style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        SettingRow("Default quality", settings.defaultQuality.label) { showQuality = true }
        SettingRow(
            "Preferred audio language",
            AudioLanguage.fromCode(settings.preferredAudioLanguage).label,
        ) { showAudio = true }
        SettingRow("Theme", settings.themeMode.label()) { showTheme = true }
        ListItem(
            headlineContent = { Text("Audio-only mode") },
            supportingContent = { Text("Play only the audio (lighter on data and battery)") },
            trailingContent = {
                Switch(checked = settings.audioOnly, onCheckedChange = { vm.setAudioOnly(it) })
            },
            modifier = Modifier.clickable { vm.setAudioOnly(!settings.audioOnly) },
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ListItem(
            headlineContent = { Text("Skip sponsor segments") },
            supportingContent = { Text("Auto-skip sponsor & promo parts using SponsorBlock") },
            trailingContent = {
                Switch(
                    checked = settings.sponsorBlockEnabled,
                    onCheckedChange = { vm.setSponsorBlockEnabled(it) },
                )
            },
            modifier = Modifier.clickable { vm.setSponsorBlockEnabled(!settings.sponsorBlockEnabled) },
        )
        if (settings.sponsorBlockEnabled) {
            SettingRow("Segments to skip", "${settings.sponsorCategories.size} selected") {
                showSponsorCats = true
            }
        }
    }

    if (showQuality) {
        RadioDialog(
            title = "Default quality",
            options = QualityPref.entries.map { it to it.label },
            selected = settings.defaultQuality,
            onSelect = { vm.setQuality(it); showQuality = false },
            onDismiss = { showQuality = false },
        )
    }
    if (showAudio) {
        RadioDialog(
            title = "Preferred audio language",
            options = AudioLanguage.entries.map { it.code to it.label },
            selected = settings.preferredAudioLanguage,
            onSelect = { vm.setAudioLanguage(it); showAudio = false },
            onDismiss = { showAudio = false },
        )
    }
    if (showTheme) {
        RadioDialog(
            title = "Theme",
            options = ThemeMode.entries.map { it to it.label() },
            selected = settings.themeMode,
            onSelect = { vm.setTheme(it); showTheme = false },
            onDismiss = { showTheme = false },
        )
    }
    if (showSponsorCats) {
        CheckboxDialog(
            title = "Segments to skip",
            options = SponsorCategory.entries.map { it.apiName to it.label },
            selected = settings.sponsorCategories,
            onConfirm = { vm.setSponsorCategories(it); showSponsorCats = false },
            onDismiss = { showSponsorCats = false },
        )
    }
}

private fun ThemeMode.label(): String = name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun <T> RadioDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == selected, onClick = { onSelect(value) })
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

@Composable
private fun CheckboxDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var working by remember { mutableStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    val checked = value in working
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                working = if (checked) working - value else working + value
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { working = if (checked) working - value else working + value },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(working) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
