package pl.zgora.andre.poznanlbgame;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/** Activity displayed when user solved all tasks */
public class EndActivity extends Activity
{
	/** Debug tag */
	private static final String TAG = EndActivity.class.getSimpleName();
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        
        setContentView(R.layout.end);
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
    
    /** Finishes activity */
    public void btOKOnClick(View view)
    {
    	finish();
    }
}
