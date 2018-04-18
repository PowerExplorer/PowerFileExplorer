///*
// * Copyright (C) 2009 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package net.gnu.explorer;
//
//import android.app.*;
//import android.content.*;
//import android.graphics.*;
//import android.graphics.drawable.*;
//import android.net.*;
//import android.os.*;
//import android.text.*;
//import android.util.*;
//import android.view.*;
//import android.widget.*;
//import android.widget.FrameLayout.*;
//import com.bumptech.glide.*;
//import com.bumptech.glide.load.engine.*;
//import com.bumptech.glide.load.model.*;
//import com.bumptech.glide.load.resource.file.*;
//import com.bumptech.glide.samples.svg.*;
//import com.caverock.androidsvg.*;
//import com.ghostsq.commander.*;
//import com.ghostsq.commander.utils.*;
//import com.ortiz.touch.*;
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import net.gnu.util.*;
//import android.provider.MediaStore;
//import android.database.Cursor;
//
//public class PhotoFragment2 extends Frag implements View.OnTouchListener,
//GestureDetector.OnDoubleTapListener {
//
//	private static final String TAG = "PhotoFragment";
//	
//	private TouchImageView image_view;
//	private TextView name_view;
//	public  int       ca_pos = -1;
//    private PointF    last;
//    public  Handler   h = new Handler();
//    public  boolean   touch = false;
//    //public  CommanderAdapter  ca;
//    public  ProgressDialog pd; 
//    //private CommanderStub     stub;
//
//	public PhotoFragment2() {
//		super();
//		type = Frag.TYPE.PHOTO.ordinal();
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		//setRetainInstance(true);
//		super.onCreate(savedInstanceState);
//	}
//
//    /**
//     * 
//     * Called when the activity is first created.
//     */
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView " + title + ", " + savedInstanceState);
//		View v = inflater.inflate(R.layout.imageview, container, false);
//
////		LinearLayout mLinearLayout = new LinearLayout(activity);
////		ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(
////			ViewGroup.LayoutParams.MATCH_PARENT,
////			ViewGroup.LayoutParams.MATCH_PARENT);
////		mLinearLayout.setLayoutParams(layoutParams);
////        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
////		
////		name_view = new TextView(activity);
////		final int density = (int)(4 * getResources().getDisplayMetrics().density);
////		name_view.setPadding(density, 0, density, 0);
////		name_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
////		layoutParams = new LinearLayout.LayoutParams(
////		LayoutParams.WRAP_CONTENT, 
////		LayoutParams.WRAP_CONTENT, 
////		Gravity.CENTER_HORIZONTAL );
////		
////		name_view.setLayoutParams(layoutParams);
////		name_view.setSingleLine(true);
////		name_view.setTextColor(ExplorerActivity.TEXT_COLOR);
////		name_view.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
////		mLinearLayout.addView(name_view);
////		
////		final View v = new View(activity);
////		layoutParams = new LinearLayout.LayoutParams(
////			ViewGroup.LayoutParams.MATCH_PARENT,
////			density/4);
////		v.setLayoutParams(layoutParams);
////		v.setBackgroundColor(ExplorerActivity.LIGHT_GREY);
////		mLinearLayout.addView(v);
////		
////		FrameLayout fl = new FrameLayout(activity);
////		layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
////		LayoutParams.MATCH_PARENT);
////		
////		fl.setLayoutParams(layoutParams);
////		image_view = new TouchImageView(activity);
////		layoutParams = new FrameLayout.LayoutParams(
////			ViewGroup.LayoutParams.MATCH_PARENT,
////			ViewGroup.LayoutParams.MATCH_PARENT,
////			Gravity.CENTER_HORIZONTAL);
////		image_view.setLayoutParams(layoutParams);
////		image_view.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
////		fl.addView(image_view);
////		mLinearLayout.addView(fl);
//
//		return v;
//    }
//
//    @Override
//    public void onViewCreated(View v, Bundle savedInstanceState) {
//        super.onViewCreated(v, savedInstanceState);
//		Bundle args = getArguments();
//		Log.d(TAG, "onViewCreated " + title + ", " + "args=" + args + ", " + savedInstanceState);
//		touch = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
//
//		//image_view = (TouchImageView)v.findViewById(R.id.imageview);
//		name_view = (TextView)v.findViewById(R.id.statusView);
//		//name_view.setTextColor(ExplorerActivity.TEXT_COLOR);
//		name_view.setSingleLine(true);
//		name_view.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//		if (touch) {
//			image_view.setOnDoubleTapListener(this);
//		} 
//		//image_view.setVisibility( View.GONE );
//		image_view.setOnTouchListener(this);
//		//stub = new CommanderStub();
////		if (args != null) {
////			title = args.getString("title");
////			path = args.getString("path");
////		}
////		if (savedInstanceState != null) {
////			title = savedInstanceState.getString("title");
////			path = savedInstanceState.getString("path");
////		}
//        Intent intent = getActivity().getIntent();
//		if (intent != null) {
//			Uri extras = intent.getData();
//			if (extras != null) {
//				CURRENT_PATH = extras.getPath();
//			}
//		}
//		updateColor(null);
//		Log.d(TAG, "path " + CURRENT_PATH);
//		load(CURRENT_PATH);
//	}
//
////	@Override
////	public void onSaveInstanceState(android.os.Bundle outState) {
////		outState.putString("path", path);
////		outState.putString("title", title);
////		Log.d(TAG, "onSaveInstanceState" + path + ", " + outState);
////		super.onSaveInstanceState(outState);
////	}
//
//	@Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if (touch && image_view.isZoomed()) return false;
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            last = new PointF(event.getX(), event.getY());
//            return true;
//        }
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            if (last == null) return false;
//            float ady = Math.abs(event.getY() - last.y);
//            int thldX = v.getWidth() / 50;
//            int thldY = v.getHeight() / 50;
//
//            if (thldY < 50) {
//                float x = event.getX();
//                float dx = x - last.x;
//                float adx = Math.abs(dx);
//                if (adx > thldX)
//                    loadNext(dx < 0);
//            }
//            last = null;
//            return true;
//        }
////        image_view.performClick();
//        return false;
//    }
//
//	private final void loadNext(boolean forward) {
//        //loadNext( forward ? 1 : -1, false );
//    }
//
////  public final void loadNext( int dir, boolean exit_at_end ) {
////	String[] projection = {MediaStore.Images.Thumbnails._ID};
////	Cursor cursor = activity.managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Thumbnails._ID + "");
////	columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
////	
////	cursor.moveToPosition(position);
////	int imageID = cursor.getInt(columnIndex);
////	images.setImageURI(Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID));
////	//images.setBackgroundResource(R.drawable.mb__messagebar_divider);
////	Switch.showNext();
////	
////        //Log.d( TAG, "pos=" + ca_pos + " forward=" + dir );
////        if( ca_pos < 0 || ca == null ) {
////            if( exit_at_end ) //this.finish();
////            return;
////        }
////        int orig_pos = ca_pos; 
////        while( true ) {
////            ca_pos += dir;
////            if( ca_pos <= 0 ) {
////                ca_pos = orig_pos;
////                if( exit_at_end ) //this.finish();
////                return;
////            } 
////            Uri pos_uri = ca.getItemUri( ca_pos );
////            if( pos_uri == null ) {
////                ca_pos = orig_pos;
////                if( exit_at_end ) //this.finish();
////                return;
////            } 
////            Log.d( TAG, "Next uri: " + pos_uri ); 
////            String name = ca.getItemName( ca_pos, false );
////            if( name == null ) {
////                Log.e( TAG, "Something is wrong, exiting" );
////                return;
////            }
////            String mime = Utils.getMimeByExt( Utils.getFileExt( name ) );
////            Log.d( TAG, "Next name: " + name + " mime: " + mime );
////            if( mime.startsWith( "image/" ) ) {
////                Log.d( TAG, "new pos=" + ca_pos );
////                name_view.setTextColor( Color.GRAY );
////                name_view.setText( getString( R.string.wait ) );
////                new LoaderThread( pos_uri, name ).start();
////                return;
////            }
////        }
////    }
//
//	@Override
//    public boolean onDoubleTap(MotionEvent arg0) {
//        return false;
//    }
//
//    @Override
//    public boolean onDoubleTapEvent(MotionEvent arg0) {
//        return false;
//    }
//
//    @Override
//    public boolean onSingleTapConfirmed(MotionEvent event) {
//        if (touch && image_view.isZoomed()) return false;
//        float x = event.getX();
//        loadNext(x > image_view.getWidth() / 2);
//        return true;
//    }
//
//    public void clone(final Frag fragO) {}
//
//	public void load(String path) {
//		Log.d(TAG, "path " + path);
//		if (path != null) {
//			this.CURRENT_PATH = path;
//			name_view.setText(path);
//			Uri uri = Uri.fromFile(new File(path));
//
//			String scheme = uri.getScheme();
//			//ca = CA.CreateAdapterInstance( uri, getContext() );            
//			String name_to_show = null; 
//
//			Uri.Builder ub = uri.buildUpon();
//			Uri p_uri = null;
//			if ("zip".equals(scheme)) {
//				String cur = uri.getFragment();
//				File cur_f = new File(cur);
//				name_to_show = cur_f.getName();
//				String parent_dir = cur_f.getParent();
//				p_uri = uri.buildUpon().fragment(parent_dir != null ? parent_dir : "").build();
//			}
////			else if( ca instanceof SAFAdapter ) {
////				p_uri = SAFAdapter.getParent( uri );
////			}
//			if ("gdrive".equals(scheme)) {
//				ca_pos = -1; // too complex parent folder calculation
//			} else {
//				ub.path("/");
//				List<String> ps = uri.getPathSegments();
//				int n = ps.size();
//				if (n > 0) n--;
//				for (int i = 0; i < n; i++) ub.appendEncodedPath(ps.get(i));
//				p_uri = ub.build();
//				name_to_show = ps.get(ps.size() - 1);
//			}
//			Log.d(TAG, "Parent dir: " + p_uri);
////			if( ca == null ) return;
////			ca.Init( stub );
////			ca.setMode( CommanderAdapter.MODE_SORTING | CommanderAdapter.MODE_SORT_DIR, 0 );//mode
//
////			com.ghostsq.commander.utils.Credentials crd = null; 
////			try {
////				crd = (com.ghostsq.commander.utils.Credentials)intent.getParcelableExtra( com.ghostsq.commander.utils.Credentials.KEY );
////				ca.setCredentials( crd );
////			} catch( Exception e ) {
////				Log.e( TAG, "on taking credentials from parcel", e );
////			}
//
//			image_view.invalidate();
////			if( p_uri != null && ca_pos > 0 ) {
////				ca.setUri( p_uri );
////				Log.d( TAG, "do read list" );
////				stub.reload_after_dir_read_done = true;
////				ca.readSource( null, null );
////			}
////			else
//			new LoaderThread(uri, name_to_show).start();
//		}
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//	public void updateColor(View rootView) {
//		getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//	}
//
//
//	final public void showWait() {
//        if (pd == null)
//            pd = ProgressDialog.show(PhotoFragment2.this.getContext(), "", getString(R.string.loading), true, true);
//    }
//    final public void hideWait() {
//        if (pd != null)
//            pd.cancel();
//        pd = null;
//    }
//    public final void setBitmapToView(Bitmap bmp, String name) {
//        try {
//            //Log.v( TAG, "Bitmap is ready" );
//            hideWait();
//            if (bmp != null) {
//                image_view.setVisibility(View.VISIBLE);
//                image_view.setImageBitmap(bmp);
//
////                if( name != null ) {
////                    name_view.setText( name );
////                }
//                return;
//            }
//        } catch ( Throwable e ) {
//            e.printStackTrace();
//        }
//    }
//
////	private class CommanderStub implements Commander {
////        boolean reload_after_dir_read_done = false;
////
////        @Override
////        public Context getContext() {
////            return PhotoFragment2.this.getContext();
////        }
////        @Override
////        public void issue( Intent in, int ret ) {
////        }
////        @Override
////        public void showError( String msg ) {
////        }
////        @Override
////        public void showInfo( String msg ) {
////        }
////        @Override
////        public void showDialog( int dialog_id ) {
////        }
////        @Override
////        public void Navigate( Uri uri, com.ghostsq.commander.utils.Credentials crd, String positionTo ) {
////        }
////        @Override
////        public void dispatchCommand( int id ) {
////        }
////        @Override
////        public void Open( Uri uri, com.ghostsq.commander.utils.Credentials crd ) {
////        }
////        @Override
////        public int getResolution() {
////            return 0;
////        }
////        @Override
////        public boolean notifyMe( Message m ) {
////            if( m.what == OPERATION_COMPLETED ) {
////                Log.d( TAG, "Completed" );
////                if( reload_after_dir_read_done )
////                    loadNext( 0, true );
////                reload_after_dir_read_done = false;
////            }
////            if( m.obj != null ) {
////                String s = null;
////                if( m.obj instanceof Bundle )
////                    s = ( (Bundle)m.obj ).getString( MESSAGE_STRING );
////                else if( m.obj instanceof String ) {
////                    s = (String)m.obj;
////                }
////                if( Utils.str( s ) ) {
////                    boolean html = Utils.isHTML( s );
////                    Toast.makeText( PhotoFragment2.this.getContext(), html ? Html.fromHtml( s ) : s, Toast.LENGTH_LONG ).show();
////                }
////            }
////            return false;
////        }
////        @Override
////        public boolean startEngine( Engine e ) {
////            e.setHandler( new Handler() {
////					@Override
////					public void handleMessage( Message msg ) {
////						if( msg.what == OPERATION_COMPLETED_REFRESH_REQUIRED ) {
////							Log.d( TAG, "Completed, need refresh" );
////							reload_after_dir_read_done = true;
////							ca.readSource( null, null );
////							notifyMe( msg );
////							return;
////						}
////					}
////				});
////            e.start();
////            return true;
////        }
////    }
//
//	private class LoaderThread extends Thread {
//        private Context ctx;
//        private byte[]  buf;
//        private Uri     u;
//        private Bitmap  bmp;
//        private String  name_to_show = null;
//
//        LoaderThread(Uri u_, String name_) {
//            ctx = PhotoFragment2.this.getContext();
//            u = u_;
//            name_to_show = name_;
//            //file_path = null;
//            setName("PictureLoader");
//        }
//
//        @Override
//        public void run() {
//            try {
//                final int BUF_SIZE = 1024 * 1024; 
//                buf = new byte[BUF_SIZE];
//                String scheme = u.getScheme();
////                if( PhotoFragment2.this.ca == null ) {
////                    Log.e( TAG, "No adapter instance!" );
////                    return;
////                }
////                if( ca.hasFeature( CommanderAdapter.Feature.LOCAL ) ) {
//				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inTempStorage = buf;
//				InputStream is = null;
//				ContentResolver cr = (ContentResolver.SCHEME_CONTENT.equals(scheme))
//					? PhotoFragment2.this.getContext().getContentResolver() : null;
//				if (!CURRENT_PATH.toLowerCase().endsWith("xml") && !CURRENT_PATH.toLowerCase().endsWith("svg")) {
//					for (int b = 1; b < 0x80000; b <<= 1) {
//                        try {
//                            options.inSampleSize = b;
////                            if( ca != null )
////                                is = new BufferedInputStream(PhotoFragment2.this.ca.getContent( u ));
////                            else 
//							if (cr != null) {
//								is = new BufferedInputStream(cr.openInputStream(u));
//							} else if ("file".equals(scheme)) {
//								is = new BufferedInputStream(new FileInputStream(new File(URI.create(u.toString()))));
//							}
//							if (is == null) {
//								Log.e(TAG, "Failed to get the content stream for: " + u);
//								return;
//							}
//							bmp = BitmapFactory.decodeStream(is, null, options);
//							if (bmp == null) continue;
//							if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR && 
//								(!Utils.str(scheme) || "file".equals(scheme))) {
//								CURRENT_PATH = u.getPath();
//								float degrees = ForwardCompat.getImageFileOrientationDegree(CURRENT_PATH);
//								if (degrees > 0) {
//									Log.d(TAG, "Rotating " + degrees);
//									Bitmap rbmp = PictureViewer.rotateBitmap(bmp, degrees);
//									if (rbmp != null) {
//										bmp = rbmp;
//									}
//								}
//							}
//
//
//                            break;
//                        } catch ( Throwable e ) {
//							e.printStackTrace();
//                        } finally {
//                            if (is != null)
//								FileUtil.close(is);
//							//PhotoFragment2.this.ca.closeStream( is );
//                        }
//                        Log.w(TAG, "Cant decode stream to bitmap. b=" + b);
//                    }
//				}
//				PhotoFragment2.this.h.post(new Runnable() {
//						@Override
//						public void run() {
//							if (CURRENT_PATH.toLowerCase().endsWith("xml") || CURRENT_PATH.toLowerCase().endsWith("svg")) {
//								Glide.clear(image_view);
//								GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder = Glide.with(getActivity())
//									.using(Glide.buildStreamModelLoader(Uri.class, getActivity()), InputStream.class)
//									.from(Uri.class)
//									.as(SVG.class)
//									.transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
//									.sourceEncoder(new StreamEncoder())
//									.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
//									.decoder(new SvgDecoder())
//									.placeholder(R.drawable.image_loading)
//									.error(R.drawable.image_error)
//									//.animate(android.R.anim.fade_in)
//									.dontAnimate()
//									.listener(new SvgSoftwareLayerSetter<Uri>());
//
//								requestBuilder
//									.diskCacheStrategy(DiskCacheStrategy.SOURCE)
//									// SVG cannot be serialized so it's not worth to cache it
//									.load(u)
//									.into(image_view);
//								Log.d(TAG, requestBuilder + ", " + CURRENT_PATH + ", " + u);
//							} else {
//								PhotoFragment2.this.setBitmapToView(bmp, name_to_show);
//								Log.d(TAG, bmp + CURRENT_PATH);
//								//bmp.recycle();
//							}
//						}
//					});
////                } else {
////                    File f = null;
////                    setPriority( Thread.MAX_PRIORITY );
////                    boolean local = CA.isLocal( scheme );
////                    if( local ) {
////                        f = new File( u.getPath() );
////                    } else {
////                        BufferedOutputStream fos = null;
////                        InputStream is = null;
////                        try {
////                            PhotoFragment2.this.h.post(new Runnable() {
////									@Override
////									public void run() {
////										PhotoFragment2.this.showWait();
////									}
////								});                
////                            // output - temporary file
////                            File pictvw_f = ctx.getDir( "pictvw", Context.MODE_PRIVATE );
////                            if( pictvw_f == null ) return;
////                            f = new File( pictvw_f, "file.tmp" );
////                            file_path = f.getAbsolutePath();
////                            fos = new BufferedOutputStream(new FileOutputStream( f ));
////                            // input - the content from adapter
////                            is = new BufferedInputStream(PhotoFragment2.this.ca.getContent( u ));
////                            if( is == null ) return;
////                            int n;
////                            boolean available_supported = is.available() > 0;
////                            while( ( n = is.read( buf ) ) != -1 ) {
////                                Thread.sleep( 1 );
////                                fos.write( buf, 0, n );
////                                if( available_supported ) {
////                                    for( int i = 1; i <= 10; i++ ) {
////                                        if( is.available() > 0 ) break;
////                                        Thread.sleep( 20 * i );
////                                    }
////                                    if( is.available() == 0 ) {
////                                        break;
////                                    }
////                                }
////                            }
////                        } catch( Throwable e ) {
////                            throw e;
////                        } finally {
////                            if( PhotoFragment2.this.ca != null && is != null ) 
////                                PhotoFragment2.this.ca.closeStream( is );
////                            if( fos != null ) fos.close();
////                        }
////                    }
////                    if( f != null && f.exists() && f.isFile() ) {
////                        BitmapFactory.Options options = new BitmapFactory.Options();
////                        options.inTempStorage = buf;
////                        for( int b = 1; b < 0x80000; b <<= 1 ) {
////                            try {
////                                options.inSampleSize = b;
////                                bmp = BitmapFactory.decodeFile( f.getAbsolutePath(), options );
////                            } catch( Throwable e ) {}
////                            if( bmp == null ) continue;
////                            if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR ) {
////                                file_path = u.getPath();
////                                float degrees = ForwardCompat.getImageFileOrientationDegree( file_path );
////                                Log.d( TAG, "Rotating " + degrees );
////                                if( degrees > 0 ) {
////                                    Bitmap rbmp = PictureViewer.rotateBitmap( bmp, degrees );
////                                    if( rbmp != null ) {
////										bmp = rbmp;
////									}
////                                }
////                            }
////                            if( !local )
////                                f.deleteOnExit();
////                            final String name = name_to_show;
////                            PhotoFragment2.this.h.post(new Runnable() {
////									@Override
////									public void run() {
////										PhotoFragment2.this.setBitmapToView( bmp, name );
////										//bmp.recycle();
////									}
////								});                
////                            return;
////                        }
////                    }
//                //}
//            } catch ( Throwable e ) {
//                Log.e(TAG, u != null ? u.toString() : null, e);
//                final String msgText = e.getLocalizedMessage();
//                PhotoFragment2.this.h.post(new Runnable() {
//						@Override
//						public void run() {
//							hideWait();
//							Toast.makeText(PhotoFragment2.this.getContext(), msgText != null ? msgText : ctx.getString(R.string.error), 
//										   Toast.LENGTH_LONG).show();
//						}
//					});                
//            } finally {
//                PhotoFragment2.this.h.post(new Runnable() {
//						@Override
//						public void run() {
//							hideWait();
//						}
//					});                
//            }
//        }
//    }
//
////	private void loadRes(GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder,ImageView imageViewRes) {
////        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getContext().getPackageName() + "/"
////							+ R.raw.albumart);
////        requestBuilder
////			.diskCacheStrategy(DiskCacheStrategy.NONE)
////			// SVG cannot be serialized so it's not worth to cache it
////			// and the getResources() should be fast enough when acquiring the InputStream
////			.load(uri)
////			.into(imageViewRes);
////    }
////
////    private void loadNet(GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder, ImageView imageViewNet) {
////        Uri uri = Uri.parse("http://www.clker.com/cliparts/u/Z/2/b/a/6/android-toy-h.svg");
////        requestBuilder
////			.diskCacheStrategy(DiskCacheStrategy.SOURCE)
////			// SVG cannot be serialized so it's not worth to cache it
////			.load(uri)
////			.into(imageViewNet);
////    }
//}
//
