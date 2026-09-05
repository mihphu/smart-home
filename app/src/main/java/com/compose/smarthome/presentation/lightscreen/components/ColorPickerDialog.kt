package com.compose.smarthome.presentation.lightscreen.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.compose.smarthome.R
import com.compose.smarthome.common.extensions.toHex
import com.compose.smarthome.ui.theme.ColorPurple
import com.compose.smarthome.ui.theme.ProductSans
import com.happytech.colorpickerview.compose.ColorAlphaSlider
import com.happytech.colorpickerview.compose.ColorPicker
import com.happytech.colorpickerview.compose.ColorPickerDefaults
import com.happytech.colorpickerview.compose.HueSlider
import com.happytech.colorpickerview.compose.rememberColorPickerState

@Composable
fun ColorPickerDialog(
    color: Color,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ColorPickerDialogContent(color, onColorSelected)
    }
}

@Composable
private fun ColorPickerDialogContent(
    color: Color,
    onColorSelected: (Color) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        val colorState = rememberColorPickerState(color)
        val thumb = ColorPickerDefaults.thumb(
            radius = 11.dp,
            strokeSize = 4.5.dp,
            outlineSize = 1.dp
        )
        val colors = ColorPickerDefaults.colors(
            thumbOutline = Color.Black.copy(alpha = 0.1f)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Choose color",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = ProductSans,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ColorHexLabel(color = { colorState.color })
                }
                ApplyColorButton(
                    color = { colorState.color },
                    onClick = { onColorSelected.invoke(colorState.color) }
                )
            }
            ColorPicker(
                state = colorState,
                modifier = Modifier.height(240.dp),
                colors = colors,
                thumb = thumb,
                cornerRadius = 16.dp,
                outlineWidth = 1.dp
            )
            HueSlider(
                state = colorState,
                modifier = Modifier
                    .height(21.dp)
                    .padding(horizontal = 8.dp),
                colors = colors,
                thumb = thumb,
                trackThickness = 8.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            ColorAlphaSlider(
                state = colorState,
                modifier = Modifier
                    .height(21.dp)
                    .padding(horizontal = 8.dp),
                colors = colors,
                thumb = thumb,
                showChecker = true,
                checkerRows = 2,
                trackThickness = 8.dp
            )
        }
    }
}

@Composable
private fun ApplyColorButton(
    color: () -> Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = color()
    val glow = current.copy(alpha = 1f)
    val checkTint = if (lerp(Color.White, glow, current.alpha).luminance() > 0.55f) {
        Color.Black
    } else {
        Color.White
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .drawBehind {
                val radius = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        0.00f to glow.copy(alpha = 0.42f * current.alpha),
                        0.70f to glow.copy(alpha = 0.34f * current.alpha),
                        1.00f to Color.Transparent,
                        center = center,
                        radius = radius
                    ),
                    radius = radius
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawCheckerboard()
                    drawRect(color = color())
                }
                .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Apply color",
                tint = checkTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ColorHexLabel(color: () -> Color) {
    Text(
        text = color().toHex(),
        style = TextStyle(
            fontSize = 13.sp,
            fontFamily = ProductSans,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp,
            color = Color.Black.copy(alpha = 0.45f)
        )
    )
}

private fun DrawScope.drawCheckerboard(cell: Float = 8f) {
    drawRect(color = Color.White)
    var row = 0
    var y = 0f
    while (y < size.height) {
        var column = 0
        var x = 0f
        while (x < size.width) {
            if ((row + column) % 2 == 0) {
                drawRect(
                    color = Color(0xFFE4E4E4),
                    topLeft = Offset(x, y),
                    size = Size(
                        width = minOf(cell, size.width - x),
                        height = minOf(cell, size.height - y)
                    )
                )
            }
            x += cell
            column++
        }
        y += cell
        row++
    }
}

@Preview(showBackground = false)
@Composable
fun ColorPickerDialogPreview() {
    ColorPickerDialogContent(
        color = ColorPurple,
        onColorSelected = {}
    )
}

@Preview(showBackground = false, name = "Light color - dark check")
@Composable
fun ColorPickerDialogLightColorPreview() {
    ColorPickerDialogContent(
        color = Color(0xFFFFE27A),
        onColorSelected = {}
    )
}

@Preview(showBackground = false, name = "Semi-transparent")
@Composable
fun ColorPickerDialogTransparentPreview() {
    ColorPickerDialogContent(
        color = ColorPurple.copy(alpha = 0.35f),
        onColorSelected = {}
    )
}
