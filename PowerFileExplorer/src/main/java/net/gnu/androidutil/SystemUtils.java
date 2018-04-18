package net.gnu.androidutil;

import java.io.*;
import android.util.*;
import java.util.*;
import android.hardware.*;
import android.content.*;
import android.view.*;
import android.content.res.*;
import android.content.pm.*;
import net.gnu.util.*;
import android.widget.*;
import android.os.Build;
import java.lang.reflect.*;
import android.telephony.*;

public class SystemUtils {
	
	private static final String TAG = "SystemUtils";
	
	public static ArrayList<CharSequence> getBuild() {
		StringBuilder sb;
		ArrayList<CharSequence> l = new ArrayList<>();
		Class c = Build.class;
		Field[] f = c.getFields();
		for (Field ff : f) {
			try {
				ff.setAccessible(true);
				if (ff.getType().isArray()) {
					String[] s = (String[]) ff.get(c);
					sb = new StringBuilder();
					for (String ss : s) {
						sb.append(ff.getName().toUpperCase() + ": " + ss + "\n");
					}
					if (sb.length() > 0) {
						sb.setLength(sb.length() - 1);
					}
				} else {
					sb = new StringBuilder(ff.getName().toUpperCase() + ": " + ff.get(c));
				}
				if (sb.length() > 0) {
					l.add(sb);
				}
			}
			catch (IllegalArgumentException e) {}
			catch (IllegalAccessException e) {}
		}
		return l;
	}

	public static List<CharSequence> getHardwareInfo(Context ctx) {
		FeatureInfo[] l = ctx.getPackageManager().getSystemAvailableFeatures();
		List<CharSequence> ls = new ArrayList<>(l.length);
		PackageManager packageManager = ctx.getPackageManager();
		for (FeatureInfo ll : l) {
			if (Util.isNotNull(ll) && Util.isNotNull(ll.name)) {
				ls.add(new StringBuilder(ll.name + (packageManager.hasSystemFeature(ll.name) ? ": available" : ": not available")));
			}
		}
		return ls;
	}

    public static boolean isKeyboardPresent(Context ctx) {
    	return ctx.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS; 
    }

	public static boolean isHardwareMenuButtonPresent(Context ctx) {
        return ViewConfiguration.get(ctx).hasPermanentMenuKey();
    }

    public static boolean isNFCPresent(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    public static boolean isFrontCameraPresent(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    public static boolean isGPSPresent(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }    
	
	public static ArrayList<CharSequence>[] getSensors(Context ctx) {
		
		SensorManager mgr = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);
		
		final ArrayList<StringBuilder> ls = new ArrayList<StringBuilder>(sensors.size());
		final ArrayList<StringBuilder> ls2 = new ArrayList<StringBuilder>(sensors.size());
		
//		ConsumerIrManager ir = (ConsumerIrManager) ctx.getSystemService(Context.CONSUMER_IR_SERVICE);
//		ConsumerIrManager.CarrierFrequencyRange[] c = ir.getCarrierFrequencies();
//		
//		for (ConsumerIrManager.CarrierFrequencyRange cc : c) {
//			ls.add(new StringBuilder("Min " + cc.getMinFrequency()));
//			ls2.add(new StringBuilder("Max " + cc.getMaxFrequency()));
//		}
		
		String[] modes = new String[] {
			"REPORTING_MODE_CONTINUOUS",
			"REPORTING_MODE_ON_CHANGE",
			"REPORTING_MODE_ONE_SHOT",
			"REPORTING_MODE_SPECIAL_TRIGGER"
		};
		
		StringBuilder message2 = new StringBuilder("There are " + sensors.size() + " sensors on this device.");
		ls.add(message2);
		ls2.add(new StringBuilder());
		int i = 0;
		for(Sensor sensor : sensors) {
			final StringBuilder message = new StringBuilder();
			message.append("    Type: " + sensor.getStringType() + "\n");
			message.append("    Vendor: " + sensor.getVendor() + "\n");
			message.append("    Version: " + sensor.getVersion() + "\n");
			try {
				message.append("    Reporting Mode: " + modes[sensor.getReportingMode()] + "\n");
				message.append("    Min Delay: " + sensor.getMinDelay() + "\n");
				message.append("    Max Delay: " + sensor.getMaxDelay() + "\n");
			} catch(NoSuchMethodError e) {
				e.printStackTrace();
			} 
			
			try {
				message.append("    FIFO Max Event Count: " + sensor.getFifoMaxEventCount() + "\n");
			} catch (NoSuchMethodError e) {
				e.printStackTrace();
			} 
			try {
				message.append("    FIFO Reserved Event Count: " + sensor.getFifoReservedEventCount() + "\n");
			} catch (NoSuchMethodError e) {
				e.printStackTrace();
			}
			
			message.append("    Resolution: " + sensor.getResolution() + "\n");
			message.append("    Max Range: " + sensor.getMaximumRange() + "\n");
			message.append("    Wake up: " + sensor.isWakeUpSensor() + "\n");
			message.append("    Power: " + sensor.getPower() + " mA");
			
			final int len = message.length();
			SensorEventListener sel = new SensorEventListener(){
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
				}
				
				public void onSensorChanged(SensorEvent event) {
					float [] values = event.values;
					message.setLength(len);
					if (values.length == 1) {
						message.append("\n" + "x: " +values[ 0 ]);
					} else if (values.length == 2) {
						message.append("\n" + "x: " +values[ 0 ]+ "\ny: " +values[ 1 ]);
					} else {
						message.append("\n" + "x: " +values[ 0 ]+ "\ny: " +values[ 1 ]+ "\nz: " +values[ 2 ]);
					}
					//((SpeechListAdapter)lv.getAdapter()).notifyDataSetChanged();
				}
			};
			mgr.registerListener(sel, sensor, SensorManager.SENSOR_DELAY_NORMAL);
			ls.add(new StringBuilder(++i + ". " + sensor.getName()));
			//Log.d("ls2", message.toString());
			ls2.add(message);
		}
		return new ArrayList[] {ls, ls2};
	}

//	public static List<CharSequence>[] getSystemService(Context ctx) {
//		
//		Field[] f = Context.class.getDeclaredFields();
//		
//		final ArrayList<String> ls = new ArrayList<String>();
//		final ArrayList<StringBuilder> ls2 = new ArrayList<StringBuilder>();
//		
//		for (Field ff : f) {
//			
//			//Log.d(TAG, ff + ".");
//			
//			if (ff.getType() == String.class) {// && ff.getName().equals("SENSOR_SERVICE")) {
//				
//				StringBuilder sb = new StringBuilder();
//				
//				String service = ff.getName();
//				ls.add(service.replaceAll("_", " "));
//
//				try {
//					Object fVal = ff.get(ctx);
//					Object obj = ctx.getSystemService((String)fVal);
//
//					Log.d(TAG, service + ": " + fVal + ": " + obj + ".");
//					sb.append(new ObjectDumper(obj).dump());
//
////					if (obj != null) {
////						
////						Class<? extends Object> clazz = obj.getClass();
////						Method[] ms = clazz.getMethods();
////						
////						for (Method m : ms) {
////							try {
////								String methodName = m.getName();
////								//Log.d(TAG, clazz.getName() + ": " + methodName + ": isAccessible: " + m.isAccessible() + ": " + m);
////								m.setAccessible(true);
////								
////								if (methodName.startsWith("get") 
////									&& methodName.endsWith("List")
////									&& m.getParameterTypes().length == 1
////									&& (m.getParameterTypes()[0] == Integer.TYPE
////									|| m.getParameterTypes()[0] == Integer.class)
////									) {
////									Object invoke = m.invoke(obj, -1);
////									Log.d(TAG, clazz.getName() + "." + methodName + ": invoke: " + invoke + ": " + m);
////									sb.append(Util.toString(invoke));
////								}
////							} catch (Throwable e) {
////								Log.e(TAG, ff.getName() + ": " + e.getMessage(), e);
////							} finally {
////								ls2.add(sb);
////							}
////						}
////					}
//				} catch (Throwable e) {
//					Log.e(TAG, service + ": " + e.getMessage(), e);
//				} finally {
//					ls2.add(sb);
//				}
//			}
//		}
//		return new ArrayList[] {ls, ls2};
//	}
	
