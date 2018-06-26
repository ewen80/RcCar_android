package pw.ewen.mycar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * 执行控制小车命令的执行类
 */
public class CarCommandExecutor {

    private LinkedList<CarCommand> commands = new LinkedList<>();
    private DatagramSocket ds;
    private CarServerAddress carAddress;

    public CarCommandExecutor(CarServerAddress carAddress) throws SocketException {
        ds = new DatagramSocket();
        ds.setSoTimeout(1000*5); //5秒超时

        this.carAddress = carAddress;
    }

    public CarServerAddress getCarAddress() {
        return carAddress;
    }

    public void setCarAddress(CarServerAddress carAddress) {
        this.carAddress = carAddress;
    }

    public LinkedList<CarCommand> getCommands() {
        return commands;
    }

    public void pushCommand(CarCommand command){
        this.commands.offer(command);
    }

    //发送队列中的所有命令
    private void sendCommands() throws IOException {
        DatagramPacket sendDp = null;
        StringBuilder commandStrs = new StringBuilder();

        CarCommand command;
        do{
            command = commands.poll();
            if(command != null){
                commandStrs.append(command.toString());
            }
        } while(command != null);
        if(commandStrs.length() > 0){
            sendDp = new DatagramPacket(commandStrs.toString().getBytes(), commandStrs.toString().length(), this.carAddress.getInetAddress(), this.carAddress.getPort());
            ds.send(sendDp);
        }
    }
}
