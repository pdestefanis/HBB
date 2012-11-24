package ps.age.hbb;


import ps.age.hbb.core.RecordItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ExtraActivity extends Activity {
	protected static final String tag = ExtraActivity.class.getSimpleName();
	public static final String ITEM   = "item";
	public static final int REQUEST_CODE = 620976104;
	
	ImageView		mBack;
	Button			mSave;
	EditText		mProblem;
	EditText		mOther;
	EditText		mID;
	RadioGroup 		cryGroup;
	RadioGroup 		ventilationGroup;
	Spinner			mPrimary;
	RecordItem		mItem;
	ToggleButton	mAlive;
	
	String selectedPrimary;
	 ArrayAdapter<CharSequence> mAdapter;
	/*
	 * JSON String headers
	 */
	protected static final String ID 			= "id";
	protected static final String VALUE			= "value";
	protected static final int ID_IDENT			= 1;
	protected static final int ID_PRIMARY 		= 2;
	protected static final int ID_CRY     		= 3;
	protected static final int ID_VENTILATION	= 4;
	protected static final int ID_OTHER 		= 5;
	protected static final int ID_PROBLEM 		= 6;	
	protected static final int ID_ALIVE         = 7;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra);
        try{
        Log.e(tag, "onCreate");    	

        mSave 			 = (Button) findViewById(R.id.button_save);
        cryGroup 		 = (RadioGroup) findViewById(R.id.radioCry);
        ventilationGroup = (RadioGroup) findViewById(R.id.radioVentilation);
        mPrimary 		 = (Spinner) findViewById(R.id.primary_spinner);
        mProblem	     = (EditText) findViewById(R.id.problem_editText);
        mOther		     = (EditText) findViewById(R.id.other_editText);
        mID				 = (EditText) findViewById(R.id.id_editText);
        mBack			 = (ImageView) findViewById(R.id.back);
        mAlive			 = (ToggleButton) findViewById(R.id.alive_toggle);
        mAdapter = ArrayAdapter.createFromResource(this,
               R.array.primary_array , android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPrimary.setAdapter(mAdapter);
        mPrimary.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			  public void onItemSelected(AdapterView<?> parent, View view, 
			            int pos, long id) {
				selectedPrimary = ((TextView) view).getText().toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				selectedPrimary = null;
			}
        	
        });
        mBack.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View view){
        		finish();
        	}
        });
        mAlive.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
    				mPrimary.setEnabled(false);
    				mOther.setEnabled(false);
				}else{
    				mPrimary.setEnabled(true);
    				mOther.setEnabled(true);
				}
			}
        	
        });
        mItem = (RecordItem) getIntent().getSerializableExtra(ITEM);
        mSave.setOnClickListener(listener);
        if(mItem.getExtra()!=null){
				loadForm();
        }
        final View activityRootView = findViewById(R.id.extra_root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                Log.e(tag, "onGlobalLayoutCHange, diff: "+String.valueOf(heightDiff));
                if (heightDiff > 100) { 
                	// if more than 100 pixels, its probably a keyboard...
                	mSave.setVisibility(View.GONE);

                }
                else {
                	mSave.setVisibility(View.VISIBLE);

                }
            }
        });
        }catch(Exception e){
        	Log.e(tag, e.toString());
        }
    }
    private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(mItem != null){
				saveForm();
				Intent intent = getIntent();
				intent.putExtra(ITEM, mItem);
				setResult(RESULT_OK, intent);
			}
			finish();
		}
    	
    };
    private void loadForm() {
		String[] keys = mItem.getExtraKeys();
		if(keys == null)
			return;
    	for(String key: keys){

    		String value = mItem.getExtraString(key);
    		if(key.equals(RecordItem.EXTRA_ID)){
    			mID.setText(value);
    			continue;
    		}
    		if(key.equals(RecordItem.EXTRA_CRY)){
    			((RadioButton)cryGroup.findViewWithTag(value)).setChecked(true);
    			continue;
    		}
    		if(key.equals(RecordItem.EXTRA_VENTILATION)){
    			((RadioButton)ventilationGroup.findViewWithTag(value)).setChecked(true);
    			continue;
    		}
    		if(key.equals(RecordItem.EXTRA_OTHER)){
				mOther.setText(value);
				continue;
    		}
    		if(key.equals(RecordItem.EXTRA_PROBLEM)){
    			mProblem.setText(value);
    			continue;
    		}
    		if(key.equals(RecordItem.EXTRA_PRIMARY)){
    			mPrimary.setSelection(mAdapter.getPosition(value), false);
    			continue;
    		}
    		if(key.equals(RecordItem.EXTRA_ALIVE)){
    			if(value.equals(getResources().getString(R.string.alive))){
    				mAlive.setChecked(true);
    				mPrimary.setEnabled(false);
    				mOther.setEnabled(false);
    			}
    			else{  				
        			if(value.equals(getResources().getString(R.string.dead))){
        				mAlive.setChecked(false);
        			}	
        		}
    			continue;
    		}   		

    	}
    
/*    	for(int i=0;i<mForm.length();i++){
    		JSONObject obj = mForm.get(i);
    		String value = obj.getString(VALUE);
    		int id  = obj.getInt(ID);
    		
    		if (value == null)
    			continue;
    		
    		switch(id){
    		case ID_IDENT: 
    			mID.setText(value);
    			break;
    		case ID_CRY:
    			((RadioButton)cryGroup.findViewWithTag(value)).setChecked(true);
    			break;
    		case ID_VENTILATION:
    			((RadioButton)ventilationGroup.findViewWithTag(value)).setChecked(true);
    			break;  			
    		case ID_OTHER:
				mOther.setText(value);
				break;
    		case ID_PROBLEM:
    			mProblem.setText(value);
    			break;
    		case ID_PRIMARY:
    			mPrimary.setSelection(mAdapter.getPosition(value), false);
    			break;
    		case ID_ALIVE:
    			
    			if(value.equals(getResources().getString(R.string.alive))){
    				mAlive.setChecked(true);
    				mPrimary.setEnabled(false);
    				mOther.setEnabled(false);
    			}
    			else{
        			if(value.equals(getResources().getString(R.string.dead))){
        				mAlive.setChecked(false);
        				
        			}
    			}
    		}
    		
    	}
*/
   }
    @Override
    protected void onResume(){
    	super.onResume();
        Log.e(tag, "onResume");    	
    	
    }
    @Override
    protected void onPause(){
    	super.onPause();
        Log.e(tag, "onPause");    	

    }

    @Override
    protected void onStop(){
    	super.onStop();
        Log.e(tag, "onStop");    	
    }
    @Override
    public void onDestroy(){
    	super.onDestroy();
        Log.e(tag, "onDestroy");    	
    	
    }

    private void saveForm(){
    	
        if(mID.getText().toString().length() != 0 ){
        	mItem.putExtraString(RecordItem.EXTRA_ID,	mID.getText().toString());
        }
        RadioButton btn = (RadioButton) findViewById(cryGroup.getCheckedRadioButtonId());
    	mItem.putExtraString(RecordItem.EXTRA_CRY,	btn.getText().toString());

        btn = (RadioButton) findViewById(ventilationGroup.getCheckedRadioButtonId());
    	mItem.putExtraString(RecordItem.EXTRA_VENTILATION,	btn.getText().toString());

    	if(mAlive.isChecked()){
        	mItem.putExtraString(RecordItem.EXTRA_ALIVE,	mAlive.getTextOn().toString());
    	}
    	else{
        	mItem.putExtraString(RecordItem.EXTRA_ALIVE,	mAlive.getTextOff().toString());

  		
    		/*
    		 * Check if the user selected a value in the primary spinner
    		 */
    		if(selectedPrimary != null){	
            	mItem.putExtraString(RecordItem.EXTRA_PRIMARY,	selectedPrimary);
    			
    		}
    		/*
    		 * Check if there is data in the editText field
    		 */
    		if(mOther.getText().toString().trim().length() > 0){
            	mItem.putExtraString(RecordItem.EXTRA_OTHER, mOther.getText().toString().trim());
    		}
    	}
        if(mProblem.getText().toString().trim().length() > 0){
        	mItem.putExtraString(RecordItem.EXTRA_PROBLEM,mProblem.getText().toString().trim());
        }		        
        
    }

}
