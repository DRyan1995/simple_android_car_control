package com.orz.awang;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    Button fwdButton;
    Button backButton;
    Button leftButton;
    Button rightButton;
    Button stopButton;
    Button grabButton;
    Button confirmButton;
    EditText inputIP;
    EditText inputPort;
    RecyclerView logView;
    Pattern pattern;
    InfoAdapter adapter;

    Boolean is_IP_valid = false;
    String ip_address = "";
    int port_num = 0;
    NetworkThread thread;
    UIHandler uHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        regexInit();
    }

    class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg2){
                case 0:
                    adapter.addMSG(new LogMSG("SERVER MESSAGE",0));
                    adapter.addMSG(new LogMSG((String) msg.obj,0));
                    break;
                case 1:
                    adapter.addMSG(new LogMSG("SOCKET CONNECTED",0));
                    break;
                case 2:
                    adapter.addMSG(new LogMSG("SOCKET CONNECTION FAILED",1));
                    break;
                case 8:
                    adapter.addMSG(new LogMSG("FAILED SENDING",1));
                    break;
                case 9:
                    adapter.addMSG(new LogMSG("MESSAGE SENT",0));
                    break;
            }
        }
    }



    public void init() {
        fwdButton = (Button) findViewById(R.id.fwd_btn);
        backButton = (Button) findViewById(R.id.back_btn);
        leftButton = (Button) findViewById(R.id.l_btn);
        rightButton = (Button) findViewById(R.id.r_btn);
        stopButton = (Button) findViewById(R.id.s_btn);
        confirmButton = (Button) findViewById(R.id.ip_confirm_btn);
        inputIP = (EditText) findViewById(R.id.ip_input);
        inputPort = (EditText) findViewById(R.id.ip_input_port);
        logView = (RecyclerView) findViewById(R.id.log_container);
        grabButton = (Button) findViewById(R.id.grab_btn);

        confirmButton.setOnClickListener(new IPConfirmListener());
        fwdButton.setOnClickListener(new ActionButtonListener());
        backButton.setOnClickListener(new ActionButtonListener());
        leftButton.setOnClickListener(new ActionButtonListener());
        rightButton.setOnClickListener(new ActionButtonListener());
        stopButton.setOnClickListener(new ActionButtonListener());
        grabButton.setOnClickListener(new ActionButtonListener());

        adapter = new InfoAdapter(MainActivity.this,new ArrayList<LogMSG>());
        logView.setAdapter(adapter);
        logView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        logView.setLayoutManager(linearLayoutManager);

        uHandler = new UIHandler();

    }

    public void regexInit() {
        pattern = Pattern.compile("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");
    }

    public void startSocket() {
        thread = new NetworkThread(uHandler);
        thread.start();
        //prepareREAD();
    }

    public void prepareREAD(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = Message.obtain();
                    msg.arg1 = 6;
                    thread.handler.sendMessage(msg);
                    Log.e("ATTENTION","READ MSG SEND");

                }

            }
        }).start();
    }

    class ActionButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (is_IP_valid) {
                Message msg = Message.obtain();
                switch (v.getId()) {
                    case R.id.fwd_btn:
                        msg.arg1 = 0;
                        break;
                    case R.id.back_btn:
                        msg.arg1 = 1;
                        break;
                    case R.id.l_btn:
                        msg.arg1 = 2;
                        break;
                    case R.id.r_btn:
                        msg.arg1 = 3;
                        break;
                    case R.id.s_btn:
                        msg.arg1 = 4;
                        break;
                    case R.id.grab_btn:
                        msg.arg1 = 5;
                        break;
                    default:
                        break;
                }
                Log.e("ATTENTION","BUTTON CLICKED");
                thread.handler.sendMessage(msg);
                //adapter.addMSG(new LogMSG("Message Send",0));
                logView.smoothScrollToPosition(0);

            } else {
                adapter.addMSG(new LogMSG("PLEASE CONFIGURE IP ADDRESS FIRST",1));
                logView.smoothScrollToPosition(0);
            }

        }
    }

    class IPConfirmListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String content = inputIP.getText().toString();
            Log.e("INFO", content);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                ip_address = content;

                port_num = Integer.parseInt(inputPort.getText().toString());
                Log.e("INFO", "IP is:" + ip_address + " Port is:" + port_num);
                adapter.addMSG(new LogMSG( "IP is:" + ip_address + " Port is:" + port_num,0));
                if (is_IP_valid)
                    thread.closeSocket();
                adapter.addMSG(new LogMSG("SOCKET CONNECTING",0));
                startSocket();
                is_IP_valid = true;
                logView.smoothScrollToPosition(0);
            } else {
                adapter.addMSG(new LogMSG("PLEASE CHECK YOUR IP ADDRESS",1));
                logView.smoothScrollToPosition(0);
            }

        }
    }

    class NetworkThread extends Thread {
        public Handler handler;
        Socket socket = null;
        String socketMsg = "";
        Boolean isReadFinished = true;
        UIHandler ui;
        BufferedReader reader;
        public NetworkThread(UIHandler uH) {
            super();
            ui = uH;
        }

        @Override
        public void run() {
            super.run();

            prepareSocket();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {

                            //TODO:CHANGE READ FREQUENCY HERE
                            Thread.sleep(2000);
                            //

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        NetworkThread.this.socketMsg = "READ";
                        NetworkThread.this.handleMSG();
                        Log.e("ATTENTION","READ MSG SEND");

                    }

                }
            }).start();
            Looper.prepare();
            Log.e("ATTENTION","NEW HANDLER");
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.arg1) {
                        case 0:
                            socketMsg = "*FWD*";
                            break;
                        case 1:
                            socketMsg = "*BWD*";
                            break;
                        case 2:
                            socketMsg = "*LFT*";
                            break;
                        case 3:
                            socketMsg = "*RHT*";
                            break;
                        case 4:
                            socketMsg = "*STP*";
                            break;
                        case 5:
                            socketMsg = "*GRB*";
                            break;
                        case 6:
                            socketMsg = "READ";
                            break;
                    }
                    Log.e("ATTENTION","HANDLER ENTERED");
                    NetworkThread.this.handleMSG();
                }
            };
            Looper.loop();

        }

        void handleMSG() {
            try {
                if(socketMsg.equals("READ")&isReadFinished){
                    Log.e("socketRead10.0", true + "");
                    isReadFinished = false;
                    String feedback;
                    while ((feedback = reader.readLine())!=null){
                        Message msg = Message.obtain();
                        msg.obj = feedback;
                        msg.arg2 = 0;
                        ui.sendMessage(msg);
                        isReadFinished = true;
                        socketMsg = "";
                    }
                }else{
                    socket.getOutputStream().write(socketMsg.getBytes());
                    Message msg = Message.obtain();
                    msg.arg2 = 9;
                    uHandler.sendMessage(msg);
                    Log.e("socketWrite", true + "");
                }

            } catch (IOException e) {
                Message msg = Message.obtain();
                msg.arg2 = 8;
                uHandler.sendMessage(msg);
                e.printStackTrace();
            }
        }

        void prepareSocket() {

            try {

                socket = new Socket();
                InetSocketAddress inetSocketAddress = new InetSocketAddress(ip_address,port_num);
                socket.connect(inetSocketAddress,5000);
                socket.setKeepAlive(true);
                Log.e("CONNECTION", "" + socket.isConnected());
                //For TEST
                socket.getOutputStream().write("test".getBytes());
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //
                Message msg = Message.obtain();
                msg.arg2 = 1;
                uHandler.sendMessage(msg);

            } catch (IOException e) {
                e.printStackTrace();
                Message msg = Message.obtain();
                msg.arg2 = 2;
                uHandler.sendMessage(msg);
            }
        }

        void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

