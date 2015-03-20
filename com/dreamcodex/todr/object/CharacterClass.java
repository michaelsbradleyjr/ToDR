package com.dreamcodex.todr.object;

import java.util.StringTokenizer;
import java.util.Vector;

public class CharacterClass
{
	private char classKey;   // single character code that identifies this class, so that it may be linked to other objects (for example, a Weapon to check if this character class can wield it or not)
	private String name;     // the name for this class of character
	private int expLevelUp;  // how much experience the character must accrue to reach the next level (could also be implemented as an array, for variable requirements per level) (if any array, once level passes final value in array, just use last value for each subsequent level)
	private int baseHP;      // base HitPoints (how many HP a character of this class starts with) (could also be implemented as hit dice)
	private int hpLevelGain; // how many HitPoints the character gains upon going up a level (could also be implemented as an array, for variable rewards per level) (could also be implemented as hit dice)
	private int baseSpeed;
	private int baseLuck;
	private int meleeAdd;
	private int rangeAdd;
	private int luckGain; // how much Luck the character gains upon going up a level (could also be implemented as an array, for variable rewards per level) (could also be implemented as hit dice)
	private int luckIntv; // the interval of Luck gain by levels (1 = every level, 2 = every other level, 3 = every third level, etc)
	private int baseResistance;
	private int resistanceGain; // how much Resistance the character gains upon going up a level (could also be implemented as an array, for variable rewards per level) (could also be implemented as hit dice)
	private int resistanceIntv; // the interval of Resistance gain by levels (1 = every level, 2 = every other level, 3 = every third level, etc)
	private int baseBonusMelee;
	private int meleeBonusGain; // how much Melee Weapon Bonus the character gains upon going up a level (could also be implemented as an array, for variable rewards per level) (could also be implemented as hit dice)
	private int meleeBonusIntv; // the interval of Melee Weapon Bonus gain by levels (1 = every level, 2 = every other level, 3 = every third level, etc)
	private int baseBonusRange;
	private int rangeBonusGain; // how much Ranged Weapon Bonus the character gains upon going up a level (could also be implemented as an array, for variable rewards per level) (could also be implemented as hit dice)
	private int rangeBonusIntv; // the interval of Ranged Weapon Bonus gain by levels (1 = every level, 2 = every other level, 3 = every third level, etc)
	private int baseBonusArmor;
	private int armorBonusGain; // how much Armor Bonus the character gains upon going up a level (could also be implemented as an array, for variable rewards per level) (could also be implemented as hit dice)
	private int armorBonusIntv; // the interval of Armor Bonus gain by levels (1 = every level, 2 = every other level, 3 = every third level, etc)
	private int baseTrapResist;
	private int trapBonusGain; // how much Trap Resistance the character gains upon going up a level (could also be implemented as an array, for variable rewards per level) (could also be implemented as hit dice)
	private int trapBonusIntv; // the interval of Trap Resistance gain by levels (1 = every level, 2 = every other level, 3 = every third level, etc)
	private boolean soloMode; // is this character class only allowed in solo-mode games?
	private Vector<Item> startInv; // collection of items a new character of this class starts with

	public CharacterClass(char classKey, String name, int expLevelUp, int baseHP, int hpLevelGain, int baseSpeed, int baseLuck, int meleeAdd, int rangeAdd, int baseBonusMelee, int baseBonusRange, int baseBonusArmor, int baseResistance, int baseTrapResist, int lintv, int lgain, int xintv, int xgain, int mintv, int mgain, int rintv, int rgain, int aintv, int again, int tintv, int tgain, boolean soloMode, Vector<Item> sInv)
	{
		this.classKey       = classKey;
		this.name           = new String(name);
		this.expLevelUp     = expLevelUp;
		this.baseHP         = baseHP;
		this.hpLevelGain    = hpLevelGain;
		this.baseSpeed      = baseSpeed;
		this.baseLuck       = baseLuck;
		this.meleeAdd       = meleeAdd;
		this.rangeAdd       = rangeAdd;
		this.baseBonusMelee = baseBonusMelee;
		this.baseBonusRange = baseBonusRange;
		this.baseBonusArmor = baseBonusArmor;
		this.baseResistance = baseResistance;
		this.baseTrapResist = baseTrapResist;
		this.luckIntv       = lintv;
		this.luckGain       = lgain;
		this.resistanceIntv = xintv;
		this.resistanceGain = xgain;
		this.meleeBonusIntv = mintv;
		this.meleeBonusGain = mgain;
		this.rangeBonusIntv = rintv;
		this.rangeBonusGain = rgain;
		this.armorBonusIntv = aintv;
		this.armorBonusGain = again;
		this.trapBonusIntv  = tintv;
		this.trapBonusGain  = tgain;
		this.soloMode       = soloMode;
		this.startInv       = new Vector<Item>(sInv);
	}

