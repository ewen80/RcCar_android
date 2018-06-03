package pw.ewen.mycar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    DatagramSocket ds = null;


    private TextView etxt_ServerIP;
    private TextView etxt_ServerPort;

    private TextView tv_angle;
    private TextView tv_strength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etxt_ServerIP = (TextView)findViewById(R.id.etxt_ServerIP);
        etxt_ServerPort = (TextView)findViewById(R.id.etxt_ServerPort);

        tv_angle = (TextView)findViewById(R.id.tv_angle);
        tv_strength = (TextView)findViewById(R.id.tv_strength);

        try {
            ds = new DatagramSocket();


        } catch (SocketException e) {
            e.printStackTrace();
        }

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener((angle, strength) -> {
            tv_angle.setText(String.valueOf(angle));
            tv_strength.setText(String.valueOf(strength));
        });
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

    //测试服务器是否存在
    public void testServer_OnClick(View view) {

        String serverIp = ((EditText)findViewById(R.id.etxt_ServerIP)).getText().toString();
        String serverPortStr = ((EditText)findViewById(R.id.etxt_ServerPort)).getText().toString();
        int serverPortInt = Integer.parseInt(serverPortStr);

        if(serverPortInt != 0){
            boolean serverIsExist = testServer(serverIp, serverPortInt);
            String testResultStr = serverIsExist ? "服务器存在" : "服务器不存在";
            Toast.makeText(MainActivity.this, testResultStr, Toast.LENGTH_SHORT).show();
        }
    }

    //从虚拟摇杆的力度到速度的转换（0-100 到 0-255 的转换）
    private int strenthToSpeedTransform(int strength){
        //检查strength是否在0-100内
        if(strength >=0 && strength <= 100){
            return (int)(strength / 100.00 * 255);
        } else if(strength < 0){
            return 0;
        } else if(strength > 100){
            return 255;
        }
    }

    private void process(int angle, int strength) {
        try {
            InetAddress dstAddr = InetAddress.getByName(etxt_ServerIP.getText().toString());

            try {
                int serverPort = Integer.parseInt(etxt_ServerPort.getText().toString());


            }catch(NumberFormatException e){
                Toast.makeText(MainActivity.this, "服务器端口号无法转换成数字", Toast.LENGTH_LONG).show();
            }
        } catch (UnknownHostException e) {
            Toast.makeText(MainActivity.this, "无法识别服务器", Toast.LENGTH_LONG).show();
        }

        int carAngle, carSpeed;
        if(angle > 0 && angle < 180){
            carAngle = Math.abs(angle - 90);
        }else{
            carAngle = Math.abs(angle - 270);
        }

        carSpeed = this.strenthToSpeedTransform(strength);



    }

    //发送命令
    private void sendCommand(DatagramSocket ds, String command, InetAddress addr, int port){
        DatagramPacket sendDp = new DatagramPacket(command.getBytes(), command.length(), addr, port);
        try {
            ds.send(sendDp);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "无法发送数据", Toast.LENGTH_LONG).show();
        }
    }

    //前进
    private void forward(int speed, InetAddress addr, int port){
        String command = "F" + String.valueOf(speed) + "|";
        sendCommand(this.ds, command, addr, port);
    }

    //后退
    private void reverse(int speed, InetAddress addr, int port){
        String command = "B" + String.valueOf(speed) + "|";
        sendCommand(this.ds, command, addr, port);
    }

    //制动
    private void stop(InetAddress addr, int port){
        String command = "S0|";
        sendCommand(this.ds, command, addr, port);
    }

    //左转
    private void turnleft(int degree, InetAddress addr, int port){
        String command = "L" + String.valueOf(degree) + "|";
        sendCommand(this.ds, command, addr, port);
    }

    //右转
    private void turnright(int degree, InetAddress addr, int port){
        String command = "R" + String.valueOf(degree) + "|";
        sendCommand(this.ds, command, addr, port);
    }
}
