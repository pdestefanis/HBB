package ps.age.hbb.core;

import java.io.File;
import java.util.List;

import android.content.Context;

public class DataManager {
	private DBWraper dbWrapper;
	public DataManager(Context context){
		dbWrapper = new DBWraper(context);
	}
	public void addItem(final RecordItem item){
		new Thread(){
			@Override
			public void run(){
				item.matchNameToPath();
				dbWrapper.insertRecord(item);
			}
		}.start();
	}
	
	public void updateItem(final RecordItem item){
		new Thread(){
			@Override
			public void run(){
				item.matchNameToPath();
				dbWrapper.updateRecord(item);

			}
		}.start();
	}

	public List<RecordItem> getItems(){
		return dbWrapper.getRecordsList();
	}
	public void deleteItem(RecordItem item){
		
		dbWrapper.deleteRecord(item);
		new File(item.getPath()).delete();
	}
}