	public CharacterClass(CharacterClass cc)
	{
		this(cc.getClassKey(), cc.getName(), cc.getLevelUpThreshold(), cc.getBaseHitpoints(), cc.getHPGainedPerLevel(), cc.getBaseSpeed(), cc.getBaseLuck(), cc.getMeleeAdd(), cc.getRangeAdd(), cc.getBaseMeleeBonus(), cc.getBaseRangeBonus(), cc.getBaseArmorBonus(), cc.getBaseResistance(), cc.getBaseTrapResist(), cc.getLuckGain(), cc.getLuckIntv(), cc.getResistanceGain(), cc.getResistanceIntv(), cc.getMeleeBonusGain(), cc.getMeleeBonusIntv(), cc.getRangeBonusGain(), cc.getRangeBonusIntv(), cc.getArmorBonusGain(), cc.getArmorBonusIntv(), cc.getTrapBonusGain(), cc.getTrapBonusIntv(), cc.isSoloModeChar(), cc.getStartInv());
	}

	public char    getClassKey()         { return classKey; }
	public String  getName()             { return name; }
	public int     getLevelUpThreshold() { return expLevelUp; }
	public int     getBaseHitpoints()    { return baseHP; }
	public int     getBaseSpeed()        { return baseSpeed; }
	public int     getBaseLuck()         { return baseLuck; }
	public int     getMeleeAdd()         { return meleeAdd; }
	public int     getRangeAdd()         { return rangeAdd; }
	public int     getBaseMeleeBonus()   { return baseBonusMelee; }
	public int     getBaseRangeBonus()   { return baseBonusRange; }
	public int     getBaseArmorBonus()   { return baseBonusArmor; }
	public int     getBaseResistance()   { return baseResistance; }
	public int     getBaseTrapResist()   { return baseTrapResist; }
	public int     getHPGainedPerLevel() { return hpLevelGain; }
	public int     getLuckGain()         { return luckGain; }
	public int     getLuckIntv()         { return luckIntv; }
	public int     getResistanceGain()   { return resistanceGain; }
	public int     getResistanceIntv()   { return resistanceIntv; }
	public int     getMeleeBonusGain()   { return meleeBonusGain; }
	public int     getMeleeBonusIntv()   { return meleeBonusIntv; }
	public int     getRangeBonusGain()   { return rangeBonusGain; }
	public int     getRangeBonusIntv()   { return rangeBonusIntv; }
	public int     getArmorBonusGain()   { return armorBonusGain; }
	public int     getArmorBonusIntv()   { return armorBonusIntv; }
	public int     getTrapBonusGain()    { return trapBonusGain; }
	public int     getTrapBonusIntv()    { return trapBonusIntv; }
	public boolean isSoloModeChar()      { return soloMode; }
	public Vector<Item> getStartInv()    { return startInv; }

