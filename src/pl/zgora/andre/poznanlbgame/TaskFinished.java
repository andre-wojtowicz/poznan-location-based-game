package pl.zgora.andre.poznanlbgame;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import pl.zgora.andre.poznanlbgame.entity.Task;
import pl.zgora.andre.poznanlbgame.util.NextTaskAT;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/** Activity for summary of the solved task */
public class TaskFinished extends Activity
{
	/** Debug tag */
	private static final String TAG = TaskFinished.class.getSimpleName();
	/** Solved task */
	protected Task task;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        
        Bundle extras = getIntent().getExtras();
        
        if (extras != null)
        {
        	task = extras.getParcelable(Task.class.getSimpleName());
        }
        else
        	Log.e(TAG, "No task in extras.");
        
        setContentView(R.layout.task_finished);
        
        showGoodAnswerCrouton();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Log.i(TAG, "onResume()");
    	
    	TextView tvTaskNumber = (TextView) findViewById(R.id.tvTaskNumber);
        TextView tvTaskDescription = (TextView) findViewById(R.id.tvTaskDescription);
      
        tvTaskNumber.setText(String.valueOf(task.getId()));
        tvTaskDescription.setText(task.getTextAfter());
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }
    
    /** Tries to load next task */
    public void btNextOnClick(View view)
    {
    	Integer[] params = {task.getId()+1};
    	NextTaskAT ntat = new NextTaskAT(getApplicationContext(), this);
    	ntat.execute(params);
    }
    
    private void showGoodAnswerCrouton()
    {
    	Crouton.makeText(this, R.string.good_answer, Style.CONFIRM).show();
    }
    
    public void backClick(View view)
    {
    	finish();
    }
}
