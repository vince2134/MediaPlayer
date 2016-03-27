package com.example.avggo.mediaplayer;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class ServerActivity extends AppCompatActivity {
    TextView info, infoip, msg;
    EditText portNumber;
    Button createServer;
    DatagramSocket serverSocket;
    static int SocketServerPORT;

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

    public static int IMAGE_COUNT;
    public static final String FILENAME = "img";
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        exportAssetImages();

        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        portNumber = (EditText) findViewById(R.id.portNumberField);
        createServer = (Button) findViewById(R.id.createServerBtn);
        image = (ImageView) findViewById(R.id.imageView);

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
    }

    private void exportAssetImages(){
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

                while((read = IStream.read(buffer)) != -1){
                    OStream.write(buffer, 0, read);
                }

            } catch (IOException ex) {

            }

            finally {
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

    private class SocketServerThread extends Thread {
        private int pic_index = 1;
        private String[] fileList;

        byte[] accumulatedBytes = new byte[0];
        int totalByteSize = 0;
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
                assetManager = getAssets();
                fileList = assetManager.list("");
                IMAGE_COUNT = fileList.length - 3;

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

                    if(command.contains(CONNECT)) {
                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        String response = fileList[pic_index] + "";
                        sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*int resource = getResources().getIdentifier(FILENAME + pic_index, "drawable", getPackageName());
                                image.setImageResource(resource);*/
                                try {
                                    InputStream in = assetManager.open(fileList[pic_index]);
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
                    else if(command.contains(PREVIOUS)) {
                        pic_index--;
                        if (pic_index < 1)
                            pic_index = fileList.length - 2;

                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        String response = fileList[pic_index] + "";
                        sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*int resource = getResources().getIdentifier(FILENAME + pic_index, "drawable", getPackageName());
                                image.setImageResource(resource);*/
                                try {
                                    InputStream in = assetManager.open(fileList[pic_index]);
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
                    else if(command.contains(NEXT)) {

                        /*ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*pic_index++;
                                if(pic_index == fileList.length - 4)
                                    pic_index = 1;
                                //int resource = getResources().getIdentifier(FILENAME + pic_index, "drawable", getPackageName());
                                //image.setImageResource(resource);
                                try {
                                    InputStream in = assetManager.open(fileList[pic_index]);
                                    Drawable d = Drawable.createFromStream(in, null);
                                    Bitmap b = ((BitmapDrawable)d).getBitmap();
                                    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 185 * 4, 278 * 4, false);
                                    image.setImageDrawable(new BitmapDrawable(getResources(), bitmapResized));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })*/
                        pic_index++;
                        if(pic_index == fileList.length - 2)
                            pic_index = 1;

                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        String response = fileList[pic_index] + "";
                        sendData = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        nextImage();
                    }
                    else if(command.contains(SLIDESHOW)) {
                        String secsString = new String(command.split("_")[1]);
                        int secs = Integer.parseInt(secsString) * 1000;

                        //startSlideShow(secs, runnable);

                        startSlideShow(secs);
                    }
                    else if (command.contains(RECEIVE_BYTES)) {
                        byte[] receiveBytes = new byte[1500];

                        DatagramPacket receiveFragment = new DatagramPacket(receiveBytes, receiveBytes.length);

                        serverSocket.receive(receiveFragment);

                        byte[] byteChunk = receiveFragment.getData();
                        byte[] tempBytes = new byte[accumulatedBytes.length + byteChunk.length];

                        totalByteSize += receiveFragment.getLength();

                        System.arraycopy(accumulatedBytes, 0, tempBytes, 0, accumulatedBytes.length);
                        System.arraycopy(byteChunk, 0, tempBytes, accumulatedBytes.length, byteChunk.length);

                        accumulatedBytes = new byte[totalByteSize];
                        accumulatedBytes = tempBytes;
                    }
                    else if (command.contains(RESTART_TOTAL_BYTES)) { // set bytes back to default
                        accumulatedBytes = new byte[0];
                        totalByteSize = 0;
                    }
                    else if (command.contains(PROCESS_FILE)) {
                        File processedFile = new File (LOCAL_APP_STORAGE, "img" + fileList.length + ".jpg");

                        FileOutputStream fileOStream = new FileOutputStream(processedFile);

                        fileOStream.write(accumulatedBytes);
                        fileOStream.close();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
                        InputStream in = assetManager.open(fileList[pic_index]);
                        Drawable d = Drawable.createFromStream(in, null);
                        Bitmap b = ((BitmapDrawable)d).getBitmap();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}
