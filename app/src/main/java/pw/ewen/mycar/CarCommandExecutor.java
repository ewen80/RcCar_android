package pw.ewen.mycar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

/**
 * 执行控制小车命令的执行类
 */
public class CarCommandExecutor {

    private CarCommand throttleCommand, directionCommand, otherCommand;
    private DatagramSocket ds;
    private CarServerAddress carAddress;

    public CarCommandExecutor(CarServerAddress carAddress) throws SocketException {
        ds = new DatagramSocket();
        ds.setSoTimeout(1000); //1秒超时

        this.carAddress = carAddress;
    }

    public CarServerAddress getCarAddress() {
        return carAddress;
    }

    public void setCarAddress(CarServerAddress carAddress) {
        this.carAddress = carAddress;
    }

    public boolean addThrottleCommand(CarCommand command){
        CarCommandTypeEnum ccte = command.getCommandType();
        if(ccte == CarCommandTypeEnum.Forward || ccte == CarCommandTypeEnum.Reverse){
            this.throttleCommand = command;
            return true;
        } else{
            return false;
        }
    }

    public boolean addDirectionCommand(CarCommand command){
        CarCommandTypeEnum ccte = command.getCommandType();
        if(ccte == CarCommandTypeEnum.TurnLeft || ccte == CarCommandTypeEnum.TurnRight){
            this.directionCommand = command;
            return true;
        } else{
            return false;
        }
    }

    public boolean addOtherCommand(CarCommand command){
        CarCommandTypeEnum ccte = command.getCommandType();
        if(ccte != CarCommandTypeEnum.Forward && ccte != CarCommandTypeEnum.Reverse && ccte != CarCommandTypeEnum.TurnLeft && ccte != CarCommandTypeEnum.TurnRight){
            this.otherCommand = command;
            return true;
        } else{
            return false;
        }
    }

    //发送队列中的所有命令
    private void sendCommands() throws IOException {
        DatagramPacket sendDp = null;
        StringBuilder commandStrs = new StringBuilder();

        if(this.throttleCommand != null){
            commandStrs.append(this.throttleCommand.toString());
        }
        if(this.directionCommand != null){
            commandStrs.append(this.directionCommand.toString());
        }
        if(this.otherCommand != null){
            commandStrs.append(this.otherCommand.toString());
        }

        if(commandStrs.length() > 0){
            sendDp = new DatagramPacket(commandStrs.toString().getBytes(), commandStrs.toString().length(), this.carAddress.getInetAddress(), this.carAddress.getPort());
            ds.send(sendDp);
        }
    }
}
