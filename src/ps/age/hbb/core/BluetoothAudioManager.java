package ps.age.hbb.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
/*
 * Class to determain the availability of a Bluetooth audio source
 */
public class BluetoothAudioManager {
	private Context mContext;
	private boolean canUseBluetooth;
	private boolean useBluetooth;
	private static final String tag = BluetoothAudioManager.class.getSimpleName();
	private AudioManager audioManager;
	
	public BluetoothAudioManager(Context context){
		mContext = context;
		audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		canUseBluetooth  = audioManager.isBluetoothScoAvailableOffCall();
		if(canUseBluetooth){
			mContext.registerReceiver(bluetoothReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
			audioManager.startBluetoothSco();
		}
	}
	public boolean isBluetoothAvailable(){
		Log.e(tag, "useBluetooth: "   +useBluetooth);
		Log.e(tag, "canUseBluetooth:" +canUseBluetooth);

		return useBluetooth&canUseBluetooth;
	}
	public void finish(){
		if(canUseBluetooth)
			mContext.unregisterReceiver(bluetoothReceiver);

		audioManager.stopBluetoothSco();
		
	}
	
	BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            Log.d(tag, "Audio SCO state: " + state);

            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) { 
                /* 
                 * Now the connection has been established to the bluetooth device. 
                 * Record audio or whatever (on another thread).With AudioRecord you can record with an object created like this:
                 * new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 * AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
                 *
                 * After finishing, don't forget to unregister this receiver and
                 * to stop the bluetooth connection with am.stopBluetoothSco();
                 */
            	useBluetooth = true;
                
            }
            if(AudioManager.SCO_AUDIO_STATE_DISCONNECTED == state){
            	useBluetooth = false;
            }

        }
    };
}
