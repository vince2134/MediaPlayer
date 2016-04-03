package com.example.avggo.mediaplayer;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avggo.mediaplayer.fastretransmit.Ack;
import com.example.avggo.mediaplayer.fastretransmit.Converter;
import com.example.avggo.mediaplayer.fastretransmit.Packet;
import com.example.avggo.mediaplayer.singleton.SingletonClientSimulation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ClientUploadActivity extends AppCompatActivity {

    TextView filePath;
    Button chooseFileBtn, uploadBtn;
    ProgressDialog progDialog;

    String ipAddress;
    int portNumber;

    public long timePacketSent = 0;
    public long timePacketReceived = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_upload);

        initHandlers();
    }

    private void initHandlers() {
        ipAddress = getIntent().getStringExtra(ServerActivity.KEY_ADDRESS);
        portNumber = getIntent().getIntExtra(ServerActivity.KEY_PORT, 0);

        filePath = (TextView) findViewById(R.id.filePathText);
        chooseFileBtn = (Button) findViewById(R.id.chooseFileBtn);
        uploadBtn = (Button) findViewById(R.id.uploadBtn2);

        chooseFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadTask uploadTask = null;
                try {
                    uploadTask = new UploadTask(ClientUploadActivity.this, InetAddress.getByName(ipAddress), portNumber, filePath.getText().toString());
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                uploadTask.execute();
            }
        });
    }

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    static final String TAG = "ClientUploadActivity";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = getPath(this, uri);
                    Log.d(TAG, "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload

                    filePath.setText(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private void generateToast(String message) {
        final String text = message;
        ClientUploadActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class UploadTask extends AsyncTask<Void, Void, Void>{
        private InetAddress dstAddress;
        private int dstPort;
        private String fPath;
        private Context c;
        private DatagramSocket clientSocket;

        boolean received = false;
        boolean timedOut = false;

        public UploadTask (Context c, InetAddress ipAddr, int port, String fPath) throws SocketException {
            dstAddress = ipAddr;
            dstPort = port;
            this.fPath = fPath;
            this.c = c;

            clientSocket = new DatagramSocket();
        }

        private void sendPacket (Packet packet) throws IOException, InterruptedException {
            Thread.sleep((timePacketReceived - timePacketSent) / 2);

            byte[] sendData = Converter.toBytes(packet);

            String command = ServerActivity.RECEIVE_BYTES;

            DatagramPacket commandPacket = new DatagramPacket(command.getBytes(), command.getBytes().length, dstAddress, dstPort);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dstAddress, dstPort);

            clientSocket.send(commandPacket); // command Server to Receive incoming bytes
            clientSocket.send(sendPacket); // send bytes to Server

            System.out.println("[" + new Date().toString() + "] Client sent packet with sequence number: " + packet.getSeqNo());
            timePacketSent = System.currentTimeMillis();
        }

        protected Void doInBackground (Void... arg0) {
            try {
                sendFile(new File(fPath), dstAddress, dstPort);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private void sendFile(File f, InetAddress ipAddr, int dstPort) throws IOException {
            int currSeqNo = 0;

            ArrayList<Packet> packetCollection = new ArrayList<Packet>();
            ArrayList<Ack> ackCollection = new ArrayList<Ack>();

            String command = "";
            DatagramPacket ackPacket;
            byte[] buffer = new byte[1500];
            byte[] receivedAck = new byte[1024];

            FileInputStream fileIStream = new FileInputStream(f);
            ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();

            try {
                for (int readNum; (readNum = fileIStream.read(buffer)) != -1;) {
                    byteOStream.write(buffer, 0, readNum);

                    System.out.println("read " + readNum + " bytes,");

                    packetCollection.add(new Packet (currSeqNo, byteOStream.toByteArray()));

                    byteOStream.reset();

                    currSeqNo++;
                }
            } catch (IOException ex) {
                Log.d(TAG, "Error in converting file to bytes");
            }

            try {
                SingletonClientSimulation settings = SingletonClientSimulation.getInstance();
                for (int i = 0; i < packetCollection.size(); i++) {
                    Packet p = packetCollection.get(i);
                    /*
                    if (ackCollection.size() == 3) {
                        for (Ack a : ackCollection) {
                            System.out.println("Fast Retransmit : Ack Collection contains: " + a.getPacketNo());
                        }
                        sendPacket(packetCollection.get(ackCollection.get(0).getPacketNo() + 1));

                        ackCollection.clear();

                        ackPacket = new DatagramPacket(receivedAck, receivedAck.length);

                        clientSocket.receive(ackPacket);

                        Ack ack = (Ack) Converter.toObject(ackPacket.getData());

                        if (ack.getPacketNo() != -1) {
                            ackCollection.add(ack);
                            System.out.println("Fast Retransmit: Received Ack" + ack.getPacketNo() + "!");
                        }
                    }
                    */

                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (!received) {
                                timedOut = true;
                                System.out.println("Timed out!");
                                //generateToast("Timed out!");
                            }
                        }
                    }, settings.getTimeout());

                    if (settings.getRandomLossProbability()) {
                        if (settings.getVerbosity() == 1 || settings.getVerbosity() == 2)
                            System.out.println("Lost packet with sequence number: " + p.getSeqNo());
                        else if (settings.getVerbosity() == 3)
                            System.out.println("[" + new Date().toString() + "] Lost packet with sequence number: " + p.getSeqNo());

                        continue;
                    }

                    sendPacket(p);
                    if (settings.getVerbosity() == 2) {
                        System.out.println("Sent packet with sequence number: " + p.getSeqNo());
                    }
                    else if (settings.getVerbosity() == 3) {
                        System.out.println("[" + new Date().toString() + "] Sent packet with sequence number: " + p.getSeqNo());
                    }

                    ackPacket = new DatagramPacket(receivedAck, receivedAck.length);

                    clientSocket.receive(ackPacket);

                    Ack ack = (Ack) Converter.toObject(ackPacket.getData());

                    if (ack.getPacketNo() != -1) {
                        ackCollection.add(ack);
                        if (settings.getVerbosity() == 2) {
                            System.out.println("Received ack with sequence number: " + ack.getPacketNo());
                        }
                        else if (settings.getVerbosity() == 3) {
                            System.out.println("[" + new Date().toString() + "] Received ack with sequence number: " + ack.getPacketNo());
                        }
                            //System.out.println("Fast Retransmit: Received Ack" + ack.getPacketNo() + "!");
                    }
                    timePacketReceived = System.currentTimeMillis();
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byteOStream.close();

            command = ServerActivity.PROCESS_FILE;
            clientSocket.send(new DatagramPacket(command.getBytes(), command.getBytes().length, ipAddr, dstPort));

            command = ServerActivity.RESTART_TOTAL_BYTES;
            clientSocket.send(new DatagramPacket(command.getBytes(), command.getBytes().length, ipAddr, dstPort));

            clientSocket.close();

            Log.d(TAG, "Sent");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progDialog = new ProgressDialog(c);
            progDialog.setMessage("Uploading please wait...");
            progDialog.setCanceledOnTouchOutside(false);
            progDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progDialog.dismiss();

            finish();
        }
    }
}