package pw.ewen.mycar.video;

import android.util.Log;
import android.view.SurfaceView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 对流进行H264解码
 */
public class H264StreamDecoder implements H264StreamDecodeInterface {

    private H264NauDecoder naluDecoder = new H264NauDecoder();

    //H264文件缓存区，解码器工作缓存，要能够容纳一个nalu的容量，否则解码器会出错
    public final static int PROCESS_BUFFER_SIZE = 200000;

    //工作缓存
    private byte[] processBuffer = new byte[PROCESS_BUFFER_SIZE];

    private int beginNaluPos = -1;
    //缓存区最后一个起始码的位置
    private int endNaluPos = -1;

    private Nalu currentNalu;

    //视频输入流
    private BufferedInputStream bis;

    // 填充缓冲区
    // 返回 false 表示流中已经没有数据
    private boolean fillBuffer(InputStream bis) throws Exception {
        Log.i("Decoder", "开始填充缓冲区");

        //lastProcessPoint以后的数据移到缓冲区最前端
        if(endNaluPos > 0) {
            //缓冲区内有完整nalu
            for(int i = 0; i < PROCESS_BUFFER_SIZE - endNaluPos - 1; i++) {
                processBuffer[i] = processBuffer[endNaluPos + i + 1];
            }
            return writeFullBuffer(processBuffer, bis, endNaluPos + 1, PROCESS_BUFFER_SIZE - endNaluPos - 1);
        } else if(beginNaluPos > 0) {
            //缓冲区内只找到nalu开始代码,没有找到结束代码,将开始代码之后的全部前移,后面补充
            int j = 0;
            for(int i = beginNaluPos; i < processBuffer.length; i++) {
                processBuffer[j++] = processBuffer[i];
            }
            return writeFullBuffer(processBuffer, bis, PROCESS_BUFFER_SIZE - beginNaluPos, beginNaluPos);
        } else if(beginNaluPos == -1 && endNaluPos == -1) {
            //整体填充缓冲区
            return writeFullBuffer(processBuffer, bis, 0, PROCESS_BUFFER_SIZE);
        } else if(beginNaluPos == 0 && endNaluPos == -1) {
            throw new Exception("缓冲区内无法容纳完整nalu,请尝试扩大缓冲区.");
        } else {
            return false;
        }

    }

    // 保证足量数据存入缓存中
    // 返回false 表示流已经全部读完
    private boolean writeFullBuffer(byte[] buffer, InputStream inputStream, int offset, int readCount) throws IOException {
        int readedCount = 0;    //已经读取的字节数
        while(readedCount < readCount){
            int returnedByte = 0;
            returnedByte = inputStream.read(buffer, offset + readedCount, readCount - readedCount);
            if(returnedByte > 0){
                readedCount += returnedByte;
            } else {
                return false;
            }
        }
        return true;
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

    private NaluType checkNaluType(Nalu nalu) {
        byte[] content = nalu.getByteContent();
        for(int i = 1; i < content.length; i++) {
            if(content[i - 1] == 1) {
                int unitType = content[i] & 0x1F;
                switch (unitType) {
                    case 7:
                        return NaluType.PPS;
                    case 8:
                        return NaluType.SPS;
                    case 11:
                        return NaluType.STOP;
                    default:
                        return NaluType.Other;
                }
            }
        }
        return null;
    }

    //返回是否成功找到nalu
    private Nalu findOneNalu(byte[] buffer, int beginPos) {

        this.beginNaluPos = this.endNaluPos = -1;

        int isStartCode = -1;
        int j = 0;

        for(int i = beginPos; i< buffer.length; i++) {
            isStartCode = checkNaluStartCode(buffer, i);
            if(isStartCode > 0) {
                this.beginNaluPos = i;

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
                break;
            }
        }

        if(isStartCode > 0) {
            while(j < buffer.length) {
                if(checkNaluStartCode(buffer, j) > 0) {
                    this.endNaluPos = j - 1;

//                    Log.i("Decoder", "找到NALU");
                    Nalu nalu = new Nalu();
                    byte[] naluContent = new byte[endNaluPos - beginNaluPos + 1];
                    System.arraycopy(buffer, beginNaluPos, naluContent, 0, naluContent.length);
                    nalu.setByteContent(naluContent);
                    //判断nalu类型
                    NaluType naluType = checkNaluType(nalu);
                    if(naluType != null) {
                        nalu.setType(naluType);
                        return nalu;
                    }
                }
                j++;
            }
        }



        return null;
    }

    public void decode(InputStream iStream, SurfaceView surfaceView) throws Exception {

        //新开一个线程用于解码
        Thread decodeThread = new Thread(() -> {
            try {
                naluDecoder.decode(surfaceView);
            } catch (IOException e) {
                Log.e("Decoder", "解码过程出现异常："+e.toString());
            }
        });
        decodeThread.start();

        //填充缓冲区
        while(fillBuffer(iStream)) {

            int i = 0;
            while(i < processBuffer.length) {
                Nalu nalu = findOneNalu(this.processBuffer, i);
                if(nalu != null) {
                    //找到一个nalu,推入nalu序列准备解码
                    naluDecoder.feed(nalu);
//                    naluDecoder.decode(surfaceView);

                    if(this.endNaluPos != processBuffer.length - 1) {
                        i = this.endNaluPos + 1;
                    }
//                        Log.i("Decoder", "下一个查询位:" + String.valueOf(i));
                } else if(this.beginNaluPos != -1) {
                    break;
                } else {
                    i++;
                }
            }
        }
    }
}
