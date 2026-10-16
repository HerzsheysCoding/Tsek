package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.Ink
import com.example.ui.theme.Paper

// The signature vivid blue accent dot from the official Tsek logo
val TsekBrandBlue = Color(0xFF1E88E5)

/**
 * High-fidelity vector rendering of the official Tsek Emblem:
 * Shield + Magnifying Glass + Checkmark + Signature Blue Dot.
 */
@Composable
fun TsekOfficialEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    tint: Color = Ink
) {
    Image(
        painter = painterResource(id = R.drawable.ic_tsek_emblem),
        contentDescription = "Tsek official logo emblem",
        modifier = modifier
            .size(size)
            .testTag("tsek_official_emblem")
    )
}

/**
 * Official Tsek Wordmark: "Ts" + "e" (with blue dot above) + "k".
 */
@Composable
fun TsekWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    textColor: Color = Ink,
    dotColor: Color = TsekBrandBlue
) {
    Row(
        modifier = modifier.testTag("tsek_wordmark"),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Ts",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        // The letter 'e' with the prominent blue dot over it
        Box(
            contentAlignment = Alignment.TopCenter
        ) {
            // Blue dot floating directly above the 'e'
            Box(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .size((fontSize.value * 0.22).dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Text(
                text = "e",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Text(
            text = "k",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Full official Tsek Logo with Emblem and Wordmark.
 */
@Composable
fun TsekOfficialLogo(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 32.dp,
    fontSize: TextUnit = 22.sp,
    textColor: Color = Ink,
    isStacked: Boolean = false
) {
    if (isStacked) {
        Column(
            modifier = modifier.testTag("tsek_official_logo_stacked"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TsekOfficialEmblem(
                size = emblemSize,
                tint = textColor
            )
            TsekWordmark(
                fontSize = fontSize,
                textColor = textColor
            )
        }
    } else {
        Row(
            modifier = modifier.testTag("tsek_official_logo_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TsekOfficialEmblem(
                size = emblemSize,
                tint = textColor
            )
            TsekWordmark(
                fontSize = fontSize,
                textColor = textColor
            )
        }
    }
}
