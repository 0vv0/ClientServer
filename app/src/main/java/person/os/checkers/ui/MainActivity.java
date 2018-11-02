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

public class MainActivity extends AppCompatActivity implements Handler.Callback{
    Server server;
    Client client;
    TextView msg;
    TextView log;
    Button ss;

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
                if (server == null){
                    server = new Server(3550, new Handler(getMainLooper(), MainActivity.this));
                    server.start();
                }
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.getData().getString("key")!=null){
            log.setText(msg.getData().getString("key"));
        }

        return false;
    }
}
