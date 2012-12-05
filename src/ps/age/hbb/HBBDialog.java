package ps.age.hbb;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class HBBDialog extends Dialog {

	TextView mTitle;
	TextView mExtra;
	Button mPositive;
	Button mNegative;
	Message mMsg;

	public HBBDialog(Context context) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hpp_dialog);
		mTitle = (TextView) findViewById(R.id.dialog_title);
		mExtra = (TextView) findViewById(R.id.dialog_extra);
		mPositive = (Button) findViewById(R.id.button_positive);
		mNegative = (Button) findViewById(R.id.button_negative);

		mPositive.setOnClickListener(listener);
		mNegative.setOnClickListener(listener);
	}

	public void setTitle(String text) {
		mTitle.setText(text);
	}

	public void setExtra(String text) {
		mExtra.setText(text);
	}

	public void setButtonsText(String positive, String negative) {
		mPositive.setText(positive);
		mNegative.setText(negative);
	}

	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_positive:
				mMsg.arg1 = Activity.RESULT_OK;
				break;
			case R.id.button_negative:
				mMsg.arg1 = Activity.RESULT_CANCELED;
				break;
			}
			setDismissMessage(mMsg);
			dismiss();
		}

	};

	@Override
	public void setDismissMessage(Message msg) {
		super.setDismissMessage(msg);
		mMsg = msg;
	}
}
