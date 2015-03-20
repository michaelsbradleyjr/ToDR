package com.dreamcodex.todr.object;

import java.util.StringTokenizer;
import java.util.Vector;

import com.dreamcodex.util.Coord;

public class Party
{
	private Player[] members;
	private Coord    location;
	private int      depth;
	private int      facing;
	private int      currency;
	private int      rations;
	private int      turnsElapsed;
	private int      roundsElapsed;
	private int      ratnCnsmptRate = 20;
	private int      ratnCnsmptCounter = 0;
	private int      healCnsmptRate = 20;
	private int      healCnsmptCounter = 0;
	private int      wanderingOdds;
	private int      shopCapBonus;
	private boolean  gameCompleted;

	public Party(Player[] members, Coord location, int depth, int facing, int currency, int rations, int turns, int rounds, int ratnint, int ratnctr, int healint, int healctr, int wander, int shopbonus, boolean complete)
	{
		this.members      = members;
		this.location     = new Coord(location);
		this.depth        = depth;
		this.facing       = facing;
		this.currency     = currency;
		this.rations      = rations;
		wanderingOdds     = wander;
		turnsElapsed      = turns;
		roundsElapsed     = rounds;
		ratnCnsmptRate    = ratnint;
		ratnCnsmptCounter = ratnctr;
		healCnsmptRate    = healint;
		healCnsmptCounter = healctr;
		shopCapBonus      = shopbonus;
		gameCompleted     = complete;
	}

	public Party(Player[] members, Coord location, int depth, int facing, int currency, int rations)
	{
		this(members, location, depth, facing, currency, rations, 0, 0, Globals.DEF_INTERVAL_RATIONS, 0, Globals.DEF_INTERVAL_HEALING, 0, Globals.CURR_DUNGEON.getWanderOdds(), 0, false);
	}

	public Party(int partysize, Coord location, int depth, int facing, int currency, int rations)
	{
		this(new Player[partysize], location, depth, facing, currency, rations);
	}

	public Party(int partysize)
	{
		this(partysize, Globals.CP_VALID_START, 0, Globals.NORTH, 0, 0);
	}

	public Player[] getPlayers()         { return members; }
	public Player   getPlayer(int i)     { if(i >= 0 && i <= members.length) { return members[i];} else { return (Player)null; } }
	public Coord    getLocation()        { return location; }
	public int      getLocationX()       { return location.getX(); }
	public int      getLocationY()       { return location.getY(); }
	public int      getDepth()           { return depth; }
	public int      getFacing()          { return facing; }
	public int      getCurrency()        { return currency; }
	public int      getRations()         { return rations; }
	public int      getTurnsElapsed()    { return turnsElapsed; }
	public int      getRoundsElapsed()   { return roundsElapsed; }
	public int      getRationsInterval() { return ratnCnsmptRate; }
	public int      getRationsCounter()  { return ratnCnsmptCounter; }
	public int      getHealingInterval() { return healCnsmptRate; }
	public int      getHealingCounter()  { return healCnsmptCounter; }
	public int      getWanderingOdds()   { return wanderingOdds; }
	public int      getShopCapBonus()    { return shopCapBonus; }
	public boolean  getGameCompleted()   { return gameCompleted; }

	public void setPlayers(Player[] p)      { members = p; }
	public void setPlayer(Player p, int i)  { if(i >= 0 && i <= members.length) { members[i] = p; } }
	public void setLocation(Coord cp)       { location.setLocation(cp); }
	public void setLocation(int x, int y)   { location.setLocation(x, y); }
	public void setDepth(int i)             { depth = i; }
	public void setFacing(int i)            { facing = i; }
	public void setCurrency(int i)          { currency = i; }
	public void setRations(int i)           { rations = i; }
	public void setTurnsElapsed(int i)      { turnsElapsed = i; }
	public void setRoundsElapsed(int i)     { roundsElapsed = i; }
	public void setRationsInterval(int i)   { ratnCnsmptRate = i; adjustRationInterval(0); }
	public void setRationsCounter(int i)    { ratnCnsmptCounter = i; }
	public void setHealingInterval(int i)   { healCnsmptRate = i; adjustHealingInterval(0); }
	public void setHealingCounter(int i)    { healCnsmptCounter = i; }
	public void setWanderingOdds(int i)     { wanderingOdds = i; adjustWanderingOdds(0); }
	public void setShopCapBonus(int i)      { shopCapBonus = i; }
	public void setGameCompleted(boolean b) { gameCompleted = b; }

	public int getSize() { return members.length; }

	public boolean adjustCurrency(int amount, boolean test)
	{
		if(test)
		{
			int newCurrency = currency + amount;
			if(newCurrency < 0)
			{
				return false;
			}
			else
			{
				currency += amount;
			}
		}
		else
		{
			currency += amount;
		}
		return true;
	}

	public boolean adjustCurrency(int amount)
	{
		return adjustCurrency(amount, true);
	}

