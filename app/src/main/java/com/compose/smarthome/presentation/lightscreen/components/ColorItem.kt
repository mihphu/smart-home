package com.compose.smarthome.presentation.lightscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: (Color) -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                color = if (isSelected) Color.Black else color,
                shape = CircleShape
            )
            .clickable {
                onClick(color)
            }
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Preview(showBackground = false)
@Composable
fun ColorItemPreviewSelected() {
    ColorItem(
        color = Color.Yellow,
        isSelected = true,
        onClick = {}
    )
}

@Preview(showBackground = false)
@Composable
fun ColorItemPreviewUnselected() {
    ColorItem(
        color = Color.Yellow,
        isSelected = false,
        onClick = {}
    )
}