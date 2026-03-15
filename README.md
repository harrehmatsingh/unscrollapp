# Unscroll

Unscroll helps you track daily scrolling across selected apps and adds a gentle nudge when a scroll session goes on too long. It focuses on simple, visible feedback and a friends-only leaderboard that celebrates lower scroll counts.

## What it does
- Google sign-in with Firebase Auth
- Select which apps to track
- Daily scroll count with a midnight reset
- Doomscroll alarm when continuous scrolling exceeds your limit
- Friends, mutual connections, and a friends-only daily leaderboard

## How it works (short version)
The app uses an accessibility service to detect scroll events inside the apps you choose. Counts are stored locally and synced to Firestore for the friends leaderboard. The doomscroll alarm watches for long, continuous scroll sessions and triggers an alert.

## Project setup
1. Open the project in Android Studio.
2. Add your Firebase project and place `google-services.json` in `app/` (this file is ignored by git).
3. Sync Gradle and run the `app` configuration.

## Notes
- The leaderboard ranks by lowest daily scroll count.
- Your row is highlighted in the leaderboard for quick reference.
- Alarm settings are in a separate screen, and the Stop Alarm button remains on the main screen.

## Contributing
Issues and improvements are welcome. If you want to propose changes, open an issue first so we can align on the approach.
