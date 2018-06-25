package pw.ewen.mycar;

import android.os.AsyncTask;

import java.net.DatagramSocket;
import java.net.SocketException;


/**
 * 给小车发送UDP命令
 */
class SendUDPCommandsTask extends AsyncTask<CarMoveParam, Void, Void> {

    public SendUDPCommandsTask() throws SocketException {
    }

    @Override
    protected void doInBackground(CarMoveParam... carCommands) {

    }
}
