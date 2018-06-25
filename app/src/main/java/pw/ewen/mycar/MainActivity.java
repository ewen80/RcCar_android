package pw.ewen.mycar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private static final int MIN_THROTTLE = 100; //最小油門
    private static final int MAX_THROTTLE = 180; //最大油门

    private static final int MAX_ANGLE = 50; //UI最大角度表示实际角度（angle=0或者180，对应实际舵机几度）
    private static final int THROTTLE_DEVIATION = 10;   //控件strength誤差，还未经比例放大
    private static final int DIRECTION_DEVIATION = 10;   //控件方向誤差，还未经比例放大

    DatagramSocket ds = null; //阻塞性socket

    private int lastAngle = 0; //上次的角度
    private int lastSpeed = 0; //上次的速度


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
        joystick.setEnabled(false);
        joystick.setOnMoveListener((angle, strength) -> {
            tv_angle.setText(String.valueOf(angle));
            tv_strength.setText(String.valueOf(strength));

            //如果角度和速度在误差范围内则不会调用处理函数
            if(Math.abs(angle - lastAngle) > DIRECTION_DEVIATION || Math.abs(strength - lastSpeed) > THROTTLE_DEVIATION || strength == 0){
                lastAngle = angle;
                lastSpeed = strength;
                process(angle, strength);
            }

        });
    }


    //测试服务器是否存在
    public void testServer_OnClick(View view) {

        String serverIp = ((EditText)findViewById(R.id.etxt_ServerIP)).getText().toString();
        String serverPortStr = ((EditText)findViewById(R.id.etxt_ServerPort)).getText().toString();
        int serverPortInt = Integer.parseInt(serverPortStr);

        if(serverPortInt != 0){
            boolean serverIsExist = testServer(serverIp, serverPortInt);

            JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
            joystick.setEnabled(serverIsExist);
            String testResultStr = serverIsExist ? "服务器存在" : "服务器不存在";
            Toast.makeText(MainActivity.this, testResultStr, Toast.LENGTH_SHORT).show();
        }
    }

    //从虚拟摇杆的力度到速度的转换（0-100 到 MIN_THROTTLE 到 MAX_THROTTLE 的转换）
    private int strenthToSpeedTransform(int strength){
        //检查strength是否在0-100内
        if(strength >=0 && strength <= 100){
            return ((100 - strength) * MIN_THROTTLE + MAX_THROTTLE * strength) / 100;
        } else if(strength < 0){
            return MIN_THROTTLE;
        } else {
            return MAX_THROTTLE;
        }
    }

    /**
     * 从UI的操作角度到实际舵机角度的转换
     * @param angle 相对角度即和90度（270度）的夹角
     * @return  比例对应实际角度值
     */
    private int angleToDirectionTransform(int angle){
        if(angle >=0 && angle <= 90){
            return (angle * MAX_ANGLE) / 90;
        }else{
            return 0;
        }
    }

    private void process(int angle, int strength) {
//        //避免頻繁調用該函數,间隔0.5秒才能再次调用,如果是刹车则直接调用
//        if(lastProcessTime == 0){
//            lastProcessTime = System.currentTimeMillis();
//        }else{
//            if(System.currentTimeMillis() - lastProcessTime < 500 && strength > 0){
//                return;
//            }else{
//                lastProcessTime = System.currentTimeMillis();
//            }
//        }

        try {
            InetAddress dstAddr = InetAddress.getByName(etxt_ServerIP.getText().toString());

            try {
                int serverPort = Integer.parseInt(etxt_ServerPort.getText().toString());

                //检测服务器端是否发送了允许发送指令的指令
                byte[] recBuf = new byte[1024];
                DatagramPacket recDp = new DatagramPacket(recBuf, recBuf.length);

                ds.receive(recDp);
                String recStr = new String(recDp.getData(), 0, recDp.getLength());
                if(recStr.equals("next")){
                    //如果收到指令则继续
                    int carAngle, carSpeed;
                    boolean turnLeft, forward;

                    carSpeed = this.strenthToSpeedTransform(strength);

                    String commands = "";

                    if(carSpeed > MIN_THROTTLE){
                        if(angle > 0 && angle < 180){
                            forward = true;
                            carAngle = angleToDirectionTransform(Math.abs(angle - 90));
                            turnLeft = (angle - 90) > 0;
                        }else{
                            forward = false;
                            carAngle = angleToDirectionTransform(Math.abs(angle - 270));
                            turnLeft = (angle - 270) < 0;
                        }

                        if(forward){
                            commands += forward(carSpeed, dstAddr, serverPort, false);
                        }else{
                            commands += reverse(carSpeed, dstAddr, serverPort, false);
                        }

                        if(turnLeft){
                            commands += turnleft(carAngle, dstAddr, serverPort, false);
                        }else{
                            commands += turnright(carAngle, dstAddr, serverPort,false);
                        }
                    }else{
                        commands += stop(dstAddr, serverPort, false);
                    }

                    sendCommand(this.ds, commands, dstAddr, serverPort);
                }
            } catch(NumberFormatException e){
                Toast.makeText(MainActivity.this, "服务器端口号无法转换成数字", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                //接收服务器端指令时出错
            }
        } catch (UnknownHostException e) {
            Toast.makeText(MainActivity.this, "无法识别服务器", Toast.LENGTH_LONG).show();
        }


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
    private String forward(int speed, InetAddress addr, int port, boolean sendNow){
        String command = "F" + String.valueOf(speed) + "|";
        if(sendNow){
            sendCommand(this.ds, command, addr, port);
        }
        return command;
    }

    //后退
    private String reverse(int speed, InetAddress addr, int port, boolean sendNow){
        String command = "B" + String.valueOf(speed) + "|";
        if(sendNow){
            sendCommand(this.ds, command, addr, port);
        }

        return command;
    }

    //制动
    private String stop(InetAddress addr, int port, boolean sendNow){
        String command = "S0|";
        if(sendNow){
            sendCommand(this.ds, command, addr, port);
        }
        return command;
    }

    //左转
    private String turnleft(int degree, InetAddress addr, int port, boolean sendNow){
        String command = "L" + String.valueOf(degree) + "|";
        if(sendNow){
            sendCommand(this.ds, command, addr, port);
        }
        return command;
    }

    //右转
    private String turnright(int degree, InetAddress addr, int port, boolean sendNow){
        String command = "R" + String.valueOf(degree) + "|";
        if(sendNow){
            sendCommand(this.ds, command, addr, port);
        }
        return command;
    }
}
