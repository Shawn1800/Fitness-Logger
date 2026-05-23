# HeavyLifts 🏋️‍♂️

HeavyLifts is a modern, comprehensive fitness and workout tracking Android application. Designed for athletes and fitness enthusiasts, it provides a seamless experience for tracking workouts, monitoring daily activity, and competing with a community.

## 🌟 Features

- **🔐 Secure Authentication**: Integrated with Supabase and Google Auth (Credential Manager) for a frictionless sign-in/sign-up experience.
- **📊 Workout Tracking**: Log your exercises, sets, and reps with ease.
- **👣 Real-time Step Monitoring**: Seamlessly syncs with Health Connect and Google Fit to track your daily steps and activity.
- **🏆 Community Leaderboard**: Compete with other users and climb the ranks.
- **🎯 Personalized Onboarding**: Tailored experience for new users to set their fitness goals.
- **📱 Modern UI**: Built entirely with Jetpack Compose following Material 3 design principles.
- **🔄 Dynamic Data Syncing**: Real-time updates and synchronization with Supabase.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Backend**: [Supabase](https://supabase.com/) (Auth, Postgrest)
- **Health Integration**: [Health Connect API](https://developer.android.com/health-and-fitness/guides/health-connect), Google Fit
- **Navigation**: Navigation3
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Architecture**: MVVM with Manual Dependency Injection (AppContainer)
- **Concurrency**: Coroutines & Flow
- **Data Serialization**: Kotlinx Serialization

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug | 2024.2.1 or newer
- JDK 17+
- A Supabase Project
- Google Cloud Project (for Google Auth)

### Setup

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/HeavyLifts.git
    cd HeavyLifts
    ```

2.  **Configure Environment Variables**:
    Create a `local.properties` file in the root directory (if it doesn't exist) and add your Supabase and Google credentials:
    ```properties
    SUPABASE_URL=your_supabase_url
    SUPABASE_PUBLISHABLE_KEY=your_supabase_anon_key
    GOOGLE_CLIENT_ID=your_google_web_client_id
    ```

3.  **Supabase Setup**:
    - Set up your tables in Supabase (Users, Workouts, Steps, etc.).
    - Configure Authentication providers (Email, Google).

4.  **Google Auth Setup**:
    - Configure your SHA-1 fingerprint in the Google Cloud Console.
    - Add the `google-services.json` to the `app/` directory.

5.  **Build and Run**:
    Open the project in Android Studio and run it on an emulator or physical device (API 26+).

## 📂 Project Structure

```text
app/src/main/java/com/ghostbug/heavyliftsapp/
├── data/          # Data layer (Repositories, Remote/Local sources)
├── di/            # Dependency Injection (AppContainer)
├── navigation/    # Navigation logic (MainNavigation)
├── screens/       # UI Screens (Home, Profile, Workouts, etc.)
└── ui/            # Theme and common UI components
```

## 🛠 Troubleshooting

For common issues and setup guides, refer to the following documentation:

- [Google Auth Troubleshooting](GOOGLE_AUTH_TROUBLESHOOTING.md)
- [Database Troubleshooting](DATABASE_TROUBLESHOOTING.md)
- [Email Verification Guide](EMAIL_VERIFICATION_TROUBLESHOOTING.md)
- [Play Store Submission Checklist](PLAYSTORE_SUBMISSION_CHECKLIST.md)

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

##DOWNLOAD

Join Playstore closed testing to download the the app .This app is still in alpha version

[Cloed Test Link](https://play.google.com/apps/internaltest/4701532533483957856)
