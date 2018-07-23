package com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED = 2;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE = 3;//发送消息成功
    public static final int SEND_MSG_ERROR = 4;//发送消息失败
    public static final int GET_MSG = 6;//获取新消息

    /**
     * 连接线程
     */
    private ConnectThread connectThread;

    /**
     * 监听线程
     */
    private ListenerThread listenerThread;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String s = (String)msg.obj;
            Toast.makeText(MainContext.INSTANCE.getContext(), s, Toast.LENGTH_LONG).show();
            switch (msg.what) {
                case DEVICE_CONNECTING:
                    connectThread = new ConnectThread(listenerThread.getSocket(),handler);
                    connectThread.start();
                    break;
                case DEVICE_CONNECTED:
//                    textview.setText("设备连接成功");
                    Toast.makeText(MainContext.INSTANCE.getContext(),"设备连接成功",Toast.LENGTH_LONG).show();
                    break;
                case SEND_MSG_SUCCSEE:
//                    textview.setText("发送消息成功:" + msg.getData().getString("MSG"));
                    Toast.makeText(MainContext.INSTANCE.getContext(),"发送消息成功",Toast.LENGTH_LONG).show();
                    break;
                case SEND_MSG_ERROR:
//                    textview.setText("发送消息失败:" + msg.getData().getString("MSG"));
                    Toast.makeText(MainContext.INSTANCE.getContext(),"发送消息失败",Toast.LENGTH_LONG).show();
                    break;
                case GET_MSG:
//                    textview.setText("收到消息:" + msg.getData().getString("MSG"));
                    Toast.makeText(MainContext.INSTANCE.getContext(),"收到消息",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    /*@Override
    public void run() {
        super.run();
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(9494));
            if (serverSocket != null){
                Log.d("","服务已启动，等待客户端连接");
                while (true){
                    Socket clientSocket = serverSocket.accept();
                    String remoteIp = clientSocket.getInetAddress().getHostAddress();
                    int remotePort = clientSocket.getLocalPort();
                    Log.d("FFFFF","A client connected. IP:" + remoteIp+ ", Port: " + remotePort);
                    Log.d("FFFFF","server: receiving.............");
                    // 获得 client 端的输入输出流，为进行交互做准备
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), false);
                    // 获得 client 端发送的数据
                    String tmp = in.readLine();
                    // String content = new String(tmp.getBytes("utf-8"));
                    System.out.println();
                    Log.d("FFFFF","Client message is: " + tmp);
                    // 向 client 端发送响应数据
                    out.println("Your message has been received successfully！.");
                    // 关闭各个流
//                    out.close();
//                    in.close();
                    Message message = handler.obtainMessage();
                    message.obj=tmp;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }*//*else {
                serverSocket = new ServerSocket(9494);
                while (true){
                    Socket clientSocket = serverSocket.accept();
                    String remoteIp = clientSocket.getInetAddress().getHostAddress();
                    int remotePort = clientSocket.getLocalPort();
                    Log.d("FFFFF","A client connected. IP:" + remoteIp+ ", Port: " + remotePort);
                    Log.d("FFFFF","server: receiving.............");
                    // 获得 client 端的输入输出流，为进行交互做准备
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), false);
                    // 获得 client 端发送的数据
                    String tmp = in.readLine();
                    // String content = new String(tmp.getBytes("utf-8"));
                    Log.d("FFFFF","Client message is: " + tmp);
                    // 向 client 端发送响应数据
                    out.println("Your message has been received successfully！.");
                    // 关闭各个流
//                    out.close();
//                    in.close();
//                    serverSocket.close();
                    Message message = handler.obtainMessage();
                    message.obj=tmp;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }*//*
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public void run(){
    InputStream is=null;
    InputStreamReader isr=null;
    BufferedReader br=null;
    OutputStream os=null;
    PrintWriter pw=null;
		try {
		    socket = serverSocket.accept();
        //获取输入流，并读取客户端信息
        is = socket.getInputStream();
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        String info=null;
        while((info=br.readLine())!=null){//循环读取客户端的信息
            System.out.println("我是服务器，客户端说："+info);
        }
        socket.shutdownInput();//关闭输入流
        //获取输出流，响应客户端的请求
        os = socket.getOutputStream();
        pw = new PrintWriter(os);
        pw.write("欢迎您！");
        pw.flush();//调用flush()方法将缓冲输出
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }finally{
        //关闭资源
        try {
            if(pw!=null)
                pw.close();
            if(os!=null)
                os.close();
            if(br!=null)
                br.close();
            if(isr!=null)
                isr.close();
            if(is!=null)
                is.close();
            if(socket!=null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
}
