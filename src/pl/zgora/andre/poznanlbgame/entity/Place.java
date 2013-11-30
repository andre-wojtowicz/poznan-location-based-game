package pl.zgora.andre.poznanlbgame.entity;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/** Data specific for GPS-based task */
public class Place extends Data implements Parcelable
{
	/** GPS coordinates */
	private Location location;
	/** Minimal distance of the user to desired position */
	private double minDistance;
	/** Not used */
	private double thetaSight;
	
	public Place(int id, double _gps_latitude, double _gps_longitude, double _min_distance,
			double _theta_sight)
	{
		super(id);
		
		location = new Location("network");
		location.setLatitude(_gps_latitude);
		location.setLongitude(_gps_longitude);
		
		minDistance = _min_distance;
		thetaSight = _theta_sight;
	}
	
	/** Creates new instance with id == -1 and rest of the fields equal to null */
	public Place()
	{
		super(-1);
	}
	
	/** @return GPS coordiantes */
	public Location getLocation()
	{
		return location;
	}
	
	/** @return minimal distance of the user to desired position */
	public double getMinDistance()
	{
		return minDistance;
	}
	
	/** Not used */
	public double getThetaSight()
	{
		return thetaSight;
	}
	
	// --------------- PERCELABLE IMPLEMENTATION -----------------

	/** Possible methods to create Place from Parcel */
	public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>()
	{
		
		public Place createFromParcel(Parcel src)
		{
			return new Place(src);
		}
	
		public Place[] newArray(int size)
		{
			return new Place[size];
		}
	};
	
	private Place(Parcel src)
	{
		super(src.readInt());
		readFromParcel(src);
	}
	
	@Override
	public void writeToParcel(Parcel dst, int flags)
	{
		dst.writeDouble(location.getLatitude());
		dst.writeDouble(location.getLongitude());
		dst.writeDouble(minDistance);
		dst.writeDouble(thetaSight);
	}
	
	public void readFromParcel(Parcel src)
	{
		location = new Location("network");
		location.setLatitude(src.readDouble());
		location.setLongitude(src.readDouble());
		minDistance = src.readDouble();
		thetaSight = src.readDouble();
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
}
