package pw.ewen.mycar.video.rtp;

//Nal头部信息
public class NalHeader {

    private byte Type;

    /**
     0 未指定
     1 非IDR图像的编码条带
     2 编码条带数据分割块A
     3 编码条带数据分割块B
     4 编码条带数据分割块C
     5 IDR图像的编码条带
     6 辅助增强信息SEI
     7 序列参数集 sps
     8 图像参数集 pps
     9 访问单元分隔符
     10 序列结尾
     11 流结尾
     12 填充数据
     13 序列参数集扩展
     19 未分割的辅助编码图像的编码条带
     28 Rtp FU-A分片
     29 Rtp FU-B分片
     **/
    public byte getType() {
        return Type;
    }

    public void setType(byte type) {
        Type = type;
    }
}
