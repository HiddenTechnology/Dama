package com.damefeito;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class JogoActivity extends Activity
    implements Tabuleiro.OnTurnoMudouListener, Tabuleiro.OnFimDeJogoListener {

    public static Handler jogoHandler;
    private static JogoActivity atividadeAtual;

    private TextView tvTurno1;
    private TextView tvTurno2;
    private TabuleiroView tabuleiro;
    private ImageButton btVoltar;

    private boolean multiplayer = false;
    private int meuJogador = 1;

    private android.media.MediaPlayer mpAlerta;
    private android.media.MediaPlayer mpVitoria;
    private Handler handler;
    private Runnable runnableAlerta;

    private boolean temCapturaDisponivel = false;
    private boolean alertaJaTocouNestaVez = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        atividadeAtual = this;

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

        setContentView(R.layout.jogo);

        tvTurno1 = findViewById(R.id.tvTurno1);
        tvTurno2 = findViewById(R.id.tvTurno2);
        tabuleiro = findViewById(R.id.tabuleiro);
        btVoltar = findViewById(R.id.btVoltar);

        multiplayer = getIntent().getBooleanExtra("multiplayer", false);
        meuJogador = getIntent().getIntExtra("meuJogador", 1);

        int tipoTabuleiro = getIntent().getIntExtra("tipoTabuleiro", 0);
        int cor1 = getIntent().getIntExtra("cor1", 0);
        int cor2 = getIntent().getIntExtra("cor2", 0);

        tabuleiro.configurarCores(tipoTabuleiro, cor1, cor2);
        tabuleiro.setOnTurnoMudouListener(this);
        tabuleiro.setOnFimDeJogoListener(this);
        tabuleiro.setMultiplayer(multiplayer, meuJogador);

        mpAlerta = android.media.MediaPlayer.create(this, R.raw.alerta);
        mpAlerta.setLooping(false);

        mpVitoria = android.media.MediaPlayer.create(this, R.raw.vitoria);
        mpVitoria.setLooping(false);

        handler = new Handler();

        atualizarTextoTurno(tabuleiro.getTabuleiro().getTurno());

        btVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        jogoHandler = new Handler();

        iniciarVezComPossivelAlerta();
    }

    public static void aplicarMovimentoRemoto(final int lOrigem, final int cOrigem,
                                              final int lDestino, final int cDestino) {
        if (atividadeAtual != null && atividadeAtual.tabuleiro != null) {
            atividadeAtual.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    atividadeAtual.tabuleiro.aplicarMovimentoRemoto(
                        lOrigem, cOrigem, lDestino, cDestino
                    );
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        atividadeAtual = null;
        if (mpAlerta != null) {
            mpAlerta.release();
            mpAlerta = null;
        }
        if (mpVitoria != null) {
            mpVitoria.release();
            mpVitoria = null;
        }
    }

    private void atualizarTextoTurno(int turno) {
        if (multiplayer) {
            if (turno == meuJogador) {
                tvTurno1.setText("Seu turno!");
                tvTurno2.setText("Turno do adversário");
            } else {
                tvTurno1.setText("Turno do adversário");
                tvTurno2.setText("Seu turno!");
            }
        } else {
            if (turno == 1) {
                tvTurno1.setText("Turno: Jogador 1");
                tvTurno2.setText("Turno: Jogador 2");
            } else {
                tvTurno1.setText("Turno: Jogador 2");
                tvTurno2.setText("Turno: Jogador 1");
            }
        }
    }

    @Override
    public void onTurnoMudou(int novoTurno) {
        atualizarTextoTurno(novoTurno);
        iniciarVezComPossivelAlerta();
    }

    @Override
    public void onFimDeJogo(int vencedor) {
        cancelarAlertaSeExistir();

        if (mpVitoria != null) {
            mpVitoria.seekTo(0);
            mpVitoria.start();
        }

        String mensagem;
        if (multiplayer) {
            if (vencedor == meuJogador) {
                mensagem = "Você venceu!";
            } else {
                mensagem = "Você perdeu!";
            }
        } else {
            if (vencedor == 1) {
                mensagem = "Jogador 1 venceu!";
            } else {
                mensagem = "Jogador 2 venceu!";
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_fim_jogo, null);
        builder.setView(dialogView);

        TextView titulo = dialogView.findViewById(R.id.dialogFimTitulo);
        TextView textoMensagem = dialogView.findViewById(R.id.dialogFimMensagem);
        Button botao = dialogView.findViewById(R.id.dialogFimBotao);

        textoMensagem.setText(mensagem);

        final AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }

        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void iniciarVezComPossivelAlerta() {
        cancelarAlertaSeExistir();

        int[][] tab = tabuleiro.getTabuleiro().getTabuleiro();
        int turno = tabuleiro.getTabuleiro().getTurno();

        temCapturaDisponivel = existeCapturaDisponivelParaJogador(tab, turno);
        alertaJaTocouNestaVez = false;

        if (temCapturaDisponivel) {
            runnableAlerta = new Runnable() {
                @Override
                public void run() {
                    if (temCapturaDisponivel && !alertaJaTocouNestaVez) {
                        alertaJaTocouNestaVez = true;

                        if (mpAlerta != null) {
                            mpAlerta.seekTo(0);
                            mpAlerta.start();
                        }
                    }
                }
            };

            handler.postDelayed(runnableAlerta, 5000);
        }
    }

    private boolean existeCapturaDisponivelParaJogador(int[][] tab, int jogador) {
        int[] dirL = {-1, -1, 1, 1};
        int[] dirC = {-1, 1, -1, 1};

        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                int p = tab[l][c];
                if (p == 0) continue;

                int dono = (p & 3);
                if (dono != jogador) continue;

                for (int i = 0; i < 4; i++) {
                    int lMeio = l + dirL[i];
                    int cMeio = c + dirC[i];
                    int lDest = l + 2 * dirL[i];
                    int cDest = c + 2 * dirC[i];

                    if (lDest < 0 || lDest >= 8 || cDest < 0 || cDest >= 8) continue;
                    if (lMeio < 0 || lMeio >= 8 || cMeio < 0 || cMeio >= 8) continue;

                    int pMeio = tab[lMeio][cMeio];
                    int pDest = tab[lDest][cDest];

                    if (pDest == 0 && pMeio != 0 && (pMeio & 3) != jogador) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void cancelarAlertaSeExistir() {
        if (runnableAlerta != null) {
            handler.removeCallbacks(runnableAlerta);
            runnableAlerta = null;
        }
    }

    public void notificarMovimentoFeito() {
        // Não cancela o alerta aqui
    }
}