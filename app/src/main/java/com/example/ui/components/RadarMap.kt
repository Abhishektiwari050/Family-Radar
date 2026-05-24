package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FamilyMemberEntity
import com.example.data.local.GeofenceEntity
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarMap(
    modifier: Modifier = Modifier,
    members: List<FamilyMemberEntity>,
    geofences: List<GeofenceEntity>,
    selectedMemberId: Int?,
    trails: Map<Int, List<Pair<Double, Double>>>,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onPinClicked: (Int) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetPan: () -> Unit,
    onPan: (Float, Float) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    // Decouple pointerInput from offset/scale changes to keep gesture streams stable and avoid crashes.
    val currentMembers by rememberUpdatedState(members)
    val currentScale by rememberUpdatedState(scale)
    val currentOffsetX by rememberUpdatedState(offsetX)
    val currentOffsetY by rememberUpdatedState(offsetY)
    val currentOnPinClicked by rememberUpdatedState(onPinClicked)
    val currentOnPan by rememberUpdatedState(onPan)

    // Cache measured text layouts to prevent executing heavy font measurements on the main thread during draw loops.
    val rangeLabels = remember(scale) {
        listOf(150f, 400f, 800f, 1500f).associateWith { rangeMeters ->
            textMeasurer.measure(
                text = "${rangeMeters.toInt()}m",
                style = TextStyle(color = Color(0xFF818CF8).copy(alpha = 0.5f), fontSize = 10.sp)
            )
        }
    }

    val geofenceLabels = remember(geofences) {
        geofences.associateWith { geo ->
            textMeasurer.measure(
                text = "${geo.emoji} ${geo.name}",
                style = TextStyle(color = Color(0xFF34D399), fontSize = 11.sp, textAlign = TextAlign.Center)
            )
        }
    }

    val memberEmojiLabels = remember(members) {
        members.associateWith { m ->
            textMeasurer.measure(
                text = m.avatarEmoji.ifBlank { m.name.take(1).uppercase() },
                style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center)
            )
        }
    }

    val memberNameLabels = remember(members, selectedMemberId) {
        members.associateWith { m ->
            val labelColor = if (m.isSOS) Color(0xFFFCA5A5) else Color.White
            val isSelectedModifier = if (selectedMemberId == m.id) "[ACTIVE]" else ""
            textMeasurer.measure(
                text = "${m.name}$isSelectedModifier",
                style = TextStyle(
                    color = labelColor,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 3f)
                )
            )
        }
    }

    // Anchor center on the selected member, falling back to New York default
    val anchorMember = remember(members, selectedMemberId) {
        members.find { it.id == selectedMemberId } ?: members.find { it.id == 1 } ?: members.firstOrNull()
    }
    val centerLat = anchorMember?.latitude ?: 40.7678
    val centerLng = anchorMember?.longitude ?: -73.9718

    // Decouple pointerInput from center shifts
    val currentCenterLat by rememberUpdatedState(centerLat)
    val currentCenterLng by rememberUpdatedState(centerLng)

    // Animate radar sweep rotating angle
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepRotation"
    )

    // Pulsing circle scale for active SOS or safe circles
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseEffect"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0B121F)) // Very dark space blue background
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val ppm = 0.15f * currentScale
                        val center = Offset(size.width / 2 + currentOffsetX, size.height / 2 + currentOffsetY)

                        var tappedMember: FamilyMemberEntity? = null
                        var minDistance = 50f // Tapping buffer pixels

                        for (m in currentMembers) {
                            val dx = (m.longitude - currentCenterLng) * 111320.0 * ppm * cos(Math.toRadians(currentCenterLat))
                            val dy = -(m.latitude - currentCenterLat) * 110574.0 * ppm
                            val pinCenter = Offset(center.x + dx.toFloat(), center.y + dy.toFloat())

                            val dxDiff = tapOffset.x - pinCenter.x
                            val dyDiff = tapOffset.y - pinCenter.y
                            val dist = kotlin.math.sqrt(dxDiff * dxDiff + dyDiff * dyDiff)
                            if (dist < minDistance) {
                                minDistance = dist
                                tappedMember = m
                            }
                        }

                        tappedMember?.let { m ->
                            currentOnPinClicked(m.id)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    currentOnPan(dragAmount.x, dragAmount.y)
                }
            }
            .testTag("radar_map_container")
    ) {
        // Core Radar Canvas Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + offsetX, size.height / 2 + offsetY)
            val maxRadius = size.width.coerceAtMost(size.height) * 0.95f

            // Map grid scaling factor based on zoom scale
            val ppm = 0.15f * scale // pixels per meter coordinate conversion unit

            // Draw concentric radial range rings
            val ranges = listOf(150f, 400f, 800f, 1500f)
            ranges.forEach { rangeMeters ->
                val r = rangeMeters * ppm
                drawCircle(
                    color = Color(0xFF6366F1).copy(alpha = 0.12f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f))
                )

                // Label for each concentric range from cache
                val textLayoutResult = rangeLabels[rangeMeters]
                if (textLayoutResult != null) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(center.x + 5.dp.toPx(), center.y - r - textLayoutResult.size.height / 2)
                    )
                }
            }

            // Crosshair lines
            drawLine(
                color = Color(0xFF6366F1).copy(alpha = 0.1f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF6366F1).copy(alpha = 0.1f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1.dp.toPx()
            )

            // Draw Radar Sweep Line gradient
            val radians = Math.toRadians(sweepAngle.toDouble())
            val sweepEndX = center.x + maxRadius * cos(radians).toFloat()
            val sweepEndY = center.y + maxRadius * sin(radians).toFloat()
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF818CF8).copy(alpha = 0.4f), Color(0xFF10B981).copy(alpha = 0.05f)),
                    start = center,
                    end = Offset(sweepEndX, sweepEndY)
                ),
                start = center,
                end = Offset(sweepEndX, sweepEndY),
                strokeWidth = 2.5.dp.toPx()
            )

            // Draw Safety Zones / Geofences representation first (so they are drawn background level)
            geofences.forEach { geo ->
                val dx = (geo.longitude - centerLng) * 111320.0 * ppm * cos(Math.toRadians(centerLat))
                val dy = -(geo.latitude - centerLat) * 110574.0 * ppm
                val geoCenter = Offset(center.x + dx.toFloat(), center.y + dy.toFloat())
                val radiusPx = (geo.radiusMeters * ppm).toFloat()

                // Glow ring
                drawCircle(
                    color = Color(0xFF34D399).copy(alpha = 0.06f),
                    radius = radiusPx,
                    center = geoCenter
                )
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.35f),
                    radius = radiusPx,
                    center = geoCenter,
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Dot & text in center of geofence
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.7f),
                    radius = 4.dp.toPx(),
                    center = geoCenter
                )

                val infoLabel = geofenceLabels[geo]
                if (infoLabel != null) {
                    drawText(
                        textLayoutResult = infoLabel,
                        topLeft = Offset(geoCenter.x - infoLabel.size.width / 2, geoCenter.y + 6.dp.toPx())
                    )
                }
            }

            // Draw historical tracks (trails) of members
            members.forEach { member ->
                val track = trails[member.id] ?: emptyList()
                if (track.size > 1) {
                    val pathColor = try {
                        Color(android.graphics.Color.parseColor(member.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF818CF8)
                    }

                    val points = track.map { pt ->
                        val dx = (pt.second - centerLng) * 111320.0 * ppm * cos(Math.toRadians(centerLat))
                        val dy = -(pt.first - centerLat) * 110574.0 * ppm
                        Offset(center.x + dx.toFloat(), center.y + dy.toFloat())
                    }

                    // Draw connected trail lines
                    for (i in 0 until points.size - 1) {
                        val progress = i.toFloat() / points.size
                        drawLine(
                            color = pathColor.copy(alpha = progress * 0.5f),
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = (2 + progress * 3).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw dynamic pin pointers for Family Members
            members.forEach { m ->
                val dx = (m.longitude - centerLng) * 111320.0 * ppm * cos(Math.toRadians(centerLat))
                val dy = -(m.latitude - centerLat) * 110574.0 * ppm
                val pinCenter = Offset(center.x + dx.toFloat(), center.y + dy.toFloat())

                val pinColor = try {
                    Color(android.graphics.Color.parseColor(m.colorHex))
                } catch (e: Exception) {
                    Color(0xFF6366F1)
                }

                // If Selected, draw a dynamic pulsing high-contrast glow halo
                if (selectedMemberId == m.id) {
                    drawCircle(
                        color = pinColor.copy(alpha = 0.15f),
                        radius = 28.dp.toPx() * pulseScale,
                        center = pinCenter
                    )
                }

                // SOS Trigger alert circles
                if (m.isSOS) {
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = 0.25f),
                        radius = 34.dp.toPx() * pulseScale,
                        center = pinCenter
                    )
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = 0.4f),
                        radius = 22.dp.toPx() * pulseScale,
                        center = pinCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Draw Core Pin point
                drawCircle(
                    color = pinColor,
                    radius = 11.dp.toPx(),
                    center = pinCenter
                )
                // White stroke ring
                drawCircle(
                    color = Color.White,
                    radius = 11.dp.toPx(),
                    center = pinCenter,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Render Emoji/Avatar Letter or Emoji from cache
                val emojiTextResult = memberEmojiLabels[m]
                if (emojiTextResult != null) {
                    drawText(
                        textLayoutResult = emojiTextResult,
                        topLeft = Offset(pinCenter.x - emojiTextResult.size.width / 2, pinCenter.y - emojiTextResult.size.height / 2)
                    )
                }

                // Render Member Name tag overlay from cache
                val nameLabel = memberNameLabels[m]
                if (nameLabel != null) {
                    drawText(
                        textLayoutResult = nameLabel,
                        topLeft = Offset(pinCenter.x - nameLabel.size.width / 2, pinCenter.y - 25.dp.toPx())
                    )
                }

                // Battery Indicator Small Dot (Red if < 20%, Green if good)
                val batColor = when {
                    m.batteryPercent < 20 -> Color(0xFFEF4444)
                    m.batteryPercent < 50 -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
                drawCircle(
                    color = batColor,
                    radius = 3.dp.toPx(),
                    center = Offset(pinCenter.x + 8.dp.toPx(), pinCenter.y - 8.dp.toPx())
                )
            }
        }

        // Pins are tappable via the container-level pointerInput gesture handler.

        // Map Control Buttons (Pinch, Zoom, Reset)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledIconButton(
                onClick = onZoomIn,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)),
                modifier = Modifier.size(42.dp).testTag("zoom_in_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Zoom In", tint = Color.White)
            }

            FilledIconButton(
                onClick = onZoomOut,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)),
                modifier = Modifier.size(42.dp).testTag("zoom_out_button")
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Zoom Out", tint = Color.White)
            }

            FilledIconButton(
                onClick = onResetPan,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)),
                modifier = Modifier.size(42.dp).testTag("reset_pan_button")
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Recenter Map", tint = Color.White)
            }
        }

        // Mini Badge indicating Radar Status
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.75f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
                Text(
                    text = "RADAR SCANNING",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF34D399),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
