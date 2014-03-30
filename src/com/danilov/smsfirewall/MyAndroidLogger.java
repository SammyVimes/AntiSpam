package com.danilov.smsfirewall;

import android.os.Environment;

import com.danilov.logger.ILogger;
import com.danilov.logger.Logger;

public class MyAndroidLogger {

	public static String PATH = "/antispamlogs/"; 
	public static String fileName = "log.txt";
	static {
		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		PATH = sdPath + PATH;
	}
	private static ILogger logger = new Logger(PATH, fileName);
	private String className;
	
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
