package com.example.avggo.mediaplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.avggo.mediaplayer.singleton.SingletonServerSimulation;
import com.example.avggo.mediaplayer.singleton.SingletonSimulation;

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
        etLossProbability = (EditText) findViewById(R.id.lossText);
        etTimeout = (EditText) findViewById(R.id.timeoutText);
        etDelay = (EditText) findViewById(R.id.delayText);
        rgVerbosity = (RadioGroup) findViewById(R.id.rg);
        btnSave = (Button) findViewById(R.id.saveButton);
        btnCancel = (Button) findViewById(R.id.cancelButton);

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
        rgVerbosity.addView(button);

        rgVerbosity.check(0);
        */

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lossProbability;
                try {
                    lossProbability = Integer.parseInt(etLossProbability.getText().toString());
                } catch (NumberFormatException e) {
                    lossProbability = SingletonSimulation.LOSS_PROBABILITY_DEFAULT;
                }

                int timeout;
                try {
                    timeout = Integer.parseInt(etTimeout.getText().toString());
                } catch (NumberFormatException e) {
                    timeout = SingletonSimulation.TIMEOUT_DEFAULT;
                }

                int delay;
                try {
                    delay = Integer.parseInt(etDelay.getText().toString());
                } catch (NumberFormatException e) {
                    delay = SingletonSimulation.DELAY_DEFAULT;
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
                } catch (NumberFormatException e) {
                    verbosity = SingletonSimulation.VERBOSITY_DEFAULT;
                }

                SingletonServerSimulation settings = SingletonServerSimulation.getInstance();
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
