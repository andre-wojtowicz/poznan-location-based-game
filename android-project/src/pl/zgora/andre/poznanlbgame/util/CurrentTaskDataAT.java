package pl.zgora.andre.poznanlbgame.util;

import pl.zgora.andre.poznanlbgame.TaskActivity;
import pl.zgora.andre.poznanlbgame.entity.Task;
import android.os.AsyncTask;

/** Gets desired Task with full data. The process is running in the UI-thread. */
public class CurrentTaskDataAT extends AsyncTask<Integer, Integer, Task>
{
	///** Debug tag */
	//private static final String TAG = CurrentTaskDataAT.class.getSimpleName();

	/** Activity where should be set task. It will be done directly because
	 * some data can be too large to pass in Intent. */
	private TaskActivity activity;
	/** Data extractor. */
	private DataBaseHelper myDbHelper;
	
	/** Creates new instance of CurrentTaskDataAT.
	 * @param _activity activity which starts this AsyncTask */
	public CurrentTaskDataAT(TaskActivity _activity)
	{
		activity = _activity;
	}
	
	/** Extracts the Task with data from the database.
	 * @param params one-element list with id number of desired Task */
	@Override
	protected Task doInBackground(Integer... params)
	{
		myDbHelper = new DataBaseHelper(activity.getApplicationContext());
		myDbHelper.open();
		
		int taskId = params[0];
		Task task = myDbHelper.getTask(taskId, true);
		
		myDbHelper.close();
		
		return task;
	}

	/** Sets extracted Task and hides progress dialog. 
	 * @param result extracted Task*/
	protected void onPostExecute(Task result)
	{
		activity.setTask(result);
		activity.hideProgressDialog();
	}
	
	/** Exposed to possible interrupt of data extraction
	 * @return if running then current instance of Task extractor; otherwise null */
	public DataBaseHelper getDBHelper()
	{
		return myDbHelper;
	}
}
