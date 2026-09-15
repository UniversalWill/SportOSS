package com.universalwill.sportoss.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.universalwill.sportoss.BuildConfig
import com.universalwill.sportoss.R
import com.universalwill.sportoss.domain.model.AppLanguage
import com.universalwill.sportoss.domain.model.UserPreferences
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
internal fun SettingsContent(
    preferences: UserPreferences,
    appLanguage: AppLanguage,
    onAppLanguageClick: () -> Unit,
    onThemeModeClick: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onMapLanguageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = MaterialTheme.dimensions.spacingExtraLarge),
    ) {
        item { SettingsSectionTitle(stringResource(R.string.settings_section_appearance)) }
        item {
            SettingsValueRow(
                title = stringResource(R.string.settings_app_language),
                value = appLanguageLabel(appLanguage),
                onClick = onAppLanguageClick,
            )
        }
        item { SettingsDivider() }
        item {
            SettingsValueRow(
                title = stringResource(R.string.settings_theme),
                value = themeModeLabel(preferences.themeMode),
                onClick = onThemeModeClick,
            )
        }
        item { SettingsDivider() }
        item {
            SettingsSwitchRow(
                title = stringResource(R.string.settings_dynamic_colors),
                supportingText = stringResource(R.string.settings_dynamic_colors_description),
                checked = preferences.dynamicColorEnabled,
                onCheckedChange = onDynamicColorChanged,
            )
        }
        item { SettingsSectionTitle(stringResource(R.string.settings_section_map)) }
        item {
            SettingsValueRow(
                title = stringResource(R.string.settings_map_language),
                value = mapLanguageLabel(preferences.mapLabelLanguage),
                onClick = onMapLanguageClick,
            )
        }
        item { SettingsSectionTitle(stringResource(R.string.settings_section_about)) }
        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_name)) },
                supportingContent = { Text(stringResource(R.string.settings_about_description)) },
                trailingContent = {
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = MaterialTheme.dimensions.spacingExtraLarge,
            top = MaterialTheme.dimensions.spacingLarge,
            end = MaterialTheme.dimensions.spacingExtraLarge,
            bottom = MaterialTheme.dimensions.spacingSmall,
        ),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SettingsValueRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(supportingText) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        },
        modifier = modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = MaterialTheme.dimensions.spacingExtraLarge),
    )
}

internal data class SettingsOption<T>(
    val value: T,
    val label: String,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun <T> SettingsOptionPicker(
    title: String,
    options: List<SettingsOption<T>>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            item {
                Text(
                    text = title,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.dimensions.spacingExtraLarge,
                        vertical = MaterialTheme.dimensions.spacingMedium,
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            items(options) { option ->
                ListItem(
                    headlineContent = { Text(option.label) },
                    trailingContent = {
                        RadioButton(
                            selected = option.value == selectedValue,
                            onClick = null,
                        )
                    },
                    modifier = Modifier.clickable { onValueSelected(option.value) },
                )
            }
        }
    }
}
