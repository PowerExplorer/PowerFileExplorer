//package net.gnu.explorer;
//
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.afollestad.easyvideoplayer.EasyVideoCallback;
//import com.afollestad.easyvideoplayer.EasyVideoPlayer;
//import com.afollestad.materialdialogs.MaterialDialog;
//import android.view.*;
//import android.content.*;
//import java.io.*;
//
//public class VideoFragment extends Frag implements EasyVideoCallback {
//	
//	private EasyVideoPlayer player;
//	
//	public VideoFragment() {
//		super();
//		type = Frag.TYPE.MEDIA;
//		TAG = "VideoFragment";
//	}
//
//    /**
//     * 
//     * Called when the activity is first created.
//     */
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		//setRetainInstance(true);
//        Log.d(TAG, "onCreateView " + title + ", " + savedInstanceState);
//		View v = inflater.inflate(R.layout.video, container, false);
//		
//		return v;
//    }
//
//    @Override
//    public void onViewCreated(final View v, Bundle savedInstanceState) {
//        super.onViewCreated(v, savedInstanceState);
//		Bundle args = getArguments();
//		Log.d(TAG, "onViewCreated " + title + ", " + "args=" + args + ", " + savedInstanceState);
//		
//		player = (EasyVideoPlayer) v.findViewById(R.id.player);
////        assert player != null;
//        player.setCallback(this);
//		player.setAutoPlay(true);
//		if (args != null) {
//			title = args.getString("title");
//			url = args.getString("url");
//		}
//		if (savedInstanceState != null) {
//			title = savedInstanceState.getString("title");
//			url = savedInstanceState.getString("url");
//		}
//        Intent intent = getActivity().getIntent();
//		if (intent != null) {
//			Uri extras = intent.getData();
//			if (extras != null) {
//				url = extras.toString();
//			}
//		}
//		v.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		Log.d(TAG, "path " + url);
//	}
//	
//	public void load(String path) {
//		Log.d(TAG, "path " + path);
//		if (path != null) {
//			//doCleanUp();
//			//player.release();//.releaseMediaPlayer();
//			this.url = path;
//			File file = new File(path);
//			player.setCustomLabelText(file.getName());
//			player.setSource(Uri.fromFile(file));
//			player.start();
//		}
//	}
//	
//	@Override
//	public void onSaveInstanceState(android.os.Bundle outState) {
//		outState.putString("url", url);
//		outState.putString("title", title);
//		Log.d(TAG, "onSaveInstanceState" + url + ", " + outState);
//		super.onSaveInstanceState(outState);
//	}
//	
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_main);
////
////        player = (EasyVideoPlayer) findViewById(R.id.player);
////        assert player != null;
////        player.setCallback(this);
////        // All further configuration is done from the XML layout.
////    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        player.pause();
//    }
//
//	@Override
//    public void onStarted(EasyVideoPlayer player) {
//    }
//
//    @Override
//    public void onPaused(EasyVideoPlayer player) {
//    }
//
//    @Override
//    public void onPreparing(EasyVideoPlayer player) {
//        Log.d("EVP-Sample", "onPreparing()");
//    }
//
//    @Override
//    public void onPrepared(EasyVideoPlayer player) {
//        Log.d("EVP-Sample", "onPrepared()");
//    }
//
//    @Override
//    public void onBuffering(int percent) {
//        Log.d("EVP-Sample", "onBuffering(): " + percent + "%");
//    }
//
//    @Override
//    public void onError(EasyVideoPlayer player, Exception e) {
//        Log.d("EVP-Sample", "onError(): " + e.getMessage());
//        new MaterialDialog.Builder(getContext())
//			.title(R.string.error)
//			.content(e.getMessage())
//			.positiveText(android.R.string.ok)
//			.show();
//    }
//
//    @Override
//    public void onCompletion(EasyVideoPlayer player) {
//        Log.d("EVP-Sample", "onCompletion()");
//    }
//
//    @Override
//    public void onRetry(EasyVideoPlayer player, Uri source) {
//        Toast.makeText(getContext(), "Retry", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onSubmit(EasyVideoPlayer player, Uri source) {
//        Toast.makeText(getContext(), "Submit", Toast.LENGTH_SHORT).show();
//    }
//
//    
//}
