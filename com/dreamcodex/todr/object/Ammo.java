package com.dreamcodex.todr.object;

import java.awt.Image;
import java.util.StringTokenizer;

import com.dreamcodex.todr.object.Globals;

public class Ammo extends BaseItem
{
	protected int quantity; // default bundle size in store for this Ammo, otherwise the amount in the Player's ammoSatchel

	public Ammo(String name, int cost, int quantity, String permissions, Image imgNormal)
	{
		super(Globals.ITEMTYPE_AMMO, name, Globals.ITEMCLASS_AMMO, cost, permissions, imgNormal);
		this.quantity = quantity;
	}

	public Ammo(String name, int cost, int quantity, String permissions)
	{
		this(name, cost, quantity, permissions, (Image)null);
	}

	public Ammo(String name, int cost, int quantity)
	{
		this(name, cost, quantity, Globals.PERMIT_ALL);
	}

	public String getName()     { return name; }
	public int    getCost()     { return cost; }
	public int    getQuantity() { return quantity; }

	public void setName(String s)  { name = new String(s); }
	public void setCost(int i)     { cost = i; }
	public void setQuantity(int i) { quantity = i; }

	public Ammo getInstance(int quant)
	{
		return new Ammo(this.getName(), this.getCost(), quant, this.getPermissions(), this.getImage());
	}

	public Ammo getInstance()
	{
		return this.getInstance(this.getQuantity());
	}

	public int getNumber()
	{
		return Globals.INDEX_AMMOLIST.indexOf(this.getName());
	}

	public String getSaveString()
	{
		return "X" + Globals.INDEX_AMMOLIST.indexOf(this.getName()) + "|" + this.getQuantity();
	}

	public static Ammo createFromString(String strp)
	{
		StringTokenizer stParse = new StringTokenizer(strp, "|", false);
		int ammoNum = Integer.parseInt(stParse.nextToken());
		int ammoQnt = Integer.parseInt(stParse.nextToken());
		return ((Ammo)(Globals.AMMOLIST.elementAt(ammoNum))).getInstance(ammoQnt);
	}
}