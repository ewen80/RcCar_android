package pw.ewen.mycar.video;

import android.view.SurfaceView;

import java.io.InputStream;

/**
 * H264解码接口
 */
public interface H264StreamDecodeInterface {

    //解码
    void decode(InputStream inputStream, SurfaceView surfaceView) throws Exception;
}
