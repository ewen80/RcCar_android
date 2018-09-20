package pw.ewen.mycar.video.rtp;

//单Nal rtp包
public class RtpSingleNalPacket extends RtpPacket {

    public NalHeader getNalHeader() {
        return nalHeader;
    }

    public RtpSingleNalPacket(byte[] rtpBytes) {

        super(rtpBytes);
    }

    //返回除了rtp头以外的所有内容,包含nal头
    @Override
    public byte[] getNal() {
        return this.rtpPayload;
    }
}
