package net.gnu.explorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import java.util.*;
import android.view.*;
import android.support.v7.app.*;
import android.widget.*;
import android.content.*;
import net.gnu.androidutil.*;
import com.amaze.filemanager.utils.*;
import com.amaze.filemanager.ui.icons.*;
import android.net.Uri;
import java.io.*;
import com.amaze.filemanager.filesystem.*;
import android.graphics.*;
import android.util.*;
import android.preference.*;
import android.content.res.*;
import com.tekinarslan.sample.PdfFragment;
import net.gnu.texteditor.Main;
import android.support.v4.app.*;
import com.amaze.filemanager.ui.LayoutElement;
import com.google.android.exoplayer2.demo.MediaPlayerFragment;
import android.view.animation.AnimationUtils;
import com.amaze.filemanager.services.asynctasks.CopyFileCheck;
import com.amaze.filemanager.activities.ThemedActivity;
import android.os.AsyncTask;
import com.bumptech.glide.Glide;
import com.amaze.filemanager.utils.files.Futils;
//import org.geometerplus.android.fbreader.FBReader;
//import com.jarvanmo.demo.MediaPlayerFragment;

public abstract class Frag extends Fragment implements View.OnTouchListener, View.OnClickListener, Cloneable, Serializable {

	private static final String TAG = "Frag";

	public TYPE type = TYPE.EXPLORER;
	public String currentPathTitle;
	protected String title;

	protected ViewGroup status;
	public ExplorerActivity activity;
	protected FragmentActivity fragActivity;
	public SharedPreferences sharedPref;
	public int theme1;

	ArrayList<LayoutElement> dataSourceL1 = new ArrayList<>();
	ArrayList<LayoutElement> tempOriDataSourceL1 = new ArrayList<>();
	ArrayList selectedInList1 = new ArrayList<LayoutElement>();
	ArrayList tempSelectedInList1 = new ArrayList<>();
	ArrayList<LayoutElement> dataSourceL2;
	LayoutElement tempPreviewL2 = null;

	private View horizontalDivider0;
	private View horizontalDivider12;
	private View horizontalDivider7;
	public String fabSkin;
	View horizontalDivider6;
	View horizontalDivider11;
//	ViewGroup leftCommands;
//	ViewGroup rightCommands;
	ViewGroup left;
	ViewGroup right;
	//int size;
	public ViewGroup commands;
	public View horizontalDivider;
	//Button deletePastes;
	Button deletePastesBtn;
	public SlidingTabsFragment slidingTabsFragment;
	public OpenMode openMode = OpenMode.FILE;
	//public Bundle bundle;
	protected boolean fake = false;

	public static final enum TYPE {
		EMPTY, EXPLORER, SELECTION, TEXT, WEB, PDF, PHOTO, MEDIA, APP, TRAFFIC_STATS, PROCESS//FBReader, 
		};
	
