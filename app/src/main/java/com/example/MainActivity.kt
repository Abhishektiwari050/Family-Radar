package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.FamilyMemberEntity
import com.example.ui.viewmodel.FamilyViewModel
import com.example.ui.components.RadarMap
import com.example.ui.screens.FamilyListScreen
import com.example.ui.screens.GeofenceManagerScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StarkOffWhite
                ) {
                    FamilyTrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FamilyTrackerApp(
    viewModel: FamilyViewModel = viewModel()
) {
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Enable active tracking of actual system GPS coordinates if permissions are given
    GPSLocationTracker(viewModel = viewModel, locationPermissionState = locationPermissionState)

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val loggedInUsername by viewModel.loggedInUsername.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { username ->
                viewModel.loginUser(username)
            }
        )
        return
    }

    val members by viewModel.members.collectAsStateWithLifecycle()
    val geofences by viewModel.geofences.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    val selectedId by viewModel.selectedMemberId.collectAsStateWithLifecycle()
    val mapScale by viewModel.mapScale.collectAsStateWithLifecycle()
    val mapOffsetX by viewModel.mapOffsetX.collectAsStateWithLifecycle()
    val mapOffsetY by viewModel.mapOffsetY.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulationActive.collectAsStateWithLifecycle()
    val trails by viewModel.memberTrails.collectAsStateWithLifecycle()

    // Determine current selected member detail
    val selectedMember = remember(members, selectedId) {
        members.find { it.id == selectedId }
    }

    val activeAnchor = remember(members, selectedId) {
        members.find { it.id == selectedId } ?: members.find { it.id == 1 } ?: members.firstOrNull()
    }
    val activeBaseLat = activeAnchor?.latitude ?: 40.7678
    val activeBaseLng = activeAnchor?.longitude ?: -73.9718

    // Tab state (0 = Members, 1 = Zones, 2 = Logs)
    var currentMobileTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StarkOffWhite,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(CyberGreen)
                                .border(2.dp, DeepInkBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Radar,
                                contentDescription = null,
                                tint = DeepInkBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FAMILY_RADAR",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DeepInkBlack,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                NeoSticker(
                                    text = "ONLINE",
                                    backgroundColor = CyberGreen
                                )
                            }
                            Text(
                                text = "LOGGED_IN: $loggedInUsername".uppercase(),
                                fontSize = 10.sp,
                                color = DeepInkBlack.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logoutUser() },
                        modifier = Modifier
                            .testTag("logout_button")
                            .padding(end = 8.dp)
                            .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                            .background(PinkGlow)
                            .size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = StarkOffWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DeepInkBlack
                ),
                modifier = Modifier.border(3.dp, DeepInkBlack)
            )
        }
    ) { scaffoldPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            val isTablet = maxWidth >= 720.dp

            if (isTablet) {
                // Large screen/Tablet Layout: Side-by-Side Split Cockpit UI dashboard!
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Wing: Members list cockpit showing GPS synchronization card and stats details (50% share)
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .neoCard(backgroundColor = Color.White, cornerRadius = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 12.dp)
                        ) {
                            FamilyListScreen(
                                viewModel = viewModel,
                                members = members,
                                selectedId = selectedId,
                                onSelectMember = { viewModel.selectMember(it) }
                            )
                        }
                    }

                    // Right Wing: Control Deck containing boundaries and tracking logs (50% share)
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .neoCard(backgroundColor = Color.White, cornerRadius = 12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            var deckTab by remember { mutableIntStateOf(0) }

                            TabRow(
                                selectedTabIndex = deckTab,
                                containerColor = Color.White,
                                contentColor = DeepInkBlack,
                                modifier = Modifier.neoBorder(width = 2.dp, color = DeepInkBlack, cornerRadius = 0.dp)
                            ) {
                                Tab(
                                    selected = deckTab == 0,
                                    onClick = { deckTab = 0 },
                                    text = { Text("BOUNDARIES", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                                    icon = { Icon(Icons.Filled.VerifiedUser, null, modifier = Modifier.size(18.dp)) },
                                    selectedContentColor = DeepInkBlack,
                                    unselectedContentColor = Color.Gray
                                )
                                Tab(
                                    selected = deckTab == 1,
                                    onClick = { deckTab = 1 },
                                    text = { Text("ALL LOGS", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                                    icon = { Icon(Icons.Filled.History, null, modifier = Modifier.size(18.dp)) },
                                    selectedContentColor = DeepInkBlack,
                                    unselectedContentColor = Color.Gray
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 12.dp)
                            ) {
                                when (deckTab) {
                                    0 -> GeofenceManagerScreen(
                                        viewModel = viewModel,
                                        geofences = geofences,
                                        baseLat = activeBaseLat,
                                        baseLng = activeBaseLng
                                    )
                                    1 -> LogsScreen(
                                        logs = logs,
                                        onClearLogs = { viewModel.clearLogHistory() }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Compact mobile layouts: Mobile Bottom Tabbed navigation (3 tabs)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = StarkOffWhite,
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            contentColor = DeepInkBlack,
                            modifier = Modifier.neoBorder(width = 3.dp, color = DeepInkBlack, cornerRadius = 0.dp)
                        ) {
                            NavigationBarItem(
                                selected = currentMobileTab == 0,
                                onClick = { currentMobileTab = 0 },
                                icon = { Icon(Icons.Filled.People, contentDescription = "Family List") },
                                label = { Text("MEMBERS", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepInkBlack,
                                    selectedTextColor = DeepInkBlack,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CyberGreen
                                )
                            )
                            NavigationBarItem(
                                selected = currentMobileTab == 1,
                                onClick = { currentMobileTab = 1 },
                                icon = { Icon(Icons.Filled.VerifiedUser, contentDescription = "Safety Zones") },
                                label = { Text("ZONES", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepInkBlack,
                                    selectedTextColor = DeepInkBlack,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = PinkGlow
                                )
                            )
                            NavigationBarItem(
                                selected = currentMobileTab == 2,
                                onClick = { currentMobileTab = 2 },
                                icon = { Icon(Icons.Filled.History, contentDescription = "Activity history timeline") },
                                label = { Text("TIMELINE", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepInkBlack,
                                    selectedTextColor = DeepInkBlack,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = CyberGreen
                                )
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (currentMobileTab) {
                            0 -> { // Members tab page
                                FamilyListScreen(
                                    viewModel = viewModel,
                                    members = members,
                                    selectedId = selectedId,
                                    onSelectMember = {
                                        viewModel.selectMember(it)
                                    }
                                )
                            }
                            1 -> { // Safety Zones tab page
                                GeofenceManagerScreen(
                                    viewModel = viewModel,
                                    geofences = geofences,
                                    baseLat = activeBaseLat,
                                    baseLng = activeBaseLng
                                )
                            }
                            2 -> { // Logs timeline tab page
                                LogsScreen(
                                    logs = logs,
                                    onClearLogs = { viewModel.clearLogHistory() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapNavigationTitlePanel(
    isSimulating: Boolean,
    onToggleSim: (Boolean) -> Unit,
    gpsActive: Boolean,
    onRequestGPS: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .neoCard(backgroundColor = Color.White, cornerRadius = 8.dp)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberGreen)
                        .border(2.dp, DeepInkBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Radar, contentDescription = null, tint = DeepInkBlack, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        text = "RADAR SIGNAL CHANNEL",
                        fontWeight = FontWeight.Black,
                        color = DeepInkBlack,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (gpsActive) {
                            NeoSticker(
                                text = "REAL GPS LIVE",
                                backgroundColor = CyberGreen
                            )
                        } else {
                            NeoSticker(
                                text = "SIMULATOR MODE",
                                backgroundColor = PinkGlow,
                                textColor = Color.White
                            )
                            Text(
                                text = "SYNC GPS",
                                color = DeepInkBlack,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { onRequestGPS() }
                                    .background(CyberGreen)
                                    .border(1.5.dp, DeepInkBlack, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Live Simulation active toggle button styled beautifully
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isSimulating) "PLAY" else "PAUS",
                    color = DeepInkBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Switch(
                    checked = isSimulating,
                    onCheckedChange = onToggleSim,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepInkBlack,
                        checkedTrackColor = CyberGreen,
                        uncheckedThumbColor = DeepInkBlack,
                        uncheckedTrackColor = IndustrialGray
                    ),
                    modifier = Modifier.graphicsLayer {
                        scaleX = 0.85f
                        scaleY = 0.85f
                    }
                )
            }
        }
    }
}

@Composable
fun MemberDetailStatsCard(
    member: FamilyMemberEntity,
    onToggleSOS: () -> Unit
) {
    val themeColor = try {
        Color(android.graphics.Color.parseColor(member.colorHex))
    } catch (e: Exception) {
        CyberGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                            .background(themeColor)
                            .border(2.dp, DeepInkBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(member.avatarEmoji.ifBlank { "👤" }, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name.uppercase(),
                                fontWeight = FontWeight.Black,
                                color = DeepInkBlack,
                                fontSize = 14.sp
                            )
                            if (member.isSOS) {
                                Spacer(modifier = Modifier.width(6.dp))
                                NeoSticker(
                                    text = "SOS INCIDENT",
                                    backgroundColor = PinkGlow,
                                    textColor = Color.White
                                )
                            }
                        }
                        Text(
                            text = member.statusMessage,
                            color = if (member.isSOS) PinkGlow else DeepInkBlack.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Interactive Emergency direct action button in Neo-Brutalist red
                IconButton(
                    onClick = onToggleSOS,
                    modifier = Modifier
                        .size(32.dp)
                        .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                        .background(if (member.isSOS) PinkGlow else StarkOffWhite)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "SOS Incident Toggle",
                        tint = if (member.isSOS) Color.White else DeepInkBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub stats indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Battery metric
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                        .background(StarkOffWhite)
                        .padding(6.dp)
                ) {
                    Text("BATTERY", color = DeepInkBlack.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BatteryChargingFull,
                            contentDescription = null,
                            tint = if (member.batteryPercent < 20) PinkGlow else DeepInkBlack,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${member.batteryPercent}%",
                            color = DeepInkBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Speed metric
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                        .background(StarkOffWhite)
                        .padding(6.dp)
                ) {
                    Text("VELOCITY", color = DeepInkBlack.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = null, tint = DeepInkBlack, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${"%.1f".format(member.speed)} km/h",
                            color = DeepInkBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Latitude/Longitude coordinates
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .border(2.dp, DeepInkBlack, RoundedCornerShape(4.dp))
                        .background(StarkOffWhite)
                        .padding(6.dp)
                ) {
                    Text("COORDINATES", color = DeepInkBlack.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.LocationSearching, contentDescription = null, tint = DeepInkBlack, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${"%.3f".format(member.latitude)}, ${"%.3f".format(member.longitude)}",
                            color = DeepInkBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GPSLocationTracker(
    viewModel: FamilyViewModel,
    locationPermissionState: com.google.accompanist.permissions.PermissionState
) {
    val context = LocalContext.current

    DisposableEffect(key1 = locationPermissionState.status.isGranted) {
        var callback: LocationCallback? = null
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (locationPermissionState.status.isGranted) {
            try {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    5150L
                ).apply {
                    setMinUpdateIntervalMillis(2500L)
                }.build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        for (location in result.locations) {
                            val speedKmh = location.speed * 3.6 // speed m/s to km/h conversion
                            viewModel.updateMeLocation(
                                lat = location.latitude,
                                lng = location.longitude,
                                speedKmh = speedKmh
                            )
                        }
                    }
                }
                callback = locationCallback

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    context.mainLooper
                )
            } catch (e: SecurityException) {
                // permission revoked or security lock
            } catch (e: Exception) {
                // general exception guard
            }
        }

        onDispose {
            callback?.let {
                try {
                    fusedLocationClient.removeLocationUpdates(it)
                } catch (e: Exception) {
                    // Ignore gracefully
                }
            }
        }
    }
}
