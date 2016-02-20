package pl.zgora.andre.poznanlbgame;

import java.util.ArrayList;

import pl.zgora.andre.poznanlbgame.util.DataBaseHelper;
import pl.zgora.andre.poznanlbgame.util.NextTaskAT;
import pl.zgora.andre.poznanlbgame.util.StartArrayAdapter;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

/** Activity with the main menu */
public class StartActivity extends ListActivity
{
	/** Debug tag */
	private static final String TAG = StartActivity.class.getSimpleName();
	// Shared preferences keys
	public static final String  GAME_SHARED_PREFERENCE_NAME = "poznanlbgame";
	public static final String  GAME_KEY_LAST_TASK_ID = "last_task_id";
	public static final String  GAME_KEY_CAMERA_ID = "camera_id";
	private static final int    NO_GAME_PLAYED_BEFORE = -1;
    
	/** Number of last played task */
	private int	lastTaskId;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        
        checkBackfaceCameraId();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Log.i(TAG, "onResume()");
    	
        loadLastTaskId();
    	
    	ArrayList<String> strCommands = new ArrayList<String>();
    	strCommands.add(getString(R.string.new_game));
    	if (lastTaskId != NO_GAME_PLAYED_BEFORE)
    		strCommands.add(getString(R.string.resume_game));
		strCommands.add(getString(R.string.tutorial));
		strCommands.add(getString(R.string.about_app));
		strCommands.add(getString(R.string.exit));
        
        setListAdapter(new StartArrayAdapter(this, strCommands));
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	Log.i(TAG, "onPause()");
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	Log.i(TAG, "onDestroy()");
    }
    
    /** Starts game from task id == 1 */
    private void startNewGame()
    {
    	SharedPreferences sp = getApplicationContext().getSharedPreferences(GAME_SHARED_PREFERENCE_NAME, ListActivity.MODE_PRIVATE);
    	Editor ed = sp.edit();
    	ed.remove(GAME_KEY_LAST_TASK_ID);
    	ed.commit();
    	
    	Integer[] params = { 1 };
		new NextTaskAT(getApplicationContext(), null).execute(params);
    }
    
    /** Handles user action */
    @Override
	protected void onListItemClick(ListView l, android.view.View v, int position, long id)
    {
		String item = (String) getListAdapter().getItem(position);
		
		if (item.equals(getString(R.string.new_game)))
		{
			if (lastTaskId != NO_GAME_PLAYED_BEFORE)
			{
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage(getString(R.string.sure_to_start_new_game))
	        	       .setCancelable(false)
	        	       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
	        	           public void onClick(DialogInterface dialog, int id) {
	        	                startNewGame();
	        	           }
	        	       })
	        	       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
	        	           public void onClick(DialogInterface dialog, int id) {
	        	                dialog.cancel();
	        	           }
	        	       });
	        	AlertDialog alert = builder.create();
	        	alert.show();
			}
			else
				startNewGame();
		}
		else if (item.equals(getString(R.string.resume_game)))
		{
			Integer[] params = {lastTaskId == NO_GAME_PLAYED_BEFORE ? 1 : lastTaskId};
			new NextTaskAT(getApplicationContext(), null).execute(params);
		}
		else if (item.equals(getString(R.string.tutorial)))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage(getString(R.string.tutorial_text))
        	       .setCancelable(false)
        	       .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	alert.show();
		}
		else if (item.equals(getString(R.string.about_app)))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage(getString(R.string.info_about_app))
        	       .setCancelable(false)
        	       .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	alert.show();
		}
		else if (item.equals(getString(R.string.exit)))
		{
			finish();
		}
	}
    
    /** Gets from Shared Preferences number of last played task*/
    private void loadLastTaskId()
    {
    	DataBaseHelper myDbHelper = new DataBaseHelper(this);
		myDbHelper.open();
		int notasks = myDbHelper.getNumberOfTasks();
		myDbHelper.close();
    	
    	SharedPreferences sp = getApplicationContext().getSharedPreferences(GAME_SHARED_PREFERENCE_NAME, ListActivity.MODE_PRIVATE);
    	lastTaskId = sp.getInt(GAME_KEY_LAST_TASK_ID, NO_GAME_PLAYED_BEFORE);
    	
    	if (lastTaskId > notasks)
    	{
    		Editor ed = sp.edit();
        	ed.remove(StartActivity.GAME_KEY_LAST_TASK_ID);
        	ed.commit();
        	lastTaskId = NO_GAME_PLAYED_BEFORE;
    	}
    }
    
    /** Gets which id has backface camera */
    private void checkBackfaceCameraId()
	{
    	SharedPreferences sp = getApplicationContext().getSharedPreferences(GAME_SHARED_PREFERENCE_NAME, ListActivity.MODE_PRIVATE);
    	int cameraId = sp.getInt(GAME_KEY_CAMERA_ID, -1);
    	
    	if (cameraId < 0)
    	{
    		for (int i=0; i<Camera.getNumberOfCameras(); i++)
    		{
    			Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                	Editor ed = sp.edit();
                	ed.putInt(GAME_KEY_CAMERA_ID, i);
                	ed.commit();
                	return;
                }
    		}

    		Log.e(TAG, "No back-facing camera");
    		Toast.makeText(this, R.string.no_backface_camera, Toast.LENGTH_LONG).show();
    	}
	}
}