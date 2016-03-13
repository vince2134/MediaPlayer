package com.example.avggo.mediaplayer;

import android.content.Intent;
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

    TextView fileName;
    EditText slideShowLength;
    Button nextBtn, prevBtn, playBtn, uploadBtn;
    ImageView image;

    String ipAddress;
    int portNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        initializeHandlers();
        connectToServer();
    }

    private void connectToServer() {
        ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.CONNECT);
        clientTask.execute();
        Toast.makeText(getBaseContext(), "Successfully connected to server.", Toast.LENGTH_SHORT).show();
    }

    private void initializeHandlers() {
        ipAddress = getIntent().getStringExtra(ServerActivity.KEY_ADDRESS);
        portNumber = getIntent().getIntExtra(ServerActivity.KEY_PORT, 0);

        fileName = (TextView) findViewById(R.id.fileNameText);
        slideShowLength = (EditText) findViewById(R.id.slideShowPauseEditText);

        prevBtn = (Button) findViewById(R.id.prevBtn);
        playBtn = (Button) findViewById(R.id.playBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);
        uploadBtn = (Button) findViewById(R.id.uploadBtn);

        prevBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.PREVIOUS);
                clientTask.execute();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.NEXT);
                clientTask.execute();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setClass(getBaseContext(), ClientUploadActivity.class);
                intent.putExtra(ServerActivity.KEY_ADDRESS, ipAddress);
                intent.putExtra(ServerActivity.KEY_PORT, portNumber);

                startActivity(intent);
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int secs;
                try {
                    secs = Integer.parseInt(slideShowLength.getText().toString());
                } catch(NumberFormatException e) {
                    secs = 0;
                }
                if (secs > 0) {
                    ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.SLIDESHOW + "_" + secs + "_");
                    clientTask.execute();
                }
                else {
                    Toast.makeText(getBaseContext(), "Input valid pause length!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setFileName(String fileName) {
        this.fileName.setText(fileName);
    }

    class ClientTask extends AsyncTask<Void, Void, Void> {

        private String dstAddress;
        private int dstPort;
        private String response = "";
        private String command;

        ClientTask(String addr, int port, String command){
            dstAddress = addr;
            dstPort = port;
            this.command = command;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Socket socket = null;
            DatagramSocket clientSocket;
            try {
                clientSocket = new DatagramSocket();
                byte[] sendData;
                byte[] receiveData = new byte[10240];

                sendData = command.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(dstAddress), dstPort);
                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                response = new String(receivePacket.getData());
                ClientActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setFileName(response);
                    }
                });
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
            super.onPostExecute(result);
        }
    }
}
