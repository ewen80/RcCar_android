package pw.ewen.mycar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

/**
 * 执行控制小车命令的执行类
 * 单例模式-饿汉式
 */
public class CarCommandExecutor {

    private static final CarCommandExecutor instance = new CarCommandExecutor();

    private volatile CarCommand  command;

    private CarCommandExecutor() {
    }

    public static  CarCommandExecutor getInstance(){
        return instance;
    }

    public void addCommand(CarCommand command){
        this.command = command;
    }

    //清空命令
    public void clearCommands(){
        this.command =  null;
    }

    //发送队列中的所有命令
    public void sendCommand(DatagramSocket ds, CarServerAddress carAddress) throws IOException {
        if(this.command != null){
            String commandStr = this.command.toString();
            DatagramPacket sendDp = new DatagramPacket(commandStr.getBytes(), commandStr.length(), carAddress.getInetAddress(), carAddress.getPort());
            ds.send(sendDp);
            this.command = null;
        }
    }

    //发送需要返回值的命令
    public String sendCallbackCommand(DatagramSocket ds, CarServerAddress carAddress, CarCommand command, int trytimes) throws IOException {

        byte[] recBuf = new byte[1024];
        DatagramPacket recDp = new DatagramPacket(recBuf, recBuf.length);

        boolean receivedResponse = false;

        ds.send(new DatagramPacket("N0|".getBytes(), "N0|".length(), carAddress.getInetAddress(), carAddress.getPort()));
        while(true){
            try{
                ds.receive(recDp);
            } catch(SocketTimeoutException e){
                //一旦服务器端停止发送next信息，则发送正式指令，否则等待
                break;
            }
        }

        while (!receivedResponse && trytimes > 0) {
            if(command != null) {
                ds.send(new DatagramPacket(command.toString().getBytes(), command.toString().length(), carAddress.getInetAddress(), carAddress.getPort()));
                try {
                    ds.receive(recDp);
                    //判断接收到的数据是否来自发送地址
                    if (recDp.getAddress().equals(carAddress.getInetAddress())) {
                        receivedResponse = true;
                    }
                } catch (IOException e) {
                    trytimes--;
                }
            }

        }

        ds.send(new DatagramPacket("N1|".getBytes(), "N1|".length(), carAddress.getInetAddress(), carAddress.getPort()));

        return new String(recDp.getData(), 0, recDp.getLength());
    }
}
