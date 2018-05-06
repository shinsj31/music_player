package com.example.soo.hw4_2015726017;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by soo on 2017-12-24.
 */

public class MusicNoti {
    private Context mContext;
    private RemoteViews remoteViews;
    private NotificationCompat.Builder builder;
    private Notification noti;

    public MusicNoti(Context context, Bitmap albumArt, String title){
        mContext=context;
        builder=new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.drawable.pause);
        notiSetting(albumArt,title);
    }

    private void notiSetting(Bitmap albumArt, String title){
        remoteViews=new RemoteViews(mContext.getPackageName(),R.layout.activity_notification_layout);
        remoteViews.setTextViewText(R.id.title,title);
        remoteViews.setImageViewResource(R.id.play,R.drawable.play);
        if(albumArt!=null)
            remoteViews.setImageViewBitmap(R.id.imageView,albumArt);
        noti=builder.build();
        setListeners();
        noti.bigContentView=remoteViews;
    }
    //pending Intent 달기
    private void setListeners(){
        Intent playIntent=new Intent("com.example.soo.hw4_2015726017.PLAY");
        PendingIntent playPenddingIntent=PendingIntent.getBroadcast(mContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.play,playPenddingIntent);

        Intent prevIntent=new Intent("com.example.soo.hw4_2015726017.PREV");
        PendingIntent prevPenddingIntent=PendingIntent.getBroadcast(mContext,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.prev,prevPenddingIntent);

        Intent nextIntent=new Intent("com.example.soo.hw4_2015726017.NEXT");
        PendingIntent nextPenddingIntent=PendingIntent.getBroadcast(mContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.next,nextPenddingIntent);

        //액티비티가 닫혀있고, 노티피케이션으로 액티비티를 열 때 사용.
        Intent displayBroadcaseIntent=new Intent("com.example.soo.hw4_2015726017.DISPLAY");
        PendingIntent disBroadPenddingIntent=PendingIntent.getBroadcast(mContext,0,displayBroadcaseIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.imageView,disBroadPenddingIntent);
    }
    /*노티피케이션이 변경되는 상황
    * 0. 플레이버튼 누름(플레이버튼이 pause로 바뀌어야함.)
    * 1. 일시정지버튼 누름(일시정지버튼이 play로 바뀌어야함.)
    * 2. 다음곡, 혹은 이전곡으로 변경(앨범아트와 타이틀이 바뀌어야함)*/
    public void updateNoti(Bitmap albumArt, String title, int flag){
        switch (flag){
            case 0:
                remoteViews.setImageViewResource(R.id.play,R.drawable.pause);
                builder.setSmallIcon(R.drawable.play);
                noti=builder.build();
                noti.bigContentView=remoteViews;
                break;
            case 1:
                remoteViews.setImageViewResource(R.id.play,R.drawable.play);
                builder.setSmallIcon(R.drawable.pause);
                noti=builder.build();
                noti.bigContentView=remoteViews;
                break;
            case 2:
                if(albumArt!=null)
                    remoteViews.setImageViewBitmap(R.id.imageView,albumArt);
                else
                    remoteViews.setImageViewResource(R.id.imageView,R.drawable.no_img);
                remoteViews.setTextViewText(R.id.title,title);
                noti.bigContentView=remoteViews;
                break;
        }
    }

    public RemoteViews getRemoteViews(){return remoteViews;}
    public NotificationCompat.Builder getBuilder(){return builder;}
    public Notification getNoti(){return noti;}
}
