package com.frankyu.imagepicker;

import android.os.Environment;

public class Const {
	public static final String SAVE_PATH = Environment.getExternalStorageDirectory() + "/imagepicker/";
	public static final String IMAGE_SUFFIX = ".png";
	public static final long NOTIFICATION_INTERVAL = 4 * 1000;
}