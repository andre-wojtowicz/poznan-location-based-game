package pl.zgora.andre.poznanlbgame.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import pl.zgora.andre.poznanlbgame.entity.Answer;
import pl.zgora.andre.poznanlbgame.entity.ArchitecturalElement;
import pl.zgora.andre.poznanlbgame.entity.Data;
import pl.zgora.andre.poznanlbgame.entity.Place;
import pl.zgora.andre.poznanlbgame.entity.Task;
import pl.zgora.andre.poznanlbgame.entity.View;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** Wraps the SQLite database content. Concretely, extracts and creates Tasks instances
 * with data from entity package. 
 * reference: http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
 */
public class DataBaseHelper extends SQLiteOpenHelper
{
	/** Debug tag */
	private static final String TAG = DataBaseHelper.class.getSimpleName();
	/** Extracted keypoints must be stored in special OpenCV float matrix. Each element
	 * (keypoint) there is a float array. */
	private static final int KEYPOINT_NO_ELEMENTS = 7;
	/** Position in float array for point x-coordinate */
	private static final int KEYPOINT_PT_X = 0;
	/** Position in float array for point y-coordinate */
	private static final int KEYPOINT_PT_Y = 1;
	/** Position in float array for point size */
	private static final int KEYPOINT_SIZE = 2;
	/** Length of SIFT keypoint descriptor vector */
	private static final int SIFT_DESCRIPTOR_SIZE = 128;
	
	/** Path where database will be stored */
    private static String DB_PATH = "/data/data/pl.zgora.andre.poznanlbgame/databases/";
    /** Name of the database file */
    private static String DB_NAME = "database.db";
    
    /** SQL column name for photo-based tasks */
    private static String ARCH_ELEM  = "architectural element";
    /** SQL column name for GPS-based tasks */
    private static String PLACE      = "place";
    /** SQL column name for text-based tasks */
    private static String ANSWER     = "answer";
 
    /** Holds data extractor */
    private SQLiteDatabase myDataBase;
    /** Contexts with assets where database file is stored */
    private final Context myContext;
    
    /** Indicates wheter current query process should be stopped */
    private boolean cancelQuery;
 
    /*
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context)
    {
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
        
        cancelQuery = false;
        
        Log.i(TAG, "DataBaseHelper()");
    }
    
    /** For debug purposes */
    protected void finalize()
    {
    	Log.i(TAG, "finalize()");
    }
 
    /*
     * Creates a empty database on the system and rewrites it with your own database.
     */
    private void createDataBase() throws IOException
    {
    	Log.i(TAG, "createDataBase()");
    	boolean dbExist = checkDataBase();
 
    	if (dbExist)
    	{
    		// do nothing - database already exist
    	}
    	else
    	{
    		// By calling this method and empty database will be created into the default system path
            // of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try
        	{
    			copyDataBase();
    		}
        	catch (IOException e)
        	{
        		throw new Error("Error copying database");
        	}
    	}
    }

    /*
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase()
    {
    	Log.i(TAG, "checkDataBase()");
    	SQLiteDatabase checkDB = null;
 
    	try
    	{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	}
    	catch (SQLiteException e)
    	{
    		//database does't exist yet.
    	}
 
    	if (checkDB != null)
    	{
    		checkDB.close();
    	}
 
    	return checkDB != null ? true : false;
    }
 
    /*
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException
    {
    	Log.i(TAG, "copyDataBase()");
    	// Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	
    	while ((length = myInput.read(buffer)) > 0)
    	{
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
 
    /** Creates SQLiteDatabase instance from database file (read-only mode) */
    private void openDataBase() throws SQLException
    {
    	Log.i(TAG, "openDataBase()");
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
    
    /** Tries to create database file and opens it */
    public void open()
    {
    	Log.i(TAG, "open()");
    	
    	try
        {
        	createDataBase();
	 	} 
        catch (IOException ioe)
        {
	 		throw new Error("Unable to create database");
	 	}
	 
	 	try
	 	{
	 		openDataBase();
	 	} 
	 	catch (SQLException sqle)
	 	{
	 		throw sqle;
	 	}
    }
 
    /** Shutdowns current connection to databse */
    @Override
	public synchronized void close()
    {
    	Log.i(TAG, "close()");
    	
    	if (myDataBase != null)
    	{
    		Log.i(TAG, "myDataBase.close()");
    		myDataBase.close();
    	}
 
    	super.close();
	}
 
    /** Not used */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
 
	}
 
