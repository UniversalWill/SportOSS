package com.universalwill.sportoss.ui.screens.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.universalwill.sportoss.R
import com.universalwill.sportoss.ui.theme.dimensions

@Composable
internal fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(
        title = stringResource(R.string.activities_load_error),
        supportingText = stringResource(R.string.activities_load_error_hint),
        buttonLabel = stringResource(R.string.retry),
        onButtonClick = onRetry,
        modifier = modifier,
    )
}

@Composable
internal fun EmptyContent(
    onStartActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(
        title = stringResource(R.string.activities_empty_title),
        supportingText = stringResource(R.string.activities_empty_description),
        buttonLabel = stringResource(R.string.start_workout),
        onButtonClick = onStartActivity,
        modifier = modifier,
        showIcon = true,
    )
}

@Composable
private fun CenteredMessage(
    title: String,
    supportingText: String,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimensions.spacingHuge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.dimensions.iconLarge),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingSmall))
        Text(
            text = supportingText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingExtraLarge))
        Button(onClick = onButtonClick) {
            Text(buttonLabel)
        }
    }
}
