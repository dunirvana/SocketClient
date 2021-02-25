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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    private void reconnectToServer() {
        ((IMessage)_context).connectToServer();
    }

    @Override
    public void run() {

        AtomicBoolean disconnected = new AtomicBoolean(false);

        try {
            InetAddress serverAddr = InetAddress.getByName(_serverIp);
            _socket = new Socket(serverAddr, _serverPort);

            while (!Thread.currentThread().isInterrupted()) {

                _dataInputStream = new DataInputStream(_socket.getInputStream());

                //String messageFromClient = _dataInputStream.readUTF();
                String messageFromClient = Cryptography.decrypt(_dataInputStream.readUTF(), null);

                final JSONObject jsondata = new JSONObject(messageFromClient);
                AtomicReference<String> message = new AtomicReference<>(jsondata.getString("id") + "-" + jsondata.getString("message"));

                if ("Disconnect".contentEquals(message.get())) {
                    disconnected.set(true);
                    message.set("Server Disconnected.");
                    showMessage(message.get(), Color.RED);
                    break;
                }

                if (!jsondata.has("isConfirmationMessage") || jsondata.getString("isConfirmationMessage") != "true") {

                    // se nao for uma mensagem de confirmacao coloca texto no display
                    showMessage("Server: " + message, _clientTextColor);

                    // envia para o servidor a confirmacao
                    sendMessage("Cliente recebeu mensagem: " + jsondata.getString("id"), true);
                }
                else {
                    _lastMessageSentConfirmed = 'Y';

                }

            }

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (disconnected.get())
            reconnectToServer();
    }

    private int _globalId = 0;

    private char _lastMessageSentConfirmed = '-';

    void sendMessage(final String message, boolean isConfirmationMessage) {

        if (_lastMessageSentConfirmed == 'N' && !isConfirmationMessage) {
            showMessage("A ultima mensagem nao foi entregue, realize nova conexao com o servidor", Color.MAGENTA);
            return;
        }

        new Thread(() -> {
            try {
                if (null != _socket) {

                    final JSONObject jsonData = new JSONObject();
                    jsonData.put("id", _globalId++);
                    jsonData.put("message", message);
                    jsonData.put("isConfirmationMessage", isConfirmationMessage);

                    DataOutputStream dataOutputStream = new DataOutputStream(_socket.getOutputStream());

                    String jsonEncrypted = Cryptography.encrypt(jsonData.toString(), null);

                    dataOutputStream.writeUTF(jsonEncrypted);

                    // se for uma mensagem de confirmacao de recebimento significa que o servidor esta acessivel, entao liberar novas tentativas de envio
                    _lastMessageSentConfirmed = isConfirmationMessage ? '-' : 'N';
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}