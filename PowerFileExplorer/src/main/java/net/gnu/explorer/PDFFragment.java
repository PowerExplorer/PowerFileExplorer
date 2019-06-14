package net.gnu.explorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import net.gnu.explorer.R;
import net.gnu.mupdf.viewer.MuPDFCore;
import net.gnu.mupdf.viewer.ReaderView;
import net.gnu.mupdf.viewer.SearchTask;
import net.gnu.mupdf.viewer.OutlineActivity;
import net.gnu.mupdf.viewer.SearchTaskResult;
import net.gnu.mupdf.viewer.PageAdapter;
import net.gnu.mupdf.viewer.PageView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.util.Log;
import java.io.File;
import com.amaze.filemanager.ui.icons.MimeTypes;
import net.gnu.common.*;

public class PDFFragment extends Frag {
	/* The core rendering instance */
	enum TopBarMode {Main, Search, More};

	private MuPDFCore    core;
	private String       mFileName;
	private ReaderView   mDocView;
	private View         mButtonsView;
	private boolean      mButtonsVisible;
	private EditText     mPasswordView;
	private TextView     mFilenameView;
	private SeekBar      mPageSlider;
	private int          mPageSliderRes;
	private TextView     mPageNumberView;
	private ImageButton  mSearchButton;
	private ImageButton  mOutlineButton;
	private ViewAnimator mTopBarSwitcher;
	private ImageButton  mLinkButton;
	private TopBarMode   mTopBarMode = TopBarMode.Main;
	private ImageButton  mSearchBack;
	private ImageButton  mSearchFwd;
	private ImageButton  mSearchClose;
	private EditText     mSearchText;
	private SearchTask   mSearchTask;
	private AlertDialog.Builder mAlertBuilder;
	private boolean    mLinkHighlight = false;
	private final Handler mHandler = new Handler();
	private boolean mAlertsActive= false;
	private AlertDialog mAlertDialog;
	private ArrayList<OutlineActivity.Item> mFlatOutline;
	private static final String TAG = "PDFFragment";
    private RelativeLayout mainLayout;

    public PDFFragment() {
		super();
		type = Frag.TYPE.PDF;
		title = "Pdf";
    }

	private MuPDFCore openFile(String path) {
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1
							   ? path
							   : path.substring(lastSlashPos + 1));
		Log.d(TAG, "Trying to open " + path);
		try {
			core = new MuPDFCore(path);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} catch (java.lang.OutOfMemoryError e) {
			//  out of memory is not an Exception, so we catch it separately.
			e.printStackTrace();
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[], String magic) {
		Log.d(TAG, "Trying to open byte buffer");
		try {
			core = new MuPDFCore(buffer, magic);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return core;
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pdf, container, false);
        mainLayout = (RelativeLayout) rootView.findViewById(R.id.pdflayout);

        load2(savedInstanceState);
		updateColor(rootView);
		Log.d(TAG, "path " + currentPathTitle);
		//load(currentPathTitle);
        return rootView;
    }

	@Override
	public void load(String path) {
		this.currentPathTitle = path;
		load2(new Bundle());
	}

