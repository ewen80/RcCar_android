package pw.ewen.mycar.video.rtp;

//rtp包抽象类
public abstract class RtpPacket {

    private RtpHeader header;

    public RtpHeader getHeader() {
        return header;
    }

    public void setHeader(RtpHeader header) {
        this.header = header;
    }

    public abstract byte[] getNal();
}
