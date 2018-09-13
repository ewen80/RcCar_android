package pw.ewen.mycar.video.rtp;

//rtp头部
public class RtpHeader {

    // 一帧的结束
    private boolean isFrameEnd;
    //负载类型（96=H264）
    private RtpPayloadType payloadType;
    private short sequence;
    private int timestamp;

    public boolean isFrameEnd() {
        return isFrameEnd;
    }

    public RtpPayloadType getPayloadType() {
        return payloadType;
    }

    public short getSequence() {
        return sequence;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public RtpHeader(byte[] headerbytes) {
        if(headerbytes.length >= 12){
            //M: 标记，占1位，不同的有效载荷有不同的含义，对于视频，标记一帧的结束；对于音频，标记会话的开始。（对于分组中的重要事件可用该位标识）
            isFrameEnd = !((headerbytes[1] & 0x80)  == 0);
            switch (headerbytes[1] & 127){
                case 96:
                    payloadType = RtpPayloadType.H264;
                    break;
                default:
                    break;
            }
            sequence = (short) (headerbytes[2] << 8 | headerbytes[3]);
            timestamp = headerbytes[4] << 32 | headerbytes[5] << 16 | headerbytes[6] << 8 | headerbytes[7];
        }

    }
}
