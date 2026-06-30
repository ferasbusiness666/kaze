package com.kaze.browser.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaze.browser.ui.theme.KazeTheme

/** Thin wrapper so screens don't repeat painterResource + size each time. */
@Composable
fun KazeIcon(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = KazeTheme.colors.fg,
    size: Dp = 22.dp,
) {
    Icon(
        painter = painterResource(id),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}

/** Coloured rounded-square badge with a single letter — used for engines & shortcuts. */
@Composable
fun LetterBadge(
    letter: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    corner: Dp = 11.dp,
    fontSize: TextUnit = 15.sp,
    textColor: Color = Color.White,
) {
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(corner)).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(letter, color = textColor, fontWeight = FontWeight.Bold, fontSize = fontSize)
    }
}
