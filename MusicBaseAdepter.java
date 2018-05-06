package com.example.soo.hw4_2015726017;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by soo on 2017-12-18.
 */

public class MusicBaseAdepter extends BaseAdapter {
    LayoutInflater mLayoutInflater;
    Context mContext;
    ArrayList<MusicInfo> mData;

    /*리스트 뷰 최적화를 위한 뷰홀더!*/
    class ViewHolder{
        ImageView imageView;
        TextView mTitleTv;
        TextView mArtistTv;
    }
    public MusicBaseAdepter(Context context, ArrayList<MusicInfo> data){
        mContext=context;
        mData=data;
        mLayoutInflater=LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {return mData.size();}
    @Override
    public Object getItem(int position) {return mData.get(position);}
    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView=convertView;
        ViewHolder holder=null;
        if(itemView==null){
            itemView=mLayoutInflater.inflate(R.layout.activity_list_view_item,null);
            holder=new ViewHolder();
            holder.imageView=itemView.findViewById(R.id.imageView);
            holder.mTitleTv=itemView.findViewById(R.id.title_tv);
            holder.mArtistTv=itemView.findViewById(R.id.artist_tv);
            itemView.setTag(holder);
        }else{
            holder=(ViewHolder) itemView.getTag();
        }

        holder.mTitleTv.setText(mData.get(position).title);
        holder.mArtistTv.setText(mData.get(position).artist);
        Bitmap albumArt=getAlbumImage(position);

        if(albumArt!=null){
            holder.imageView.setImageBitmap(albumArt);
        }
        else{
            BitmapDrawable drawable=(BitmapDrawable)mContext.getResources().getDrawable(R.drawable.no_img,null);
            Bitmap noImg=drawable.getBitmap();
            holder.imageView.setImageBitmap(noImg);
        }

        return itemView;
    }

    /*Album_id를 통해서 앨범이미지를 얻어오는 함수
    * content://media/external/audio/albumart/album_id 에서 얻어올 수 있다.*/
    private Bitmap getAlbumImage(int position){
        ContentResolver resolver=mContext.getContentResolver();
        Uri uri=Uri.parse("content://media/external/audio/albumart/"+mData.get(position).album_id);
        InputStream in=null;
        try{
            in=resolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.i("log",mData.get(position).album_id);
            return null;
        }
        Bitmap art= BitmapFactory.decodeStream(in);

        return art;
    }
}
