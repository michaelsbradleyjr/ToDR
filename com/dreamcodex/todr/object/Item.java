package com.dreamcodex.todr.object;

import java.awt.Image;
import java.util.StringTokenizer;

import com.dreamcodex.todr.object.Effect;
import com.dreamcodex.todr.object.Globals;
import com.dreamcodex.todr.object.ItemClass;

public class Item extends BaseItem
{
	protected int     charges;      // maximum number of times this item can be used
	protected Effect  effect;       // item's effect, of course
	protected int     value;        // a quantifier for items without Effects, such as currency or rations
	protected boolean bImmediate;   // whether or not item takes effect upon being taken (like the Map item)
	protected boolean bGroupPickUp; // if true, this is picked up by the group with a "get", no need to query individual player
	protected boolean bIdentified;  // has this item been identified yet?

	public Item(int type, ItemClass itemClass, String name, int charges, Effect effect, int value, boolean bImmediate, boolean bGroupPickUp, int cost, boolean bIdentified, boolean unique)
	{
		super(type, name, itemClass, cost, itemClass.getPermissions(), unique);
		this.charges      = (bImmediate ? 0 : Math.max(charges, 0));
		this.effect       = new Effect(effect);
		this.value        = value;
		this.bImmediate   = bImmediate;
		this.bGroupPickUp = bGroupPickUp;
		this.bIdentified  = bIdentified;
		if(itemClass == Globals.ITEMCLASS_CURRENCY || itemClass == Globals.ITEMCLASS_RATION)
		{
			this.effect.setName(name);
			this.effect.setPower(value);
		}
	}

	public Item(int type, ItemClass itemClass, String name, int charges, Effect effect, int value, boolean bImmediate, boolean bGroupPickUp, int cost, boolean bIdentified)
	{
		this(type, itemClass, name, charges, effect, value, bImmediate, bGroupPickUp, cost, bIdentified, false);
	}

	public Item(ItemClass itemClass, String name, int charges, Effect effect, int value, boolean bImmediate, boolean bGroupPickUp, int cost, boolean bIdentified, boolean unique)
	{
		this(Globals.ITEMTYPE_ITEM, itemClass, name, charges, effect, value, bImmediate, bGroupPickUp, cost, bIdentified, unique);
	}

	public Item(ItemClass itemClass, String name, int charges, Effect effect, int value, boolean bImmediate, boolean bGroupPickUp, int cost, boolean bIdentified)
	{
		this(Globals.ITEMTYPE_ITEM, itemClass, name, charges, effect, value, bImmediate, bGroupPickUp, cost, bIdentified, false);
	}

	public Item(ItemClass itemClass, String name, int charges, Effect effect, int value, boolean bImmediate, boolean bGroupPickUp, int cost)
	{
		this(itemClass, name, charges, effect, value, bImmediate, bGroupPickUp, cost, false);
	}

	public Item(ItemClass itemClass, String name, int charges, Effect effect, int value, boolean bImmediate, boolean bGroupPickUp)
	{
		this(itemClass, name, charges, effect, value, bImmediate, bGroupPickUp, Globals.NOT_FOR_SALE);
	}

	public Item(Item itemToClone)
	{
		this(itemToClone.getItemClass(), itemToClone.getName(), itemToClone.getCharges(), itemToClone.getEffect(), itemToClone.getValue(), itemToClone.isImmediate(), itemToClone.isGroupPickUp(), itemToClone.getCost(), itemToClone.isIdentified(), itemToClone.isUnique());
	}

	public int     getCharges()    { return charges; }
	public Effect  getEffect()     { return effect; }
	public int     getValue()      { return value; }
	public boolean isImmediate()   { return bImmediate; }
	public boolean isGroupPickUp() { return bGroupPickUp; }
	public boolean isIdentified()  { return bIdentified; }

	public void setCharges(int i) { charges = i; }
	public void setValue(int i)   { value = i; }

	public Image getImage()
	{
		if(super.getImage() != null)
		{
			return super.getImage();
		}
		else
		{
			return this.getItemClass().getImage();
		}
	}

	public String getIngameName()
	{
		if(bIdentified)
		{
			return this.getName();
		}
		else
		{
			return this.getItemClass().getName();
		}
	}

	public String getInventoryText()
	{
		return (bIdentified ? "" : Globals.getDungeonText("itemUnidentified")) + this.getIngameName();
	}

	public void expendCharge() { charges--; if(charges < 0) { charges = 0; } }
	public void identify()     { bIdentified = true; }
	public void unidentify()   { bIdentified = false; }

	public Item getInstance(int charges, int value)
	{
		return new Item(this.getType(), this.getItemClass(), this.getName(), charges, this.getEffect(), value, this.isImmediate(), this.isGroupPickUp(), this.getCost(), this.isIdentified(), this.isUnique());
	}

	public Item getInstance(int charges)
	{
		return getInstance(charges, this.getValue());
	}

	public Item getInstance()
	{
		return getInstance((this.getCharges() <= 0 ? 0 : Globals.rnd.nextInt(this.getCharges()) + 1));
	}

	public String getSaveString()
	{
		if(type == Globals.ITEMTYPE_MAP)
		{
			return "M|1";
		}
		else if(type == Globals.ITEMTYPE_CURRENCY)
		{
			return "C|" + this.getValue();
		}
		else if(type == Globals.ITEMTYPE_RATIONS)
		{
			return "R|" + this.getValue();
		}
		return "I" + Globals.INDEX_COLLECTUM.indexOf(this.getName()) + "|" + this.getCharges() + "|" + (this.isIdentified() ? "Y" : "N");
	}

	public static Item createFromString(String strp)
	{
		StringTokenizer stParse = new StringTokenizer(strp, "|", false);
		String itemKey = stParse.nextToken();
		int    itemVal = Integer.parseInt(stParse.nextToken());
		if(itemKey.startsWith("M"))
		{
			return Globals.ITEM_MAP.getInstance();
		}
		else if(itemKey.startsWith("C"))
		{
			return Globals.ITEM_CURRENCY.getInstance(1, itemVal);
		}
		else if(itemKey.startsWith("R"))
		{
			return Globals.ITEM_RATION.getInstance(1, itemVal);
		}
		else if(itemKey.startsWith("I"))
		{
			int itemNum = Integer.parseInt(itemKey.substring(1));
			boolean bIded = stParse.nextToken().equals("Y");
			Item item = Globals.getCollectumItem(Globals.INDEX_COLLECTUM.elementAt(itemNum).toString(), itemVal);
			if(bIded) { item.identify(); }
			return item;
		}
		return (Item)null;
	}
}