	public static Frag getFrag(final SlidingTabsFragment s, final TYPE t, final String path) {

		if (t == TYPE.SELECTION) {
			final ContentFragment contentFragment2 = new ContentFragment();
			contentFragment2.type = TYPE.SELECTION;
			contentFragment2.slidingTabsFragment = s;
			contentFragment2.title = "Selection";
			if (path != null && path.length() > 0) {
				final List<String> asList = Arrays.asList(path.split("\\|"));
				for (String le : asList) {
					contentFragment2.dataSourceL1.add(new LayoutElement(new File(le)));
				}
			}
			return contentFragment2;
		} else if (t == TYPE.APP) {
			final AppsFragment appsFragment = new AppsFragment();
			appsFragment.slidingTabsFragment = s;
			return appsFragment;
		} else if (t == TYPE.PROCESS) {
			final ProcessFragment processFragment = new ProcessFragment();
			processFragment.slidingTabsFragment = s;
			return processFragment;
		} else if (t == TYPE.TEXT) {
			final Main main = Main.newInstance(null, "Text", path);
			main.slidingTabsFragment = s;
			return main;
		} else if (t == TYPE.PDF) {
			final PdfFragment pdfFragment = new PdfFragment();
			pdfFragment.slidingTabsFragment = s;
			return pdfFragment;
		} else if (t == TYPE.WEB) {
			final WebFragment webFragment = WebFragment.newInstance(path);
			webFragment.slidingTabsFragment = s;
			return webFragment;
//		} else if (t == TYPE.FBReader.ordinal()) {
//			return new FBReader();
		} else if (t == TYPE.MEDIA) {
			final MediaPlayerFragment mediaPlayerFragment = new MediaPlayerFragment();
			mediaPlayerFragment.slidingTabsFragment = s;
			return mediaPlayerFragment;
		} else if (t == TYPE.PHOTO) {
			final PhotoFragment photoFragment = new PhotoFragment();
			photoFragment.slidingTabsFragment = s;
			return photoFragment;
		} else if (t == TYPE.TRAFFIC_STATS) {
			final DataTrackerFrag dataTrackerFrag = new DataTrackerFrag();
			dataTrackerFrag.slidingTabsFragment = s;
			return dataTrackerFrag;
		} else if (t == TYPE.EXPLORER) {
			final ContentFragment contentFragment = new ContentFragment();
			contentFragment.slidingTabsFragment = s;
			return contentFragment;
		} 
		return null;
	}

//	public static Frag newInstance(final Width w, final String title, int type, String path, Bundle bundle) {
//		Log.d("Frag", "newInstance " + title);
//		final Frag fragment2 = Frag.getFrag(w, type);
//		if (bundle == null) {
//			bundle = fragment2.getArguments();
//		}
//		if (bundle == null) {
//			bundle = new Bundle();
//		} 
//		bundle.putString("title", title);
//		bundle.putString(ExplorerActivity.EXTRA_DIR_PATH, path);
//		bundle.putString(ExplorerActivity.EXTRA_SUFFIX, ".*");
//		bundle.putBoolean(ExplorerActivity.EXTRA_MULTI_SELECT, true);
//		fragment2.setArguments(bundle);
//		fragment2.currentPathTitle = title;
//		fragment2.currentPathTitle = path;
//		return fragment2;
//	}

//	public static Frag newInstance(final SlidingTabsFragment s, final Frag frag) {
//		Log.d("Frag", "newInstance " + frag);
//		final Frag frag2 = Frag.getFrag(s, frag.type);
//		frag2.clone(frag);
//		return frag2;
//	}

	public void updateList() {} //TODO
	public void load(String path) {}
	public void open(final int curPos, final List<LayoutElement> path) {}
	public void updateColor(View rootView) {}
	public void clone(final Frag frag, final boolean fake) {
		this.title = frag.title;
		this.currentPathTitle = frag.currentPathTitle;
		this.slidingTabsFragment = frag.slidingTabsFragment;
	}
	public Frag clone(final boolean fake) {
		final Frag frag = Frag.getFrag(slidingTabsFragment, type, currentPathTitle);
		frag.clone(this, fake);
		return frag;
	}

	public String getTitle() {
		if (currentPathTitle != null) {
			return currentPathTitle.substring(currentPathTitle.lastIndexOf("/") + 1);
		} else {
			return title;
		}
	}

	@Override
	public void onSaveInstanceState(android.os.Bundle outState) {
		//Log.d(TAG, "onSaveInstanceState" + path + ", " + outState);
		super.onSaveInstanceState(outState);
		outState.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);
		outState.putString("title", title);
	}

	@Override
	public boolean onTouch(android.view.View p1, android.view.MotionEvent p2) {
		//Log.d(TAG, "onTouch " + p1 + ", " + p2);
		if (activity != null) {
			select(true);
		}
		return false;
	}

	public boolean select(boolean sel) {
		ViewGroup statusOther;
		if (sel) {
			if (status != null) {
				status.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				if (activity.slideFrag2 != null && (statusOther = activity.slideFrag2.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				}
				activity.slideFrag1Selected = sel;
			} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
				if ((statusOther = activity.slideFrag.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				}
				activity.slideFrag1Selected = !sel;
			}
		} else {
			if (status != null) {
				status.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				if ((statusOther = activity.slideFrag2.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				}
				activity.slideFrag1Selected = sel;
			} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
				if ((statusOther = activity.slideFrag.getCurrentFragment().status) != null) {
					statusOther.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				}
				activity.slideFrag1Selected = !sel;
			}
		}
		return false;
	}

//	@Override
//	public void onAttachFragment(Fragment childFragment) {
//		//Log.d(TAG, "onAttachFragment " + childFragment);
//		super.onAttachFragment(childFragment);
//	}

//	@Override
//    public void onAttach(Context activity) {
//        //Log.d(TAG, "onAttach" + title);
//        super.onAttach(activity);
//    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		//Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(view, savedInstanceState);
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}

		setRetainInstance(true);
		AndroidUtils.setOnTouchListener(view, this);
		final Bundle args = getArguments();
		if (currentPathTitle == null && args != null) {
			title = args.getString("title");
			currentPathTitle = args.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
		}
		if (savedInstanceState != null) {
			title = savedInstanceState.getString("title");
			currentPathTitle = savedInstanceState.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
		}
        status = (ViewGroup)view.findViewById(R.id.status);

		horizontalDivider0 = view.findViewById(R.id.horizontalDivider0);
		horizontalDivider12 = view.findViewById(R.id.horizontalDivider12);
		horizontalDivider7 = view.findViewById(R.id.horizontalDivider7);

		horizontalDivider6 = view.findViewById(R.id.horizontalDivider6);
		horizontalDivider11 = view.findViewById(R.id.horizontalDivider11);
		deletePastesBtn = (Button) view.findViewById(R.id.deletes_pastes);

