# Super Star Trek (Android Port)

![sst_screenshot](https://github.com/user-attachments/assets/74746a52-945f-4bc7-b385-2b0cdad552e0)

📥 **Looking for the playable APK?** [Download the latest Android release here! (v1.0.0)](https://github.com/cagriakyurt/SuperStarTrek/releases/tag/v1.0.0)

This project is a modern **Kotlin & Android** port of the classic **Super Star Trek** (BASIC) game, originally released in 1978. It has been entirely rewritten from scratch for modern mobile devices using Android Studio, meticulously preserving the authentic rules, mechanics, and spirit of the original original source code.

## Features

*   **Optimized Retro Terminal UI:** Features a sleek, monospace green-phosphor terminal layout that emulates the look and feel of playing on a 1970s mainframe.
*   **Real-time Teletype Output:** The classic BASIC `GOTO` chains and string slicing paradigms are reimagined using asynchronous Kotlin Coroutines and `delay()`. This creates a nostalgic "teletype typewriter" rhythm as the text streams onto the terminal!
*   **Original Combat & Game Logic:** Everything functions 100% true to the original 1978 math—including Long Range Scan (LRS), Short Range Scan (SRS), Phasers (PHA), Photon Torpedoes (TOR), and the exact algorithms for quadrant mapping and Starbase docking. 
*   **Vector Radar Icon:** Comes with a custom-built, purely XML VectorDrawable adaptive icon featuring an edge-to-edge neon green retro radar, replacing the standard Android icon perfectly across all launcher shapes.

## How to Play

Upon running the app, you will be prompted for a command (`COMMAND>`). You must navigate the galaxy and manage your ship's resources within the limited timeframe (Stardates) granted by Starfleet Command.
Your goal is to eliminate all remaining Klingon warships and save the Federation!

*   **NAV:** Engage warp engines to change your course and speed (Warp factor: '0-8'). Course vectors are based on the traditional 1-9 clock-face layout, where the Enterprise is situated at the center:
    ```text
          4   3   2
           \  |  /      (North = 3)
            \ | /
      5 ----- * ----- 1 (East = 1)
            / | \
           /  |  \
          6   7   8
    ```
*   **SRS:** Perform a Short Range Sensor scan to print the map of your current galactic quadrant (costs no energy).
*   **LRS:** Perform a Long Range Sensor scan to radar-sweep adjacent galactic quadrants.
*   **PHA:** Fire Phasers at enemy ships by distributing your available energy.
*   **TOR:** Launch Photon Torpedoes at neighboring Klingon vessels using course coordinates (1-9).
*   **SHE:** Transfer energy to raise or lower the Enterprise's defensive shields.
*   **DAM:** Display damage control reports showing the repair status of the ship's internal devices.
*   **COM:** Access the main Library-Computer to calculate exact course/distance vectors, check mission status, or view your total explored Galactic Record.
*   **XXX:** Resign your command and restart the simulation.

**How do I Dock at a Starbase?**: When you navigate into any of the 8 immediate adjacent tiles next to a Starbase (`>!<`) on the local map, your condition automatically changes to `DOCKED` and your Energy + Torpedoes are fully replenished!

## Development Environment
*   **Language:** Kotlin
*   **Minimum SDK:** API 24
*   **Architecture:** Implements a custom Asynchronous `IOHandler` Interface to act as a bridge between the core game loop processing and the UI's layout scrolling views.
