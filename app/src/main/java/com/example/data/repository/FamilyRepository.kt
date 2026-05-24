package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FamilyRepository(
    private val memberDao: FamilyMemberDao,
    private val geofenceDao: GeofenceDao,
    private val logDao: TrackingLogDao
) {
    val allMembers: Flow<List<FamilyMemberEntity>> = memberDao.getAllMembers()
    val allGeofences: Flow<List<GeofenceEntity>> = geofenceDao.getAllGeofences()
    val allLogs: Flow<List<TrackingLogEntity>> = logDao.getAllLogs()

    private val random = Random()

    // Base coordinates to keep members within boundary
    private val baseLat = 40.7678
    private val baseLng = -73.9718

    suspend fun insertMember(member: FamilyMemberEntity) {
        memberDao.insertOrUpdate(member)
        logDao.insertLog(TrackingLogEntity(memberName = member.name, event = "👤 Added or updated profile."))
    }

    suspend fun deleteMember(id: Int, name: String) {
        memberDao.deleteById(id)
        logDao.insertLog(TrackingLogEntity(memberName = "System", event = "❌ Removed family member: $name"))
    }

    suspend fun insertGeofence(geofence: GeofenceEntity) {
        geofenceDao.insertOrUpdate(geofence)
        logDao.insertLog(TrackingLogEntity(memberName = "System", event = "📍 Added safety boundary: ${geofence.name} (${geofence.radiusMeters.toInt()}m)"))
    }

    suspend fun deleteGeofence(id: Int, name: String) {
        geofenceDao.deleteById(id)
        logDao.insertLog(TrackingLogEntity(memberName = "System", event = "🗑️ Deleted safety zone: $name"))
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }

    suspend fun insertLog(memberName: String, event: String) {
        logDao.insertLog(TrackingLogEntity(memberName = memberName, event = event))
    }

    /**
     * Simulates a single time tick:
     * - Randomly moves members who are not stationary.
     * - Adjusts battery percentage and speeds.
     * - Triggers geofence check triggers (enter / exit events).
     */
    suspend fun runSimulationTick(currentMembers: List<FamilyMemberEntity>, geofences: List<GeofenceEntity>) {
        val me = currentMembers.find { it.id == 1 }
        val activeBaseLat = me?.latitude ?: baseLat
        val activeBaseLng = me?.longitude ?: baseLng

        for (member in currentMembers) {
            // "Me" doesn't simulate random wander automatically (unless desired), keeps user-controlled.
            // Or maybe everyone moves slightly! Let's simulate movement for those who are "Online".
            if (!member.isOnline) continue

            var newLat = member.latitude
            var newLng = member.longitude
            var newSpeed = member.speed
            var newBattery = member.batteryPercent
            var newStatus = member.statusMessage
            var isSos = member.isSOS

            // Decrement battery occasionally (or recharge if "Me" or simulated)
            if (random.nextInt(100) < 15) {
                newBattery = (newBattery - 1).coerceIn(1, 100)
                if (newBattery < 20 && member.batteryPercent >= 20) {
                    logDao.insertLog(TrackingLogEntity(
                        memberName = member.name,
                        event = "🪫 Battery critically low! (${newBattery}%)"
                    ))
                }
            }

            // Decide current motion state
            val stateRand = random.nextInt(100)
            if (member.relationship != "Me") {
                if (stateRand < 10) {
                    // Stop movement
                    newSpeed = 0.0
                    newStatus = when (member.relationship) {
                        "Mom" -> "🛋️ Relaxing at home"
                        "Dad" -> "💼 Working desk-side"
                        "Sister" -> "📖 Reading textbook"
                        else -> "📍 Stationary"
                    }
                } else if (stateRand < 30) {
                    // Start moving (walking or driving)
                    val driving = random.nextBoolean()
                    newSpeed = if (driving) (30 + random.nextInt(35)).toDouble() else (3 + random.nextInt(4)).toDouble()
                    newStatus = if (driving) "🚗 Commuting to meeting" else "🚶 Walking nearby"
                }

                // Apply small displacement if moving
                if (newSpeed > 0.0) {
                    val angle = random.nextDouble() * 2 * Math.PI
                    // Convert km/h to approximate degrees lat/lng per tick (very simplified animation offsets)
                    val velocityCoeff = if (newSpeed > 10) 0.00078 else 0.00015
                    newLat += sin(angle) * velocityCoeff
                    newLng += cos(angle) * velocityCoeff

                    // Bind boundaries so they don't wander off completely
                    val distFromBase = calculateDistanceMeters(newLat, newLng, activeBaseLat, activeBaseLng)
                    if (distFromBase > 2000.0) {
                        // Pull back to center
                        newLat = newLat * 0.9 + activeBaseLat * 0.1
                        newLng = newLng * 0.9 + activeBaseLng * 0.1
                    }
                }
            }

            // Perform Geofence check
            checkGeofenceChanges(member, newLat, newLng, geofences)

            // Update database
            memberDao.insertOrUpdate(member.copy(
                latitude = newLat,
                longitude = newLng,
                speed = newSpeed,
                batteryPercent = newBattery,
                statusMessage = newStatus,
                isSOS = isSos,
                lastUpdateTime = System.currentTimeMillis()
            ))
        }
    }

    private suspend fun checkGeofenceChanges(
        member: FamilyMemberEntity,
        newLat: Double,
        newLng: Double,
        geofences: List<GeofenceEntity>
    ) {
        val oldLat = member.latitude
        val oldLng = member.longitude

        for (g in geofences) {
            val wasIn = calculateDistanceMeters(oldLat, oldLng, g.latitude, g.longitude) <= g.radiusMeters
            val isIn = calculateDistanceMeters(newLat, newLng, g.latitude, g.longitude) <= g.radiusMeters

            if (!wasIn && isIn) {
                logDao.insertLog(TrackingLogEntity(
                    memberName = member.name,
                    event = "${g.emoji} Entered Safety Zone: ${g.name}"
                ))
            } else if (wasIn && !isIn) {
                logDao.insertLog(TrackingLogEntity(
                    memberName = member.name,
                    event = "🏃 Departed Safety Zone: ${g.name}"
                ))
            }
        }
    }

    /**
     * Standard distance calculation between coordinates in meters
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
