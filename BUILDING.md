# Building Xintinglei Extras

Requires JDK 21 or newer. The included wrapper uses Gradle 9.2.1.

Windows:

```powershell
.\gradlew.bat build
```

Linux / macOS:

```sh
./gradlew build
```

The first build requires internet access to download Gradle and dependencies.
The installable mod is `build/libs/XintingleiExtras-1.0.0.jar`.
The `-sources.jar` file contains sources and is not an installable mod.

Models and textures are checked in under `src/main/resources`; no external
asset generator or private server workspace is needed for a normal build.

English translations are in `src/main/resources/assets/xintinglei/lang/en_us.json`.
Simplified Chinese translations are in `src/main/resources/assets/xintinglei/lang/zh_cn.json`.
