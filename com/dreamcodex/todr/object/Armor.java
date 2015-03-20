package com.dreamcodex.todr.object;

import java.awt.Image;
import java.util.StringTokenizer;

import com.dreamcodex.todr.object.Globals;

public class Armor extends BaseItem
{
	protected int     baseProt;    // normal protection provided by this armor
	protected int     currProt;    // improvements or degradation to armor (should be limited by global factor, no more that 5 points either way, say)
	protected boolean bBodyArmor;  // is this body armor? if not, it's special slot armor (shield)
	protected boolean bChangeable; // can this armor's protection rating be altered by magic, attacks, etc?

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions, boolean bChangeable, int currProt, Image imgNormal, boolean unique)
	{
		super(Globals.ITEMTYPE_ARMOR, name, (bBodyArmor ? Globals.ITEMCLASS_ARMBDY : Globals.ITEMCLASS_ARMSPC), cost, permissions, imgNormal, unique);
		this.baseProt    = baseProt;
		this.bBodyArmor  = bBodyArmor;
		this.bChangeable = bChangeable;
		this.currProt    = currProt;
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions, boolean bChangeable, int currProt, Image imgNormal)
	{
		this(name, baseProt, cost, bBodyArmor, permissions, bChangeable, currProt, imgNormal, false);
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions, boolean bChangeable, int currProt)
	{
		this(name, baseProt, cost, bBodyArmor, permissions, bChangeable, currProt, (Image)null);
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions, boolean bChangeable, Image imgNormal, boolean unique)
	{
		this(name, baseProt, cost, bBodyArmor, permissions, bChangeable, baseProt, imgNormal, unique);
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions, boolean bChangeable, Image imgNormal)
	{
		this(name, baseProt, cost, bBodyArmor, permissions, bChangeable, baseProt, imgNormal);
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions, boolean bChangeable)
	{
		this(name, baseProt, cost, bBodyArmor, permissions, bChangeable, baseProt);
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions, int currProt)
	{
		this(name, baseProt, cost, bBodyArmor, permissions, true, currProt);
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor, String permissions)
	{
		this(name, baseProt, cost, bBodyArmor, permissions, baseProt);
	}

	public Armor(String name, int baseProt, int cost, boolean bBodyArmor)
	{
		this(name, baseProt, cost, bBodyArmor, Globals.PERMIT_ALL);
	}

	public Armor(String name, int baseProt, int cost)
	{
		this(name, baseProt, cost, true);
	}

	public String  getName()      { return name; }
	public int     getBaseProt()  { return baseProt; }
	public int     getCurrProt()  { return currProt; }
	public int     getCost()      { return cost; }
	public boolean isBodyArmor()  { return bBodyArmor; }
	public boolean isChangeable() { return bChangeable; }

	public void setName(String s)          { name = new String(s); }
	public void setBaseProt(int i)         { baseProt = i; }
	public void setCurrProt(int i)         { currProt = i; }
	public void setCost(int i)             { cost = i; }
	public void setIsBodyArmor(boolean b)  { bBodyArmor = b; }
	public void setIsChangeable(boolean b) { bChangeable = b; }

	public Armor getInstance(int modProt)
	{
		Armor armor = new Armor(this.getName(), this.getBaseProt(), this.getCost(), this.isBodyArmor(), this.getPermissions(), this.isChangeable(), this.getImage(), this.isUnique());
		armor.alterProt(modProt);
		return armor;
	}

	public Armor getInstance()
	{
		return this.getInstance(0);
	}

	public void alterProt(int delta)
	{
		if(delta < 0)
		{
			currProt = Math.max(currProt + delta, 0);
		}
		else
		{
			currProt = Math.min(currProt + delta, baseProt + Globals.MAXIMUM_CHANGE);
		}
	}

	public String getInventoryText()
	{
		return this.getName() + " (" + this.getCurrProt() + ")";
	}

	public String getSaveString()
	{
		return "A" + Globals.INDEX_ARMOURY.indexOf(this.getName()) + "|" + (this.getCurrProt() - this.getBaseProt());
	}

	public static Armor createFromString(String strp)
	{
		StringTokenizer stParse = new StringTokenizer(strp, "|", false);
		int   itemNum = Integer.parseInt(stParse.nextToken());
		int   altProt = Integer.parseInt(stParse.nextToken());
		return ((Armor)(Globals.ARMOURY.get(Globals.INDEX_ARMOURY.elementAt(itemNum).toString()))).getInstance(altProt);
	}
}