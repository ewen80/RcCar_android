package pw.ewen.mycar.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/***
 * 解码序列中的H264 Nalu
 */
public class H264NaluDecoder {

    private LinkedList<Nalu> nalus = new LinkedList<>();

    private MediaCodec mCodec;
    private int naluIndex = 0;

    //定义视频参数
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private int videoWidth = 640;
    private int videoHeight = 480;
    private int fps = 15;

    //停止解码标志位
    private boolean stopDecode = false;
    //解码器是否已经初始化标志位
    private boolean codecIsInitialed = false;

    //pps,sps内容
    private byte[] sps = null;
    private byte[] pps = null;

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

    //对单个nalu送进解码器解码
    private void naluDecode(Nalu nalu) throws IOException {

        int inputBufferIndex = mCodec.dequeueInputBuffer(-1);

        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferIndex);
            if(inputBuffer != null) {
                inputBuffer.clear();
                inputBuffer.put(nalu.getByteContent());
                mCodec.queueInputBuffer(inputBufferIndex, 0, nalu.getByteContent().length, this.naluIndex++ * 1000000 /this.fps, 0);
            }

            // Get output buffer index
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 200);
            while (outputBufferIndex >= 0) {
                mCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 200);
            }
        }
    }

    //初始化解码器
    private void initDecoder(byte[] sps, byte[] pps, SurfaceView surfaceView) throws IOException {

        mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                videoWidth, videoHeight);

        if(sps != null) {
            mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        }

        if(pps != null) {
            mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
        }

        mCodec.configure(mediaFormat, surfaceView.getHolder().getSurface(),
                null, 0);
        mCodec.start();
    }

    //解码
    public void decode(SurfaceView surfaceView) throws IOException {

        while(!stopDecode){
            if(nalus.size() > 0){
                Nalu nalu = nalus.poll();
                if(nalu != null) {
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

                    if(pps != null && sps != null) {
                        if(!codecIsInitialed) {
                            Log.i("Decoder", "初始化解码器");
                            initDecoder(sps, pps, surfaceView);
                            codecIsInitialed = true;
                        }

                        naluDecode(nalu);
                    }
                }
            } else {
                //Nalu序列如果为空等待50毫秒继续检测
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //添加Nalu
    public void feed(Nalu nalu){
        nalus.add(nalu);
    }
}
