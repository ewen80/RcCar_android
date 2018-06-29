package pw.ewen.mycar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    private static final int THROTTLE_DEVIATION = 10;   //控件strength誤差，还未经比例放大
    private static final int DIRECTION_DEVIATION = 10;   //控件方向誤差，还未经比例放大

    DatagramSocket ds = null; //阻塞性socket

    private int lastAngle = 0; //上次的角度
    private int lastSpeed = 0; //上次的速度


    private TextView etxt_ServerIP;
    private TextView etxt_ServerPort;

    private TextView tv_angle;
    private TextView tv_strength;

    CarCommandExecutor carCommandExecutor = CarCommandExecutor.getInstance();

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
            String serverIP = etxt_ServerIP.getText().toString();
            int port = Integer.parseInt(etxt_ServerIP.getText().toString());

            CarServerAddress carAddress = new CarServerAddress(serverIP, port);

            //启动一个线程发送命令
            Thread sendThread = new Thread(()->{
                //检测服务器端是否发送了允许发送指令的指令
                byte[] recBuf = new byte[1024];
                DatagramPacket recDp = new DatagramPacket(recBuf, recBuf.length);

                try {
                    ds.receive(recDp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String recStr = new String(recDp.getData(), 0, recDp.getLength());
                if(recStr.equals("next")){
                    try {
                        carCommandExecutor.sendCommands(ds, carAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            sendThread.start();


        } catch (SocketException e) {
            e.printStackTrace();
        }

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setEnabled(false);
        joystick.setOnMoveListener((angle, strength) -> {
            tv_angle.setText(String.valueOf(angle));
            tv_strength.setText(String.valueOf(strength));

            //如果角度和速度在误差范围内则不会调用处理函数
            if(Math.abs(angle - lastAngle) > DIRECTION_DEVIATION || Math.abs(strength - lastSpeed) > THROTTLE_DEVIATION || strength == 0){
                lastAngle = angle;
                lastSpeed = strength;
                addCommand(angle, strength);
            }
        });
    }




    //测试服务器是否存在
    public void testServer_OnClick(View view) {

        String serverIp = etxt_ServerIP.getText().toString();
        String serverPortStr = etxt_ServerPort.getText().toString();

        int serverPortInt = Integer.parseInt(serverPortStr);

        if(serverPortInt != 0){
            boolean serverIsExist = testServer(serverIp, serverPortInt);

            JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
            joystick.setEnabled(serverIsExist);
            String testResultStr = serverIsExist ? "服务器存在" : "服务器不存在";
            Toast.makeText(MainActivity.this, testResultStr, Toast.LENGTH_SHORT).show();
        }
    }



    //写入命令
    private void addCommand(int angle, int strength) {
        CarCommand carCommand = new CarCommand();
        CarMoveParam carMoveParam = new CarMoveParam(strength, angle);
        carCommand.setMoveParam(carMoveParam);
        carCommandExecutor.addCommand(carCommand);
    }
}
