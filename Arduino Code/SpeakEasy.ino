#include <SD.h>
#include <SPI.h>
#include <driver/i2s.h>
#include <WiFi.h>
#include <WebServer.h>

// Wi-Fi settings
const char* ssid = "ESP32_Audio";
const char* password = "12345678";

// SD card settings
#define SD_CS_PIN 5 // Chip Select pin for SD card module

// I2S settings
#define I2S_BCLK 26    // Bit Clock
#define I2S_LRC 25     // Left/Right Clock (Word Select)
#define I2S_DOUT 22    // Data Out

// WAV header structure
typedef struct {
    char riff_header[4];   // Contains "RIFF"
    int wav_size;          // File size - 8
    char wave_header[4];   // Contains "WAVE"
    char fmt_header[4];    // Contains "fmt "
    int fmt_chunk_size;    // Should be 16 for PCM
    short audio_format;    // Should be 1 for PCM
    short num_channels;    // Number of audio channels
    int sample_rate;       // Sampling rate
    int byte_rate;         // Bytes per second
    short sample_alignment;// NumChannels * BytesPerSample
    short bit_depth;       // Bits per sample
    char data_header[4];   // Contains "data"
    int data_bytes;        // Number of data bytes
} wav_header_t;

// Globals for playback
File wavFile;
wav_header_t wavHeader;
int16_t audioBuffer[8192];
int audioChannels = 0;
int sampleRate = 44100; // Default sample rate
bool i2sConfigured = false;
float volumeMultiplier = 2; // Amplify audio

// Button pins
const int buttonPins[] = {4, 13, 14, 15, 12}; // Added button on pin 12
const int numButtons = 5;
const char* fileNames[] = {"1.wav", "2.wav", "3.wav", "4.wav", "5.wav"}; // Added file "5.wav"

// Web server
WebServer server(80);

// Button press tracking
bool buttonState[numButtons] = {false};
unsigned long lastDebounceTime[numButtons] = {0};
const unsigned long debounceDelay = 200;
bool playbackInProgress = false;

// Initialize SD card
bool initializeSDCard() {
    if (!SD.begin(SD_CS_PIN)) {
        Serial.println("Failed to initialize SD card!");
        return false;
    }
    Serial.println("SD card initialized successfully.");
    return true;
}

// Configure Wi-Fi
void configureWiFi() {
    WiFi.softAP(ssid, password);
    Serial.printf("Wi-Fi AP started\nSSID: %s, Password: %s\n", ssid, password);
    Serial.println(WiFi.softAPIP());
}

// Handle file upload
void handleFileUpload() {
    HTTPUpload& upload = server.upload();
    static File file;
    static String currentFilename = "";

    switch (upload.status) {
        case UPLOAD_FILE_START: {
            String path = "/" + String(upload.filename); // Use the filename provided by the client
            Serial.printf("Starting upload for: %s\n", path.c_str());

            if (file) file.close();
            if (SD.exists(path)) SD.remove(path);

            file = SD.open(path, FILE_WRITE);
            if (!file) {
                Serial.printf("Failed to open file for writing: %s\n", path.c_str());
                server.send(500, "text/plain", "Failed to open file");
                return;
            }

            currentFilename = path;
            break;
        }
        case UPLOAD_FILE_WRITE: {
            if (!file) return;
            size_t written = file.write(upload.buf, upload.currentSize);
            if (written != upload.currentSize) {
                Serial.printf("Write error! Expected %d, wrote %d bytes\n", upload.currentSize, written);
            }
            break;
        }
        case UPLOAD_FILE_END: {
            if (file) {
                file.close();
                Serial.printf("Upload complete: %s\n", currentFilename.c_str());
                server.send(200, "text/plain", "Upload successful");
            }
            break;
        }
        case UPLOAD_FILE_ABORTED: {
            if (file) {
                file.close();
                if (!currentFilename.isEmpty()) SD.remove(currentFilename);
                Serial.println("Upload aborted");
            }
            break;
        }
    }
}

// Handle web root
void handleRoot() {
    server.send(200, "text/plain", "ESP32 Audio Server Ready");
    Serial.println("Client connected to root endpoint");
}

