package pw.ewen.mycar;

import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class CarCommand {

    private CarCommandEnum commandType;
    private CarMoveParam moveParam;
    private CarServerAddress carAddress;

    private DatagramSocket ds;

    public CarCommand(CarCommandEnum commandType, CarServerAddress carAddress) throws SocketException {
        this.commandType = commandType;
        this.carAddress = carAddress;

        ds = new DatagramSocket();
        ds.setSoTimeout(1000*5); //5秒超时
    }

    public CarMoveParam getMoveParam() {
        return moveParam;
    }

    public void setMoveParam(CarMoveParam moveParam) {
        this.moveParam = moveParam;
    }

    public boolean execute(){
        String commandsStr = "";

        if(this.commandType != null){
            switch (commandType){
                case Forward:
                    //前进
                    commandsStr += getForwardCommandStr(false);
                    break;
                case Reverse:
                    commandsStr += getReverseCommandStr(false);
                    break;
                case TurnLeft:
                    commandsStr += getTurnLeftCommandStr(false);
                    break;
                case TurnRight:
                    commandsStr += getTurnRightCommandStr(false);
                case Find:
                    //测试小车服务器端可否到达
                    try {
                        return findServer();
                    } catch (IOException e) {
                        return false;
                    }
                    break;
            }
            if(commandsStr.equals("")){
                return sendCommand(commandsStr);
            }
        } else {
            return false;
        }
    }

    //发送命令
    private boolean sendCommand(String commandStr){
        DatagramPacket sendDp = null;
        try {
            sendDp = new DatagramPacket(commandStr.getBytes(), commandStr.length(), this.carAddress.getInetAddress(), this.carAddress.getPort());
            try {
                ds.send(sendDp);
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (UnknownHostException e) {
            return false;
        }

    }

    //前进
    private String getForwardCommandStr(boolean sendNow){
        if(this.moveParam != null){
            String command = "F" + String.valueOf(this.moveParam.getSpeed()) + "|";
            if(sendNow){
                sendCommand(command);
            }
            return command;
        } else {
            return "";
        }

    }

    //后退
    private String getReverseCommandStr(boolean sendNow){
        if(this.moveParam != null){
            String command = "B" + String.valueOf(this.moveParam.getSpeed()) + "|";
            if(sendNow){
                sendCommand(command);
            }
            return command;
        } else{
            return "";
        }

    }

    //制动
    private String getStopCommandStr(boolean sendNow){
        String command = "S0|";
        if(sendNow){
            sendCommand(command);
        }
        return command;
    }

    //左转
    private String getTurnLeftCommandStr(boolean sendNow){
        if(this.moveParam != null){
            String command = "L" + String.valueOf(this.moveParam.getAngle()) + "|";
            if(sendNow){
                sendCommand(command);
            }
            return command;
        } else{
            return "";
        }

    }

    //右转
    private String getTurnRightCommandStr(boolean sendNow){
        if(this.moveParam != null){
            String command = "R" + String.valueOf(this.moveParam.getAngle()) + "|";
            if(sendNow){
                sendCommand(command);
            }
            return command;
        } else{
            return "";
        }
    }

    //测试服务器是否存在
    private boolean findServer() throws IOException {
        String commandStr = "W0|";

        InetAddress dstAddr = this.carAddress.getInetAddress();
        DatagramPacket sendDp = new DatagramPacket(commandStr.getBytes(), commandStr.length(), dstAddr, this.carAddress.getPort());

        byte[] recBuf = new byte[1024];
        DatagramPacket recDp = new DatagramPacket(recBuf, recBuf.length);

        //接收数据,重试3次
        int trytimes = 3;
        boolean receivedResponse = false;

        //如果没有收到回应并且重试次数小于3次
        while (!receivedResponse && trytimes > 0) {
            ds.send(sendDp);
            try {
                ds.receive(recDp);
                //判断接收到的数据是否来自发送地址
                if (recDp.getAddress().equals(dstAddr)) {
                    receivedResponse = true;
                }
            } catch (IOException e) {
                trytimes--;
            }
        }

        String recStr = new String(recDp.getData(), 0, recDp.getLength());

        return recStr.equals("next");
    }
}
