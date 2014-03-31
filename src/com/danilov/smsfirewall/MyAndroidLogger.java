package com.danilov.smsfirewall;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;

import com.danilov.logger.ILogger;
import com.danilov.logger.Logger;

public class MyAndroidLogger {

	public static String LOG_PATH = "/antispamlogs/"; 
	public static String PATH = ""; 
	public static String fileName = "log.txt";
	static {
	    String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			PATH = getSdPath();
		} else {
			PATH = getInternalPath();
		}
	}
	private static ILogger logger = new Logger(PATH, fileName);
	private String className;
	
	public static String getSdPath() {
		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		sdPath = sdPath + LOG_PATH;
		return sdPath;
	}
	
	public static String getInternalPath() {
		Context context = MyApplication.getAppContext();
		ContextWrapper cw = new ContextWrapper(context);
		return cw.getDir("logs", Context.MODE_WORLD_READABLE).getAbsolutePath() + "/";
	}
	
	public MyAndroidLogger(final Class type) {
		this.className = type.getSimpleName();
	}
	
	public MyAndroidLogger(final String className) {
		this.className = className;
	}

	public void log(String message) {
		logger.log(message, className);
	}

	public void log(String tag, String message) {
		logger.log(tag, message, className);
	}
	
}
