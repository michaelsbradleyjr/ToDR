package com.dreamcodex.todr.util;

import java.awt.Image;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import com.dreamcodex.todr.object.Ammo;
import com.dreamcodex.todr.object.Armor;
import com.dreamcodex.todr.object.BaseItem;
import com.dreamcodex.todr.object.Board;
import com.dreamcodex.todr.object.Chest;
import com.dreamcodex.todr.object.Dungeon;
import com.dreamcodex.todr.object.Globals;
import com.dreamcodex.todr.object.Item;
import com.dreamcodex.todr.object.ItemContainer;
import com.dreamcodex.todr.object.Monster;
import com.dreamcodex.todr.object.MonsterDef;
import com.dreamcodex.todr.object.Party;
import com.dreamcodex.todr.object.Quest;
import com.dreamcodex.todr.object.QuestItem;
import com.dreamcodex.todr.object.Room;
import com.dreamcodex.todr.object.Vault;
import com.dreamcodex.todr.object.Weapon;
import com.dreamcodex.todr.util.ObjectParser;
import com.dreamcodex.util.Coord;

public class LevelMaker
{
	private static Random rnd = new Random(System.currentTimeMillis());

	private static Vector<String> vcUniqueMonsters = new Vector<String>();
	private static Vector<String> vcUniqueItems    = new Vector<String>();

	public LevelMaker()
	{
	}

	public static Board makeLevel(String levelName, int levelX, int levelY, int roomCount, int levelDepth, int difficulty, int partySize, int monsterOdds, int itemOdds, int chestOdds, int trapOdds, int vaultOdds, Coord[] cpStairsUp, boolean hasDownStairs, boolean hasLevelGraphics, String levelGraphicsString)
	{
		int[][] plan = new int[levelY][levelX];
		int[][] mapd = new int[levelY][levelX];
		Hashtable<String, Room> rooms = new Hashtable<String, Room>(roomCount);
		boolean designingLevel = true;
		String  levelGraphicName = "";
		boolean hasOpenCeiling = false;
		boolean hasOpenFloor   = false;
		if(levelGraphicsString != null && levelGraphicsString.indexOf("|") > -1)
		{
			levelGraphicName = levelGraphicsString.substring(0, levelGraphicsString.indexOf("|"));
			hasOpenCeiling   = levelGraphicsString.substring(levelGraphicsString.indexOf("|") + 1, levelGraphicsString.indexOf("|") + 2).equals("Y");
			hasOpenFloor     = levelGraphicsString.substring(levelGraphicsString.indexOf("|") + 2, levelGraphicsString.indexOf("|") + 3).equals("Y");
		}
		while(designingLevel)
		{
			plan = new int[levelY][levelX];
			mapd = new int[levelY][levelX];
			rooms = new Hashtable<String, Room>(roomCount);
			// draw outer border
			for(int y = 0; y < levelY; y++)
			{
				for(int x = 0; x < levelX; x++)
				{
					if(y == 0 || y == (levelY - 1) || x == 0 || x == (levelX - 1))
					{
						plan[y][x] = -1;
					}
					else
					{
						plan[y][x] = 0;
					}
				}
			}
			// plot rooms
			plotRooms(plan, rooms, roomCount, levelDepth, difficulty, partySize, monsterOdds, itemOdds, chestOdds, trapOdds, vaultOdds, cpStairsUp, hasDownStairs);
			connectRooms(plan);
			cleanUp(plan);
			// test if layout valid
			designingLevel = isLevelInvalid(plan, cpStairsUp[0]);
			// if valid, set map to unexplored
			if(!designingLevel)
			{
				for(int fillY = 0; fillY < levelY; fillY++)
				{
					for(int fillX = 0; fillX < levelX; fillX++)
					{
						mapd[fillY][fillX] = Globals.MAPPED_NONE;
					}
				}
			}
		}
		return new Board(levelName, plan, mapd, rooms, false, hasLevelGraphics, levelGraphicName, (Image[])null, hasOpenCeiling, hasOpenFloor);
	}

