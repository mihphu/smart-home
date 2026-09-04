package com.compose.smarthome.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.compose.smarthome.R

val ProductSans = FontFamily(
    Font(R.font.product_sans_thin, FontWeight.Thin),
    Font(R.font.product_sans_light, FontWeight.Light),
    Font(R.font.product_sans_regular, FontWeight.Normal),
    Font(R.font.product_sans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.product_sans_medium, FontWeight.Medium),
    Font(R.font.product_sans_bold, FontWeight.Bold),
    Font(R.font.product_sans_black, FontWeight.Black),
    Font(R.font.product_sans_black_italic, FontWeight.Black, FontStyle.Italic)
)