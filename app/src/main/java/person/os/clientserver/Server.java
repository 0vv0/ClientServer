package person.os.clientserver;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends Thread {
    private static final String TAG = Server.class.getName();
    private final Set<Client> clients = Collections.newSetFromMap(new ConcurrentHashMap<Client, Boolean>());
    private volatile int port;
    private final Handler handler;

    public Server(int port, Handler handler) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Use 0<port<=65535");
        }
        this.port = port;
        this.handler = handler;
    }

    public void addClient(Socket client) throws IOException {
        Client clientThread = new Client(client, handler);
        clients.add(clientThread);
    }

    public void removeClient(Socket client) {
        for (Client cl : clients) {
            if(cl.getAddress().equals(client.getInetAddress())){
                clients.remove(cl);
                break;
            }
        }
    }

    private static void log(Exception e) {
        Log.e(TAG, e.getLocalizedMessage());
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);
            port = ss.getLocalPort();
            while (!Thread.interrupted()) {
                clientsCleanup();
                Socket client = ss.accept();
                addClient(client);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void clientsCleanup(){
        Set<Client> clientsToRemove = new HashSet<>();
        for (Client client : clients) {
            if(!client.isAlive()){
                clientsToRemove.add(client);
            }
        }
        clients.removeAll(clientsToRemove);
    }

    @Override
    public void interrupt() {
        super.interrupt();

        for (Client client : clients) {
            client.interrupt();
        }
    }

    public synchronized int getPort() {
        return port;
    }
}
