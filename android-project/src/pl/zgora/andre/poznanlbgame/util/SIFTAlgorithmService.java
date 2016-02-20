package pl.zgora.andre.poznanlbgame.util;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.calib3d.Calib3d;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import pl.zgora.andre.poznanlbgame.entity.View;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

/** Service to process OpenCV operations in order to say whether picture taken by user
 * is correct (concretely, if the photo matches patterns in the database). */
public class SIFTAlgorithmService extends Service
{
	/** Debug tag */
	private static final String TAG = SIFTAlgorithmService.class.getSimpleName();
	// intent keys and actions
	public  static final String A_SIFT_ENDED = "pl.zgora.andre.poznanlbgame.sift_ended";
	public  static final String A_SIFT_PROGRESS = "pl.zgora.andre.poznanlbgame.sift_progress";
	public  static final String A_SIFT_PROGRESS_MAX = "pl.zgora.andre.poznanlbgame.sift_progress_max";
	public  static final String K_SIFT_RESULT = "sift_result";
	public  static final String K_SIFT_PROGRESS = "sift_progress";
	public  static final String K_SIFT_PROGRESS_MAX = "sift_progress_max";
	
	private static final String MANUFACTURER_SAMSUNG = "samsung";
	private static final String MANUFACTURER_HTC = "htc";
    private static final String MODEL_ID_GALAXY_S = "gt-i9000";
    private static final String MODEL_ID_ONE_V = "htc one v";
	
	/** Matched keypoints must be stored in special OpenCV float matrix. Each element
	 * (match) there is a float array. */
	private static final int	DMATCH_NO_ELEMENTS = 4;
	/** Position in float array for match query index */
	private static final int	DMATCH_QUERY_IDX = 0;
	/** Position in float array for match train index */
	private static final int	DMATCH_TRAIN_IDX = 1;
	/** Position in float array for match distance */
	private static final int	DMATCH_DISTANCE = 3;
	/** Extracted keypoints must be stored in special OpenCV float matrix. Each element
	 * (keypoint) there is a float array. */
	private static final int    KEYPOINT_NO_ELEMENTS = 7;
	/** Position in float array for point x-coordinate */
	private static final int 	KEYPOINT_PT_X = 0;
	/** Position in float array for point y-coordinate */
	private static final int 	KEYPOINT_PT_Y = 1;
	/** Converted keypoints must be stored in special OpenCV float matrix. Each element
	 * (point) there is a float array. */
	private static final int 	POINT2F_NO_ELEMENTS = 2;
	/** Position in float array for point x-coordinate */
	private static final int 	POINT2F_X = 0;
	/** Position in float array for point y-coordinate */
	private static final int 	POINT2F_Y = 1;
	
	/** x-size of the photo taken by the user */
	private static final int IMG_WIDTH  = 800;
	/** y-size of the photo taken by the user */
	private static final int IMG_HEIGHT = 480;
	
	/** ratioTest parameter */ 
	private static final float KNN_DISTANCE_RATIO = 0.65f;
	
	/** RANSAC parameter - see OpenCV doc for findFundamentalMat() */
	private static final double RANSAC_DISTANCE   = 3.0;
	/** RANSAC parameter - see OpenCV doc for findFundamentalMat() */
	private static final double RANSAC_CONFIDENCE = 0.99;
	
	/** Indicates current step in the algorithm */
	private static int progress = 0;
	     
	// parameters for logistic-regression classifier
	private static final double MU_X1 = 21.607;
	private static final double MU_X2 = 36.930;
	private static final double SIGMA_X1 = 50.843;
	private static final double SIGMA_X2 = 71.727;
	private static final double THETA_0 =  8.442978;
	private static final double THETA_1 =  9.270537;
	private static final double THETA_2 =  22.137282;
	private static final double SIGMOID_THRESHOLD = 0.50;
	
	/** Stores jpeg data taken by TakePhotoActivity */
	private static byte[] photoDataByte;
	/** Stores path to photo taken by MediaStore */
	private static String photoDataPath;
	/** Stores SIFT patterns */
	private static ArrayList<View> views;
	/** Indicates whether algorithm is running */
	private static boolean isRunning = false;
	