    public View load2(Bundle savedInstanceState) {
		Bundle args = this.getArguments();
        if (savedInstanceState != null && savedInstanceState.size() > 0) {
			title = savedInstanceState.getString("title");
			currentPathTitle = savedInstanceState.getString(Constants.EXTRA_ABSOLUTE_PATH);
		} else if (args != null) {
			title = args.getString("title");
			if (args.getString(Constants.EXTRA_ABSOLUTE_PATH) != null) {
				currentPathTitle = args.getString(Constants.EXTRA_ABSOLUTE_PATH);
			}
		}

		Log.d(TAG, "load " + currentPathTitle);
		mAlertBuilder = new AlertDialog.Builder(fragActivity);

		core = null;
		if (currentPathTitle != null && currentPathTitle.length() > 0) {
			if (core == null) {
				//Intent intent = getIntent();
				byte buffer[] = null;

				if (currentPathTitle.startsWith("file:/")) {
					core = openFile(currentPathTitle.substring("file:".length()));
				} else if (currentPathTitle.startsWith("/")) {
					core = openFile(currentPathTitle);
				} else {
					try {
						InputStream is = fragActivity.getContentResolver().openInputStream(Uri.parse(currentPathTitle));
						int len;
						ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
						byte[] data = new byte[65535];
						while ((len = is.read(data, 0, data.length)) != -1) {
							bufferStream.write(data, 0, len);
						}
						bufferStream.flush();
						buffer = bufferStream.toByteArray();
						is.close();
					} catch (IOException e) {
						String reason = e.toString();
						Resources res = getResources();
						final AlertDialog alert = mAlertBuilder.create();
						alert.setTitle(String.format(Locale.ROOT, res.getString(R.string.cannot_open_document_Reason), reason));
						alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
						alert.show();
						//return;
					}
					final String mimeType = MimeTypes.getMimeType(currentPathTitle);
					Log.d(TAG, "mimeType " + mimeType);
					core = openBuffer(buffer, mimeType);
				}
				SearchTaskResult.set(null);
				
				if (core != null && core.needsPassword()) {
					requestPassword(savedInstanceState);
					//return;
				}
				if (core != null && core.countPages() == 0) {
					core = null;
				}
			}
		}

		if (core == null) {
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.cannot_open_document);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			alert.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
					}
				});
			alert.show();
			//return;
		}

		return createUI(savedInstanceState);
	}

	public void updateColor(View rootView) {
		rootView.setBackgroundColor(Constants.BASE_BACKGROUND);
	}

	public void requestPassword(final Bundle savedInstanceState) {
		mPasswordView = new EditText(fragActivity);
		mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.enter_password);
		alert.setView(mPasswordView);
		alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (core.authenticatePassword(mPasswordView.getText().toString())) {
						createUI(savedInstanceState);
					} else {
						requestPassword(savedInstanceState);
					}
				}
			});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
			new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		alert.show();
	}

	public View createUI(Bundle savedInstanceState) {
//		if (core == null)
//			return;

		// Now create the UI.
		// First create the document view
			mDocView = new ReaderView(fragActivity) {
				@Override
				protected void onMoveToChild(int i) {
					if (core == null)
						return;

					mPageNumberView.setText(String.format(Locale.ROOT, "%d / %d", i + 1, core.countPages()));
					mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
					mPageSlider.setProgress(i * mPageSliderRes);
					super.onMoveToChild(i);
				}

				@Override
				protected void onTapMainDocArea() {
					if (!mButtonsVisible) {
						showButtons();
					} else {
						if (mTopBarMode == TopBarMode.Main)
							hideButtons();
					}
				}

				@Override
				protected void onDocMotion() {
					hideButtons();
				}
			};
		mDocView.setAdapter(new PageAdapter(fragActivity, core));

		mSearchTask = new SearchTask(fragActivity, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				// Ask the ReaderView to move to the resulting page
				mDocView.setDisplayedViewIndex(result.pageNumber);
				// Make the ReaderView act on the change to SearchTaskResult
				// via overridden onChildSetup method.
				mDocView.resetupChildren();
			}
		};

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;

		// Set the file-name text
		String docTitle = core.getTitle();
		if (docTitle != null)
			mFilenameView.setText(docTitle);
		else
			mFilenameView.setText(mFileName);

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
					mDocView.pushHistory();
					mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
				}

				public void onStartTrackingTouch(SeekBar seekBar) {}

				public void onProgressChanged(SeekBar seekBar, int progress,
											  boolean fromUser) {
					updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
				}
			});

		// Activate the search-preparing button
		mSearchButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					searchModeOn();
				}
			});

		mSearchClose.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					searchModeOff();
				}
			});

		// Search invoking buttons are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);
		mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {
					boolean haveText = s.toString().length() > 0;
					setButtonEnabled(mSearchBack, haveText);
					setButtonEnabled(mSearchFwd, haveText);

					// Remove any previous search results
					if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
						SearchTaskResult.set(null);
						mDocView.resetupChildren();
					}
				}
				public void beforeTextChanged(CharSequence s, int start, int count,
											  int after) {}
				public void onTextChanged(CharSequence s, int start, int before,
										  int count) {}
			});

		//React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE)
						search(1);
					return false;
				}
			});

		mSearchText.setOnKeyListener(new View.OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
						search(1);
					return false;
				}
			});

		// Activate search invoking buttons
		mSearchBack.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					search(-1);
				}
			});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					search(1);
				}
			});

		mLinkButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					setLinkHighlight(!mLinkHighlight);
				}
			});

		if (core.hasOutline()) {
			mOutlineButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (mFlatOutline == null)
							mFlatOutline = core.getOutline();
						if (mFlatOutline != null) {
							Intent intent = new Intent(fragActivity, OutlineActivity.class);
							Bundle bundle = new Bundle();
							bundle.putInt("POSITION", mDocView.getDisplayedViewIndex());
							bundle.putSerializable("OUTLINE", mFlatOutline);
							intent.putExtras(bundle);
							fragActivity.startActivityForResult(intent, Constants.OUTLINE_REQUEST_CODE);
						}
					}
				});
		} else {
			mOutlineButton.setVisibility(View.GONE);
		}

		// Reenstate last state if it was recorded
		SharedPreferences prefs = fragActivity.getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();

		// Stick the document view and the buttons overlay into a parent view
		//RelativeLayout layout = new RelativeLayout(fragActivity);
		mainLayout.removeAllViews();
		mainLayout.setBackgroundColor(Color.DKGRAY);
		mainLayout.addView(mDocView);
		mainLayout.addView(mButtonsView);
		return mainLayout;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case Constants.OUTLINE_REQUEST_CODE:
				if (resultCode >= Activity.RESULT_FIRST_USER) {
					mDocView.pushHistory();
					mDocView.setDisplayedViewIndex(resultCode - Activity.RESULT_FIRST_USER);
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			SharedPreferences prefs = fragActivity.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.apply();
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		if (mTopBarMode == TopBarMode.Search)
			outState.putBoolean("SearchMode", true);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mSearchTask != null)
			mSearchTask.stop();

		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = fragActivity.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.apply();
		}
	}

	public void onDestroy() {
		if (mDocView != null) {
			mDocView.applyToChildren(new ReaderView.ViewMapper() {
					public void applyToView(View view) {
						((PageView)view).releaseBitmaps();
					}
				});
		}
		if (core != null)
			core.onDestroy();
		core = null;
		super.onDestroy();
	}

	private void setButtonEnabled(ImageButton button, boolean enabled) {
		button.setEnabled(enabled);
		button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255) : Color.argb(255, 128, 128, 128));
	}

	private void setLinkHighlight(boolean highlight) {
		mLinkHighlight = highlight;
		// LINK_COLOR tint
		mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 0x00, 0x66, 0xCC) : Color.argb(0xFF, 255, 255, 255));
		// Inform pages of the change.
		mDocView.setLinksEnabled(highlight);
	}

	private void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
			mPageSlider.setProgress(index * mPageSliderRes);
			if (mTopBarMode == TopBarMode.Search) {
				mSearchText.requestFocus();
				showKeyboard();
			}

			Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
					public void onAnimationStart(Animation animation) {
						mTopBarSwitcher.setVisibility(View.VISIBLE);
					}
					public void onAnimationRepeat(Animation animation) {}
					public void onAnimationEnd(Animation animation) {}
				});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
					public void onAnimationStart(Animation animation) {
						mPageSlider.setVisibility(View.VISIBLE);
					}
					public void onAnimationRepeat(Animation animation) {}
					public void onAnimationEnd(Animation animation) {
						mPageNumberView.setVisibility(View.VISIBLE);
					}
				});
			mPageSlider.startAnimation(anim);
		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();

			Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
					public void onAnimationStart(Animation animation) {}
					public void onAnimationRepeat(Animation animation) {}
					public void onAnimationEnd(Animation animation) {
						mTopBarSwitcher.setVisibility(View.INVISIBLE);
					}
				});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
					public void onAnimationStart(Animation animation) {
						mPageNumberView.setVisibility(View.INVISIBLE);
					}
					public void onAnimationRepeat(Animation animation) {}
					public void onAnimationEnd(Animation animation) {
						mPageSlider.setVisibility(View.INVISIBLE);
					}
				});
			mPageSlider.startAnimation(anim);
		}
	}

	private void searchModeOn() {
		if (mTopBarMode != TopBarMode.Search) {
			mTopBarMode = TopBarMode.Search;
			//Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}

	private void searchModeOff() {
		if (mTopBarMode == TopBarMode.Search) {
			mTopBarMode = TopBarMode.Main;
			hideKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}

	private void updatePageNumView(int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format(Locale.ROOT, "%d / %d", index + 1, core.countPages()));
	}

	private void makeButtonsView() {
		mButtonsView = fragActivity.getLayoutInflater().inflate(R.layout.document_mu_activity, null);
		mFilenameView = (TextView)mButtonsView.findViewById(R.id.docNameText);
		mPageSlider = (SeekBar)mButtonsView.findViewById(R.id.pageSlider);
		mPageNumberView = (TextView)mButtonsView.findViewById(R.id.pageNumber);
		mSearchButton = (ImageButton)mButtonsView.findViewById(R.id.searchButton);
		mOutlineButton = (ImageButton)mButtonsView.findViewById(R.id.outlineButton);
		mTopBarSwitcher = (ViewAnimator)mButtonsView.findViewById(R.id.switcher);
		mSearchBack = (ImageButton)mButtonsView.findViewById(R.id.searchBack);
		mSearchFwd = (ImageButton)mButtonsView.findViewById(R.id.searchForward);
		mSearchClose = (ImageButton)mButtonsView.findViewById(R.id.searchClose);
		mSearchText = (EditText)mButtonsView.findViewById(R.id.searchText);
		mLinkButton = (ImageButton)mButtonsView.findViewById(R.id.linkButton);
		mTopBarSwitcher.setVisibility(View.INVISIBLE);
		mPageNumberView.setVisibility(View.INVISIBLE);

		mPageSlider.setVisibility(View.INVISIBLE);
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)fragActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(mSearchText, 0);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)fragActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	private void search(int direction) {
		hideKeyboard();
		int displayPage = mDocView.getDisplayedViewIndex();
		SearchTaskResult r = SearchTaskResult.get();
		int searchPage = r != null ? r.pageNumber : -1;
		mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
	}

	//@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return fragActivity.onSearchRequested();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		super.onPrepareOptionsMenu(menu);
	}

	//@Override
	public void onBackPressed() {
		mDocView.popHistory();
//		if (!mDocView.popHistory())
//			super.onBackPressed();
	}
}
