package com.example.speakeasy_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WhiteButton extends AppCompatActivity {

    private static final String TAG = "WhiteButtonActivity"; // Tag for logs
    private static final String PREFS_NAME = "WhiteButtonPrefs";
    private static final String KEY_SAVED_TEXT = "savedText";
    private static final String KEY_SAVED_WAV_PATH = "savedWavPath";

    private EditText editTextTextWhite;
    private TextView textViewWhitePrompt;
    private SharedPreferences sharedPreferences;
    private TextToSpeech textToSpeech;
    private String savedWavPath;

    private static final String WAV_FILE_NAME = "5.wav";
    private static final String ESP32_UPLOAD_URL = "http://192.168.4.1/upload";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Enable Edge-to-Edge fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        setContentView(R.layout.activity_white_button);

        Log.d(TAG, "Initializing WhiteButton Activity");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editTextTextWhite = findViewById(R.id.editTextTextWhite);
        textViewWhitePrompt = findViewById(R.id.textViewWhitePrompt);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
                Log.d(TAG, "TextToSpeech initialized successfully");
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });

        // Load saved text and WAV path
        String savedText = sharedPreferences.getString(KEY_SAVED_TEXT, "Default prompt for White Button.");
        textViewWhitePrompt.setText(savedText);
        savedWavPath = sharedPreferences.getString(KEY_SAVED_WAV_PATH, null);

        if (savedWavPath != null) {
            Log.d(TAG, "Loaded saved WAV path: " + savedWavPath);
        } else {
            Log.d(TAG, "No saved WAV path found");
        }
    }

    public void updateTextView(View view) {
        // Get the input text from the EditText
        String newText = editTextTextWhite.getText().toString();

        if (newText.isEmpty()) {
            Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the input text to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SAVED_TEXT, newText);
        editor.apply(); // Save the data asynchronously

        // Update the TextView
        textViewWhitePrompt.setText(newText);
        Log.d(TAG, "Saved new text: " + newText);

        // Optional feedback
        Toast.makeText(this, "Text saved successfully!", Toast.LENGTH_SHORT).show();

        // Generate WAV file with updated text
        createWavFromText(newText);
    }

    // Create a WAV file from the entered text
    private void createWavFromText(String text) {
        File outputDir = getExternalFilesDir(null);
        File wavFile = new File(outputDir, WAV_FILE_NAME);
        savedWavPath = wavFile.getAbsolutePath();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SAVED_WAV_PATH, savedWavPath);
        editor.apply();

        Log.d(TAG, "Generating WAV file at path: " + savedWavPath);

        textToSpeech.synthesizeToFile(text, null, wavFile, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        Toast.makeText(this, "WAV file creation started.", Toast.LENGTH_SHORT).show();
    }

    // Play the WAV file
    public void playMP3(View view) {
        if (savedWavPath == null || savedWavPath.isEmpty()) {
            Toast.makeText(this, "No WAV file available to play", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "No WAV file available for playback");
            return;
        }

        File wavFile = new File(savedWavPath);
        if (!wavFile.exists()) {
            Toast.makeText(this, "WAV file not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "WAV file not found at path: " + savedWavPath);
            return;
        }

        // Use MediaPlayer to play the WAV file
        try {
            android.media.MediaPlayer mediaPlayer = new android.media.MediaPlayer();
            mediaPlayer.setDataSource(savedWavPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing WAV file", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Playing WAV file: " + savedWavPath);

            mediaPlayer.setOnCompletionListener(mp -> {
                Toast.makeText(this, "Playback completed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Playback completed for WAV file");
                mp.release();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error playing WAV file", e);
            Toast.makeText(this, "Error playing WAV file", Toast.LENGTH_SHORT).show();
        }
    }

    // Upload WAV file to ESP32
    public void uploadWavToESP32(View view) {
        if (savedWavPath == null || savedWavPath.isEmpty()) {
            Toast.makeText(this, "No WAV file available to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        File wavFile = new File(savedWavPath);
        if (!wavFile.exists()) {
            Toast.makeText(this, "WAV file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String boundary = UUID.randomUUID().toString();
                HttpURLConnection connection = (HttpURLConnection) new URL(ESP32_UPLOAD_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream outputStream = connection.getOutputStream();
                     FileInputStream fileInputStream = new FileInputStream(wavFile)) {

                    outputStream.write(("--" + boundary + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"5.wav\"\r\n").getBytes());
                    outputStream.write(("Content-Type: audio/wav\r\n\r\n").getBytes());

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
                    outputStream.flush();
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "HTTP Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(WhiteButton.this, "File uploaded successfully", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(WhiteButton.this, "Upload failed: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload error", e);
                runOnUiThread(() -> Toast.makeText(WhiteButton.this, "Upload error", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            Log.d(TAG, "TextToSpeech stopped and shut down");
        }
        super.onDestroy();
    }

    // Go back button functionality
    public void goBack(View view) {
        Log.d(TAG, "Returning to previous screen");
        finish();
    }
}
