package com.damefeito;

public class Tabuleiro {
    private int[][] tabuleiro = new int[8][8];
    private int turno = 1;
    private boolean deveContinuarCaptura = false;
    private int linhaCapturaContinua = -1;
    private int colunaCapturaContinua = -1;

    private boolean multiplayer = false;
    private int meuJogador = 1;

    public interface OnTurnoMudouListener {
        void onTurnoMudou(int novoTurno);
    }

    public interface OnFimDeJogoListener {
        void onFimDeJogo(int vencedor);
    }

    private OnTurnoMudouListener listener;
    private OnFimDeJogoListener fimDeJogoListener;

    public void setOnTurnoMudouListener(OnTurnoMudouListener l) {
        this.listener = l;
    }

    public void setOnFimDeJogoListener(OnFimDeJogoListener l) {
        this.fimDeJogoListener = l;
    }

    public void setMultiplayer(boolean multiplayer, int meuJogador) {
        this.multiplayer = multiplayer;
        this.meuJogador = meuJogador;
    }

    public void configurarTabuleiroInicial() {
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                tabuleiro[l][c] = 0;
                if ((l + c) % 2 == 1) {
                    if (l <= 2) {
                        tabuleiro[l][c] = 2;
                    } else if (l >= 5) {
                        tabuleiro[l][c] = 1;
                    }
                }
            }
        }
        turno = 1;
        deveContinuarCaptura = false;
    }

    public int[][] getTabuleiro() {
        return tabuleiro;
    }

    public int getTurno() {
        return turno;
    }

    public boolean isDeveContinuarCaptura() {
        return deveContinuarCaptura;
    }

    public int getLinhaCapturaContinua() {
        return linhaCapturaContinua;
    }

    public int getColunaCapturaContinua() {
        return colunaCapturaContinua;
    }

    public boolean isTurnoDoJogador(int jogador) {
        if (!multiplayer) {
            return true;
        }
        // No multiplayer, só permite mover se for o turno do meuJogador
        return turno == meuJogador;
    }

    public boolean podeMover(int linhaOrigem, int colunaOrigem, int linhaDestino, int colunaDestino) {
        int pecaOrigem = tabuleiro[linhaOrigem][colunaOrigem];
        int pecaDestino = tabuleiro[linhaDestino][colunaDestino];
        int jogador = (pecaOrigem & 3);

        if (pecaDestino != 0 && (pecaDestino & 3) == jogador) {
            return false;
        }

        if (pecaDestino != 0) {
            return false;
        }

        int deltaLinha = linhaDestino - linhaOrigem;
        int deltaColuna = Math.abs(colunaDestino - colunaOrigem);

        if (Math.abs(deltaLinha) == 1 && deltaColuna == 1) {
            boolean ehDama = (pecaOrigem & 4) != 0;
            boolean frente = (turno == 1 && deltaLinha < 0) || (turno == 2 && deltaLinha > 0);

            if (ehDama) {
                return true;
            } else {
                return frente;
            }
        }

        if (Math.abs(deltaLinha) == 2 && deltaColuna == 2) {
            int dirLinha = Integer.compare(deltaLinha, 0);
            int lMeio = linhaOrigem + dirLinha;
            int cMeio = colunaOrigem + Integer.compare(colunaDestino - colunaOrigem, 0);
            int pMeio = tabuleiro[lMeio][cMeio];
            return pMeio != 0 && (pMeio & 3) != jogador;
        }

        return false;
    }

    public boolean podeCapturar(int linha, int coluna) {
        int p = tabuleiro[linha][coluna];
        if (p == 0) return false;
        int jogador = (p & 3);

        int[] dirL = {-1, -1, 1, 1};
        int[] dirC = {-1, 1, -1, 1};
        for (int i = 0; i < 4; i++) {
            int lMeio = linha + dirL[i];
            int cMeio = coluna + dirC[i];
            int lDest = linha + 2 * dirL[i];
            int cDest = coluna + 2 * dirC[i];

            if (lDest >= 0 && lDest < 8 && cDest >= 0 && cDest < 8) {
                if (tabuleiro[lDest][cDest] == 0) {
                    int pMeio = tabuleiro[lMeio][cMeio];
                    if (pMeio != 0 && (pMeio & 3) != jogador) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean fazerMovimento(int linhaOrigem, int colunaOrigem, int linhaDestino, int colunaDestino) {
        int pecaOrigem = tabuleiro[linhaOrigem][colunaOrigem];
        int jogador = (pecaOrigem & 3);

        if (!podeMover(linhaOrigem, colunaOrigem, linhaDestino, colunaDestino)) {
            return false;
        }

        boolean foiCaptura = false;
        int lCapturada = -1;
        int cCapturada = -1;

        int deltaLinha = linhaDestino - linhaOrigem;
        int deltaColuna = Math.abs(colunaDestino - colunaOrigem);

        if (Math.abs(deltaLinha) == 2 && deltaColuna == 2) {
            int dirLinha = Integer.compare(deltaLinha, 0);
            lCapturada = linhaOrigem + dirLinha;
            cCapturada = colunaOrigem + Integer.compare(colunaDestino - colunaOrigem, 0);
            foiCaptura = true;
        }

        tabuleiro[linhaDestino][colunaDestino] = pecaOrigem;
        tabuleiro[linhaOrigem][colunaOrigem] = 0;

        if (foiCaptura && lCapturada >= 0) {
            tabuleiro[lCapturada][cCapturada] = 0;
        }

        if ((pecaOrigem & 4) == 0) {
            if ((turno == 1 && linhaDestino == 0) || (turno == 2 && linhaDestino == 7)) {
                tabuleiro[linhaDestino][colunaDestino] = pecaOrigem | 4;
            }
        }

        if (foiCaptura && podeCapturar(linhaDestino, colunaDestino)) {
            deveContinuarCaptura = true;
            linhaCapturaContinua = linhaDestino;
            colunaCapturaContinua = colunaDestino;
        } else {
            deveContinuarCaptura = false;
            turno = (turno == 1) ? 2 : 1;
            if (listener != null) {
                listener.onTurnoMudou(turno);
            }
        }

        int vencedor = verificarFimDeJogo();
        if (vencedor != 0 && fimDeJogoListener != null) {
            fimDeJogoListener.onFimDeJogo(vencedor);
        }

        return foiCaptura;
    }

    private int verificarFimDeJogo() {
        int pecas1 = 0;
        int pecas2 = 0;
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                int p = tabuleiro[l][c];
                if ((p & 3) == 1) pecas1++;
                if ((p & 3) == 2) pecas2++;
            }
        }
        if (pecas1 == 0) return 2;
        if (pecas2 == 0) return 1;
        return 0;
    }

    public void reiniciarJogo() {
        configurarTabuleiroInicial();
        if (listener != null) {
            listener.onTurnoMudou(turno);
        }
    }
}