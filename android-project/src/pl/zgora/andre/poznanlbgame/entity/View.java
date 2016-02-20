package pl.zgora.andre.poznanlbgame.entity;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/** One photo in photo-based tasks. Concretely, contains SIFT features and keypoints. */
public class View implements Parcelable
{
	private static final String TAG = View.class.getSimpleName();
	
	/** Extracted keypoints must be stored in special OpenCV float matrix. Each element
	 * (keypoint) there is an float array. */
	private static final int KEYPOINT_NO_ELEMENTS = 7;
	/** Position in float array for point x-coordinate */
	private static final int KEYPOINT_PT_X = 0;
	/** Position in float array for point y-coordinate */
	private static final int KEYPOINT_PT_Y = 1;
	/** Position in float array for point size */
	private static final int KEYPOINT_SIZE = 2;
	/** Length of SIFT keypoint descriptor vector */
	private static final int SIFT_DESCRIPTOR_SIZE = 128;
	
	/** Number of the task (in database) */
	private int id;
	/** Name of the corresponding photo file */
	private String name;
	/** SIFT keypoints */
	private MatOfKeyPoint keypoints;
	/** SIFT description of keypoints */
	private Mat descriptors;
	
	/** @param _id number of the task (in database)
	 * @param _name name of the correspondig photo file
	 * @param _keypoints SIFT keypoints
	 * @param _descriptors description of keypoints */
	public View(int _id, String _name, MatOfKeyPoint _keypoints, Mat _descriptors)
	{
		id = _id;
		name = _name;
		keypoints = _keypoints;
		descriptors = _descriptors;
	}
	
	/** @return number of the task */
	public int getId()
	{
		return id;
	}
	
	/** @return name of the corresponding photo file */
	public String getName()
	{
		return name;
	}
	
	/** @return SIFT keypoints */
	public MatOfKeyPoint getKeypoints()
	{
		return keypoints;
	}
	
	/** @return SIFT keypoints description */
	public Mat getDescriptors()
	{
		return descriptors;
	}
	
	// --------------- PERCELABLE IMPLEMENTATION -----------------

	/** Possible methods to create View from Parcel */
	public static final Parcelable.Creator<View> CREATOR = new Parcelable.Creator<View>()
	{
		
		public View createFromParcel(Parcel src)
		{
			return new View(src);
		}
	
		public View[] newArray(int size)
		{
			return new View[size];
		}
	};
	
	private View(Parcel src)
	{
		readFromParcel(src);
	}
	
	@Override
	public void writeToParcel(Parcel dst, int flags)
	{
		dst.writeInt(id);
		dst.writeString(name);
		
		dst.writeInt(keypoints.rows());
		
		for (int i=0; i<keypoints.rows(); i++)
		{
			float[] kp = new float[KEYPOINT_NO_ELEMENTS];
			keypoints.get(i, 0, kp);
			
			dst.writeFloat(kp[KEYPOINT_PT_X]);
			dst.writeFloat(kp[KEYPOINT_PT_Y]);
			dst.writeFloat(kp[KEYPOINT_SIZE]);
		}
		
		dst.writeInt(descriptors.rows());
		
		for (int i=0; i<descriptors.rows(); i++)
			for (int j=0; j<SIFT_DESCRIPTOR_SIZE; j++)
				dst.writeInt((int)descriptors.get(i, j)[0]);
		
	}
	
	public void readFromParcel(Parcel src)
	{
		id = src.readInt();
		name = src.readString();
		
		keypoints = new MatOfKeyPoint();
		int noKeypoints = src.readInt();
		keypoints.alloc(noKeypoints);
		
		for (int i=0; i<noKeypoints; i++)
		{
			float[] kp = new float[KEYPOINT_NO_ELEMENTS];
			
			kp[KEYPOINT_PT_X] = src.readFloat();
			kp[KEYPOINT_PT_Y] = src.readFloat();
			kp[KEYPOINT_SIZE] = src.readFloat();
			keypoints.put(i, 0, kp);
		}
		
		int noDescriptors = src.readInt();
		
		descriptors = new Mat(noDescriptors, SIFT_DESCRIPTOR_SIZE, CvType.CV_32F);
		for (int i=0; i<noDescriptors; i++)
			for (int j=0; j<SIFT_DESCRIPTOR_SIZE; j++)
				descriptors.put(i, j, src.readInt());
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
}
