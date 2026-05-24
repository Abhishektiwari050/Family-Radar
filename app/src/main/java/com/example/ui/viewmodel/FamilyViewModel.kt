package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FamilyMemberEntity
import com.example.data.local.GeofenceEntity
import com.example.data.local.TrackingLogEntity
import com.example.data.repository.FamilyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = FamilyRepository(
        database.familyMemberDao(),
        database.geofenceDao(),
        database.trackingLogDao()
    )

    // SharedPreferences persistence for Login state persistence
    private val sharedPrefs = application.getSharedPreferences("family_tracker_prefs", android.content.Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInUsername = MutableStateFlow(sharedPrefs.getString("logged_in_username", "") ?: "")
    val loggedInUsername: StateFlow<String> = _loggedInUsername.asStateFlow()

    // UI state flows from Repository database sources
    val members: StateFlow<List<FamilyMemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val geofences: StateFlow<List<GeofenceEntity>> = repository.allGeofences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<TrackingLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map customization and interactive states
    private val _selectedMemberId = MutableStateFlow<Int?>(1) // Home / Me by default
    val selectedMemberId: StateFlow<Int?> = _selectedMemberId.asStateFlow()

    private var hasShiftedCluster = false

    private val _mapScale = MutableStateFlow(1.0f) // Zoom scale factor
    val mapScale: StateFlow<Float> = _mapScale.asStateFlow()

    private val _mapOffsetX = MutableStateFlow(0f)
    val mapOffsetX: StateFlow<Float> = _mapOffsetX.asStateFlow()

    private val _mapOffsetY = MutableStateFlow(0f)
    val mapOffsetY: StateFlow<Float> = _mapOffsetY.asStateFlow()

    private val _isSimulationActive = MutableStateFlow(true)
    val isSimulationActive: StateFlow<Boolean> = _isSimulationActive.asStateFlow()

    // Key-value cache tracking trail logs in-memory for visual paths
    private val _memberTrails = MutableStateFlow<Map<Int, List<Pair<Double, Double>>>>(emptyMap())
    val memberTrails: StateFlow<Map<Int, List<Pair<Double, Double>>>> = _memberTrails.asStateFlow()

    init {
        // Collect members and append coordinates to trails
        viewModelScope.launch {
            members.collect { memberList ->
                val currentTrails = _memberTrails.value.toMutableMap()
                for (m in memberList) {
                    val trail = currentTrails[m.id]?.toMutableList() ?: mutableListOf()
                    val newPoint = Pair(m.latitude, m.longitude)
                    if (trail.isEmpty() || trail.last() != newPoint) {
                        trail.add(newPoint)
                        if (trail.size > 15) {
                            trail.removeAt(0)
                        }
                        currentTrails[m.id] = trail
                    }
                }
                _memberTrails.value = currentTrails
            }
        }

        // Background simulation timer loop
        viewModelScope.launch {
            while (true) {
                delay(4000) // ticks every 4 seconds
                if (_isSimulationActive.value) {
                    runSimulationTick()
                }
            }
        }
    }

    fun selectMember(id: Int?) {
        _selectedMemberId.value = id
    }

    fun setSimulationActive(active: Boolean) {
        _isSimulationActive.value = active
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLog("System", if (active) "🔄 Real-time location simulation resumed." else "⏸️ Real-time location simulation paused.")
        }
    }

    fun zoomIn() {
        _mapScale.value = (_mapScale.value * 1.2f).coerceIn(0.5f, 3f)
    }

    fun zoomOut() {
        _mapScale.value = (_mapScale.value / 1.2f).coerceIn(0.5f, 3f)
    }

    fun resetMapPan() {
        _mapOffsetX.value = 0f
        _mapOffsetY.value = 0f
    }

    fun panMap(dx: Float, dy: Float) {
        _mapOffsetX.value += dx
        _mapOffsetY.value += dy
    }

    fun runSimulationTick() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.runSimulationTick(members.value, geofences.value)
        }
    }

    // Interactive operations
    fun pingMember(id: Int, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = database.familyMemberDao().getMemberById(id) ?: return@launch
            // Simulate random ping responses with status messages and updating timestamp
            repository.insertLog(name, "📳 Received check-in ping request. Battery: ${member.batteryPercent}%. Slipped coordinate updates: OK.")
            repository.insertMember(member.copy(
                lastUpdateTime = System.currentTimeMillis()
            ))
        }
    }

    fun toggleSOS(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = database.familyMemberDao().getMemberById(id) ?: return@launch
            val newSOSState = !member.isSOS
            repository.insertMember(member.copy(
                isSOS = newSOSState,
                statusMessage = if (newSOSState) "🚨 ACTIVE DISPATCH EMERGENCY!" else "🗺️ Viewing radar dashboard",
                speed = if (newSOSState) 0.0 else member.speed
            ))

            val alertMsg = if (newSOSState) "🚨 Triggered active SOS transmission! Neighbors and family notified." else "🟢 Canceled SOS incident flag safely."
            repository.insertLog(member.name, alertMsg)
        }
    }

    fun saveMember(name: String, relationship: String, avatar: String, colorHex: String, baseLat: Double, baseLng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newMember = FamilyMemberEntity(
                name = name,
                relationship = relationship,
                latitude = baseLat,
                longitude = baseLng,
                batteryPercent = 90 + (0..10).random(),
                statusMessage = "🗺️ Dynamic locator active",
                avatarEmoji = avatar,
                colorHex = colorHex,
                speed = 0.0
            )
            repository.insertMember(newMember)
        }
    }

    fun removeMember(id: Int, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMember(id, name)
            if (_selectedMemberId.value == id) {
                _selectedMemberId.value = members.value.firstOrNull { it.id != id }?.id
            }
        }
    }

    fun saveGeofence(name: String, emoji: String, radius: Double, lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val geo = GeofenceEntity(
                name = name,
                emoji = emoji,
                radiusMeters = radius,
                latitude = lat,
                longitude = lng
            )
            repository.insertGeofence(geo)
        }
    }

    fun removeGeofence(id: Int, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGeofence(id, name)
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearLogs()
            repository.insertLog("System", "🧹 Event log history wiped successfully.")
        }
    }

    // Move "Me" location manually to simulate user moving!
    fun moveMeUp() {
        _selectedMemberId.value?.let { id ->
            if (id == 1) {
                viewModelScope.launch(Dispatchers.IO) {
                    val me = database.familyMemberDao().getMemberById(1) ?: return@launch
                    val newLat = me.latitude + 0.0005
                    repository.insertMember(me.copy(latitude = newLat, speed = 4.2, statusMessage = "🚶 Moving North"))
                }
            }
        }
    }

    fun moveMeDown() {
        _selectedMemberId.value?.let { id ->
            if (id == 1) {
                viewModelScope.launch(Dispatchers.IO) {
                    val me = database.familyMemberDao().getMemberById(1) ?: return@launch
                    val newLat = me.latitude - 0.0005
                    repository.insertMember(me.copy(latitude = newLat, speed = 4.2, statusMessage = "🚶 Moving South"))
                }
            }
        }
    }

    fun moveMeLeft() {
        _selectedMemberId.value?.let { id ->
            if (id == 1) {
                viewModelScope.launch(Dispatchers.IO) {
                    val me = database.familyMemberDao().getMemberById(1) ?: return@launch
                    val newLng = me.longitude - 0.0007
                    repository.insertMember(me.copy(longitude = newLng, speed = 4.2, statusMessage = "🚶 Moving West"))
                }
            }
        }
    }

    fun moveMeRight() {
        _selectedMemberId.value?.let { id ->
            if (id == 1) {
                viewModelScope.launch(Dispatchers.IO) {
                    val me = database.familyMemberDao().getMemberById(1) ?: return@launch
                    val newLng = me.longitude + 0.0007
                    repository.insertMember(me.copy(longitude = newLng, speed = 4.2, statusMessage = "🚶 Moving East"))
                }
            }
        }
    }

    fun updateMeLocation(lat: Double, lng: Double, speedKmh: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = database.familyMemberDao().getMemberById(1) ?: return@launch
            val distance = repository.calculateDistanceMeters(me.latitude, me.longitude, lat, lng)
            val updatedMe = me.copy(
                latitude = lat,
                longitude = lng,
                speed = speedKmh,
                statusMessage = "🔋 Active Sync • Real GPS",
                lastUpdateTime = System.currentTimeMillis()
            )
            repository.insertMember(updatedMe)

            if (distance > 10.0) {
                repository.insertLog(me.name, "🛰️ Real GPS localized. Position updated accurately.")
            }

            // If the user is far from Manhattan, shift other default members to center around them
            val distanceFromManhattan = repository.calculateDistanceMeters(lat, lng, 40.7678, -73.9718)
            if (distanceFromManhattan > 15000.0 && !hasShiftedCluster) {
                hasShiftedCluster = true
                relocateSimulatedClusterTo(lat, lng)
            }
        }
    }

    private suspend fun relocateSimulatedClusterTo(userLat: Double, userLng: Double) {
        // Shift all other family members relative to the new live location
        val otherMembers = members.value.filter { it.id != 1 }
        for (member in otherMembers) {
            val updatedStatus = when (member.relationship) {
                "Mom" -> "☕ Coffee shop nearby"
                "Dad" -> "🚗 Headed your way"
                "Sister" -> "📚 Studying nearby"
                "Brother" -> "🍕 Pizza shop close by"
                "Grandma" -> "🏡 Visiting neighbors"
                "Grandpa" -> "🌳 Walking in the park"
                else -> "📍 Simulated tracking nearby"
            }
            // Generate spaced offsets around user location
            val offsetIndex = (member.id % 5) + 1
            val dLat = (offsetIndex * 0.0018) * if (member.id % 2 == 0) 1.0 else -1.0
            val dLng = (offsetIndex * 0.0022) * if (member.id % 3 == 0) 1.0 else -1.0

            repository.insertMember(member.copy(
                latitude = userLat + dLat,
                longitude = userLng + dLng,
                statusMessage = updatedStatus,
                lastUpdateTime = System.currentTimeMillis()
            ))
        }

        // Shift geofences relative to user's local address
        val currentGeos = geofences.value
        currentGeos.find { it.id == 1 }?.let { homeGeo ->
            repository.insertGeofence(homeGeo.copy(
                name = "My Real Home Base",
                latitude = userLat,
                longitude = userLng
            ))
        }
        currentGeos.find { it.id == 2 }?.let { libGeo ->
            repository.insertGeofence(libGeo.copy(
                latitude = userLat + 0.0062,
                longitude = userLng + 0.0042
            ))
        }
        currentGeos.find { it.id == 3 }?.let { coffeeGeo ->
            repository.insertGeofence(coffeeGeo.copy(
                latitude = userLat + 0.0034,
                longitude = userLng - 0.0022
            ))
        }

        repository.insertLog("System", "🌍 Cluster relocated dynamically around your actual GPS coordinates!")
    }

    fun loginUser(username: String) {
        sharedPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("logged_in_username", username)
            .apply()
        _isLoggedIn.value = true
        _loggedInUsername.value = username

        viewModelScope.launch(Dispatchers.IO) {
            val centerLat = 40.7678
            val centerLng = -73.9718
            val existing = database.familyMemberDao().getMemberById(1)
            val nameToUse = if (username.contains("(Me)", ignoreCase = true)) username else "$username (Me)"

            val userEntity = FamilyMemberEntity(
                id = 1,
                name = nameToUse,
                relationship = "Me",
                latitude = existing?.latitude ?: centerLat,
                longitude = existing?.longitude ?: centerLng,
                batteryPercent = existing?.batteryPercent ?: 95,
                statusMessage = "🗺️ Connected via Family Radar Hub",
                avatarEmoji = "🧑‍💻",
                colorHex = "#6366F1", // Indigo
                speed = existing?.speed ?: 0.0
            )
            repository.insertMember(userEntity)
            repository.insertLog(nameToUse, "🔐 Signed in & established connection link successfully.")
            _selectedMemberId.value = 1
        }
    }

    fun logoutUser() {
        val username = _loggedInUsername.value
        sharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("logged_in_username", "")
            .apply()
        _isLoggedIn.value = false
        _loggedInUsername.value = ""

        viewModelScope.launch(Dispatchers.IO) {
            val nameToUse = if (username.isBlank()) "Me" else if (username.contains("(Me)", ignoreCase = true)) username else "$username (Me)"
            repository.insertLog(nameToUse, "🔓 Signed out & disconnected link safely.")
            repository.deleteMember(1, nameToUse)
            _selectedMemberId.value = null
        }
    }
}