//		leftCommands = (ViewGroup) view.findViewById(R.id.leftCommands);
//		rightCommands = (ViewGroup) view.findViewById(R.id.rightCommands);
		commands = (ViewGroup) view.findViewById(R.id.commands);
		
//		Log.d(TAG, this + "commands " + commands);
//		Log.d(TAG, this + "leftCommands " + leftCommands);
//		Log.d(TAG, this + "rightCommands " + rightCommands);
//		Log.d(TAG, this + "horizontalDivider6 " + horizontalDivider6);
//		Log.d(TAG, this + "horizontalDivider11 " + horizontalDivider11);

		if (type == Frag.TYPE.EXPLORER && slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			//commands = leftCommands;
			right = activity.right;
			left = activity.left;
			slidingTabsFragment.width = activity.leftSize;
			horizontalDivider = horizontalDivider11;
		} else {
			if (this instanceof ContentFragment) {//explorer// && ((ContentFragment)this).type == 1
				//rightCommands = leftCommands;
				//leftCommands = null;
				horizontalDivider6 = horizontalDivider11;
				horizontalDivider11 = null;
			}
			if (activity != null) {
				//commands = rightCommands;
				right = activity.left;
				left = activity.right;
				slidingTabsFragment.width = -activity.leftSize;
				horizontalDivider = horizontalDivider6;
			}
		}

//		Log.d(TAG, this + "commands " + commands);
//		Log.d(TAG, this + "leftCommands " + leftCommands);
//		Log.d(TAG, this + "rightCommands " + rightCommands);
//		Log.d(TAG, this + "horizontalDivider6 " + horizontalDivider6);
//		Log.d(TAG, this + "horizontalDivider11 " + horizontalDivider11);

		if (activity != null && !(this instanceof PhotoFragment)) {
			view.findViewById(R.id.copys).setOnClickListener(this);
			view.findViewById(R.id.cuts).setOnClickListener(this);
			deletePastesBtn.setOnClickListener(this);
			view.findViewById(R.id.renames).setOnClickListener(this);
			view.findViewById(R.id.shares).setOnClickListener(this);
			view.findViewById(R.id.favourites).setOnClickListener(this);
			view.findViewById(R.id.hides).setOnClickListener(this);
			view.findViewById(R.id.encrypts).setOnClickListener(this);
			view.findViewById(R.id.infos).setOnClickListener(this);
			view.findViewById(R.id.addScreens).setOnClickListener(this);
			view.findViewById(R.id.compresss).setOnClickListener(this);
			if (selectedInList1.size() == 0 && activity.COPY_PATH == null && activity.MOVE_PATH == null) {
				if (commands.getVisibility() == View.VISIBLE) {
					horizontalDivider.setVisibility(View.GONE);
					commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
					commands.setVisibility(View.GONE);
				}
			} else if (commands.getVisibility() == View.GONE) {
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
				commands.setVisibility(View.VISIBLE);
				horizontalDivider.setVisibility(View.VISIBLE);
			}
		} else {
			View v = view.findViewById(R.id.right_scroll);
			if (v != null) {
				v.setVisibility(View.GONE);
				horizontalDivider6.setVisibility(View.GONE);
			}
		}

		if (savedInstanceState == null) {
			Button b;
			if (commands != null) {
				int no = commands.getChildCount();
				for (int i = 0; i < no; i++) {
					b = (Button) commands.getChildAt(i);
					b.setTextColor(ExplorerActivity.TEXT_COLOR);
					b.getCompoundDrawables()[1].setAlpha(0xff);
					b.getCompoundDrawables()[1].setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
				}
			}
//			if (rightCommands != null) {
//				int no = rightCommands.getChildCount();
//				for (int i = 0; i < no; i++) {
//					b = (Button) rightCommands.getChildAt(i);
//					b.setTextColor(ExplorerActivity.TEXT_COLOR);
//					b.getCompoundDrawables()[1].setAlpha(0xff);
//					b.getCompoundDrawables()[1].setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//				}
//			}
		}

		sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		int theme = sharedPref.getInt("theme", 2);
		theme1 = theme == 2 ? PreferenceUtils.hourOfDay() : theme;
	}

