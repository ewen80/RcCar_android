package pw.ewen.mycar.video.rtp;

//rtp包抽象类
public abstract class RtpPacket {

    protected RtpHeader rtpHeader;

    public abstract byte[] getNal();
}
