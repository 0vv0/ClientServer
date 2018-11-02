package person.os.checkers.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import person.os.clientserver.Client;
import person.os.clientserver.R;
import person.os.clientserver.Server;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements Handler.Callback {
    Server server;
    Client client;
    TextView msg;
    TextView log;
    Button ss;
    Button cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msg = findViewById(R.id.msg);
        ss = findViewById(R.id.startserver);
        log = findViewById(R.id.log);

        ss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverInitialization();
            }
        });

        cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                }
            }
        });


    }

    private void serverInitialization() {
        if (server == null || !server.isAlive()) {
            server = new Server(3550, new Handler(getMainLooper(), MainActivity.this));
            server.start();
        }
        ss.setText("Working on: " + server.getName() + ":" + server.getPort());
    }

    private void clientInitialization() {
        if (client == null || !client.isAlive()) {

            try {
                client = new Client(
                        new Socket(
                                ((TextView) findViewById(R.id.host)).getText().toString(),
                                Integer.valueOf(((TextView) findViewById(R.id.port)).getText().toString()))
                        , new Handler(this.getMainLooper(), this));
            } catch (IOException e) {
                log.setText(e.getLocalizedMessage());
            }

        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.getData().getString("key") != null) {
            log.setText(msg.getData().getString("key"));
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.interrupt();
        }
        if (client != null) {
            client.interrupt();
        }
    }
}
