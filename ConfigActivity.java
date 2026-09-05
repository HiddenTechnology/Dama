package com.damefeito;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ConfigActivity extends Activity {

    private Button btTabuleiro;
    private Button btCor1;
    private Button btCor2;
    private Button btIniciar;

    private MediaPlayer mpClick;

    private int tipoTabuleiro = 0;
    private int cor1 = 0;
    private int cor2 = 0;

    private String[] nomesTabuleiros;
    private String[] nomesCores;

    private SharedPreferences prefs;

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

        setContentView(R.layout.config);

        mpClick = MediaPlayer.create(this, R.raw.click);

        nomesTabuleiros = getResources().getStringArray(R.array.tabuleiros);
        nomesCores = getResources().getStringArray(R.array.cores_peca1);

        btTabuleiro = findViewById(R.id.btTabuleiro);
        btCor1 = findViewById(R.id.btCor1);
        btCor2 = findViewById(R.id.btCor2);
        btIniciar = findViewById(R.id.btIniciar);

        prefs = getSharedPreferences("config_dama", MODE_PRIVATE);
        carregarPreferencias();

        atualizarTextoBotoes();

        btTabuleiro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tocarClick();
                escolherTabuleiro();
            }
        });

        btCor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tocarClick();
                escolherCor(1);
            }
        });

        btCor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tocarClick();
                escolherCor(2);
            }
        });

        btIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tocarClick();

                salvarPreferencias();

                Intent intent = new Intent(
                    ConfigActivity.this,
                    JogoActivity.class
                );

                intent.putExtra("tipoTabuleiro", tipoTabuleiro);
                intent.putExtra("cor1", cor1);
                intent.putExtra("cor2", cor2);
                intent.putExtra("multiplayer", false);

                startActivity(intent);

                MusicaFundoHelper.pararMusicaFundo();
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

    private void carregarPreferencias() {
        tipoTabuleiro = prefs.getInt("tipoTabuleiro", 0);
        cor1 = prefs.getInt("cor1", 0);
        cor2 = prefs.getInt("cor2", 0);
    }

    private void salvarPreferencias() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("tipoTabuleiro", tipoTabuleiro);
        editor.putInt("cor1", cor1);
        editor.putInt("cor2", cor2);
        editor.apply();
    }

    private void atualizarTextoBotoes() {
        btTabuleiro.setText(
            "Tabuleiro: " + nomesTabuleiros[tipoTabuleiro]
        );

        btCor1.setText(
            "Cor Peça 1: " + nomesCores[cor1]
        );

        btCor2.setText(
            "Cor Peça 2: " + nomesCores[cor2]
        );
    }

    private void escolherTabuleiro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
        builder.setView(dialogView);

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        title.setText("Escolha o tabuleiro");

        ListView listView = dialogView.findViewById(R.id.dialogList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,
            R.layout.item_lista,
            nomesTabuleiros
        );
        listView.setAdapter(adapter);

        final AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tocarClick();
                tipoTabuleiro = position;
                atualizarTextoBotoes();
                dialog.dismiss();
            }
        });
    }

    private void escolherCor(final int jogador) {
        String[] nomes;

        if (jogador == 1) {
            nomes = getResources().getStringArray(R.array.cores_peca1);
        } else {
            nomes = getResources().getStringArray(R.array.cores_peca2);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
        builder.setView(dialogView);

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        title.setText("Escolha a cor - Peça " + jogador);

        ListView listView = dialogView.findViewById(R.id.dialogList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,
            R.layout.item_lista,
            nomes
        );
        listView.setAdapter(adapter);

        final AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tocarClick();
                if (jogador == 1) {
                    cor1 = position;
                } else {
                    cor2 = position;
                }
                atualizarTextoBotoes();
                dialog.dismiss();
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