//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		//Log.d(TAG, "onActivityCreated " + savedInstanceState);
//		super.onActivityCreated(savedInstanceState);
//	}

//	@Override
//	public void onViewStateRestored(Bundle savedInstanceState) {
//		//Log.d(TAG, "onViewStateRestored " + savedInstanceState);
//		super.onViewStateRestored(savedInstanceState);
//	}

//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		//Log.d(TAG, "onSaveInstanceState " + outState);
//		super.onSaveInstanceState(outState);
//	}

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		//Log.d(TAG, "onConfigurationChanged " + newConfig);
//		super.onConfigurationChanged(newConfig);
//	}

	@Override
	public void onLowMemory() {
		Log.d(TAG, "onLowMemory " + Runtime.getRuntime().freeMemory());
		super.onLowMemory();
		Glide.get(getContext()).clearMemory();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}

		super.onStart();
	}

	@Override
    public void onResume() {
        //Log.d(TAG, "onResume " + title);
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
		super.onResume();
    }

//	@Override
//    public void onPause() {
//        //Log.d(TAG, "onPause " + title);
//		super.onPause();
//    }

//	@Override
//	public void onStop() {
//		//Log.d(TAG, "onStop");
//		super.onStop();
//	}

//	public void onLowMemory() {
//		//Log.d(TAG, "onLowMemory " + title);
//		super.onLowMemory();
//	}

//	@Override
//	public void onDestroyView() {
//		//Log.d(TAG, "onDestroyView");
//		super.onDestroyView();
//	}

//	@Override
//	public void onDestroy() {
//		//Log.d(TAG, "onDestroy");
//		super.onDestroy();
//	}

	protected void showToast(CharSequence st) {
		//Log.d(TAG, "showToast this.getContext()" + this + ", " + getActivity() + ", " + this.getContext());
		//if ((fragActivity = getActivity()) != null)
		Toast.makeText(fragActivity, st, Toast.LENGTH_LONG).show();
		//else if (getContext() != null)
		//Toast.makeText(getContext(), st, Toast.LENGTH_LONG).show();
	}

//	public boolean copys(final View item) {
////		activity.mode = OpenMode.FILE;
////		activity.copyl.clear();
////		activity.cutl.clear();
////		final ArrayList<LayoutElements> al = new ArrayList<>(1);
////		al.add(new LayoutElements(new File(path)));
////		activity.copyl.addAll(al);
////		return true;
//		activity.MOVE_PATH = null;
//		ArrayList<BaseFile> copies = new ArrayList<>();
//		for (LayoutElements le : selectedInList1) {//int i2 = 0; i2 < plist.size(); i2++
//			copies.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i2))
//		}
//		activity.COPY_PATH = copies;
//		return true;
//	}

//	public boolean cuts(final View item) {
////		activity.mode = OpenMode.FILE;
////		activity.copyl.clear();
////		activity.cutl.clear();
////		final ArrayList<LayoutElements> al = new ArrayList<>(1);
////		al.add(new LayoutElements(new File(path)));
////		activity.cutl.addAll(al);
//		activity.COPY_PATH = null;
//		ArrayList<BaseFile> copie = new ArrayList<>();
//		for (LayoutElements le : selectedInList1) {//int i3 = 0; i3 < plist.size(); i3++
//			copie.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i3))
//		}
//		activity.MOVE_PATH = copie;
//		return true;
//	}

