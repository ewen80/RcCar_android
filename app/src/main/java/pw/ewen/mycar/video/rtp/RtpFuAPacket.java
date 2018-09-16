package pw.ewen.mycar.video.rtp;

//Fu-A分片 rtp包
public class RtpFuAPacket  extends RtpPacket {

    private FUHeader fuHeader;

    //Fu indicator
    public NalHeader getNalHeader() {
        return nalHeader;
    }


    //Fu header
    public FUHeader getFuHeader() {
        return fuHeader;
    }

    public RtpFuAPacket(byte[] rtpBytes) {

        super(rtpBytes);
        //构造FuHeader
        if(this.rtpPayload.length > 2){
            this.fuHeader = new FUHeader(this.rtpPayload[1]);
        }

    }

    //返回数据中不包含nal header
    @Override
    public byte[] getNal() {
        byte[] nalBytes = new byte[this.rtpPayload.length - 2];
        System.arraycopy(this.rtpPayload, 2, nalBytes, 0, nalBytes.length);
        return nalBytes;
    }
}
