package pw.ewen.mycar.video.rtp;

//单Nal rtp包
public class RtpSingleNalPacket extends RtpPacket {

    private byte[] nalBytes;
    private NalHeader nalHeader;

    public NalHeader getNalHeader() {
        return nalHeader;
    }

    public RtpSingleNalPacket(byte[] rtpBytes) {
        this.rtpHeader = new RtpHeader(rtpBytes);
        int csrcCount = rtpHeader.getCSRCCount();

        this.nalHeader = new NalHeader(rtpBytes[12 + csrcCount]);
        System.arraycopy(rtpBytes, 12+csrcCount, this.nalBytes, 0, rtpBytes.length - 12 - csrcCount);

    }

    @Override
    public byte[] getNal() {
        return nalBytes;
    }
}
