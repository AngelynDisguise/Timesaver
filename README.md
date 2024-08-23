# Timesaver ([0.0.1-alpha](https://github.com/AngelynDisguise/Timesaver/releases/0.0.1-alpha))
Optimize your productivity with automatic time tracking and effortless task switching. <br/>
Stay focused on work, get insights on your day, and achieve more.

## Table of Contents
<details>
  <summary>See Table of Contents</summary>
  
1) [Features](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#features)
2) [Screenshots](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#screenshots)
3) [Built With](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#built-with)
4) [Roadmap](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#roadmap)
5) [Gettiing Started](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#getting-started)
    - [Prerequisites](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#prerequisites)
    - [Build App via Android Studio](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#build-app-via-android-studio)
    - [Build App via CLI](https://github.com/AngelynDisguise/Timesaver/new/main?filename=README.md#build-app-via-cli)

</details>

## Features
- ⏱️ **Track your productivity**: Time your work and breaks with fast switching
- 📊 **Visualize your progress**: Watch progress bars grow across time
- 📔 **Logging in real-time**: Know exactly when and how long you did anything
- 📈 **Graphs**: Review a your time at a glance *(currently in development)*
- 🎨 **Color-code & Theming**: Color-code activities & change app theme *(currently in development)*
- 📵 **Offline**: No internet needed to use

## Screenshots

| <img src="https://github.com/user-attachments/assets/5d9ea43e-080c-4505-bc16-9e3f654b09cd"> | <img src="https://github.com/user-attachments/assets/c8498ca6-9bcd-40f0-9047-a96e73102064"> |
|-------------------------|-------------------------|
| <img src="https://github.com/user-attachments/assets/779bc54b-c421-41b7-809d-5f70edb8abdd"> | <img src="https://github.com/user-attachments/assets/e4cd6e03-ee7f-43e6-977e-2f4a7a5d6625"> |

<br/>

## Built with
- Kotlin and Jetpack Compose for best modern Android practices
- Reactive coroutines and flows for efficient app performance
- The MVVM (Model, View, ViewModel) architecture
- A single-activity architecture with navigation fragments
- A local Room (SQLite) database for data persistence
- Paging for in-memory caching and saving system resources
- Instrumented unit testing

## Roadmap
- [ ] Graphs for activity insights fragment
- [ ] Customize activity colors
- [ ] Color code the action bars in activity fragment and the list items in activity menu fragment
- [ ] Add app theme options - use prettier colors than the default Android theme
- [ ] Add search for logs fragment - when was I doing this, or what was I doing at this time?
- [ ] Note-taking for each day, include
- [ ] Include a graphs fragment with a retrospective section that has feedback?
- [ ] Integrate migration to [Timewarrior](https://timewarrior.net/docs/)

<br/>

# Getting Started

## Prerequisites
- Android Studio ([see docs for installation](https://developer.android.com/studio/install))
- Kotlin and XML
- Gradle
- A physical Android phone or an [Android Virtual Device (AVD)](https://developer.android.com/studio/run/managing-avds)

## Build App via Android Studio (see [docs](https://developer.android.com/studio/run))

### 1) Import Project
- In Android Studio, go to: **File --> New --> New Project from Version Control**
- Enter the GitHub repo URL:
```
git@github.com:AngelynDisguise/Timesaver.git
```
Then the repo will be created as a new project in Android Studio.

### 2) Run
- In the target device menu, select the device to run the app on
- Click "Run" to build and run app

## Build App via CLI (see [docs](https://developer.android.com/build/building-cmdline))

### 1) Import Project
```
git clone git@github.com:AngelynDisguise/Timesaver.git
```

### 2) Build a debug APK
- Use the Gradle wrapper to build:
```
./gradlew build
```
or
- Build via Android Studio: **Build --> Build App Bundle(s) / APK(s) --> Build APK(s)

### 3) [Connect to an Android emulator or a physical Android device](https://developer.android.com/studio/run/device)

### 4) Install the app using ```adb```
  ```
  cd ~/AndroidStudioProjects/Timesaver/app/build/outputs/apk/debug
  ```
  ```
  adb install app-debug.apk 
  ```
