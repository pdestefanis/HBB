package ps.age.hbb;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;

import ps.age.hbb.core.RecordItem;
import ps.age.util.DBWraper;
import ps.age.util.WebClient;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends Activity {
	public static final String TAG = ListActivity.class.getSimpleName();
	
    /** Called when the activity is first created. */
	ArrayList<RecordItem> mList;
	private DateFormat fmt = DateFormat.getDateTimeInstance();
	
	private ProgressDialog mProgressDialog;
	private ListView listView;
	private ImageView syncView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        WebClient.setURL(getResources().getString(R.string.url));
        
        listView = (ListView) findViewById(R.id.items_list);
        syncView = (ImageView)findViewById(R.id.sync);
        
        syncView.setOnClickListener(listener);
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.progress_loadItems);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				Intent intent = new Intent(ListActivity.this,HBBActivity.class);
				intent.putExtra(ExtraActivity.ITEM, mList.get(position));
				startActivity(intent);
				finish();
			}
        	
        });
        
        LoadListTask task = new LoadListTask();
        task.execute();
    }
    private OnClickListener listener = new OnClickListener(){
    	@Override
    	public void onClick(View view){
            mProgressDialog.setTitle(R.string.progress_uploadItems);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
            UploadTask task = new UploadTask();
            task.execute();
            
    	}
    };
    
    private class LoadListTask extends AsyncTask<Void, Void, ArrayList<RecordItem>> {
        @Override
        protected ArrayList<RecordItem> doInBackground(Void ... args) {
        	DBWraper wraper = new DBWraper(ListActivity.this);
	        ArrayList<RecordItem> list = wraper.getRecordsList();
	        wraper.close();
        	
          return list;
        }

        @Override
        protected void onPostExecute(ArrayList<RecordItem> result) {
        	mProgressDialog.dismiss();
        	mList = result;
        	if(result != null){
        		listView.setAdapter(new MenuAdapter(ListActivity.this, R.layout.list_item,result ));
        	}
        	else{
        		Toast.makeText(ListActivity.this, R.string.error_noData, Toast.LENGTH_LONG).show();
        	}
        }
      }
    
    
	private class MenuAdapter extends ArrayAdapter<RecordItem>{

	    Context context; 
	    int layoutResourceId;    
	    RecordItem data[] = null;
	    Date date = new Date();
	    public MenuAdapter(Context context, int layoutResourceId, ArrayList<RecordItem> data) {
	        super(context, layoutResourceId, data);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data.toArray(new RecordItem[data.size()]);
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        RecordItem item = data[position];
	        if(row == null)
	        {
	        	Log.e("tag", String.valueOf(item.getFirstMark())+":"+String.valueOf(item.getSecondMark())+":"+String.valueOf(item.getThirdMark())+":"+
	        			String.valueOf(item.getFourthMark()));
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	           LinearLayout view =  ((LinearLayout)row);
	           date.setTime(item.getTime());
	           TextView title = (TextView) view.findViewById(R.id.item_title);
	           title.setText(fmt.format(date));
	           TextView marks = (TextView) view.findViewById(R.id.item_marks);
	           int totalMarks = item.getTotalMarks();
	           // no mark set
	           if(totalMarks == 0){
	        	   view.setBackgroundResource(R.drawable.background_nomarks);
	        	   marks.setText("");
	           }
	           else{
	        	   view.setBackgroundResource(R.drawable.background_marks);
	        	   marks.setText(String.valueOf(totalMarks)+marks.getText());
	           }
	        }
	        
	        return row;
	    }
	    

	}
	protected void showDialog(String title,String msg){
		  new AlertDialog.Builder(ListActivity.this)
		  .setTitle(title)
		  .setMessage(msg)
		  .setPositiveButton(android.R.string.ok, null)
		  .show();
	}
	protected void showError(String msg){
		if(msg != null)
			showDialog(getResources().getString(R.string.error),msg);
	}
	
	public final class UploadTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			WebClient.setContext(getApplicationContext());
			return WebClient.synchronizeRecords("usrName", "key", mList);
			
		}
		@Override
		public void onPostExecute(Boolean success){
			Log.e(TAG, "onPostExcute");
			mProgressDialog.dismiss();
			if(success){
					
					Toast.makeText(ListActivity.this, "Success", Toast.LENGTH_LONG).show();
					
	
				
			}
			else{
				showError(WebClient.getErrorMessage());	
			}
		}
	}
}