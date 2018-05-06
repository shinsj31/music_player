package com.example.soo.hw4_2015726017;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlayMusicActivity extends AppCompatActivity {
    Intent intent;
    ArrayList<MusicInfo> list;
    int position;
    TextView topTitle; TextView topArtist; TextView title;
    TextView progressTv; TextView stdTv;
    ImageView imageView;
    SeekBar seekBar;
    boolean isStart=false;
    AsyncTask musicTask;
    private static final String PLAY="com.example.soo.hw4_2015726017.PLAY";
    private static final String PREV="com.example.soo.hw4_2015726017.PREV";
    private static final String NEXT="com.example.soo.hw4_2015726017.NEXT";

    int curPosition;

    Intent serviceIntent;
    BroadcastReceiver musicReceiver;

    public static final String path="com.example.soo.hw4_2015726017";

    private IMusicService musicService=null;
    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("service","onServiceConnected");
            musicService=IMusicService.Stub.asInterface(service);
            if(isStart){
                Log.i("service","onServiceConnected, isPlaying");
                findViewById(R.id.play).setBackgroundResource(R.drawable.pause);
                makeTask();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        intent=getIntent();

        String action=intent.getAction();

        list=(ArrayList<MusicInfo>) intent.getSerializableExtra("musicList");
        position=intent.getIntExtra("position",0);
        isStart=intent.getBooleanExtra("isPlaying",false);

        topTitle=(TextView)findViewById(R.id.topTitle);
        topArtist=(TextView)findViewById(R.id.topArtist);
        title=(TextView)findViewById(R.id.title_tv);
        progressTv=(TextView)findViewById(R.id.progressTv);
        stdTv=(TextView)findViewById(R.id.standartTv);

        imageView=(ImageView)findViewById(R.id.imageView);
        seekBar=(SeekBar)findViewById(R.id.seekBar);

        curPosition=intent.getIntExtra("curPosition",0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    try {
                        musicService.moveTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setting(position);
        SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
        Date time=new Date();
        seekBar.setProgress(curPosition);
        time.setTime(curPosition);
        progressTv.setText(sdf.format(time));

        if(action!=null){
            Log.i("display",action);
            serviceIntent=new Intent(getApplicationContext(),MusicService.class);
            serviceIntent.setAction("com.example.soo.hw4_2015726017.MUSICPLAYER");
            serviceIntent.putExtra("musicList",list);
            serviceIntent.putExtra("position",position);
            startService(serviceIntent);
            bindService(serviceIntent,mConnection,BIND_AUTO_CREATE);
        }else{
            serviceIntent=new Intent(getApplicationContext(),MusicService.class);
            stopService(serviceIntent);
            serviceIntent.setAction("com.example.soo.hw4_2015726017.MUSICPLAYER");
            serviceIntent.putExtra("musicList",list);
            serviceIntent.putExtra("position",position);
            startService(serviceIntent);
            bindService(serviceIntent,mConnection,BIND_AUTO_CREATE);
        }

        musicReceiver=new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                if(action.equals(PLAY)){
                        if(isStart){
                            findViewById(R.id.play).setBackgroundResource(R.drawable.play);
                            Log.i("receiver","play->pause");
                            isStart=false;
                        }else{
                            findViewById(R.id.play).setBackgroundResource(R.drawable.pause);
                            if(musicTask==null){
                                makeTask();
                            }
                            Log.i("receiver","pause->play");
                            isStart=true;
                        }
                    }else if(action.equals(NEXT)){
                    try {
                        musicService.next();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                        Log.i("receiver","next");
                    }else if(action.equals(PREV)){
                    try {
                        musicService.prev();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                        Log.i("receiver","prev");
                    }
            }
        };
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(path+".PLAY");
        intentFilter.addAction(path+".PREV");
        intentFilter.addAction(path+".NEXT");
        registerReceiver(musicReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        Log.i("end","destroy");
        unbindService(mConnection);
        unregisterReceiver(musicReceiver);
        if(musicTask!=null)
            musicTask.cancel(true);

        super.onDestroy();
    }

    public void makeTask(){
        if(musicTask==null){
            musicTask= new MusicTask(this,seekBar,stdTv,progressTv,musicService,position);
            musicTask.execute();
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.play:
                try {
                    if(!isStart){
                        musicService.play();
                        findViewById(R.id.play).setBackgroundResource(R.drawable.pause);
                        isStart=true;
                        makeTask();
                    }else{
                        musicService.pause();
                        findViewById(R.id.play).setBackgroundResource(R.drawable.play);
                        isStart=false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.prev:
                try {
                    musicService.prev();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.next:
                try {
                    musicService.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void setting(int position){
        this.position=position;
        topTitle.setText(list.get(position).title);
        topArtist.setText(list.get(position).artist);
        title.setText(list.get(position).title);

        SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
        Date time=new Date();
        time.setTime(list.get(position).duration);
        stdTv.setText(" / "+sdf.format(time));
        seekBar.setMax(list.get(position).duration);

        Bitmap img=getAlbumImage(position);

        if(img!=null)
            imageView.setImageBitmap(img);
        img=null;
        img=getAlbumImage(position);
    }
    private Bitmap getAlbumImage(int position){
        ContentResolver resolver=this.getContentResolver();
        Uri uri=Uri.parse("content://media/external/audio/albumart/"+list.get(position).album_id);
        InputStream in=null;
        try{
            in=resolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            return null;
        }
        Bitmap art= BitmapFactory.decodeStream(in);

        return art;
    }
}
