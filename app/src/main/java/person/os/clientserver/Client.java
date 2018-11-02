package person.os.clientserver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {
    private static final String TAG = Client.class.getName();

    public InetAddress getAddress() {
        return socket.getInetAddress();
    }

    private final Socket socket;
    private final Handler handler;
    private final Thread receivingThread;
    private final Thread sendingThread;
    private volatile String msg;

    public Client(Socket client, Handler handler) throws IOException {
        if (client == null || handler == null) {
            throw new IllegalArgumentException("socket and handler should not be null");
        }
        this.socket = client;
        this.handler = handler;
        this.receivingThread = new Thread(new ReceivingThread(socket.getInputStream()));
        this.sendingThread = new Thread(new SendingThread(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        receivingThread.start();
        sendingThread.start();
        while (!isInterrupted()) {
            log("ping");
            yield();
        }

    }

    public synchronized void send(String msg) {
        log("send");
        this.msg = msg;
    }

    private synchronized String getMsg() {
        return msg;
    }

    public synchronized void receive(String msg) {
        log("receive: " +msg);
        Message message = handler.obtainMessage();
        Bundle bundle = message.getData();
        bundle.putString("key", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void log(String msg) {
        Log.e(TAG, msg);
    }

    private static void log(Exception e) {
        Log.e(TAG, e.getLocalizedMessage());
    }

    @Override
    public void interrupt() {
        Log.e(TAG, "interrupted");
        super.interrupt();

        receivingThread.interrupt();
        sendingThread.interrupt();

        try {
            socket.getInputStream().close();
        } catch (IOException e) {
            log(e);
        }
        try {
            socket.getOutputStream().close();
        } catch (IOException e) {
            log(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            log(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return socket.equals(obj);
    }

    private class ReceivingThread implements Runnable {
        private final BufferedReader br;

        private ReceivingThread(InputStream clientInputStream) {
            br = new BufferedReader(new InputStreamReader(clientInputStream));
        }


        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (br.ready()) {
                        StringBuffer sb = new StringBuffer();
                        while (br.ready()) {
                            sb.append(br.readLine());
                        }
                        receive(sb.toString());
                    } else {
                        yield();
                    }
                } catch (IOException e) {
                    log(e);
                }
            }
        }
    }

    private class SendingThread implements Runnable {
        private final BufferedWriter bw;

        private SendingThread(OutputStream clientOutputStream) {
            this.bw = new BufferedWriter(new OutputStreamWriter(clientOutputStream));
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                String msg = getMsg();
                if (msg != null) {
                    try {
                        bw.write(msg);
                        bw.flush();
                        send(null);
                    } catch (IOException e) {
                        log(e);
                    }
                } else {
                    yield();
                }
            }
        }
    }
}
