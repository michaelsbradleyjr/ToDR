package com.dreamcodex.todr.object;

import java.util.Vector;

public class ItemContainer
{
	protected Vector<BaseItem> contents;

	public ItemContainer(Vector<BaseItem> contents)
	{
		this.contents = new Vector<BaseItem>(contents);
	}

	public ItemContainer()
	{
		this(new Vector<BaseItem>());
	}

	public Vector<BaseItem> getContents() { return contents; }

	public void setContents(Vector<BaseItem> vc) { contents = new Vector<BaseItem>(vc); }

	public int getContentCount() { return contents.size(); }

	public BaseItem getContentItem(int index)                { return (BaseItem)(contents.elementAt(index)); }
	public void     addContentItem(BaseItem item)            { contents.add(item); }
	public void     delContentItem(int index)                { contents.remove(index); }
	public void     setContentItem(int index, BaseItem item) { contents.setElementAt(item, index); }
	public boolean  hasContentItem(BaseItem item)            { return contents.contains(item); }
}