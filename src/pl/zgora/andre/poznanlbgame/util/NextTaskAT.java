package pl.zgora.andre.poznanlbgame.util;

import pl.zgora.andre.poznanlbgame.EndActivity;
import pl.zgora.andre.poznanlbgame.TaskAnswerActivity;
import pl.zgora.andre.poznanlbgame.TaskArchElemActivity;
import pl.zgora.andre.poznanlbgame.TaskPlaceActivity;
import pl.zgora.andre.poznanlbgame.entity.Task;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/** Extracts next-in-game task without specific data (or ends game) and creates new activity. */
public class NextTaskAT extends AsyncTask<Integer, Integer, Intent>
{
	/** Debug tag */
	private static final String TAG = NextTaskAT.class.getSimpleName();
	
	/** Activity environment */
	private Context context;
	/** Activity which tries to set a new task */
	private Activity executorToFinish;
	
	/** Creates new instance of NextTaskAT.
	 * @param _context activity environment
	 * @param _executorToFinish activity to finish (take off from stack) when new activity started */
	public NextTaskAT(Context _context, Activity _executorToFinish)
	{
		context = _context;
		executorToFinish = _executorToFinish;
	}
	
	/** Creates next activity intent.
	 * @param params one-element list of parameters with id of desired task
	 * @return intent with Task*Activity or EndActivity when game ends */
	@Override
	protected Intent doInBackground(Integer... params)
	{
		DataBaseHelper myDbHelper = new DataBaseHelper(context);
		myDbHelper.open();
		
		int taskId = params[0];
		Task task = myDbHelper.getTask(taskId, false);
		
		if (task == null) // just finished last task
		{
			Intent intent = new Intent();
			intent.setClass(context, EndActivity.class);
			myDbHelper.close();
			return intent;
		}
		
		Log.i(TAG, "New task id="+task.getId()+", type="+task.getType());
		
		Intent intent = new Intent(Task.A_NEW_TASK);
		intent.putExtra(Task.class.getSimpleName(), task);
		
		switch (task.getType())
		{
		case ARCHITECTURAL_ELEMENT:
			intent.setClass(context, TaskArchElemActivity.class);
			break;
		case PLACE:
			intent.setClass(context, TaskPlaceActivity.class);
			break;
		case ANSWER:
			intent.setClass(context, TaskAnswerActivity.class);
			break;
		}
		
		myDbHelper.close();
		
		return intent;
	}
	
	/** Starts new activity and finishes executor.
	 * @param result may indicate Task*Activity or EndActivity */
	protected void onPostExecute(Intent result)
	{		
		result.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(result);
		if (executorToFinish != null)
			executorToFinish.finish();
	}

}
