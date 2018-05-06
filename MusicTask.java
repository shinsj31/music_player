package com.example.soo.hw4_2015726017;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by soo on 2017-12-21.
 */

public class MusicTask extends AsyncTask {
    SeekBar musicBar;
    TextView duration;
    TextView curTime;
    IMusicService service;
    int postion;
    AppCompatActivity playActivity;
    SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
    Date time=new Date();

    boolean nextMusic=false;

    public MusicTask(AppCompatActivity playActivity, SeekBar seekBar, TextView duration, TextView curTime, IMusicService service, int position)
    {
        this.playActivity=playActivity;
        musicBar=seekBar;
        this.duration=duration;
        this.curTime=curTime;
        this.service=service;
        this.postion=position;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        int listPosition;
        while(true){
            try {
                listPosition=service.getListPosition();
                if(postion!=listPosition){
                    postion=listPosition;
                    nextMusic=true;
                }
                publishProgress();
                Thread.sleep(1000);
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;} catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        if(nextMusic){//이미지 바꿔줌
            ((PlayMusicActivity)playActivity).setting(postion);
            nextMusic=false;
        }
        try {
            int curTime=service.getCurrentPosition();
            time.setTime(curTime);
            this.curTime.setText(sdf.format(time));
            musicBar.setProgress(curTime);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        Log.i("end","cancelled!");
        super.onCancelled();
    }
}
