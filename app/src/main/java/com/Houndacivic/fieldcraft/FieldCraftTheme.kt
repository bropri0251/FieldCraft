package com.Houndacivic.fieldcraft.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.Houndacivic.fieldcraft.R

@Composable
fun FieldCraftTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = colorResource(R.color.fc_primary),
        onPrimary = colorResource(R.color.fc_onPrimary),
        background = colorResource(R.color.fc_background),
        onBackground = colorResource(R.color.fc_onBackground),
        surface = colorResource(R.color.fc_surface),
        onSurface = colorResource(R.color.fc_onSurface),
        secondary = colorResource(R.color.fc_secondary),
        onSecondary = colorResource(R.color.fc_onSecondary),
        error = colorResource(R.color.fc_error),
        onError = colorResource(R.color.fc_onError),
    )
    MaterialTheme(
        colorScheme = scheme,
        typography = androidx.compose.material3.Typography(),
        shapes = androidx.compose.material3.Shapes(),
        content = content
    )
}
