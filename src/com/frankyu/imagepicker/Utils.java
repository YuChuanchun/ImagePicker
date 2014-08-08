package com.frankyu.imagepicker;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.widget.Toast;

public class Utils {
	private static final String IMAGE_PICKER = "image_picker";

	public static boolean isSDCardAvailable() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkSDCard(Context context) {
		if (!isSDCardAvailable()) {
			Toast.makeText(context, "SD卡不可用", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private static final String PREF_KEY_FILE_PATH= "FILE_PATH";
	public static void setFILE_PATH(Context context, String path) {
		SharedPreferences pref = context.getSharedPreferences(IMAGE_PICKER, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(PREF_KEY_FILE_PATH, path);
		editor.commit();
	}
	
	public static String getFILE_PATH(Context context) {
		SharedPreferences pref = context.getSharedPreferences(IMAGE_PICKER, Activity.MODE_PRIVATE);
		return pref.getString(PREF_KEY_FILE_PATH, null);
	}
	
    public static int getNetworkType(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return (networkInfo == null || !networkInfo.isConnected()) ? -1 : networkInfo.getType();
    }
   
    public static boolean checkNetworkAailable(Context context) {
		if (getNetworkType(context) == -1) {
			Toast.makeText(context, "网络不可用，请检查网络", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
    }
    
    public static void showNotification(Context context, String content) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ic_launcher, content, System.currentTimeMillis());
		n.flags = Notification.FLAG_AUTO_CANCEL;
//		n.defaults = Notification.DEFAULT_SOUND;
		nm.cancel(R.string.app_name);
		n.setLatestEventInfo(context, "ImagePicker", content, null);
		nm.notify(R.string.app_name, n);
    }
}