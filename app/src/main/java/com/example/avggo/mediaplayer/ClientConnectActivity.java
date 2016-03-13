package com.example.avggo.mediaplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ClientConnectActivity extends AppCompatActivity {

    EditText editTextAddress, editTextPort;
    Button buttonConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_connect);

        initializeHandlers();
    }

    private void initializeHandlers() {
        editTextAddress = (EditText) findViewById(R.id.address);
        editTextPort = (EditText) findViewById(R.id.port);
        buttonConnect = (Button) findViewById(R.id.connectButton);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = editTextAddress.getText().toString();
                int portNumber;

                try {
                    portNumber = Integer.parseInt(editTextPort.getText().toString());
                } catch (NumberFormatException e) {
                    portNumber = -1;
                }

                if(ipAddress.length() != 0 && portNumber >= 0) {
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), ClientActivity.class);
                    intent.putExtra(ServerActivity.KEY_ADDRESS, ipAddress);
                    intent.putExtra(ServerActivity.KEY_PORT, portNumber);
                    startActivity(intent);
                }
                else
                    Toast.makeText(getBaseContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
