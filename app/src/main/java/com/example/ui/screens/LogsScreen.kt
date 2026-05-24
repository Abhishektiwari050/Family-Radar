package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TrackingLogEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen(
    logs: List<TrackingLogEntity>,
    onClearLogs: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().background(StarkOffWhite)) {
        // Header Panel designed with heavy industrial borders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .neoBorder(3.dp, DeepInkBlack, 0.dp)
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TIMELINE_FEED //",
                    color = DeepInkBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "SYSTEM LOGS: ${logs.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepInkBlack
                )
            }

            if (logs.isNotEmpty()) {
                Button(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.buttonColors(containerColor = PinkGlow, contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .testTag("clear_logs_button")
                        .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp)
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear Event Logs", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CLEAR ALL", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = DeepInkBlack.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("NO SYSTEM TRACKING LOGS AVAILABLE", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    LogTimelineRow(log = log, timeStr = sdf.format(Date(log.timestamp)))
                }
            }
        }
    }
}

@Composable
fun LogTimelineRow(
    log: TrackingLogEntity,
    timeStr: String
) {
    val isAlert = log.event.contains("SOS") || log.event.contains("Emergency")
    val isBattery = log.event.contains("Battery") || log.event.contains("🪫")

    val stickerBg = when {
        isAlert -> PinkGlow
        isBattery -> CyberGreen
        log.event.contains("Zone") -> CyberGreen
        else -> IndustrialGray
    }

    val stickerTextCol = if (isAlert) Color.White else DeepInkBlack

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_timeline_row"),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(55.dp)
        ) {
            Text(
                text = timeStr,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = DeepInkBlack
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(stickerBg)
                    .border(1.5.dp, DeepInkBlack, CircleShape)
            )

            // Connection line below point
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(38.dp)
                    .background(DeepInkBlack)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .neoCard(
                    backgroundColor = if (isAlert) PinkGlow.copy(alpha = 0.15f) else Color.White,
                    cornerRadius = 6.dp
                )
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.memberName.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = DeepInkBlack
                    )

                    NeoSticker(
                        text = if (isAlert) "ALERT" else "EVENT",
                        backgroundColor = stickerBg,
                        textColor = stickerTextCol
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.event,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepInkBlack
                )
            }
        }
    }
}
