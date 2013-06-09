package ps.age.hbb;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ps.age.hbb.core.DataManager;
import ps.age.hbb.core.RecordItem;
import ps.age.hbb.core.RecordItem.State;
import ps.age.hbb.core.SharedObjects;
import ps.age.hbb.net.WebClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
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
	// Constants for sharedPreferences userName ...
	public static final String PREF = "pref";
	public static final String USER_NAME = "userName";

	public static final String TAG = ListActivity.class.getSimpleName();
	public static final int DELETE_DIALOG = 321;
	/** Called when the activity is first created. */
	List<RecordItem> mList;
	private DateFormat fmt = DateFormat.getDateTimeInstance();

	private ProgressDialog mProgressDialog;
	private ListView listView;
	private ImageView syncView;
	private Handler mHandler;
	private MenuAdapter mAdapter;
	private ImageView mBack;
	private String userName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		WebClient.setURL(getResources().getString(R.string.url));
		Log.e(TAG, "onCreate");
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == DELETE_DIALOG && msg.arg1 == RESULT_OK) {
					deleteRecord(msg.arg2);
				}
			}
		};
		listView = (ListView) findViewById(R.id.items_list);
		syncView = (ImageView) findViewById(R.id.sync);
		mBack = (ImageView) findViewById(R.id.back);

		registerForContextMenu(listView);
		syncView.setOnClickListener(listener);
		mBack.setOnClickListener(listener);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(R.string.progress_loadItems);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				Log.e(TAG, "selected item: "+String.valueOf(position));
				RecordItem item = mList.get(position);
				if(item.getExtra() !=null)
					Log.e(TAG, "item: extra"+item.getExtra());

				Intent intent = new Intent(ListActivity.this, HBBActivity.class);
				intent.putExtra(ExtraActivity.ITEM, item );
				startActivity(intent);
				finish();
			}

		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				/*
				 * Checks if the item has been uploaded to the server and
				 * creates a menu if so by propagating the long click event
				 */
				Log.e(TAG, "Long click "+String.valueOf(position)+
							"time "+String.valueOf(mList.get(position).getUploadTime()));	
				switch(mList.get(position).getState()){
					case EDITED:
					case UPLOADED:
						return false;
					default:
						return true;
				}
			}

		});
		LoadListTask task = new LoadListTask();
		task.execute();
		// Code to get userName
		final SharedPreferences pref = getSharedPreferences(PREF,
				Context.MODE_PRIVATE);
		userName = pref.getString(USER_NAME, null);
		if (userName == null) {
			// generate userName
			Random rand = new Random();

			userName = String.format("%s:%s_%d ",
					android.os.Build.MANUFACTURER, android.os.Build.MODEL,
					rand.nextLong());
			pref.edit().putString(USER_NAME, userName).commit();
		}
		Log.e(TAG, "userName: "+userName);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_menu, menu);
		Log.e(TAG, "creating context Menu");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete:
			createDeleteDialog((int) info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
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

	protected void showDialog(String title, String msg) {
		new AlertDialog.Builder(ListActivity.this).setTitle(title)
				.setMessage(msg).setPositiveButton(android.R.string.ok, null)
				.show();
	}

	protected void showError(String msg) {
		if (msg != null)
			showDialog(getResources().getString(R.string.error), msg);
	}

	/*
	 * Create a delete dialog related to RecordItem at position in mList
	 */
	public void createDeleteDialog(int position) {
		Log.e(TAG, "create Delete dialog");
		HBBDialog dialog = new HBBDialog(ListActivity.this);
		dialog.setCancelable(false);
		Message msg = mHandler.obtainMessage();
		msg.what = DELETE_DIALOG;
		msg.arg2 = position;
		dialog.setDismissMessage(msg);
		dialog.setTitle(getResources().getString(R.string.deleteDialog_title));
		dialog.setExtra(getResources().getString(R.string.deleteDialog_extra));
		dialog.setButtonsText(
				getResources().getString(R.string.deleteDialog_positive),
				getResources().getString(R.string.deleteDialog_negative));
		dialog.show();
	}

	/*
	 * Delete a record at a specified position in the record list
	 */
	private void deleteRecord(int position) {
		Log.e(TAG, "deleting record "+String.valueOf(position));
		RecordItem item = mList.get(position);
		mList.remove(item);
		mAdapter.setData(mList);
		mAdapter.notifyDataSetChanged();
		listView.forceLayout();
		SharedObjects.getDataManager(getApplicationContext()).deleteItem(item);
		// No more records , finish the activity
		if (mList.size() == 0)
			finish();
	}

	private class MenuAdapter extends ArrayAdapter<RecordItem> {

		Context context;
		int layoutResourceId;
		RecordItem data[] = null;
		Date date = new Date();

		public MenuAdapter(Context context, int layoutResourceId,
				List<RecordItem> data) {
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data.toArray(new RecordItem[data.size()]);
		}
		public void setData(List<RecordItem> data){
			this.data = data.toArray(new RecordItem[data.size()]);
			for (RecordItem item : data) {
				Log.d(TAG, "item: " + item.getExtraString(RecordItem.EXTRA_ID));
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout row = (LinearLayout) convertView;
			RecordItem item = data[position];
			Log.e("tag", "ListItem: " + position + ": " +item.getPath() + " : " + item.getState().name() + " serverKey: " + item.getServerKey());
			if (row == null) {

				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = (LinearLayout) inflater.inflate(layoutResourceId, parent,
						false);
			}
			LinearLayout view = ((LinearLayout) row);
			date.setTime(item.getTime());
			TextView title = (TextView) view.findViewById(R.id.item_title);
			title.setText(fmt.format(date));
			TextView marks = (TextView) view.findViewById(R.id.item_marks);

			String extra_id = item.getExtraString(RecordItem.EXTRA_ID);
			TextView extra_ident = (TextView) view
					.findViewById(R.id.item_id);
			if (extra_id != null) {
				extra_ident.setText(extra_id);
			} else {
				extra_ident.setText("");
			}
			int totalMarks = item.getTotalMarks();
			switch(item.getState()){
			case NEW:
				view.setBackgroundResource(R.drawable.background_new);
				marks.setText("");
				break;
			case UPLOADED:
				view.setBackgroundResource(R.drawable.background_uploaded);
				
	//			if (totalMarks != 0) {
					marks.setTextColor(Color.BLACK);
					marks.setText(String.valueOf(totalMarks) + (String)marks.getTag());
/*				} else {
					marks.setText("error");
				}
*/				break;
			case REVIEWED:
				view.setBackgroundResource(R.drawable.background_reviewed);
				marks.setText(String.valueOf(totalMarks) + (String)marks.getTag());
				break;
			case EDITED:
				view.setBackgroundResource(R.drawable.background_edited);
//				if (totalMarks != 0) {
					marks.setTextColor(Color.BLACK);
					marks.setText(String.valueOf(totalMarks) + (String)marks.getTag());
/*				} else {
					marks.setText("error");
				}
*/				break;
			}
		
			return row;
		}

	}

	public final class UploadTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			WebClient.setContext(getApplicationContext());
			ArrayList<RecordItem> list = new ArrayList<RecordItem>();
			
			for (RecordItem item : mList) {
				switch(item.getState()){
				case REVIEWED:
				case EDITED:
					list.add(item);
				default:
				}
			}
			if(list.size() == 0) {
				return true;
			}
			if (WebClient.synchronizeRecords(userName, "key", list)) {
				DataManager manager = SharedObjects.getDataManager(getApplication());
				String response = WebClient.getResponse();
				JSONArray array = null;
				try {
					array = new JSONArray(response);
				}catch(JSONException ex) {
					Log.e(TAG, "failed to parse generated jsonarray " + ex.toString());
				}
				
				for (RecordItem item : list) {
					if (array != null) {
						try {
							for (int i = 0; i < array.length() ; i++) {
								JSONObject object = array.getJSONObject(i);
								long id = object.getLong(WebClient.LOCAL_KEY);
								if (id == item.getId()) {
									item.setServerKey(object.getString(WebClient.SERVER_KEY));
								}
							}
						} catch (JSONException excep) {
							Log.e(TAG, "jsonex " + excep.toString());
						}
					}
					item.setUploadTime(System.currentTimeMillis());
					item.setState(State.UPLOADED);
					
					manager.updateItem(item);
				}
				return true;
			}
			return false;
		}

		@Override
		public void onPostExecute(Boolean success) {
			Log.e(TAG, "onPostExcute " + String.valueOf(success));
			mProgressDialog.dismiss();
			if (success) {

				mAdapter = new MenuAdapter(ListActivity.this,
						R.layout.list_item, mList);
				Toast.makeText(ListActivity.this, "Success", Toast.LENGTH_LONG)
						.show();
				listView.setAdapter(mAdapter);

			} else {
				showError(WebClient.getErrorMessage());
			}
		}
	}

	private class LoadListTask extends
			AsyncTask<Void, Void, List<RecordItem>> {
		@Override
		protected List<RecordItem> doInBackground(Void... args) {
			DataManager manager = SharedObjects.getDataManager(getApplicationContext());
			List<RecordItem> list = manager.getItems();

			return list;
		}

		@Override
		protected void onPostExecute(List<RecordItem> result) {
			mProgressDialog.dismiss();
			mList = result;
			if (result != null && (!isFinishing())) {
				mAdapter = new MenuAdapter(ListActivity.this,
						R.layout.list_item, result);
				listView.setAdapter(mAdapter);

			} else {
				Toast.makeText(ListActivity.this, R.string.error_noData,
						Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

}