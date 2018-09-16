package pw.ewen.mycar.video.rtp;

//单Nal rtp包
public class RtpSingleNalPacket extends RtpPacket {

    public NalHeader getNalHeader() {
        return nalHeader;
    }

    public RtpSingleNalPacket(byte[] rtpBytes) {

        super(rtpBytes);
    }

    @Override
    public byte[] getNal() {
        return this.rtpPayload;
    }
}