// Read WAV header
bool readWAVHeader(const char* filename) {
    String filePath = String("/") + filename;
    Serial.printf("Opening file: %s\n", filePath.c_str());

    wavFile = SD.open(filePath.c_str(), FILE_READ);
    if (!wavFile) {
        Serial.printf("Failed to open file: %s\n", filePath.c_str());
        return false;
    }

    if (wavFile.read((uint8_t*)&wavHeader, sizeof(wav_header_t)) != sizeof(wav_header_t)) {
        Serial.println("Failed to read WAV header!");
        wavFile.close();
        return false;
    }

    if (strncmp(wavHeader.riff_header, "RIFF", 4) != 0 ||
        strncmp(wavHeader.wave_header, "WAVE", 4) != 0 ||
        wavHeader.audio_format != 1 ||
        wavHeader.bit_depth != 16) {
        Serial.println("Unsupported WAV format!");
        wavFile.close();
        return false;
    }

    audioChannels = wavHeader.num_channels;
    sampleRate = wavHeader.sample_rate;

    Serial.printf("Loaded WAV file: %s\n", filePath.c_str());
    Serial.printf("Channels: %d, Sample Rate: %d Hz, Bit Depth: %d bits\n",
                  audioChannels, sampleRate, wavHeader.bit_depth);
    return true;
}

// Configure I2S for playback
void configureI2S() {
    if (i2sConfigured) return;

    esp_err_t uninstallResult = i2s_driver_uninstall(I2S_NUM_0);
    if (uninstallResult != ESP_OK && uninstallResult != ESP_ERR_INVALID_STATE) {
        Serial.println("I2S driver uninstall failed.");
    }

    i2s_config_t i2sConfig = {
        .mode = (i2s_mode_t)(I2S_MODE_MASTER | I2S_MODE_TX),
        .sample_rate = sampleRate,
        .bits_per_sample = I2S_BITS_PER_SAMPLE_16BIT,
        .channel_format = audioChannels == 2 ? I2S_CHANNEL_FMT_RIGHT_LEFT : I2S_CHANNEL_FMT_ONLY_LEFT,
        .communication_format = I2S_COMM_FORMAT_I2S,
        .intr_alloc_flags = ESP_INTR_FLAG_LEVEL1,
        .dma_buf_count = 16,
        .dma_buf_len = 512
    };

    i2s_pin_config_t pinConfig = {
        .bck_io_num = I2S_BCLK,
        .ws_io_num = I2S_LRC,
        .data_out_num = I2S_DOUT,
        .data_in_num = I2S_PIN_NO_CHANGE
    };

    if (i2s_driver_install(I2S_NUM_0, &i2sConfig, 0, NULL) != ESP_OK) {
        Serial.println("I2S driver install failed.");
        return;
    }

    if (i2s_set_pin(I2S_NUM_0, &pinConfig) != ESP_OK) {
        Serial.println("I2S pin configuration failed.");
        return;
    }

    i2s_set_clk(I2S_NUM_0, sampleRate, I2S_BITS_PER_SAMPLE_16BIT,
                audioChannels == 2 ? I2S_CHANNEL_STEREO : I2S_CHANNEL_MONO);
    i2sConfigured = true;
    Serial.println("I2S configured.");
}

// Play WAV file
void playWAV(const char* filename) {
    if (!readWAVHeader(filename)) return;

    configureI2S();

    playbackInProgress = true;
    Serial.println("Starting playback...");

    while (wavFile.available()) {
        int bytesRead = wavFile.read((uint8_t*)audioBuffer, sizeof(audioBuffer));
        if (bytesRead <= 0) break;

        for (int i = 0; i < bytesRead / 2; i++) {
            audioBuffer[i] = constrain((int16_t)(audioBuffer[i] * volumeMultiplier), -32768, 32767);
        }

        size_t bytesWritten = 0;
        esp_err_t result = i2s_write(I2S_NUM_0, audioBuffer, bytesRead, &bytesWritten, portMAX_DELAY);
        if (result != ESP_OK || bytesWritten != bytesRead) {
            Serial.println("I2S write failed!");
            break;
        }
    }

    if (wavFile) wavFile.close();
    Serial.println("Playback finished.");
    playbackInProgress = false;
}

void setup() {
    Serial.begin(115200);

    if (!initializeSDCard()) {
        Serial.println("SD card initialization failed!");
        while (true);
    }

    configureWiFi();

    server.on("/", handleRoot);
    server.on("/upload", HTTP_POST, []() { server.send(200, "text/plain", "Upload complete"); }, handleFileUpload);
    server.begin();
    Serial.println("Web server started.");

    for (int i = 0; i < numButtons; i++) {
        pinMode(buttonPins[i], INPUT_PULLUP);
    }

    Serial.println("Setup complete. Press a button to play audio.");
}

void loop() {
    server.handleClient();

    if (playbackInProgress) return;

    for (int i = 0; i < numButtons; i++) {
        int state = digitalRead(buttonPins[i]);
        if (state == LOW && millis() - lastDebounceTime[i] > debounceDelay && !buttonState[i]) {
            buttonState[i] = true;
            lastDebounceTime[i] = millis();
            playWAV(fileNames[i]);
        }
        if (state == HIGH) buttonState[i] = false;
    }
}
