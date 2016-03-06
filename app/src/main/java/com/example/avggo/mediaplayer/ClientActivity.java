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

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyClientTask myClientTask = new MyClientTask(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()), "previous");
                myClientTask.execute();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyClientTask myClientTask = new MyClientTask(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()), "next");
                myClientTask.execute();
            }
        });
    }

    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    if(editTextAddress.getText().toString().length() != 0 && editTextPort.getText().toString().length() != 0) {
                        MyClientTask myClientTask = new MyClientTask(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()), "connect");
                        myClientTask.execute();
                    }
                    else
                        Toast.makeText(getBaseContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                }
            };

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String command;

        MyClientTask(String addr, int port, String command){
            dstAddress = addr;
            dstPort = port;
            this.command = command;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            DatagramSocket clientSocket = null;
            try {
                clientSocket = new DatagramSocket();
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[10240];

                sendData = command.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(dstAddress), dstPort);
                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                response = new String(receivePacket.getData());

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }
    }
}
