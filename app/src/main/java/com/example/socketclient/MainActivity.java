package com.example.socketclient;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IMessage {

    public static final int SERVERPORT = 3003;
    public static final String SERVER_IP = "192.168.0.11";

    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Client");
        clientTextColor = Color.parseColor("#52FF33");
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(String.format("%s [%s]", message, getTime()));
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message, final int color) {
        handler.post(() -> msgList.addView(textView(message, color)));
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.connect_server) {
            connectToServer();
        } else if (view.getId() == R.id.send_data) {
            sendMessage();
        }
    }

    public void connectToServer() {

        msgList.removeAllViews();
        showMessage("Connecting to Server...", clientTextColor);
        clientThread = new ClientThread(MainActivity.this, SERVER_IP, SERVERPORT, clientTextColor);
        thread = new Thread(clientThread);
        thread.start();
        showMessage("Connected to Server...", clientTextColor);
    }

    private void sendMessage() {
        String clientMessage = edMessage.getText().toString().trim();
        showMessage(clientMessage, Color.BLUE);
        if (null != clientThread) {
            clientThread.sendMessage(clientMessage, false);
        }
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect", false);
            clientThread = null;
        }
    }
}