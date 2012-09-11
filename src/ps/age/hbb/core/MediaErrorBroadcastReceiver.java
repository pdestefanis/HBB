package ps.age.hbb.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MediaErrorBroadcastReceiver extends BroadcastReceiver {
	public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
	MediaRecordEventListener mListener;
	
	@Override
	public void onReceive(Context arg0, Intent intent) {
		if(mListener == null)
			return;
		
		if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)){
			mListener.onReady();
		}
		if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
			mListener.onError();
		}
		if(intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)){
			mListener.onError();
		}
		if(intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)){
			mListener.onError();
		}
		if(intent.getAction().equals(Intent.ACTION_MEDIA_SHARED)){
			mListener.onError();
		}
		if(intent.getAction().equals(ACTION_PHONE_STATE_CHANGED)){
			Bundle extras = intent.getExtras();
			if (extras != null) {
			  String state = extras.getString(TelephonyManager.EXTRA_STATE);
			  Log.w("DEBUG", state);
			  if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				  mListener.onError();
			  }
			  if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
					mListener.onReady();
			  }
			  if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
					mListener.onReady();
			  }
			}
		}
		
	}
	public void setMediaRecordEventListener(MediaRecordEventListener listener){
		mListener = listener;
	}

}

/*
Bundle extras = intent.getExtras();
if (extras != null) {
  String state = extras.getString(TelephonyManager.EXTRA_STATE);
  Log.w("DEBUG", state);
  if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
    String phoneNumber = extras
        .getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
    Log.w("DEBUG", phoneNumber);
  }
}
*/