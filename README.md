# WLAN AI Toolbox

A sophisticated and modern Android application designed for WLAN professionals, engineers, and enthusiasts to diagnose local network properties, execute standard RF calculations, resolve network domains, and learn about wireless technologies.

Featuring the **Sophisticated Dark** design theme, this toolbox combines top-tier functionality with a beautiful, high-contrast Material 3 interface.

## 📱 Key Features

1. **Live Signal Monitor (Diagnostic Dashboard)**:
   - Live network strength monitoring.
   - Built-in metrics like Signal-to-Noise Ratio (SNR), Noise Floor, and expected data rates.
   - Intelligent AI Assistant built to analyze signal quality and suggest optimizations.

2. **RF & WLAN Calculators**:
   - **FSPL (Free Space Path Loss)**: Calculate path loss across different distances and frequencies (2.4 GHz, 5 GHz, 6 GHz).
   - **Link Budget**: Easily calculate receiver power based on transmitter power, gains, and losses.
   - **Wavelength**: Quickly find the precise wavelength of physical radio frequencies.

3. **Field Reference**:
   - Detailed channel mappings for **2.4 GHz**, **5 GHz**, and **6 GHz** bands.
   - Interactive channel grid to visualize overlapping frequencies and channel widths.

4. **Network Utilities**:
   - **Ping utility**: Test remote server latency directly from your phone.
   - **DNS Lookup**: Query A, AAAA, MX, and TXT records of any domain.

5. **Learning Center**:
   - Comprehensive documentation on key wireless concepts, covering modulation types (QAM), standards (Wi-Fi 6/7), MIMO, and beamforming.

---

## 🛠️ Build and Setup Instructions

### Prerequisites
- **Android Studio** (Koala or newer recommended)
- **Android SDK 36** (target API level 36)
- **JDK 17**

### Easy Setup
Unlike standard templates, **no manual modification of gradle files is required to run this project**. The build system is fully automated:
1. **Clone the Repository**:
   ```bash
   git clone <your-repository-url>
   ```
2. **Open in Android Studio**:
   - Go to `File -> Open` and select the root directory of the project.
   - Let Gradle sync and download all dependencies automatically.
3. **Run on Device / Emulator**:
   - Select your target device and click **Run** (`Shift + F10`).
   - The app's signing configuration is fully dynamic. If running locally, it gracefully falls back to the standard Android debug keystore automatically without crashing or throwing errors.

---

## 🎨 Sophisticated Dark Theme Design

The UI has been customized to deliver a premium dark mode experience:
- **Primary Canvas**: Rich dark slate background (`#1C1B1F`) minimizing eye strain.
- **Card Container**: Warmer textured slate containers (`#2B2930`) with fine-lined borders (`#49454F`).
- **Interactive Accents**: Soft lavender purple (`#D0BCFF`) and pastel purple (`#EADDFF`) elements to make primary buttons and key status tags stand out elegantly.
