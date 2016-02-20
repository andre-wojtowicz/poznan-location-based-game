package pl.zgora.andre.poznanlbgame.entity;

import java.util.ArrayList;

/** Data specific for photo-based task */
public class ArchitecturalElement extends Data
{
	/** List of photos -- SIFT keypoints */
	private ArrayList<View> views;
	
	public ArchitecturalElement(int id, ArrayList<View> _views)
	{
		super(id);
		views = _views;
	}
	
	/** Creates new instance with id == -1 and views == null */
	public ArchitecturalElement()
	{
		super(-1);
	}
	
	/** @return list of views -- photos in SIFT format */
	public ArrayList<View> getViews()
	{
		return views;
	}
	
	/** @param _views list of preprocessed photos -- patterns in the task */
	public void setViews(ArrayList<View> _views)
	{
		views = _views;
	}
}
