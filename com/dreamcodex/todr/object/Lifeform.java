package com.dreamcodex.todr.object;

import java.awt.Color;
import java.awt.Image;

import com.dreamcodex.util.Coord;
import com.dreamcodex.util.MP3Clip;

public class Lifeform
{
	protected int type;
	protected String name;
	protected int level;
	protected int experience;
	protected int hitPoints;
	protected int wounds;
	protected int attack;
	protected int defend;
	protected int speed;
	protected int currspeed;
	protected int resistance;

	protected Coord location;

	protected Image imgNormal;
	protected Image imgAttack;
	protected Image imgEffect;

	protected int actionPoints;
	protected int renderMode;

	protected int   projectileType;
	protected Color projectileColor;

	public Lifeform(int type, String name, int lvl, int exp, int hp, int wnd, int att, int def, int spd, int res)
	{
		this.type        = type;
		this.name        = name;
		level            = lvl;
		experience       = exp;
		hitPoints        = hp;
		wounds           = wnd;
		attack           = att;
		defend           = def;
		speed            = spd;
		currspeed        = spd;
		resistance       = res;
		location         = new Coord(Globals.CP_INVALID_LOC);
		actionPoints     = speed;
		renderMode       = (hitPoints > wounds ? Globals.RENDER_MODE_NORMAL : Globals.RENDER_MODE_DEAD);
		projectileType   = Globals.PROJECTILE_NONE;
		projectileColor  = Globals.PROJCLR_OUTLINE;
	}

	public Lifeform(int type, String name, int lvl, int exp, int hp, int att, int def, int spd, int res)
	{
		this(type, name, lvl, exp, hp, 0, att, def, spd, res);
	}

	public Lifeform(int type, String name, int lvl, int hp)
	{
		this(type, name, lvl, 0, hp, 0, 0, 0, 0);
	}

	public Lifeform(int type, String name, int lvl)
	{
		this(type, name, lvl, 0);
	}

	public Lifeform(int type, String name)
	{
		this(type, name, 0);
	}

	public Lifeform(int type)
	{
		this(type, "[default]");
	}

	public Lifeform()
	{
		this(Globals.LIFEFORM_DEFAULT);
	}

	public int     getType()              { return type; }
	public String  getName()              { return name; }
	public int     getExperience()        { return experience; }
	public int     getHitPoints()         { return hitPoints; }
	public int     getWounds()            { return wounds; }
	public int     getLevel()             { return level; }
	public int     getAttack()            { return attack; }
	public int     getDefend()            { return defend; }
	public int     getSpeed()             { return speed; }
	public int     getCurrSpeed()         { return currspeed; }
	public int     getResistance()        { return resistance; }
	public boolean isAlive()              { return (hitPoints > wounds); }
	public Coord   getLocation()          { return location; }
	public int     getActionPoints()      { return actionPoints; }
	public int     getRenderMode()        { return renderMode; }
	public int     getCurrentHitPoints()  { return hitPoints - wounds; }
	public int     getProjectileType()    { return projectileType; }
	public Color   getProjectileColor()   { return projectileColor; }

	public Image getImageNormal() { return imgNormal; }
	public Image getImageAttack() { return imgAttack; }
	public Image getImageEffect() { return imgEffect; }
	public Image getImageDead()   { return Globals.CURR_DUNGEON.getImageDeadMonster(); }

	public Image getRenderModeImage()
	{
		if(renderMode == Globals.RENDER_MODE_NORMAL)
		{
			return getImageNormal();
		}
		else if(renderMode == Globals.RENDER_MODE_ATTACK)
		{
			return getImageAttack();
		}
		else if(renderMode == Globals.RENDER_MODE_EFFECT)
		{
			return getImageEffect();
		}
		else if(renderMode == Globals.RENDER_MODE_DEAD)
		{
			return getImageDead();
		}
		return getImageNormal();
	}

	public int getAttackRank() { return attack; }
	public int getDefendRank() { return defend; }

	public int getAttackValue() { return Globals.getRandomValue(attack); }
	public int getDefendValue() { return defend; }

	public MP3Clip getAttackSound() { return (MP3Clip)null; }

	public void setType(int i)                       { type = i; }
	public void setName(String s)                    { name = s; }
	public void setExperience(int i)                 { experience = i; }
	public void setHitPoints(int i)                  { hitPoints = i; }
	public void setWounds(int i)                     { wounds = i; }
	public void setLevel(int i)                      { level = i; }
	public void setAttack(int i)                     { attack = i; }
	public void setDefend(int i)                     { defend = i; }
	public void setSpeed(int i)                      { currspeed = Math.min(currspeed + (i - speed), 9); speed = Math.min(i, 9); }
	public void setCurrSpeed(int i)                  { currspeed = Math.min(i, 9); }
	public void setResistance(int i)                 { resistance = Math.max(0, Math.min(100, i)); }
	public void setLocation(Coord cp)                { location.setLocation(cp); }
	public void setLocation(int x, int y)            { location.setLocation(x, y); }
	public void setActionPoints(int i)               { actionPoints = i; }
	public void setRenderMode(int i)                 { renderMode = i; }

	public void setImageNormal(Image img)  { imgNormal  = img; }
	public void setImageAttack(Image img)  { imgAttack  = img; }
	public void setImageEffect(Image img) { imgEffect = img; }

	/* action point methods */
	public void expendActionPoints(int delta)
	{
		if(delta == Globals.ACTION_COST_ALL)
		{
			actionPoints = 0;
		}
		else
		{
			actionPoints -= delta;
		}
		if(actionPoints < 0) { actionPoints = 0; }
	}
	public void    gainActionPoints(int delta) { actionPoints += delta; }
	public void    resetActionPoints()         { actionPoints = getCurrSpeed(); }
	public boolean hasActionsLeft()            { return actionPoints > 0; }
	public boolean canPerformAction(int cost)  { return actionPoints >= cost; }
	public boolean canPerformMove()            { return (actionPoints > 1) || (getCurrSpeed() == 1); }

	public void adjustHitpoints(int amount)
	{
		hitPoints += amount;
		if(hitPoints > Globals.MAX_PLAYER_HP) { hitPoints = Globals.MAX_PLAYER_HP; }
		if(hitPoints < 1)                     { hitPoints = 1; }
		if((hitPoints > wounds) && (renderMode == Globals.RENDER_MODE_DEAD))
		{
			renderMode = Globals.RENDER_MODE_NORMAL;
		}
		else if(hitPoints <= wounds)
		{
			renderMode = Globals.RENDER_MODE_DEAD;
		}
	}

	public void adjustWounds(int amount)
	{
		wounds += amount;
		if(wounds > (hitPoints * 2)) { wounds = (hitPoints * 2); }
		if(wounds < 0)               { wounds = 0; }
		if((hitPoints > wounds) && (renderMode == Globals.RENDER_MODE_DEAD))
		{
			renderMode = Globals.RENDER_MODE_NORMAL;
		}
		else if(hitPoints <= wounds)
		{
			renderMode = Globals.RENDER_MODE_DEAD;
		}
	}

	public void alterResistance(int delta)
	{
		resistance = Math.max(Math.min(resistance + delta, 100), 0);
	}

	public void resetSpeed()
	{
		currspeed = speed;
	}
}