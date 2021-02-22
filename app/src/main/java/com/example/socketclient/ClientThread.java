package com.example.socketclient;

import android.content.Context;
import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class ClientThread implements Runnable {

    private String _serverIp;
    private int _serverPort;
    private Context _context;
    private int _clientTextColor;

    private Socket _socket;
    private DataInputStream _dataInputStream;

    public ClientThread(Context context, String serverIp, int serverPort, int clientTextColor) {

        _clientTextColor = clientTextColor;
        _context = context;
        _serverIp = serverIp;
        _serverPort = serverPort;
    }

    private void showMessage(final String message, final int color) {
        ((IMessage)_context).showMessage(message, color);
    }

    @Override
    public void run() {

        try {
            InetAddress serverAddr = InetAddress.getByName(_serverIp);
            _socket = new Socket(serverAddr, _serverPort);

            while (!Thread.currentThread().isInterrupted()) {

                _dataInputStream = new DataInputStream(_socket.getInputStream());
                String messageFromClient = _dataInputStream.readUTF();
                final JSONObject jsondata = new JSONObject(messageFromClient);
                String message = jsondata.getString("id") + "-" + jsondata.getString("message");

                if ("Disconnect".contentEquals(message)) {
                    message = "Server Disconnected.";
                    showMessage(message, Color.RED);
                    break;
                }
                showMessage("Server: " + message, _clientTextColor);
            }

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private int _globalId = 0;

    void sendMessage(final String message) {
        new Thread(() -> {
            try {
                if (null != _socket) {

                    final JSONObject jsonData = new JSONObject();
                    jsonData.put("id", _globalId++);
                    jsonData.put("message", message);

                    DataOutputStream dataOutputStream = new DataOutputStream(_socket.getOutputStream());
                    dataOutputStream.writeUTF(jsonData.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}