	private static void plotRooms(int[][] plan, Hashtable<String, Room> rooms, int roomCount, int depth, int difficulty, int partySize, int monsterOdds, int itemOdds, int chestOdds, int trapOdds, int vaultOdds, Coord[] cpStairsUp, boolean hasDownStairs)
	{
		// adjust variables for difficulty
		monsterOdds = Math.min(Math.max(monsterOdds + Globals.DIFFMOD_MNSTODDS[difficulty], 1), 100);
		itemOdds    = Math.min(Math.max(itemOdds + Globals.DIFFMOD_ITEMODDS[difficulty], 1), 100);
		trapOdds    = Math.min(Math.max(trapOdds + Globals.DIFFMOD_TRAPODDS[difficulty], 1), 100);
		// begin room plotting
		int roomXRange = plan[0].length - 2;
		int roomYRange = plan.length - 2;
		int room  = 0;
		// plot the up stairs to the previous levels (staircases align between levels in this game)
		for(int i = 0; i < cpStairsUp.length; i++)
		{
			room += addRoom(plan, rooms, cpStairsUp[i].getX(), cpStairsUp[i].getY(), depth, difficulty, partySize, Globals.STAIRS_UP, Globals.FEATURE_NONE, 0, 0, 0, 0, false);
		}
		int specnum   = 0;
		int speccount = 0;
		int roommade  = 0;
		if(hasDownStairs)
		{
			// plot 1-2 down staircases (do not count these against room total)
			specnum   = rnd.nextInt(2) + 1;
			speccount = 0;
			while(speccount < specnum)
			{
				roommade = addRoom(plan, rooms, rnd.nextInt(roomXRange) + 1, rnd.nextInt(roomYRange) + 1, depth, difficulty, partySize, Globals.STAIRS_DOWN, Globals.FEATURE_NONE, 0, 0, 0, 0, false);
				speccount += roommade;
			}
		}
		// plot shop if present (do not count these against room total)
		if(Globals.SHOP_LEVELS[depth])
		{
			speccount = 0;
			while(speccount < 1)
			{
				speccount = addRoom(plan, rooms, rnd.nextInt(roomXRange) + 1, rnd.nextInt(roomYRange) + 1, depth, difficulty, partySize, Globals.STAIRS_NONE, Globals.FEATURE_SHOP, 0, 0, 0, 0, false);
			}
		}
		// plot 1-2 hall fountains (do not count these against room total)
		specnum   = rnd.nextInt(2) + 1;
		speccount = 0;
		while(speccount < specnum)
		{
			int plotX = rnd.nextInt(roomXRange) + 1;
			int plotY = rnd.nextInt(roomYRange) + 1;
			if(plotY > 0 && plotY < (plan.length - 1) && plotX > 0 && plotX < (plan[0].length - 1) && plan[plotY][plotX] == 0)
			{
				if(plan[plotY - 1][plotX] < Globals.MAP_FOUNTAIN && plan[plotY + 1][plotX] < Globals.MAP_FOUNTAIN && plan[plotY][plotX - 1] < Globals.MAP_FOUNTAIN && plan[plotY][plotX + 1] < Globals.MAP_FOUNTAIN)
				{
					plan[plotY][plotX] = Globals.MAP_FOUNTAIN;
					speccount += 1;
				}
			}
		}
		// plot 0-2 rooms with fountains
		specnum   = rnd.nextInt(4) - 1;
		speccount = 0;
		while(speccount < specnum && room < roomCount)
		{
			roommade = addRoom(plan, rooms, rnd.nextInt(roomXRange) + 1, rnd.nextInt(roomYRange) + 1, depth, difficulty, partySize, Globals.STAIRS_NONE, Globals.FEATURE_FOUNTAIN, monsterOdds, itemOdds, chestOdds, trapOdds, false);
			room += roommade;
			speccount += roommade;
		}
		// see if there's a vault
		specnum   = (rnd.nextInt(100) < vaultOdds ? 1 : 0);
		speccount = 0;
		while(speccount < specnum && room < roomCount)
		{
			roommade = addRoom(plan, rooms, rnd.nextInt(roomXRange) + 1, rnd.nextInt(roomYRange) + 1, depth, difficulty, partySize, Globals.STAIRS_NONE, Globals.STAIRS_NONE, 0, 0, 0, 0, true);
			room += roommade;
			speccount += roommade;
		}
		// plot 1-2 rooms with statues
		specnum   = rnd.nextInt(2) + 1;
		speccount = 0;
		while(speccount < specnum && room < roomCount)
		{
			roommade = addRoom(plan, rooms, rnd.nextInt(roomXRange) + 1, rnd.nextInt(roomYRange) + 1, depth, difficulty, partySize, Globals.STAIRS_NONE, Globals.FEATURE_STATUE, monsterOdds, itemOdds, chestOdds, trapOdds, false);
			room += roommade;
			speccount += roommade;
		}
		// determine how many more rooms need monsters and items
		int minMonsterRooms  = (int)(roomCount * Globals.ROOM_MONSTER_RATIO);
		int minItemRooms     = (int)(roomCount * Globals.ROOM_ITEM_RATIO);
		int currMonsterRooms = 0;
		int currItemRooms    = 0;
		for(Enumeration e = rooms.keys(); e.hasMoreElements();)
		{
			String roomKey = e.nextElement().toString();
			Room checkRoom = rooms.get(roomKey);
			if(checkRoom.getMonsterCount() > 0)
			{
				currMonsterRooms++;
			}
			if(checkRoom.hasItemContainer() || checkRoom.getItemCount() > 0)
			{
				currItemRooms++;
			}
		}
		// plot rooms
		while(room < roomCount)
		{
			room += addRoom(plan, rooms, rnd.nextInt(roomXRange) + 1, rnd.nextInt(roomYRange) + 1, depth, difficulty, partySize, Globals.STAIRS_NONE, Globals.FEATURE_NONE, (currMonsterRooms < minMonsterRooms ? 100 : monsterOdds), (currItemRooms < minItemRooms ? 100 : itemOdds), chestOdds, trapOdds, false);
			if(currMonsterRooms < minMonsterRooms) { currMonsterRooms++; }
			if(currItemRooms < minItemRooms) { currItemRooms++; }
		}
		// add Maps to rooms
		int mapNum = rnd.nextInt(2) + 1;
		int mapCnt = 0;
		String usedRoom = "";
		while(mapCnt < mapNum)
		{
			boolean mapAssigned = false;
			for(Enumeration e = rooms.keys(); e.hasMoreElements();)
			{
				String roomKey = e.nextElement().toString();
				Room mapRoom = rooms.get(roomKey);
				if(!mapAssigned && !(roomKey.equals(usedRoom)) && (mapRoom.getStairType() == Globals.STAIRS_NONE))
				{
					BaseItem itemReturn = (BaseItem)null;
					if(mapRoom.hasChest())
					{
						mapRoom.getChest().addContentItem(Globals.ITEM_MAP.getInstance());
						mapAssigned = true;
					}
					else if(mapRoom.hasVault())
					{
						mapRoom.getVault().addContentItem(Globals.ITEM_MAP.getInstance());
						mapAssigned = true;
					}
					else
					{
						itemReturn = mapRoom.gainItem(Globals.ITEM_MAP.getInstance());
						mapAssigned = !(itemReturn != null);
					}
					if(mapAssigned)
					{
						usedRoom = roomKey;
						mapCnt++;
					}
				}
			}
		}
	}

