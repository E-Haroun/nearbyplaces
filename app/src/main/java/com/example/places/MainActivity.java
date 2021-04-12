package com.example.places;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnNearby, btnCities, btnRoute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNearby = findViewById(R.id.btnNearby);
        btnCities = findViewById(R.id.btnCities);
        btnRoute = findViewById(R.id.btnRoute);

        btnNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent relation = new Intent(getApplicationContext(), Nearby.class);
                startActivity(relation);
            }
        });

        btnCities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent relation = new Intent(getApplicationContext(), Cities.class);
                startActivity(relation);
            }
        });

        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent relation = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(relation);
            }
        });

    }
}