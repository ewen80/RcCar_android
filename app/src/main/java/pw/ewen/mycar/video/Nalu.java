package pw.ewen.mycar.video;

/*
    H264的一个Nalu即网络传输的基本单位以0x00000001 或者 0x000001开头
 */
public class Nalu {

    public Nalu() {
    }

    private NaluType type = NaluType.Other;
    private byte[] byteContent;

    public byte[] getByteContent() {
        return byteContent;
    }

    public void setByteContent(byte[] byteContent) {
        this.byteContent = byteContent;
    }

    public NaluType getType() {
        return type;
    }

    public void setType(NaluType type) {
        this.type = type;
    }


}
