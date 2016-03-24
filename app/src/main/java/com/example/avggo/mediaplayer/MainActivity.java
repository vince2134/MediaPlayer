package com.example.avggo.mediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button setupServerButton, connectServerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File newFolder = new File (Environment.getExternalStorageDirectory() + "/Pictures/MediaPlayer");

        if (!newFolder.exists())
            newFolder.mkdir(); // Create new folder under Pictures folder for local App usage

        setupServerButton = (Button)findViewById(R.id.setupServerButton);
        connectServerBtn = (Button)findViewById(R.id.connectButton);

        setupServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), ServerActivity.class);
                startActivity(intent);
            }
        });

        connectServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), ClientConnectActivity.class);
                startActivity(intent);
            }
        });
    }
}