	/** Not used */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
 
	}
 
	/** @return number of tasks in game*/
	public int getNumberOfTasks()
	{
		Cursor c = myDataBase.rawQuery("SELECT COUNT(*) FROM tasks", null);
		c.moveToNext();
		
		int ret = c.getInt(0);
		
		c.close();
				
		return ret;
	}
	
	/** @return list of task ids */
	public int[] getListOfTaskIds()
	{
		Cursor c = myDataBase.rawQuery("SELECT _id FROM tasks", null);
		int[] ret = new int[c.getCount()];
		
		while(c.moveToNext())
			ret[c.getPosition()] = c.getInt(0);
		
		c.close();
		
		return ret;
	}
	
	/** Extracts Task instance from database.
	 * @param id number of task
	 * @param withData if true then specific data (keypoints, GPS-data etc.) will be also extracted
	 * @return extracted Task; if null then desired task doesn't exist */
	public Task getTask(int id, boolean withData)
	{
		Cursor c = myDataBase.rawQuery(
				"SELECT tasks._id AS 'id', types.name AS 'type', tasks.text_before, tasks.text_after, " +
				"tasks.img_before, tasks.img_after " +
				"FROM tasks " +
				"JOIN types ON tasks.type_id = types._id " +
				"WHERE tasks._id = " + String.valueOf(id),
				null);
		
		if (c.getCount() == 0)
		{
			Log.e(TAG, "Couldn't get task with id = " + String.valueOf(id));
			c.close();
			return null;
		}
		
		c.moveToNext();
		
		Data taskData = null;
		
		String t = c.getString(c.getColumnIndex("type"));
		
		if (t.equals(ARCH_ELEM))
			taskData = (withData ? getArchElementData(id) : new ArchitecturalElement());
		else if (t.equals(PLACE))
			taskData = (withData ? getPlaceData(id) : new Place());
		else if (t.equals(ANSWER))
			taskData = (withData ? getAnswerData(id): new Answer());
		else 
		{
			Log.e(TAG, "Unknown type of task with id = " + String.valueOf(id));
			c.close();
			return null;
		}
		
		
		Task ret = new Task(id, taskData,
				c.getString(c.getColumnIndex("text_before")), c.getString(c.getColumnIndex("text_after")), 
				c.getString(c.getColumnIndex("img_before")), c.getString(c.getColumnIndex("img_after")));
		
		c.close();
		
		return ret;
	}
 
	/** Creates data specific for photo-based task
	 * @param taskId number of desired task 
	 * @return null if error occurs (see logs)*/
	private ArchitecturalElement getArchElementData(int taskId)
	{
		Cursor cv = myDataBase.rawQuery(
				"SELECT _id AS id, name " +
				"FROM views " +
				"WHERE task_id = " + String.valueOf(taskId),
				null);
		
		if (cv.getCount() == 0)
		{
			Log.e(TAG, "Couldn't get views with task_id = " + String.valueOf(taskId));
			return null;
		}
		
		ArrayList<View> views = new ArrayList<View>();
		
		while (cv.moveToNext())
		{
			int viewId = cv.getInt(cv.getColumnIndex("id"));
			
			Cursor cf = myDataBase.rawQuery(
					"SELECT _id AS id, pt_x, pt_y, desc " +
					"FROM features " +
					"WHERE view_id = " + String.valueOf(viewId),
					null);
			
			if (cf.getCount() == 0)
			{
				Log.e(TAG, "Couldn't get features with view_id = " + String.valueOf(viewId));
				return null;
			}
			
			MatOfKeyPoint keypoints = new MatOfKeyPoint();
			keypoints.alloc(cf.getCount());
			Mat descriptors = new Mat(cf.getCount(), SIFT_DESCRIPTOR_SIZE, CvType.CV_32F);
			
			while (cf.moveToNext())
			{
				if (cancelQuery)
				{
					Log.d(TAG, "ArchitecturalElement data query interrupted");
					cf.close();
					cv.close();
					cancelQuery = false;
					return null;
				}
				
				float[] kp = new float[KEYPOINT_NO_ELEMENTS];
				kp[KEYPOINT_PT_X] = cf.getFloat(cf.getColumnIndex("pt_x"));
				kp[KEYPOINT_PT_Y] = cf.getFloat(cf.getColumnIndex("pt_y"));
				kp[KEYPOINT_SIZE] =  1.0f; 
				
				keypoints.put(cf.getPosition(), 0, kp);
				
				String desc = cf.getString(cf.getColumnIndex("desc"));
				String[] vals = desc.split(",");
				
				if (vals.length != SIFT_DESCRIPTOR_SIZE)
				{
					Log.e(TAG, "Wrong descriptor lenght in feature with _id = " + cf.getString(cf.getColumnIndex("id")));
					return null;
				}
				
				for (int j=0; j < vals.length; j++)
					descriptors.put(cf.getPosition(), j, Float.valueOf(vals[j]));

			}
			
			cf.close();
			
			views.add(new View(viewId, cv.getString(cv.getColumnIndex("name")), keypoints, descriptors));
		}
		
		ArchitecturalElement ret = new ArchitecturalElement(taskId, views);
		
		cv.close();
		
		return ret;
	}
	
	/** Creates data specific to GPS-based task
	 * @param taskId number of desired task
	 * @return null if error occured (see logs) */
	private Place getPlaceData(int taskId)
	{
		Cursor c = myDataBase.rawQuery(
				"SELECT _id AS id, gps_latitude, gps_longitude, min_distance, theta_sight " +
				"FROM places " +
				"WHERE task_id = " + String.valueOf(taskId),
				null);
		
		if (c.getCount() != 1)
		{
			Log.e(TAG, "Couldn't get place data with task_id = " + String.valueOf(taskId));
			return null;
		}
		
		c.moveToNext();
		
		Place ret = new Place(c.getInt(c.getColumnIndex("id")), c.getDouble(c.getColumnIndex("gps_latitude")), 
				c.getDouble(c.getColumnIndex("gps_longitude")), c.getDouble(c.getColumnIndex("min_distance")),
				c.getDouble(c.getColumnIndex("theta_sight")));
		
		c.close();
		
		return ret;
	}
	
	/** Creates data specific to text-based task.
	 * @param taskId number of desired task
	 * @return null if error occurs (see logs) */
	private Answer getAnswerData(int taskId)
	{
		Cursor c = myDataBase.rawQuery(
				"SELECT _id AS id, text " +
				"FROM answers " +
				"WHERE task_id = " + String.valueOf(taskId),
				null);
		
		if (c.getCount() != 1)
		{
			Log.e(TAG, "Couldn't get answer data with task_id = " + String.valueOf(taskId));
			return null;
		}
		
		c.moveToNext();
		
		Answer ret = new Answer(c.getInt(c.getColumnIndex("id")), c.getString(c.getColumnIndex("text")));
		
		c.close();
		
		return ret;
	}
	
	/** Sets a flag indicating that current query should be stopped as soon as possible */
	public void interrupt()
	{
		cancelQuery = true;
	}
}
