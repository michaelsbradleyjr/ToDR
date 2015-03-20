package com.dreamcodex.todr.object;

import java.awt.Color;
import java.awt.Image;
import java.util.StringTokenizer;

import com.dreamcodex.todr.object.Globals;

public class Weapon extends BaseItem
{
	protected int     baseDmg;         // normal damage for this weapon
	protected int     currDmg;         // improvements or degradation to weapon (should be limited by global factor, no more that 5 points either way, say)
	protected boolean bRanged;         // is this a ranged weapon?
	protected boolean bThrowable;      // is this weapon thrown when used in a ranged attack?
	protected Image   imgAttack;       // image to use for this weapon when player is in attack mode
	protected int     ammoType;        // name of ammo this weapon requires, if any
	protected int     projectileType;  // projectile effect number
	protected Color   projectileColor; // projectile drawing color
	protected int     soundNum;        // index number of sound to use with this Weapon

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, int currDmg, Image imgNormal, Image imgAttack, int projectileType, Color projectileColor, int soundNum, boolean unique)
	{
		super(Globals.ITEMTYPE_WEAPON, name, (bRanged ? Globals.ITEMCLASS_WPNRNG : Globals.ITEMCLASS_WPNMEL), cost, permissions, imgNormal, unique);
		this.baseDmg         = baseDmg;
		this.bRanged         = bRanged;
		this.ammoType        = ammoType;
		this.bThrowable      = (bThrowable && bRanged);
		this.currDmg         = currDmg;
		this.imgAttack       = imgAttack;
		this.projectileType  = projectileType;
		this.projectileColor = projectileColor;
		this.soundNum        = soundNum;
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, int currDmg, Image imgNormal, Image imgAttack, int projectileType, Color projectileColor, int soundNum)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, currDmg, imgNormal, imgAttack, projectileType, projectileColor, soundNum, false);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, int currDmg, Image imgNormal, Image imgAttack, int soundNum, boolean unique)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, currDmg, imgNormal, imgAttack, Globals.PROJECTILE_NONE, Globals.PROJCLR_OUTLINE, soundNum, unique);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, int currDmg, Image imgNormal, Image imgAttack, int soundNum)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, currDmg, imgNormal, imgAttack, Globals.PROJECTILE_NONE, Globals.PROJCLR_OUTLINE, soundNum, false);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, int currDmg)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, currDmg, (Image)null, (Image)null, Globals.SOUND_NONE);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, Image imgNormal, Image imgAttack, int projectileType, Color projectileColor, int soundNum, boolean unique)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, baseDmg, imgNormal, imgAttack, projectileType, projectileColor, soundNum, unique);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, Image imgNormal, Image imgAttack, int projectileType, Color projectileColor, int soundNum)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, baseDmg, imgNormal, imgAttack, projectileType, projectileColor, soundNum);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable, Image imgNormal, Image imgAttack, int soundNum)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, baseDmg, imgNormal, imgAttack, soundNum);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, boolean bThrowable)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, bThrowable, baseDmg);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions, int currDmg)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, true, currDmg);
	}

	public Weapon(String name, int baseDmg, int cost, boolean bRanged, int ammoType, String permissions)
	{
		this(name, baseDmg, cost, bRanged, ammoType, permissions, baseDmg);
	}

	public Weapon(String name, int baseDmg, int cost)
	{
		this(name, baseDmg, cost, false, Globals.AMMO_NOT_REQUIRED, Globals.PERMIT_ALL);
	}

	public int     getBaseDmg()         { return baseDmg; }
	public int     getCurrDmg()         { return currDmg; }
	public boolean isRanged()           { return bRanged; }
	public boolean isThrowable()        { return bThrowable; }
	public int     getAmmoType()        { return ammoType; }
	public int     getProjectileType()  { return projectileType; }
	public Color   getProjectileColor() { return projectileColor; }
	public int     getSoundNumber()     { return soundNum; }

	public Image getImageNormal() { return this.getImage(); }
	public Image getImageAttack() { if(imgAttack != null) { return imgAttack; } else { return this.getImageNormal(); }  }

	public Weapon getInstance(int modDamage)
	{
		Weapon weapon = new Weapon(this.getName(), this.getBaseDmg(), this.getCost(), this.isRanged(), this.getAmmoType(), this.getPermissions(), this.isThrowable(), this.getImageNormal(), this.getImageAttack(), this.getProjectileType(), this.getProjectileColor(), this.getSoundNumber(), this.isUnique());
		weapon.alterDmg(modDamage);
		return weapon;
	}

	public Weapon getInstance()
	{
		return this.getInstance(0);
	}

	public boolean requiresAmmo()
	{
		return (ammoType != Globals.AMMO_NOT_REQUIRED);
	}

	public void alterDmg(int delta)
	{
		if(delta < 0)
		{
			currDmg = Math.max(currDmg + delta, 0);
		}
		else
		{
			currDmg = Math.min(currDmg + delta, baseDmg + Globals.MAXIMUM_CHANGE);
		}
	}

	public String getInventoryText()
	{
		return this.getName() + " (" + this.getCurrDmg() + ")";
	}

	public String getSaveString()
	{
		return "W" + Globals.INDEX_ARSENAL.indexOf(this.getName()) + "|" + (this.getCurrDmg() - this.getBaseDmg());
	}

	public static Weapon createFromString(String strp)
	{
		StringTokenizer stParse = new StringTokenizer(strp, "|", false);
		int   itemNum = Integer.parseInt(stParse.nextToken());
		int   altDamg = Integer.parseInt(stParse.nextToken());
		return ((Weapon)(Globals.ARSENAL.get(Globals.INDEX_ARSENAL.elementAt(itemNum).toString()))).getInstance(altDamg);
	}
}