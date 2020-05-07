package com.mega.oceanplayer;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

    private static String TAG = LogUtil.class.getSimpleName();
    private static String app_data_dir = "/sdcard/com.mega.oceanplayer/";
    private static String log_file_name = "oceanplayer.log";
    private static Integer logLevel = Log.DEBUG;

    private static OutputStreamWriter writer = null;

    public static void init()  {
        LogUtil.logLevel = BuildConfig.LOG_LEVEL;
        try {
            File dir = new File(app_data_dir);
            if(!dir.exists()) {
                dir.mkdirs();
            }
            File logfile = new File(dir, log_file_name);
            if (!logfile.exists()) {
                logfile.createNewFile();
            }
            writer = new OutputStreamWriter(new FileOutputStream(logfile), "GBK");
        }
        catch(IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.getStackTrace().toString());
            writer = null;
        }
    }

    public static void close() {
        if(writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                Log.e(TAG, ex.toString());
            }
            finally {
                writer = null;
            }
        }
    }

    public static void v(String tag, String logText)  {
        if(logLevel <= Log.VERBOSE) {
            Log.v(tag, String.format("%s PID=%d TID=%d : %s", getTime(), android.os.Process.myPid(), android.os.Process.myPid(), logText));
            write(Log.VERBOSE, tag, logText);
        }
    }

    public static void d(String tag, String logText)  {
        if(logLevel <= Log.DEBUG) {
            Log.d(tag, String.format("%s PID=%d TID=%d : %s", getTime(), android.os.Process.myPid(), android.os.Process.myPid(), logText));
            write(Log.DEBUG, tag, logText);
        }
    }

    public static void i(String tag, String logText)  {
        if(logLevel <= Log.INFO) {
            Log.i(tag, String.format("%s PID=%d TID=%d : %s", getTime(), android.os.Process.myPid(), android.os.Process.myPid(), logText));
            write(Log.INFO, tag, logText);
        }
    }

    public static void w(String tag, String logText)  {
        if(logLevel <= Log.WARN) {
            Log.w(tag, String.format("%s PID=%d TID=%d : %s", getTime(), android.os.Process.myPid(), android.os.Process.myPid(), logText));
            write(Log.WARN, tag, logText);
        }
    }

    public static void e(String tag, String logText)  {
        if(logLevel <= Log.ERROR) {
            Log.e(tag, String.format("%s PID=%d TID=%d : %s", getTime(), android.os.Process.myPid(), android.os.Process.myPid(), logText));
            write(Log.ERROR, tag, logText);
        }
    }

    private static void write(int logLevel, String tag, String logText) {
        try {
            if(writer != null) {
                String logLevelStr = getLogLevelStr(logLevel);
                writer.write(String.format("+++ %s %s PID=%d TID=%d %s: %s", getTime(), tag, android.os.Process.myPid(), android.os.Process.myPid(), logLevelStr, logText));
                writer.flush();
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private static String getLogLevelStr(int logLevel) {
        switch(logLevel) {
            case Log.VERBOSE: return "VERBOSE";
            case Log.DEBUG: return "DEBUG";
            case Log.INFO: return "INFO";
            case Log.WARN: return "WARN";
            case Log.ERROR: return "ERROR";
            default: return "UNKNOWN";
        }
    }

    public static String getTime() {
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }

    public static String getMusicServerErrorTxtByCode(Integer errCode) {
        switch (errCode) {
            case -1004:
                return "MEDIA_ERROR_IO";
            case -1007:
                return "MEDIA_ERROR_MALFORMED";
            case 200:
                return "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
            case 100:
                return "MEDIA_ERROR_SERVER_DIED";
            case -110:
                return "MEDIA_ERROR_TIMED_OUT";
            case 1:
                return "MEDIA_ERROR_UNKNOWN";
            case -1010:
                return "MEDIA_ERROR_UNSUPPORTED";
            default:
                return "UNKNOWN_ERROR_CODE";
        }
    }

    public static String getMusicServerExtraTextByCode(Integer extraCode) {
        switch (extraCode) {
            case 800:
                return "MEDIA_INFO_BAD_INTERLEAVING";
            case 702:
                return "MEDIA_INFO_BUFFERING_END";
            case 701:
                return "MEDIA_INFO_METADATA_UPDATE";
            case 802:
                return "MEDIA_INFO_METADATA_UPDATE";
            case 801:
                return "MEDIA_INFO_NOT_SEEKABLE";
            case 1:
                return "MEDIA_INFO_UNKNOWN";
            case 3:
                return "MEDIA_INFO_VIDEO_RENDERING_START";
            case 700:
                return "MEDIA_INFO_VIDEO_TRACK_LAGGING";
            default:
                return "UNKNOWN_ERROR_CODE";
        }
    }
}
