package pw.ewen.mycar.video;

import pw.ewen.mycar.video.rtp.RtpPayloadType;

/**
 * rtp包头部信息
 */
public class RtpHeader {

    //标记一帧的结束标志
    private boolean frameEnd = false;
    //负载类型（96=H264）
    private RtpPayloadType payloadType;
    //rtp包序列
    private int sequence;
    //时间戳
    private int timestamp;

    public RtpHeader(byte[] rtpHeader) {
        if(rtpHeader.length >= 12){
            frameEnd = !((rtpHeader[1] & 0x80)  == 0);
            switch (rtpHeader[1] & 127){
                case 96:
                    payloadType = RtpPayloadType.H264;
                    break;
                default:
                    break;
            }
            sequence = (rtpHeader[2] << 8 | rtpHeader[3]);
            timestamp = rtpHeader[4] << 32 | rtpHeader[5] << 16 | rtpHeader[6] << 8 | rtpHeader[7];
        }

    }
}
