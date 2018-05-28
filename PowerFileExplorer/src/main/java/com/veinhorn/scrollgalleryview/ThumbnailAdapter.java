package com.veinhorn.scrollgalleryview;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.File;
import java.util.List;
import android.view.View.OnClickListener;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import net.gnu.util.Util;
import android.widget.FrameLayout;
import android.view.Gravity;
import com.veinhorn.example.GlideImageLoader;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {

    //private static final String TAG = "ThumbnailAdapter";

    private final List<File> mDataSet;
	//private final List<String> mimes;
	private final Context ctx;
	private final OnClickListener thumbnailOnClickListener;
	private final int thumbnailSize;
	//private final String parentPath;
	
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public ThumbnailAdapter(final Context ctx, final List<File> dataSet, final OnClickListener thumbnailOnClickListener, final int thumbnailSize) {//final List<String> mimes, final String parentPath, 
        mDataSet = dataSet;
		//this.parentPath = parentPath;
		//this.mimes = mimes;
		this.ctx = ctx;
		this.thumbnailOnClickListener = thumbnailOnClickListener;
		this.thumbnailSize = thumbnailSize;
		setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {

		//final long start= System.currentTimeMillis();
        
		final ImageView thumbnail = new ImageView(ctx);
		final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(thumbnailSize, thumbnailSize, Gravity.CENTER);
        //lp.setMargins(2, 2, 2, 0);
		thumbnail.setLayoutParams(lp);
		thumbnail.setPadding(0, 4, 0, 0);
		thumbnail.setBackgroundColor(0x80808080);
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		thumbnail.setOnClickListener(thumbnailOnClickListener);
        //Log.d(TAG, "onCreateViewHolder " + Util.nf.format(System.currentTimeMillis() - start));
		return new ViewHolder(thumbnail);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //final long start= System.currentTimeMillis();
        viewHolder.itemView.setContentDescription(position + "");
		GlideImageLoader.loadThumbnail(mDataSet.get(position), ctx, (ImageView)viewHolder.itemView);//, thumbnailSize);// , callback, mimes.get(position)new File(parentPath, 
        //Log.d(TAG, "onBindViewHolder " + Util.nf.format(System.currentTimeMillis() - start));
	}

    @Override
	public long getItemId(int position) {
		return position;
	}
	
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

	
}
