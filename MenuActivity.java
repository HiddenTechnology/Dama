package com.damefeito;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MenuActivity extends Activity {

    private Button btIniciar;
    private Button btSobre;
    private Button btWifiMultiplayer;

    private MediaPlayer mpClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Esconde barra de navegação (modo imersivo)
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        setContentView(R.layout.menu);

        mpClick = MediaPlayer.create(this, R.raw.click);

        if (!MusicaFundoHelper.isTocando()) {
            MusicaFundoHelper.iniciarMusicaFundo(this);
        }

        btIniciar = findViewById(R.id.btIniciar);
        btSobre = findViewById(R.id.btSobre);

        btIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tocarClick();

                Intent intent = new Intent(
                    MenuActivity.this,
                    ConfigActivity.class
                );
                startActivity(intent);
            }
        });

        btSobre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tocarClick();

                Intent intent = new Intent(
                    MenuActivity.this,
                    SobreActivity.class
                );
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!MusicaFundoHelper.isTocando()) {
            MusicaFundoHelper.iniciarMusicaFundo(this);
        }
    }

    private void tocarClick() {
        if (mpClick != null) {
            mpClick.seekTo(0);
            mpClick.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mpClick != null) {
            mpClick.release();
            mpClick = null;
        }
    }
}