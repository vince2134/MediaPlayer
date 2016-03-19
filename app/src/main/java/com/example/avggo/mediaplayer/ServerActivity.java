package com.example.avggo.mediaplayer;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
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
    public static final String PLAY = "Play";
    public static final String SLIDESHOW = "Slideshow";


    public static int IMAGE_COUNT;
    public static final String FILENAME = "img";
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

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

    private class SocketServerThread extends Thread {
        private int pic_index = 0;
        private String[] fileList;
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
                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*int resource = getResources().getIdentifier(FILENAME + pic_index, "drawable", getPackageName());
                                image.setImageResource(resource);*/
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
                    else if(command.contains(PREVIOUS)) {
                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pic_index--;
                                if (pic_index < 1)
                                    pic_index = fileList.length - 4;

                                /*int resource = getResources().getIdentifier(FILENAME + pic_index, "drawable", getPackageName());
                                image.setImageResource(resource);*/
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
                        nextImage();
                    }
                    else if(command.contains(SLIDESHOW)) {
                        String secsString = new String(command.split("_")[1]);
                        int secs = Integer.parseInt(secsString) * 1000;

                        //startSlideShow(secs, runnable);

                        startSlideShow(secs);
                    }
                    
                    InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    String response = fileList[pic_index] + "";
                    sendData = response.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
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
                    pic_index++;
                    if(pic_index == fileList.length - 4)
                        pic_index = 1;

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
