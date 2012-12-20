package ps.age.util;

public class Log {
	private static Logger mLogger;
	public void setLogger(Logger logger){
		mLogger = logger;
	}
	public static final boolean LOG_ENABLED = true;
	
	public static void e(String tag, String msg){
		if(LOG_ENABLED){
			android.util.Log.e(tag, msg);
		}
	}
	public static void d(String tag, String msg){
		if(LOG_ENABLED){
			android.util.Log.d(tag, msg);
		}
	}
	public static void i(String tag, String msg){
		if(LOG_ENABLED){
			android.util.Log.i(tag, msg);
		}
	}
	public static void w(String tag, String msg){
		if(LOG_ENABLED){
			android.util.Log.w(tag, msg);
			
		}
	}
	public static void v(String tag, String msg){
		if(LOG_ENABLED){
			android.util.Log.v(tag, msg);
			
		}
	}
}
