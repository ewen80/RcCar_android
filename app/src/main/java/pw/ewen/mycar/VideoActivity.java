package pw.ewen.mycar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class VideoActivity extends AppCompatActivity {

    private String h264Path = "/mnt/sdcard/720pq.h264";
    private File h264File = new File(h264Path);
    private InputStream is = null;
    private FileInputStream fs = null;

    private SurfaceView mSurfaceView;
    private Button mReadButton;
    private MediaCodec mCodec;

    Thread readFileThread;
    boolean isInit = false;

    // Video Constants
    private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private final static int VIDEO_WIDTH = 1280;
    private final static int VIDEO_HEIGHT = 720;
    private final static int TIME_INTERNAL = 30;
    private final static int HEAD_OFFSET = 0;

    int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mSurfaceView = findViewById(R.id.sv1);

        if(ContextCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            play();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    play();
                } else {
                    Toast.makeText(VideoActivity.this, "没有权限", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void play() {
        if (h264File.exists()) {
            if (!isInit) {
                try {
                    initDecoder();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isInit = true;
            }

            readFileThread = new Thread(readFile);
            readFileThread.start();
        } else {
            Toast.makeText(getApplicationContext(),
                    "H264 file not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void initDecoder() throws IOException {

        mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                VIDEO_WIDTH, VIDEO_HEIGHT);
        mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
                null, 0);
        mCodec.start();
    }

    /**
     * Find H264 frame head
     *
     * @param buffer
     * @param len
     * @return the offset of frame head, return 0 if can not find one
     */
    static int findHead(byte[] buffer, int len) {
        int i;
        for (i = HEAD_OFFSET; i < len; i++) {
            if (checkHead(buffer, i))
                break;
        }
        if (i == len)
            return 0;
        if (i == HEAD_OFFSET)
            return 0;
        return i;
    }

    /**
     * Check if is H264 frame head
     *
     * @param buffer
     * @param offset
     * @return whether the src buffer is frame head
     */
    static boolean checkHead(byte[] buffer, int offset) {
        // 00 00 00 01
        if (buffer[offset] == 0 && buffer[offset + 1] == 0
                && buffer[offset + 2] == 0 && buffer[3] == 1)
            return true;
        // 00 00 01
        if (buffer[offset] == 0 && buffer[offset + 1] == 0
                && buffer[offset + 2] == 1)
            return true;
        return false;
    }

    public boolean onFrame(byte[] buf, int offset, int length) {
        Log.e("Media", "onFrame start");
        Log.e("Media", "onFrame Thread:" + Thread.currentThread().getId());
        // Get input buffer index
//		ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        int inputBufferIndex = mCodec.dequeueInputBuffer(100);

        Log.e("Media", "onFrame index:" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferIndex);
//			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount
                    * TIME_INTERNAL, 0);
            mCount++;
        } else {
            return false;
        }

        // Get output buffer index
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 100);
        while (outputBufferIndex >= 0) {
            mCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
        Log.e("Media", "onFrame end");
        return true;
    }

    Runnable readFile = new Runnable() {

        @Override
        public void run() {
            int h264Read = 0;
            int frameOffset = 0;
            // buffer是从视频文件读取数据的缓冲区
            byte[] buffer = new byte[100000];

            byte[] framebuffer = new byte[200000];
            boolean readFlag = true;
            try {

                fs = new FileInputStream(h264File);
                is = new BufferedInputStream(fs);

                while (!Thread.interrupted() && readFlag) {
                    try {
                        int length = is.available();
                        if (length > 0) {
                            // Read file and fill buffer
                            // count 从文件buffer中读取数据的字节数
                            int count = is.read(buffer);
                            Log.i("count", "" + count);
                            //
                            h264Read += count;
                            Log.d("Read", "count:" + count + " h264Read:"
                                    + h264Read);
                            // Fill frameBuffer
                            // 如果framebuffer空余空间还能装下则装入，否则framebuffer当前位置指针重置为0,从头开始复制
                            if (frameOffset + count < framebuffer.length) {
                                System.arraycopy(buffer, 0, framebuffer,
                                        frameOffset, count);
                                frameOffset += count;
                            } else {
                                frameOffset = 0;
                                System.arraycopy(buffer, 0, framebuffer,
                                        frameOffset, count);
                                frameOffset += count;
                            }

                            // Find H264 head
                            int offset = findHead(framebuffer, frameOffset);
                            Log.i("find head", " Head:" + offset);
                            while (offset > 0) {
                                if (checkHead(framebuffer, offset)) {
                                    // Fill decoder
                                    boolean flag = onFrame(framebuffer, 0, offset);
                                    if (flag) {
                                        byte[] temp = framebuffer;
                                        framebuffer = new byte[200000];
                                        System.arraycopy(temp, offset, framebuffer,
                                                0, frameOffset - offset);
                                        frameOffset -= offset;
                                        Log.e("Check", "is Head:" + offset);
                                        // Continue finding head
                                        offset = findHead(framebuffer, frameOffset);
                                    }
                                } else {

                                    offset = 0;
                                }

                            }
                            Log.d("loop", "end loop");
                        } else {
                            h264Read = 0;
                            frameOffset = 0;
                            readFlag = false;
                            // Start a new thread
                            readFileThread = new Thread(readFile);
                            readFileThread.start();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(TIME_INTERNAL);
                    } catch (InterruptedException e) {

                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    };
}
