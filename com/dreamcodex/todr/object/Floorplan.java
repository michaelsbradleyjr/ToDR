package com.dreamcodex.todr.object;

import com.dreamcodex.util.Coord;

public class Floorplan
{
	private char[][] plan;
	private Coord    cpAreaStart; // this is the beginning of the active "inside" area (where monsters and item are confined)
	private Coord    cpAreaEnd;   // this is the end of the active "inside" area
	private int[][]  layout;      // holds volatile room layout, changes based on processing of plan template against current room settings

	public Floorplan(char[][] plan, Coord cpStart, Coord cpEnd)
	{
		this.plan   = plan;
		cpAreaStart = new Coord(cpStart);
		cpAreaEnd   = new Coord(cpEnd);
		layout      = new int[plan.length][plan[0].length];
	}

	public char[][]  getPlan()       { return plan; }
	public int       getHeight()     { return plan.length; }
	public int       getWidth()      { return plan[0].length; }
	public Coord     getAreaStart()  { return cpAreaStart; }
	public int       getAreaStartX() { return cpAreaStart.getX(); }
	public int       getAreaStartY() { return cpAreaStart.getY(); }
	public Coord     getAreaEnd()    { return cpAreaEnd; }
	public int       getAreaEndX()   { return cpAreaEnd.getX(); }
	public int       getAreaEndY()   { return cpAreaEnd.getY(); }
	public int       getAreaXPos()   { return ((cpAreaEnd.getX() - cpAreaStart.getX()) / 2) + 1; } // number of valid X positions inside room for objects (objects are 2 units wide)
	public int       getAreaYPos()   { return ((cpAreaEnd.getY() - cpAreaStart.getY()) / 2) + 1; } // number of valid Y positions inside room for objects (objects are 2 units tall)
	public int[][]   getLayout()     { return layout; }

	public char getPlanChar(int x, int y) { return plan[y][x]; }

	public int getLayoutType(int x, int y)
	{
		if((y < 0) || (x < 0) || (y >= layout.length) || (x >= layout[0].length))
		{
			return Globals.ROOM_OFFMAP;
		}
		else
		{
			return layout[y][x];
		}
	}

	public void setLayoutType(int x, int y, int i)
	{
		if((y < 0) || (x < 0) || (y >= layout.length) || (x >= layout[0].length))
		{
			// off map, ignore
		}
		else
		{
			layout[y][x] = i;
		}
	}
}