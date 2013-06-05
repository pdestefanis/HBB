package ps.age.hbb.core;

import android.content.Context;

public class SharedObjects {
	private static DataManager dataManager;
	public static DataManager getDataManager(Context context){
			if(dataManager == null) {
				dataManager = new DataManager(context);
			}
		
		return dataManager;
	}
}
