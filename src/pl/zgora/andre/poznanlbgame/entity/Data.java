package pl.zgora.andre.poznanlbgame.entity;

/** Root specific data for the task */
public abstract class Data
{
	/** Number of the task (in database) */
	protected int id;
	
	/** Creates new instance of Data.
	 * @param _id number of the task (in database) */
	public Data(int _id)
	{
		id = _id;
	}
	
	/** @return number of the task (in database) */
	public int getId()
	{
		return id;
	}
}
