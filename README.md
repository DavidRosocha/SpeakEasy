
# SpeakEasy Communication Device

## Overview
The **SpeakEasy Communication Device** is a custom-built solution designed to assist non-verbal individuals with cerebral palsy in communicating effectively. It leverages tactile inputs, Bluetooth, Wi-Fi, and text-to-speech technology to provide a seamless communication experience. This device empowers users to interact with their peers, significantly enhancing social interaction and independence.

---

## Features
- **Custom Circuit Board**
  - Designed with tactile push buttons for intuitive input.
  - Integrated with an ESP32 microcontroller for Bluetooth and Wi-Fi connectivity.

- **Bluetooth and Wi-Fi Communication**
  - Real-time communication with the Android app via Bluetooth.
  - Wi-Fi-enabled WAV file transfer for audio playback on the device.

- **SpeakEasy Android App**
  - Developed to create and update text-to-speech prompts easily.
  - Facilitates wireless WAV file export to the device.
  - Provides a user-friendly interface for seamless operation.

- **Audio Playback System**
  - Plays WAV files through a speaker mounted on the wheelchair.
  - Ensures clear and reliable text-to-speech output.

---

## Technical Details
- **Hardware Components**
  - ESP32 Arduino Board for processing and connectivity.
  - Custom-designed PCB with tactile push buttons for input.
  - Speaker system for audio output.

- **Software Development**
  - Android app coded in Java, supporting text-to-speech and WAV file export.
  - ESP32 firmware written in C++ for managing inputs, Bluetooth/Wi-Fi communication, and audio playback.

- **Technologies Used**
  - **Languages:** C++ (ESP32 firmware), Java (Android app).
  - **Tools:** Android Studio, KiCad (PCB design).
  - **Protocols:** Bluetooth, Wi-Fi.

---

## Impact
The SpeakEasy device increased the client’s social interactions by **140%**, enabling effective and intuitive communication in a workplace setting. The project demonstrates the potential of assistive technology to enhance quality of life through innovative hardware-software integration.

---

## Installation & Usage
1. **Hardware Setup**
   - Assemble the PCB and connect it to the ESP32 microcontroller.
   - Mount the speaker to the client’s wheelchair.

2. **Software Installation**
   - Install the SpeakEasy Android app (APK available in this repository).
   - Flash the ESP32 firmware using the provided files in the `/firmware` directory.

3. **Usage**
   - Use the Android app to create and update text-to-speech prompts.
   - Prompts are wirelessly sent to the ESP32, which stores them and plays them through the speaker.

---

## Future Enhancements
- Expand tactile input support for improved accessibility.
- Include multilingual support and additional voice options.
- Refine the Android app's UI/UX for greater ease of use.

---

## Acknowledgments
This project was created in collaboration with a caretaker and client at a caretaking facility. Their insights and feedback were integral to designing a solution that meets their specific needs.
