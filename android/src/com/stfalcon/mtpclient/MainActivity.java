package com.stfalcon.mtpclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.math.BigInteger;
import java.util.Random;


public class MainActivity extends Activity {

    TCPLink tcpLink = new TCPLink();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        startService(new Intent(MainActivity.this, TCPLink.class));
        super.onCreate(savedInstanceState);
        RequestBuilder.SESSION_ID = new byte[8];
        new Random().nextBytes(RequestBuilder.SESSION_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        context = this;
        Button startService = (Button) findViewById(R.id.start);
        Button stopService = (Button) findViewById(R.id.stop);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpLink.sendReqPqRequest();
            }
        });
        return true;
    }

}
