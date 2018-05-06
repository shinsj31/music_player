package com.example.soo.hw4_2015726017;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

/**
 * Created by soo on 2017-12-25.
 */

public class MusicReceiver extends BroadcastReceiver{
    private static final String PLAY="com.example.soo.hw4_2015726017.PLAY";
    private static final String PREV="com.example.soo.hw4_2015726017.PREV";
    private static final String NEXT="com.example.soo.hw4_2015726017.NEXT";
    private static final String DISPLAY="com.example.soo.hw4_2015726017.DISPLAY";
    private MediaPlayer mediaPlayer;
    private boolean bound;

    public MusicReceiver(MediaPlayer player){
        mediaPlayer=player;
    }
    public void setBound(boolean bound){
        this.bound=bound;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
            if(action.equals(PLAY)){
                if(mediaPlayer.isPlaying()){
                    ((MusicService)context).pause();
                }else{
                    ((MusicService)context).play();
                }
            }else if(action.equals(NEXT)){
                if(!bound){
                    ((MusicService)context).next();
                }
            }else if(action.equals(PREV)){
                if(!bound){
                    ((MusicService)context).prev();
                }
            }else if(action.equals(DISPLAY)){
                Intent displayIntent=new Intent(context,PlayMusicActivity.class);
                displayIntent.setAction("com.example.soo.hw4_2015726017.DISPLAY");
                displayIntent.putExtra("musicList",((MusicService)context).getMusicList());
                displayIntent.putExtra("position",((MusicService)context).getPosition());
                displayIntent.putExtra("isPlaying",((MusicService)context).getIsPlaying());
                displayIntent.putExtra("curPosition",((MusicService)context).getCurrentPosition());
                displayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(displayIntent);
            }
    }
}
