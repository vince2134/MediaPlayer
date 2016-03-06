package com.example.avggo.mediaplayer;

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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends AppCompatActivity {
    TextView info, infoip, msg;
    EditText portNumber;
    Button createServer;
    DatagramSocket serverSocket;
    static int SocketServerPORT;
    public static final String CONNECT = "connect";
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";
    public static final String PLAY = "play";
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
                if(portNumber.getText().toString().length() != 0) {
                    infoip.setText(getIpAddress());
                    SocketServerPORT = Integer.parseInt(portNumber.getText().toString());
                    Thread socketServerThread = new Thread(new SocketServerThread());
                    socketServerThread.start();
                }
                else
                    Toast.makeText(getBaseContext(), "Please fill in port number field.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SocketServerThread extends Thread {
        private int pic_index = 1;

        @Override
        public void run() {
            try {
                serverSocket = new DatagramSocket(SocketServerPORT);
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[10240];

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

                    if(command.toLowerCase().contains(CONNECT)) {
                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int resource = getResources().getIdentifier("test" + pic_index, "drawable", getPackageName());
                                image.setImageResource(resource);
                            }
                        });
                    }
                    else if(command.toLowerCase().contains(PREVIOUS) && pic_index > 1) {
                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pic_index--;
                                int resource = getResources().getIdentifier("test" + pic_index, "drawable", getPackageName());
                                image.setImageResource(resource);
                            }
                        });
                    }
                    else if(command.toLowerCase().contains(NEXT) && pic_index < 10) {
                        ServerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pic_index++;
                                int resource = getResources().getIdentifier("test" + pic_index, "drawable", getPackageName());
                                image.setImageResource(resource);
                            }
                        });
                    }
                    
                    InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    System.out.println(command);
                    String capitalizedSentence = command.toUpperCase();
                    sendData = capitalizedSentence.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
