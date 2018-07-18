package net.gnu.explorer;

import android.widget.*;
import android.content.*;
import android.view.*;
import android.view.View.*;
import java.util.*;
import android.util.*;

public class ExtensibleAdapter extends BaseAdapter implements OnClickListener {

	@Override
	public void onClick(View p1) {
		toggle(p1.getTag());
	}

	/**
	 * Our data, part 1.
	 */
	private List<CharSequence> mTitles;

	/**
	 * Our data, part 2.
	 */
	private List<CharSequence> mDialogue;

	/**
	 * Our data, part 3.
	 */
	private ArrayList<Boolean> mExpanded;

	public ExtensibleAdapter(Context context, List<CharSequence> mTitles, List<CharSequence> mDialogue, ArrayList<Boolean> mExpanded) {
		mContext = context;
		this.mTitles = mTitles;
		this.mDialogue = mDialogue;
		this.mExpanded = mExpanded;
		if (mExpanded == null) {
			int size = mTitles.size();
//			Log.d("size()", size + ".");
//			Log.d("mTitles.size()", mTitles.size() + ".");
//			Log.d("mDialogue.size()", mDialogue.size() + ".");
			this.mExpanded = new ArrayList<Boolean>(size);
			for (int i = 0; i < size; i++) {
				this.mExpanded.add(false);
			}
//			Log.d("mExpanded.size()", this.mExpanded.size() + ".");
		}
	}

	/**
	 * The number of items in the list is determined by the number of speeches
	 * in our array.
	 * 
	 * @see android.widget.ListAdapter#getCount()
	 */
	public int getCount() {
		return mTitles.size();
	}

	/**
	 * Since the data comes from an array, just returning
	 * the index is sufficent to get at the data. If we
	 * were using a more complex data structure, we
	 * would return whatever object represents one 
	 * row in the list.
	 * 
	 * @see android.widget.ListAdapter#getItem(int)
	 */
	public Object getItem(int position) {
		return position;
	}

	/**
	 * Use the array index as a unique id.
	 * @see android.widget.ListAdapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Make a SpeechView to hold each row.
	 * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		SpeechView sv;
//		Log.d("mTitles.size()", mTitles.get(position) + ".");
//		Log.d("mDialogue.size()", mDialogue.get(position) + ".");
//		Log.d("mExpanded", mExpanded.get(position) + ".");
		if (convertView == null) {
			sv = new SpeechView(mContext, mTitles.get(position), mDialogue.get(position), mExpanded.get(position));
		} else {
			sv = (SpeechView)convertView;
			sv.setTitle(mTitles.get(position));
			sv.setDialogue(mDialogue.get(position));
			sv.setExpanded(mExpanded.get(position));
		}
		sv.mTitle.setOnClickListener(this);
		sv.mTitle.setTag(position);
		sv.mDialogue.setOnClickListener(this);
		sv.mDialogue.setTag(position);

		return sv;
	}

	public void toggle(int position) {
		mExpanded.set(position, !mExpanded.get(position));
		notifyDataSetChanged();
	}

	/**
	 * Remember our context so we can use it when constructing views.
	 */
	private Context mContext;


    /**
     * We will use a SpeechView to display each speech. It's just a LinearLayout
     * with two text fields.
     *
     */
    private class SpeechView extends LinearLayout {



        public SpeechView(Context context, CharSequence title, CharSequence dialogue, boolean expanded) {
            super(context);

            this.setOrientation(VERTICAL);

            // Here we build the child views in code. They could also have
            // been specified in an XML file.

            mTitle = new TextView(context);
            mTitle.setText(title);
			mTitle.setTextSize(18);
            addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText(dialogue);
            mDialogue.setTextSize(18);
			addView(mDialogue, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }

        /**
         * Convenience method to set the title of a SpeechView
         */
        public void setTitle(CharSequence title) {
            mTitle.setText(title);
        }

        /**
         * Convenience method to set the dialogue of a SpeechView
         */
        public void setDialogue(CharSequence words) {
            mDialogue.setText(words);
        }

        /**
         * Convenience method to expand or hide the dialogue
         */
        public void setExpanded(boolean expanded) {
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }

        private TextView mTitle;
        private TextView mDialogue;
    }
}
