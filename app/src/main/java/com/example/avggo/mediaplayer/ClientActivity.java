package com.example.avggo.mediaplayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientActivity extends AppCompatActivity {

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear, nextBtn, prevBtn, playBtn;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        textResponse = (TextView)findViewById(R.id.response);
        prevBtn = (Button) findViewById(R.id.prevBtn);
        playBtn = (Button) findViewById(R.id.playBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);

        //clientTask = new ClientTask(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()));

        //buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientTask clientTask = new ClientTask(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()), ServerActivity.PREVIOUS);
                clientTask.execute();
                setTextResponse(ServerActivity.PREVIOUS);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientTask clientTask = new ClientTask(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()), ServerActivity.NEXT);
                clientTask.execute();
                setTextResponse(ServerActivity.NEXT);
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextAddress.getText().toString().length() != 0 && editTextPort.getText().toString().length() != 0) {
                    ClientTask clientTask = new ClientTask(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()), ServerActivity.CONNECT);
                    clientTask.execute();
                    setTextResponse(ServerActivity.CONNECT);
                }
                else
                    Toast.makeText(getBaseContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTextResponse(String command) {
        textResponse.setText("Initiated " + command + " command to the server.");
    }
}