	public boolean spendCurrency(int amount)
	{
		return adjustCurrency(-(Math.abs(amount)));
	}

	public boolean gainCurrency(int amount)
	{
		return adjustCurrency(Math.abs(amount));
	}

	public void adjustRations(int amount)
	{
		rations += amount;
		if(rations < 0)
		{
			rations = 0;
		}
		else if(rations > Globals.RATIONS_MAX_ABS)
		{
			rations = Globals.RATIONS_MAX_ABS;
		}
	}

	public void adjustRationInterval(int amount)
	{
		ratnCnsmptRate += amount;
		if(ratnCnsmptRate < 1)
		{
			ratnCnsmptRate = 1;
		}
	}

	public void adjustHealingInterval(int amount)
	{
		healCnsmptRate += amount;
		if(healCnsmptRate < 1)
		{
			healCnsmptRate = 1;
		}
	}

	public void adjustWanderingOdds(int amount)
	{
		wanderingOdds += amount;
		if(wanderingOdds < Globals.MIN_WANDERING_CHANCE)
		{
			wanderingOdds = Globals.MIN_WANDERING_CHANCE;
		}
		if(wanderingOdds > Globals.MAX_WANDERING_CHANCE)
		{
			wanderingOdds = Globals.MAX_WANDERING_CHANCE;
		}
	}

	public void elapseRound()
	{
		this.roundsElapsed++;
		while(roundsElapsed >= Globals.ROUNDS_PER_TURN)
		{
			this.roundsElapsed -= Globals.ROUNDS_PER_TURN;
			this.turnsElapsed++;
		}
		healCnsmptCounter++;
		while(healCnsmptCounter >= healCnsmptRate)
		{
			if(rations > 0)
			{
				// heal party members
				for(int i = 0; i < this.getSize(); i++)
				{
					int healingAmount = Globals.rnd.nextInt(Globals.DEF_HEALING_AMT_MAX - Globals.DEF_HEALING_AMT_MIN) + Globals.DEF_HEALING_AMT_MIN;
					this.getPlayer(i).adjustWounds(-healingAmount);
				}
			}
			healCnsmptCounter -= healCnsmptRate;
		}
		ratnCnsmptCounter++;
		while(ratnCnsmptCounter >= ratnCnsmptRate)
		{
			adjustRations(-(this.getSize()));
			ratnCnsmptCounter -= ratnCnsmptRate;
		}
		// NOTE: could also activate turn-based party member effects here
		//       but most of these things, like checking for random encounters,
		//       will be done in the main class
	}

	public void swapPositions(int player1, int player2)
	{
		if(player1 != player2)
		{
			Player pTemp = members[player2];
			members[player2] = members[player1];
			members[player1] = pTemp;
			pTemp = null;
		}
	}

	public String getSaveString()
	{
		StringBuffer sbData = new StringBuffer();
		sbData.append(this.getLocationX() + "|");
		sbData.append(this.getLocationY() + "|");
		sbData.append(this.getDepth() + "|");
		sbData.append(this.getFacing() + "|");
		sbData.append(this.getCurrency() + "|");
		sbData.append(this.getRations() + "|");
		sbData.append(this.getTurnsElapsed() + "|");
		sbData.append(this.getRoundsElapsed() + "|");
		sbData.append(this.getRationsInterval() + "|");
		sbData.append(this.getRationsCounter() + "|");
		sbData.append(this.getHealingInterval() + "|");
		sbData.append(this.getHealingCounter() + "|");
		sbData.append(this.getWanderingOdds() + "|");
		sbData.append(this.getShopCapBonus() + "|");
		sbData.append((this.getGameCompleted() ? "Y" : "N"));
		return sbData.toString();
	}

	public static Party createFromString(String strp)
	{
		StringTokenizer stParse = new StringTokenizer(strp, "|", false);
		int     locX      = Integer.parseInt(stParse.nextToken());
		int     locY      = Integer.parseInt(stParse.nextToken());
		int     depth     = Integer.parseInt(stParse.nextToken());
		int     facing    = Integer.parseInt(stParse.nextToken());
		int     currency  = Integer.parseInt(stParse.nextToken());
		int     rations   = Integer.parseInt(stParse.nextToken());
		int     turns     = Integer.parseInt(stParse.nextToken());
		int     rounds    = Integer.parseInt(stParse.nextToken());
		int     rationint = Integer.parseInt(stParse.nextToken());
		int     rationcnt = Integer.parseInt(stParse.nextToken());
		int     healint   = Integer.parseInt(stParse.nextToken());
		int     healcnt   = Integer.parseInt(stParse.nextToken());
		int     wander    = Integer.parseInt(stParse.nextToken());
		int     shopbonus = Integer.parseInt(stParse.nextToken());
		boolean completed = (stParse.nextToken().equals("Y"));
		return new Party(new Player[0], new Coord(locX, locY), depth, facing, currency, rations, turns, rounds, rationint, rationcnt, healint, healcnt, wander, shopbonus, completed);
	}
}
