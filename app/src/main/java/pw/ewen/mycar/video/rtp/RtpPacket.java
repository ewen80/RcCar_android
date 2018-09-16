package pw.ewen.mycar.video.rtp;

//rtp包抽象类
public abstract class RtpPacket {

    protected RtpHeader rtpHeader;
    protected NalHeader nalHeader;
    protected byte[] rtpPayload;

    public RtpPacket(byte[] rtpBytes) {
        this.rtpHeader = new RtpHeader(rtpBytes);
        int csrcCount = rtpHeader.getCSRCCount();

        this.nalHeader = new NalHeader(rtpBytes[12 + csrcCount]);
        System.arraycopy(rtpBytes, 12+csrcCount, this.rtpPayload, 0, rtpBytes.length - 12 - csrcCount);
    }

    public abstract byte[] getNal();
}