	private static int addRoom(int[][] plan, Hashtable<String, Room> rooms, int plotX, int plotY, int depth, int difficulty, int partySize, int stairType, int featureType, int monsterOdds, int itemOdds, int chestOdds, int trapOdds, boolean hasVault)
	{
		// check position is legitimate
		if(plotY > 0 && plotY < (plan.length - 1) && plotX > 0 && plotX < (plan[0].length - 1) && plan[plotY][plotX] == 0)
		{
			// check adjacencies - no rooms allow adjacent on any of the cardinal directions
			if(plan[plotY - 1][plotX] < Globals.MAP_FOUNTAIN && plan[plotY + 1][plotX] < Globals.MAP_FOUNTAIN && plan[plotY][plotX - 1] < Globals.MAP_FOUNTAIN && plan[plotY][plotX + 1] < Globals.MAP_FOUNTAIN)
			{
				// determine if room has monsters, and if so, what kind
				Vector<Monster> vcMonsters = new Vector<Monster>();
				if(monsterOdds > 0)
				{
					if(rnd.nextInt(100) < monsterOdds)
					{
						// room has monsters
						// now determine type (based on level depth) and quantity (defined in MonsterDef as MaxGroup)
						MonsterDef mdef = Globals.getMonsterForDepth(depth, difficulty);
						while(mdef.isUnique() && vcUniqueMonsters.contains(mdef.getName()))
						{
							mdef = Globals.getMonsterForDepth(depth, difficulty);
						}
						int randQuant = Math.max(1, ((mdef.getMaxGroup() - 4) + partySize) + Globals.DIFFMOD_MNSTGRUP[difficulty]);
						int monsterQuant = rnd.nextInt(randQuant) + 1;
						for(int i = 0; i < monsterQuant; i++)
						{
							vcMonsters.add(mdef.getInstance());
						}
						if(mdef.isUnique())
						{
							vcUniqueMonsters.add(mdef.getName());
							if(!(mdef.getMobType().equals(MonsterDef.MOBTYPE_NONE)))
							{
								mdef  = Globals.BESTIARY.get(mdef.getMobType());
								randQuant = Math.max(1, ((mdef.getMaxGroup() - 4) + partySize) + Globals.DIFFMOD_MNSTGRUP[difficulty]);
								monsterQuant = rnd.nextInt(randQuant) + 1;
								for(int i2 = 0; i2 < monsterQuant; i2++)
								{
									vcMonsters.add(mdef.getInstance());
								}
							}
						}
					}
				}
				ItemContainer itmcon = (ItemContainer)null;
				Vector<BaseItem> vcItems = new Vector<BaseItem>();
				if(hasVault)
				{
					int vaultItems = rnd.nextInt((Globals.MAX_VAULT_ITEMS - Globals.MIN_VAULT_ITEMS) + 1) + Globals.MIN_VAULT_ITEMS;
					Vector<BaseItem> treasure = new Vector<BaseItem>(vaultItems);
					boolean hasBad = false;
					boolean okayToAdd = true;
					int itm = 0;
					while(itm < vaultItems)
					{
						okayToAdd = true;
						BaseItem bitem = Globals.getVaultItemForDepth(depth, difficulty);
						while(bitem.isUnique() && vcUniqueItems.contains(bitem.getName()))
						{
							bitem = Globals.getItemForDepth(depth, difficulty);
						}
						if(bitem.getType() == Globals.ITEMTYPE_ITEM && Globals.isBadEffect(((Item)bitem).getEffect()))
						{
							if(hasBad)
							{
								okayToAdd = false;
							}
							else
							{
								hasBad = true;
							}
						}
						if(bitem.getType() != Globals.ITEMTYPE_ITEM)
						{
							for(int v = 0; v < treasure.size(); v++)
							{
								if(bitem.getType() == treasure.elementAt(v).getType())
								{
									okayToAdd = false;
								}
							}
						}
						if(okayToAdd)
						{
							treasure.add(bitem);
							if(bitem.getType() == Globals.ITEMTYPE_WEAPON)
							{
								if(((Weapon)bitem).requiresAmmo())
								{
									treasure.add(Globals.AMMOLIST.get(((Weapon)bitem).getAmmoType()).getInstance());
								}
							}
							if(bitem.isUnique())
							{
								vcUniqueItems.add(bitem.getName());
							}
							itm++;
						}
					}
					StringBuffer sb = new StringBuffer();
					for(int i = 0; i < Globals.VAULT_DIGITS_PER_LEVEL[depth]; i++)
					{
						sb.append("" + (Globals.rnd.nextInt(Globals.VAULT_VALUES_PER_LEVEL[depth]) + 1));
					}
					int code = Integer.parseInt(sb.toString());
					itmcon = new Vault(treasure, code);
				}
				else
				{
					// determine if room has item(s) or chest, and if chest, chest trap if any
					int chestTrap = Globals.TRAP_NONE;
					if(itemOdds > 0)
					{
						if(rnd.nextInt(100) < itemOdds)
						{
							// room has item
							// now determine if it's an item or a chest
							if(rnd.nextInt(100) < chestOdds)
							{
								// it's a chest
								// see if it's trapped
								if(rnd.nextInt(100) < trapOdds)
								{
									// it's trapped
									chestTrap = rnd.nextInt(depth);
								}
								// get items for chest
								int chestItems = rnd.nextInt((Globals.MAX_CHEST_ITEMS - Globals.MIN_CHEST_ITEMS) + 1) + Globals.MIN_CHEST_ITEMS;
								Vector<BaseItem> treasure = new Vector<BaseItem>(chestItems);
								boolean hasBad = false;
								boolean okayToAdd = true;
								int itm = 0;
								while(itm < chestItems)
								{
									okayToAdd = true;
									BaseItem bitem = Globals.getItemForDepth(depth, difficulty);
									while(bitem.isUnique() && vcUniqueItems.contains(bitem.getName()))
									{
										bitem = Globals.getItemForDepth(depth, difficulty);
									}
									if(bitem.getType() == Globals.ITEMTYPE_ITEM && Globals.isBadEffect(((Item)bitem).getEffect()))
									{
										if(hasBad)
										{
											okayToAdd = false;
										}
										else
										{
											hasBad = true;
										}
									}
									if(bitem.getType() != Globals.ITEMTYPE_ITEM)
									{
										for(int v = 0; v < treasure.size(); v++)
										{
											if(bitem.getType() == treasure.elementAt(v).getType())
											{
												okayToAdd = false;
											}
										}
									}
									if(okayToAdd)
									{
										treasure.add(bitem);
										if(bitem.getType() == Globals.ITEMTYPE_WEAPON)
										{
											if(((Weapon)bitem).requiresAmmo())
											{
												treasure.add(Globals.AMMOLIST.get(((Weapon)bitem).getAmmoType()).getInstance());
											}
										}
										if(bitem.isUnique())
										{
											vcUniqueItems.add(bitem.getName());
										}
										itm++;
									}
								}
								if(chestTrap == Globals.TRAP_NONE)
								{
									itmcon = new Chest(treasure);
								}
								else
								{
									itmcon = new Chest(treasure, chestTrap, ((Integer)(Globals.TRAPODDS.elementAt(chestTrap))).intValue());
								}
							}
							else
							{
								// it's an item
								BaseItem bitem = Globals.getItemForDepth(depth, difficulty);
								while(bitem.isUnique() && vcUniqueItems.contains(bitem.getName()))
								{
									bitem = Globals.getItemForDepth(depth, difficulty);
								}
								vcItems.add(bitem);
								if(bitem.getType() == Globals.ITEMTYPE_WEAPON)
								{
									if(((Weapon)bitem).requiresAmmo())
									{
										vcItems.add(Globals.AMMOLIST.get(((Weapon)bitem).getAmmoType()).getInstance());
									}
								}
								if(bitem.isUnique())
								{
									vcUniqueItems.add(bitem.getName());
								}
							}
						}
					}
				}
				// add physical room to board
				plan[plotY][plotX] = Globals.MAP_ROOM;
				rooms.put(Globals.getRoomKey(plotX, plotY), new Room(stairType, featureType, vcMonsters, vcItems, itmcon));
				return 1;
			}
		}
		return 0;
	}

