package pw.ewen.mycar.video.rtp;

//分片单元FU头
public class FUHeader {

    private byte S;
    private byte E;
    private byte type;

    //S: 1 bit 当设置成1,开始位指示分片NAL单元的开始。当跟随的FU荷载不是分片NAL单元荷载的开始，开始位设为0。
    public byte getS() {
        return S;
    }

    public void setS(byte s) {
        S = s;
    }

    //E: 1 bit 当设置成1, 结束位指示分片NAL单元的结束，即, 荷载的最后字节也是分片NAL单元的最后一个字节。当跟随的 FU荷载不是分片NAL单元的最后分片,结束位设置为0。
    public byte getE() {
        return E;
    }

    public void setE(byte e) {
        E = e;
    }

    //原始Nal type
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
