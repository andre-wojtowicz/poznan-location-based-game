package pl.zgora.andre.poznanlbgame.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/** Creates a structure for in-game task */
public class Task implements Parcelable
{
	/** Possible types of tasks */
	public enum Type
	{
		ARCHITECTURAL_ELEMENT,
		PLACE,
		ANSWER,
		UNKNOWN;
	}
	
	/** Debug tag */
	private static final String TAG = Task.class.getSimpleName();
	
	// Intent keys and actions
	public static final String K_ID = "id";
	public static final String K_GET_DATA = "get_data";
	public static final String K_LAST_PART = "last_part";
	public static final String A_NEW_TASK  = "pl.zgora.andre.poznanlbgame.new_task";
	public static final String A_DATA_ARCHEL_PART = "pl.zgora.andre.poznanlbgame.data_archel_part";
	public static final String A_DATA_PLACE = "pl.zgora.andre.poznanlbgame.data_place";
	public static final String A_DATA_ANSWER = "pl.zgora.andre.poznanlbgame.data_answer";
	
	/** Number of task */
	private int id;
	/** Specific data attached to task (might be Answer, Place or ArchitecturalElement) */
	private Data data;
	/** Text containing hint for the task */
	private String text_before;
	/** Text displayed when task is solved */
	private String text_after;
	/** Not used */
	private String img_before;
	/** Not used */
	private String img_after;
	
	/** Creates new instance of task with given data.
	 * @param _id 			number of task (in database)
	 * @param _data			specific data for the task
	 * @param _text_before	text containing hint for the task
	 * @param _text_after	text displayed when task is solved
	 * @param _img_before	not used
	 * @param _img_after	not used */
	public Task(int _id, Data _data, String _text_before, String _text_after, String _img_before,
			String _img_after)
	{
		id 			= _id;
		data 		= _data;
		text_before = _text_before;
		text_after  = _text_after;
		img_before  = _img_before;
		img_after   = _img_after;
	}
	
	/** @return number of the task */
	public int getId()
	{
		return id;
	}
	
	/** @return specific data for the task; might be instance of ArchitecturalElement, Place or Answer */
	public Data getData()
	{
		return data;
	}
	
	/** @param _data data specific for the task; must be instance of ArchitecturalElement, Place or Answer */
	public void setData(Data _data)
	{
		data = _data;
	}
	
	public String getTextBefore()
	{
		return text_before;
	}
	
	public String getTextAfter()
	{
		return text_after;
	}
	
	public String getImgBefore()
	{
		return img_before;
	}
	
	public String getImgAfter()
	{
		return img_after;
	}
	
	/** @return type of data field */
	public Type getType()
	{
		if (data instanceof ArchitecturalElement)
			return Type.ARCHITECTURAL_ELEMENT;
		else if (data instanceof Place)
			return Type.PLACE;
		else if (data instanceof Answer)
			return Type.ANSWER;
		
		Log.e(TAG, "Unknown type");
		return Type.UNKNOWN;
	}
	
	// --------------- PERCELABLE IMPLEMENTATION -----------------
	
	/** Possible methods to create Task from Parcel */
	public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>()
	{
		public Task createFromParcel(Parcel src)
		{
			return new Task(src);
		}
	
		public Task[] newArray(int size)
		{
			return new Task[size];
		}
	};
	
	private Task(Parcel src)
	{
		readFromParcel(src);
	}

	@Override
	public void writeToParcel(Parcel dst, int flags)
	{
		dst.writeInt(id);
		dst.writeString(text_before);
		dst.writeString(text_after);
		dst.writeString(img_before);
		dst.writeString(img_after);
		dst.writeString(getType().toString());
	}
	
	public void readFromParcel(Parcel src)
	{
		id = src.readInt();
		text_before = src.readString();
		text_after = src.readString();
		img_before = src.readString();
		img_after = src.readString();

		switch (Type.valueOf(src.readString()))
		{
		case ARCHITECTURAL_ELEMENT:
			data = new ArchitecturalElement();
			break;
		case PLACE:
			data = new Place();
			break;
		case ANSWER:
			data = new Answer();
			break;
		default:
			Log.e(TAG, "Unknown type");
		}
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
}
