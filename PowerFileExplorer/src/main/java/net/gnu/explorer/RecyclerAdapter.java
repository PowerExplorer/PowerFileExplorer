package net.gnu.explorer;

import android.support.v7.widget.*;
import android.view.*;
import java.util.*;
import android.widget.Filter;
import android.widget.Filterable;

public abstract class RecyclerAdapter<M, VH extends RecyclerView.ViewHolder>
extends RecyclerView.Adapter<VH> implements Filterable {

	protected final List<M> mDataset;
	private Filter planetFilter;

	@Override
	public Filter getFilter() {
		if (planetFilter == null)
			planetFilter = new PlanetFilter();
		return planetFilter;
	}

	private class PlanetFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			// We implement here the filter logic
			if (constraint == null || constraint.length() == 0) {
				// No filter implemented we return all the list
				results.values = mDataset;
				results.count = mDataset.size();
			} else {
				// We perform filtering operation
				List<M> nPlanetList = new ArrayList<M>();
				for (M p : mDataset) {
					if (p.toString().toUpperCase().contains(constraint.toString().toUpperCase()))
						nPlanetList.add(p);
				}
				results.values = nPlanetList;
				results.count = nPlanetList.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
									  FilterResults results) {
			// Now we have to inform the adapter about the new list filtered
			if (results.count == 0) {
				mDataset.clear();
				notifyDataSetChanged();
			} else {
				mDataset.clear();
				mDataset.addAll((List<M>)results.values);
				notifyDataSetChanged();
			}
		}
	}

	public RecyclerAdapter(List<M> m) {
		setHasStableIds(true);
		mDataset = m;
	}

	public void add(int position, M item) {
		mDataset.add(position, item);
		notifyItemInserted(position);
	}

	public void add(M item) {
		mDataset.add(item);
		notifyItemInserted(mDataset.size() - 1);
	}

	public void addAll(Collection<? extends M> collection) {
		if (collection != null) {
			mDataset.addAll(collection);
			notifyDataSetChanged();
		}
	}

	public void addAll(M... items) {
		addAll(Arrays.asList(items));
	}

	public void clear() {
		mDataset.clear();
		notifyDataSetChanged();
	}

	public void removeAll(Collection<? extends M> collection) {
		if (collection != null) {
			mDataset.removeAll(collection);
			notifyDataSetChanged();
		}
	}

	public void removeAll(M... items) {
		removeAll(Arrays.asList(items));
	}

	public int remove(M item) {
		int position = mDataset.indexOf(item);
		mDataset.remove(position);
		notifyItemRemoved(position);
		return position;
	}

	public M remove(int position) {
		M m = mDataset.remove(position);
		notifyItemRemoved(position);
		return m;
	}

	public M getItem(int position) {
		return mDataset.get(position);
	}

	public int indexOf(M m) {
		return mDataset.indexOf(m);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}
}
