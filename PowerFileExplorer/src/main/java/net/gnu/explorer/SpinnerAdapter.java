package net.gnu.explorer;

import android.widget.ArrayAdapter;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.TextView;
import android.util.TypedValue;
import android.view.Gravity;
import net.gnu.common.*;

class SpinnerAdapter extends ArrayAdapter<String> {
	public SpinnerAdapter(Context context, int resource, String[] objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final String st = getItem(position);

		if (convertView == null) {
			convertView = new TextView(getContext());
		} 
		final TextView tv = (TextView)convertView;
		
		tv.setText(st);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		tv.setTextColor(Constants.TEXT_COLOR);
		tv.setLayoutParams(new ViewGroup.LayoutParams(
							   ViewGroup.LayoutParams.WRAP_CONTENT,
							   ViewGroup.LayoutParams.WRAP_CONTENT));
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		return convertView;
	}
	
	
}

	