	public String getSaveString()
	{
		StringBuffer sbData = new StringBuffer();
		sbData.append(getClassKey() + ",");
		sbData.append(getName() + ",");
		sbData.append(getLevelUpThreshold() + ",");
		sbData.append(getBaseHitpoints() + ",");
		sbData.append(getHPGainedPerLevel() + ",");
		sbData.append(getBaseSpeed() + ",");
		sbData.append(getBaseLuck() + ",");
		sbData.append(getMeleeAdd() + ",");
		sbData.append(getRangeAdd() + ",");
		sbData.append(getBaseMeleeBonus() + ",");
		sbData.append(getBaseRangeBonus() + ",");
		sbData.append(getBaseArmorBonus() + ",");
		sbData.append(getBaseResistance() + ",");
		sbData.append(getBaseTrapResist() + ",");
		sbData.append(getLuckGain() + ",");
		sbData.append(getLuckIntv() + ",");
		sbData.append(getResistanceGain() + ",");
		sbData.append(getResistanceIntv() + ",");
		sbData.append(getMeleeBonusGain() + ",");
		sbData.append(getMeleeBonusIntv() + ",");
		sbData.append(getRangeBonusGain() + ",");
		sbData.append(getRangeBonusIntv() + ",");
		sbData.append(getArmorBonusGain() + ",");
		sbData.append(getArmorBonusIntv() + ",");
		sbData.append(getTrapBonusGain() + ",");
		sbData.append(getTrapBonusIntv() + ",");
		sbData.append((isSoloModeChar() ? "1" : "0") + ",");
		sbData.append("{");
		for(int i = 0; i < startInv.size(); i++)
		{
			if(i > 0) { sbData.append(":"); }
			sbData.append(((Item)(startInv.elementAt(i))).getSaveString());
		}
		sbData.append("}");
		return sbData.toString();
	}

	public static CharacterClass createFromString(String strp)
	{
		StringTokenizer stParse = new StringTokenizer(strp, ",", false);
		char classKey      = stParse.nextToken().charAt(0);
		String className   = stParse.nextToken();
		int levelUp        = Integer.parseInt(stParse.nextToken());
		int baseHP         = Integer.parseInt(stParse.nextToken());
		int gainHP         = Integer.parseInt(stParse.nextToken());
		int baseSpeed      = Integer.parseInt(stParse.nextToken());
		int baseLuck       = Integer.parseInt(stParse.nextToken());
		int meleeAdd       = Integer.parseInt(stParse.nextToken());
		int rangeAdd       = Integer.parseInt(stParse.nextToken());
		int baseMelee      = Integer.parseInt(stParse.nextToken());
		int baseRange      = Integer.parseInt(stParse.nextToken());
		int baseArmor      = Integer.parseInt(stParse.nextToken());
		int baseResist     = Integer.parseInt(stParse.nextToken());
		int baseTrapRs     = Integer.parseInt(stParse.nextToken());
		int luckIntv       = Integer.parseInt(stParse.nextToken());
		int luckGain       = Integer.parseInt(stParse.nextToken());
		int resistanceIntv = Integer.parseInt(stParse.nextToken());
		int resistanceGain = Integer.parseInt(stParse.nextToken());
		int meleeBonusIntv = Integer.parseInt(stParse.nextToken());
		int meleeBonusGain = Integer.parseInt(stParse.nextToken());
		int rangeBonusIntv = Integer.parseInt(stParse.nextToken());
		int rangeBonusGain = Integer.parseInt(stParse.nextToken());
		int armorBonusIntv = Integer.parseInt(stParse.nextToken());
		int armorBonusGain = Integer.parseInt(stParse.nextToken());
		int trapBonusIntv  = Integer.parseInt(stParse.nextToken());
		int trapBonusGain  = Integer.parseInt(stParse.nextToken());
		boolean isSoloChar = (Integer.parseInt(stParse.nextToken()) == 1);
		String startingInv = stParse.nextToken();
		Vector<Item> vInv  = new Vector<Item>();
		if(!(startingInv.equals(Globals.EMPTY_INVENTORY_STRING)))
		{
			String invlist = startingInv.substring(1, startingInv.length() - 1);
			stParse = new StringTokenizer(invlist, ":", false);
			while(stParse.hasMoreTokens())
			{
				vInv.add(Item.createFromString(stParse.nextToken()));
			}
		}
		return new CharacterClass(classKey, className, levelUp, baseHP, gainHP, baseSpeed, baseLuck, meleeAdd, rangeAdd, baseMelee, baseRange, baseArmor, baseResist, baseTrapRs, luckIntv, luckGain, resistanceIntv, resistanceGain, meleeBonusIntv, meleeBonusGain, rangeBonusIntv, rangeBonusGain, armorBonusIntv, armorBonusGain, trapBonusIntv, trapBonusGain, isSoloChar, vInv);
	}

}
