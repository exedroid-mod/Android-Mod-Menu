//Credit: Raunak Mods - https://t.me/raunakmods786

package com.android.support;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;

public final class CrashHandler implements UncaughtExceptionHandler {
    private final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();
	private File crashFile;
	private String dirName, versionName, versionCode;
	private Context app;

    public CrashHandler(Context context) {
		app = context;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
			dirName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
		else
			dirName = app.getExternalFilesDir(null).getAbsolutePath();
		try {
			PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
			versionName = packageInfo.versionName;
			versionCode = String.valueOf(Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode() : packageInfo.versionCode);
		}
		catch (Exception ignored) {
			versionName = versionCode = "unknown";
		}
    }

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		Log.e("AppCrash", "Error just lunched");
		try {
			tryUncaughtException(thread, throwable);
		}
		catch (Exception e) {
			e.printStackTrace();
			if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
				DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
			else
				exit();
		}
	}

	private void tryUncaughtException(Thread thread, Throwable throwable) throws IOException {
		Log.e("AppCrash", "Try saving log");

		final String time = new SimpleDateFormat("yyyy_MM_dd-HH:mm").format(System.currentTimeMillis());
		final String fileName = "mod_menu_crash_" + time + ".txt";
		crashFile = new File(dirName, fileName);

		StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        if (cause != null)
            cause.printStackTrace(printWriter);
        printWriter.close();

        String fullStackTrace = writer.toString();
		Log.e("Crash Log", fullStackTrace);

		StringBuilder devInfo = new StringBuilder();
		devInfo.append("************* Crash Head ****************");
		devInfo.append("\nTime Of Crash      : ").append(time);
		devInfo.append("\nDevice Manufacturer: ").append(Build.MANUFACTURER);
		devInfo.append("\nDevice Model       : ").append(Build.MODEL);
		devInfo.append("\nAndroid Version    : ").append(Build.VERSION.RELEASE);
		devInfo.append("\nAndroid SDK        : ").append(Build.VERSION.SDK_INT);
		devInfo.append("\nApp VersionName    : ").append(versionName);
		devInfo.append("\nApp VersionCode    : ").append(versionCode);
		devInfo.append("\n************* Crash Log ****************\n");
		devInfo.append(fullStackTrace);

		String errorLog = devInfo.toString();

		writeFile(crashFile, errorLog);

		Toast.makeText(app, "Game has crashed unexpectedly", Toast.LENGTH_SHORT).show();
		Toast.makeText(app, "Log saved to: " + crashFile.getName(), Toast.LENGTH_LONG).show();

		Log.e("AppCrash", "Done");
		exit();
	}

	private void exit() {
		Process.killProcess(Process.myPid());
		// If process is still alive
		System.exit(2);
	}

	private void writeFile(File file, String content) throws IOException {
		File parentFile = file.getParentFile();
		FileOutputStream fos = null;
		if (parentFile != null && !parentFile.exists())
			parentFile.mkdirs();
		try {
			fos = new FileOutputStream(file);
			fos.write(content.getBytes());
		}
		finally {
			fos.close();
		}
	}
}

