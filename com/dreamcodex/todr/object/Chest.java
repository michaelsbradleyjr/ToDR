package com.dreamcodex.todr.object;

import java.util.StringTokenizer;
import java.util.Vector;

public class Chest extends ItemContainer
{
	protected int trapNum;
	protected int trapOdds;

	public Chest(Vector<BaseItem> contents, int trapNum, int trapOdds)
	{
		super(contents);
		this.trapNum  = trapNum;
		this.trapOdds = trapOdds;
	}

	public Chest(Vector<BaseItem> contents)
	{
		this(contents, Globals.TRAP_NONE, 0);
	}

	public int getTrapNum()  { return trapNum; }
	public int getTrapOdds() { return trapOdds; }

	public void setTrapNum(int index) { trapNum  = index; }
	public void setTrapOdds(int i)    { trapOdds = i; }

	public boolean hasTrap() { return (trapNum != Globals.TRAP_NONE); }
	public Effect  getTrap() { return (Effect)(Globals.TRAPDEX.elementAt(trapNum)); }

	public String getSaveString()
	{
		if(contents.size() > 0)
		{
			StringBuffer sb = new StringBuffer();
			sb.append("&");
			sb.append("" + (trapNum == Globals.TRAP_NONE ? "-" : trapNum));
			sb.append("(");
			for(int i = 0; i < contents.size(); i++)
			{
				if(i > 0) { sb.append(":"); }
				sb.append(getContentItem(i).getSaveString());
			}
			sb.append(")");
			return sb.toString();
		}
		return "";
	}

	public static Chest createFromString(String strp)
	{
		char trapChar = strp.charAt(0);
		int trapType = (trapChar == '-' ? Globals.TRAP_NONE : Character.digit(trapChar, 10));
		Vector<BaseItem> treasure = new Vector<BaseItem>();
		String sTreasure = strp.substring(strp.indexOf("(") + 1, strp.indexOf(")"));
		StringTokenizer stParse = new StringTokenizer(sTreasure, ":", false);
		while(stParse.hasMoreTokens())
		{
			String itementry = (String)(stParse.nextElement());
			if(itementry.startsWith("W"))
			{
				treasure.add(Weapon.createFromString(itementry.substring(1)));
			}
			else if(itementry.startsWith("A"))
			{
				treasure.add(Armor.createFromString(itementry.substring(1)));
			}
			else if(itementry.startsWith("X"))
			{
				treasure.add(Ammo.createFromString(itementry.substring(1)));
			}
			else if(itementry.startsWith("Q"))
			{
				treasure.add(QuestItem.createFromString(itementry.substring(1)));
			}
			else
			{
				treasure.add(Item.createFromString(itementry));
			}
		}
		if(trapType > -1)
		{
			return new Chest(treasure, trapType, ((Integer)(Globals.TRAPODDS.elementAt(trapType))).intValue());
		}
		else
		{
			return new Chest(treasure);
		}
	}

}