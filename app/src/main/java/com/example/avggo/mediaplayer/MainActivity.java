package com.example.avggo.mediaplayer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    Button setupServerButton, connectServerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File newFolder = new File (Environment.getExternalStorageDirectory() + "/Pictures/MediaPlayer");

        if (!newFolder.exists()) { // move sample pictures to local storage
            newFolder.mkdir(); // Create new folder under Pictures folder for local App usage
        }

        AssetManager assetManager = getAssets();

        String[] files = null;

        try {
            files = assetManager.list("");
        } catch (IOException e) {

        }

        if (files != null) for (String filename : files) {
            InputStream IStream = null;
            OutputStream OStream = null;

            try {
                IStream = assetManager.open(filename);
                File OFile = new File(Environment.getExternalStorageDirectory() + "/Pictures/MediaPlayer/", filename);

                OStream = new FileOutputStream(OFile);
                byte[] buffer = new byte[1024];

                int read;

                while((read = IStream.read(buffer)) != -1){
                    OStream.write(buffer, 0, read);
                }

            } catch (IOException ex) {

            }

            finally {
                if (IStream != null) {
                    try {
                        IStream.close();
                    } catch (IOException ex) {

                    }
                }
                if (OStream != null) {
                    try {
                        OStream.close();
                    } catch (IOException ex) {

                    }
                }
            }
        }

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
