package com.atul.workouter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color


@Composable
fun Boxer(content: @Composable () -> Unit) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20))
            .background(
            color=MaterialTheme.colors.primary
        )
    ) {
        content()
    }
}