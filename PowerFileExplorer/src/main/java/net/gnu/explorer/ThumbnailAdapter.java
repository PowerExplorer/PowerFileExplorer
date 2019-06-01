package net.gnu.explorer;

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
import android.net.Uri;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {

    private static final String TAG = "ThumbnailAdapter";

    private final List<Uri> mDataSet;
	private final Context ctx;
	private final OnClickListener thumbnailOnClickListener;
	private final int thumbnailSize;

    public static class ViewHolder extends RecyclerView.ViewHolder {
//        final ImageView thumbnail;
		public ViewHolder(View frameLayout) {
            super(frameLayout);
//			thumbnail = new ImageView(frameLayout.getContext());
//			thumbnail.setPadding(3, 3, 3, 3);
//			thumbnail.setBackgroundColor(0x80808080);
//			thumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//			thumbnail.setOnClickListener(thumbnailOnClickListener);
//			final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(thumbnailSize, thumbnailSize, Gravity.CENTER);
//			//thumbnail.setLayoutParams(lp);
//			//lp.setMargins(2, 2, 2, 2);
//			((FrameLayout)frameLayout).addView(thumbnail, lp);
        }
//		
//		private void bind(final int position) {
//			thumbnail.setContentDescription(position + "");
//			GlideImageLoader.loadMedia(mDataSet.get(position), ctx, thumbnail, DiskCacheStrategy.RESULT);//, thumbnailSize);// , callback, mimes.get(position)new File(parentPath, 
//			Log.d(TAG, "onBindViewHolder viewHolder.itemView " + thumbnail + ", " + thumbnail.getContentDescription());
//		}
    }

    public ThumbnailAdapter(final Context ctx, final List<Uri> dataSet, final OnClickListener thumbnailOnClickListener, final int thumbnailSize) {//final List<String> mimes, final String parentPath, 
        mDataSet = dataSet;
		this.ctx = ctx;
		this.thumbnailOnClickListener = thumbnailOnClickListener;
		this.thumbnailSize = thumbnailSize;
		setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
		//Log.d(TAG, "onCreateViewHolder viewGroup " + viewGroup);
		//final long start= System.currentTimeMillis();
		//final FrameLayout frameLayout= new FrameLayout(viewGroup.getContext());
		final ImageView thumbnail = new ImageView(ctx);
		thumbnail.setPadding(3, 3, 3, 3);
		thumbnail.setBackgroundColor(0x80808080);
		thumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		thumbnail.setOnClickListener(thumbnailOnClickListener);
		final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(thumbnailSize, thumbnailSize, Gravity.CENTER);
		thumbnail.setLayoutParams(lp);
		//lp.setMargins(2, 2, 2, 2);
		//Log.d(TAG, "onCreateViewHolder " + Util.nf.format(System.currentTimeMillis() - start));
		return new ViewHolder(thumbnail);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
//        //final long start= System.currentTimeMillis();
//		Log.d(TAG, "onBindViewHolder viewHolder.itemView " + viewHolder.itemView + ", " + viewHolder.itemView.getContentDescription());
        viewHolder.itemView.setContentDescription(position + "");

		GlideImageLoader.loadMedia(mDataSet.get(position), ctx, (ImageView)viewHolder.itemView, DiskCacheStrategy.RESULT);//, thumbnailSize);// , callback, mimes.get(position)new File(parentPath, 
        //Log.d(TAG, "onBindViewHolder viewHolder.itemView " + viewHolder.itemView + ", " + viewHolder.itemView.getContentDescription());
        //Log.d(TAG, "onBindViewHolder " + Util.nf.format(System.currentTimeMillis() - start));
		//viewHolder.bind(position);
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
