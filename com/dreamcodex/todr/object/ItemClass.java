package com.dreamcodex.todr.object;

import java.awt.Image;

/** Item Grouping Classes
  * Represents groups of objects (Scrolls, Potions, Wands, etc) which share common traits and permissions
  */

public class ItemClass
{
	private String classKey;    // character key that identifies this class, so that it may be linked to other objects (for example, a Weapon to check if this character class can wield it or not)
	private String name;        // the name for this class of character
	private String permissions; // a string containing either * (for everyone) or a series of characters that map to the CharacterClass keys, indicating which classes can use this item
	private Image  imgPicture;  // the image that is shown for items of this class
	private Image  imgIcon;     // the image that is used for inventory list and other categoric uses
	private int    sortSeq;     // sort rank of this item class among all item classes

	public ItemClass(String classKey, String name, String permissions, Image picture, Image icon, int sortSeq)
	{
		this.classKey    = new String(classKey);
		this.name        = new String(name);
		this.permissions = new String(permissions);
		this.imgPicture  = picture;
		this.imgIcon     = icon;
		this.sortSeq     = sortSeq;
	}

	public String getClassKey()    { return classKey; }
	public String getName()        { return name; }
	public String getPermissions() { return permissions; }
	public Image  getImage()       { return imgPicture; }
	public Image  getIcon()        { return imgIcon; }
	public int    getSortSeq()     { return sortSeq; }

	public boolean isPermitted(CharacterClass cc)
	{
		return Globals.isPermitted(cc, permissions);
	}
}
