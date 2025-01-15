package com.example.speakeasy_app;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onButton1Click(View view)
    {
        // Code to handle button 1 click
        Toast.makeText(this, "Button 1 clicked", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, RedButton.class);

        // Start the new activity
        startActivity(intent);
    }

    public void onButton2Click(View view)
    {
        // Code to handle button 2 click
        Toast.makeText(this, "Button 2 clicked", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, BlueButton.class);

        // Start the new activity
        startActivity(intent);
    }

    public void onButton3Click(View view)
    {
        // Code to handle button 3 click
        Toast.makeText(this, "Button 3 clicked", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, GreenButton.class);

        // Start the new activity
        startActivity(intent);
    }

    public void onButton4Click(View view)
    {
        // Code to handle button 4 click
        Toast.makeText(this, "Button 4 clicked", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, YellowButton.class);

        // Start the new activity
        startActivity(intent);
    }

    public void onButton5Click(View view)
    {
        // Code to handle button 5 click
        Toast.makeText(this, "Button 5 clicked", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, WhiteButton.class);

        // Start the new activity
        startActivity(intent);
    }


}