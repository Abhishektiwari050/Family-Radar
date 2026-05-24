package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GeofenceEntity
import com.example.ui.viewmodel.FamilyViewModel
import com.example.ui.theme.*

@Composable
fun GeofenceManagerScreen(
    viewModel: FamilyViewModel,
    geofences: List<GeofenceEntity>,
    baseLat: Double,
    baseLng: Double
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(StarkOffWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header block in heavy dual tone brutalism
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
                        text = "SAFETY_ZONES //",
                        color = DeepInkBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "DEFINED: ${geofences.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepInkBlack
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkGlow, contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .testTag("add_geofence_button")
                        .neoShadow(DeepInkBlack, 3.dp, 3.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Geofence", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NEW ZONE", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }

            if (geofences.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.PinDrop, contentDescription = null, modifier = Modifier.size(64.dp), tint = DeepInkBlack.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("NO SAFETY BOUNDARIES DEFINED", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(geofences, key = { it.id }) { geo ->
                        GeofenceDetailRow(
                            geofence = geo,
                            onDelete = { viewModel.removeGeofence(geo.id, geo.name) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddGeofenceDialog(
                baseLat = baseLat,
                baseLng = baseLng,
                onDismiss = { showAddDialog = false },
                onSave = { name, emoji, radius, lat, lng ->
                    viewModel.saveGeofence(name, emoji, radius, lat, lng)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun GeofenceDetailRow(
    geofence: GeofenceEntity,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("geofence_item_${geofence.name.lowercase().replace(" ", "_")}")
            .neoCard(backgroundColor = Color.White, cornerRadius = 8.dp)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(CyberGreen)
                    .border(2.dp, DeepInkBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = geofence.emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = geofence.name.uppercase(),
                    fontWeight = FontWeight.Black,
                    color = DeepInkBlack,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .border(1.5.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                        .background(StarkOffWhite)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DeepInkBlack, modifier = Modifier.size(12.dp))
                    Text(
                        text = "RADIUS: ${geofence.radiusMeters.toInt()}M | CENTER: ${"%.4f".format(geofence.latitude)}, ${"%.4f".format(geofence.longitude)}",
                        color = DeepInkBlack,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Interactive Delete in flat Neo style
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .size(38.dp)
                    .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                    .neoBorder(2.dp, DeepInkBlack, 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PinkGlow,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Geofence", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AddGeofenceDialog(
    baseLat: Double,
    baseLng: Double,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📍") }
    var radiusStr by remember { mutableStateOf("200") }

    var latOffset by remember { mutableStateOf("0.003") }
    var lngOffset by remember { mutableStateOf("0.002") }

    val emojis = listOf("📌", "🏠", "🏫", "🏢", "🏋️", "☕", "🌳", "🏬", "🏥")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StarkOffWhite,
        modifier = Modifier.border(3.dp, DeepInkBlack, RoundedCornerShape(8.dp)),
        title = {
            Text(
                "DEFINE BOUNDARY",
                color = DeepInkBlack,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("BOUNDARY NAME", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("enter zone name (e.g. School)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = DeepInkBlack,
                            unfocusedTextColor = DeepInkBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .neoBorder(2.dp, DeepInkBlack, 4.dp)
                            .testTag("add_geofence_input_name")
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("RADIUS LIMIT (METERS)", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    TextField(
                        value = radiusStr,
                        onValueChange = { radiusStr = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = DeepInkBlack,
                            unfocusedTextColor = DeepInkBlack
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().neoBorder(2.dp, DeepInkBlack, 4.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ZONE SYMBOL ICON", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        emojis.forEach { symbol ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, DeepInkBlack, CircleShape)
                                    .background(if (emoji == symbol) CyberGreen else Color.White)
                                    .clickable { emoji = symbol },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(symbol, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CENTER OFFSET COORDINATES", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = latOffset,
                            onValueChange = { latOffset = it },
                            label = { Text("Lat Offset") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).neoBorder(2.dp, DeepInkBlack, 4.dp)
                        )
                        TextField(
                            value = lngOffset,
                            onValueChange = { lngOffset = it },
                            label = { Text("Lng Offset") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).neoBorder(2.dp, DeepInkBlack, 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val rad = radiusStr.toDoubleOrNull() ?: 200.0
                        val lOffset = latOffset.toDoubleOrNull() ?: 0.0
                        val nOffset = lngOffset.toDoubleOrNull() ?: 0.0
                        onSave(name, emoji, rad, baseLat + lOffset, baseLng + nOffset)
                    }
                },
                modifier = Modifier
                    .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                    .neoBorder(2.dp, DeepInkBlack, 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = DeepInkBlack),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SAVE BOUNDARY", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                    .neoBorder(2.dp, DeepInkBlack, 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DeepInkBlack),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    )
}
