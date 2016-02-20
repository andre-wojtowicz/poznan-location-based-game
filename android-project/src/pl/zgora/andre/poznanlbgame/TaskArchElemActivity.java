package pl.zgora.andre.poznanlbgame;

import java.io.File;
import java.util.ArrayList;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

import pl.zgora.andre.poznanlbgame.entity.ArchitecturalElement;
import pl.zgora.andre.poznanlbgame.entity.Task;
import pl.zgora.andre.poznanlbgame.entity.View;
import pl.zgora.andre.poznanlbgame.util.SIFTAlgorithmService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

/** Activity for photo-based task */
public class TaskArchElemActivity extends TaskActivity
{
	/** Debug tag */
	private static final String TAG = TaskArchElemActivity.class.getSimpleName();
	private static final int	C_MSTORE_PHOTO = 2;
	private static final String K_VIEWS		   = "views";
	/** Path where MediaStore should save picture */
	private static final String PHOTO_PATH	   = Environment.getExternalStorageDirectory() + 
												 File.separator + "tmp_poznan_citygame.jpg";
	
	/** Maximum steps in comparing algorithm (for progress bar) */
	private int progressMax;
	/** Current step in comparing algorithm (for progress bar) */
	private int progressState;
	
	/** Receives result of the comparison of the user pictures with patterns */
	private SIFTReceiver siftReceiver;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        
        setContentView(R.layout.task_archelem);
	}
	
	@Override
    public void onResume()
    {
    	super.onResume();
    	Log.i(TAG, "onResume()");
    	
    	if (siftReceiver==null)
    		siftReceiver = new SIFTReceiver();
    	
    	IntentFilter infilUpdate = new IntentFilter(SIFTAlgorithmService.A_SIFT_ENDED),
    			     infilProgress = new IntentFilter(SIFTAlgorithmService.A_SIFT_PROGRESS),
    			     infilProgressMax = new IntentFilter(SIFTAlgorithmService.A_SIFT_PROGRESS_MAX);
    	registerReceiver(siftReceiver, infilUpdate);
    	registerReceiver(siftReceiver, infilProgress);
    	registerReceiver(siftReceiver, infilProgressMax);
    	
    	if (SIFTAlgorithmService.isRunning())
    	{
    		showProgressDialogHorizontal();
    		setProgressDialogMax(progressMax);
    		updateProgressDialog(progressState);
    	}
    }
	
	@Override
    public void onPause()
    {
    	super.onPause();
    	Log.i(TAG, "onPause()");
    	
    	unregisterReceiver(siftReceiver);
    }
	
	@Override
    public void onDestroy()
    {
    	super.onDestroy();
    	Log.i(TAG, "onDestroy()");
    }
	
	/** Starts MediaStore activity to take a photo */
	public void btTakePhotoOnClick(android.view.View view)
	{		
		File file = new File(PHOTO_PATH);
	    Uri outputFileUri = Uri.fromFile(file);
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		
		startActivityForResult(intent, C_MSTORE_PHOTO);
	}
	
	/** Handles MediaStore result (starts SIFT service) */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d(TAG, "requestCode="+requestCode);
		Log.d(TAG, "resultCode="+resultCode); 
		
		if (requestCode ==  C_MSTORE_PHOTO)
		{
			if (resultCode == RESULT_OK)
			{
				Log.i(TAG, "Photo taken");
				startSIFT();
			}
		}
		
	}
	
	/** Starts algorithm (service) to compate photo taken by user with patterns. Shows progress bar */
	private void startSIFT()
	{
		Log.d(TAG, "Starting SIFT");
		
		Intent service = new Intent(this, SIFTAlgorithmService.class);
		SIFTAlgorithmService.setViews(((ArchitecturalElement)task.getData()).getViews());
		SIFTAlgorithmService.setPhotoData(PHOTO_PATH);
		Log.d(TAG, "photo path: " + PHOTO_PATH);
		startService(service);
		
		showProgressDialogHorizontal();
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState)
	{
    	Log.i(TAG, "onSaveInstanceState");
    	super.onSaveInstanceState(outState);
    	
    	if (task.getData() != null)
    	{
    		ArchitecturalElement data = (ArchitecturalElement)task.getData();
    		Log.d(TAG, "Saving "+ data.getViews().size() + " views");
    		outState.putParcelableArrayList(K_VIEWS, data.getViews());
    	}
    	
    	outState.putInt("progress_max", getProgressDialogMax());
    	outState.putInt("progress_state", getProgressDialogValue());
    }
	
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	Log.i(TAG, "onRestoreInstanceState");
    	
    	ArrayList<View> views = savedInstanceState.getParcelableArrayList(K_VIEWS);
    	
    	if (views != null)
    	{
    		Log.d(TAG, "Restoring " + views.size() + " views");
    		
    		int id = task.getId();
        	task.setData(new ArchitecturalElement(id, views));
    	}
    	
    	progressMax = savedInstanceState.getInt("progress_max", 0);
    	progressState = savedInstanceState.getInt("progress_state", 0);
    }
	
	public void setTask(Task _task)
    {
    	task = _task;
    }
	
	/** Handles information broadcasted by SIFTAlgorithmService */
	private class SIFTReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals(SIFTAlgorithmService.A_SIFT_ENDED))
			{
				boolean result = intent.getExtras().getBoolean(SIFTAlgorithmService.K_SIFT_RESULT);
				
				hideProgressDialog();
				
				if (result == true)
					prepareToNextTask();
				else
					showWrongAnswerCrouton();
			}
			else if (intent.getAction().equals(SIFTAlgorithmService.A_SIFT_PROGRESS))
			{
				int value = intent.getExtras().getInt(SIFTAlgorithmService.K_SIFT_PROGRESS);
				updateProgressDialog(value);
			}
			else if (intent.getAction().equals(SIFTAlgorithmService.A_SIFT_PROGRESS_MAX))
			{
				int value = intent.getExtras().getInt(SIFTAlgorithmService.K_SIFT_PROGRESS_MAX);
				setProgressDialogMax(value);
			}
		}
	}
    
    public void updateProgressDialog(int value)
    {
    	if (progressDialog != null)
    		progressDialog.setProgress(value);
    }
    
    public void showProgressDialogHorizontal()
    {
    	hideProgressDialog();
    	
    	progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.photo_processing));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }
    
    public void setProgressDialogMax(int value)
    {
    	if (progressDialog != null)
            progressDialog.setMax(value);
    }
    
    public void setProgressDialogIncr(int value)
    {
    	if (progressDialog != null)
    		progressDialog.incrementProgressBy(value);
    }
    
    public int getProgressDialogMax()
    {
    	if (progressDialog != null)
    		return progressDialog.getMax();
    	return 0;
    }
    
    public int getProgressDialogValue()
    {
    	if (progressDialog != null)
    		return progressDialog.getProgress();
    	return 0;
    }
}
