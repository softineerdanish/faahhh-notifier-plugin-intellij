# Panic Plugin - Viral IntelliJ/Android Studio Plugin

A hilarious IntelliJ/Android Studio plugin that brings theatrical reactions to your build failures and successes!

## 🎬 Features

### 🔴 Panic Mode (Rage Mode)
When a build or run configuration fails, the IDE responds with:
- **🌊 Pulsing Red Overlay** - Sine-wave animated red screen covering your editor
- **🎵 Scream Sound** - A digital wail of despair (`faaah.mp3`)

### 🟢 Redemption Mode (Success Mode)  
When ANY build or run configuration succeeds after a prior failure:
- **💚 GTA "Mission Passed" Overlay** - Green tint with styled "MISSION PASSED" and "RESPECT +" text
- **🎵 Mission Passed Audio** - Iconic GTA mission completion theme

## 🎯 What Triggers Panic/Redemption?

| Trigger | Status |
|---------|--------|
| **IDE Builds** (Build menu, Gradle panel) | ✅ Supported |
| **Run Configurations** | ✅ Supported |

## 💾 State Persistence

The `FaahStateService` persists your panic state across IDE sessions using IntelliJ's `PersistentStateComponent`. Your panic state is remembered even after restart!

## 📋 Requirements

- **IDE**: IntelliJ IDEA / Android Studio 2024.1 or later
- **Java**: Java 17+
- **Gradle**: 8.5+
- **Audio Files**: Bundled with plugin (no external dependencies)

## 🔧 Building

```bash
./gradlew build
```

Output: `build/distributions/Panic Plugin-1.0.0.zip`

## 🚀 Running in Sandbox IDE

```bash
./gradlew runIde
```

## 📦 Installation

1. Build: `./gradlew build`
2. IDE → Preferences → Plugins → ⚙️ → Install Plugin from Disk
3. Select `build/distributions/Panic Plugin-1.0.0.zip`
4. Restart IDE

## 🎮 Testing

### Quick Test
1. Press `Cmd+Shift+A` (or `Ctrl+Shift+A` on Windows/Linux)
2. Search "Test Panic" → Enter
3. Should hear **SCREAM audio** 🔊
4. Search "Test Redemption" → Enter  
5. Should hear **MISSION PASSED audio** 🎵

### Real Build Test
1. Go to **Build** menu → **Build Project**
2. If it fails: Hear **SCREAM**, see red overlay 🔴
3. Fix and run successful build: Hear **MISSION PASSED**, see green overlay 🟢

## 📝 Architecture

- **FaahStateService**: Persistent state management
- **ExecutionListener**: Catches IDE build/run executions
- **PanicOverlayManager**: Renders red pulsing overlay + plays scream
- **RedemptionManager**: Renders GTA overlay + plays mission passed
- **AudioPlayer**: Plays bundled MP3 files with fallback to `afplay`

## 📄 License

MIT - Make your builds epic!

## 🙏 Credits

- Scream audio: Classic internet meme
- Mission Passed audio: GTA series (fan usage)
- Built with ❤️ for developers who need theatrical feedback
