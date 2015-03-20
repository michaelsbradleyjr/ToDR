package com.dreamcodex.todr.object;

import java.awt.Image;
import java.util.Hashtable;

import com.dreamcodex.todr.object.Globals;
import com.dreamcodex.todr.object.Room;
import com.dreamcodex.util.Coord;

/** Board
  * Class containing the game board layout and attributes
  *
  * @author Howard Kistler
  */

public class Board
{
//  Instance Variables ----------------------------------------------------------------------/

	private Coord     size;
	private int[][]   layout;
	private int[][]   mapped;
	private Hashtable<String, Room> rooms;
	private String    name;
	private boolean   mapfound;
	private boolean   bGraphics;
	private String    graphicname;
	private Image[]   graphics;
	private boolean   openCeiling;
	private boolean   openFloor;

//  Constructors ----------------------------------------------------------------------------/

	public Board(String name, int[][] layout, int[][] mapped, Hashtable<String, Room> rooms, boolean mapfound, boolean hasLvlgrfx, String grfxname, Image[] lvlgrfx, boolean openc, boolean openf)
	{
		size          = new Coord(layout[0].length, layout.length);
		this.name     = name;
		this.layout   = layout;
		this.rooms    = new Hashtable<String, Room>(rooms);
		this.mapfound = mapfound;
		if(mapped.length < 1)
		{
			this.mapped = new int[layout.length][layout[0].length];
			for(int y = 0; y < layout.length; y++)
			{
				for(int x = 0; x < layout[0].length; x++)
				{
					this.mapped[y][x]  = Globals.MAPPED_NONE;
				}
			}
		}
		else
		{
			this.mapped = mapped;
		}
		bGraphics   = hasLvlgrfx;
		graphicname = grfxname;
		graphics    = lvlgrfx;
		openCeiling = openc;
		openFloor   = openf;
	}

	public Board(String name, int[][] layout, int[][] mapped, Hashtable<String, Room> rooms, boolean mapfound, boolean hasLvlgrfx, String grfxname, Image[] lvlgrfx)
	{
		this(name, layout, mapped, rooms, mapfound, hasLvlgrfx, grfxname, lvlgrfx, false, false);
	}

	public Board(String name, int[][] layout, int[][] mapped, Hashtable<String, Room> rooms, boolean mapfound, boolean hasLvlgrfx, String grfxname)
	{
		this(name, layout, mapped, rooms, mapfound, hasLvlgrfx, grfxname, (Image[])null);
	}

	public Board(String name, int[][] layout, int[][] mapped, Hashtable<String, Room> rooms, boolean mapfound)
	{
		this(name, layout, mapped, rooms, mapfound, false, (String)null, (Image[])null);
	}

	public Board(String name, int[][] layout, int[][] mapped, Hashtable<String, Room> rooms)
	{
		this(name, layout, mapped, rooms, false);
	}

	public Board(String name, int[][] layout, Hashtable<String, Room> rooms)
	{
		this(name, layout, new int[0][0], rooms);
	}

	public Board(String name, int[][] layout)
	{
		this(name, layout, new Hashtable<String, Room>());
	}

	public Board(Board board)
	{
		this(board.getName(), board.getLayout(), board.getMapped(), board.getRooms(), board.isMapFound());
		if(board.hasLevelGraphics())
		{
			this.setHasLevelGraphics(board.hasLevelGraphics());
			this.setLevelGraphicName(board.getLevelGraphicName());
			this.setLevelGraphics(board.getLevelGraphics());
			this.setHasOpenCeiling(board.hasOpenCeiling());
			this.setHasOpenFloor(board.hasOpenFloor());
		}
	}

//  Accessor Methods ------------------------------------------------------------------------/

	public String    getName()      { return name; }
	public Coord     getSize()      { return size; }
	public int       getWidth()     { return size.getX(); }
	public int       getHeight()    { return size.getY(); }
	public int[][]   getLayout()    { return layout; }
	public Hashtable<String, Room> getRooms()     { return rooms; }
	public int       getRoomCount() { return rooms.size(); }
	public int[][]   getMapped()    { return mapped; }
	public boolean   isMapFound()   { return mapfound; }

	public boolean hasLevelGraphics() { return bGraphics; }
	public boolean hasOpenCeiling()   { return openCeiling; }
	public boolean hasOpenFloor()     { return openFloor; }

	public void setHasLevelGraphics(boolean b) { bGraphics = b; }
	public void setHasOpenCeiling(boolean b)   { openCeiling = b; }
	public void setHasOpenFloor(boolean b)     { openFloor = b; }

