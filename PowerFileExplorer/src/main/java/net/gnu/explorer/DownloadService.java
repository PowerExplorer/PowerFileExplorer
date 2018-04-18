//package net.gnu.explorer;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.URL;
//
//import android.app.Activity;
//import android.app.IntentService;
//import android.app.Service;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Message;
//import android.os.Messenger;
//import android.util.Log;
//import java.io.*;
//import android.app.*;
//import android.widget.*;
//
//public class DownloadService extends IntentService {
//
//	private int result = Activity.RESULT_CANCELED;
//	public static final String SRC = "urlpath";
//	public static final String FILENAME = "filename";
//	public static final String FILEPATH = "filepath";
//	public static final String RESULT = "result";
//	public static final String NOTIFICATION = "com.vogella.android.service.receiver";
//
//	public DownloadService() {
//		super("DownloadService");
//	}
//
//	// called asynchronously be Android
//	@Override
//	protected void onHandleIntent(Intent intent) {
//		String urlPath = intent.getStringExtra(SRC);
//		String fileName = intent.getStringExtra(FILENAME);
//		Log.d("ds", intent + ", " + urlPath + ". " + fileName);
//		File output = new File(fileName);
//		if (output.exists()) {
//			output.delete();
//		}
//		InputStream stream = null;
//		OutputStream fos = null;
//		File file = new File(urlPath);
//		final long len = file.length();//output.length();
//		Log.d("ds", file.exists() + ", " + urlPath + " = " + len);
//		if (len > 0) {
//			try {
//				//URL url = new URL(urlPath);
//				String name = output.getName();
//				stream = new FileInputStream(urlPath);//url.openConnection().getInputStream();
//				stream = new BufferedInputStream(stream);
//				fos = new BufferedOutputStream(new FileOutputStream(output));
//				int next = -1;
//				long read = 0;
//				final byte[] buf = new byte[65535];
//				while ((next = stream.read(buf)) != -1) {
//					fos.write(buf, 0, next);
//					createNotification(name, (int)((read += next) * 100 / len));
//				}
//				result = Activity.RESULT_OK;
//			} catch (Throwable e) {
//				e.printStackTrace();
//			} finally {
//				if (stream != null) {
//					try {
//						stream.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				if (fos != null) {
//					try {
//						fos.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//		
//		publishResults(output.getAbsolutePath(), result);
//	}
//
//	public void createNotification(String fName, int done) {
//
//		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//		Notification notification = new Notification(R.drawable.icon,
//													 "A new notification", System.currentTimeMillis());
//		
//		notification.flags |= Notification.FLAG_AUTO_CANCEL;
//
//		RemoteViews view = new RemoteViews(getPackageName(), R.layout.main);
//		view.setProgressBar(R.id.progressBar1, 100, done, false);
//		view.setTextViewText(R.id.textView1,
//							 "Copying " + fName + "..." + done + " %");
//		notification.contentView = view;
//
//		Intent intent = new Intent(this, MainActivity.class);
//		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//																intent, PendingIntent.FLAG_CANCEL_CURRENT);
//		notification.contentIntent = pendingIntent;
//		notificationManager.notify(0, notification);
//
//		
//
//	}
//
//	private void publishResults(String outputPath, int result) {
//		Intent intent = new Intent(NOTIFICATION);
//		intent.putExtra(FILEPATH, outputPath);
//		intent.putExtra(RESULT, result);
//		sendBroadcast(intent);
//	}
//}
