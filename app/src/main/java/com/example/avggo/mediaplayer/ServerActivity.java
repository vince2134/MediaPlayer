package com.example.avggo.mediaplayer;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.example.avggo.mediaplayer.fastretransmit.Ack;
import com.example.avggo.mediaplayer.fastretransmit.Converter;
import com.example.avggo.mediaplayer.fastretransmit.Packet;
import com.example.avggo.mediaplayer.singleton.SingletonServerSimulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class ServerActivity extends AppCompatActivity {
    TextView info, infoip, msg;
    EditText portNumber;
    Button createServer, simulateButton;
    DatagramSocket serverSocket;
    static int SocketServerPORT;

    ArrayList<File> fileCollection = new ArrayList<File>();

    //Hash Keys
    public static final String KEY_ADDRESS = "IP_Address";
    public static final String KEY_PORT = "Port_Number";

    //Commands
    public static final String CONNECT = "Connect";
    public static final String NEXT = "Next";
    public static final String PREVIOUS = "Previous";
    public static final String SLIDESHOW = "Slideshow";
    public static final String RECEIVE_BYTES = "Receive Bytes";
    public static final String RESTART_TOTAL_BYTES = "Restart Total Bytes";
    public static final String PROCESS_FILE = "Process File";

    public static final String LOCAL_APP_STORAGE = Environment.getExternalStorageDirectory() + "/Pictures/MediaPlayer/";
    ImageSwitcher image;

    private boolean timedOut = false;
    private boolean received = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        exportAssetImages();

        File localFolder = new File (LOCAL_APP_STORAGE);
        File[] listOfFiles = localFolder.listFiles();

        for (File f : listOfFiles) {
            fileCollection.add(f);
        }

        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        portNumber = (EditText) findViewById(R.id.portNumberField);
        createServer = (Button) findViewById(R.id.createServerBtn);
        image = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        simulateButton = (Button) findViewById(R.id.simulateButton);

        image.setFactory(new ViewFactory() {
            @Override
            public View makeView() {
                ImageView myView = new ImageView(getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                return myView;
            }
        });

        createServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!portNumber.getText().toString().isEmpty()) {
                    infoip.setText(getIpAddress());
                    SocketServerPORT = Integer.parseInt(portNumber.getText().toString());
                    Thread socketServerThread = new Thread(new SocketServerThread());
                    socketServerThread.start();
                } else
                    Toast.makeText(getBaseContext(), "Please fill in port number field.", Toast.LENGTH_SHORT).show();
            }
        });

        simulateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), ServerSimulationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void exportAssetImages() {
        AssetManager assetManager = getAssets();

        String[] files = null;

        /* START of copying files from assets to local storage */
        try {
            files = assetManager.list("");
        } catch (IOException e) {

        }

        if (files != null) for (String filename : files) {
            InputStream IStream = null;
            OutputStream OStream = null;

            try {
                IStream = assetManager.open(filename);
                File OFile = new File(LOCAL_APP_STORAGE, filename);

                OStream = new FileOutputStream(OFile);
                byte[] buffer = new byte[1024];

                int read;

                while ((read = IStream.read(buffer)) != -1) {
                    OStream.write(buffer, 0, read);
                }

            } catch (IOException ex) {

            } finally {
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
        /* END of copying files from assets to local storage */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverSocket != null)
            serverSocket.close();
    }

    private class SocketServerThread extends Thread {
        private int pic_index = 0;

        private byte[] accumulatedBytes = new byte[0];
        private int totalByteSize = 0;

        private ArrayList<Packet> collectedPackets = new ArrayList<Packet>();
        private ArrayList<Ack> acksInLine = new ArrayList<Ack>();

        //private boolean slideShowStarted = false;
        //private Handler handler = new Handler();
        private Timer timer;
        private TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                nextImage();
            }
        };
        private AssetManager assetManager = getAssets();

        /*Runnable nextImageRunnable = new Runnable() {
            @Override
            public void run() {
                nextImage();
            }
        };*/

        @Override
        public void run() {
            try {
                serverSocket = new DatagramSocket(SocketServerPORT);
                byte[] receiveData = new byte[1024];
                byte[] sendData;
                acksInLine.add(new Ack(-1));
                assetManager = getAssets();
                SingletonServerSimulation settings = SingletonServerSimulation.getInstance();

                int prevSeqNo = -1;


                ServerActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("Port Number: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    String command = new String(receivePacket.getData());

                    if (command.contains(RESTART_TOTAL_BYTES)) { // set bytes back to default
                        collectedPackets.clear();
                        accumulatedBytes = new byte[0];
                        totalByteSize = 0;
                    }
                    else if (command.contains(PROCESS_FILE)) {
                        File processedFile = new File(LOCAL_APP_STORAGE, "img" + fileCollection.size() + ".jpg");

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
                        accumulatedBytes = new byte[totalByteSize];
                        for (Packet p : collectedPackets) {
                            System.arraycopy(p.getData(), 0, accumulatedBytes, currByteIndex, p.getData().length);

                            currByteIndex += p.getData().length;
                        }

                        FileOutputStream fileOStream = new FileOutputStream(processedFile);

                        fileOStream.write(accumulatedBytes);

                        fileCollection.add(processedFile);

                        fileOStream.close();

                        prevSeqNo = -1;

                        //continue;
                    }

                    if (settings.getRandomLossProbability()) {
                        System.out.println("Packet lost!");
                        this.sleep(settings.getDelay());

                    }

                    else if (command.contains(RECEIVE_BYTES)) {
                        this.sleep(settings.getDelay());

                        byte[] receiveBytes = new byte[2048];
                        DatagramPacket receiveFragment = new DatagramPacket(receiveBytes, receiveBytes.length);
                        DatagramPacket ackPacket;

                        try {
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

                        Packet receivedPacket = (Packet) Converter.toObject(receiveFragment.getData());

                        if (receivedPacket.getSeqNo() == (acksInLine.get(0).getPacketNo() + 1)) {
                            acksInLine.remove(0);
                        } else {

                            if (((receivedPacket.getSeqNo() - 1) != prevSeqNo) && (prevSeqNo != -1)) {

                                if (acksInLine.get(0).getPacketNo() == -1)
                                    acksInLine.set(0, new Ack(prevSeqNo));
                                else
                                    acksInLine.add(new Ack(prevSeqNo));

                                System.out.println ("[" + new Date().toString() + "] Lost packet with sequence number: " + (receivedPacket.getSeqNo()-1));

                            }
                        }

                        if (acksInLine.isEmpty())
                            acksInLine.add(new Ack(-1));

                        byte[] sendAck = Converter.toBytes(acksInLine.get(0));

                        ackPacket = new DatagramPacket(sendAck, sendAck.length, receiveFragment.getAddress(), receiveFragment.getPort());

                        serverSocket.send(ackPacket);

                        collectedPackets.add(receivedPacket);
                        totalByteSize += receivedPacket.getData().length;

                        if (!(receivedPacket.getSeqNo() < prevSeqNo))
                            prevSeqNo = receivedPacket.getSeqNo();
                    }
                    else if (command.contains(CONNECT)) {
                        Animation in = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in);
                        Animation out = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_out);
                        image.setInAnimation(in);
                        image.setOutAnimation(out);

                        for (int i = 0; i < fileCollection.size(); i++)
                            System.out.println(fileCollection.get(i).getName());

                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        String response = fileCollection.get(pic_index).getName();
                        sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FileInputStream in = new FileInputStream(fileCollection.get(pic_index));
                                    Drawable d = Drawable.createFromStream(in, null);
                                    Bitmap b = ((BitmapDrawable) d).getBitmap();
                                    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 185 * 4, 278 * 4, false);
                                    image.setImageDrawable(new BitmapDrawable(getResources(), bitmapResized));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else if (command.contains(PREVIOUS)) {
                        Animation in = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.slide_in_left);
                        Animation out = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.slide_out_right);
                        image.setInAnimation(in);
                        image.setOutAnimation(out);

                        pic_index--;
                        if (pic_index < 0)
                            pic_index = fileCollection.size() - 1;

                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        String response = fileCollection.get(pic_index).getName();
                        sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FileInputStream in = new FileInputStream(fileCollection.get(pic_index));
                                    Drawable d = Drawable.createFromStream(in, null);
                                    Bitmap b = ((BitmapDrawable) d).getBitmap();
                                    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 185 * 4, 278 * 4, false);
                                    image.setImageDrawable(new BitmapDrawable(getResources(), bitmapResized));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else if (command.contains(NEXT)) {

                        Animation in = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_right);
                        Animation out = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out_left);
                        image.setInAnimation(in);
                        image.setOutAnimation(out);

                        pic_index++;
                        if (pic_index == fileCollection.size())
                            pic_index = 0;

                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        String response = fileCollection.get(pic_index).getName();
                        sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        nextImage();
                    }
                    else if (command.contains(SLIDESHOW)) {
                        String secsString = new String(command.split("_")[1]);
                        int secs = Integer.parseInt(secsString) * 1000;

                        //startSlideShow(secs, runnable);

                        startSlideShow(secs);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void generateToast(String message) {
            final String text = message;
            ServerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void startSlideShow(int secs) {
            final int seconds = secs;
            /*slideShowStarted = true;
            handler.postDelayed(nextImageRunnable, secs);*/
            ServerActivity.this.runOnUiThread(new Runnable() {
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

        public void nextImage() {
            ServerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        FileInputStream in = new FileInputStream(fileCollection.get(pic_index));
                        Drawable d = Drawable.createFromStream(in, null);
                        Bitmap b = ((BitmapDrawable) d).getBitmap();
                        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 185 * 4, 278 * 4, false);
                        image.setImageDrawable(new BitmapDrawable(getResources(), bitmapResized));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "IP Address: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}
