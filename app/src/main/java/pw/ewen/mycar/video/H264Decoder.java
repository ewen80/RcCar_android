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

    private void closeFile() throws IOException {
        bis.close();
    }

    private int fillBuffer(BufferedInputStream bis) throws Exception {
        Log.i("Decoder", "开始填充缓冲区");

        //lastProcessPoint以后的数据移到缓冲区最前端
        if(endNaluPos > 0) {
            //缓冲区内有完整nalu
            for(int i = 0; i < PROCESS_BUFFER_SIZE - endNaluPos - 1; i++) {
                processBuffer[i] = processBuffer[endNaluPos + i + 1];
            }
            return bis.read(processBuffer, endNaluPos + 1, PROCESS_BUFFER_SIZE - endNaluPos - 1);
        } else if(beginNaluPos > 0) {
            //缓冲区内只找到nalu开始代码,没有找到结束代码,将开始代码之后的全部前移,后面补充
            int j = 0;
            for(int i = beginNaluPos; i < processBuffer.length; i++) {
                processBuffer[j++] = processBuffer[i];
            }
            return bis.read(processBuffer, PROCESS_BUFFER_SIZE - beginNaluPos, beginNaluPos);
        } else if(beginNaluPos == -1 && endNaluPos == -1) {
            //整体填充缓冲区
            return bis.read(processBuffer, 0, PROCESS_BUFFER_SIZE );
        } else if(beginNaluPos == 0 && endNaluPos == -1) {
            throw new Exception("缓冲区内无法容纳完整nalu,请尝试扩大缓冲区.");
        } else {
            return -1;
        }

    }


    //检查是否是开始码
    //return 0 : 不是开始码 1: 00 00 00 01开始码 2: 00 00 01开始码
    private int checkNaluStartCode(byte[] buffer, int beginPos) {
        // 00 00 00 01
        if(beginPos + 3 < this.processBuffer.length) {
            if(buffer[beginPos] == 0 && buffer[beginPos + 1] == 0
                    && buffer[beginPos + 2] == 0 && buffer[beginPos + 3] == 1) {
                return 1;
            }
        }

        // 00 00 01
        if(beginPos + 2 < this.processBuffer.length) {
            if(buffer[beginPos] == 0 && buffer[beginPos + 1] == 0
                    && buffer[beginPos + 2] == 1) {
                return 2;
            }
        }

        return 0;
    }

    //返回是否成功找到nalu
    private Nalu findOneNalu(byte[] buffer, int beginPos) {
        Log.i("Decoder", "寻找NALU: beginPos"+String.valueOf(beginPos));

        for(int i = beginPos; i < buffer.length; i++) {
            int isStartCode = checkNaluStartCode(buffer, i);
            if(isStartCode > 0) {
                this.beginNaluPos = i;

                int j;
                switch (isStartCode) {
                    case 1:
                        j = i + 4;
                        break;
                    case 2:
                        j = i + 3;
                        break;
                    default:
                        j = -1;
                        break;
                }

                while(j < buffer.length) {
                    if(checkNaluStartCode(buffer, j) > 0) {
                        this.endNaluPos = j - 1;

                        Log.i("Decoder", "找到NALU");
                        Nalu nalu = new Nalu();
                        byte[] naluContent = new byte[endNaluPos - i + 1];
                         System.arraycopy(buffer, beginNaluPos, naluContent, 0, naluContent.length);
                        nalu.setByteContent(naluContent);
                        return nalu;
                    }
                    j++;
                }

            }
        }
        return null;
    }

    //对单个nalu送进解码器解码
    private void naluDecode(Nalu nalu) {
        Log.i("Decoder", "找到1个nalu:" + nalu.toString());
    }


    public  void decode() throws Exception {
        if(!this.readingFile) {
            Log.i("Decoder", "开始读取文件");
            readFile();
            this.readingFile = true;
        }

        //填充缓冲区
        while(fillBuffer(this.bis) != -1) {
            this.beginNaluPos = this.endNaluPos = -1;
            for(int i = 0; i < processBuffer.length; i++) {
                Nalu nalu = findOneNalu(this.processBuffer, i);
                if(nalu != null) {
                    //找到一个nalu
                    naluDecode(nalu);

                    if(this.endNaluPos != processBuffer.length - 1) {
                        i = this.endNaluPos + 1;
                    }
                }
            }
        }

        if(this.readingFile) {
            closeFile();
            this.readingFile = false;
            Log.i("Decoder", "文件读取完毕");
        }
    }

}