	/** Not used */
	public SIFTAlgorithmService()
	{
	}
	
	/** Launches new Thread with the main SIFT-based image comparing algorithm. */
	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		isRunning = true;
		
		new Thread()
		{
			public void run()
			{
				Log.d(TAG, "Executed");
				boolean sift_result = false;
				
				sendPrepareProgress(3 + views.size());
				
				Log.d(TAG, "Loading image");
				
				Mat tempImage, photoImage = new Mat();
				
				if (photoDataByte != null)
				{
					Mat photoImageRaw = new Mat(1, photoDataByte.length, CvType.CV_8U);
					photoImageRaw.put(0, 0, photoDataByte);
					tempImage = Highgui.imdecode(photoImageRaw, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
				}
				else 
				{
					tempImage = Highgui.imread(photoDataPath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
				}
				
				Log.d(TAG, "MAT: " + tempImage.cols() + " " + tempImage.rows());
				
				Imgproc.resize(tempImage, photoImage, new Size(IMG_WIDTH, IMG_HEIGHT));
				
				sendProgress();
				
				Log.d(TAG, "MAT: " + photoImage.cols() + " " + photoImage.rows());
				Log.d(TAG, "Detection of keypoints");
				
				Log.d(TAG, "FeatureDetector creation");
				FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
				Log.d(TAG, "MatOfKeyPoint creation");
				MatOfKeyPoint photoKeypoints = new MatOfKeyPoint();
				
				Log.d(TAG, "detector.detect()");
				detector.detect(photoImage, photoKeypoints);
				
				sendProgress();
				
				Log.i(TAG, "Keypoints: " + photoKeypoints.rows());
				Log.d(TAG, "Extraction of descriptors.");
				
				DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
				Mat photoDescriptors = new Mat();
				
				extractor.compute(photoImage, photoKeypoints, photoDescriptors);
				
				sendProgress();
				
				Log.i(TAG, "Descriptors: " + photoDescriptors.rows());
				
				DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
				
				int results[] = new int[views.size()];
				
				for (View view: views)
				{					
					Log.d(TAG, "View " + (views.indexOf(view)+1) + "/" + views.size());
					
					MatOfKeyPoint dbKeypoints = view.getKeypoints();
					Mat dbDescriptors = view.getDescriptors();
					
					List<MatOfDMatch> matches1 = new ArrayList<MatOfDMatch>(),
							    	  matches2 = new ArrayList<MatOfDMatch>();
					
					try
					{
						matcher.knnMatch(photoDescriptors, dbDescriptors, matches1, 2);
						matcher.knnMatch(dbDescriptors, photoDescriptors, matches2, 2);
					}
					catch (CvException e)
					{
						Log.w(TAG, "kNN error - skipping cuurent view...");
						continue;
					}
					
					Log.d(TAG, "Matches (1): " + matches1.size() + ", " + matches2.size());
					
					int r1 = ratioTest(matches1);
					int r2 = ratioTest(matches2);
					
					Log.d(TAG, "Removed: " + r1 + ", " + r2 + " after ratio test");
					
					Log.d(TAG, "Matches (2): " + matches1.size() + ", " + matches2.size());
					
					MatOfPoint2f symmetryPoints1 = new MatOfPoint2f(),
								 symmetryPoints2 = new MatOfPoint2f();
					
					List<DMatch> symmetryMatches = symmetryTest(matches1, matches2, photoKeypoints, dbKeypoints, symmetryPoints1, symmetryPoints2);
					
					Log.d(TAG, "Matches (3): " + symmetryMatches.size() + " after symmetry test");
					
					Mat statusMask = new Mat();
					
					try
					{
						/*Mat F = */Calib3d.findFundamentalMat(symmetryPoints1, symmetryPoints2, 
											   Calib3d.FM_RANSAC,
											   RANSAC_DISTANCE, 
											   RANSAC_CONFIDENCE,
											   statusMask);
					}
					catch (CvException e)
					{
						Log.w(TAG, "Can't find fundamental matrix");
						continue;
					}
					
					int inliers = 0;
					for (int i=0; i<statusMask.rows(); i++)
						if (statusMask.get(i, 0)[0] == 1)
							inliers++;
				
					sendProgress();
					
					Log.i(TAG, "Inliers: " + inliers + ", statusMask.rows(): " + statusMask.rows());
					
					results[views.indexOf(view)] = inliers;
				}
				
				Arrays.sort(results);
				sift_result = predict(results[results.length-2], results[results.length-1]);
				
				// clean-up files
				
				File file = new File(photoDataPath);
				file.delete();
				
				
				// - check if should delete from camera directory
//				String dirName = "",
//					   manufacturer = android.os.Build.MANUFACTURER.toLowerCase(),
//		               model = android.os.Build.MODEL.toLowerCase();
//				
//				if (manufacturer.equals(MANUFACTURER_SAMSUNG)) {
//					 if (model.contains(MODEL_ID_GALAXY_S)) {
//						 dirName = "Camera"; 
//					 }
//				}
//				else if (manufacturer.equals(MANUFACTURER_HTC)) {
//					 if (model.contains(MODEL_ID_ONE_V)) {
//						 dirName = "100MEDIA"; 
//					 }
//				}
//				
//				if (!dirName.isEmpty())
//				{
//					boolean dcimClean = deleteLastFromDCIM(dirName);
//					Log.d(TAG, "deleteLastFromDCIM() = " + dcimClean);
//				}
				
				// send result
				
				Intent intent = new Intent(A_SIFT_ENDED);
				intent.putExtra(K_SIFT_RESULT, sift_result);
				sendBroadcast(intent);
				
				isRunning = false;
				progress = 0;
				stopSelf();
			}
		}.start();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	/** Performs ratio test (operation in-place).
	 * @param matches matches between keypoints in two images;
	 * @return number of removed matches */
	private static int ratioTest(List<MatOfDMatch> matches)
	{
		List<MatOfDMatch> toRemove = new ArrayList<MatOfDMatch>();
		
		for (MatOfDMatch match: matches)
		{
			if (match.rows() == 2)
			{
				float[] neigh1 = new float[DMATCH_NO_ELEMENTS],
						neigh2 = new float[DMATCH_NO_ELEMENTS];
				match.get(0, 0, neigh1);
				match.get(1, 0, neigh2);
				
				float distance1 = neigh1[DMATCH_DISTANCE],
					  distance2 = neigh2[DMATCH_DISTANCE];
				
				if (distance1 > KNN_DISTANCE_RATIO * distance2)
				{
					toRemove.add(match);
				}
			}
			else // should never happened!
			{
				Log.w(TAG, "ratio test, match.rows() != 2");
				toRemove.add(match);
			}
		}
		
		for (MatOfDMatch match: toRemove)
			matches.remove(match);
		
		return toRemove.size();
	}
	
	/** Performs symmetry test in keypoints matches.
	 * @param matches1 img1 -> img2 matches
	 * @param matches2 img1 <- img1 matches
	 * @param keypoints1 img1 keypoints
	 * @param keypoints2 img2 keypoints
	 * @param symmetryPoints1 returning img1 points
	 * @param symmetryPoints2 returning img2 points */
	private static List<DMatch> symmetryTest(List<MatOfDMatch> matches1, List<MatOfDMatch> matches2,
									 MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2,
									 MatOfPoint2f symmetryPoints1, MatOfPoint2f symmetryPoints2)
	{
		List<DMatch> symmetryMatches = new ArrayList<DMatch>();
		
		for (MatOfDMatch match1: matches1)
		{
			for (MatOfDMatch match2: matches2)
			{
				float[] neigh1 = new float[DMATCH_NO_ELEMENTS],
						neigh2 = new float[DMATCH_NO_ELEMENTS];
				match1.get(0, 0, neigh1);
				match2.get(0, 0, neigh2);
				
				if (neigh1[DMATCH_QUERY_IDX] == neigh2[DMATCH_TRAIN_IDX] && 
					neigh2[DMATCH_QUERY_IDX] == neigh1[DMATCH_TRAIN_IDX])
				{
					DMatch dmatch = new DMatch((int)neigh1[DMATCH_QUERY_IDX], (int)neigh1[DMATCH_TRAIN_IDX], 
							neigh1[DMATCH_DISTANCE]);
					
					symmetryMatches.add(dmatch);
					break;
				}
			}
		}
		
		symmetryPoints1.alloc(symmetryMatches.size());
		symmetryPoints2.alloc(symmetryMatches.size());
		
		for (DMatch dmatch: symmetryMatches)
		{
			int i = symmetryMatches.indexOf(dmatch);
			float[] kp = new float[KEYPOINT_NO_ELEMENTS];
			
			keypoints1.get(dmatch.queryIdx, 0, kp);
			float[] p1 = new float[POINT2F_NO_ELEMENTS];
			p1[POINT2F_X] = kp[KEYPOINT_PT_X];
			p1[POINT2F_Y] = kp[KEYPOINT_PT_Y];
			symmetryPoints1.put(i, 0, p1);
			
			keypoints2.get(dmatch.trainIdx, 0, kp);
			float[] p2 = new float[POINT2F_NO_ELEMENTS];
			p2[POINT2F_X] = kp[KEYPOINT_PT_X];
			p2[POINT2F_Y] = kp[KEYPOINT_PT_Y];
			symmetryPoints2.put(i, 0, p2);
		}
		
		return symmetryMatches;
	}
	
	/** Logistic regression classifier. Says finally if user has taken picture of
	 * desired object.
	 * @param x1_raw non-scaled first feature
	 * @param x2_raw non-scaled second feature
	 * @return true if task is done */
	private static boolean predict(int x1_raw, int x2_raw)
	{
		boolean p = false;
		
		double x1 = (x1_raw - MU_X1)/SIGMA_X1,
			   x2 = (x2_raw - MU_X2)/SIGMA_X2;
		
		double z = THETA_0 + x1*THETA_1 + x2*THETA_2;
		
		double g = 1.0 / (1.0 + Math.exp(-z)); // sigmoid
		Log.d(TAG, "sigmoid: " + g);
		
		if (g > SIGMOID_THRESHOLD)
			p = true;
		
		return p;
	}
	
	/** Sets jpeg data when photo was taken by TakePhotoActivity
	 * @param _photoData jpeg data */
	public static void setPhotoData(byte[] _photoData)
	{
		photoDataByte = _photoData;
	}
	
	/** Sets path to MediaStore photo
	 * @param _path path where photo will be saved */
	public static void setPhotoData(String _path)
	{
		photoDataPath = _path;
	}
	
	/** Sets list of patterns to be compared with photo taken by the user
	 * @param _views SIFT data from Task */
	public static void setViews(ArrayList<View> _views)
	{
		views = _views;
	}
	
	/** @return true when algorithm is running */
	public static boolean isRunning()
	{
		return isRunning;
	}
	
	/** Removes no-more-used photo file saved by MediaStrore.
	 * reference: http://stackoverflow.com/a/9532341
	 * @return true if file was deleted
	 */
	private static boolean deleteLastFromDCIM(String dirName) {

        boolean success = false;
        try {
            File[] images = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "DCIM"  + File.separator + dirName).listFiles();
            File latestSavedImage = images[0];
            for (int i = 1; i < images.length; ++i) {
                if (images[i].lastModified() > latestSavedImage.lastModified()) {
                    latestSavedImage = images[i];
                }
            }

            success = latestSavedImage.delete(); 
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return success;
        }

    }
	
	/** Broadcasts max steps in progress dialog
	 * @param maxProgress number of maximum steps in the algorithm */
	private void sendPrepareProgress(int maxProgress)
	{
		Intent intent = new Intent(A_SIFT_PROGRESS_MAX);
		intent.putExtra(K_SIFT_PROGRESS_MAX, maxProgress);
		
		sendBroadcast(intent);
	}
	
	/** Broadcasts next step for progress dialog */
	private void sendProgress()
	{
		Intent intent = new Intent(A_SIFT_PROGRESS);
		intent.putExtra(K_SIFT_PROGRESS, ++progress);
		
		sendBroadcast(intent);
	}

	/** Not used */
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
}
