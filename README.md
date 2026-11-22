# MyGardens Android client

This branch contains the Android Studio client only. The Spring Boot backend is maintained on the `server` branch.

## Open and build

Open this repository folder in Android Studio while the `client` branch is checked out. The project uses Android Gradle
Plugin 8.7.3, Gradle 8.9, and JDK 17. Android Studio can build the app with **Build > Make Project** or with
`gradlew.bat assembleDebug` on Windows.

## Configure the API URL

The emulator uses `http://10.0.2.2:8080/` by default. For a physical Android device, add this to the ignored
`local.properties` file in the repository root, replacing the placeholder with the computer's current LAN address:

```properties
gardenersApiBaseUrl=http://<computer-LAN-IP>:8080/
```

Keep the phone and computer on the same network. Rebuild and reinstall the app after changing the URL. Do not commit
`local.properties`; it contains machine-specific settings.
