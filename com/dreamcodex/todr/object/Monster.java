package com.dreamcodex.todr.object;

import java.awt.Image;

import com.dreamcodex.todr.object.Lifeform;
import com.dreamcodex.util.MP3Clip;

public class Monster extends Lifeform
{
	protected int mobiper;
	protected int specper;
	protected int negoper;

	public Monster(String name, int lvl, int hp, int wnd, int def, int att, int res, int spd, int exp)
	{
		super(Globals.LIFEFORM_MONSTER, name, lvl, exp, hp, wnd, att, def, spd, res);
		mobiper = this.getMonsterDef().getMobility();
		specper = this.getMonsterDef().getEffectPer();
		negoper = this.getMonsterDef().getNegotiation();
	}

	public MonsterDef getMonsterDef() { return ((MonsterDef)(Globals.BESTIARY.get(this.getName()))); }

	public boolean isRanged()           { return this.getMonsterDef().getRanged(); }
	public int     getBaseAttack()      { return this.getMonsterDef().getAttack(); }
	public int     getBaseDefend()      { return this.getMonsterDef().getDefend(); }
	public int     getBaseSpeed()       { return this.getMonsterDef().getSpeed(); }
	public int     getBaseMobility()    { return this.getMonsterDef().getMobility(); }
	public int     getBaseResistance()  { return this.getMonsterDef().getResistance(); }
	public int     getBaseNegotiation() { return this.getMonsterDef().getNegotiation(); }
	public Effect  getEffect()          { return this.getMonsterDef().getEffect(); }
	public int     getBaseEffectPer()   { return this.getMonsterDef().getEffectPer(); }
	public boolean isEffectRanged()     { return this.getMonsterDef().getEffectRanged(); }
	public int     getExpGrant()        { return this.getMonsterDef().getExpGrant(); }
	public int     getSoundNumber()     { return this.getMonsterDef().getSoundNumber(); }
	public Image   getImageNormal()     { return this.getMonsterDef().getImgNormal(); }
	public Image   getImageAttack()     { return this.getMonsterDef().getImgAttack(); }
	public Image   getImageEffect()     { return this.getMonsterDef().getImgEffect(); }

	public int getMobility()    { return mobiper; }
	public int getEffectPer()   { return specper; }
	public int getNegotiation() { return negoper; }

	public void setMobility(int i)    { mobiper = Math.max(Math.min(i, 100), 0); }
	public void setEffectPer(int i)   { specper = (this.getMonsterDef().getEffectPer() == 0 ? 0 : Math.max(Math.min(i, 100), 0)); }
	public void setNegotiation(int i) { negoper = Math.max(Math.min(i, 100), 0); }

	public int getAttackValue() { return Globals.getDiceValue(this.getMonsterDef().getMaxDamage()); }
	public int getDefendValue() { return defend; }

	public MP3Clip getAttackSound() { return Globals.CURR_DUNGEON.getMonsterSound(this.getSoundNumber()); }

	public int getNegotiationAmount() { return Math.max(Math.max(this.getAttackRank(), this.getDefendRank()), 1); }

	public void adjustAttack(int delta)
	{
		if(delta < 0)
		{
			this.attack = Math.max(this.attack + delta, 0);
			this.attack = Math.max(this.attack, this.getMonsterDef().getAttack() - Globals.MAXIMUM_CHANGE);
		}
		else
		{
			this.attack = Math.min(this.attack + delta, this.getMonsterDef().getAttack() + Globals.MAXIMUM_CHANGE);
		}
	}

	public void adjustDefend(int delta)
	{
		if(delta < 0)
		{
			defend = Math.max(defend + delta, 0);
			defend = Math.max(defend, this.getMonsterDef().getDefend() - Globals.MAXIMUM_CHANGE);
		}
		else
		{
			defend = Math.min(defend + delta, this.getMonsterDef().getDefend() + Globals.MAXIMUM_CHANGE);
		}
	}
}