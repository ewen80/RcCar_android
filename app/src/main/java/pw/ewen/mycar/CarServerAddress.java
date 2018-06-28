package pw.ewen.mycar;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CarServerAddress {

    private String ip;
    private int port;

    public CarServerAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(ip);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
