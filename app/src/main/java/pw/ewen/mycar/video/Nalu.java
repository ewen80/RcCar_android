package pw.ewen.mycar.video;

import java.util.Arrays;

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

    //获取去除开始码的内容
    public byte[] getNoStartCodeContent() {
        for(int i = 1; i < byteContent.length; i++) {
            if(byteContent[i - 1] == 1) {
                byte[] retBytes = new byte[byteContent.length - i];
                System.arraycopy(byteContent, i, retBytes, 0, retBytes.length);
                return retBytes;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < byteContent.length; i++) {
            sb.append(String.format("%02X ", byteContent[i]));
        }
        return "Nalu{" +
                "type=" + type +
                ", byteContent=" + sb.toString() +
                '}';
    }
}
