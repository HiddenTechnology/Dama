package com.damefeito;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TabuleiroView extends View {

    private Tabuleiro tabuleiro;

    private int linhaSelecionada = -1;
    private int colunaSelecionada = -1;

    private Paint paintQuadrado;
    private Paint paintPeca;
    private Paint paintSelecao;
    private Paint paintCoroa;
    private Paint paintBordaPeca;

    private int corClara = Color.WHITE;
    private int corEscura = Color.parseColor("#C0C0C0");
    private int corPeca1 = Color.RED;
    private int corPeca2 = Color.BLACK;

    private MediaPlayer mpMover;
    private MediaPlayer mpCapturar;
    private MediaPlayer mpVitoria;

    public interface OnMovimentoFeitoListener {
        void onMovimentoFeito(int lOrigem, int cOrigem, int lDestino, int cDestino);
    }

    private OnMovimentoFeitoListener movimentoListener;

    public void setOnMovimentoFeitoListener(OnMovimentoFeitoListener l) {
        this.movimentoListener = l;
    }

    public TabuleiroView(Context context) {
        super(context);
        init();
    }

    public TabuleiroView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        tabuleiro = new Tabuleiro();
        tabuleiro.configurarTabuleiroInicial();

        paintQuadrado = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPeca = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSelecao = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSelecao.setColor(Color.parseColor("#39FF14"));
        paintSelecao.setStyle(Paint.Style.STROKE);
        paintSelecao.setStrokeWidth(4f);

        paintCoroa = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCoroa.setColor(Color.parseColor("#FFD700"));
        paintCoroa.setStyle(Paint.Style.STROKE);
        paintCoroa.setStrokeWidth(3f);

        paintBordaPeca = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBordaPeca.setStyle(Paint.Style.STROKE);
        paintBordaPeca.setStrokeWidth(3f);

        mpMover = MediaPlayer.create(getContext(), R.raw.mover);
        mpCapturar = MediaPlayer.create(getContext(), R.raw.capturar);
        mpVitoria = MediaPlayer.create(getContext(), R.raw.vitoria);
    }

    public void setTabuleiro(Tabuleiro tabuleiro) {
        this.tabuleiro = tabuleiro;
        invalidate();
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public int getTurno() {
        return tabuleiro.getTurno();
    }

    public void configurarCores(int tipoTabuleiro, int cor1Idx, int cor2Idx) {
        if (tipoTabuleiro == 0) {
            corClara = Color.WHITE;
            corEscura = Color.BLACK;
        } else if (tipoTabuleiro == 1) {
            corClara = Color.parseColor("#E8C39E");
            corEscura = Color.parseColor("#8B5A2B");
        } else if (tipoTabuleiro == 2) {
            corClara = Color.parseColor("#0a0a0a");
            corEscura = Color.parseColor("#1a1a1a");
        } else if (tipoTabuleiro == 3) {
            corClara = Color.parseColor("#E3F2FD");
            corEscura = Color.parseColor("#90A4AE");
        } else if (tipoTabuleiro == 4) {
            corClara = Color.parseColor("#1B2E1B");
            corEscura = Color.parseColor("#0F1F0F");
        }

        if (cor1Idx == 0) {
            corPeca1 = Color.RED;
        } else if (cor1Idx == 1) {
            corPeca1 = Color.BLUE;
        } else if (cor1Idx == 2) {
            corPeca1 = Color.GREEN;
        } else if (cor1Idx == 3) {
            corPeca1 = Color.YELLOW;
        } else if (cor1Idx == 4) {
            corPeca1 = Color.WHITE;
        } else if (cor1Idx == 5) {
            corPeca1 = Color.parseColor("#D2B48C");
        } else if (cor1Idx == 6) {
            corPeca1 = Color.parseColor("#8B4513");
        } else if (cor1Idx == 7) {
            corPeca1 = Color.BLACK;
        }

        if (cor2Idx == 0) {
            corPeca2 = Color.RED;
        } else if (cor2Idx == 1) {
            corPeca2 = Color.BLUE;
        } else if (cor2Idx == 2) {
            corPeca2 = Color.GREEN;
        } else if (cor2Idx == 3) {
            corPeca2 = Color.YELLOW;
        } else if (cor2Idx == 4) {
            corPeca2 = Color.WHITE;
        } else if (cor2Idx == 5) {
            corPeca2 = Color.parseColor("#D2B48C");
        } else if (cor2Idx == 6) {
            corPeca2 = Color.parseColor("#8B4513");
        } else if (cor2Idx == 7) {
            corPeca2 = Color.BLACK;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int largura = getWidth();
        int altura = getHeight();
        int tamanhoCasa = Math.min(largura, altura) / 8;

        int offsetLeft = (largura - tamanhoCasa * 8) / 2;
        int offsetTop = (altura - tamanhoCasa * 8) / 2;

        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                int x = offsetLeft + c * tamanhoCasa;
                int y = offsetTop + l * tamanhoCasa;

                if ((l + c) % 2 == 0) {
                    paintQuadrado.setColor(corClara);
                } else {
                    paintQuadrado.setColor(corEscura);
                }
                canvas.drawRect(x, y, x + tamanhoCasa, y + tamanhoCasa, paintQuadrado);
            }
        }

        int[][] tab = tabuleiro.getTabuleiro();
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                int peca = tab[l][c];
                if (peca != 0) {
                    int x = offsetLeft + c * tamanhoCasa;
                    int y = offsetTop + l * tamanhoCasa;

                    int jogador = (peca & 3);
                    boolean ehDama = (peca & 4) != 0;

                    int corBase = (jogador == 1) ? corPeca1 : corPeca2;

                    int centroX = x + tamanhoCasa / 2;
                    int centroY = y + tamanhoCasa / 2;
                    int raio = tamanhoCasa / 2 - 4;

                    int corClaraPeca = lighten(corBase, 0.4f);
                    int corEscuraPeca = darken(corBase, 0.3f);

                    RadialGradient gradient = new RadialGradient(
                            centroX, centroY - raio / 3,
                            raio,
                            new int[]{corClaraPeca, corBase, corEscuraPeca},
                            new float[]{0.0f, 0.5f, 1.0f},
                            Shader.TileMode.CLAMP
                    );
                    paintPeca.setShader(gradient);
                    paintPeca.setStyle(Paint.Style.FILL);

                    canvas.drawCircle(centroX, centroY, raio, paintPeca);

                    paintBordaPeca.setColor(darken(corBase, 0.5f));
                    canvas.drawCircle(centroX, centroY, raio, paintBordaPeca);

                    if (ehDama) {
                        float raioCoroa = raio * 0.6f;
                        canvas.drawCircle(centroX, centroY, raioCoroa, paintCoroa);
                        paintCoroa.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(centroX - raioCoroa * 0.5f, centroY - raioCoroa * 0.3f, raioCoroa * 0.25f, paintCoroa);
                        canvas.drawCircle(centroX, centroY - raioCoroa * 0.6f, raioCoroa * 0.25f, paintCoroa);
                        canvas.drawCircle(centroX + raioCoroa * 0.5f, centroY - raioCoroa * 0.3f, raioCoroa * 0.25f, paintCoroa);
                        paintCoroa.setStyle(Paint.Style.STROKE);
                    }
                }
            }
        }

        if (linhaSelecionada >= 0 && colunaSelecionada >= 0) {
            int x = offsetLeft + colunaSelecionada * tamanhoCasa;
            int y = offsetTop + linhaSelecionada * tamanhoCasa;
            canvas.drawRect(x, y, x + tamanhoCasa, y + tamanhoCasa, paintSelecao);
        }
    }

    private int lighten(int color, float factor) {
        int r = (int) (Color.red(color) + (255 - Color.red(color)) * factor);
        int g = (int) (Color.green(color) + (255 - Color.green(color)) * factor);
        int b = (int) (Color.blue(color) + (255 - Color.blue(color)) * factor);
        return Color.rgb(r, g, b);
    }

    private int darken(int color, float factor) {
        int r = (int) (Color.red(color) * (1 - factor));
        int g = (int) (Color.green(color) * (1 - factor));
        int b = (int) (Color.blue(color) * (1 - factor));
        return Color.rgb(r, g, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        // Controle de turno multiplayer
        if (!tabuleiro.isTurnoDoJogador(tabuleiro.getTurno())) {
            return true;
        }

        int largura = getWidth();
        int altura = getHeight();
        int tamanhoCasa = Math.min(largura, altura) / 8;

        int offsetLeft = (largura - tamanhoCasa * 8) / 2;
        int offsetTop = (altura - tamanhoCasa * 8) / 2;

        int x = (int) event.getX();
        int y = (int) event.getY();

        int coluna = (x - offsetLeft) / tamanhoCasa;
        int linha = (y - offsetTop) / tamanhoCasa;

        if (linha < 0 || linha >= 8 || coluna < 0 || coluna >= 8) {
            return true;
        }

        if (linhaSelecionada == -1) {
            int[][] tab = tabuleiro.getTabuleiro();
            int peca = tab[linha][coluna];
            int jogador = (peca & 3);

            if (peca != 0 && jogador == tabuleiro.getTurno()) {
                linhaSelecionada = linha;
                colunaSelecionada = coluna;
                invalidate();
            }
        } else {
            int[][] tab = tabuleiro.getTabuleiro();
            int pecaOrigem = tab[linhaSelecionada][colunaSelecionada];
            int pecaDestino = tab[linha][coluna];
            int jogador = (pecaOrigem & 3);

            if (linha == linhaSelecionada && coluna == colunaSelecionada) {
                linhaSelecionada = -1;
                colunaSelecionada = -1;
                invalidate();
                return true;
            }

            if (pecaDestino != 0 && (pecaDestino & 3) == jogador) {
                linhaSelecionada = linha;
                colunaSelecionada = coluna;
                invalidate();
                return true;
            }

            if (tabuleiro.podeMover(linhaSelecionada, colunaSelecionada, linha, coluna)) {
                boolean foiCaptura = tabuleiro.fazerMovimento(linhaSelecionada, colunaSelecionada, linha, coluna);

                if (foiCaptura) {
                    if (mpCapturar != null) mpCapturar.start();
                } else {
                    if (mpMover != null) mpMover.start();
                }

                if (movimentoListener != null) {
                    movimentoListener.onMovimentoFeito(
                        linhaSelecionada, colunaSelecionada,
                        linha, coluna
                    );
                }

                if (tabuleiro.isDeveContinuarCaptura()) {
                    linhaSelecionada = tabuleiro.getLinhaCapturaContinua();
                    colunaSelecionada = tabuleiro.getColunaCapturaContinua();
                } else {
                    linhaSelecionada = -1;
                    colunaSelecionada = -1;
                }

                invalidate();
                return true;
            }

            linhaSelecionada = -1;
            colunaSelecionada = -1;
            invalidate();
        }

        return true;
    }

    public void aplicarMovimentoRemoto(int lOrigem, int cOrigem, int lDestino, int cDestino) {
        tabuleiro.fazerMovimento(lOrigem, cOrigem, lDestino, cDestino);
        invalidate();
    }

    public void reiniciarJogo() {
        tabuleiro.reiniciarJogo();
        linhaSelecionada = -1;
        colunaSelecionada = -1;
        invalidate();
    }

    public void setOnTurnoMudouListener(Tabuleiro.OnTurnoMudouListener l) {
        tabuleiro.setOnTurnoMudouListener(l);
    }

    public void setOnFimDeJogoListener(Tabuleiro.OnFimDeJogoListener l) {
        tabuleiro.setOnFimDeJogoListener(l);
    }

    public void setMultiplayer(boolean multiplayer, int meuJogador) {
        tabuleiro.setMultiplayer(multiplayer, meuJogador);
    }
}