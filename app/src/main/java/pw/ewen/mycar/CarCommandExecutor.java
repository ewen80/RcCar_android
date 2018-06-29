package pw.ewen.mycar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * 执行控制小车命令的执行类
 * 单例模式-饿汉式
 */
public class CarCommandExecutor {

    private static final CarCommandExecutor instance = new CarCommandExecutor();

    private CarCommand  command;

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
    public void sendCommands(DatagramSocket ds, CarServerAddress carAddress) throws IOException {
        if(this.command != null){
            String commandStr = this.command.toString();
            DatagramPacket sendDp = new DatagramPacket(commandStr.getBytes(), commandStr.length(), carAddress.getInetAddress(), carAddress.getPort());
            ds.send(sendDp);
        }
    }

    //发送需要返回值的命令
    public String sendCallbackCommand(DatagramSocket ds, CarServerAddress carAddress) throws IOException {
        sendCommands(ds, carAddress);
    }
}
