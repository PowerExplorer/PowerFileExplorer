package com.amaze.filemanager.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import net.gnu.explorer.R;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.files.Futils;
import com.amaze.filemanager.utils.OpenMode;

import java.io.File;
import java.util.ArrayList;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.Frag;
import net.gnu.explorer.ContentFragment;


/**
 * Created by Arpit on 16-11-2014.
 */
public class HiddenAdapter extends RecyclerArrayAdapter<HFile, HiddenAdapter.ViewHolder> {

    private Futils utils;

    private ContentFragment context;
    private Context c;
    public ArrayList<HFile> items;
    private MaterialDialog materialDialog;
    private boolean hide;
    private DataUtils dataUtils = DataUtils.getInstance();
    ///	public HashMap<Integer, Boolean> myChecked = new HashMap<Integer, Boolean>();

    public HiddenAdapter(Context context, ContentFragment mainFrag, Futils utils, @LayoutRes int layoutId,
                         ArrayList<HFile> items, MaterialDialog materialDialog, boolean hide) {
        addAll(items);
        this.utils = utils;
        this.c = context;
        this.context = mainFrag;
        this.items = items;
        this.hide = hide;
        this.materialDialog = materialDialog;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = (LayoutInflater) c
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.bookmarkrow, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.render(position, getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton image;
        private final TextView txtTitle;
        private final TextView txtDesc;
        private final LinearLayout row;

        ViewHolder(final View view) {
            super(view);

            txtTitle = (TextView) view.findViewById(R.id.text1);
            image = (ImageButton) view.findViewById(R.id.delete_button);
            txtDesc = (TextView) view.findViewById(R.id.text2);
            row = (LinearLayout) view.findViewById(R.id.bookmarkrow);
			image.setColorFilter(ExplorerActivity.TEXT_COLOR);
			txtDesc.setTextColor(ExplorerActivity.TEXT_COLOR);
		}

        void render(final int position, final HFile file) {
            txtTitle.setText(file.getName());
            final String path = file.getPath();
            txtDesc.setText(file.getReadablePath(path));
			if (hide)
                image.setVisibility(View.GONE);

            // TODO: move the listeners to the constructor
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (!file.isSmb() && file.isDirectory()) {
                        final ArrayList<BaseFile> a = new ArrayList<>(1);
                        final BaseFile baseFile = new BaseFile(path + "/.nomedia");
                        baseFile.setMode(OpenMode.FILE);
                        a.add(baseFile);
                        new DeleteTask(c, null).execute((a));//context.getActivity().getContentResolver(), 
                    }
                    dataUtils.removeHiddenFile(path);
                    remove(position);
					if (getItemCount() == 0) {
						materialDialog.dismiss();
					}
				}
            });
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    materialDialog.dismiss();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (file.isDirectory()) {
                                context.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        context.loadlist(path, 
										//false, 
										OpenMode.UNKNOWN);
                                    }
                                });
                            } else {
                                if (!file.isSmb()) {
                                    context.getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            utils.openFile(new File(path), (ExplorerActivity) context.getActivity());
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                }
            });
        }
    }

    public void updateDialog(final MaterialDialog dialog) {
        materialDialog = dialog;
    }
}
