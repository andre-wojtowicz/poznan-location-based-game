package pl.zgora.andre.poznanlbgame;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import pl.zgora.andre.poznanlbgame.entity.Task;
import pl.zgora.andre.poznanlbgame.util.CurrentTaskDataAT;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/** Root activity to be displayed when player solves tasks */
public abstract class TaskActivity extends Activity
{
	/** Debug tag */
	private static final String TAG = TaskActivity.class.getSimpleName();
	
	/** Task to be solved */
	protected Task task;
	/** AsyncTask which gets specific (mostly large) portion of task Task data */
	protected CurrentTaskDataAT dataCollector;
	/** Indicator that something is in progress */
	protected ProgressDialog progressDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        
        Bundle extras = getIntent().getExtras();
        
        if (extras != null)
        {
        	task = extras.getParcelable(Task.class.getSimpleName());
        	task.setData(null);
        }
        else
        	Log.e(TAG, "No task in extras.");
        
        dataCollector = new CurrentTaskDataAT(this);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Log.i(TAG, "onResume()");
    	
    	TextView tvTaskNumber = (TextView) findViewById(R.id.tvTaskNumber);
        TextView tvTaskDescription = (TextView) findViewById(R.id.tvTaskDescription);
      
        tvTaskNumber.setText(String.valueOf(task.getId()));
        tvTaskDescription.setText(task.getTextBefore());
        
        Button btBack = (Button) findViewById(R.id.btBack);
        int goatId;
        double chc = Math.random();
        if (chc < 0.333)
        	goatId = R.drawable.goat_1;
        else if (chc < 0.666)
        	goatId = R.drawable.goat_2;
        else
        	goatId = R.drawable.goat_3;
        
        btBack.setCompoundDrawablesWithIntrinsicBounds(0, 0, goatId, 0);
    	
    	if (task.getData() == null)
    	{   
    		if (dataCollector.getStatus() == Status.PENDING)
    		{
    	        Integer[] params = {task.getId()};
    	        dataCollector.execute(params);
    		}
        	showProgressDialog();
    	}
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	
    	Log.d(TAG, "dataCollector status = " + dataCollector.getStatus());
    	if (dataCollector.getStatus() == Status.RUNNING)
    	{
    		Log.d(TAG, "dataCollector cancel...");
    		dataCollector.cancel(true);
    		dataCollector.getDBHelper().interrupt();
    	}
    	
    	hideProgressDialog();
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }
    
    public Task getTask()
    {
    	return task;
    }
    
    /** Updates lasts solved task and displays summary of the current task */
    protected void prepareToNextTask()
    {
    	SharedPreferences sp = getApplicationContext().getSharedPreferences(StartActivity.GAME_SHARED_PREFERENCE_NAME, ListActivity.MODE_PRIVATE);
    	Editor ed = sp.edit();
    	ed.putInt(StartActivity.GAME_KEY_LAST_TASK_ID, task.getId()+1);
    	ed.commit();
    	
    	Intent intent = new Intent();
    	intent.setClass(this, TaskFinished.class);
    	intent.putExtra(Task.class.getSimpleName(), task);
    	startActivity(intent);
    	finish();
    }
    
    protected void showWrongAnswerCrouton()
    {
    	Crouton.makeText(this, R.string.wrong_answer, Style.ALERT).show();
    }
    
    public void showProgressDialog()
    {
    	hideProgressDialog();
    	
    	progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.show();
    }
    
    public void hideProgressDialog()
    {
    	if (progressDialog != null)
    		progressDialog.dismiss();
    }
    
    public void setProgressDialogStyle(int style)
    {
    	progressDialog.setProgressStyle(style);
    }
    
    /** Sets retrieved Task from databse. */
    public void setTask(Task _task)
    {
    	task = _task;
    }
    
    /** Prepares custom Toast with information if user input answer is correct or not
     * @param isCorrect true if answer was ok
     * @return custom Toast: image and text, long duration */
    protected Toast getAnswerToast(boolean isCorrect)
    {
    	LayoutInflater inflater = getLayoutInflater();
    	 
		View layout = inflater.inflate(R.layout.toast_user_answer, (ViewGroup) findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(isCorrect ? R.drawable.answer_ok : R.drawable.answer_wrong);

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(isCorrect ? R.string.good_answer : R.string.wrong_answer);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		
		return toast;
    }
    
    public void backClick(View view)
    {
    	finish();
    }
    
    // ------------------- DEBUG ------------------
    
    public void skipTask(View view)
    {
    	prepareToNextTask();
    }
}
