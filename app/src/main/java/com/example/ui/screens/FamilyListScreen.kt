package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.content.Intent
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FamilyMemberEntity
import com.example.ui.viewmodel.FamilyViewModel
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

@Composable
fun FamilyListScreen(
    viewModel: FamilyViewModel,
    members: List<FamilyMemberEntity>,
    selectedId: Int?,
    onSelectMember: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(StarkOffWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header panel in heavy raw brutalism style
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
                        text = "MEMBERS_TRACKED //",
                        color = DeepInkBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TOTAL COUNT: ${members.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepInkBlack
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = DeepInkBlack),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .testTag("add_member_button")
                        .neoShadow(DeepInkBlack, 3.dp, 3.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Member", modifier = Modifier.size(16.dp), tint = DeepInkBlack)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD MEMBER", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }

            if (members.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = DeepInkBlack.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("NO REGISTERED FAMILY MEMBERS", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        GPSSyncControlCenterCard(
                            viewModel = viewModel,
                            members = members
                        )
                    }

                    items(members, key = { it.id }) { member ->
                        FamilyMemberRow(
                            member = member,
                            isSelected = member.id == selectedId,
                            onSelect = { onSelectMember(member.id) },
                            onPing = { viewModel.pingMember(member.id, member.name) },
                            onSOS = { viewModel.toggleSOS(member.id) },
                            onDelete = { viewModel.removeMember(member.id, member.name) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            val me = remember(members) { members.find { it.id == 1 } }
            val baseLat = me?.latitude ?: 40.7678
            val baseLng = me?.longitude ?: -73.9718
            AddMemberDialog(
                baseLat = baseLat,
                baseLng = baseLng,
                onDismiss = { showAddDialog = false },
                onSave = { name, relation, avatar, color, lat, lng ->
                    viewModel.saveMember(name, relation, avatar, color, lat, lng)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun FamilyMemberRow(
    member: FamilyMemberEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    onSOS: () -> Unit,
    onDelete: () -> Unit
) {
    val themeColor = try {
        Color(android.graphics.Color.parseColor(member.colorHex))
    } catch (e: Exception) {
        CyberGreen
    }

    val batteryColor = when {
        member.batteryPercent < 20 -> PinkGlow
        else -> DeepInkBlack
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("family_member_card_${member.id}")
            .neoCard(
                backgroundColor = if (isSelected) CyberGreen else Color.White,
                cornerRadius = 8.dp
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(themeColor)
                        .border(2.dp, DeepInkBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = member.avatarEmoji.ifBlank { "👤" }, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name.uppercase(),
                            fontWeight = FontWeight.Black,
                            color = DeepInkBlack,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, DeepInkBlack, RoundedCornerShape(2.dp))
                                .background(StarkOffWhite)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = member.relationship.uppercase(),
                                color = DeepInkBlack,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = member.statusMessage,
                        color = if (member.isSOS) PinkGlow else DeepInkBlack.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Battery & Signal tag
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (member.batteryPercent < 20) Icons.Filled.BatteryAlert else Icons.Filled.BatteryChargingFull,
                            contentDescription = "Battery Status",
                            tint = batteryColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${member.batteryPercent}%",
                            color = batteryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    NeoSticker(
                        text = if (member.isOnline) "ONLINE" else "OFFLINE",
                        backgroundColor = if (member.isOnline) CyberGreen else IndustrialGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub info row: Speed & coordinates with rigid styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                    .background(StarkOffWhite)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, contentDescription = null, tint = DeepInkBlack, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SPEED: ${"%.1f".format(member.speed)} KM/H",
                        color = DeepInkBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DeepInkBlack, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${"%.4f".format(member.latitude)}, ${"%.4f".format(member.longitude)}",
                        color = DeepInkBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map launcher
            val context = LocalContext.current
            Button(
                onClick = { openGoogleMaps(context, member.latitude, member.longitude, member.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                    .neoBorder(2.dp, DeepInkBlack, 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DeepInkBlack
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(14.dp), tint = DeepInkBlack)
                Spacer(modifier = Modifier.width(6.dp))
                Text("LAUNCH GOOGLE MAPS //", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Control Action Deck (SOS, Ping, Delete if not "Me")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSOS,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (member.isSOS) CyberGreen else PinkGlow,
                        contentColor = if (member.isSOS) DeepInkBlack else Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (member.isSOS) "DISARM SOS" else "TRIGGER SOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = onPing,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndustrialGray,
                        contentColor = DeepInkBlack
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.CellTower, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PING DEVICE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                // Delete configuration if not "Me"
                if (member.relationship != "Me") {
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
                        Icon(Icons.Filled.Delete, contentDescription = "Delete config", modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddMemberDialog(
    baseLat: Double,
    baseLng: Double,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Sister") }
    var avatar by remember { mutableStateOf("👤") }
    var colorHex by remember { mutableStateOf("#F59E0B") }

    var latOffset by remember { mutableStateOf("0.002") }
    var lngOffset by remember { mutableStateOf("-0.003") }

    val relationships = listOf("Mom", "Dad", "Sister", "Brother", "Grandma", "Grandpa")
    val avatars = listOf("👤", "👩", "👨", "👧", "👦", "👵")
    val colors = listOf("#EC4899" to "Pink", "#F59E0B" to "Amber", "#10B981" to "Emerald", "#06B6D4" to "Teal", "#3B82F6" to "Blue", "#8B5CF6" to "Purple")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StarkOffWhite,
        modifier = Modifier.border(3.dp, DeepInkBlack, RoundedCornerShape(8.dp)),
        title = {
            Text(
                "REGISTER NEW UNIT",
                color = DeepInkBlack,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("IDENTIFIER / NAME", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("enter user name...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = DeepInkBlack,
                                unfocusedTextColor = DeepInkBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBorder(2.dp, DeepInkBlack, 4.dp)
                                .testTag("add_member_input_name")
                        )
                    }
                }

                item {
                    Text("RELATIONSHIP", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        relationships.take(4).forEach { rel ->
                            Box(
                                modifier = Modifier
                                    .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                                    .background(if (relationship == rel) CyberGreen else Color.White)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable { relationship = rel }
                            ) {
                                Text(rel.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = DeepInkBlack)
                            }
                        }
                    }
                }

                item {
                    Text("AVATAR ICON", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        avatars.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, DeepInkBlack, CircleShape)
                                    .background(if (avatar == emoji) CyberGreen else Color.White)
                                    .clickable { avatar = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text("COLOR CODE BADGE", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        colors.forEach { (hex, _) ->
                            val drawColor = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(drawColor, CircleShape)
                                    .border(2.dp, DeepInkBlack, CircleShape)
                                    .clickable { colorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (colorHex == hex) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Text("MAP GEOLOCATION OFFSET", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
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
                        val lOffset = latOffset.toDoubleOrNull() ?: 0.0
                        val nOffset = lngOffset.toDoubleOrNull() ?: 0.0
                        onSave(name, relationship, avatar, colorHex, baseLat + lOffset, baseLng + nOffset)
                    }
                },
                modifier = Modifier
                    .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                    .neoBorder(2.dp, DeepInkBlack, 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = DeepInkBlack),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SAVE UNIT", fontSize = 11.sp, fontWeight = FontWeight.Black)
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

fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, name: String) {
    try {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(name)})")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        try {
            val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        } catch (e2: Exception) {
            // safe guard
        }
    }
}

fun triggerDeviceGPSFetch(
    context: Context,
    viewModel: FamilyViewModel,
    onSuccess: () -> Unit = {}
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateMeLocation(
                        lat = location.latitude,
                        lng = location.longitude,
                        speedKmh = location.speed * 3.6
                    )
                    onSuccess()
                } else {
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        1000L
                     ).setMaxUpdates(1).build()

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val loc = result.lastLocation
                            if (loc != null) {
                                viewModel.updateMeLocation(
                                    lat = loc.latitude,
                                    lng = loc.longitude,
                                    speedKmh = loc.speed * 3.6
                                )
                                onSuccess()
                            }
                            try {
                                fusedLocationClient.removeLocationUpdates(this)
                            } catch (e: Exception) {
                                // Ignore gracefully
                            }
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        callback,
                        context.mainLooper
                    )
                }
            }
            .addOnFailureListener {
                // Ignore gracefully
            }
    } catch (e: SecurityException) {
        // Safe check
    } catch (e: Exception) {
        // Safe check
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GPSSyncControlCenterCard(
    viewModel: FamilyViewModel,
    members: List<FamilyMemberEntity>
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val me = remember(members) { members.find { it.id == 1 } }
    val isRealGPS = me?.statusMessage?.contains("GPS") == true

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .neoCard(backgroundColor = Color.White, cornerRadius = 8.dp)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(CyberGreen)
                            .border(2.dp, DeepInkBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRealGPS) Icons.Filled.GpsFixed else Icons.Filled.GpsNotFixed,
                            contentDescription = null,
                            tint = DeepInkBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "DEVICE TELEMETRY //",
                            fontWeight = FontWeight.Black,
                            color = DeepInkBlack,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            if (isRealGPS) {
                                NeoSticker(
                                    text = "REAL HARDWARE GPS",
                                    backgroundColor = CyberGreen
                                )
                            } else {
                                NeoSticker(
                                    text = "SIMULATOR PATHWAY",
                                    backgroundColor = PinkGlow,
                                    textColor = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (me != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                        .background(StarkOffWhite)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("MY_LATITUDE", color = DeepInkBlack.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        Text(String.format("%.4f", me.latitude), color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Column {
                        Text("MY_LONGITUDE", color = DeepInkBlack.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        Text(String.format("%.4f", me.longitude), color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("VELOCITY", color = DeepInkBlack.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        Text(String.format("%.1f", me.speed) + " km/h", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (locationPermissionState.status.isGranted) {
                            triggerDeviceGPSFetch(context, viewModel)
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = DeepInkBlack),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(15.dp), tint = DeepInkBlack)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SYNC LIVE COORDS", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                if (me != null) {
                    Button(
                        onClick = {
                            openGoogleMaps(context, me.latitude, me.longitude, me.name)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                            .neoBorder(2.dp, DeepInkBlack, 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DeepInkBlack),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp), tint = DeepInkBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("OPEN MAPS SCREEN", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            if (!isRealGPS) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DeepInkBlack, thickness = 2.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "📢 SIMULATION D-PAD CONTROLLER //",
                    color = DeepInkBlack,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Up Button
                        IconButton(
                            onClick = { viewModel.moveMeUp() },
                            modifier = Modifier
                                .size(34.dp)
                                .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .testTag("sim_move_up")
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Simulate Walk North", modifier = Modifier.size(24.dp), tint = DeepInkBlack)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Button
                            IconButton(
                                onClick = { viewModel.moveMeLeft() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .testTag("sim_move_left")
                            ) {
                                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Simulate Walk West", modifier = Modifier.size(24.dp), tint = DeepInkBlack)
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(CyberGreen, CircleShape)
                                    .border(2.dp, DeepInkBlack, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.DirectionsWalk, contentDescription = "Walk State", tint = DeepInkBlack, modifier = Modifier.size(18.dp))
                            }

                            // Right Button
                            IconButton(
                                onClick = { viewModel.moveMeRight() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .testTag("sim_move_right")
                            ) {
                                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Simulate Walk East", modifier = Modifier.size(24.dp), tint = DeepInkBlack)
                            }
                        }

                        // Down Button
                        IconButton(
                            onClick = { viewModel.moveMeDown() },
                            modifier = Modifier
                                .size(34.dp)
                                .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .testTag("sim_move_down")
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Simulate Walk South", modifier = Modifier.size(24.dp), tint = DeepInkBlack)
                        }
                    }
                }
            }
        }
    }
}
