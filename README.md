# Ad Skipper

An Android app that automatically skips ads in **YouTube** and **YouTube Music** by tapping the Skip button for you. Runs quietly in the background.

No root. No modified APKs. No WebView. Just the real YouTube app, with ads skipped.

## Features

- **Auto-skip ads** in YouTube and YouTube Music
- **Skip timing** — optional delay (Off / 5 / 10 / 15 seconds) so creators still get ad revenue
- **Pause list** — automatically pauses itself when banking apps, password managers, and other Accessibility-sensitive apps are in front
- **Skip log** — shows every skip with timestamp, source (exact or heuristic), and button details
- **Heuristic fallback** — survives YouTube UI updates by looking for the Skip button's position when the exact match fails

## How it works

The app uses Android's **Accessibility Service** API. It watches the screen when YouTube is in the foreground, finds the ad's Skip button, and taps it. It does not modify YouTube, does not run a proxy, and does not use any network connection.

The service is only active while YouTube or YouTube Music is on screen. All other apps are ignored — except for the pause list, which forces the service to idle when a sensitive app is in front.

## Privacy

The app:

- Does **not** send any data anywhere
- Does **not** require internet permission
- Does **not** collect analytics
- Only reads screen content from YouTube and YouTube Music
- Uses Android's Accessibility API, which shows a system warning when enabled — this is expected and standard for any app of this type

## Installation

This app is not on the Play Store. You install it manually:

1. Download the APK from [Releases](https://github.com/wolverinesingh1-droid/skipper-android/releases) (once available)
2. Enable "Install from unknown sources" for your browser or file manager
3. Open the APK and install
4. Open **Ad Skipper**
5. Tap **Enable Ad Skipper** → this opens Android's Accessibility settings
6. Find **Ad Skipper** in the list and toggle it on
7. On Android 13+, you may need to allow "Restricted settings" first (see below)

### Android 13+ restricted settings

Android 13 and newer block Accessibility Services for apps installed from outside the Play Store. To allow Ad Skipper:

1. Settings → Apps → **Ad Skipper**
2. Tap the **three-dot menu** in the top-right
3. Tap **Allow restricted settings**
4. Go back and enable the service

On some Samsung devices (One UI 8.5 / Android 16), the three-dot menu is missing. Use ADB instead:


## Usage

Once enabled, the service runs in the background. Nothing else to do.

**Settings:**

- **Skip timing** — how long to wait before skipping. Off skips instantly. 5/10/15 seconds lets the creator get partial ad revenue.
- **Pause List** — packages of apps that should pause the service. Add your bank, password manager, etc.

**Skip Log** — shows a running history of every skip. Useful for debugging if ads stop being skipped after a YouTube update.

## Known limitations

- **Non-skippable ads** cannot be skipped. If there is no Skip button on screen, there is nothing to tap. This is a fundamental limitation of the Accessibility approach.
- **Fast-forward** of ads is not possible. Accessibility Services cannot control video playback speed in native apps.
- **Muting ads** was attempted but removed. Samsung's audio stack blocks programmatic volume restore, leaving the phone stuck on mute. Not worth the side effects.
- **Icon may not update** on some launchers until the app is uninstalled and reinstalled.

## Building from source

Requirements:

- Android Studio (Panda 2 / 2025.3.2 or newer)
- Android SDK, API 27 or higher (tested on API 36)
- A device running Android 8.1 or newer

Steps:

Then open the project in Android Studio and click **Run**.

## Project structure

## License

Not yet specified.

## Contributing

Not accepting contributions at this time.
