package com.dreamcodex.todr.object;

import java.awt.Color;
import java.util.StringTokenizer;

public class Effect
{
	protected String name;
	protected String effect;
	protected char   target;
	protected char   polarity;
	protected int    power;
	protected int    projectileType;
	protected Color  projectileColor;

	public Effect(String name, String effect, char target, char polarity, int power, int projType, Color projColor)
	{
		this.name            = new String(name);
		this.effect          = new String(effect);
		this.target          = target;
		this.polarity        = polarity;
		this.power           = power;
		this.projectileType  = projType;
		this.projectileColor = projColor;
	}

	public Effect(String name, String effect, char target, char polarity, int power)
	{
		this(name, effect, target, polarity, power, Globals.PROJECTILE_NONE, Globals.PROJCLR_OUTLINE);
	}

	public Effect(Effect fx)
	{
		this.name            = fx.getName();
		this.effect          = fx.getEffect();
		this.target          = fx.getTarget();
		this.polarity        = fx.getPolarity();
		this.power           = fx.getPower();
		this.projectileType  = fx.getProjectileType();
		this.projectileColor = fx.getProjectileColor();
	}

	public String getName()            { return name; }
	public String getEffect()          { return effect; }
	public char   getTarget()          { return target; }
	public char   getPolarity()        { return polarity; }
	public int    getPower()           { return power; }
	public int    getProjectileType()  { return projectileType; }
	public Color  getProjectileColor() { return projectileColor; }

	public boolean isPositive() { return polarity == Globals.FX_POLAR_INCREASE; }
	public boolean isNegative() { return polarity == Globals.FX_POLAR_DECREASE; }
	public boolean isNeutral()  { return polarity == Globals.FX_POLAR_NONE; }

	public void setName(String s)             { name = s; }
	public void setEffect(String s)           { effect = s; }
	public void setTarget(char c)             { target = c; }
	public void setPolarity(char c)           { polarity = c; }
	public void setPower(int i)               { power = i; }
	public void setProjectileType(int i)      { projectileType = i; }
	public void setProjectileColor(Color clr) { projectileColor = clr; }

	public String getSaveString()
	{
		if(this == Globals.NO_EFFECT)
		{
			return Globals.NO_EFFECT_KEY;
		}
		else
		{
			StringBuffer sb = new StringBuffer();
			sb.append(name + "|");
			sb.append(effect + "|");
			sb.append(polarity + "|");
			sb.append(target + "|");
			sb.append(power);
			if(projectileType != Globals.PROJECTILE_NONE)
			{
				sb.append("|" + projectileType + "|");
				sb.append(Integer.toHexString(projectileColor.getRed()) + Integer.toHexString(projectileColor.getGreen()) + Integer.toHexString(projectileColor.getBlue()));
			}
			return sb.toString();
		}
	}

	public static Effect createFromString(String strp)
	{
		if(strp.equals(Globals.NO_EFFECT_KEY))
		{
			return Globals.NO_EFFECT;
		}
		else
		{
			StringTokenizer stParse = new StringTokenizer(strp, "|", false);
			String name     = stParse.nextToken();
			String effect   = stParse.nextToken();
			char   polarity = stParse.nextToken().charAt(0);
			char   target   = stParse.nextToken().charAt(0);
			int    power    = Integer.parseInt(stParse.nextToken());
			if(stParse.hasMoreTokens())
			{
				int    projtype = Integer.parseInt(stParse.nextToken());
				Color  projclr  = new Color(Integer.parseInt(stParse.nextToken(), 16));
				return new Effect(name, effect, target, polarity, power, projtype, projclr);
			}
			return new Effect(name, effect, target, polarity, power);
		}
	}
}