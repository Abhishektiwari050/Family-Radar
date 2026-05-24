# 🛰️ Family Radar // Stark Wireframe Edition

A high-impact, real-time family tracking console and telemetry dashboard engineered with a bold **Neo-Brutalist design language**. Synthesizing rich coordinate tracking, safety geofences, simulated pathway vectors, and instant SOS lifelines, Family Radar brings enterprise-grade reliability and modular execution to personal telemetry.

[![Platform](https://img.shields.io/badge/Platform-Android-00FF80?style=flat-square&logo=android&logoColor=black)](#)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F00FF?style=flat-square&logo=kotlin&logoColor=white)](#)
[![Design](https://img.shields.io/badge/Design-Neo--Brutalist-D2FF00?style=flat-square&logo=materialdesign&logoColor=black)](#)
[![Database](https://img.shields.io/badge/Database-Room-PinkGlow?style=flat-square&logo=sqlite&logoColor=white)](#)

---

## ⬇️ Pre-Compiled Delivery // Quick Install

Get the application up and running on your Android device:

* **[Download Latest Release APK](https://github.com/yourusername/family-radar/releases/latest/download/app-debug.apk)** *(Ready-to-install debug architecture signature)*
* **Alternative Build Actions**:
  * Build your own APK from source locally in seconds by running the compilation task pipeline listed in the [Building & Launching](#-building--launching) section below.
  * In active development environments, utilize the container's export tools to package signed release APKs/AABs or zip files directly through the workspace options list.

---

## 🎨 Visual Identity & Brutalist Manifesto

Family Radar rejects generic, polished corporate design in favor of **strict, high-contrast flat panels**. 

Developed from raw structural shapes, the application implements:
* **High-Stroke Vector Boarders**: Constant 2.5dp–4dp solid black layouts framing every single interaction deck.
* **Aggressive Accent Pairings**: Saturated **Cyber Green (`#D2FF00`)** used for active status and live pathways, combined with neon **Pink Glow (`#FF007F`)** for alert gates and urgent event logs.
* **Generous Stark Negative Space**: Clean backgrounds anchored on highly legible grotesque text sizing for immediate optical scanning.
* **Adaptive Dual-Column Cockpit**: Seamless canonical transition which scales into a side-by-side cockpit layout on tablets/foldables, returning to an ergonomic bottom-bar on compact viewports.

---

## 🛰️ Major Capabilities

### 1. Unified Telemetry Center
* **Live Unit Geolocation**: Tracking of battery statuses, real-time movement velocity, active coordinates, and local connection feeds.
* **Dynamic Coordinate Shift**: Integrated device hardware sensors via `FusedLocationProviderClient` to extract fine-grained position tracking.

### 2. High-Fidelity Vector Radar Map
* **Directional Radial Sweep**: Interactive custom vectors drawing active boundary scopes, dynamic member anchors, and step-history traces.
* **Navigation Pan Deck**: Supports real-time coordinate transformations, canvas-interactive drag offsets, and scaling.

### 3. Safety Geofence Gates (Safety Zones)
* **Radial Boundaries**: Customize physical boundaries with specialized limits defined in meters.
* **Instant Event Dispatch**: Background coordinate checks identify zone crossings, logging exact entry/exit timestamps automatically.

### 4. Interactive Simulation Pathways
* **Simulation D-PAD Controller**: Allows hardware path simulation directly within the client interface. Shift, step, and testing coordinate sequences using a simple four-axis mock pathway control grid.

### 5. Instant SOS Lifelines
* **Beacons**: Trigger Emergency alert vectors. Instantly paints the tracked member with active emergency graphics, popping alert banners up globally.
* **Direct Deep Launcher**: Directly launch coordinates to Google Maps to trigger immediate rescue routing.

---

## 📂 Architecture & Technical Stack

Family Radar is structured following strict **MVVM (Model-View-ViewModel)** guidelines and clean separating layers:

```
├── data/
│   ├── local/
│   │   ├── FamilyDatabase.kt       # Room SQLite Database
│   │   ├── FamilyMemberEntity.kt   # Tracked unit persistent parameters
│   │   ├── GeofenceEntity.kt       # Radial boundary configurations
│   │   └── TrackingLogEntity.kt    # System logs & transition events
├── ui/
│   ├── theme/
│   │   ├── Color.kt                # Neo-Brutalist palette tokens
│   │   ├── Theme.kt                # Material 3 typography integrations
│   │   └── NeoBrutalistUtils.kt    # High-stroke card and shadow modifiers
│   ├── screens/
│   │   ├── LoginScreen.kt          # Stark multi-mode entrance gate
│   │   ├── FamilyListScreen.kt     # Main telemetry listings & D-Pad
│   │   ├── GeofenceManagerScreen.kt# Configurable safety boundaries 
│   │   └── LogsScreen.kt           # Timeline feeds and historical checks
│   ├── components/
│   │   └── RadarMap.kt             # Custom Canvas coordinate-system raster
│   └── viewmodel/
│       └── FamilyViewModel.kt      # State Management and GPS updates
└── MainActivity.kt                 # Canonical view dispatcher & edge-to-edge config
```

* **Jetpack Compose**: 100% declarative layouts structured using custom standard components, material drawers, and dynamic modifiers.
* **Kotlin Coroutines / Flows**: Reactive asynchronous pipelines emitting telemetry pulses every second without thread blocks.
* **Room Database**: Rigid client-side thread-safe persistence with automated setup and migration layers.
* **Google Play Services Location**: Device location adapters mapping real-world coordinates via high-accuracy callbacks.

---

## 🚀 Building & Launching

Family Radar compiles cleanly utilizing modern tools. Follow these steps to build the APK.

### Prerequisites
* **Android Studio Ladybug** (or later)
* **JDK 17 / 21**
* **Gradle 8.4+**

### Compilation Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/family-radar.git
   cd family-radar
   ```
2. Build the debug application bundle via Gradle task runners:
   ```bash
   gradle assembleDebug
   ```
3. Run tests locally:
   ```bash
   gradle test
   ```

---

## ⚙️ Native Configurations

The application runs purely **offline and local-first** — no external servers, database connections, or developer subscription credentials are required to run. All locations are stored locally using SQLite and security is handled through simple offline profiles. 

Hardware telemetry requests standard Android Runtime Permissions:
* `Manifest.permission.ACCESS_FINE_LOCATION` (Required to map coordinates via the GPS transmitter)
* `Manifest.permission.ACCESS_COARSE_LOCATION`

---

## 🖼️ User Interface Preview

```
+-------------------------------------------------------------+
|  [🛰️] FAMILY_RADAR //                      LOGGED_IN: ADMIN |
+-------------------------------------------------------------+
|                                                             |
|   +-----------------------+    +------------------------+   |
|   | MEMBERS_TRACKED //    |    | SAFETY_ZONES //        |   |
|   | TOTAL COUNT: 4        |    | DEFINED: 2             |   |
|   |                       |    |                        |   |
|   | [👩 ME] L1: (Active)  |    | [🏠 HOME] 200m         |   |
|   | Battery: 94%          |    | [🏫 SCHOOL] 150m       |   |
|   |                       |    +------------------------+   |
|   | [👨 DAD] L2: G-Maps   |    +------------------------+   |
|   | Battery: 80%          |    | TIMELINE_FEED //       |   |
|   |                       |    | [09:12] ME Left Home   |   |
|   | [🚨 EMERGENCY ACTIVE] |    | [09:15] ME Entered Sch |   |
|   +-----------------------+    +------------------------+   |
|                                                             |
+-------------------------------------------------------------+
```

---

## 📜 Licensing & Usage

Distributed under the **MIT License**. Under strict adherence to modular development protocols, you are free to modify, extend, or skin this system for your personal tracking requirements.
