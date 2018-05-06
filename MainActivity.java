package com.example.soo.hw4_2015726017;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<MusicInfo> data;
    BaseAdapter adapter;
    MediaScannerConnection msc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*권한이 허용되었는지 먼저 체크한다.(자동으로 권한 체크가 안나와서..ㅠ)
        * 허용 된 경우: PackageManager.PERMISSION_GRANTED 반환
        * 허용이 되지 않은 경우: PackageManager.PERMISSION_DENIED 반환*/
        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            mediaScan();
            settingListView();
        }
    }
    public void settingListView(){
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.AudioColumns.DURATION
        };
        listView=(ListView)findViewById(R.id.listView);
        data=new ArrayList<MusicInfo>();
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        while(cursor.moveToNext()){
            MusicInfo musicInfo=new MusicInfo();
            musicInfo.album_id=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            musicInfo.data=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            musicInfo.title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            musicInfo.artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            musicInfo._id=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            musicInfo.duration=Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
            data.add(musicInfo);
        }

        adapter=new MusicBaseAdepter(this,data);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),PlayMusicActivity.class);
                intent.putExtra("musicList",data);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }

    private void mediaScan(){
        Log.i("main","mediaScan");
        MediaScannerConnection.MediaScannerConnectionClient mScanClient= new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                Log.i("main","media scanner connected!");
                File file=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

                File[] files=file.listFiles();
                if(files!=null){
                    Log.i("main","lengh: "+files.length);
                    for(int i=0;i<files.length;i++){
                        msc.scanFile(files[i].getAbsolutePath(),null);
                        Log.i("main","ab: "+files[i].getAbsolutePath());
                    }
                }
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.i("main","scan complete: "+path);
            }
        };
        msc=new MediaScannerConnection(this,mScanClient);
        msc.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            settingListView();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
