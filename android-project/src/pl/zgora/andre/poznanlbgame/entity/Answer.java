package pl.zgora.andre.poznanlbgame.entity;

import android.os.Parcel;
import android.os.Parcelable;

/** Data specific for text-based task */
public class Answer extends Data implements Parcelable
{
	/** Text to be compared by-sign with user input */
	private String text;
	
	public Answer(int id, String _text)
	{
		super(id);
		text = _text;
	}
	
	/** Creates instance with id == -1 and text = null */
	public Answer()
	{
		super(-1);
	}
	
	/** @return text answer for the task */
	public String getText()
	{
		return text;
	}
	
	// --------------- PERCELABLE IMPLEMENTATION -----------------

	/** Possible methods to create Answer from Parcel */
	public static final Parcelable.Creator<Answer> CREATOR = new Parcelable.Creator<Answer>()
	{
		
		public Answer createFromParcel(Parcel src)
		{
			return new Answer(src);
		}
	
		public Answer[] newArray(int size)
		{
			return new Answer[size];
		}
	};
	
	private Answer(Parcel src)
	{
		super(src.readInt());
		
		readFromParcel(src);
	}
	
	@Override
	public void writeToParcel(Parcel dst, int flags)
	{
		dst.writeInt(id);
		dst.writeString(text);
	}
	
	public void readFromParcel(Parcel src)
	{
		text = src.readString();
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
}
