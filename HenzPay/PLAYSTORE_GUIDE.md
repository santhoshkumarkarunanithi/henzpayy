# Publishing HenzPay Pay Alerts

1. Install Android Studio, open this folder, let Gradle sync (it creates the wrapper).
2. Add a real app icon (Right-click res > New > Image Asset) and replace the placeholder in AndroidManifest.xml.
3. Test on a real phone: make a small UPI payment to yourself and check each app's notification parses. Adjust PaymentParser regexes if wording differs.
4. Build > Generate Signed App Bundle (.aab). Create a keystore and BACK IT UP. Enable Play App Signing.
5. Create a Google Play Developer account (one-time USD 25) at play.google.com/console. Individual accounts created after Nov 2023 must run a closed test with 12+ testers for 14 days before production.
6. Store listing: name, short/full description, 512x512 icon, 1024x500 feature graphic, 2+ phone screenshots.
7. Policy items (important, rejection is common here):
   - Host a Privacy Policy URL stating you read payment notifications on-device only and do not upload/share them.
   - Data safety form: declare financial info processed on device, not collected/shared.
   - Notification access is a sensitive permission: the in-app "prominent disclosure" screen (already included) must show before the user is sent to settings.
   - Do not use "PhonePe/Google Pay/Paytm/BHIM" logos or imply affiliation. Mention names only descriptively.
   - Add: "Not affiliated with PhonePe, Google, Paytm or NPCI."
8. Submit for review (usually a few days).
