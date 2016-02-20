package pl.zgora.andre.poznanlbgame;

import java.text.Normalizer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pl.zgora.andre.poznanlbgame.entity.Answer;

/** Activity for text-based tasks */
public class TaskAnswerActivity extends TaskActivity
{
	/** Debug tag */
	private final String TAG = TaskAnswerActivity.class.getSimpleName();
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        
        setContentView(R.layout.task_answer);
    }
	
	@Override
    public void onResume()
    {
    	super.onResume();
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
	
	/** Compares user input (sign by sign, no special chars, lowercase) with desired answer */
	public void btOKOnClick(View view)
	{
		EditText etAnswer = (EditText) findViewById(R.id.etAnswer);
		
		String userAnswer = Normalizer.normalize(etAnswer.getText().toString(), Normalizer.Form.NFD)
		           .replaceAll("[^\\p{ASCII}]", "").toLowerCase();
		String taskAnswer = Normalizer.normalize(((Answer)task.getData()).getText(), Normalizer.Form.NFD)
		           .replaceAll("[^\\p{ASCII}]", "") 
				.toLowerCase();
		
		if (userAnswer.equals(taskAnswer))
			prepareToNextTask();
		else
			showWrongAnswerCrouton();
	}
}