	public static String fetch_tel_status(Context cx) {
		String result = null;
		TelephonyManager tm = (TelephonyManager) cx
			.getSystemService(Context.TELEPHONY_SERVICE);//
		String str = "";
		str += "DeviceId(IMEI) = " + tm.getDeviceId() + "\n";
		str += "DeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion()
			+ "\n";
		str += "Line1Number = " + tm.getLine1Number() + "\n";
		str += "NetworkCountryIso = " + tm.getNetworkCountryIso() + "\n";
		str += "NetworkOperator = " + tm.getNetworkOperator() + "\n";
		str += "NetworkOperatorName = " + tm.getNetworkOperatorName() + "\n";
		str += "NetworkType = " + tm.getNetworkType() + "\n";
		str += "PhoneType = " + tm.getPhoneType() + "\n";
		str += "SimCountryIso = " + tm.getSimCountryIso() + "\n";
		str += "SimOperator = " + tm.getSimOperator() + "\n";
		str += "SimOperatorName = " + tm.getSimOperatorName() + "\n";
		str += "SimSerialNumber = " + tm.getSimSerialNumber() + "\n";
		str += "SimState = " + tm.getSimState() + "\n";
		str += "SubscriberId(IMSI) = " + tm.getSubscriberId() + "\n";
		str += "VoiceMailNumber = " + tm.getVoiceMailNumber() + "\n";

		int mcc = cx.getResources().getConfiguration().mcc;
		int mnc = cx.getResources().getConfiguration().mnc;
		str += "IMSI MCC (Mobile Country Code):" + String.valueOf(mcc) + "\n";
		str += "IMSI MNC (Mobile Network Code):" + String.valueOf(mnc) + "\n";
		result = str;
		return result;
	}
	
	public static boolean slientInstall(File file) {
		if (file == null) {
			return true;
		}
		boolean result = false;
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(out);
			// dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n");
			dataOutputStream
				.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
							+ file.getPath());

			dataOutputStream.flush();

			dataOutputStream.close();
			out.close();
			int value = process.waitFor();

			if (value == 0) {
				result = true;
			} else {
				result = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean slientUnInstall(String packagename) {
		boolean result = false;
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(out);
			dataOutputStream
				.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall  "
							+ packagename);
			dataOutputStream.flush();
			dataOutputStream.close();
			out.close();
			int value = process.waitFor();
			if (value == 0) {
				result = true;
			} else {
				result = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

}
