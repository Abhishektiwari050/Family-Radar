package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Neo-Brutalist Colors
val NeoBgPrimary = Color(0xFFF4F4F6)
val NeoBgSecondary = Color(0xFFE6E6EB)
val NeoOutline = Color(0xFF000000)
val NeoText = Color(0xFF000000)
val NeoAccentGreen = Color(0xFFD2FF00)
val NeoAccentPink = Color(0xFFFF007F)
val NeoWhite = Color(0xFFFFFFFF)

// Neo-Brutalist Design Tokens
object NeoBrutalist {
    val BorderWidth = 3.dp
    val ShortBorderWidth = 2.dp
    val ShadowOffsetDefault = 5.dp
    val ShadowOffsetShort = 3.dp
    val CornerRadiusDefault = 4.dp
    val CornerRadiusRound = 12.dp
}

/**
 * Draws a solid flat drop shadow under the component.
 */
fun Modifier.neoShadow(
    shadowColor: Color = NeoOutline,
    offsetX: Dp = NeoBrutalist.ShadowOffsetDefault,
    offsetY: Dp = NeoBrutalist.ShadowOffsetDefault,
    cornerRadius: Dp = NeoBrutalist.CornerRadiusDefault
): Modifier = this.drawBehind {
    val cornerRadiusPx = cornerRadius.toPx()
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}

/**
 * Draws the thick high-contrast black border used in Neo-Brutalist aesthetics.
 */
fun Modifier.neoBorder(
    width: Dp = NeoBrutalist.BorderWidth,
    color: Color = NeoOutline,
    cornerRadius: Dp = NeoBrutalist.CornerRadiusDefault
): Modifier = this.border(
    width = width,
    color = color,
    shape = RoundedCornerShape(cornerRadius)
)

/**
 * Combined neo shadow, border, background for standard layout blocks.
 */
fun Modifier.neoCard(
    backgroundColor: Color = NeoWhite,
    borderColor: Color = NeoOutline,
    borderWidth: Dp = NeoBrutalist.BorderWidth,
    shadowColor: Color = NeoOutline,
    shadowOffset: Dp = NeoBrutalist.ShadowOffsetDefault,
    cornerRadius: Dp = NeoBrutalist.CornerRadiusDefault
): Modifier = this
    .neoShadow(shadowColor, shadowOffset, shadowOffset, cornerRadius)
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .neoBorder(borderWidth, borderColor, cornerRadius)

@Composable
fun NeoSticker(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = NeoOutline
) {
    Box(
        modifier = modifier
            .neoShadow(NeoOutline, 2.dp, 2.dp, 2.dp)
            .background(backgroundColor, RoundedCornerShape(2.dp))
            .neoBorder(2.dp, NeoOutline, 2.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}
