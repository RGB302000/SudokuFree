# Sudoku Free — Production Checklist

## AdMob
The project currently uses Google's official TEST ad unit IDs.
Do NOT publish these as a monetized production configuration.

Replace these constants in `MainActivity.kt`:
- TEST_BANNER
- TEST_INTERSTITIAL
- TEST_REWARDED

Create the corresponding ad units in your own AdMob account and use those IDs.

## Consent / Privacy
Before release, integrate Google's User Messaging Platform (UMP) and show consent where required.
Add a Privacy Policy URL to the Play Console and inside the app.

## Release
In Android Studio:
1. Build > Generate Signed Bundle / APK
2. Choose Android App Bundle (AAB)
3. Create a new upload key if you don't already have one.
4. Build the `release` bundle.
5. Keep the keystore and passwords backed up securely.

## Play Console
Prepare:
- App name
- Short description
- Full description
- App icon
- Feature graphic
- Phone screenshots
- Content rating
- Data Safety form
- Privacy Policy URL
- Ads declaration

## Important
Test the game on a real Android phone before uploading.
Do not click your own live ads. During development use only test ads.
