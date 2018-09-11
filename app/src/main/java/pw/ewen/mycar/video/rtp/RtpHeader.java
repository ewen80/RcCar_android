package pw.ewen.mycar.video.rtp;

//rtp头部
public class RtpHeader {

    private byte CC;
    private byte M;
    private byte PT;
    private short sequence;
    private int timestamp;

    //CC：CSRC计数器，占4位，指示CSRC 标识符的个数（作用信源CSRC计数器）
    public byte getCC() {
        return CC;
    }

    public void setCC(byte CC) {
        this.CC = CC;
    }

    //M: 标记，占1位，不同的有效载荷有不同的含义，对于视频，标记一帧的结束；对于音频，标记会话的开始。（对于分组中的重要事件可用该位标识）
    public byte getM() {
        return M;
    }

    public void setM(byte m) {
        M = m;
    }

    //PT: 有效荷载类型，占7位，用于说明RTP报文中有效载荷的类型，如GSM音频、JPEM图像等,在流媒体中大部分是用来区分音频流和视频流的，这样便于客户端进行解析。
    public byte getPT() {
        return PT;
    }

    public void setPT(byte PT) {
        this.PT = PT;
    }

    //序列号：占16位，用于标识发送者所发送的RTP报文的序列号，每发送一个报文，序列号增1。这个字段当下层的承载协议用UDP的时候，网络状况不好的时候可以用来检查丢包。
    // 同时出现网络抖动的情况可以用来对数据进行重新排序，序列号的初始值是随机的，同时音频包和视频包的sequence是分别记数的。
    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }

    //时戳(Timestamp)：占32位，必须使用90 kHz 时钟频率。时戳反映了该RTP报文的第一个八位组的采样时刻。接收者使用时戳来计算延迟和延迟抖动，并进行同步控制。
    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
