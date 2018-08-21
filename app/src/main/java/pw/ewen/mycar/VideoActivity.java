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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import pw.ewen.mycar.video.H264Decoder;

public class VideoActivity extends AppCompatActivity {

    private String h264Path = "/mnt/sdcard/video_ih.h264";

    private SurfaceView sv1;

    private H264Decoder decoder;

    Thread playStreamThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        sv1 = findViewById(R.id.sv1);
        sv1.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(ContextCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    play();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });


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
        decoder = H264Decoder.getInstance();

        playStreamThread = new Thread(playStream);
        playStreamThread.start();
    }



    Runnable playStream = new Runnable() {

        @Override
        public void run() {
//            decoder.setDecodeFile(h264Path);

            try {
                Socket socket = new Socket("192.168.3.24", 7777);
                InputStream inputStream = socket.getInputStream();
                try {
                    decoder.decode(inputStream, sv1);
                } finally {
                    inputStream.close();
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DecoderError", e.toString());
            }
        }
    };
}
