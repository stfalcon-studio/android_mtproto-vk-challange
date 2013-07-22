package com.stfalcon.mtpclient;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * Created by user on 7/19/13.
 */
public class TCPLink extends Service {
    private static final String HOST = "95.142.192.65";
    public static TCPLink self;
    String binaryString;

    public static TCPLink getInstance() {
        if (self == null) {
            self = new TCPLink();
        }
        return self;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
    }

    // Здесь выполняем инициализацию нужных нам значений
// и открываем наше сокет-соединение
    private void startService() {

        Log.i("Loger", "Start Servise");

        binaryString = "34 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1d 36 e8 51 14 00 00 00 78 97 46 60 04 91 61 61 8e 47 6b bd 82 3b 53 fb 09 1a 88 00 5d bc 15 65";

        try {
            openConnection();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // данный метод открыает соединение
    public void openConnection() throws InterruptedException {
        try {

            // PutData - это класс, с помощью которого мы передадим параметры в
            // создаваемый поток
            PutData data = new PutData();
            data.dataFromServer = binaryString;
            data.context = this;

            // создаем новый поток для сокет-соединения
            new ToSocket().execute(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PutData {
        String dataFromServer;
        Context context;
    }

    class ToSocket extends AsyncTask<PutData, Integer, Integer> {
        Context mCtx;
        Socket mySock;

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Integer result) {
            Log.v("SEND_DATA", "done");
            // Это выполнится после завершения работы потока
        }

        protected void onCancelled(Integer result) {
            Log.v("SEND_DATA", "canceled");
            // Это выполнится после завершения работы потока
        }

        protected Integer doInBackground(PutData... param) {
            Log.v("SEND_DATA", "start");
            InetAddress serverAddr;

            mCtx = param[0].context;
            String toServer = param[0].dataFromServer;

            try {
                // while (true) {
                serverAddr = InetAddress.getByName(HOST);
                mySock = new Socket(serverAddr, 80);

                // открываем сокет-соединение
                SocketData data = new SocketData();
                data.context = mCtx;
                data.socket = mySock;

                // Еще один поток, именно он будет принимать входящие сообщения
                GetPacket pack = new GetPacket();
                AsyncTask<SocketData, Integer, Integer> running = pack.execute(data);

                String message = toServer;
                // Посылаем dataFromServer на сервер
                try {
                    //Log.v("SEND_DATA", "send " + message);
                    DataOutputStream dos = new DataOutputStream(mySock.getOutputStream());
                    long time = System.currentTimeMillis() / 1000L;
                    ByteBuffer bytes = ByteBuffer.allocate(8);
                    bytes.order(ByteOrder.LITTLE_ENDIAN);
                    bytes.putInt(52);
                    Log.v("LOGER", Utils.byteArrayToHex(bytes.array()));
                    byte[] arrayHeader = bytes.array();
                    bytes.clear();

                    bytes = ByteBuffer.allocate(8);
                    bytes.order(ByteOrder.LITTLE_ENDIAN);
                    Log.v("LOGER", Utils.byteArrayToHex(bytes.array()));
                    byte[] arrayAuth = bytes.array();
                    bytes.clear();

                    bytes = ByteBuffer.allocate(8);
                    bytes.putLong(System.currentTimeMillis() / 1000L);
                    Log.v("LOGER", Utils.byteArrayToHex(bytes.array()));
                    byte[] arrayMessageID = bytes.array();
                    bytes.clear();

                    bytes = ByteBuffer.allocate(4);
                    bytes.order(ByteOrder.LITTLE_ENDIAN);
                    bytes.putInt(20);
                    Log.v("LOGER", Utils.byteArrayToHex(bytes.array()));
                    byte[] arrayMessageLength = bytes.array();
                    bytes.clear();

                    bytes = ByteBuffer.allocate(4);
                    byte[] req_pq = {0x78, (byte) 0x97, 0x46, 0x60};
                    bytes.order(ByteOrder.LITTLE_ENDIAN);
                    bytes.put(req_pq);
                    Log.v("LOGER", Utils.byteArrayToHex(bytes.array()));
                    byte[] arrayReqPq = bytes.array();
                    bytes.clear();

                    bytes = ByteBuffer.allocate(16);
                    byte[] b_nonce = new byte[16];
                    new Random().nextBytes(b_nonce);
                    bytes.order(ByteOrder.LITTLE_ENDIAN);
                    Log.v("LOGER", Utils.byteArrayToHex(b_nonce));
                    byte[] arrayNonce = bytes.array();
                    bytes.put(b_nonce);
                    bytes.clear();

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(arrayHeader);
                    outputStream.write(arrayAuth);
                    outputStream.write(arrayMessageID);
                    outputStream.write(arrayMessageLength);
                    outputStream.write(arrayReqPq);
                    outputStream.write(arrayNonce);

                    bytes = ByteBuffer.allocate(4);
                    CRC32 crc32 = new CRC32();
                    crc32.update(outputStream.toByteArray(), 0, outputStream.size());
                    bytes.order(ByteOrder.LITTLE_ENDIAN);
                    bytes.putInt((int) crc32.getValue());
                    Log.v("LOGER", Utils.byteArrayToHex(bytes.array()));
                    byte[] arrayCRC32 = bytes.array();
                    bytes.clear();

                    outputStream.write(arrayCRC32);

                    Log.v("LOGER", "REQ_PQ" + Utils.byteArrayToHex(outputStream.toByteArray()));


                    //Log.v("Loger", "" + Integer.toHexString(b.length));
                    //System.currentTimeMillis()<<32;
                    dos.write(outputStream.toByteArray());
                    dos.flush();

                    //out.println(message);

                } catch (Exception e) {
                    e.printStackTrace();
                    while (running.getStatus().equals(AsyncTask.Status.RUNNING)) ;
                    Log.v("SEND_DATA", "fail thread");
                }  // Следим за потоком, принимающим сообщения
                // Если поток закончил принимать сообщения - это означает,
                // что соединение разорвано (других причин нет).
                // Это означает, что нужно закрыть сокет
                // и открыть его опять в бесконечном цикле (см. while(true) выше)
                try {
                    //mySock.close();
                } catch (Exception e) {
                }
                return 1;
                // }
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    class SocketData {
        Socket socket;
        Context context;
    }

    class GetPacket extends AsyncTask<SocketData, Integer, Integer> {
        Context mCtx;
        byte[] mData;
        Socket mySock;

        protected void onProgressUpdate(Integer... progress) {
            try {
                // Получаем принятое от сервера сообщение
                //String prop = String.valueOf(mData);
                Log.v("GET_DATA", "data: " + Utils.byteArrayToHex(mData));
            } catch (Exception e) {
                MTPapp.showToastMessage("Socket error: " + e.getMessage());
            }
        }

        protected void onPostExecute(Integer result) {
            // Это выполнится после завершения работы потока
            Log.v("GET_DATA", "done");
        }

        protected void onCancelled(Integer result) {
            // Это выполнится после завершения работы потока
            Log.v("GET_DATA", "canceled");
        }

        protected Integer doInBackground(SocketData... param) {
            Log.v("GET_DATA", "start get thread");
            mySock = param[0].socket;
            mCtx = param[0].context;
            mData = new byte[4096];

            try {
                DataInputStream reader = new DataInputStream(mySock.getInputStream());
                int read = 0;
                // Принимаем сообщение от сервера
                // Данный цикл будет работать, пока соединение не оборвется
                // или внешний поток не скажет данному cancel()
                while (((read = reader.read(mData)) >= 0 && !isCancelled())) {
                    // "Вызываем" onProgressUpdate каждый раз, когда принято сообщение
                    Log.v("GET_DATA", "read data");
                    //read = reader.read(mData);
                    if (read > 0) publishProgress(read);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
    }


}

	 

	
