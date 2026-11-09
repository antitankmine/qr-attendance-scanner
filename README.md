# QR Attendance Scanner (`qr-attendance-scanner`)

Okay. This is a school attendance system. Students scan a QR code, staff verify it, and the school keeps track of who showed up without turning the whole thing into a clipboard circus.

No drama. No cloud dependency unless you want it. It works locally, it survives bad Wi‑Fi, and it keeps the attendance process moving instead of grinding to a halt when the network decides to take a nap.

---

## Table of contents
- [What this is](#what-this-is)
- [Why it exists](#why-it-exists)
- [Key features](#key-features)
  - [1. Security and identity checks](#1-security-and-identity-checks)
  - [2. Attendance and operations](#2-attendance-and-operations)
  - [3. Real-time communication and automation](#3-real-time-communication-and-automation)
  - [4. Data and server setup](#4-data-and-server-setup)
- [System architecture](#system-architecture)
- [Institution setup models](#institution-setup-models)
- [Feature matrix](#feature-matrix)
- [Tech stack](#tech-stack)
- [Installation and setup](#installation-and-setup)

---

## What this is

`qr-attendance-scanner` is a simple idea, done properly: a mobile app plus a local backend that tracks student attendance in real time.

Students scan. Staff confirm. The system records attendance. Everyone gets less chaos.

This is for schools and institutions that are tired of manual roll calls, fake check-ins, and systems that die the second the internet hiccups.

---

## Why it exists

Because attendance tracking is usually a mess.

Paper lists are easy to lose. Static QR codes are easy to cheat. Weak systems get overwhelmed during busy periods. And a lot of schools still run on infrastructure that is basically held together by hope and an old router.

This project exists to make that less awful. It keeps the process fast at the gate, gives teachers a better view of what is happening, and gives admins actual control without forcing everything into the cloud.

---

## Key features

### 1. Security and identity checks
* **Dynamic QR codes:** Not the lazy static kind. These refresh on a timer so someone cannot just screenshot a code and reuse it later like a chaos goblin.
* **Role-based access:** Different people get different jobs and different permissions.
  * **Gate guards:** Quick scan interface with instant feedback. Green = present, yellow = late, red = invalid.
  * **Teachers:** Classroom check-in, exception management, and live attendance trends.
  * **Administrators:** User setup, class schedules, server settings, and audit logs.
* **Device and IP restrictions:** Keep admin access and scanning locked to approved hardware or trusted Wi‑Fi ranges.

### 2. Attendance and operations
* **Entry, exit, and transit tracking:** It knows the difference between entering campus, leaving campus, and checking into a class. That matters.
* **Custom grace periods and schedule logic:** Bell schedules are not one-size-fits-all. The system can classify attendance as on time, late, or unexcused absence based on working rules.
* **Offline-first scan queue:** If the network drops, scans still get saved locally and sync later when the connection is back. Smart, not dramatic.

### 3. Real-time communication and automation
* **Guardian alerts:** Parents can get notified when a student is late or misses a scan window.
* **Emergency count and roll call mode:** When there is a drill or a real emergency, staff can see who is on campus and who is still unaccounted for in real time.

### 4. Data and server setup
* **Self-hosted local backend:** Runs on a local institutional server with Docker, which is exactly what you want when you do not trust a third-party cloud setup with student data.
* **Bulk CSV and Excel processing:** Import large rosters and export reports without manually doing all the work by hand.
* **Automated backups:** Scheduled backups help keep records safe and make compliance less painful.

---

## System architecture

```
                               ┌─────────────────────────┐
                               │ Dynamic QR Pass         │
                               │ (Student Phone / Card)  │
                               └───────────┬─────────────┘
                                           │
                                           ▼
 ┌──────────────────────────────────────────────────────────────────────────────────┐
 │                            Android Scanning Client                               │
 │                                                                                  │
 │  ┌─────────────────────┐      ┌──────────────────────┐     ┌──────────────────┐  │
 │  │ Camera / Scan Logic │ ───► │ Status Verification  │ ──► │ Encrypted Local  │  │
 │  └─────────────────────┘      └──────────────────────┘     │ SQLite Sync Queue│  │
 └──────────────────────────────────────────┬─────────────────┴────────┬─────────┘  │
                                            │                          │            │
                                  Online Sync (REST)         Offline Retries        │
                                            │                          │            │
                                            ▼                          ▼            │
 ┌──────────────────────────────────────────────────────────────────────────────────┐
 │                     Self-Hosted Institutional Server                             │
 │                                                                                  │
 │  ┌─────────────────────┐      ┌──────────────────────┐     ┌──────────────────┐  │
 │  │ Spring Boot REST API│ ───► │ PostgreSQL / MySQL   │ ──► │ Notification Gate│  │
 │  └─────────────────────┘      └──────────────────────┘     └──────────────────┘  │
 └──────────────────────────────────────────────────────────────────────────────────┘
```

---

## Institution setup models

| Institution size | Deployment approach |
| :--- | :--- |
| **Small (K-12 / single facility)** | One desktop machine acts as the local server; Android scanners connect over the school Wi‑Fi. |
| **Medium (college / multi-building)** | An on-premise server handles the campus network, with checkpoint tablets and teacher devices connected to it. |
| **Large (university / multi-campus)** | Dockerized services run on a high-availability local setup with multiple checkpoints and synchronized gate operations. |

---

## Feature matrix

| Feature | Primary role | Operational impact |
| :--- | :--- | :--- |
| **Offline sync queue** | Gate guards / teachers | Stops the line from freezing when the internet decides to quit. |
| **Dynamic QR verification** | Gate guards | Cuts down on proxy check-ins and fake attendance. |
| **Automated alerts** | Parents / admins | Keeps people informed without chasing them down manually. |
| **Bulk roster import/export** | Teachers / admins | Saves hours of repetitive admin work. |

---

## Tech stack

* **Mobile app:** Java (Android SDK), CameraX / ZXing for scanning, Room / SQLite for offline storage, Retrofit2 for API communication.
* **Backend:** Java Spring Boot (or Node.js/Express) with Spring Security for authentication.
* **Database:** PostgreSQL / MySQL.
* **Containerization:** Docker and Docker Compose for local deployment without the usual setup pain.

---

#- die die die die die die die die die die hi 
<img width="1280" height="720" alt="image" src="https://github.com/user-attachments/assets/ffd38525-2e11-4e88-859a-a9c3358229c6" />