//	public boolean renames(final View item) {
//		final File oldF = new File(currentPathTitle);
//		if (!oldF.exists()) {
//			showToast("\"" + currentPathTitle + "\" is not existed to rename");
//		} else {
//			Log.d(TAG, "oldPath " + currentPathTitle + ", " + item);
//			final EditText editText = new EditText(activity);
//			editText.setText(oldF.getName());
//			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);
//			layoutParams.gravity = Gravity.CENTER;
//			editText.setLayoutParams(layoutParams);
//			editText.setSingleLine(true);
//			editText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//			editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//			editText.setMinEms(2);
//			//editText.setGravity(Gravity.CENTER);
//			final int density = 8 * (int)getResources().getDisplayMetrics().density;
//			editText.setPadding(density, density, density, density);
//
//			AlertDialog dialog = new AlertDialog.Builder(this.getContext())
//				.setIconAttribute(android.R.attr.dialogIcon)
//				.setTitle("New Name")
//				.setView(editText)
//				.setPositiveButton(R.string.ok,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						String name = editText.getText().toString();
//						File newF = new File(oldF.getParent(), name);
//						String newPath = newF.getAbsolutePath();
//						Log.d("newF", newPath);
//						if (newF.exists()) {
//							showToast("\"" + newF + "\" is existing. Please choose another name");
//						} else {
//							boolean ok = AndroidPathUtils.renameFolder(oldF, newF, Frag.this.getContext());
//							if (ok) {
//								showToast("Rename successfully");
//							} else {
//								showToast("Rename unsuccessfully");
//							}
//							dialog.dismiss();
//						}
//					}
//				})
//				.setNegativeButton(R.string.cancel,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						dialog.dismiss();
//					}
//				}).create();
//			dialog.show();
//		}
//		return true;
//	}

//	public boolean deletes(final View item) {
//		if (currentPathTitle != null && new File(currentPathTitle).exists()) {
//			ArrayList<String> arr = new ArrayList<>();
//			arr.add(currentPathTitle);
//			//new Futils().deleteFiles(arr, activity);
//		}
//		return true;
//	}

//	public boolean compresss(View item) {
//		if (currentPathTitle != null && new File(currentPathTitle).exists()) {
//			ArrayList<File> arrayList = new ArrayList<File>();
//			arrayList.add(new File(currentPathTitle));
//		}
//		return true;
//	}

//	public boolean shares(View item) {
//		if (currentPathTitle != null && new File(currentPathTitle).exists()) {
//			ArrayList<File> arrayList2 = new ArrayList<File>();
//			arrayList2.add(new File(currentPathTitle));
//			//new Futils().shareFiles(arrayList, activity, theme1, Color.BLUE);
//			new Futils().shareFiles(arrayList2, activity, activity.getAppTheme(), Color.parseColor(fabSkin));
//		}
//		return true;
//	}



//	public boolean sends(View item) {
//		File f = new File(path);
//		Uri uri = Uri.fromFile(f);
//		Intent i = new Intent(Intent.ACTION_SEND);
//		i.setFlags(0x1b080001);
//
//		i.setData(uri);
//		Log.d("i.setData(uri)", uri + "." + i);
//		//String suff = FileUtil.getExtension(f);
//		//ComparableEntry<String, String> comparableEntry = new ComparableEntry<String, String>(suff, "");
//		//ComparableEntry<String, String> floor = null;
//		//if ((floor = mimeMap.floor(comparableEntry)).equals(comparableEntry)) {
//		String mimeType = MimeTypes.getMimeType(f);
//		i.setDataAndType(uri, mimeType);  //floor.getValue()
//		Log.d(TAG, f + " = " + mimeType);
//		//}
//
//		Log.d("send", i + ".");
//		Log.d("send.getExtras()", AndroidUtils.bundleToString(i.getExtras()));
//		Intent createChooser = Intent.createChooser(i, "Send via..");
//		Log.d("createChooser", createChooser + ".");
//		Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
//		startActivity(createChooser);
//		return true;
//	}

