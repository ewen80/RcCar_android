package pw.ewen.mycar.video;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
    负责解码H264裸流
 */
public class H264Decoder {

    private static class LazyHolder {
        private static final H264Decoder INSTANCE = new H264Decoder();
    }

    private H264Decoder(){
    }

    public static H264Decoder getInstance() {

        return LazyHolder.INSTANCE;
    }

    //H264文件缓存区，解码器工作缓存，要能够容纳一个nalu的容量，否则解码器会出错
    private final static int PROCESS_BUFFER_SIZE = 4096;
    private int beginNaluPos = -1;
    //缓存区最后一个起始码的位置
    private int endNaluPos = -1;
    private byte[] processBuffer = new byte[PROCESS_BUFFER_SIZE];
    private Nalu currentNalu;
    private BufferedInputStream bis;
    private boolean readingFile = false;


    private String decodeFile;

    public Nalu getCurrentNalu() {
        return currentNalu;
    }

    public String getDecodeFile() {
        return decodeFile;
    }

    public void setDecodeFile(String decodeFile) {
        this.decodeFile = decodeFile;
    }

    private void readFile() throws FileNotFoundException {
        File file = new File(this.decodeFile);
        bis = new BufferedInputStream(new FileInputStream(file), PROCESS_BUFFER_SIZE);
        try {
            readingFile =  bis.available() > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillBuffer() throws Exception {
        //lastProcessPoint以后的数据移到缓冲区最前端
        if(endNaluPos > 0) {
            int i = 0;
            while(endNaluPos < PROCESS_BUFFER_SIZE) {
                processBuffer[i] = processBuffer[endNaluPos + i];
                i++;
            }
            bis.read(processBuffer, endNaluPos, PROCESS_BUFFER_SIZE - endNaluPos + 1);
        } else {
            throw new Exception("缓冲区内没有完整NALU，请尝试扩大缓冲区长度。");
        }
    }


    //检查是否是开始码
    private boolean checkNaluStartCode(byte[] buffer, int beginPos) {
        // 00 00 00 01
        if(buffer[beginPos] == 0 && buffer[beginPos + 1] == 0
                && buffer[beginPos + 2] == 0 && buffer[beginPos + 3] == 1) {
            return true;
        }

        // 00 00 01
        if(buffer[beginPos] == 0 && buffer[beginPos + 1] == 0
                && buffer[beginPos + 2] == 1) {
            return true;
        }

        return false;
    }

    //返回是否成功找到nalu
    private Nalu findOneNalu(byte[] buffer, int beginPos) {
        for(int i = beginPos; i < buffer.length; i++) {
            if(checkNaluStartCode(buffer, i)) {
                this.beginNaluPos = i;

                for(int j = i; j < buffer.length; j++) {
                    if(checkNaluStartCode(buffer, j)) {
                        this.endNaluPos = j;

                        Nalu nalu = new Nalu();
                        byte[] naluContent = new byte[endNaluPos - beginPos + 1];
                        System.arraycopy(buffer, beginNaluPos, naluContent, 0, naluContent.length);
                        nalu.setByteContent(naluContent);
                        return nalu;
                    }
                }
                break;
            }
        }
        return null;
    }

}
