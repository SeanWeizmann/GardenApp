# Gardeners backend

Spring Boot REST backend for the MyGardens Android app. It stores customers and garden jobs in MySQL. MySQL Server must
be installed and running; MySQL Workbench is used to view the same server and schema. The defaults connect to
`localhost:3306`, schema `gardenapp`, user `root`. Set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`
to match your MySQL Server login, either in the environment or in the ignored local `secrets.properties` file.
Hibernate creates missing tables and preserves existing data.

## Run locally

From this folder, run `mvn spring-boot:run` (or `mvnw.cmd spring-boot:run`). Keep the backend running while using the
Android app. It listens on port 8080 on all network interfaces. An Android emulator reaches the host at
`http://10.0.2.2:8080/`. A physical Android device needs the computer's LAN address in the app base URL, with both
devices on the same network and Windows Firewall allowing inbound TCP port 8080.

For a physical-device debug build, set `gardenersApiBaseUrl=http://<computer-LAN-IP>:8080/` in
`client/local.properties`. That file is ignored by Git. Rebuild and reinstall the app after changing it.

## Repository layout and branches

- The Spring Boot backend is in the repository root.
- The Android Studio project is in `client/`. Open that folder in Android Studio; Git will use the repository root.
- `server` and `client` branches both start with the complete project. Commit backend changes on `server` and Android
  changes on `client`, then merge or cherry-pick changes when you want to combine them.

The Android app connects to the Spring API, which reads and writes the `customers` and `garden_tasks` tables. In
Workbench, connect to that same MySQL Server, open the `gardenapp` schema, and refresh the table list after the backend
starts. Android-created missions are saved as rows in `garden_tasks`.

## API

- `GET /api/customers?q=...` (also available as `GET /api/clients` for client dropdowns), `GET /api/customers/{id}`, `POST /api/customers`, `PUT /api/customers/{id}`
  , `DELETE /api/customers/{id}`
- `GET /api/tasks?date=YYYY-MM-DD&customerId=...`, `GET /api/tasks/{id}`, `POST /api/tasks`, `PUT /api/tasks/{id}`. Task create/update accepts `clientId` (or legacy `customerId`) and links it to the authenticated owner's client record.
  , `PATCH /api/tasks/{id}/completion?completed=true`, `DELETE /api/tasks/{id}`

Tasks accept ISO dates (`yyyy-MM-dd`) and 24-hour times (`HH:mm`). Scheduling rejects overlapping jobs for the same
assigned gardener. Customers with linked jobs cannot be deleted until those jobs are removed or reassigned. Validation
and API errors are returned as JSON.

## Google sign-in and Calendar

The Android reference app starts with Google sign-in and requests a server authorization code with the
`calendar.events` scope. The backend redeems that one-time code, verifies the Google user through OpenID Connect,
stores the Google account and OAuth tokens in `google_accounts`, and returns an opaque 30-day session token. The
session token is stored hashed on the server. Google client secrets must only be configured on the backend.

Configure Google Cloud before running sign-in:

1. Select or create a Google Cloud project, then enable **Google Calendar API**.
2. Configure the OAuth consent screen / Google Auth Platform branding, audience, and test users as appropriate.
3. Create an OAuth 2.0 **Web application** client ID for the backend. Keep its client secret in the server environment.
4. Create an OAuth 2.0 **Android** client ID for package `com.example.mygardens` and the signing certificate SHA-1
   used by the installed build. The Android app uses the Web client ID to request its server authorization code.
5. Set the Android Gradle property `googleWebClientId` to the Web client ID. This checkout has it in the local
   Android `gradle.properties`; use `%USERPROFILE%\.gradle\gradle.properties` for your own builds instead. For a
   deployed backend, set `gardenersApiBaseUrl=https://<API_HOST>/` there too.
6. Set `GOOGLE_OAUTH_CLIENT_ID` and `GOOGLE_OAUTH_CLIENT_SECRET` in the backend environment. For this local checkout, the
   backend imports the ignored `secrets.properties` file; the client secret is kept out of source control. The Android server-auth-code flow
   redeems with an empty `redirect_uri`; no web redirect URI is used. Restart the backend after configuration.

API endpoints:

- `POST /api/auth/google/exchange` with `{"code":"<server authorization code>"}` returns the initialized app
  session and verified Google profile. The client sends the code over HTTPS in production; it never receives the
  Google client secret. All other `/api/**` endpoints require that session in a Bearer header.
- `POST /api/calendar/events` accepts a Google Calendar event resource and requires
  `Authorization: Bearer <sessionToken>`. It creates an event on the signed-in user's primary calendar and refreshes
  the access token when Google reports it expired.

The repository's Android base URL and cleartext traffic setting are for local development only. Use HTTPS for the
backend before sending authorization codes, sessions, or customer data over a network. OAuth tokens are stored in
the configured application database; protect that database and its backups accordingly. Customer and task data
remain shared across authenticated accounts; add per-account ownership before using multiple gardener accounts.