//	public boolean addScreens(final View v) {
//		AndroidUtils.addShortcut(activity, new File(currentPathTitle));
//		return true;
//	}

	public void onClick(final View v) {
		Log.d(TAG, "onClick v " + v + ", " + selectedInList1.size());
		switch (v.getId()) {
			case R.id.copys:
				activity.MOVE_PATH = null;
				ArrayList<BaseFile> copies = new ArrayList<>();
				for (LayoutElement le : selectedInList1) {//int i2 = 0; i2 < plist.size(); i2++
					copies.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i2))
				}
				activity.COPY_PATH = copies;

				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.curExploreFrag.commands.getVisibility() == View.GONE) {//type == -1
					activity.curExploreFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curExploreFrag.commands.setVisibility(View.VISIBLE);
					activity.curExploreFrag.horizontalDivider.setVisibility(View.VISIBLE);
					activity.curExploreFrag.updateDelPaste();
				} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.curContentFrag.commands.getVisibility() == View.GONE) {//type != -1
					activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curContentFrag.commands.setVisibility(View.VISIBLE);
					activity.curContentFrag.horizontalDivider.setVisibility(View.VISIBLE);
					activity.curContentFrag.updateDelPaste();
				}
				break;
			case R.id.cuts:
				activity.COPY_PATH = null;
				ArrayList<BaseFile> copie = new ArrayList<>();
				for (LayoutElement le : selectedInList1) {//int i3 = 0; i3 < plist.size(); i3++
					copie.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i3))
				}
				activity.MOVE_PATH = copie;

				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.curExploreFrag.commands.getVisibility() == View.GONE) {
					activity.curExploreFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curExploreFrag.commands.setVisibility(View.VISIBLE);
					activity.curExploreFrag.horizontalDivider.setVisibility(View.VISIBLE);
					activity.curExploreFrag.updateDelPaste();
				} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.curContentFrag.commands.getVisibility() == View.GONE) {
					activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curContentFrag.commands.setVisibility(View.VISIBLE);
					activity.curContentFrag.horizontalDivider.setVisibility(View.VISIBLE);
					activity.curContentFrag.updateDelPaste();
				}
				break;
			case R.id.deletes_pastes:
				Log.d(TAG, "deletesPastes selectedInList1.size() " + selectedInList1.size());
				if (selectedInList1.size() > 0) {
					//curContentFrag.deletes(view);
					//ArrayList<Integer> positions = new ArrayList<>();
					//positions.add(pos);
					new Futils().deleteFiles(selectedInList1, activity, /*positions, */activity.getAppTheme());
				} else {
					//curContentFrag.pastes(view);
					String path = currentPathTitle;
					ArrayList<BaseFile> arrayList = activity.COPY_PATH != null ? activity.COPY_PATH: activity.MOVE_PATH;
					boolean move = activity.MOVE_PATH != null;
					new CopyFileCheck(this, path, move, activity, ThemedActivity.rootMode)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList);
					//COPY_PATH = null;
					activity.MOVE_PATH = null;
				}
				break;
			case R.id.renames:
				break;
			case R.id.compresss:
				ArrayList<BaseFile> copies1 = new ArrayList<>();
				for (LayoutElement le : selectedInList1) {//int i4 = 0; i4 < plist.size(); i4++
					copies1.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i4)
				}
				activity.getFutils().showCompressDialog(activity, copies1, currentPathTitle);
//				if (currentPathTitle != null && new File(currentPathTitle).exists()) {
//					ArrayList<File> arrayList = new ArrayList<File>();
//					arrayList.add(new File(currentPathTitle));
//				}
				break;
			case R.id.shares:
				if (selectedInList1.size() > 0) {

					ArrayList<File> arrayList = new ArrayList<>();
					//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
					for (LayoutElement i : selectedInList1) {//plist
						arrayList.add(new File(i.path));//dataSourceL1.get(
					}
					if (selectedInList1.size() > 100)
						Toast.makeText(getActivity(), getResources().getString(R.string.share_limit),
									   Toast.LENGTH_SHORT).show();
					else {

						switch (dataSourceL1.get(0).getMode()) {
							case DROPBOX:
							case BOX:
							case GDRIVE:
							case ONEDRIVE:
								activity.getFutils().shareCloudFile(dataSourceL1.get(0).path,
																	dataSourceL1.get(0).getMode(), getContext());
								break;
							default:
								activity.getFutils().shareFiles(arrayList, getActivity(), activity.getAppTheme(), Color.parseColor(fabSkin));
								break;
						}
					}
				}
				break;
			case R.id.hides:
				break;
			case R.id.addScreens:
				activity = (ExplorerActivity) getActivity();
				for (LayoutElement f : selectedInList1) {
					AndroidUtils.addShortcut(activity, f.bf.f);
				}
				break;
			case R.id.favourites:
				break;
			case R.id.encrypts:
				break;
			case R.id.infos:
				if (selectedInList1.size() > 0) {

					ArrayList<LayoutElement> arrayList = new ArrayList<>();
					for (LayoutElement s : selectedInList1) {
						arrayList.add(s);
					}
					if (selectedInList1.size() > 100)
						Toast.makeText(getContext(), "Can't share more than 100 files", Toast.LENGTH_SHORT).show();
					else {
						final ArrayList<File> lf = new ArrayList<>(selectedInList1.size());
						for (LayoutElement le : selectedInList1) {
							lf.add(le.bf.f);
						}
						//new Futils().shareFiles(lf, activity, theme1, Color.BLUE);
						new Futils().shareFiles(lf, activity, activity.getAppTheme(), Color.parseColor(fabSkin));
					}
				} else {
					showToast("No file selected");
				}
				break;

		}
	}

}
