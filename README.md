# Smart Task Engine 🧠⏳

An intelligent Android task engine that dynamically prioritizes, visualizes urgency, and automatically cleans expired tasks.

## 🚀 Features
- Time-aware task lifecycle with auto-deletion
- Importance-based priority scoring
- Real-time urgency color indicators
- Background cleanup using WorkManager
- Swipe-to-delete with undo support
- Clean MVVM architecture with Room & Flow

## 🧠 Engine Logic
| Condition | Indicator |
|----------|-----------|
| ≤ 10 min left | 🔴 Red |
| 11–30 min left | 🟠 Orange |
| >30 min + importance ≥4 | 🟢 Green |

Tasks automatically remove themselves after expiry.

## 🛠 Tech Stack
- Kotlin
- Android Jetpack (Room, ViewModel, WorkManager)
- MVVM + Repository pattern
- Coroutines & Flow

## 📸 Screenshots
![Home](home.png)

## 🎥 Demo

[Watch Demo](smart_task_engine_demo.mp4)

## 💡 Why This Project?
Built to demonstrate system design thinking beyond CRUD apps — this is a self-regulating task engine.
