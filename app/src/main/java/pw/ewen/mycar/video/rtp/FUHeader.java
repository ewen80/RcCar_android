package pw.ewen.mycar.video.rtp;

//分片单元FU头
public class FUHeader {

    private boolean isBegin;
    private boolean isEnd;

    public boolean isBegin() {
        return isBegin;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public FUHeader(byte headerByte) {

        isBegin = (headerByte & 0x80) == 0x80;
        isEnd = (headerByte & 0x40) == 0x40;
    }
}
