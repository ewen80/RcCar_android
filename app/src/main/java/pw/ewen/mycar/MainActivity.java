package pw.ewen.mycar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity  {

    public static final String EXTRA_MESSAGE = "pw.ewen.demo.MESSAGE";

    DatagramSocket ds = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        try {
            ds = new DatagramSocket();


        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(View view){
        Intent intent = new Intent(this, Demo.class);
        EditText editText = (EditText)   findViewById(R.id.editText3);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    //测试服务器是否存在
    public boolean testServer(String ip, int number) {
        String testStr = "W0|";
        try {
            InetAddress dstAddr = InetAddress.getByName(ip);
            DatagramPacket sendDp = new DatagramPacket(testStr.getBytes(), testStr.length(), dstAddr, number);

            byte[] recBuf = new byte[1024];
            DatagramPacket recDp = new DatagramPacket(recBuf, recBuf.length);
            

            //接收数据
            ds.setSoTimeout(1000*5); //5秒超时
            int trytimes = 0;
            boolean receivedResponse = false;

            //如果没有收到回应并且重试次数小于3次
            while(!receivedResponse && trytimes < 3){
                ds.send(sendDp);
                try{
                    ds.receive(recDp);
                    //判断接收到的数据是否来自发送地址
                    if(recDp.getAddress().equals(dstAddr)){
                        receivedResponse = true;
                    }
                }catch (InterruptedIOException e){
                    trytimes++;
                }
            }
            

            String recStr = new String(recDp.getData(), 0, recDp.getLength());
            if(recStr.equals("here")){
                return true;
            } else {
                return false;
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void testServer_OnClick(View view) {

        String serverIp = ((EditText)findViewById(R.id.serverIP)).getText().toString();
        String serverPortStr = ((EditText)findViewById(R.id.serverPort)).getText().toString();
        int serverPortInt = Integer.getInteger(serverPortStr, 0);

        if(serverPortInt != 0){
            testServer(serverIp, serverPortInt);
        }
    }
}
