package pw.ewen.mycar.video.rtp;

//单Nal rtp包
public class RtpSingleNalPacket extends RtpPacket {

    private NalHeader nalHeader;

    public NalHeader getNalHeader() {
        return nalHeader;
    }

    public void setNalHeader(NalHeader nalHeader) {
        this.nalHeader = nalHeader;
    }

    @Override
    public byte[] getNal() {
        return new byte[0];
    }
}
