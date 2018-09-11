package pw.ewen.mycar.video.rtp;

//Fu-A分片 rtp包
public class RtpFuAPacket  extends RtpPacket {

    private NalHeader nalHeader;
    private FUHeader fuHeader;

    @Override
    public byte[] getNal() {
        return new byte[0];
    }

    //Fu indicator
    public NalHeader getNalHeader() {
        return nalHeader;
    }

    public void setNalHeader(NalHeader nalHeader) {
        this.nalHeader = nalHeader;
    }

    //Fu header
    public FUHeader getFuHeader() {
        return fuHeader;
    }

    public void setFuHeader(FUHeader fuHeader) {
        this.fuHeader = fuHeader;
    }
}
