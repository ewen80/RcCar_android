package pw.ewen.mycar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import pw.ewen.mycar.command.CarCommand;
import pw.ewen.mycar.command.CarCommandExecutor;
import pw.ewen.mycar.command.CarCommandTypeEnum;
import pw.ewen.mycar.command.CarMoveParam;

public class MainActivity extends AppCompatActivity {

    private static final int THROTTLE_DEVIATION = 10;   //控件strength誤差，还未经比例放大
    private static final int DIRECTION_DEVIATION = 10;   //控件方向誤差，还未经比例放大

    private DatagramSocket ds = null; //阻塞性socket
    private CarServerAddress carAddress = null;

    private volatile boolean stopSendCommand = false; //中断指令发送

    private int lastAngle = 0; //上次的角度
    private int lastSpeed = 0; //上次的速度


    private TextView etxt_ServerIP;
    private TextView etxt_ServerPort;

    private TextView tv_angle;
    private TextView tv_strength;

    private CarCommand carCommand = new CarCommand();
    private CarMoveParam carMoveParam = new CarMoveParam();

    CarCommandExecutor carCommandExecutor = CarCommandExecutor.getInstance();


    private class ConfirmServerTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            String serverIP = etxt_ServerIP.getText().toString();
            int port = Integer.parseInt(etxt_ServerPort.getText().toString());

            carAddress = new CarServerAddress(serverIP, port);

            if(ds != null) {
                try {
                    CarCommand findCommand = new CarCommand();
                    findCommand.setCommandType(CarCommandTypeEnum.Find);

                    stopSendCommand = true;
                    String result = carCommandExecutor.sendCallbackCommand(ds, carAddress,findCommand, 3);
                    stopSendCommand = false;
                    return result.equals("here");
                } catch (IOException e) {
                    return false;
                }
            } else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            JoystickView joystick = findViewById(R.id.joystickView);
            joystick.setEnabled(result);
            String testResultStr = result ? "服务器存在" : "服务器不存在";
            Toast.makeText(MainActivity.this, testResultStr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etxt_ServerIP = findViewById(R.id.etxt_ServerIP);
        etxt_ServerPort = findViewById(R.id.etxt_ServerPort);

        tv_angle = findViewById(R.id.tv_angle);
        tv_strength = findViewById(R.id.tv_strength);

        try {
            ds = new DatagramSocket();
            ds.setSoTimeout(2000);

            //启动一个线程发送命令
            Thread sendThread = new Thread(()->{
                while(true){
                    if(!stopSendCommand){
                        //检测服务器端是否发送了允许发送指令的指令
                        byte[] recBuf = new byte[1024];
                        DatagramPacket recDp = new DatagramPacket(recBuf, recBuf.length);

                        try {
                            ds.receive(recDp);
                        } catch (IOException e) {
                            continue;
                        }
                        String recStr = new String(recDp.getData(), 0, recDp.getLength());
                        if(recStr.equals("next") && carAddress != null){
                            try {
                                carCommandExecutor.sendCommand(ds, carAddress);
                            } catch (IOException e) {
                                continue;
                            }
                        }
                    }

                }

            });

            sendThread.start();



        } catch (SocketException e) {
            e.printStackTrace();
        }

        JoystickView joystick = findViewById(R.id.joystickView);
        joystick.setEnabled(false);
        joystick.setOnMoveListener((angle, strength) -> {
            tv_angle.setText(String.valueOf(angle));
            tv_strength.setText(String.valueOf(strength));

            //如果角度和速度在误差范围内则不会调用处理函数
            if(Math.abs(angle - lastAngle) > DIRECTION_DEVIATION || Math.abs(strength - lastSpeed) > THROTTLE_DEVIATION || strength == 0){
                if((strength == 0 && lastSpeed != 0) || strength != 0) {
                    lastAngle = angle;
                    lastSpeed = strength;
                    addCarMoveCommand(angle, strength);
                }
            }
        });
    }




    //连接服务器
    public void connectServer_OnClick(View view) {

        new ConfirmServerTask().execute();
    }

    //播放视频点击按钮
    public void playVideoButton_OnClick(View view) {
        Intent playVideoIntent = new Intent(this, VideoActivity.class);
        startActivity(playVideoIntent);
    }



    //写入命令
    //参数:  angle 摇杆角度
    //      strength 摇杆力度
    private void addCarMoveCommand(int angle, int strength) {
        this.carMoveParam.transformJoystickParam(strength, angle);
        carCommand.setMoveParam(carMoveParam);
        carCommandExecutor.addCommand(carCommand);
    }
}
