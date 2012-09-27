package ps.age.hbb;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class HelpActivity extends Activity {
	ImageView mBack;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        mBack = (ImageView) findViewById(R.id.back);
        mBack.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();				
			}
        	
        });
    }
}
