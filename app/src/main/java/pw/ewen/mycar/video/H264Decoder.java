package pw.ewen.mycar.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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

    private MediaCodec mCodec;
    private int naluCount = 0;

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    private SurfaceView surfaceView;

    //定义视频参数
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private int videoWidth = 640;
    private int videoHeight = 480;
    private int fps = 15;


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
    //视频文件
//    private String decodeFile;

//    private boolean readingFile = false;



    private byte[] sps = null;
    private byte[] pps = null;

    private boolean codecIsInitialed = false;

    public Nalu getCurrentNalu() {
        return currentNalu;
    }

//    public String getDecodeFile() {
//        return decodeFile;
//    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

//    public void setDecodeFile(String decodeFile) {
//        this.decodeFile = decodeFile;
//    }

    public void initDecoder(byte[] sps, byte[] pps) throws IOException {

        mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                videoWidth, videoHeight);

        if(sps != null) {
            mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        }

        if(pps != null) {
            mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
        }

        mCodec.configure(mediaFormat, this.surfaceView.getHolder().getSurface(),
                null, 0);
        mCodec.start();
    }

//    private void readFile() throws FileNotFoundException {
//        File file = new File(this.decodeFile);
//        bis = new BufferedInputStream(new FileInputStream(file), PROCESS_BUFFER_SIZE);
//        try {
//            readingFile =  bis.available() > 0;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closeFile() throws IOException {
//        bis.close();
//    }


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
//            return bis.read(processBuffer, endNaluPos + 1, PROCESS_BUFFER_SIZE - endNaluPos - 1);
            return writeFullBuffer(processBuffer, bis, endNaluPos + 1, PROCESS_BUFFER_SIZE - endNaluPos - 1);
        } else if(beginNaluPos > 0) {
            //缓冲区内只找到nalu开始代码,没有找到结束代码,将开始代码之后的全部前移,后面补充
            int j = 0;
            for(int i = beginNaluPos; i < processBuffer.length; i++) {
                processBuffer[j++] = processBuffer[i];
            }
//            return bis.read(processBuffer, PROCESS_BUFFER_SIZE - beginNaluPos, beginNaluPos);
            return writeFullBuffer(processBuffer, bis, PROCESS_BUFFER_SIZE - beginNaluPos, beginNaluPos);
        } else if(beginNaluPos == -1 && endNaluPos == -1) {
            //整体填充缓冲区
//            return bis.read(processBuffer, 0, PROCESS_BUFFER_SIZE );
            return writeFullBuffer(processBuffer, bis, 0, PROCESS_BUFFER_SIZE);
        } else if(beginNaluPos == 0 && endNaluPos == -1) {
            throw new Exception("缓冲区内无法容纳完整nalu,请尝试扩大缓冲区.");
        } else {
            return false;
        }

    }

    //保证足量数据存入缓存中
    // 返回false 表示流已经全部读完
    private boolean writeFullBuffer(byte[] buffer, InputStream inputStream, int offset, int readCount) throws IOException {
        int readedCount = 0;    //已经读取的字节数
        while(readedCount < readCount){
            int returnedByte =  inputStream.read(buffer, offset + readedCount, readCount - readedCount);
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

    //对单个nalu送进解码器解码
    private void naluDecode(Nalu nalu) throws IOException {
//        Log.i("Decoder", "nalu:" + nalu.toString());

        int inputBufferIndex = mCodec.dequeueInputBuffer(-1);

        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferIndex);
            if(inputBuffer != null) {
                inputBuffer.clear();
                inputBuffer.put(nalu.getByteContent());
                mCodec.queueInputBuffer(inputBufferIndex, 0, nalu.getByteContent().length, this.naluCount * 1000000 /this.fps, 0);
            }

            // Get output buffer index
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 200);
            while (outputBufferIndex >= 0) {
                mCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 200);
            }
        }
        naluCount++;


    }


    public  void decode(InputStream iStream, SurfaceView surfaceView) throws Exception {
//        try {
            this.surfaceView = surfaceView;

//            if(!this.readingFile) {
//                Log.i("Decoder", "开始读取文件");
//                readFile();
//                this.readingFile = true;
//            }

            //填充缓冲区
            while(fillBuffer(iStream)) {

                int i = 0;
                while(i < processBuffer.length) {
                    Nalu nalu = findOneNalu(this.processBuffer, i);
                    if(nalu != null) {
                        //找到一个nalu
                        Log.i("Decoder", "beginNaluPos:"+this.beginNaluPos+",endNaluPos:"+this.endNaluPos);
                        //等待sps和pps信息后再初始化解码器
                        NaluType naluType = nalu.getType();
                        if(sps == null && naluType.equals(NaluType.SPS)) {
                            sps = nalu.getNoStartCodeContent();
                            Log.i("Decoder", "找到sps");
                        }
                        if(pps == null && naluType.equals(NaluType.PPS)) {
                            pps = nalu.getNoStartCodeContent();
                            Log.i("Decoder", "找到pps");
                        }

                        if(sps != null && pps != null) {
                            if(!codecIsInitialed) {
                                Log.i("Decoder", "初始化解码器");
                                initDecoder(sps, pps);
                                codecIsInitialed = true;
                            }

                            naluDecode(nalu);
                        }

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

            if(this.mCodec != null) {
                this.mCodec.stop();
                this.mCodec.release();
            }
    }

}
