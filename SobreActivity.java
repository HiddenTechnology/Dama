package com.damefeito;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class SobreActivity extends Activity {

    private Button btVoltar;
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

        setContentView(R.layout.sobre);

        mpClick = MediaPlayer.create(this, R.raw.click);

        btVoltar = findViewById(R.id.btVoltar);

        btVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tocarClick();
                finish();
            }
        });
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