package com.dreamcodex.todr.object;

import java.awt.Image;

import com.dreamcodex.todr.object.CharacterClass;
import com.dreamcodex.todr.object.Globals;

/** Baseline Item Definition
  * Represents all the properties item classes (Item, Weapon, Armor) have in common
  */

public class BaseItem implements Comparable
{
	protected int       type;
	protected String    name;
	protected ItemClass itemClass;   // class of items this belongs to
	protected int       cost;        // how much currency you will pay for this in a shop (-1 indicates it is not sold in shops)
	protected String    permissions; // a string containing either * (for everyone) or a series of characters that map to the CharacterClass keys, indicating which classes can use this item
	protected Image     imgPicture = (Image)null;
	protected boolean   unique;

	public BaseItem(int type, String name, ItemClass itemClass, int cost, String permissions, Image img, boolean unique)
	{
		this.type        = type;
		this.name        = new String(name);
		this.itemClass   = itemClass;
		this.cost        = cost;
		this.permissions = new String(permissions);
		imgPicture       = img;
		this.unique      = unique;
	}

	public BaseItem(int type, String name, ItemClass itemClass, int cost, String permissions, Image img)
	{
		this(type, name, itemClass, cost, permissions, img, false);
	}

	public BaseItem(int type, String name, ItemClass itemClass, int cost, String permissions, boolean unique)
	{
		this(type, name, itemClass, cost, permissions, (Image)null, unique);
	}

	public BaseItem(int type, String name, ItemClass itemClass, int cost, String permissions)
	{
		this(type, name, itemClass, cost, permissions, (Image)null);
	}

	public BaseItem()
	{
		this(Globals.ITEMTYPE_UNDEFINED, "[default]", Globals.ITEMCLASS_UNDEF, -1, "");
	}

	public int       getType()        { return type; }
	public String    getName()        { return name; }
	public ItemClass getItemClass()   { return itemClass; }
	public int       getCost()        { return cost; }
	public String    getPermissions() { return permissions; }
	public Image     getImage()       { return imgPicture; }
	public Image     getClassIcon()   { return itemClass.getIcon(); }
	public boolean   isUnique()       { return unique; }

	public int getSortSeq()
	{
		return this.getItemClass().getSortSeq();
	}

	public boolean isPermitted(CharacterClass cc)
	{
		return Globals.isPermitted(cc, permissions);
	}

	public String getSaveString()
	{
		return "B" + getType();
	}

	public boolean equals(BaseItem bi)
	{
		if(bi == null) { return false; }
		return ((bi.getType() == this.getType()) && (bi.getName().equals(this.getName())) && (bi.getItemClass() == this.getItemClass()) && (bi.getCost() == this.getCost()) && (bi.getPermissions().equals(this.getPermissions())) && (this.isUnique() == bi.isUnique()));
	}

	public String getInventoryText()
	{
		return this.getName();
	}

	/* Comparable interface methods */
	public int compareTo(Object o)
	{
		if(o instanceof BaseItem)
		{
			return this.getSortSeq() - ((BaseItem)o).getSortSeq();
		}
		return 1;
	}
}
