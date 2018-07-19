package pw.ewen.mycar.video;

import android.util.Log;

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

    private void fillBuffer(BufferedInputStream bis) throws Exception {
        //lastProcessPoint以后的数据移到缓冲区最前端
        if(endNaluPos > 0) {
            //缓冲区内有完整nalu
            int i = 0;
            while(endNaluPos < PROCESS_BUFFER_SIZE) {
                processBuffer[i] = processBuffer[endNaluPos + i];
                i++;
            }
            bis.read(processBuffer, endNaluPos, PROCESS_BUFFER_SIZE - endNaluPos + 1);
        } else if(beginNaluPos > 0) {
            //缓冲区内只找到nalu开始代码,没有找到结束代码,将开始代码之后的全部前移,后面补充
            int j = 0;
            for(int i = beginNaluPos; i < processBuffer.length; i++) {
                processBuffer[j++] = processBuffer[i];
            }
            bis.read(processBuffer, PROCESS_BUFFER_SIZE - beginNaluPos, beginNaluPos);
        } else if(beginNaluPos == -1 && endNaluPos == -1) {
            //整体填充缓冲区
            bis.read(processBuffer, 0, PROCESS_BUFFER_SIZE );
        } else if(beginNaluPos == 0 && endNaluPos == -1) {
            throw new Exception("缓冲区内无法容纳完整nalu,请尝试扩大缓冲区.");
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

    //对单个nalu送进解码器解码
    private void naluDecode(Nalu nalu) {
        Log.i("Decode", "找到1个nalu:" + nalu.toString());
    }


    public  void decode() throws Exception {
        if(!this.readingFile) {
            readFile();
        }
        //初始化缓冲区
        fillBuffer(this.bis);

        for(int i = 0; i < processBuffer.length; i++) {
            Nalu nalu = findOneNalu(this.processBuffer, i);
            if(nalu != null) {
                //找到一个nalu
                naluDecode(nalu);
            } else {
                //没有找到nalu,继续寻找
                continue;
            }
        }


    }

}