	public String getLevelGraphicName()         { return graphicname; }
	public void   setLevelGraphicName(String s) { graphicname = new String(s); }

	public Image[] getLevelGraphics()             { return graphics; }
	public void    setLevelGraphics(Image[] imgs) { graphics = imgs; }

	public Image getLevelGraphic(int index)            { return graphics[index]; }
	public void  setLevelGraphic(int index, Image img) { graphics[index] = img; }

	public int getLayoutType(int x, int y)
	{
		return layout[y][x];
	}

	public Room getRoom(String roomKey)
	{
		if(rooms.containsKey(roomKey))
		{
			return rooms.get(roomKey);
		}
		else
		{
			return (Room)null;
		}
	}

	public Room getRoom(int x, int y)
	{
		if(rooms.containsKey(Globals.getRoomKey(x, y)))
		{
			return rooms.get(Globals.getRoomKey(x, y));
		}
		else
		{
			return (Room)null;
		}
	}

	public boolean isSeen(int x, int y)        { return (mapped[y][x] == Globals.MAPPED_SEEN); }
	public boolean isMapped(int x, int y)      { return (mapped[y][x] == Globals.MAPPED_MAPPED); }
	public boolean isVisited(int x, int y)     { return (mapped[y][x] == Globals.MAPPED_VISITED); }
	public boolean isAwareOfTile(int x, int y) { return (mapped[y][x] != Globals.MAPPED_NONE); }

	public void setMappedState(int x, int y, int state) { mapped[y][x] = state; }
	public void setSeen(int x, int y)      { setMappedState(x, y, Globals.MAPPED_SEEN); }
	public void setMapped(int x, int y)    { setMappedState(x, y, Globals.MAPPED_MAPPED); }
	public void setVisited(int x, int y)   { setMappedState(x, y, Globals.MAPPED_VISITED); }
	public void setMapFound(boolean found) { mapfound = found; }

	public void setConditionalMappedState(int x, int y, int state)
	{
		if(state == Globals.MAPPED_MAPPED && mapped[y][x] == Globals.MAPPED_VISITED)
		{
			// do not set, as proposed condition is "lesser" than current condition
		}
		else if(state == Globals.MAPPED_SEEN && (mapped[y][x] == Globals.MAPPED_VISITED || mapped[y][x] == Globals.MAPPED_MAPPED))
		{
			// do not set, as proposed condition is "lesser" than current condition
		}
		else
		{
			mapped[y][x] = state;
		}
	}
	public void setConditionalSeen(int x, int y)    { setConditionalMappedState(x, y, Globals.MAPPED_SEEN); }
	public void setConditionalMapped(int x, int y)  { setConditionalMappedState(x, y, Globals.MAPPED_MAPPED); }
	public void setConditionalVisited(int x, int y) { setConditionalMappedState(x, y, Globals.MAPPED_VISITED); }

	public void setName(String s) { name = new String(s); }

//  Class Methods ---------------------------------------------------------------------------/

	public int getGridValue(int x, int y) { if(x < 0 || y < 0 || x >= size.getX() || y >= size.getY()) { return 0; } else { return layout[y][x]; } }
	public int getGridValue(Coord cp)     { return getGridValue(cp.getX(), cp.getY()); }

	public void setGridValue(int x, int y, int v) { layout[y][x] = v; }
	public void setGridValue(Coord cp, int v)     { setGridValue(cp.getX(), cp.getY(), v); }

	public boolean hasFountain(int x, int y) { return ((layout[y][x] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN); }
	public boolean hasFountain(Coord cp)     { return hasFountain(cp.getX(), cp.getY()); }

	public void addRoom(Room room, int x, int y)
	{
		if(rooms.containsKey(Globals.getRoomKey(x, y)))
		{
			rooms.remove(Globals.getRoomKey(x, y));
		}
		rooms.put(Globals.getRoomKey(x, y), room);
	}

	public void setAllMappedStates(int state)
	{
		for(int y = 0; y < layout.length; y++)
		{
			for(int x = 0; x < layout[0].length; x++)
			{
				setMappedState(x, y, state);
			}
		}
	}

	public void setConditionalAllMappedStates(int state)
	{
		for(int y = 0; y < layout.length; y++)
		{
			for(int x = 0; x < layout[0].length; x++)
			{
				setConditionalMappedState(x, y, state);
			}
		}
	}

	public void findMap()
	{
		setConditionalAllMappedStates(Globals.MAPPED_MAPPED);
		setMapFound(true);
	}
}
