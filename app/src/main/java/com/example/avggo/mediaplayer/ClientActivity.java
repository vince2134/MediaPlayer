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

import com.example.avggo.mediaplayer.singleton.SingletonClientSimulation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class ClientActivity extends AppCompatActivity {

    TextView fileName;
    EditText slideShowLength;
    Button nextBtn, prevBtn, playBtn, stopBtn, uploadBtn, simulateBtn;
    ImageView imageView;

    String ipAddress;
    int portNumber;

    public static final String RECEIVE_BYTES = "Receive Bytes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        initializeHandlers();
        Toast.makeText(getBaseContext(), "Attempting to connect to server...", Toast.LENGTH_SHORT).show();
        //connectToServer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectToServer();
    }

    private void connectToServer() {
        ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.CONNECT);
        clientTask.execute();
    }

    private void initializeHandlers() {
        ipAddress = getIntent().getStringExtra(ServerActivity.KEY_ADDRESS);
        portNumber = getIntent().getIntExtra(ServerActivity.KEY_PORT, 0);

        fileName = (TextView) findViewById(R.id.fileNameText);
        imageView = (ImageView) findViewById(R.id.clientImgView);
        slideShowLength = (EditText) findViewById(R.id.slideShowPauseEditText);

        prevBtn = (Button) findViewById(R.id.prevBtn);
        playBtn = (Button) findViewById(R.id.playBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        simulateBtn = (Button) findViewById(R.id.btnSimulate);

        prevBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stopSlideShow();
                ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.PREVIOUS);
                clientTask.execute();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSlideShow();
                ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.NEXT);
                clientTask.execute();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSlideShow();
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
                } catch (NumberFormatException e) {
                    secs = 0;
                }

                if (secs > 0) {
                    /*ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.SLIDESHOW + "_" + secs + "_");
                    clientTask.execute();*/
                    startSlideShow(secs * 1000);
                } else {
                    Toast.makeText(getBaseContext(), "Input valid pause length!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSlideShow();
            }
        });

        simulateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), ClientSimulationActivity.class);
                startActivity(intent);
            }
        });
    }

    private Timer timer;
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.NEXT);
            clientTask.execute();
        }
    };

    private void startSlideShow(int secs) {
        final int seconds = secs;
        ClientActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (timer != null) {
                    return;
                }

                timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 0, seconds);
            }
        });
    }

    public void stopSlideShow() {
            /*slideShowStarted = false;
            handler.removeCallbacks(nextImageRunnable);*/
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void setFileName(String fileName) {
        if (this.fileName.getText().equals("Filename"))
            Toast.makeText(getBaseContext(), "Successfully connected to server.", Toast.LENGTH_SHORT).show();

        this.fileName.setText(fileName);
    }

    class ClientTask extends AsyncTask<Void, Void, Void> {

        private String dstAddress;
        private int dstPort;
        private String response = "";
        private String command;

        private byte[] accumulatedBytes = new byte[0];
        private int totalByteSize = 0;

        private boolean received = false;
        ClientTask(String addr, int port, String command){
            dstAddress = addr;
            dstPort = port;
            this.command = command;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //SingletonClientSimulation settings = SingletonClientSimulation.getInstance();

            final DatagramSocket clientSocket;
            try {
                clientSocket = new DatagramSocket();
                byte[] sendData;
                byte[] receiveData = new byte[10240];

                sendData = command.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(dstAddress), dstPort);

                /*
                if (!settings.getRandomLossProbability()) {
                    Toast.makeText(getBaseContext(), "Packet lost!", Toast.LENGTH_SHORT).show();
                    System.out.println(sendPacket.toString());
                    return null;
                }*/


                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                /*
                Timer t = new Timer();

                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // do stuff here
                        if (!received) {
                            Toast.makeText(getBaseContext(), "Timeout!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, settings.getTimeout());
                */

                clientSocket.receive(receivePacket);
                received = true;
                //t.cancel();
                response = new String(receivePacket.getData());

                //clientSocket.close();
                //System.out.println(response + " Ey");

                ClientActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.contains(RECEIVE_BYTES))
                            setFileName(response);
                    }
                });

                while (response.contains(RECEIVE_BYTES)) {
                    byte[] receiveBytes = new byte[1500];

                    DatagramPacket receiveFragment = new DatagramPacket(receiveBytes, receiveBytes.length);

                    try {
                        clientSocket.receive(receiveFragment);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] byteChunk = receiveFragment.getData();
                    byte[] tempBytes = new byte[accumulatedBytes.length + byteChunk.length];

                    totalByteSize += receiveFragment.getLength();

                    System.arraycopy(accumulatedBytes, 0, tempBytes, 0, accumulatedBytes.length);
                    System.arraycopy(byteChunk, 0, tempBytes, accumulatedBytes.length, byteChunk.length);

                    accumulatedBytes = new byte[totalByteSize];
                    accumulatedBytes = tempBytes;
                }

                    System.out.println("Client recieves Total Bytes Received: " + accumulatedBytes.length);

            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(getBaseContext(), "Could not connect to server.", Toast.LENGTH_SHORT).show();
                finish();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
