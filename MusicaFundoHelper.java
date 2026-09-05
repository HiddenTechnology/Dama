package com.damefeito;

import android.media.MediaPlayer;

public class MusicaFundoHelper {

    private static MediaPlayer mpMusicaFundo;

    public static void iniciarMusicaFundo(android.content.Context context) {
        if (mpMusicaFundo != null && mpMusicaFundo.isPlaying()) {
            return;
        }

        mpMusicaFundo = MediaPlayer.create(context, R.raw.musica_fundo);
        mpMusicaFundo.setLooping(true);
        mpMusicaFundo.start();
    }

    public static void pararMusicaFundo() {
        if (mpMusicaFundo != null) {
            if (mpMusicaFundo.isPlaying()) {
                mpMusicaFundo.pause();
            }
            mpMusicaFundo.release();
            mpMusicaFundo = null;
        }
    }

    public static boolean isTocando() {
        return mpMusicaFundo != null && mpMusicaFundo.isPlaying();
    }
}
