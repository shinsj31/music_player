package com.example.soo.hw4_2015726017;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by soo on 2017-12-21.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener{
    private MediaPlayer mMediaPlayer;
    int position=-1;
    int playCurPosition=0;
    boolean stayPlaying=false;
    boolean isPrepared=false;
    NotificationManager manager;
    MusicNoti musicNoti;

    MusicReceiver musicReceiver;

    public static final String path="com.example.soo.hw4_2015726017";

    ArrayList<MusicInfo> musicList;
    IMusicService.Stub mBinder=new IMusicService.Stub(){
        public void play(){
            if(playCurPosition==0&&!isPrepared){
                mMediaPlayer.prepareAsync();
            }else{
                mMediaPlayer.start();
            }
            stayPlaying=true;
            musicNoti.updateNoti(null,null,0);
            manager.notify(1,musicNoti.getNoti());
        }
        public void pause(){
            playCurPosition=mMediaPlayer.getCurrentPosition();
            if(mMediaPlayer.isPlaying())
                 mMediaPlayer.pause();
            stayPlaying=false;
            musicNoti.updateNoti(null,null,1);
            manager.notify(1,musicNoti.getNoti());
        }
        public void prev(){
            if(position==0)
                position=musicList.size()-1;
            else
                position--;
            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(musicList.get(position).data);
                mMediaPlayer.prepareAsync();;
            } catch (IOException e) {
                e.printStackTrace();
            }
            playCurPosition=0;
            musicNoti.updateNoti(getAlbumImage(position),musicList.get(position).title,2);
            manager.notify(1,musicNoti.getNoti());
        }
        public void next(){
            if(position==musicList.size()-1)
                position=0;
            else
                position++;
            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(musicList.get(position).data);
                mMediaPlayer.prepareAsync();;
            } catch (IOException e) {
                e.printStackTrace();
            }
            playCurPosition=0;
            musicNoti.updateNoti(getAlbumImage(position),musicList.get(position).title,2);
            manager.notify(1,musicNoti.getNoti());
        }
        public void moveTo(int position){
            mMediaPlayer.seekTo(position);
        }

        public int getDuration()
        {
            return mMediaPlayer.getDuration();
        }
        public int getCurrentPosition(){
            return mMediaPlayer.getCurrentPosition();
        }

        public int getListPosition(){
            return position;
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };
    @Override
    public void onCreate() {
        if(mMediaPlayer==null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i("complete!",position+"");
                    if(position==musicList.size()-1)
                        position=0;
                    else
                        position++;
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(musicList.get(position).data);
                        playCurPosition=0;
                        mMediaPlayer.prepareAsync();
                        musicNoti.updateNoti(getAlbumImage(position),musicList.get(position).title,2);
                        manager.notify(1,musicNoti.getNoti());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            musicReceiver=new MusicReceiver(mMediaPlayer);
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction(path+".PLAY");
            intentFilter.addAction(path+".PREV");
            intentFilter.addAction(path+".NEXT");
            intentFilter.addAction(path+".DISPLAY");
            registerReceiver(musicReceiver,intentFilter);
            musicReceiver.setBound(true);
        }
        super.onCreate();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared=true;
        if(stayPlaying&&isPrepared){
            mMediaPlayer.start();
            isPrepared=false;
        }
    }

    public void play(){
        try {
            mBinder.play();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void pause(){
        try {
            mBinder.pause();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void prev(){
        try {
            mBinder.prev();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void next(){
        try {
            mBinder.next();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(position==-1){
            position=intent.getIntExtra("position",0);
            musicList=(ArrayList<MusicInfo>) intent.getSerializableExtra("musicList");
            try {
                mMediaPlayer.setDataSource(musicList.get(position).data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if(musicNoti==null){
                musicNoti=new MusicNoti(this,getAlbumImage(position),musicList.get(position).title);
            }
            startForeground(1,musicNoti.getNoti());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("service","destroy");
        this.stopSelf();
        mMediaPlayer.release();
        unregisterReceiver(musicReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        musicReceiver.setBound(false);
        return super.onUnbind(intent);
    }
    private Bitmap getAlbumImage(int position){
        ContentResolver resolver=this.getContentResolver();
        Uri uri=Uri.parse("content://media/external/audio/albumart/"+musicList.get(position).album_id);
        InputStream in=null;
        try{
            in=resolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            return null;
        }
        Bitmap art= BitmapFactory.decodeStream(in);

        return art;
    }

    public ArrayList<MusicInfo> getMusicList(){return musicList;}
    public int getPosition(){return position;}
    public boolean getIsPlaying(){return mMediaPlayer.isPlaying();}
    public int getCurrentPosition(){return mMediaPlayer.getCurrentPosition();}
    public void exitNoti(){stopForeground(true);}
}
