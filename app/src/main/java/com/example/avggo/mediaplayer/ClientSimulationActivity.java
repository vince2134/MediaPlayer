package com.example.avggo.mediaplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.avggo.mediaplayer.singleton.SingletonClientSimulation;

public class ClientSimulationActivity extends AppCompatActivity {

    EditText etLossProbability;
    EditText etTimeout;
    EditText etDelay;
    RadioGroup rgVerbosity;
    Button btnSave;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_simulation);
        initializeHandlers();
    }

    protected void initializeHandlers() {
        etLossProbability = (EditText) findViewById(R.id.etLossProbability);
        etTimeout = (EditText) findViewById(R.id.etTimeout);
        etDelay = (EditText) findViewById(R.id.etDelay);
        rgVerbosity = (RadioGroup) findViewById(R.id.rgVerbosity);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        /*
        RadioButton button;

        button = new RadioButton(this);
        button.setText("Level 1");
        rgVerbosity.addView(button);

        button = new RadioButton(this);
        button.setText("Level 2");
        rgVerbosity.addView(button);

        button = new RadioButton(this);
        button.setText("Level 3");
        rgVerbosity.addView(button);*/

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lossProbability;
                try {
                    lossProbability = Integer.parseInt(etLossProbability.getText().toString());
                } catch (NumberFormatException e) {
                    lossProbability = 0;
                }

                int timeout;
                try {
                    timeout = Integer.parseInt(etTimeout.getText().toString());
                } catch (NumberFormatException e) {
                    timeout = 0;
                }

                int delay;
                try {
                    delay = Integer.parseInt(etDelay.getText().toString());
                } catch (NumberFormatException e) {
                    delay = 0;
                }

                int verbosity;
                try {
                    //verbosity =
                    if (rgVerbosity.getChildAt(0).isSelected())
                        verbosity = 1;
                    else if (rgVerbosity.getChildAt(0).isSelected())
                        verbosity = 2;
                    else
                        verbosity = 3;
                } catch (Exception e) {
                    verbosity = 1;
                }

                SingletonClientSimulation settings = SingletonClientSimulation.getInstance();
                settings.setLossProbability(lossProbability);
                settings.setTimeout(timeout);
                settings.setDelay(delay);
                settings.setVerbosity(verbosity);

                finish();
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
