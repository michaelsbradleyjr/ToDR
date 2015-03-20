package com.dreamcodex.todr.object;

import java.awt.Image;

/** MonsterDef
  *   Baseline Monster definition file
  *   Describes Monster types, generates specific Monster instances
  *
  * @author Howard Kistler
  */

public class MonsterDef
{
	protected String  name;          // name of this kind of monster (also used as a key to refer back to this definition within the game)
	protected int     level;         // Monster level, represents overall rating & also hit dice
	protected int     expGrant;      // how much experience a kill of this monster grants the player (stored in Lifeform.experience field in Monster class)
	protected int     defend;        // (stored in Lifeform.defense field in Monster class)
	protected int     attack;        // (stored in Lifeform.attack field in Monster class)
	protected int     maxDmg;        // the most damage the monster does in an attack
	protected boolean ranged;        // attack is ranged?
	protected Effect  effect;        // what kind of special attack/effect the Monster may perform
	protected int     effectPer;     // percent chance of the monster attempting the Special Effect on a given round
	protected boolean effectRanged;  // special attack is ranged?
	protected int     speed;         // (stored in Lifeform.speed field in Monster class)
	protected int     mobility;      // percent chance a monster will move (if necessary) on a given combat round
	protected int     resistance;    // (stored in Lifeform.resistance field in Monster class)
	protected int     negotiation;   // percent chance a monster will be likely to negotiate
	protected int     maxGroup;      // the most of this kind of monster you'll encounter at once
	protected int     soundNum;      // number of the sound effect that is to be used for monsters of this type
	protected boolean unique;        // is the monster a one-of-a-kind unique?
	protected String  mobType;       // name of the subordinate monsters that follow this unique
	protected Image   imgNormal;     // image of Monster in normal wait state
	protected Image   imgAttack;     // image of Monster performing attack
	protected Image   imgEffect;     // image of Monster performing special effect

	public static final String MOBTYPE_NONE = "(none)";

	public MonsterDef(String name, int level, int defend, int attack, int maxDmg, int resistance, int mobility, int negotiation, int speed, int expGrant, int maxGroup, int soundNum, boolean unique, String mobType, Effect effect, int effectPer, boolean effectRanged, Image imgNormal, Image imgAttack, Image imgEffect)
	{
		this.name          = new String(name);
		this.level         = level;
		this.defend        = defend;
		this.ranged        = (attack < 0);
		this.attack        = Math.abs(attack);
		this.maxDmg        = maxDmg;
		this.effectPer     = effectPer;
		this.effect        = effect;
		this.effectRanged  = effectRanged;
		this.resistance    = resistance;
		this.mobility      = mobility;
		this.negotiation   = negotiation;
		this.speed         = speed;
		this.expGrant      = expGrant;
		this.maxGroup      = maxGroup;
		this.soundNum      = soundNum;
		this.unique        = unique;
		this.mobType       = (mobType != null ? new String(mobType) : (String)null);
		this.imgNormal     = imgNormal;
		this.imgAttack     = imgAttack;
		this.imgEffect     = imgEffect;
	}

	public MonsterDef(String name, int level, int defend, int attack, int maxDmg, int resistance, int mobility, int negotiation, int speed, int expGrant, int maxGroup, int soundNum, Effect effect, int effectPer, boolean effectRanged, Image imgNormal, Image imgAttack, Image imgEffect)
	{
		this(name, level, defend, attack, maxDmg, resistance, mobility, negotiation, speed, expGrant, maxGroup, soundNum, false, MOBTYPE_NONE, effect, effectPer, effectRanged, imgNormal, imgAttack, imgEffect);
	}

	public String  getName()         { return name; }
	public int     getLevel()        { return level; }
	public int     getMaxDamage()    { return maxDmg; }
	public int     getAttack()       { return attack; }
	public boolean getRanged()       { return ranged; }
	public int     getDefend()       { return defend; }
	public int     getSpeed()        { return speed; }
	public int     getMobility()     { return mobility; }
	public int     getResistance()   { return resistance; }
	public int     getNegotiation()  { return negotiation; }
	public Effect  getEffect()       { return effect; }
	public int     getEffectPer()    { return effectPer; }
	public boolean getEffectRanged() { return effectRanged; }
	public int     getExpGrant()     { return expGrant; }
	public Image   getImgNormal()    { return imgNormal; }
	public Image   getImgAttack()    { return imgAttack; }
	public Image   getImgEffect()    { return imgEffect; }
	public int     getMaxGroup()     { return maxGroup; }
	public int     getSoundNumber()  { return soundNum; }
	public boolean isUnique()        { return unique; }
	public String  getMobType()      { return mobType; }

	public int rollHitPoints()
	{
		int hps = 0;
		for(int i = 0; i < this.getLevel(); i++)
		{
			hps = hps + (Globals.rnd.nextInt(6) + 1);
		}
		return hps;
	}

	public Monster getInstance()
	{
		return new Monster(this.getName(), this.getLevel(), this.rollHitPoints(), 0, this.getDefend(), this.getAttack(), this.getResistance(), this.getSpeed(), this.getExpGrant());
	}
}