	private static void connectRooms(int[][] plan)
	{
		int[] matchRoomListX = new int[plan.length + plan[0].length];
		int[] matchRoomListY = new int[plan.length + plan[0].length];
		int matchRoomCount = 0;
		int[] matchHallListX = new int[plan.length + plan[0].length];
		int[] matchHallListY = new int[plan.length + plan[0].length];
		int matchHallCount = 0;
		for(int y = 0; y < plan.length; y++)
		{
			for(int x = 0; x < plan[0].length; x++)
			{
				if(plan[y][x] >= Globals.MAP_FOUNTAIN)
				{
					// re-init match vars
					matchRoomListX = new int[plan.length + plan[0].length];
					matchRoomListY = new int[plan.length + plan[0].length];
					matchRoomCount = 0;
					matchHallListX = new int[plan.length + plan[0].length];
					matchHallListY = new int[plan.length + plan[0].length];
					matchHallCount = 0;
					// scan for other rooms along vertical
					for(int v = 0; v < plan.length; v++)
					{
						if(v != y && plan[v][x] >= Globals.MAP_FOUNTAIN)
						{
							// add to room match list
							matchRoomListX[matchRoomCount] = x;
							matchRoomListY[matchRoomCount] = v;
							matchRoomCount++;
						}
						else if(v != y && plan[v][x] == 10)
						{
							// add perpendicular hallway to hall match list
							matchHallListX[matchHallCount] = x;
							matchHallListY[matchHallCount] = v;
							matchHallCount++;
						}
					}
					// scan for other rooms along horizontal
					for(int h = 0; h < plan[0].length; h++)
					{
						if(h != x && plan[y][h] >= Globals.MAP_FOUNTAIN)
						{
							// add to room match list
							matchRoomListX[matchRoomCount] = h;
							matchRoomListY[matchRoomCount] = y;
							matchRoomCount++;
						}
						else if(h != x && plan[y][h] == 5)
						{
							// add perpendicular hallway to hall match list
							matchHallListX[matchHallCount] = h;
							matchHallListY[matchHallCount] = y;
							matchHallCount++;
						}
					}
					// now pick a room from the match list to create a hallway to
					if(matchRoomCount < 1)
					{
						// no room match, look for hallway match
						if(matchHallCount < 1)
						{
							return;
						}
						else
						{
							int whichHall = rnd.nextInt(matchHallCount);
							int hallX = matchHallListX[whichHall];
							int hallY = matchHallListY[whichHall];
							if(hallX == x)
							{
								// connect vertically to hallway
								int start = (hallY > y ? y + 1 : hallY + 1);
								int end   = (y > hallY ? y : hallY);
//								System.out.println("drawing vertical hall-to-hall from " + y + " to " + hallY);
								// add connector
								for(int c = start; c < end; c++)
								{
									if(plan[c][x] < 0)
									{
										c = end;
									}
									else if(plan[c][x] < Globals.MAP_ROOM)
									{
										plan[c][x] = plan[c][x] | 5;
									}
								}
								// modify hall segment with new connecting hallway
								if(y > hallY)
								{
									plan[hallY][hallX] = plan[hallY][hallX] | 4;
								}
								else
								{
									plan[hallY][hallX] = plan[hallY][hallX] | 1;
								}
								// add openings to hall fountain, if that's what the current tile is
								if((plan[y][x] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
								{
									plan[y][x] = plan[y][x] | (hallY > y ? 4 : 1);
								}
							}
							else
							{
								// connect horizontally to hallway
								int start = (hallX > x ? x + 1 : hallX + 1);
								int end   = (x > hallX ? x : hallX);
//								System.out.println("drawing horizontal hall-to-hall from " + x + " to " + hallX);
								// add connector
								for(int c = start; c < end; c++)
								{
									if(plan[y][c] < 0)
									{
										c = end;
									}
									else if(plan[y][c] < Globals.MAP_ROOM)
									{
										plan[y][c] = plan[y][c] | 10;
									}
								}
								// modify hall segment with new connecting hallway
								if(x > hallX)
								{
									plan[hallY][hallX] = plan[hallY][hallX] | 2;
								}
								else
								{
									plan[hallY][hallX] = plan[hallY][hallX] | 8;
								}
								// add openings to hall fountain, if that's what the current tile is
								if((plan[y][x] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
								{
									plan[y][x] = plan[y][x] | (hallX > x ? 2 : 8);
								}
							}
						}
					}
					else
					{
						int whichRoom = rnd.nextInt(matchRoomCount);
						int roomX = matchRoomListX[whichRoom];
						int roomY = matchRoomListY[whichRoom];
						if(roomX == x)
						{
							// connect vertically to room
							int start = (roomY > y ? y + 1 : roomY + 1);
							int end   = (y > roomY ? y : roomY);
//							System.out.println("drawing vertical hall-to-room from " + y + " to " + roomY);
							// add connector
							for(int c = start; c < end; c++)
							{
								if(plan[c][x] < 0)
								{
									c = end;
								}
								else if(plan[c][x] < Globals.MAP_ROOM)
								{
									plan[c][x] = plan[c][x] | 5;
								}
							}
							// add openings to hall fountain, if that's what either endpoint tile is
							if((plan[roomY][roomX] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
							{
								plan[roomY][roomX] = plan[roomY][roomX] | (roomY > y ? 1 : 4);
							}
							if((plan[y][x] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
							{
								plan[y][x] = plan[y][x] | (roomY > y ? 4 : 1);
							}
						}
						else
						{
							// connect horizontally to room
							int start = (roomX > x ? x + 1 : roomX + 1);
							int end   = (x > roomX ? x : roomX);
//							System.out.println("drawing horizontal hall-to-room from " + x + " to " + roomX);
							// add connector
							for(int c = start; c < end; c++)
							{
								if(plan[y][c] < 0)
								{
									c = end;
								}
								else if(plan[y][c] < Globals.MAP_ROOM)
								{
									plan[y][c] = plan[y][c] | 10;
								}
							}
							// add openings to hall fountain, if that's what either endpoint tile is
							if((plan[roomY][roomX] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
							{
								plan[roomY][roomX] = plan[roomY][roomX] | (roomX > x ? 8 : 2);
							}
							if((plan[y][x] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
							{
								plan[y][x] = plan[y][x] | (roomX > x ? 2 : 8);
							}
						}
					}
				}
			}
		}
		// finally, connect fountains to all adjacent hallways
		for(int y = 1; y < plan.length - 1; y++)
		{
			for(int x = 1; x < plan[0].length - 1; x++)
			{
				if((plan[y][x] & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
				{
					if(plan[y - 1][x] > 0)
					{
						plan[y - 1][x] = (plan[y - 1][x] | 4);
						plan[y][x]     = (plan[y][x]     | 1);
					}
					if(plan[y + 1][x] > 0)
					{
						plan[y + 1][x] = (plan[y + 1][x] | 1);
						plan[y][x]     = (plan[y][x]     | 4);
					}
					if(plan[y][x - 1] > 0)
					{
						plan[y][x - 1] = (plan[y][x - 1] | 2);
						plan[y][x]     = (plan[y][x]     | 8);
					}
					if(plan[y][x + 1] > 0)
					{
						plan[y][x + 1] = (plan[y][x + 1] | 8);
						plan[y][x]     = (plan[y][x]     | 2);
					}
				}
			}
		}
	}

	private static void cleanUp(int[][] plan)
	{
		for(int y = 0; y < plan.length; y++)
		{
			for(int x = 0; x < plan[0].length; x++)
			{
				if(plan[y][x] == -1)
				{
					plan[y][x] = 0;
				}
			}
		}
	}

	private static Board createStartLevel(String levelName, int levelWidth, int levelHeight)
	{
		int[][] plan = new int[levelHeight][levelWidth];
		Hashtable<String, Room> rooms = new Hashtable<String, Room>(1);
		addRoom(plan, rooms, rnd.nextInt(plan[0].length - 2) + 1, rnd.nextInt(plan.length - 2) + 1, 0, Globals.DIFF_NORMAL, 1, Globals.STAIRS_DOWN, Globals.FEATURE_SHOP, 0, 0, 0, 0, false);
		return new Board(levelName, plan, rooms);
	}

	private static boolean isLevelInvalid(int[][] levelPlan, Coord startRoom)
	{
		int[][] floodPlan = new int[levelPlan.length][levelPlan[0].length];
		for(int y = 0; y < levelPlan.length; y++)
		{
			for(int x = 0; x < levelPlan[0].length; x++)
			{
				if(levelPlan[y][x] == 16)
				{
					floodPlan[y][x] = 2;
				}
				else if(levelPlan[y][x] > 0)
				{
					floodPlan[y][x] = 1;
				}
				else
				{
					floodPlan[y][x] = 0;
				}
			}
		}
		floodPlan[startRoom.getY()][startRoom.getX()] = 3;
		while(floodFloor(floodPlan)) { ; }
		for(int y = 0; y < floodPlan.length; y++)
		{
			for(int x = 0; x < floodPlan[0].length; x++)
			{
				if(floodPlan[y][x] != 0 && floodPlan[y][x] != 3)
				{
					return true;
				}
			}
		}
		return false;
	}

	private static boolean floodFloor(int[][] floodPlan)
	{
		boolean bChanged = false;
		for(int y = 0; y < floodPlan.length; y++)
		{
			for(int x = 0; x < floodPlan[0].length; x++)
			{
				if(floodPlan[y][x] > 0 && floodPlan[y][x] < 3)
				{
					if(
						(floodPlan[y - 1][x] == 3) ||
						(floodPlan[y + 1][x] == 3) ||
						(floodPlan[y][x - 1] == 3) ||
						(floodPlan[y][x + 1] == 3)
					)
					{
						floodPlan[y][x] = 3;
						bChanged = true;
					}
				}
			}
		}
		return bChanged;
	}

	public static Vector<Board> makeDungeon(int levelwidth, int levelheight, int levelcount, int roomcountmin, int roomcountmax, int difficulty, int partysize, int monsterodds, int itemodds, int chestodds, int trapodds, int vaultodds, String[] levelNames, String[] levelGraphics)
	{
		vcUniqueMonsters.removeAllElements();
		vcUniqueItems.removeAllElements();
		int currlevel = 1;
		Vector<Board> vcDungeon = new Vector<Board>(levelcount + 1);
		vcDungeon.add(LevelMaker.createStartLevel(levelNames[0], levelwidth, levelheight));
		while(currlevel <= levelcount)
		{
			Vector<Coord> vcUpStairs = new Vector<Coord>();
			Board  boardPrev  = vcDungeon.elementAt(currlevel - 1);
			for(int y = 0; y < boardPrev.getHeight(); y++)
			{
				for(int x = 0; x < boardPrev.getWidth(); x++)
				{
					if(boardPrev.getRoom(x, y) != null)
					{
						if(boardPrev.getRoom(x, y).getStairType() == Globals.STAIRS_DOWN)
						{
							vcUpStairs.add(new Coord(x, y));
						}
					}
				}
			}
			Coord[] upStairs = new Coord[vcUpStairs.size()];
			for(int c = 0; c < vcUpStairs.size(); c++)
			{
				upStairs[c] = new Coord(vcUpStairs.elementAt(c));
			}
			vcDungeon.add(LevelMaker.makeLevel(levelNames[currlevel], levelwidth, levelheight, rnd.nextInt(roomcountmax - roomcountmin) + roomcountmin, currlevel, difficulty, partysize, monsterodds, itemodds, chestodds, trapodds, vaultodds, upStairs, (currlevel < levelcount), (levelGraphics[currlevel] != null && levelGraphics[currlevel].length() > 0), levelGraphics[currlevel]));
			currlevel++;
		}
		return vcDungeon;
	}

	public static Vector<Quest> makeQuests(int levels)
	{
		Vector<Quest> vcQuests = new Vector<Quest>();
		if(Globals.QUESTLOG.size() > 0)
		{
			for(int i = 0; i < Globals.QUESTLOG.size(); i++)
			{
				Quest q = new Quest((Quest)(Globals.QUESTLOG.elementAt(i)));
				if(q.getTurns() != Globals.QUEST_NOTIMELIMIT)
				{
					q.setTurns(q.getTurns() * levels);
				}
				if(!(q.getFixedLevel().equals(Quest.NO_FIXED_LEVEL)) && Globals.CURR_DUNGEON.getLevelIndexByName(q.getFixedLevel()) > -1)
				{
					q.setLevel(Globals.CURR_DUNGEON.getLevelIndexByName(q.getFixedLevel()));
				}
				else if(q.getLevel() > levels)
				{
					q.setLevel(levels);
				}
				int rooms = Globals.CURR_DUNGEON.getLevel(q.getLevel()).getRoomCount();
				boolean seekingRoom = true;
				int searchPass = 0;
				while(seekingRoom)
				{
					for(Enumeration<String> e = Globals.CURR_DUNGEON.getLevel(q.getLevel()).getRooms().keys(); e.hasMoreElements();)
					{
						String roomKey = e.nextElement();
						Coord rPos = Globals.getRoomKeyCoord(roomKey);
						boolean isOkay = true;
						for(int r = 0; r < i; r++)
						{
							Quest q2 = vcQuests.elementAt(r);
							if(q.getLevel() == q2.getLevel() && rPos.equals(q2.getRoom()))
							{
								isOkay = false;
							}
						}
						if(isOkay && seekingRoom)
						{
							Room rm = Globals.CURR_DUNGEON.getLevel(q.getLevel()).getRoom(roomKey);
							if(rm.hasVault() && searchPass >= 0)
							{
								q.setRoom(rPos);
								rm.getVault().addContentItem(QuestItem.createFromString(i + ""));
								seekingRoom = false;
							}
							else if(rm.hasChest() && rm.getMonsterCount() > 0 && searchPass >= 1)
							{
								q.setRoom(rPos);
								rm.getChest().addContentItem(QuestItem.createFromString(i + ""));
								seekingRoom = false;
							}
							else if(rm.hasChest() && searchPass >= 2)
							{
								q.setRoom(rPos);
								rm.getChest().addContentItem(QuestItem.createFromString(i + ""));
								seekingRoom = false;
							}
							else if(rm.getMonsterCount() > 0 && searchPass >= 3)
							{
								BaseItem biReturn = rm.gainItem(QuestItem.createFromString(i + ""));
								if(biReturn != null)
								{
									seekingRoom = true;
								}
								else
								{
									q.setRoom(rPos);
									seekingRoom = false;
								}
							}
							else if(rm.getStairType() == Globals.STAIRS_NONE && searchPass >= 4)
							{
								BaseItem biReturn = rm.gainItem(QuestItem.createFromString(i + ""));
								if(biReturn != null)
								{
									seekingRoom = true;
								}
								else
								{
									q.setRoom(rPos);
									seekingRoom = false;
								}
							}
							else if(searchPass >= 5)
							{
								BaseItem biReturn = rm.gainItem(QuestItem.createFromString(i + ""));
								if(biReturn != null)
								{
									seekingRoom = true;
								}
								else
								{
									q.setRoom(rPos);
									seekingRoom = false;
								}
							}
						}
					}
					if(seekingRoom)
					{
						searchPass++;
					}
					else
					{
						searchPass = 0;
					}
				}
				vcQuests.add(q);
			}
		}
		return vcQuests;
	}

	public static void saveDungeon(String filename, String moduleName, Vector<Board> vcDungeon)
	{
		System.out.print("Saving dungeon...");
		Globals.CURR_DUNGEON.setLevels(vcDungeon);
		Globals.CURR_DUNGEON.setQuests(LevelMaker.makeQuests(vcDungeon.size()));
		ObjectParser.saveGameFile(filename, moduleName, Globals.CURR_DUNGEON, new Party(0));
		System.out.println("done");
	}
}