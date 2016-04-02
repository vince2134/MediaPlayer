package com.example.avggo.mediaplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avggo.mediaplayer.fastretransmit.Ack;
import com.example.avggo.mediaplayer.fastretransmit.Converter;
import com.example.avggo.mediaplayer.fastretransmit.Packet;
import com.example.avggo.mediaplayer.singleton.SingletonClientSimulation;
import com.example.avggo.mediaplayer.singleton.SingletonServerSimulation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ClientActivity extends AppCompatActivity {

    TextView fileName;
    EditText slideShowLength;
    Button nextBtn, prevBtn, slideshowBtn, stopBtn, uploadBtn, simulateBtn;
    Bitmap bmp;
    //ImageView image;

    public static final String PROCESS_BITMAP = "Process Bitmap";

    String ipAddress;
    int portNumber;

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
        /*ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.CONNECT);
        clientTask.execute();*/
        executeCommand(ServerActivity.CONNECT);
    }

    private void initializeHandlers() {
        ipAddress = getIntent().getStringExtra(ServerActivity.KEY_ADDRESS);
        portNumber = getIntent().getIntExtra(ServerActivity.KEY_PORT, 0);

        fileName = (TextView) findViewById(R.id.fileNameText);
        slideShowLength = (EditText) findViewById(R.id.slideShowPauseEditText);

        prevBtn = (Button) findViewById(R.id.prevBtn);
        slideshowBtn = (Button) findViewById(R.id.slideshowBtn);
        nextBtn = (Button) findViewById(R.id.nextBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        simulateBtn = (Button) findViewById(R.id.btnSimulate);

        prevBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*stopSlideShow();
                ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.PREVIOUS);
                clientTask.execute();*/
                executeCommand(ServerActivity.PREVIOUS);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*stopSlideShow();
                ClientTask clientTask = new ClientTask(ipAddress, portNumber, ServerActivity.NEXT);
                clientTask.execute();*/
                executeCommand(ServerActivity.NEXT);
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

        slideshowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int secs;
                try {
                    secs = Integer.parseInt(slideShowLength.getText().toString());
                } catch (NumberFormatException e) {
                    secs = 0;
                }

                if (secs > 0) {
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

    private void executeCommand(String command) {
        /*final String task = command;
        stopSlideShow();
        SingletonClientSimulation settings = SingletonClientSimulation.getInstance();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ClientTask clientTask = new ClientTask(ipAddress, portNumber, task);
                clientTask.execute();
                System.out.println("TASK EXECUTED");
            }
        }, settings.getDelay());*/
        stopSlideShow();
        ClientTask clientTask = new ClientTask(ipAddress, portNumber, command);
        clientTask.execute();

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

    private void receiveMedia (DatagramSocket clientSocket) throws IOException, ClassNotFoundException {
        //this.sleep(settings.getDelay());
        SingletonServerSimulation settings = SingletonServerSimulation.getInstance();

        int prevSeqNo = -1;
        int totalByteSize = 0;
        byte[] commandBytes = new byte[1024];
        byte[] receiveBytes = new byte[2048];
        DatagramPacket receiveCommand = new DatagramPacket(commandBytes, commandBytes.length);
        DatagramPacket receiveFragment = new DatagramPacket(receiveBytes, receiveBytes.length);
        ArrayList<Packet> collectedPackets = new ArrayList<>();
        ArrayList<Ack> acksInLine = new ArrayList<Ack>();

        DatagramPacket ackPacket;

        /*try {

            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    // do stuff here
                    if (!received) {
                        timedOut = true;
                        System.out.println("Timeout!");
                        generateToast("Timeout!");
                    }
                }
            }, settings.getTimeout());

            if (timedOut) {
                // Resend?

            }

            serverSocket.receive(receiveFragment);

            received = true;
            if (t != null) {
                t.cancel();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        clientSocket.receive(receiveCommand);

        String command = new String (receiveCommand.getData());

        while (command.contains(ServerActivity.RECEIVE_BYTES)) {

            if (settings.getRandomLossProbability()) {
                System.out.println("Packet lost!");
                Packet receivedPacket = (Packet) Converter.toObject(receiveFragment.getData());

                if (receivedPacket.getSeqNo() == (acksInLine.get(0).getPacketNo() + 1)) {
                    acksInLine.remove(0);
                } else {
                    if (((receivedPacket.getSeqNo() - 1) != prevSeqNo) && (prevSeqNo != -1)) {

                        if (acksInLine.get(0).getPacketNo() == -1)
                            acksInLine.set(0, new Ack(prevSeqNo));
                        else
                            acksInLine.add(new Ack(prevSeqNo));

                        if (settings.getVerbosity() == 1)
                            System.out.println("Lost packet with sequence number: " + (receivedPacket.getSeqNo() - 1));
                        if (settings.getVerbosity() == 3)
                            System.out.println("[" + new Date().toString() + "] Lost packet with sequence number: " + (receivedPacket.getSeqNo() - 1));
                    }
                }

                if (acksInLine.isEmpty())
                    acksInLine.add(new Ack(-1));

                byte[] sendAck = Converter.toBytes(acksInLine.get(0));

                ackPacket = new DatagramPacket(sendAck, sendAck.length, receiveFragment.getAddress(), receiveFragment.getPort());

                clientSocket.send(ackPacket);

                collectedPackets.add(receivedPacket);
                totalByteSize += receivedPacket.getData().length;

                if (!(receivedPacket.getSeqNo() < prevSeqNo))
                    prevSeqNo = receivedPacket.getSeqNo();
            }

            clientSocket.receive(receiveCommand);

            command = new String (receiveCommand.getData());
        }

        if (command.contains(PROCESS_BITMAP)) {

            Collections.sort(collectedPackets, new Comparator<Packet>() {
                @Override
                public int compare(Packet p1, Packet p2) {
                    return p1.getSeqNo() - p2.getSeqNo();
                }
            });
                        /*
                        for (Packet p : collectedPackets){
                            System.out.println("Packet: " + p.getSeqNo());
                        }
                        */
            int currByteIndex = 0;
            byte[] accumulatedBytes = new byte[totalByteSize];
            for (Packet p : collectedPackets) {
                System.arraycopy(p.getData(), 0, accumulatedBytes, currByteIndex, p.getData().length);

                currByteIndex += p.getData().length;
            }

            bmp = BitmapFactory.decodeByteArray(accumulatedBytes, 0, accumulatedBytes.length);

            prevSeqNo = -1;

            //continue;
        }
    }

    private void setImageView() {

    }

    private void generateToast(String message) {
        final String text = message;
        ClientActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class ClientTask extends AsyncTask<Void, Void, Void> {

        private String dstAddress;
        private int dstPort;
        private String response = "";
        private String command;

        private boolean timedOut = false;
        private boolean received = false;
        ClientTask(String addr, int port, String command){
            dstAddress = addr;
            dstPort = port;
            this.command = command;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Socket socket = null;
            SingletonClientSimulation settings = SingletonClientSimulation.getInstance();

            DatagramSocket clientSocket;
            try {
                clientSocket = new DatagramSocket();
                byte[] sendData;
                byte[] receiveData = new byte[10240];

                sendData = command.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(dstAddress), dstPort);

                /*if (!command.contains(ServerActivity.CONNECT) && settings.getRandomLossProbability()) {
                    generateToast("Packet lost!");
                    System.out.println("Packet lost!");
                    System.out.println("Client: " + sendPacket.toString());
                    return null;
                }*/

                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);


                /*Timer t = new Timer();
                if (!command.contains(ServerActivity.CONNECT)) {
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // do stuff here
                            if (!received) {
                                timedOut = true;
                                System.out.println("Timeout!");
                                generateToast("Timeout!");
                            }
                        }
                    }, settings.getTimeout());
                }*/

                if (timedOut) {
                    // Resend?
                    executeCommand(command);
                    return null;
                }

                clientSocket.receive(receivePacket);
                /*received = true;
                if (t != null) {
                    t.cancel();
                }*/
                response = new String(receivePacket.getData());
                //System.out.println(response + " Ey");

                ClientActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setFileName(response);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                generateToast("Could not connect to server");
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
