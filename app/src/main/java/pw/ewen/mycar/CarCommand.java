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


    public CarCommand(CarCommandEnum commandType) throws SocketException {
        this.commandType = commandType;
    }

    public CarMoveParam getMoveParam() {
        return moveParam;
    }

    public void setMoveParam(CarMoveParam moveParam) {
        this.moveParam = moveParam;
    }


    @Override
    public String toString(){
        if(this.commandType != null){
            switch (commandType){
                case Forward:
                    //前进
                    return getForwardCommandStr();
                case Reverse:
                    //后退
                    return getReverseCommandStr();
                case TurnLeft:
                    //左传
                    return getTurnLeftCommandStr();
                case TurnRight:
                    //右转
                    return getTurnRightCommandStr();
                case Stop:
                    //制动
                    return getStopCommandStr();
                default:
                    return "";
            }

        } else {
            return "";
        }
    }

    //前进
    private String getForwardCommandStr(){
        if(this.moveParam != null){
            return "F" + String.valueOf(this.moveParam.getSpeed()) + "|";
        } else {
            return "";
        }

    }

    //后退
    private String getReverseCommandStr(){
        if(this.moveParam != null){
            return "B" + String.valueOf(this.moveParam.getSpeed()) + "|";
        } else{
            return "";
        }

    }

    //制动
    private String getStopCommandStr(){
        return "S0|";
    }

    //左转
    private String getTurnLeftCommandStr(){
        if(this.moveParam != null){
            return "L" + String.valueOf(this.moveParam.getAngle()) + "|";
        } else{
            return "";
        }

    }

    //右转
    private String getTurnRightCommandStr(){
        if(this.moveParam != null){
            return "R" + String.valueOf(this.moveParam.getAngle()) + "|";
        } else{
            return "";
        }
    }

    //测试服务器是否存在
//    private boolean findServer() throws IOException {
////        String commandStr = "W0|";
////
////        InetAddress dstAddr = this.carAddress.getInetAddress();
////        DatagramPacket sendDp = new DatagramPacket(commandStr.getBytes(), commandStr.length(), dstAddr, this.carAddress.getPort());
////
////        byte[] recBuf = new byte[1024];
////        DatagramPacket recDp = new DatagramPacket(recBuf, recBuf.length);
////
////        //接收数据,重试3次
////        int trytimes = 3;
////        boolean receivedResponse = false;
////
////        //如果没有收到回应并且重试次数小于3次
////        while (!receivedResponse && trytimes > 0) {
////            ds.send(sendDp);
////            try {
////                ds.receive(recDp);
////                //判断接收到的数据是否来自发送地址
////                if (recDp.getAddress().equals(dstAddr)) {
////                    receivedResponse = true;
////                }
////            } catch (IOException e) {
////                trytimes--;
////            }
////        }
////
////        String recStr = new String(recDp.getData(), 0, recDp.getLength());
////
////        return recStr.equals("next");
////    }
}
