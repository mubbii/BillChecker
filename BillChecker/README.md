# Bill Checker

A tiny Android app to check LESCO (electricity) and SNGPL (gas) bills without
re-typing your consumer number every time, and without any password — because
these lookups never needed one.

## How it works

1. **Setup (do this once per company):** Menu → "Setup LESCO page" (or SNGPL).
   This opens a WebView starting at the official homepage. Navigate to the
   real bill-inquiry/duplicate-bill page yourself (the exact URL/path changes
   over time and I couldn't verify it live, so you confirm it once), then tap
   the save icon in the top bar. The app remembers that exact URL from then on.
2. **Add a bill:** Tap the `+` button, pick the company, give it a nickname
   (e.g. "Home", "Shop"), and enter your consumer/reference number.
3. **Check a bill:** Tap the saved entry. It opens the saved page and tries to
   auto-fill the number into the right field. It also always copies the
   number to your clipboard, so if auto-fill guesses wrong you just paste
   (long-press the field → Paste). You still solve the captcha yourself —
   there's no way around that without risking the app breaking every time the
   captcha changes.

Everything (labels, numbers, saved page URLs) is stored only in local
SharedPreferences on your device. Nothing is sent anywhere by the app itself;
the WebView just talks to the utility's own website, same as your browser
would.

## Building the APK

You'll need Android Studio (easiest) or just the command-line SDK tools.

### Option A — Android Studio
1. Open Android Studio → "Open" → select this `BillChecker` folder.
2. Let it sync (downloads Gradle + SDK deps automatically, needs internet).
3. Build → Build Bundle(s)/APK(s) → Build APK(s).
4. APK lands in `app/build/outputs/apk/debug/app-debug.apk`. Copy it to your
   phone and install (you'll need to allow "install unknown apps" for
   whichever app you copy it with).

### Option B — command line
```
# from the BillChecker folder, with ANDROID_HOME set and an SDK installed
./gradlew assembleDebug
```
(There's no `gradlew` wrapper script included — Android Studio generates one
on first open, or run `gradle wrapper` if you have Gradle installed globally.)

## Adding more companies later

Everything about a company (display name, homepage) lives in one place:
`app/src/main/java/com/ahsan/billchecker/Company.kt`. Add a new enum entry
there (e.g. `WASA`), and it'll automatically show up in the "Add bill" spinner
and get its own "Setup" flow — no other code changes needed. I left WASA out
for now since most WASA boards don't have a working online bill-check portal
at all — worth checking your specific city's WASA site before adding it.

## If auto-fill never finds the field

Open `WebViewActivity.kt` and look at the `injectAutofill` function — it's a
plain JS snippet that scores input fields by keywords in their id/name/
placeholder. If a site's field genuinely doesn't match any of those keywords,
add a more specific keyword there, or just rely on the clipboard-paste
fallback (always works regardless).
