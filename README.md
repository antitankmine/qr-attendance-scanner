# SchoolThing — Dynamic QR Attendance System

**SchoolThing** is an offline-first Android attendance tracking platform designed for educational institutions. It uses dynamic, time-sensitive QR codes to eliminate proxy check-ins, provides role-based management for Students, Gate Guards, Teachers, and Administrators, and guarantees uninterrupted gate operations using local Room SQLite queuing and dual cloud/REST backend synchronization.

---

## Table of Contents
- [Overview](#overview)
- [Key Features](#key-features)
  - [1. Security & Dynamic QR Verification](#1-security--dynamic-qr-verification)
  - [2. Role-Based Dashboards](#2-role-based-dashboards)
  - [3. Offline-First Architecture & Data Sync](#3-offline-first-architecture--data-sync)
- [System Architecture](#system-architecture)
- [User Roles & Permissions](#user-roles--permissions)
- [Tech Stack & Dependencies](#tech-stack--dependencies)
- [Project Directory Structure](#project-directory-structure)
- [Setup & Installation](#setup--installation)

---

## Overview

Traditional attendance systems relying on manual roll calls or static QR codes suffer from proxy attendance, screenshot sharing, and single-point-of-failure network dependencies. 

**SchoolThing** resolves these problems by combining:
1. **Time-sensitive dynamic QR passes** generated locally with HMAC-SHA256 signatures that auto-refresh every 30 seconds.
2. **CameraX + ZXing real-time scanner** for gate guards with instant visual pass/fail feedback cards and flashlight control.
3. **Offline-first local queuing** via AndroidX Room database, ensuring gate scanning never halts due to network outages.
4. **Role-specific portals** for student pass generation, teacher classroom monitoring, and admin system controls.

---

## Key Features

### 1. Security & Dynamic QR Verification
* **Cryptographic HMAC-SHA256 Signatures:** QR payloads encode student ID, name, timestamp, and a secret HMAC signature (`QrCryptoUtils.java`) to prevent tampering.
* **30-Second Refresh Cycle:** QR codes auto-expire and regenerate on a 30-second timer (`StudentQrActivity.java`), preventing screenshot sharing or reuse.
* **Clock Skew Tolerance:** Includes a 15-second grace buffer to accommodate minor device clock differences.

### 2. Role-Based Dashboards
* **Student Pass (`StudentQrActivity`):**
  * Live dynamic QR pass display with a visual countdown timer and progress bar.
  * Password update dialog with Firebase Auth / local storage sync.
  * Personal attendance history log.
* **Gate Guard Scanner (`GateGuardScannerActivity`):**
  * Real-time camera scanner powered by AndroidX CameraX and ZXing.
  * Flashlight / torch toggle button.
  * Instant visual feedback cards (Green = Entry Approved/On Time, Yellow = Late Arrival, Red = Access Denied/Invalid QR).
  * Today's scanned accounts log view.
* **Teacher Console (`TeacherDashboardActivity`):**
  * Student account creation modal.
  * Real-time searchable classroom attendance roster.
  * Direct access to the Gate Scanner module.
* **Administrator Console (`AdminDashboardActivity`):**
  * Account creation and revocation system across all roles.
  * Interactive `TimePickerDialog` threshold setters for Late and Absent cutoff times.
  * Live active on-campus student presence monitoring (`fetchActiveOnCampus`).

### 3. Offline-First Architecture & Data Sync
* **Local Room Database (`AppDatabase` & `ScanRecordDao`):** Every scan is instantly recorded into local SQLite (`scan_records` table) before network dispatch.
* **Dual Remote Integration:**
  * **Firebase Integration (`FirebaseManager`):** Manages user authentication, Firestore user profiles (`users` collection), and attendance event logs (`attendance` collection).
  * **Retrofit 2 REST API (`AttendanceRepository`):** Syncs unsynced local records to a local REST server (`http://10.0.2.2:8080`).
* **Seamless Fallback:** Operates offline smoothly using local `SharedPreferences` (`UserManager`) and local Room storage when internet connectivity is lost or Firebase is unavailable.

---

## System Architecture

```
                               ┌─────────────────────────┐
                               │ Dynamic QR Pass         │
                               │  - Student ID & Name    │
                               │  - 30s HMAC-SHA256 Sig  │
                               └───────────┬─────────────┘
                                           │
                                           ▼
 ┌──────────────────────────────────────────────────────────────────────────────────┐
 │                            Android Scanning Client                               │
 │                                                                                  │
 │  ┌─────────────────────┐      ┌──────────────────────┐     ┌──────────────────┐  │
 │  │  CameraX + ZXing    │ ───► │  HMAC Verification   │ ──► │  Local Room DB   │  │
 │  │    ImageAnalysis    │      │  & Status Calculation│     │  (SQLite Queue)  │  │
 │  └─────────────────────┘      └──────────────────────┘     └────────┬─────────┘  │
 └──────────────────────────────────────────┬──────────────────────────┴─────────┘  │
                                            │                                       │
                                   Firebase / REST Sync                     Offline Queue
                                            │                                       │
                                            ▼                                       ▼
 ┌──────────────────────────────────────────────────────────────────────────────────┐
 │                             Remote / Local Backend                               │
 │                                                                                  │
 │  ┌─────────────────────┐      ┌──────────────────────┐     ┌──────────────────┐  │
 │  │ Firebase Auth /     │      │ Firestore DB         │     │ Retrofit REST    │  │
 │  │ UserManager         │      │ (users / attendance) │     │ Server (8080)    │  │
 │  └─────────────────────┘      └──────────────────────┘     └──────────────────┘  │
 └──────────────────────────────────────────────────────────────────────────────────┘
```

---

## User Roles & Permissions

| Role | Access Level | Primary Capabilities | Target Activity |
| :--- | :--- | :--- | :--- |
| **Student** | Student Pass | Display dynamic QR code, view personal logs, update password | `StudentQrActivity` |
| **Gate Guard** | Scanner Portal | Scan student QR codes, view scan status feedback, toggle flash | `GateGuardScannerActivity` |
| **Teacher** | Classroom Portal | View/search roster logs, create student accounts, launch scanner | `TeacherDashboardActivity` |
| **Administrator** | Admin Console | Create/revoke any user role, configure cutoff times, view on-campus report | `AdminDashboardActivity` |

---

## Tech Stack & Dependencies

* **Language & Core:** Java (Android SDK), ViewBinding, AndroidX AppCompat.
* **UI Design:** XML Layouts, Material Design Components (`com.google.android.material`), Jetpack Compose integration support.
* **Camera & Scanning:**
  * `androidx.camera:camera-core:1.4.1`
  * `androidx.camera:camera-camera2:1.4.1`
  * `androidx.camera:camera-lifecycle:1.4.1`
  * `androidx.camera:camera-view:1.4.1`
  * `com.google.zxing:core:3.5.3`
  * `com.journeyapps:barcodescanner:4.3.0`
* **Local Persistence:**
  * `androidx.room:room-runtime:2.6.1`
  * `SharedPreferences` (`UserManager.java`)
* **Networking & Backend:**
  * `com.squareup.retrofit2:retrofit:2.11.0`
  * `com.squareup.retrofit2:converter-gson:2.11.0`
  * Firebase BOM (`com.google.firebase:firebase-bom:33.9.0`) with Firebase Auth and Cloud Firestore.
* **Target & Minimum SDK:** `compileSdk 35`, `targetSdk 35`, `minSdk 24` (Android 7.0+).

---

## Project Directory Structure

```
app/src/main/java/com/example/schoolthing/
├── MainActivity.java                # Main portal entry point
├── data/
│   ├── local/
│   │   ├── AppDatabase.java         # Singleton Room Database definition
│   │   ├── ScanRecordDao.java       # Room Data Access Object for scan queue
│   │   ├── ScanRecordEntity.java    # SQLite entity model for scans
│   │   └── UserManager.java         # SharedPreferences session manager
│   ├── model/
│   │   ├── AttendanceStatus.java    # Enum: PRESENT, LATE, INVALID
│   │   ├── AttendanceType.java      # Enum: CAMPUS_ENTRY, CAMPUS_EXIT, CLASS_CHECKIN
│   │   ├── SystemConfig.java        # Cutoff thresholds model
│   │   └── UserRole.java            # Enum: STUDENT, GATE_GUARD, TEACHER, ADMINISTRATOR
│   ├── remote/
│   │   ├── AttendanceApiService.java # Retrofit interface for REST backend
│   │   └── FirebaseManager.java     # Firebase Firestore & Auth orchestrator
│   └── repository/
│       └── AttendanceRepository.java # Offline-first scan queuing & sync repository
├── security/
│   ├── DynamicQrPass.java           # Model for parsed dynamic QR data
│   └── QrCryptoUtils.java           # HMAC-SHA256 generator & signature verifier
└── ui/
    ├── LoginActivity.java           # User authentication & role routing
    ├── SignUpActivity.java          # Account registration entry point
    ├── StudentQrActivity.java       # Student dynamic pass UI with 30s timer
    ├── GateGuardScannerActivity.java# CameraX scanner interface with status cards
    ├── TeacherDashboardActivity.java# Roster search & student creation portal
    └── AdminDashboardActivity.java  # Admin settings, account revocation, campus status
```

---

## Setup & Installation

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer.
- Android SDK 35 (Android 15).
- Java 11 JDK configured.
- (Optional) Firebase project with `google-services.json` placed in the `app/` directory for cloud sync.

### Building & Running
1. Clone or open the project directory in Android Studio:
   ```bash
   cd schoolthing
   ```
2. Sync the project with Gradle files (`app/build.gradle.kts`).
3. Deploy to a physical Android device or emulator running API 24 or higher.
4. On initial launch, complete account registration via the **Sign Up** screen to set up your primary account and assign your target role.
