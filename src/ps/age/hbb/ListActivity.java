package ps.age.hbb;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import ps.age.hbb.core.RecordItem;
import ps.age.hbb.core.DBWraper;
import ps.age.hbb.net.WebClient;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class ListActivity extends Activity {
	public static final String TAG = ListActivity.class.getSimpleName();
	public static final int DELETE_DIALOG = 321;
    /** Called when the activity is first created. */
	ArrayList<RecordItem> mList;
	private DateFormat fmt = DateFormat.getDateTimeInstance();
	
	private ProgressDialog mProgressDialog;
	private ListView listView;
	private ImageView syncView;
	private Handler mHandler;
	private MenuAdapter mAdapter;
	private ImageView mBack;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        WebClient.setURL(getResources().getString(R.string.url));
        mHandler = new Handler(){
        	
        	@Override
        	public void handleMessage(Message msg){
        		if(msg.what == DELETE_DIALOG  && msg.arg1 == RESULT_OK){
        			deleteRecord(msg.arg2);
        		}
        	}
        };
        listView = (ListView) findViewById(R.id.items_list);
        syncView = (ImageView)findViewById(R.id.sync);
        mBack    = (ImageView)findViewById(R.id.back);
        
        registerForContextMenu(listView);
        syncView.setOnClickListener(listener);
        mBack.setOnClickListener(listener);
        
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
       listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				/*
				 * Checks if the item has been uploaded to the server 
				 * 	and creates a menu if so by propagating the 
				 * long click event
				 */
				
				if(mList.get(position).getUploadTime() == -1)
						return true;
				
				return false;
			}
        	
        });
        LoadListTask task = new LoadListTask();
        task.execute();
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                createDeleteDialog((int) info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private OnClickListener listener = new OnClickListener(){
    	@Override
    	public void onClick(View view){
    		switch(view.getId()){
    		case R.id.sync:
                mProgressDialog.setTitle(R.string.progress_uploadItems);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
                UploadTask task = new UploadTask();
                task.execute();   			
    			break;
    		case R.id.back:
    			finish();
    			break;
    		}
            
    	}
    };
    
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
	
    /*
     * Create a delete dialog related to RecordItem at position in mList
     * 
     */
    public void createDeleteDialog(int position){
		HBBDialog dialog = new HBBDialog(ListActivity.this);
		dialog.setCancelable(false);
		Message msg = mHandler.obtainMessage();
		msg.what = DELETE_DIALOG;
		msg.arg2 = position;
		dialog.setDismissMessage(msg);
		dialog.setTitle(getResources().getString(
				R.string.deleteDialog_title));
		dialog.setExtra(getResources().getString(
				R.string.deleteDialog_extra));
		dialog.setButtonsText(
				getResources().getString(
							R.string.deleteDialog_positive),
				getResources().getString(
								R.string.deleteDialog_negative));
	dialog.show();
    }
    /*
     * Delete a record at a specified position in the record list
     */
    private void deleteRecord(int position){
    	DBWraper wraper = new DBWraper(this);
    	RecordItem item = mList.get(position);
    	mList.remove(position);
    	wraper.deleteRecord(item);
    	new File(item.getPath()).delete();
    	mAdapter.notifyDataSetChanged();
    	// No more records , finish the activity
    	if(mList.size() == 0)
    		finish();
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
	        LinearLayout row = (LinearLayout) convertView;
	        RecordItem item = data[position];
	        if(row == null)
	        {

	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = (LinearLayout) inflater.inflate(layoutResourceId, parent, false);
	        }
	           LinearLayout view =  ((LinearLayout)row);
	           date.setTime(item.getTime());
	           TextView title = (TextView) view.findViewById(R.id.item_title);
	           title.setText(fmt.format(date));
	           TextView marks = (TextView) view.findViewById(R.id.item_marks);
	           String extra_id = item.getExtraString(RecordItem.EXTRA_ID);
	           if(extra_id != null){
	        	   TextView extra_ident = (TextView) view.findViewById(R.id.item_id);
	        	   extra_ident.setText(extra_id);
	           }
        	   int totalMarks = item.getTotalMarks();	           
	           if(item.getUploadTime() == -1){
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
	           else{
        		   view.setBackgroundResource(R.drawable.background_uploaded);
        		   if(totalMarks != 0){
            		   marks.setTextColor(Color.BLACK);
	        		   marks.setText(String.valueOf(totalMarks)+marks.getText());    			   
        		   }
        		   else
        			   marks.setText("");    		   
	           }
	        
	        return row;
	    }
	    

	}
	
	public final class UploadTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			WebClient.setContext(getApplicationContext());
			ArrayList<RecordItem> list = new ArrayList<RecordItem>();
			
			for(RecordItem item : mList){
				if(item.isReviewed())
					list.add(item);
			}
				
				
			if(WebClient.synchronizeRecords("usrName", "key", list)){
				DBWraper wraper = new DBWraper(ListActivity.this);
				for(RecordItem item : list){
					item.setUploadTime(System.currentTimeMillis());
					wraper.updateRecord(item);
				}
				wraper.close();
				return true;
			}
			return false;
		}
		@Override
		public void onPostExecute(Boolean success){
			Log.e(TAG, "onPostExcute "+String.valueOf(success));
			mProgressDialog.dismiss();
			if(success){
				
        			mAdapter =new MenuAdapter(ListActivity.this, R.layout.list_item,mList ); 
					Toast.makeText(ListActivity.this, "Success", Toast.LENGTH_LONG).show();
					listView.setAdapter(mAdapter);
				
			}
			else{
				showError(WebClient.getErrorMessage());	
			}
		}
	}
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
	        	if(result != null && (!isFinishing())){
	        		mAdapter =new MenuAdapter(ListActivity.this, R.layout.list_item,result ); 
	        		for(RecordItem item : mList){
	        			Log.e(TAG, String.valueOf(item.getId())+":"+String.valueOf(item.getUploadTime()));
	        		}
	        		listView.setAdapter(mAdapter);
	        		
	        	}
	        	else{
	        		Toast.makeText(ListActivity.this, R.string.error_noData, Toast.LENGTH_LONG).show();
	        		finish();
	        	}
	        }
	      }
	    

}