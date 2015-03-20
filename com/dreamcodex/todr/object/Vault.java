package com.dreamcodex.todr.object;

import java.util.StringTokenizer;
import java.util.Vector;

public class Vault extends ItemContainer
{
	protected int code;

	public Vault(Vector<BaseItem> contents, int code)
	{
		super(contents);
		this.code = code;
	}

	public int  getCode()      { return code; }
	public void setCode(int i) { code = i; }

	public String getCodeMatch(int testCode)
	{
		if(testCode == code)
		{
			return Globals.VAULT_CODE_MATCH;
		}
		else
		{
			int placeMatches = 0;
			String sCode = "" + code;
			String sTest = "" + testCode;
			for(int i = 0; i < sCode.length(); i++)
			{
				if(sCode.charAt(i) == sTest.charAt(i))
				{
					placeMatches++;
				}
			}
			return ((testCode > code ? "<" : ">") + placeMatches);
		}
	}

	public String getSaveString()
	{
		if(contents.size() > 0)
		{
			StringBuffer sb = new StringBuffer();
			sb.append("$" + Integer.toString(code, 36).toUpperCase());
			sb.append("(");
			for(int i = 0; i < contents.size(); i++)
			{
				if(i > 0) { sb.append(":"); }
				sb.append(getContentItem(i).getSaveString());
			}
			sb.append(")");
			return sb.toString();
		}
		else
		{
			return "";
		}
	}

	public static Vault createFromString(String strp)
	{
		String sCode = strp.substring(0, strp.indexOf("("));
		int vaultCode = Integer.parseInt(sCode, 36);
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
		return new Vault(treasure, vaultCode);
	}
}