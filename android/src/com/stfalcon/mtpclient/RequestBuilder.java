package com.stfalcon.mtpclient;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.CRC32;


public class RequestBuilder {

    public static byte[] NEW_NONCE;
    public static byte[] G_A;
    public static byte[] G;
    public static byte[] AUTH_KEY;
    public static byte[] AUTH_KEY_HESH;
    public static byte[] DH_PRIME;
    public static byte[] SERVER_NONCE;
    public static byte[] NEW_NONCE_HASH1;
    public static byte[] NONCE;
    public static byte[] AUTH_HASH;
    public static byte[] MSG_KEY;
    public static byte[] SALT;
    public static byte[] AES_IV;
    public static byte[] AES_KEY;
    public static byte[] SESSION_ID;
    public static byte[] MESSAGE_D;
    public static BigInteger B;

    public static byte[] createReq_PqRequest() {
        try {
            //Req_Pq
            ByteBuffer bytes = ByteBuffer.allocate(4);
            byte[] req_pq = {0x78, (byte) 0x97, 0x46, 0x60};
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put(req_pq);
            byte[] arrayReqPq = bytes.array();
            bytes.clear();

            //Nonce
            bytes = ByteBuffer.allocate(16);
            byte[] b_nonce = new byte[16];
            new Random().nextBytes(b_nonce);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayNonce = bytes.array();
            bytes.put(b_nonce);
            bytes.clear();

            //AUTH_KEY ()
            bytes = ByteBuffer.allocate(8);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayAuth = bytes.array();
            bytes.clear();

            //MessageID
            bytes = ByteBuffer.allocate(8);
            bytes.putLong(System.currentTimeMillis() / 1000L);
            byte[] arrayMessageID = bytes.array();
            bytes.clear();

            //MessageLength
            bytes = ByteBuffer.allocate(4);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.putInt(arrayReqPq.length + arrayNonce.length);
            byte[] arrayMessageLength = bytes.array();
            bytes.clear();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(arrayAuth);
            outputStream.write(arrayMessageID);
            outputStream.write(arrayMessageLength);
            outputStream.write(arrayReqPq);
            outputStream.write(arrayNonce);
            byte[] arrayBodyMessage = outputStream.toByteArray();
            outputStream.flush();
            outputStream.close();
            ByteArrayOutputStream resultStreem = new ByteArrayOutputStream();
            resultStreem.write(createHeader(arrayBodyMessage.length));
            resultStreem.write(arrayBodyMessage);
            resultStreem.write(createCRC32(resultStreem.toByteArray()));
            return resultStreem.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] createHeader(int messageLength) {

        try {
            //длина сообщения
            ByteBuffer bytes = ByteBuffer.allocate(4);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.putInt(messageLength + 12);
            byte[] arrayHeader = bytes.array();
            bytes.clear();

            //порядковый номер пакета(пока ненужен)
            bytes = ByteBuffer.allocate(4);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayPacketId = bytes.array();
            bytes.clear();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(arrayHeader);
            outputStream.write(arrayPacketId);
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] createCRC32(byte[] message) {

        ByteBuffer bytes = ByteBuffer.allocate(4);
        CRC32 crc32 = new CRC32();
        crc32.update(message, 0, message.length);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.putInt((int) crc32.getValue());
        byte[] arrayCRC32 = bytes.array();
        bytes.clear();
        return arrayCRC32;
    }

    public static byte[] createP_Q_inner_data(HashMap<String, Object> hashMap) {
        try {
            //Req_Pq
            ByteBuffer bytes = ByteBuffer.allocate(4);
            byte[] p_q_inner_data = {(byte) 0x83, (byte) 0xc9, (byte) 0x5a, (byte) 0xec};
            Utils.reverseArray(p_q_inner_data);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put(p_q_inner_data);
            byte[] arrayReqPq = bytes.array();
            bytes.clear();

            bytes = ByteBuffer.allocate(12);
            byte[] pq = (byte[]) hashMap.get(Parser.PQ);
            //bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put(pq);
            byte[] arrayPQ = bytes.array();
            bytes.clear();

            bytes = ByteBuffer.allocate(8);
            bytes.put((byte[]) hashMap.get(Parser.P));
            byte[] array_p = bytes.array();
            bytes.clear();

            bytes = ByteBuffer.allocate(8);
            bytes.put((byte[]) hashMap.get(Parser.Q));
            byte[] array_q = bytes.array();
            bytes.clear();

            bytes = ByteBuffer.allocate(16);
            byte[] nonce = (byte[]) hashMap.get(Parser.NONCE);
            bytes.put(nonce);
            byte[] arrayNonce = bytes.array();
            bytes.clear();

            bytes = ByteBuffer.allocate(16);
            byte[] server_nonce = (byte[]) hashMap.get(Parser.SERVER_NONCE);
            bytes.put(server_nonce);
            byte[] arrayServerNonce = bytes.array();
            bytes.clear();

            //NewNonce
            bytes = ByteBuffer.allocate(32);
            byte[] b_nonce = new byte[32];
            new Random().nextBytes(b_nonce);
            //bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayNewNonce = bytes.array();
            NEW_NONCE = arrayNewNonce;

            bytes.put(b_nonce);
            bytes.clear();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(arrayReqPq);
            outputStream.write(arrayPQ);
            outputStream.write(array_p);
            outputStream.write(array_q);
            outputStream.write(arrayNonce);
            outputStream.write(arrayServerNonce);
            outputStream.write(arrayNewNonce);

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] create_client_DH_inner_data(HashMap<String, Object> hashMap) {
        try {
            //Client_DH_inner_data
            ByteBuffer bytes = ByteBuffer.allocate(4);
            byte[] DH_inner_data = {(byte) 0x66, (byte) 0x43, (byte) 0xb6, (byte) 0x54};
            Utils.reverseArray(DH_inner_data);
            bytes.put(DH_inner_data);
            byte[] arrayDH_inner_data = bytes.array();
            bytes.clear();

            //Nonce
            bytes = ByteBuffer.allocate(16);
            byte[] nonce = (byte[]) hashMap.get(Parser.NONCE);
            bytes.put(nonce);
            byte[] arrayNonce = bytes.array();
            bytes.clear();

            //Server_nonce
            bytes = ByteBuffer.allocate(16);
            byte[] server_nonce = (byte[]) hashMap.get(Parser.SERVER_NONCE);
            bytes.put(server_nonce);
            byte[] arrayServerNonce = bytes.array();
            bytes.clear();

            //Retry_id
            bytes = ByteBuffer.allocate(8);
            byte[] arrayRetry_id = bytes.array();
            bytes.clear();


            //Encrypted_data_bytes_header
            bytes = ByteBuffer.allocate(4);
            byte[] bytes_header = new byte[]{(byte) 0xFE, 0x00, 0x01, 0x00};
            bytes.put(bytes_header);
            byte[] gb_header = bytes.array();
            bytes.clear();

            //g_b
            bytes = ByteBuffer.allocate(256);
            byte[] g_b = generateGB(new BigInteger(G), new BigInteger(1, DH_PRIME));//G_B
            bytes.put(g_b);

            byte[] array_g_b = bytes.array();
            bytes.clear();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(arrayDH_inner_data);
            outputStream.write(arrayNonce);
            outputStream.write(arrayServerNonce);
            outputStream.write(arrayRetry_id);
            outputStream.write(gb_header);
            outputStream.write(array_g_b);

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] createReq_DHRequest(HashMap<String, Object> hashMap) {
        try {

            //AUTH_KEY ()
            ByteBuffer bytes = ByteBuffer.allocate(8);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayAuth = bytes.array();
            bytes.clear();

            //MessageID
            bytes = ByteBuffer.allocate(8);
            //
            bytes.putLong(System.currentTimeMillis() / 1000L);
            byte[] arrayMessageID = bytes.array();
            bytes.clear();

            //MessageLength
            bytes = ByteBuffer.allocate(4);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayMessageLength = bytes.array();
            bytes.putInt(320);
            bytes.clear();

            //Req_DH
            bytes = ByteBuffer.allocate(4);
            byte[] req_dh = {(byte) 0xbe, (byte) 0xe4, 0x12, (byte) 0xd7};
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put(req_dh);
            byte[] arrayReqDH = bytes.array();
            bytes.clear();

            //Nonce
            bytes = ByteBuffer.allocate(16);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put((byte[]) hashMap.get(Parser.NONCE));
            byte[] arrayNonce = bytes.array();
            bytes.clear();

            //Server_nonce
            bytes = ByteBuffer.allocate(16);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put((byte[]) hashMap.get(Parser.SERVER_NONCE));
            byte[] arrayServerNonce = bytes.array();
            bytes.clear();

            //P
            bytes = ByteBuffer.allocate(8);
            bytes.put((byte[]) hashMap.get(Parser.P));
            byte[] P = bytes.array();
            bytes.clear();

            //Q
            bytes = ByteBuffer.allocate(8);
            bytes.put((byte[]) hashMap.get(Parser.Q));
            byte[] Q = bytes.array();
            bytes.clear();

            //Public_key_fingerprint
            bytes = ByteBuffer.allocate(8);
            bytes.put((byte[]) hashMap.get(Parser.FINGER_PRINTS));
            byte[] Public_key_fingerprint = bytes.array();
            bytes.clear();

            //Encrypted_data
            bytes = ByteBuffer.allocate(4);
            byte[] bytes_header = new byte[]{(byte) 0xFE, 0x00, 0x01, 0x00};
            bytes.put(bytes_header);
            byte[] encrypted_data_header = bytes.array();
            bytes.clear();

            //Encrypted_data
            bytes = ByteBuffer.allocate(256);
            bytes.put(EncryptData.RSAEncrypt(EncryptData.getDataWithHash(RequestBuilder.createP_Q_inner_data(hashMap))));
            byte[] encrypted_data = bytes.array();
            bytes.clear();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(arrayAuth);
            outputStream.write(arrayMessageID);
            outputStream.write(arrayMessageLength);
            outputStream.write(arrayReqDH);
            outputStream.write(arrayNonce);
            outputStream.write(arrayServerNonce);
            outputStream.write(P);
            outputStream.write(Q);
            outputStream.write(Public_key_fingerprint);
            outputStream.write(encrypted_data_header);
            outputStream.write(encrypted_data);
            byte[] arrayBodyMessage = outputStream.toByteArray();
            outputStream.flush();
            outputStream.close();
            ByteArrayOutputStream resultStreem = new ByteArrayOutputStream();
            resultStreem.write(createHeader(arrayBodyMessage.length));
            resultStreem.write(arrayBodyMessage);
            resultStreem.write(createCRC32(resultStreem.toByteArray()));
            return resultStreem.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] create_Set_client_DHRequest(HashMap<String, Object> hashMap) {
        try {

            //AUTH_KEY ()
            ByteBuffer bytes = ByteBuffer.allocate(8);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayAuth = bytes.array();
            bytes.clear();

            //MessageID
            bytes = ByteBuffer.allocate(8);
            //
            bytes.putLong(Long.reverseBytes((new Date().getTime() / 1000) << 32));
            byte[] arrayMessageID = bytes.array();
            bytes.clear();

            //MessageLength
            bytes = ByteBuffer.allocate(4);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            byte[] arrayMessageLength = bytes.array();
            bytes.putInt(376);
            bytes.clear();

            //client_DH
            bytes = ByteBuffer.allocate(4);
            byte[] req_dh = {(byte) 0xF5, (byte) 0x04, 0x5F, (byte) 0x1F};
            Utils.reverseArray(req_dh);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put(req_dh);
            byte[] arrayReqDH = bytes.array();
            bytes.clear();

            //Nonce
            bytes = ByteBuffer.allocate(16);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put((byte[]) hashMap.get(Parser.NONCE));
            byte[] arrayNonce = bytes.array();
            bytes.clear();

            //Server_nonce
            bytes = ByteBuffer.allocate(16);
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            bytes.put((byte[]) hashMap.get(Parser.SERVER_NONCE));
            byte[] arrayServerNonce = bytes.array();
            bytes.clear();

            //Encrypted_data_bytes_header
            bytes = ByteBuffer.allocate(4);
            byte[] bytes_header = new byte[]{(byte) 0xFE, 0x50, 0x01, 0x00};
            bytes.put(bytes_header);
            byte[] encrypted_data_header = bytes.array();
            bytes.clear();

            //Encrypted_data
            bytes = ByteBuffer.allocate(336);
            byte[] decrypt_answer = EncryptData.decrypt_message((byte[]) hashMap.get(Parser.ENC_ANSWER), (byte[]) hashMap.get(Parser.SERVER_NONCE), NEW_NONCE);
            byte[] client_DH_inner_data = RequestBuilder.create_client_DH_inner_data(Parser.parse_server_DH_inner_data(decrypt_answer));
            bytes.put(EncryptData.igeEncrypt(EncryptData.IGE_KEY, EncryptData.IGE_IV, EncryptData.getDataWithHash1(client_DH_inner_data)));
            byte[] encrypted_data = bytes.array();
            bytes.clear();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(arrayAuth);
            outputStream.write(arrayMessageID);
            outputStream.write(arrayMessageLength);
            outputStream.write(arrayReqDH);
            outputStream.write(arrayNonce);
            outputStream.write(arrayServerNonce);
            outputStream.write(encrypted_data_header);
            outputStream.write(encrypted_data);
            byte[] arrayBodyMessage = outputStream.toByteArray();
            outputStream.flush();
            outputStream.close();
            ByteArrayOutputStream resultStreem = new ByteArrayOutputStream();
            resultStreem.write(createHeader(arrayBodyMessage.length));
            resultStreem.write(arrayBodyMessage);
            resultStreem.write(createCRC32(resultStreem.toByteArray()));
            return resultStreem.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] generateGB(BigInteger g, BigInteger dh_prime) {
        try {

            ByteBuffer bytes = ByteBuffer.allocate(256);
            byte[] b = new byte[256];
            new Random().nextBytes(b);
            bytes.put(b);
            byte[] arrayB = bytes.array();
            B = new BigInteger(arrayB);
            BigInteger g_b = g.modPow(B, dh_prime);
            byte[] gb_arr = g_b.toByteArray();
            if (gb_arr[0] == 0x00) {
                gb_arr = Utils.subByte(gb_arr, 1, gb_arr.length - 1);
            }
            saveAuthKey();
            return gb_arr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] create_saveDeveloperInfo() {
        //SALT ()
        ByteBuffer bytes = ByteBuffer.allocate(8);
        bytes.put(SALT);
        byte[] salt = bytes.array();
        bytes.clear();

        //sessionID ()
        bytes = ByteBuffer.allocate(8);
        bytes.put(SESSION_ID);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        byte[] session_id = bytes.array();//0
        bytes.clear();

        //MessageID
        bytes = ByteBuffer.allocate(8);
        bytes.putLong((System.currentTimeMillis() / 1000) << 32);
        byte[] arrayMessageID = bytes.array();
        bytes.clear();

        //seq_no
        bytes = ByteBuffer.allocate(4);
        bytes.order(ByteOrder.LITTLE_ENDIAN);//1
        bytes.putInt(1);
        byte[] arraySeq_no = bytes.array();
        bytes.clear();

        //VK ID
        bytes = ByteBuffer.allocate(4);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.putInt(21423318);
        byte[] vk_id = bytes.array();
        bytes.clear();

        //name
        String s_name = "Степан Танасийчук";
        byte[] name = l_string(s_name.getBytes());
        bytes.clear();

        //phone

        String s_phone = "+380978740342";
        byte[] phone = l_string(s_phone.getBytes());
        bytes.clear();

        //age
        bytes = ByteBuffer.allocate(4);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.putInt(27);
        byte[] age = bytes.array();
        bytes.clear();

        //city
        String s_city = "Хмельницкий";
        byte[] city = l_string(s_city.getBytes());
        Log.v("DEVELOPER", "DATA_CITY" + Utils.byteArrayToHex(city));
        bytes.clear();

        //MessageLength
        bytes = ByteBuffer.allocate(4);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        int len = vk_id.length + name.length + phone.length + age.length + city.length;
        bytes.putInt(len);
        Log.v("DEVELOPER", "DATA_LEN" + len);
        byte[] msg_len = bytes.array();
        bytes.clear();


        //req
        bytes = ByteBuffer.allocate(4);
        byte[] req_no = {(byte) 0x9a, 0x5f, 0x6e, (byte) 0x95};
        Utils.reverseArray(req_no);
        bytes.put(req_no);
        byte[] req = bytes.array();
        bytes.clear();


        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(salt);
            outputStream.write(session_id);
            outputStream.write(arrayMessageID);
            outputStream.write(arraySeq_no);
            outputStream.write(msg_len);
            outputStream.write(req);
            outputStream.write(vk_id);
            outputStream.write(name);
            outputStream.write(phone);
            outputStream.write(age);
            outputStream.write(city);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] ping() {
        //SALT ()
        ByteBuffer bytes = ByteBuffer.allocate(8);
        bytes.put(SALT);
        byte[] salt = bytes.array();
        bytes.clear();

        //sessionID ()
        bytes = ByteBuffer.allocate(8);
        bytes.put(SESSION_ID);
        byte[] session_id = bytes.array();//0
        bytes.clear();

        //MessageID
        bytes = ByteBuffer.allocate(8);
        bytes.putLong((System.currentTimeMillis() / 1000) << 32);
        byte[] arrayMessageID = bytes.array();
        bytes.clear();

        //seq_no
        bytes = ByteBuffer.allocate(4);
        bytes.order(ByteOrder.LITTLE_ENDIAN);//1
        bytes.putInt(1);
        byte[] arraySeq_no = bytes.array();
        bytes.clear();

        //MessageLength
        byte[] msg_len = Utils.hexStringToByteArray("0C000000");
        bytes.clear();


        //req
        byte[] req = Utils.hexStringToByteArray("EC77BE7A");
        bytes.clear();

        //req
        byte[] ran = Utils.hexStringToByteArray("70D7000000000000");
        bytes.clear();


        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(salt);
            outputStream.write(session_id);
            outputStream.write(arrayMessageID);
            outputStream.write(arraySeq_no);
            outputStream.write(msg_len);
            outputStream.write(req);
            outputStream.write(ran);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] sendDeveloperInfo(HashMap<String, Object> hashMap) {
        try {
            generateAES_Key();

            //AUTH_KEY_ID ()
            ByteBuffer bytes = ByteBuffer.allocate(8);
            bytes.put(AUTH_KEY_HESH);
            byte[] arrayAuth = bytes.array();
            bytes.clear();
            Log.v("DEVELOPER", "AUTH_KEY_ID" + Utils.byteArrayToHex(arrayAuth));

            //MessageKey
            bytes = ByteBuffer.allocate(16);
            bytes.put(MSG_KEY);
            byte[] arrayMessageKey = bytes.array();
            bytes.clear();
            Log.v("DEVELOPER", "MessageKey" + Utils.byteArrayToHex(arrayMessageKey));


            //Encrypted_data
            byte[] clientDeveloperInfo = MESSAGE_D;
            Log.v("BUILDER", "" + "DATA: " + Utils.byteArrayToHex(clientDeveloperInfo));
            Log.v("BUILDER", "" + "DATA byte : " + Utils.byteArrayToHex(EncryptData.getDataWithHash2(clientDeveloperInfo)));
            byte[] encript_message = EncryptData.igeEncrypt(AES_KEY, AES_IV, EncryptData.getDataWithHash2(clientDeveloperInfo));
            bytes = ByteBuffer.wrap(encript_message);
            byte[] encrypted_data = bytes.array();
            bytes.clear();
            Log.v("STRING", Utils.byteArrayToHex(encrypted_data));
            Log.v("STRING", "encrypted_data len= " + encrypted_data.length);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(arrayAuth);
            outputStream.write(arrayMessageKey);
            outputStream.write(encrypted_data);
            byte[] arrayBodyMessage = outputStream.toByteArray();
            Log.v("STRING", "body :" + arrayBodyMessage.length);
            outputStream.flush();
            outputStream.close();
            ByteArrayOutputStream resultStreem = new ByteArrayOutputStream();
            resultStreem.write(createHeader(arrayBodyMessage.length));
            resultStreem.write(arrayBodyMessage);
            resultStreem.write(createCRC32(resultStreem.toByteArray()));
            return resultStreem.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] l_string(byte[] data) {
        byte[] res;
        if (data.length > 254) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.put((byte) 254);
            byteBuffer.put((byte) (data.length));
            byteBuffer.put((byte) (data.length >> 8));
            byteBuffer.put((byte) (data.length >> 16));
            int ran_len = 0;
            while (((data.length + 4 + ran_len) % 4) != 0) {
                ran_len++;
            }
            ByteBuffer resBuffer = ByteBuffer.allocate(data.length + 4 + ran_len);
            resBuffer.put(data);
            res = Utils.sumByte(byteBuffer.array(), resBuffer.array());
        } else {
            res = Utils.sumByte(new byte[]{(byte) data.length}, data);
            int ran_len = 0;
            while (((res.length + ran_len) % 4) != 0) {
                ran_len++;
            }
            ByteBuffer resBuffer = ByteBuffer.allocate(res.length + ran_len);
            resBuffer.put(res);
            res = resBuffer.array();
            Log.v("STRING", "after" + Utils.byteArrayToHex(res));
        }
        return res;
    }

    public static void saveAuthKey() {
        Log.v("TEST", " TEST G_A" + Utils.byteArrayToHex(G_A));
        Log.v("TEST", " TEST B" + Utils.byteArrayToHex(B.toByteArray()));
        Log.v("TEST", " TEST DH_PRIME" + Utils.byteArrayToHex(DH_PRIME));
        BigInteger key = new BigInteger(G_A).modPow(B, new BigInteger(1, DH_PRIME));
        AUTH_KEY = key.toByteArray();
        if (AUTH_KEY[0] == 0x00) {
            AUTH_KEY = Utils.subByte(AUTH_KEY, 1, AUTH_KEY.length - 1);
        }
        Log.v("STRING", "AUTH_KEY :" + Utils.byteArrayToHex(AUTH_KEY));
        try {
            byte[] sha1_auth_key = EncryptData.SHAsum(AUTH_KEY);
            AUTH_KEY_HESH = Utils.subByte(sha1_auth_key, sha1_auth_key.length - 8, 8);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void generateAES_Key_DEC(byte[] msg_key) {
        try {
            int x = 0;
            byte[] sha1_a = EncryptData.SHAsum(Utils.sumByte(msg_key, Utils.subByte(AUTH_KEY, x, 32)));
            byte[] sha1_b = EncryptData.SHAsum(Utils.sumByte(Utils.subByte(AUTH_KEY, 32 + x, 16), Utils.sumByte(msg_key, Utils.subByte(AUTH_KEY, 48 + x, 16))));
            byte[] sha1_c = EncryptData.SHAsum(Utils.sumByte(Utils.subByte(AUTH_KEY, 64 + x, 32), msg_key));
            byte[] sha1_d = EncryptData.SHAsum(Utils.sumByte(msg_key, Utils.subByte(AUTH_KEY, 96 + x, 32)));
            AES_KEY = Utils.sumByte(Utils.sumByte(Utils.subByte(sha1_a, 0, 8), Utils.subByte(sha1_b, 8, 12)), Utils.subByte(sha1_c, 4, 12));
            AES_IV = Utils.sumByte(Utils.sumByte(Utils.subByte(sha1_a, 8, 12), Utils.subByte(sha1_b, 0, 8)), Utils.sumByte(Utils.subByte(sha1_c, 16, 4), Utils.subByte(sha1_d, 0, 8)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void generateAES_Key() {
        try {
            SALT = Utils.xor(Utils.subByte(NEW_NONCE, 0, 8), Utils.subByte(SERVER_NONCE, 0, 8));
            MESSAGE_D = ping();
            byte[] sha_enc_data = EncryptData.SHAsum(MESSAGE_D);
            MSG_KEY = Utils.subByte(sha_enc_data, sha_enc_data.length - 16, 16);
            Log.v("DEVELOPER", "MSG_KEY:" + Utils.byteArrayToHex(MSG_KEY));
            int x = 0;
            byte[] sha1_a = EncryptData.SHAsum(Utils.sumByte(MSG_KEY, Utils.subByte(AUTH_KEY, x, 32)));
            byte[] sha1_b = EncryptData.SHAsum(Utils.sumByte(Utils.subByte(AUTH_KEY, 32 + x, 16), Utils.sumByte(MSG_KEY, Utils.subByte(AUTH_KEY, 48 + x, 16))));
            byte[] sha1_c = EncryptData.SHAsum(Utils.sumByte(Utils.subByte(AUTH_KEY, 64 + x, 32), MSG_KEY));
            byte[] sha1_d = EncryptData.SHAsum(Utils.sumByte(MSG_KEY, Utils.subByte(AUTH_KEY, 96 + x, 32)));
            AES_KEY = Utils.sumByte(Utils.sumByte(Utils.subByte(sha1_a, 0, 8), Utils.subByte(sha1_b, 8, 12)), Utils.subByte(sha1_c, 4, 12));
            AES_IV = Utils.sumByte(Utils.sumByte(Utils.subByte(sha1_a, 8, 12), Utils.subByte(sha1_b, 0, 8)), Utils.sumByte(Utils.subByte(sha1_c, 16, 4), Utils.subByte(sha1_d, 0, 8)));

            byte[] b = {0x01};
            byte[] auth_key_aux_hash = Utils.subByte(EncryptData.SHAsum(AUTH_KEY), 0, 8);
            byte[] SHA = EncryptData.SHAsum(Utils.sumByte(Utils.sumByte(NEW_NONCE, b), auth_key_aux_hash));
            SHA = Utils.subByte(SHA, 4, 16);

            if (Arrays.equals(NEW_NONCE_HASH1, Utils.subByte(SHA, SHA.length - 16, 16))) {
                Log.v("DEVELOPER", "KEY TRUE");
            } else {
                Log.v("DEVELOPER", "KEY" + Utils.byteArrayToHex(NEW_NONCE_HASH1) + " ====" + Utils.byteArrayToHex(SHA));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
