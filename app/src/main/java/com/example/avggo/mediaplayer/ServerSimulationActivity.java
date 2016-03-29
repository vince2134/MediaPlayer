package com.example.avggo.mediaplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ServerSimulationActivity extends AppCompatActivity {
    EditText etLossProbability;
    EditText etTimeout;
    EditText etDelay;
    RadioGroup rgVerbosity;
    Button btnSave;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_simulation);
        initializeHandlers();
    }

    protected void initializeHandlers(){
        etLossProbability = (EditText) findViewById(R.id.etLossProbability);
        etTimeout = (EditText) findViewById(R.id.etTimeout);
        etDelay = (EditText) findViewById(R.id.etDelay);
        rgVerbosity = (RadioGroup) findViewById(R.id.rg);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        RadioButton button;

        button = new RadioButton(this);
        button.setText("Level 1");
        rgVerbosity.addView(button);

        button = new RadioButton(this);
        button.setText("Level 2");
        rgVerbosity.addView(button);

        button = new RadioButton(this);
        button.setText("Level 3");
        rgVerbosity.addView(button);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
