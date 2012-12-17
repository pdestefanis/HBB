package ps.age.hbb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ps.age.hbb.core.RecordItem;
import ps.age.hbb.core.DBWraper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class HBBActivity extends Activity implements OnSeekBarChangeListener,
		OnCompletionListener, OnPreparedListener {
	public static final String tag = HBBActivity.class.getSimpleName();
	public static final int CLEAR_DIALOG = 12456;

	private MediaPlayer mPlayer;
	private Handler mHandler;
	private RecordItem mItem;
	private ImageView mBack;
	private ImageView mPlay;
	private ImageView mFirst;
	private ImageView mSecond;
	private ImageView mThird;
	private ImageView mFourth;
	private SeekBar seekBar;
	private TextView firstText;
	private TextView secondText;
	private TextView thirdText;
	private TextView fourthText;
	private TextView currentTime;
	private TextView endTime;
	private TextView reviewForm;
	private SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
	private Date date = new Date();

	private boolean isPlaying;
	private boolean isReady = false;
	private DBWraper mWraper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(tag, "onCreate");
		mWraper = new DBWraper(this);
		mItem = (RecordItem) getIntent().getSerializableExtra("item");
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

		initializeUI();

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if ((msg.what == CLEAR_DIALOG) && (msg.arg1 == RESULT_OK)) {
					mItem.setMark(0, 0);
					mItem.setMark(1, 0);
					mItem.setMark(2, 0);
					mItem.setMark(3, 0);
					updateUI();
				}
			}
		};

		PrepairPlayer();
	}

	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.e(tag, "onConfigurationChanged");

		initializeUI();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(tag, "onResume");
		if (mPlayer != null) {
			seekBar.setProgress(mPlayer.getCurrentPosition());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.e(tag, "onPause, is finishing? "+String.valueOf(isFinishing()));

		isPlaying = false;
		if (isFinishing()) {
			if (mPlayer != null) {

				if (mPlayer.isPlaying()) {
					Log.w(tag, "mPlayer is still playing , trying to stop it");
					mPlayer.stop();
				}
				Log.w(tag, "mPlayer release");				
				mPlayer.release();
				mPlayer = null;

			}

			new Thread() {
				@Override
				public void run() {
					try{
						Log.w(tag, "Background thread");
						mWraper.updateRecord(mItem);
						mWraper.close();
					}catch(Exception e){
						Log.e(tag, "this should never happen "+e.toString());
						e.printStackTrace();
					}
				}
			}.start();
		} else {
			if (mPlayer.isPlaying())
				mPlayer.pause();
			mPlay.setImageResource(android.R.drawable.ic_media_play);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e(tag, "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(tag, "onDestroy");

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(tag, "onActivityResult");
		if ((requestCode == ExtraActivity.REQUEST_CODE) && (data != null)) {
			mItem = (RecordItem) data.getSerializableExtra(ExtraActivity.ITEM);
			Log.e(tag, "mItem extra " + mItem.getExtra());
		}

	}

	/*
	 * handle UI initialization when the activity is created or when the UI
	 * configurations has changed from portrait to landscape for ex.
	 */
	private void initializeUI() {

		setContentView(R.layout.hbb);

		mFirst = (ImageView) findViewById(R.id.mark1);
		mSecond = (ImageView) findViewById(R.id.mark2);
		mThird = (ImageView) findViewById(R.id.mark3);
		mFourth = (ImageView) findViewById(R.id.mark4);
		endTime = (TextView) findViewById(R.id.time_end);
		reviewForm = (TextView) findViewById(R.id.form_textView);
		mBack = (ImageView) findViewById(R.id.back);

		if (mPlay == null) {
			mPlay = (ImageView) findViewById(R.id.play);

			seekBar = (SeekBar) findViewById(R.id.seekBar_player);

			firstText = (TextView) findViewById(R.id.mark1_time);
			secondText = (TextView) findViewById(R.id.mark2_time);
			thirdText = (TextView) findViewById(R.id.mark3_time);
			fourthText = (TextView) findViewById(R.id.mark4_time);
			currentTime = (TextView) findViewById(R.id.time_current);

			seekBar.setEnabled(false);

		} else {
			Drawable draw = mPlay.getDrawable();
			mPlay = (ImageView) findViewById(R.id.play);
			mPlay.setImageDrawable(draw);

			int value = currentTime.getVisibility();
			CharSequence txt = currentTime.getText();
			currentTime = (TextView) findViewById(R.id.time_current);
			currentTime.setVisibility(value);
			currentTime.setText(txt);

			txt = firstText.getText();
			firstText = (TextView) findViewById(R.id.mark1_time);
			firstText.setText(txt);

			txt = secondText.getText();
			secondText = (TextView) findViewById(R.id.mark2_time);
			secondText.setText(txt);
			txt = thirdText.getText();
			thirdText = (TextView) findViewById(R.id.mark3_time);
			thirdText.setText(txt);
			txt = fourthText.getText();
			fourthText = (TextView) findViewById(R.id.mark4_time);
			fourthText.setText(txt);
			txt = currentTime.getText();
			currentTime = (TextView) findViewById(R.id.time_current);
			currentTime.setText(txt);
			value = seekBar.getProgress();
			seekBar = (SeekBar) findViewById(R.id.seekBar_player);
			seekBar.setMax(mPlayer.getDuration());
			seekBar.setProgress(value);
			date.setTime(mPlayer.getDuration());
			endTime.setText(fmt.format(date));
		}

		mPlay.setOnClickListener(listener);
		mFirst.setOnClickListener(listener);
		mSecond.setOnClickListener(listener);
		mThird.setOnClickListener(listener);
		mFourth.setOnClickListener(listener);
		seekBar.setOnSeekBarChangeListener(this);

		reviewForm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(HBBActivity.this,
						ExtraActivity.class);
				intent.putExtra(ExtraActivity.ITEM, mItem);
				startActivityForResult(intent, ExtraActivity.REQUEST_CODE);
			}
		});
		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}

		});
		updateUI();
	}

	private void updateUI() {
		if (mItem.getMark(0) != -1) {
			date.setTime(mItem.getMark(0));
			firstText.setText(fmt.format(date));
		}
		if (mItem.getMark(1) != -1) {
			date.setTime(mItem.getMark(1));
			secondText.setText(fmt.format(date));
		}
		if (mItem.getMark(2) != -1) {
			date.setTime(mItem.getMark(2));
			thirdText.setText(fmt.format(date));
		}
		if (mItem.getMark(3) != -1) {
			date.setTime(mItem.getMark(3));
			fourthText.setText(fmt.format(date));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.review_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		HBBDialog dialog = new HBBDialog(this);
		dialog.setCancelable(false);
		Message msg = mHandler.obtainMessage();
		msg.what = CLEAR_DIALOG;
		dialog.setDismissMessage(msg);
		dialog.setTitle(getResources().getString(R.string.clearDialog_title));
		dialog.setExtra(getResources().getString(R.string.clearDialog_extra));
		dialog.setButtonsText(
				getResources().getString(R.string.clearDialog_positive),
				getResources().getString(R.string.clearDialog_negative));
		dialog.show();
		return true;
	}

	public void PrepairPlayer() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mItem.getPath());
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnPreparedListener(this);
			mPlayer.prepareAsync();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HBBActivity.this, R.string.error_sdcard,
					Toast.LENGTH_LONG).show();

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HBBActivity.this, R.string.error_sdcard,
					Toast.LENGTH_LONG).show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HBBActivity.this, R.string.error_sdcard,
					Toast.LENGTH_LONG).show();
		}

	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.play:
				if (isPlaying && mPlayer.isPlaying()) {
					mPlayer.pause();
					mPlay.setImageResource(android.R.drawable.ic_media_play);
				} else {
					if (isReady) {
						mPlayer.start();
						isPlaying = true;
						mHandler.postDelayed(run, 100);
						mPlay.setImageResource(android.R.drawable.ic_media_pause);
					}

				}
				return;

			case R.id.mark1:
				if (isReady) {
					mItem.setMark(0, mPlayer.getCurrentPosition());
				}
				break;
			case R.id.mark2:
				if (isReady) {
					mItem.setMark(1, mPlayer.getCurrentPosition());
				}
				break;
			case R.id.mark3:
				if (isReady) {
					mItem.setMark(2, mPlayer.getCurrentPosition());
				}
				break;
			case R.id.mark4:
				if (isReady) {
					mItem.setMark(3, mPlayer.getCurrentPosition());
				}
				break;
			}
			updateUI();
		}

	};

	@Override
	public void onProgressChanged(SeekBar view, int position, boolean fromUser) {
		if (isReady) {

			if (fromUser) {
				mPlayer.seekTo(position);
			}

			date.setTime(mPlayer.getCurrentPosition());
			currentTime.setText(fmt.format(date));
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		isPlaying = false;
		seekBar.setProgress(0);
		mPlay.setImageResource(android.R.drawable.ic_media_play);
		currentTime.setText("");
		
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		Log.e(tag, "mediaplayer ready");
		isReady = true;
		seekBar.setEnabled(true);
		seekBar.setMax(mPlayer.getDuration());
		date.setTime(mPlayer.getDuration());
		endTime.setText(fmt.format(date));

	}

	private Runnable run = new Runnable() {

		@Override
		public void run() {
			if (isPlaying && mPlayer.isPlaying()) {
				seekBar.setProgress(mPlayer.getCurrentPosition());
				mHandler.postDelayed(this, 100);
			}
		}

	};
}