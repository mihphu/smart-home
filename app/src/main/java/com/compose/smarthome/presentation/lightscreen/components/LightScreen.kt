package com.compose.smarthome.presentation.lightscreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.smarthome.R
import com.compose.smarthome.presentation.lightscreen.LightScreenEvent
import com.compose.smarthome.presentation.lightscreen.LightScreenUiState
import com.compose.smarthome.presentation.lightscreen.LightScreenViewModel
import com.compose.smarthome.ui.theme.ColorBrown
import com.compose.smarthome.ui.theme.ColorCyan
import com.compose.smarthome.ui.theme.ColorGreen
import com.compose.smarthome.ui.theme.ColorOrange
import com.compose.smarthome.ui.theme.ColorPurple
import com.compose.smarthome.ui.theme.ColorRed
import com.compose.smarthome.ui.theme.ProductSans
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun LightScreen(
    viewModel: LightScreenViewModel = koinViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LightScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun LightScreenContent(
    state: LightScreenUiState,
    onEvent: (LightScreenEvent) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val animLightColor by animateColorAsState(
        targetValue = state.lightColor,
        animationSpec = tween(durationMillis = 500)
    )

    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(
            targetScale = 0.85f,
            animationSpec = tween(150)
        )
    ) {
        ColorPickerDialog(
            color = state.lightColor,
            onColorSelected = { color ->
                onEvent(LightScreenEvent.ChangeLightColor(color))
                showDialog = false
            },
            onDismissRequest = { showDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(400.dp)
        ) {
            LightBeam(
                modifier = Modifier
                    .padding(start = 50.dp, top = 230.dp)
                    .size(
                        width = 200.dp,
                        height = 300.dp
                    ),
                isVisible = state.isLightOn,
                lightOpacity = state.brightness,
                lightColor = { animLightColor }
            )
            Image(
                painter = painterResource(R.drawable.img_red_light),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 50.dp)
                    .size(
                        width = 200.dp,
                        height = 250.dp
                    )
            )
            LightBeam(
                modifier = Modifier
                    .padding(top = 180.dp)
                    .offset(x = (-25).dp)
                    .size(
                        width = 200.dp,
                        height = 300.dp
                    ),
                isVisible = state.isLightOn,
                topConeWidth = 240f,
                lightOpacity = state.brightness,
                lightColor = { animLightColor }
            )
            Image(
                painter = painterResource(R.drawable.img_black_light),
                contentDescription = null,
                modifier = Modifier
                    .size(
                        width = 150.dp,
                        height = 200.dp
                    )
            )
            RealisticRopeSwitch(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(100.dp)
                    .height(400.dp),
                isOn = state.isLightOn,
                onCheckedChange = { isChecked ->
                    onEvent(LightScreenEvent.ToggleLightOn(isOn = isChecked))
                },
                ropeLength = 200.dp,
                ropeWidth = 4.dp,
                ringWidth = 40.dp,
                ringHeight = 54.dp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color.White,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Schedule",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ProductSans,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "From",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ProductSans,
                    color = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildTimeText("7:00"),
                    style = TextStyle(
                        fontFamily = ProductSans
                    )
                )
                Text(
                    text = "To",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ProductSans,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = buildTimeText("12:00"),
                    style = TextStyle(
                        fontFamily = ProductSans
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Brightness",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ProductSans,
                        color = Color.Black
                    )
                )

                Text(
                    text = "${(state.brightness * 100f).roundToInt()}%",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ProductSans,
                        color = Color.Black
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AppSeekbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                progress = state.brightness,
                onProgressChanged = { value ->
                    onEvent(LightScreenEvent.ChangeBrightness(value))
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 0.dp,
                    bottom = 16.dp
                )
        ) {
            Text(
                text = "Color of Lights",
                style = TextStyle(
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ProductSans,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .padding(0.dp),
                    content = {
                        Image(
                            painter = painterResource(R.drawable.ic_color_picker),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
                VerticalDivider(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .height(48.dp),
                    color = Color.Gray.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 8.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.lightColors,
                        key = { it.toArgb() }
                    ) { item ->
                        ColorItem(
                            color = item,
                            isSelected = item == state.lightColor,
                            onClick = { color ->
                                onEvent(LightScreenEvent.ChangeLightColor(color))
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun buildTimeText(time: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        ) {
            append(time)
        }

        withStyle(
            style = SpanStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        ) {
            append(" PM")
        }
    }
}

@Preview(showBackground = false, showSystemUi = true)
@Composable
fun LightScreenPreview() {
    LightScreenContent(
        state = LightScreenUiState(
            isLightOn = true,
            lightColor = ColorOrange,
            brightness = 0.7f,
            lightColors = listOf(
                ColorPurple,
                ColorOrange,
                ColorCyan,
                ColorGreen,
                ColorRed,
                ColorBrown
            )
        ),
        onEvent = {}
    )
}
