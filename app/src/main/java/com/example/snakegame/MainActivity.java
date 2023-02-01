package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ajoute le fragment HomeFragment
        getSupportFragmentManager().beginTransaction().add(R.id.container, new HomeFragment()).commit();
    }
}