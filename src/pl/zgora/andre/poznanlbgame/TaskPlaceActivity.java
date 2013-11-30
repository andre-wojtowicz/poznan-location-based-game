package pl.zgora.andre.poznanlbgame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import pl.zgora.andre.poznanlbgame.entity.Place;

/** Activity for GPS-based task */
public class TaskPlaceActivity extends TaskActivity implements LocationListener
{
	/** Debug tag */
	private final String TAG = TaskPlaceActivity.class.getSimpleName();
	
	/** Time (in ms) indicating how long phone should vibrate when user solved task */
	private static final int VIBR_TIME = 500;
	
	LocationManager locationManager = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        setContentView(R.layout.task_place);
    }
	
	@Override
    public void onResume()
    {
    	super.onResume();
    	Log.i(TAG, "onResume()");
    	
    	boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	if (!enabled)
    	{
    		Log.i(TAG, "locationManager GPS_PROVIDER disabled");
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage(getString(R.string.gps_enable))
        	       .setCancelable(false)
        	       .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        	       			startActivity(intent);
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	alert.show();
    	}
    	else
    	{
    		Log.i(TAG, "locationManager GPS_PROVIDER enabled");
    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    	}
    }
	
	@Override
    public void onPause()
    {
    	super.onPause();
    	Log.i(TAG, "onPause()");
    	
    	try
    	{
    		locationManager.removeUpdates(this);
    	}
    	catch (IllegalArgumentException e)
    	{
    		// pass
    	}
    }
	
	@Override
    public void onDestroy()
    {
    	super.onDestroy();
    }
	
	/** For debug purposes */
	public void btTestOnClick(View view)
	{
		//setMockLocation(52.408758, 16.932970, 500);
		setMockLocation(52.408558, 16.933313, 500);
	}

	/** Checks whether user is near desired position. Proximity workaround. */
	@Override
	public void onLocationChanged(Location userLocation)
	{
		Log.i(TAG, "onLocationChanged()");
		
		Place place = (Place) task.getData();
		Location taskLocation = place.getLocation();
		double minDistance = place.getMinDistance();
		
		Toast.makeText(getApplicationContext(), "dist: " + userLocation.distanceTo(taskLocation) + ", minDist: "+ minDistance, Toast.LENGTH_LONG).show();
		Log.d(TAG, "userLocation.distanceTo(taskLocation): " + userLocation.distanceTo(taskLocation));
		Log.d(TAG, "minDistance: " + minDistance);
		if (userLocation.distanceTo(taskLocation) <= minDistance)
		{
			((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VIBR_TIME);
			//Toast.makeText(getApplicationContext(), getString(R.string.good_answer), Toast.LENGTH_LONG).show();
			prepareToNextTask();
		}
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		Log.v(TAG, "Provider disabled "+ provider);
	}

	@Override
	public void onProviderEnabled(String provider)
	{
		Log.v(TAG, "Provider enabled "+ provider);
	}

	/** For debug purposes */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
        int satellites = extras.getInt("satellites", -1);
        
        String statusInfo = String.format("Provider: %s, status: ds, satellites: %d", provider, status, satellites);
        Log.v(TAG, statusInfo);
	}
	
	/** For debug purposes */
	private void setMockLocation(double latitude, double longitude, float accuracy)
	{
	    locationManager.addTestProvider (LocationManager.GPS_PROVIDER,
	                        "requiresNetwork" == "",
	                        "requiresSatellite" == "",
	                        "requiresCell" == "",
	                        "hasMonetaryCost" == "",
	                        "supportsAltitude" == "",
	                        "supportsSpeed" == "",
	                        "supportsBearing" == "",
	                         android.location.Criteria.POWER_LOW,
	                         android.location.Criteria.ACCURACY_FINE);      

	    Location newLocation = new Location(LocationManager.GPS_PROVIDER);

	    newLocation.setLatitude(latitude);
	    newLocation.setLongitude(longitude);
	    newLocation.setAccuracy(accuracy);

	    locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

	    locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
	                             LocationProvider.AVAILABLE,
	                             null,System.currentTimeMillis());    
	  
	    locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);      

	}
}
