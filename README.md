
# SpeakEasy Communication Device

## Overview
The **SpeakEasy Communication Device** is an assistive technology solution designed to enable non-verbal individuals with limited dexterity to communicate effectively. The device combines custom hardware, firmware, and an Android application to create an integrated system that converts user inputs into text-to-speech audio.

This project was specifically designed for a client with cerebral palsy, allowing them to interact using tactile push buttons, which trigger text-to-speech prompts. The device is wheelchair-mounted and uses Bluetooth and Wi-Fi for communication and WAV file transfer.

---

## What's Included
This repository contains all the necessary files to recreate the SpeakEasy Communication Device:
- **ESP32 Firmware:** Located in the `firmware/` folder, written in C++ and utilizes the `Audio.h` library to handle WAV file playback.
- **Android Application Source Code:** Found in the `android_app/` folder, written in Java using Android Studio. The app enables users to create, update, and export text-to-speech prompts wirelessly to the ESP32 device.

---

## Features
- **Custom Hardware:**
  - ESP32 microcontroller for Bluetooth and Wi-Fi communication.
  - Tactile push buttons mounted on a custom PCB for user input.
  - Speaker system for clear and reliable audio output.
  
- **Android Application:**
  - Create and edit text-to-speech prompts.
  - Export WAV files over Wi-Fi to the ESP32.
  - User-friendly interface tailored for accessibility.
  
- **Firmware:**
  - Manages input from tactile buttons.
  - Handles WAV file storage and playback through the speaker.
  - Integrates with the `Audio.h` library for efficient audio handling.

---

## Usage
1. **Hardware Setup:**
   - Assemble the PCB and connect it to the ESP32 microcontroller.
   - Attach the speaker to the wheelchair for audio playback.

2. **Software Installation:**
   - Flash the ESP32 firmware from the `firmware/` folder onto the microcontroller.
   - Install the SpeakEasy Android app from the `android_app/` folder.

3. **Operating the Device:**
   - Use the tactile buttons to trigger preloaded text-to-speech prompts.
   - Update or add new prompts via the Android app and transfer them wirelessly to the ESP32.

---

## Video Demonstration
A video demonstration of the SpeakEasy Communication Device in action can be viewed [here](https://www.youtube.com/watch?v=1a2fzGXfXE4&t=1s&ab_channel=CallanU).

---

## Future Enhancements
- Add more input options for broader accessibility.
- Expand language support and voice customization in the Android app.
- Improve audio quality and playback latency.

---

## Acknowledgments
This project was developed in collaboration with a caretaker and client at a caretaking facility. Their feedback and insights were critical in designing a device tailored to their unique needs.
