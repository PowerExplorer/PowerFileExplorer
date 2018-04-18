//package net.gnu.explorer;
//import android.app.*;
//import android.widget.*;
//import android.content.*;
//import android.os.*;
//import java.util.regex.*;
//import android.view.View.*;
//import android.view.*;
//import com.artifex.mupdfdemo.*;
//
//public class GeneratedText extends Activity {
//
//	private Button close;
//	private Button clear;
//	private TextView tv;
//	private String data;
//	String text = "";
//	Intent i;
//	Context mContext;
//	//    MuPDFPageView pdfview = new MuPDFPageView(mContext, null, null);
//    private EditText edit;
//	private Button undo;
//	public static GeneratedText screen;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_generated_text);
//
//		close = (Button)findViewById(R.id.close);
//		clear = (Button)findViewById(R.id.clear);
//		tv = (TextView)findViewById(R.id.text1);
//		edit = (EditText)findViewById(R.id.edit);
//		undo = (Button)findViewById(R.id.undo);
//		undo.setEnabled(false);
//
//		i = getIntent();
//
//		data = i.getStringExtra("data");
//
//		tv.setText(data);
//		String mypattern = "Name and address of the Employee \n";
//
//		Pattern p = Pattern.compile(mypattern, Pattern.DOTALL);
//		if (data.matches(mypattern)) {
//			System.out.println("Start Printing name");
//		} else
//        //do nothing
//			edit.setText(data);
//
//		System.out.println("hello user " + "/n" + "user1" + "\n" + "user2");
//
//		SharedPreferences pref = getSharedPreferences("key", 0);
//		SharedPreferences.Editor editor = pref.edit();
//		editor.putString("text", data);
//		editor.commit();
//
//		clear.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					tv.setText("");
//					edit.setText("");
//					undo.setEnabled(true);
//				}
//			});
//		close.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					finish();
//				}
//			});
//		undo.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					String value = "";
//					SharedPreferences pref = getSharedPreferences("key", 0);
//					value = pref.getString("text", value);
//					edit.setText(value);
//					tv.setText(value);
//					undo.setEnabled(false); 
//				}
//			});
//
//	}
//
//	//1.GeneratedText activity
//	//1. now in mupdfactivity write this
//
//	public void Showtext() {
//		destroyAlertWaiter();
//		core.stopAlerts();
//
//		MuPDFPageView pdfview = new MuPDFPageView(GeneratedText.this, core, null);
//		String data = "";
//		pdfview.setFocusable(true);
//		data = pdfview.getSelectedText();
//		Intent i = new Intent(getApplicationContext(), GeneratedText.class);
//		i.putExtra("data", data);
//
//		startActivity(i); 
//
//	}
//	// call Showtext in OnAcceptButtonClick and you will get your text.
//}
