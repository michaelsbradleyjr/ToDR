package com.dreamcodex.todr.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.dreamcodex.todr.object.Ammo;
import com.dreamcodex.todr.object.Armor;
import com.dreamcodex.todr.object.BaseItem;
import com.dreamcodex.todr.object.Board;
import com.dreamcodex.todr.object.CharacterClass;
import com.dreamcodex.todr.object.Chest;
import com.dreamcodex.todr.object.Dungeon;
import com.dreamcodex.todr.object.Floorplan;
import com.dreamcodex.todr.object.Globals;
import com.dreamcodex.todr.object.Item;
import com.dreamcodex.todr.object.ItemClass;
import com.dreamcodex.todr.object.ItemContainer;
import com.dreamcodex.todr.object.Lifeform;
import com.dreamcodex.todr.object.Monster;
import com.dreamcodex.todr.object.MonsterDef;
import com.dreamcodex.todr.object.Party;
import com.dreamcodex.todr.object.Player;
import com.dreamcodex.todr.object.Quest;
import com.dreamcodex.todr.object.QuestItem;
import com.dreamcodex.todr.object.Room;
import com.dreamcodex.todr.object.Effect;
import com.dreamcodex.todr.object.Vault;
import com.dreamcodex.todr.object.Weapon;
import com.dreamcodex.util.Coord;
import com.dreamcodex.util.MP3Clip;

public class ObjectParser
{
	// filekey constants
	public static final String FILEKEY_LEVEL_COORD = "C:";
	public static final String FILEKEY_LEVEL_GRID  = "M:";
	public static final String FILEKEY_LEVEL_ROOM  = "R";
	public static final String FILEKEY_LEVEL_GRPHX = "G:";
	public static final String FILEKEY_LEVEL_MAP   = "X";
	public static final String FILEKEY_DIFFICULTY  = "DF:";
	public static final String FILEKEY_PARTY       = "PX:";
	public static final String FILEKEY_PLAYER      = "PM:";
	public static final String FILEKEY_CHARCLASS   = "CC:";
	public static final String FILEKEY_CHARDSTNG   = "CL:";
	public static final String FILEKEY_CHARASSOC   = "CA:";
	public static final String FILEKEY_MONSTERDEF  = "MD:";
	public static final String FILEKEY_MONSTERUNIQ = "MU:";
	public static final String FILEKEY_WEAPON      = "W";
	public static final String FILEKEY_WEAPON_NORM = FILEKEY_WEAPON + "N:";
	public static final String FILEKEY_WEAPON_DEF  = FILEKEY_WEAPON + "D:";
	public static final String FILEKEY_WEAPON_LGND = FILEKEY_WEAPON + "L:";
	public static final String FILEKEY_ARMOR       = "A";
	public static final String FILEKEY_ARMOR_NORM  = FILEKEY_ARMOR + "N:";
	public static final String FILEKEY_ARMOR_DEF   = FILEKEY_ARMOR + "D:";
	public static final String FILEKEY_ARMOR_LGND  = FILEKEY_ARMOR + "L:";
	public static final String FILEKEY_AMMO        = "X";
	public static final String FILEKEY_AMMO_ITEM   = FILEKEY_AMMO + "A:";
	public static final String FILEKEY_QUEST       = "Q";
	public static final String FILEKEY_ITEMCLASS   = "IG:";
	public static final String FILEKEY_ITEM_UNIV   = "IU:";
	public static final String FILEKEY_ITEM_MODULE = "IC:";
	public static final String FILEKEY_ITEM_RATION = "IR:";
	public static final String FILEKEY_ITEM_HEAL   = "IH:";
	public static final String FILEKEY_ITEM        = "IT:";
	public static final String FILEKEY_ITEM_MODIF  = "IM:";
	public static final String FILEKEY_ITEMCAP_WPN = "IPW:";
	public static final String FILEKEY_ITEMCAP_ARM = "IPA:";
	public static final String FILEKEY_EFFECT      = "FX:";
	public static final String FILEKEY_TRAPDEF     = "TD:";
	public static final String FILEKEY_MODULENAME  = "MN:";
	public static final String FILEKEY_MODULEDESCR = "MO:";
	public static final String FILEKEY_MODULEQUEST = "MQ:";
	public static final String FILEKEY_QUEST_SAVED = "QS:";
	public static final String FILEKEY_BOARD_NAME  = "N:";
	public static final String FILEKEY_BOARD_TERM  = "Q:";

	private static MediaTracker mediaTracker;
	private static String       moduleName;

	public static void setMediaTracker(MediaTracker mt) { mediaTracker = mt; }

	public static void   setModuleName(String name) { moduleName = new String(name); }
	public static String getModuleName()            { return moduleName; }
	public static String getModulePath()            { return Globals.MODSPATH + moduleName + File.separator; }

	/* Image loading utility method */
	public static Image loadImage(String imgname)
	{
		if(imgname == null || imgname.equals("") || imgname.equals("[none]") || mediaTracker == null) { return (Image)null; }
		Image imgTemp = Toolkit.getDefaultToolkit().getImage(Globals.RSRCPATH + "images" + File.separator + imgname);
		mediaTracker.addImage(imgTemp, 1);
		try
		{
			mediaTracker.waitForID(1);
		}
		catch(InterruptedException ie)
		{
			System.err.println("Error loading image " + imgname + " - " + ie.getMessage());
		}
		return imgTemp;
	}

	/* Image loading utility method */
	public static Image loadModuleImage(String imgname)
	{
		if(imgname == null || imgname.equals("") || imgname.startsWith("[none]") || mediaTracker ==  null) { return (Image)null; }
		Image imgTemp = Toolkit.getDefaultToolkit().getImage(Globals.MODSPATH + (moduleName != null && !(moduleName.equals("")) ? moduleName + File.separator : "") + "images" + File.separator + imgname);
		mediaTracker.addImage(imgTemp, 1);
		try
		{
			mediaTracker.waitForID(1);
		}
		catch(InterruptedException ie)
		{
			System.err.println("Error loading image " + imgname + " - " + ie.getMessage());
		}
		return imgTemp;
	}

	/* Soundclip loading utility method (global) */
	public static MP3Clip loadSound(String sndname)
	{
		if(sndname == null || sndname.equals("") || sndname.startsWith("[none]")) { return (MP3Clip)null; }
		return new MP3Clip(Globals.RSRCPATH + "sounds" + File.separator + sndname);
	}

	/* Soundclip loading utility method (module) */
	public static MP3Clip loadModuleSound(String sndname)
	{
		if(sndname == null || sndname.equals("") || sndname.startsWith("[none]")) { return (MP3Clip)null; }
		return new MP3Clip(Globals.MODSPATH + (moduleName != null && !(moduleName.equals("")) ? moduleName + File.separator : "") + "sounds" + File.separator + sndname);
	}

	/* Parse game world method */
	public static Vector<Board> parseLevelsFromFile(String filename)
	{
		Vector<Board> vcLevels = new Vector<Board>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String lineIn = "";
			String currBoardName = "";
			int[][] boardGrid = new int[0][0];
			int[][] boardMap  = new int[0][0];
			int rowcount = 0;
			int colcount = 0;
			boolean isMapped = false;
			Hashtable<String, Room> htRooms = new Hashtable<String, Room>();
			boolean showAll = false;
			boolean withinCurrBoard = false;
			String lvlGraphicsName = (String)null;
			boolean hasLvlGraphics = false;
			boolean hasOpenCeiling = false;
			boolean hasOpenFloor = false;
			Image[] lvlGraphics = new Image[5];
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					/* this is a comment line */;
				}
				else if(lineIn.startsWith(FILEKEY_BOARD_NAME) || lineIn.startsWith(FILEKEY_BOARD_TERM)) // Name of board || Board terminator
				{
					if(withinCurrBoard)
					{
						// close and save board
						Board tmpBoard = new Board(currBoardName, boardGrid, boardMap, htRooms, isMapped);
						if(hasLvlGraphics)
						{
							tmpBoard.setHasLevelGraphics(hasLvlGraphics);
							tmpBoard.setLevelGraphicName(lvlGraphicsName);
							tmpBoard.setLevelGraphics(lvlGraphics);
							tmpBoard.setHasOpenCeiling(hasOpenCeiling);
							tmpBoard.setHasOpenFloor(hasOpenFloor);
						}
						vcLevels.add(new Board(tmpBoard));
						currBoardName = "";
						htRooms.clear();
						rowcount = 0;
						colcount = 0;
						isMapped = false;
						withinCurrBoard = false;
						hasLvlGraphics = false;
						lvlGraphics = new Image[5];
						hasOpenCeiling = false;
						hasOpenFloor = false;
					}
					if(lineIn.startsWith(FILEKEY_BOARD_NAME))
					{
						currBoardName = lineIn.substring(lineIn.indexOf(FILEKEY_BOARD_NAME) + FILEKEY_BOARD_NAME.length(), lineIn.length());
						withinCurrBoard = true;
					}
				}
				else if(lineIn.startsWith(FILEKEY_LEVEL_COORD)) // Coord dimensions of board (grid points)
				{
					int boardX = Integer.parseInt(lineIn.substring(lineIn.indexOf(FILEKEY_LEVEL_COORD) + FILEKEY_LEVEL_COORD.length(), lineIn.indexOf("/")));
					int boardY = Integer.parseInt(lineIn.substring(lineIn.indexOf("/") + 1, lineIn.length()));
					boardGrid = new int[boardY][boardX];
					boardMap  = new int[boardY][boardX];
				}
				else if(lineIn.startsWith(FILEKEY_LEVEL_GRID)) // Board layout (Terrains mapped to grid points)
				{
					lineIn = lineIn.substring(lineIn.indexOf(FILEKEY_LEVEL_GRID) + FILEKEY_LEVEL_GRID.length(), lineIn.length());
					colcount = 0;
					while(colcount < lineIn.length())
					{
						boardGrid[rowcount][colcount / 2] = Character.digit(lineIn.charAt(colcount), 33);
						boardMap[rowcount][colcount / 2]  = (lineIn.charAt(colcount + 1) == Globals.MAPPED_CHAR_VISITED ? Globals.MAPPED_VISITED : (lineIn.charAt(colcount + 1) == Globals.MAPPED_CHAR_MAPPED ? Globals.MAPPED_MAPPED : (lineIn.charAt(colcount + 1) == Globals.MAPPED_CHAR_SEEN ? Globals.MAPPED_SEEN : Globals.MAPPED_NONE)));
						colcount += 2;
					}
					rowcount++;
				}
				else if(lineIn.startsWith(FILEKEY_LEVEL_ROOM)) // Room object
				{
					// format of Room object entry in file:
					// RXXYYSFP[MM:#]{I?}&
					//   XX = x coordinate (int, 2 chars)
					//   YY = y coordinate (int, 2 chars)
					//   S  = stair type   (int, 1 char)
					//   F  = feature type (int, 1 char)
					//   P  = plan number  (int, 1 char)
					//   [MM:#,*] = monster block (not always present)
					//     MM = monster index     (int, 2 chars)
					//     # = number of monsters (int, 1 char)
					//     ,* = additional monster entries
					//   {I:?,*} = item block (not always present)
					//     I = item name
					//     ? = item variant attribute value (usually "charges")
					//     ,* = additional item entries
					//   &#() = chest ('&', 1 char, not always present)
					//      # = trap type (int, 1 char, present if chest present, - == no trap)
					//      () = items in chest
					//   $#() = vault ('$', 1 char, not always present)
					//      # = code (int, multiple chars, present if vault present, base36 encoded)
					//      () = items in vault
					String roomKey = lineIn.substring(0, 5);
					int roomX = Globals.getRoomKeyX(roomKey);
					int roomY = Globals.getRoomKeyY(roomKey);
					int stairType   = Character.digit(lineIn.charAt(5), 10);
					int featureType = Character.digit(lineIn.charAt(6), 10);
					int planNumber  = Character.digit(lineIn.charAt(7), 10);
					lineIn = lineIn.substring(8, lineIn.length());
					Vector<Monster>  vcMonsters = new Vector<Monster>();
					Vector<BaseItem> vcItems = new Vector<BaseItem>();
					ItemContainer itmcon = (ItemContainer)null;
					int trapType = Globals.TRAP_NONE;
					while(lineIn.indexOf("[") > -1 && lineIn.indexOf("]") > -1)
					{
						String monsterblock = lineIn.substring(lineIn.indexOf("[") + 1, lineIn.indexOf("]"));
						lineIn = lineIn.substring(lineIn.indexOf(monsterblock) + monsterblock.length() + 1);
						int    monsterindex = Integer.parseInt(monsterblock.substring(0, 2));
						int    monstercount = Integer.parseInt(monsterblock.substring(2));
						for(int c = 0; c < monstercount; c++)
						{
							vcMonsters.add(Globals.BESTIARY.get(Globals.INDEX_BESTIARY.get(monsterindex)).getInstance());
						}
					}
					if(lineIn.indexOf("{") > -1 && lineIn.indexOf("}") > -1)
					{
						String itemblock = lineIn.substring(lineIn.indexOf("{") + 1, lineIn.indexOf("}"));
						StringTokenizer stParse = new StringTokenizer(itemblock, ":", false);
						while(stParse.hasMoreTokens())
						{
							String itementry = (String)(stParse.nextElement());
							if(itementry.startsWith(FILEKEY_WEAPON))
							{
								vcItems.add(Weapon.createFromString(itementry.substring(1)));
							}
							else if(itementry.startsWith(FILEKEY_ARMOR))
							{
								vcItems.add(Armor.createFromString(itementry.substring(1)));
							}
							else if(itementry.startsWith(FILEKEY_AMMO))
							{
								vcItems.add(Ammo.createFromString(itementry.substring(1)));
							}
							else if(itementry.startsWith(FILEKEY_QUEST))
							{
								vcItems.add(QuestItem.createFromString(itementry.substring(1)));
							}
							else
							{
								vcItems.add(Item.createFromString(itementry));
							}
						}
					}
					if(lineIn.indexOf("$") > -1)
					{
						itmcon = Vault.createFromString(lineIn.substring(lineIn.indexOf("$") + 1));
					}
					else if(lineIn.indexOf("&") > -1)
					{
						itmcon = Chest.createFromString(lineIn.substring(lineIn.indexOf("&") + 1));
					}
					htRooms.put(Globals.getRoomKey(roomX, roomY), new Room(stairType, featureType, planNumber, vcMonsters, vcItems, itmcon));
				}
				else if(lineIn.startsWith(FILEKEY_LEVEL_GRPHX))
				{
					hasLvlGraphics  = true;
					lvlGraphicsName = lineIn.substring(lineIn.indexOf(FILEKEY_LEVEL_GRPHX) + FILEKEY_LEVEL_GRPHX.length(), lineIn.indexOf("|"));
					hasOpenCeiling  = lineIn.substring(lineIn.indexOf("|") + 1, lineIn.indexOf("|") + 2).equals("Y");
					hasOpenFloor    = lineIn.substring(lineIn.indexOf("|") + 2, lineIn.indexOf("|") + 3).equals("Y");
					lvlGraphics[Globals.GRFX_WALL]    = loadModuleImage(Globals.LVLGRFX_PREFIX + Globals.LVLGRFX_SEP + lvlGraphicsName + Globals.LVLGRFX_SEP + Globals.LVLGRFX_WALL    + Globals.IMAGEXTN);
					lvlGraphics[Globals.GRFX_DOOR]    = loadModuleImage(Globals.LVLGRFX_PREFIX + Globals.LVLGRFX_SEP + lvlGraphicsName + Globals.LVLGRFX_SEP + Globals.LVLGRFX_DOOR    + Globals.IMAGEXTN);
					lvlGraphics[Globals.GRFX_FLOOR]   = loadModuleImage(Globals.LVLGRFX_PREFIX + Globals.LVLGRFX_SEP + lvlGraphicsName + Globals.LVLGRFX_SEP + Globals.LVLGRFX_FLOOR   + Globals.IMAGEXTN);
					lvlGraphics[Globals.GRFX_CEILING] = loadModuleImage(Globals.LVLGRFX_PREFIX + Globals.LVLGRFX_SEP + lvlGraphicsName + Globals.LVLGRFX_SEP + Globals.LVLGRFX_CEILING + Globals.IMAGEXTN);
				}
				else if(lineIn.equals(FILEKEY_LEVEL_MAP))
				{
					isMapped = true;
				}
				else if(lineIn.startsWith(FILEKEY_DIFFICULTY))
				{
					Globals.CURR_DUNGEON.setDifficulty(Integer.parseInt(lineIn.substring(FILEKEY_DIFFICULTY.length())));
				}
				else
				{
					// non-levels line, ignore
				}
			} while (lineIn != null);
			br.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading levels from save file (" + filename + ") : "  + ioe.getMessage());
			return (Vector<Board>)null;
		}
		return vcLevels;
	}

	public static Party parsePartyFromFile(String filename)
	{
		Party party = (Party)null;
		Vector<Player> vcPlayers = new Vector<Player>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String lineIn = "";
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// comment line, do not parse
				}
				else if(lineIn.startsWith(FILEKEY_PLAYER))
				{
					// new party member (Player object) entry
					lineIn = lineIn.substring(3, lineIn.length());
					vcPlayers.add(Player.createFromString(lineIn));
				}
				else if(lineIn.startsWith(FILEKEY_PARTY))
				{
					party = Party.createFromString(lineIn.substring(3));
				}
				else
				{
					// unknown file entry
				}
			} while (lineIn != null);
			br.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading party from file (" + filename + ") : "  + ioe.getMessage());
			System.exit(1);
		}
		catch(NumberFormatException nfe)
		{
			System.out.println("Error loading party from file (" + filename + ") : "  + nfe.getMessage());
			System.exit(1);
		}
		if(vcPlayers.size() < 1)
		{
			System.out.println("No players in saved party");
			System.exit(2);
		}
		Player[] players = new Player[vcPlayers.size()];
		for(int p = 0; p < vcPlayers.size(); p++)
		{
			players[p] = vcPlayers.elementAt(p);
			players[p].setImageNormal(loadModuleImage("player_" + players[p].getCharacterClassName().toLowerCase() + "_" + Globals.CHARACTER_DIFF_KEYS[players[p].getCharacterDiff()].toLowerCase() + "_normal" + Globals.IMAGEXTN));
			players[p].setImageAttack(loadModuleImage("player_" + players[p].getCharacterClassName().toLowerCase() + "_" + Globals.CHARACTER_DIFF_KEYS[players[p].getCharacterDiff()].toLowerCase() + "_attack" + Globals.IMAGEXTN));
		}
		vcPlayers = null;
		if(party != null)
		{
			party.setPlayers(players);
		}
		else
		{
			System.out.println("No party in save file");
			System.exit(2);
		}
		return party;
	}

	public static Vector<Quest> parseQuestsFromFile(String filename)
	{
		Vector<Quest> vcQuests = new Vector<Quest>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String lineIn = "";
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					/* this is a comment line */;
				}
				else if(lineIn.startsWith(FILEKEY_QUEST_SAVED))
				{
					vcQuests.add(Quest.createFromString(lineIn.substring(3)));
				}
			} while (lineIn != null);
			br.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading quests from save file (" + filename + ") : "  + ioe.getMessage());
			return (Vector<Quest>)null;
		}
		return vcQuests;
	}

	// Load module-specific graphic properties
	public static void parseDungeonProperties()
	{
		Properties dungeonProperties = new Properties();
		try
		{
			dungeonProperties.load(new FileInputStream(new File(getModulePath() + "dungeon.properties")));
			// shared settings
			//   colors
			Globals.COLOR_MAIN_BACKGROUND = new Color(Integer.parseInt(dungeonProperties.getProperty("colormainback"), 16));
			//   fonts
			Globals.FONTSIZE        = Integer.parseInt(dungeonProperties.getProperty("fontsize"));
			Globals.FONTWIDTH       = Integer.parseInt(dungeonProperties.getProperty("fontwidth"));
			Globals.FONTHEIGHT      = Integer.parseInt(dungeonProperties.getProperty("fontheight"));
			Globals.FONTRATIO       = Globals.TILESCALE / Globals.FONTWIDTH;
			Globals.FONTLINE        = Globals.TILESCALE / Globals.FONTHEIGHT;
			Globals.FONTSIZESMALL   = Integer.parseInt(dungeonProperties.getProperty("fontsizesmall"));
			Globals.FONTWIDTHSMALL  = Integer.parseInt(dungeonProperties.getProperty("fontwidthsmall"));
			Globals.FONTHEIGHTSMALL = Integer.parseInt(dungeonProperties.getProperty("fontheightsmall"));
			FileInputStream isFontA = new FileInputStream(getModulePath() + "fonts" + File.separator + dungeonProperties.getProperty("fontmainttf"));
			Font fntLoadA = Font.createFont(Font.TRUETYPE_FONT, isFontA);
			Globals.FONT_MAIN = fntLoadA.deriveFont(Font.PLAIN, Globals.FONTSIZE);
			FileInputStream isFontC = new FileInputStream(getModulePath() + "fonts" + File.separator + dungeonProperties.getProperty("fontsmallttf"));
			Font fntLoadC = Font.createFont(Font.TRUETYPE_FONT, isFontC);
			Globals.FONT_SMALL = fntLoadC.deriveFont(Font.PLAIN, Globals.FONTSIZESMALL);
			// Global vars
			Globals.DEF_INTERVAL_HEALING = Integer.parseInt(dungeonProperties.getProperty("healingintdef"));
			Globals.DEF_INTERVAL_RATIONS = Integer.parseInt(dungeonProperties.getProperty("rationintdef"));
			Globals.DEF_HEALING_AMT_MIN  = Integer.parseInt(dungeonProperties.getProperty("healingmin"));
			Globals.DEF_HEALING_AMT_MAX  = Integer.parseInt(dungeonProperties.getProperty("healingmax"));
			Globals.MIN_WANDERING_CHANCE = Integer.parseInt(dungeonProperties.getProperty("wandermin"));
			Globals.MAX_WANDERING_CHANCE = Integer.parseInt(dungeonProperties.getProperty("wandermax"));
			Globals.ESC_WANDERING_CHANCE = Integer.parseInt(dungeonProperties.getProperty("wanderesc"));
			Globals.MIN_CHEST_ITEMS      = Math.min(Integer.parseInt(dungeonProperties.getProperty("chestitemsmin")), Globals.MAX_FLOOR_STACK);
			Globals.MAX_CHEST_ITEMS      = Math.min(Integer.parseInt(dungeonProperties.getProperty("chestitemsmax")), Globals.MAX_FLOOR_STACK);
			Globals.MIN_VAULT_ITEMS      = Math.min(Integer.parseInt(dungeonProperties.getProperty("vaultitemsmin")), Globals.MAX_FLOOR_STACK);
			Globals.MAX_VAULT_ITEMS      = Math.min(Integer.parseInt(dungeonProperties.getProperty("vaultitemsmax")), Globals.MAX_FLOOR_STACK);
			Globals.MAX_FOUND_CURRENCY   = Integer.parseInt(dungeonProperties.getProperty("findcurrency"));
			Globals.MAX_FOUND_RATIONS    = Integer.parseInt(dungeonProperties.getProperty("findrations"));
			Globals.CURRENCY_DROP_ODDS   = Integer.parseInt(dungeonProperties.getProperty("currencydropodds"));
			Globals.ROOM_MONSTER_RATIO   = Double.parseDouble(dungeonProperties.getProperty("roommonsterratio"));
			Globals.ROOM_ITEM_RATIO      = Double.parseDouble(dungeonProperties.getProperty("roomitemratio"));
			Globals.STARTING_GOLD        = Integer.parseInt(dungeonProperties.getProperty("startinggold"));
			Globals.STARTING_RATIONS     = Integer.parseInt(dungeonProperties.getProperty("startingrations"));
			// Dungeon object settings
			//    text boxes
			Globals.CURR_DUNGEON.setNotificationBox(parseRectangle(dungeonProperties.getProperty("noticebox")));
			Globals.CURR_DUNGEON.setFileListBox(parseRectangle(dungeonProperties.getProperty("filelistbox")));
			Globals.CURR_DUNGEON.setFileInfoBox(parseRectangle(dungeonProperties.getProperty("fileinfobox")));
			Globals.CURR_DUNGEON.setPlayerInfoBox(parseRectangle(dungeonProperties.getProperty("playerinfobox")));
			Globals.CURR_DUNGEON.setMonsterInfoBox(parseRectangle(dungeonProperties.getProperty("monsterinfobox")));
			Globals.CURR_DUNGEON.setItemManageBox(parseRectangle(dungeonProperties.getProperty("itemmanagebox")));
			Globals.CURR_DUNGEON.setItemPlayerBox(parseRectangle(dungeonProperties.getProperty("itemplayerbox")));
			Globals.CURR_DUNGEON.setShoppingBox(parseRectangle(dungeonProperties.getProperty("shoppingbox")));
			Globals.CURR_DUNGEON.setQuestListBox(parseRectangle(dungeonProperties.getProperty("questlistbox")));
			Globals.CURR_DUNGEON.setQuestTurnBox(parseRectangle(dungeonProperties.getProperty("questturnbox")));
			Globals.CURR_DUNGEON.setQuestInfoBox(parseRectangle(dungeonProperties.getProperty("questinfobox")));
			Globals.CURR_DUNGEON.setDoorBounds(parseRectangle(dungeonProperties.getProperty("doorbounds")));
			//   level parameters
			Globals.CURR_DUNGEON.setLevelWidth(Integer.parseInt(dungeonProperties.getProperty("levelwidth")));
			Globals.CURR_DUNGEON.setLevelHeight(Integer.parseInt(dungeonProperties.getProperty("levelheight")));
			Globals.CURR_DUNGEON.setRoomsMin(Integer.parseInt(dungeonProperties.getProperty("roomsmin")));
			Globals.CURR_DUNGEON.setRoomsMax(Integer.parseInt(dungeonProperties.getProperty("roomsmax")));
			Globals.CURR_DUNGEON.setMonsterOdds(Integer.parseInt(dungeonProperties.getProperty("monsterodds")));
			Globals.CURR_DUNGEON.setItemOdds(Integer.parseInt(dungeonProperties.getProperty("itemodds")));
			Globals.CURR_DUNGEON.setChestOdds(Integer.parseInt(dungeonProperties.getProperty("chestodds")));
			Globals.CURR_DUNGEON.setVaultOdds(Integer.parseInt(dungeonProperties.getProperty("vaultodds")));
			Globals.CURR_DUNGEON.setTrapOdds(Integer.parseInt(dungeonProperties.getProperty("trapodds")));
			Globals.CURR_DUNGEON.setWanderOdds(Integer.parseInt(dungeonProperties.getProperty("wanderodds")));
			String sFountainOdds = dungeonProperties.getProperty("fountainodds");
			StringTokenizer stParse = new StringTokenizer(sFountainOdds, ",", false);
			for(int i = 0; i < Globals.MAX_LEVEL_COUNT; i++)
			{
				Globals.CURR_DUNGEON.setFountainOdds(i, Integer.parseInt(stParse.nextToken()));
			}
			Globals.CURR_DUNGEON.setFountainBest(Integer.parseInt(dungeonProperties.getProperty("fountainbest")));
			Globals.CURR_DUNGEON.setFountainWorst(Integer.parseInt(dungeonProperties.getProperty("fountainworst")));
			// interface coordinate vars
			String ifacecoords = dungeonProperties.getProperty("interfacecoords");
			stParse = new StringTokenizer(ifacecoords, ",", false);
			Globals.CURR_DUNGEON.setPartyInterfaceX(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setPartyInterfaceY(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setLevelInterfaceX(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setLevelInterfaceY(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setMonstInterfaceX(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setMonstInterfaceY(Integer.parseInt(stParse.nextToken()));
			//   other vars
			String vaultcoords = dungeonProperties.getProperty("vaultcharcoords");
			stParse = new StringTokenizer(vaultcoords, ",", false);
			Globals.CURR_DUNGEON.setVaultCharX(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setVaultCharY(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setVaultHPsX(Integer.parseInt(stParse.nextToken()));
			Globals.CURR_DUNGEON.setVaultHPsY(Integer.parseInt(stParse.nextToken()));
			//   levels with shops
			String shoplvls = dungeonProperties.getProperty("shoplevels");
			stParse = new StringTokenizer(shoplvls, "|", false);
			int cnt = 0;
			while(stParse.hasMoreTokens())
			{
				Globals.SHOP_LEVELS[cnt] = stParse.nextToken().equals("+");
				cnt++;
			}
			//   colors
			Globals.CURR_DUNGEON.setColorHallBackground(new Color(Integer.parseInt(dungeonProperties.getProperty("colorhallback"), 16)));
			Globals.CURR_DUNGEON.setColorHallWall(new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolorwall"), 16)));
			Globals.CURR_DUNGEON.setColorHallFloor(new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolorfloor"), 16)));
			Globals.CURR_DUNGEON.setColorHallCeiling(new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolorceiling"), 16)));
			Globals.CURR_DUNGEON.setColorHallOutline(new Color(Integer.parseInt(dungeonProperties.getProperty("hallcoloroutline"), 16)));
			Globals.CURR_DUNGEON.setColorHallDoor(new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolordoor"), 16)));
			Globals.CURR_DUNGEON.setColorRoomBackground(new Color(Integer.parseInt(dungeonProperties.getProperty("colorroomback"), 16)));
			Globals.CURR_DUNGEON.setColorMapBackground(new Color(Integer.parseInt(dungeonProperties.getProperty("colormapbkgr"), 16)));
			Globals.CURR_DUNGEON.setColorMapMapped(new Color(Integer.parseInt(dungeonProperties.getProperty("colormapmapped"), 16)));
			Globals.CURR_DUNGEON.setColorMapVisited(new Color(Integer.parseInt(dungeonProperties.getProperty("colormapvisited"), 16)));
			Globals.CURR_DUNGEON.setColorMapParty(new Color(Integer.parseInt(dungeonProperties.getProperty("colormapparty"), 16)));
			Globals.CURR_DUNGEON.setColorActivePartyMember(new Color(Integer.parseInt(dungeonProperties.getProperty("coloractivepartymember"), 16)));
			Globals.CURR_DUNGEON.setColorMessageWindow(new Color(Integer.parseInt(dungeonProperties.getProperty("colormsgwindow"), 16)));
			Globals.CURR_DUNGEON.setColorTextBoxOutline(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextboxoutline"), 16)));
			Globals.CURR_DUNGEON.setColorTextBoxFill(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextboxfill"), 16)));
			Globals.CURR_DUNGEON.setColorDamageOutline(new Color(Integer.parseInt(dungeonProperties.getProperty("colordamageoutline"), 16)));
			Globals.CURR_DUNGEON.setColorDamageFill(new Color(Integer.parseInt(dungeonProperties.getProperty("colordamagefill"), 16)));
			Globals.CURR_DUNGEON.setColorHealingFill(new Color(Integer.parseInt(dungeonProperties.getProperty("colorhealingfill"), 16)));
			Globals.CURR_DUNGEON.setColorHitpointsFull(new Color(Integer.parseInt(dungeonProperties.getProperty("colorhpsfull"), 16)));
			Globals.CURR_DUNGEON.setColorHitpointsSome(new Color(Integer.parseInt(dungeonProperties.getProperty("colorhpssome"), 16)));
			Globals.CURR_DUNGEON.setColorHitpointsCrit(new Color(Integer.parseInt(dungeonProperties.getProperty("colorhpscrit"), 16)));
			Globals.CURR_DUNGEON.setColorHitpointsNone(new Color(Integer.parseInt(dungeonProperties.getProperty("colorhpsnone"), 16)));
			//   level-based hall colors
			for(int i = 0; i < Globals.MAX_LEVEL_COUNT; i++)
			{
				String dKey = "lvlcolorhallback" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelColorHallBackground(new Color(Integer.parseInt(dungeonProperties.getProperty(dKey), 16)), i);
				}
				dKey = "lvlhallcolorwall" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelColorHallWall(new Color(Integer.parseInt(dungeonProperties.getProperty(dKey), 16)), i);
				}
				dKey = "lvlhallcolorfloor" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelColorHallFloor(new Color(Integer.parseInt(dungeonProperties.getProperty(dKey), 16)), i);
				}
				dKey = "lvlhallcolorceiling" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelColorHallCeiling(new Color(Integer.parseInt(dungeonProperties.getProperty(dKey), 16)), i);
				}
				dKey = "lvlhallcoloroutline" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelColorHallOutline(new Color(Integer.parseInt(dungeonProperties.getProperty(dKey), 16)), i);
				}
				dKey = "lvlhallcolordoor" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelColorHallDoor(new Color(Integer.parseInt(dungeonProperties.getProperty(dKey), 16)), i);
				}
			}
			//   visualisation options
			Globals.CURR_DUNGEON.setShadeStrength(Double.parseDouble(dungeonProperties.getProperty("shadestrength")));
			Globals.CURR_DUNGEON.setOutlinesShown(dungeonProperties.getProperty("outlines").toLowerCase().equals("true"));
			//   images
			Globals.CURR_DUNGEON.setImageFloor(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgfloor")), 0);
			Globals.CURR_DUNGEON.setImageSolid(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgsolid")), 0);
			Globals.CURR_DUNGEON.setImageColumn(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgcolumn")), 0);
			Globals.CURR_DUNGEON.setImageWallH(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgwallh")), 0);
			Globals.CURR_DUNGEON.setImageWallV(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgwallv")), 0);
			Globals.CURR_DUNGEON.setImageDoorH(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgdoorh")), 0);
			Globals.CURR_DUNGEON.setImageDoorV(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgdoorv")), 0);
			Globals.CURR_DUNGEON.setImageStairsU(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgstairsu")), 0);
			Globals.CURR_DUNGEON.setImageStairsD(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgstairsd")), 0);
			Globals.CURR_DUNGEON.setImageFountain(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgfountain")), 0);
			Globals.CURR_DUNGEON.setImageStatue(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgstatue")), 0);
			Globals.CURR_DUNGEON.setImageShop(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgshop")), 0);
			Globals.CURR_DUNGEON.setImageChest(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgchest")), 0);
			Globals.CURR_DUNGEON.setImageVault(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgvault")), 0);
			Globals.CURR_DUNGEON.setImageHallFountain(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imghallfountain")));
			Globals.CURR_DUNGEON.setImageCurrency(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgcurrency")));
			Globals.CURR_DUNGEON.setImageRations(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgrations")));
			Globals.CURR_DUNGEON.setImageTurns(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgturns")));
			Globals.CURR_DUNGEON.setImageRounds(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgrounds")));
			Globals.CURR_DUNGEON.setImageVaultDigits(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgvaultdigits")));
			Globals.CURR_DUNGEON.setImageDeadPlayer(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgdeadplayer")));
			Globals.CURR_DUNGEON.setImageDeadMonster(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgdeadmonster")));
			Globals.CURR_DUNGEON.setImageMagicGood(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgmagicgood")));
			Globals.CURR_DUNGEON.setImageMagicEvil(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgmagicevil")));
			Globals.CURR_DUNGEON.setImageMagicVault(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgmagicvault")));
			Globals.CURR_DUNGEON.setImageInterface(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imginterface")));
			Globals.CURR_DUNGEON.setImageCardinals(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgcardinals")));
			Globals.CURR_DUNGEON.setImageMapTiles(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgmaptiles")));
			Globals.CURR_DUNGEON.setImageBackCreate(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackcreate")));
			Globals.CURR_DUNGEON.setImageBackExplore(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackexplore")));
			Globals.CURR_DUNGEON.setImageBackParty(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackparty")));
			Globals.CURR_DUNGEON.setImageBackPartyUnit(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackpartyunit")));
			Globals.CURR_DUNGEON.setImageBackPlayer(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackplayer")));
			Globals.CURR_DUNGEON.setImageBackMonster(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackmonster")));
			Globals.CURR_DUNGEON.setImageBackQuest(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackquest")));
			Globals.CURR_DUNGEON.setImageBackMap(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackmap")));
			Globals.CURR_DUNGEON.setImageBackShop(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackshop")));
			Globals.CURR_DUNGEON.setImageBackOrder(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackorder")));
			Globals.CURR_DUNGEON.setImageBackOrderCursor(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackordercursor")));
			Globals.CURR_DUNGEON.setImageBackItemManage(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackitemmanage")));
			Globals.CURR_DUNGEON.setImageBackVault(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackvault")));
			Globals.CURR_DUNGEON.setImageReticule(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgreticule")));
			Globals.CURR_DUNGEON.setImageHighlighter(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imghighlighter")));
			Globals.CURR_DUNGEON.setImageBattlegems(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbattlegems")));
			Globals.CURR_DUNGEON.setImageInvNotAllowed(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imginvnotallowed")));
			Globals.CURR_DUNGEON.setImageInvNotUsable(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imginvnotusable")));
			Globals.CURR_DUNGEON.setImageInvFull(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imginvfull")));
			Globals.CURR_DUNGEON.setImageGameOverSlain(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imggameoverslain")));
			Globals.CURR_DUNGEON.setImageGameOverFailed(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imggameoverfailed")));
			Globals.CURR_DUNGEON.setImageGameOverPartial(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imggameoverpartial")));
			Globals.CURR_DUNGEON.setImageGameOverVictory(ObjectParser.loadModuleImage(dungeonProperties.getProperty("imggameovervictory")));

			//   font colors
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextmenu"), 16)), Globals.FONT_COLOR_MENU);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextstat"), 16)), Globals.FONT_COLOR_STAT);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortexthead"), 16)), Globals.FONT_COLOR_HEAD);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortexttext"), 16)), Globals.FONT_COLOR_TEXT);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextactv"), 16)), Globals.FONT_COLOR_ACTV);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextinac"), 16)), Globals.FONT_COLOR_INAC);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextwarn"), 16)), Globals.FONT_COLOR_WARN);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortexthigh"), 16)), Globals.FONT_COLOR_HIGH);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextinfo"), 16)), Globals.FONT_COLOR_INFO);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortexthotk"), 16)), Globals.FONT_COLOR_HOTK);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextuniq"), 16)), Globals.FONT_COLOR_UNIQ);
			//   other values
			Globals.CURR_DUNGEON.setOffsetCardinalX(Integer.parseInt(dungeonProperties.getProperty("offsetcardinalx")));
			Globals.CURR_DUNGEON.setOffsetCardinalY(Integer.parseInt(dungeonProperties.getProperty("offsetcardinaly")));
			Globals.CURR_DUNGEON.setSizeCardinalX(Integer.parseInt(dungeonProperties.getProperty("sizecardinalx")));
			Globals.CURR_DUNGEON.setSizeCardinalY(Integer.parseInt(dungeonProperties.getProperty("sizecardinaly")));
			//   text mappings
			Globals.CURR_DUNGEON.setTextMappings(getModulePath() + "textmappings.properties");
			//   level names
			for(int i = 0; i < Globals.MAX_LEVEL_COUNT; i++)
			{
				String dKey = "lvl" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelName(i, dungeonProperties.getProperty(dKey));
				}
			}
			//   level graphics
			for(int i = 0; i < Globals.MAX_LEVEL_COUNT; i++)
			{
				String dKey = "lvlgrfx" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelGraphic(i, dungeonProperties.getProperty(dKey));
				}
			}
			//   game music
			for(int i = 0; i < Globals.MAX_LEVEL_COUNT; i++)
			{
				String dKey = "lvlmusic" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.containsKey(dKey))
				{
					Globals.CURR_DUNGEON.setLevelMusic(i, dungeonProperties.getProperty(dKey));
				}
			}
			//   sounds & themes
			int soundcount = 0;
			boolean soundsearch = true;
			while(soundsearch)
			{
				soundsearch = false;
				if(dungeonProperties.getProperty("sndmonster" + soundcount) != null)
				{
					Globals.CURR_DUNGEON.addMonsterSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("sndmonster" + soundcount)), soundcount);
					soundsearch = true;
				}
				if(dungeonProperties.getProperty("sndweapon" + soundcount) != null)
				{
					Globals.CURR_DUNGEON.addWeaponSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("sndweapon" + soundcount)), soundcount);
					soundsearch = true;
				}
				if(dungeonProperties.getProperty("combatmusic" + soundcount) != null)
				{
					Globals.CURR_DUNGEON.addCombatMusic(dungeonProperties.getProperty("combatmusic" + soundcount));
					soundsearch = true;
				}
				soundcount++;
			}
			if(dungeonProperties.getProperty("deathmusic") != null)
			{
				Globals.CURR_DUNGEON.setDeathMusic(dungeonProperties.getProperty("deathmusic"));
			}
			// get environmental sounds
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_MISS) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_MISS)), Globals.SOUND_MISS); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_STEP1) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_STEP1)), Globals.SOUND_STEP1); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_STEP2) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_STEP2)), Globals.SOUND_STEP2); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_DOOR1) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_DOOR1)), Globals.SOUND_DOOR1); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_DOOR2) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_DOOR2)), Globals.SOUND_DOOR2); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_STAIR1) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_STAIR1)), Globals.SOUND_STAIR1); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_STAIR2) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_STAIR2)), Globals.SOUND_STAIR2); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_MAGIC1) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_MAGIC1)), Globals.SOUND_MAGIC1); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_MAGIC2) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_MAGIC2)), Globals.SOUND_MAGIC2); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_VAULTSH) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_VAULTSH)), Globals.SOUND_VAULTSH); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_MENU) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_MENU)), Globals.SOUND_MENU); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_CHEST) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_CHEST)), Globals.SOUND_CHEST); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_VAULT) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_VAULT)), Globals.SOUND_VAULT); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_FOUNT) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_FOUNT)), Globals.SOUND_FOUNT); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_STATUE) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_STATUE)), Globals.SOUND_STATUE); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_GET) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_GET)), Globals.SOUND_GET); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_DROP) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_DROP)), Globals.SOUND_DROP); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_DESTROY) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_DESTROY)), Globals.SOUND_DESTROY); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_SHOP) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_SHOP)), Globals.SOUND_SHOP); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_DEATH1) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_DEATH1)), Globals.SOUND_DEATH1); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_DEATH2) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_DEATH2)), Globals.SOUND_DEATH2); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_QUEST1) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_QUEST1)), Globals.SOUND_QUEST1); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_QUEST2) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_QUEST2)), Globals.SOUND_QUEST2); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_DEFEAT) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_DEFEAT)), Globals.SOUND_DEFEAT); }
			if(dungeonProperties.getProperty("snd" + Globals.SOUND_VICTORY) != null) { Globals.CURR_DUNGEON.addEnvironSound(ObjectParser.loadModuleSound(dungeonProperties.getProperty("snd" + Globals.SOUND_VICTORY)), Globals.SOUND_VICTORY); }
		}
		catch(Exception e)
		{
			System.out.println("Exception loading module dungeon properties : ");
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}

	/* Parse character classes & color data */
	public static void parseCharacterDefinitionFile()
	{
		Vector<CharacterClass> vcTemp = new Vector<CharacterClass>();
		Vector<String> vcColorNames   = new Vector<String>();
		Vector<String> vcColorAssocs  = new Vector<String>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(getModulePath() + "chardefs" + Globals.DATAEXTN));
			int classCount = 0;
			int colorCount = 0;
			String lineIn = "";
			char classKey = '*';
			String className = "";
			int levelUp = 0;
			int baseHP = 0;
			int gainHP = 0;
			int baseSpeed = 0;
			int baseLuck  = 0;
			int baseMelee = 0;
			int baseRange = 0;
			int baseArmor = 0;
			int baseResist = 0;
			int baseTrapRs = 0;
			boolean isSoloChar = false;
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					/* this is a comment line */;
				}
				else if(lineIn.startsWith(FILEKEY_CHARCLASS)) // character class data
				{
					if(classCount < Globals.MAX_CHAR_CLASSES)
					{
						lineIn = lineIn.substring(3, lineIn.length());
						vcTemp.add(CharacterClass.createFromString(lineIn));
						classCount++;
					}
				}
				else if(lineIn.startsWith(FILEKEY_CHARDSTNG)) // character color data
				{
					if(colorCount < Globals.MAX_CHAR_CLASSES)
					{
						lineIn = lineIn.substring(3, lineIn.length());
						StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
						vcColorNames.add(stParse.nextToken());
						vcColorAssocs.add(stParse.nextToken());
						colorCount++;
					}
				}
				else if(lineIn.startsWith(FILEKEY_CHARASSOC)) // character color association descriptor
				{
					Globals.CHARACTER_DIFF_DESC = lineIn.substring(3, lineIn.length());
				}
			} while (lineIn != null);
			br.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error initialising from file (" + "chardefs" + Globals.DATAEXTN + ") : "  + ioe.getMessage());
			System.exit(1);
		}
		if(vcTemp.size() > 0)
		{
			Globals.CHARACTER_CLASSES = new CharacterClass[vcTemp.size()];
			for(int i = 0; i < vcTemp.size(); i++)
			{
				Globals.CHARACTER_CLASSES[i] = new CharacterClass((CharacterClass)(vcTemp.elementAt(i)));
			}
		}
		if(vcColorNames.size() > 0)
		{
			Globals.CHARACTER_DIFF_KEYS   = new String[vcColorNames.size()];
			Globals.CHARACTER_DIFF_ASSOCS = new String[vcColorAssocs.size()];
			for(int i = 0; i < vcColorNames.size(); i++)
			{
				Globals.CHARACTER_DIFF_KEYS[i] = new String(vcColorNames.elementAt(i));
			}
			for(int i = 0; i < vcColorAssocs.size(); i++)
			{
				Globals.CHARACTER_DIFF_ASSOCS[i] = new String(vcColorAssocs.elementAt(i));
			}
		}
	}

	public static void parseFloorplanTemplates()
	{
		Globals.PLANBOOK.clear();
		int roomWidth         = 0;
		int roomHeight        = 0;
		char[][] template     = new char[roomHeight][roomWidth];
		Coord cpAreaStart = (Coord)null;
		Coord cpAreaEnd   = (Coord)null;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(getModulePath() + "floorplans" + Globals.DATAEXTN));
			String lineIn = new String();
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// ignore comment field
				}
				else
				{
					// read room layout x/y values
					if(lineIn == null)
					{
						System.out.println("ERROR: Room template file is empty");
						System.exit(2);
					}
					else if(lineIn.indexOf(":") < 1)
					{
						System.out.println("ERROR: Room template does not seem to have size pair as second line");
						System.exit(1);
					}
					try
					{
						roomWidth  = Integer.parseInt(lineIn.substring(0, lineIn.indexOf(":")));
						roomHeight = Integer.parseInt(lineIn.substring(lineIn.indexOf(":") + 1, lineIn.length()));
					}
					catch(Exception e)
					{
						System.out.println("ERROR: Room template size pair incorrectly formatted");
						System.out.println(e.getMessage());
						System.exit(2);
					}
					if(roomWidth < 1 || roomHeight < 1)
					{
						System.out.println("ERROR: Room X:Y size not positive, non-zero dimensions");
						System.exit(1);
					}
					// read room "in the room" area x/y start/end values
					lineIn = br.readLine();
					if(lineIn == null)
					{
						System.out.println("ERROR: Room template file is missing data");
						System.exit(2);
					}
					else if(lineIn.indexOf("|") < 1)
					{
						System.out.println("ERROR: Room template does not seem to have area pair as third line");
						System.exit(1);
					}
					try
					{
						String startPair = lineIn.substring(0, lineIn.indexOf("|"));
						String endPair   = lineIn.substring(lineIn.indexOf("|") + 1, lineIn.length());
						cpAreaStart      = new Coord(Integer.parseInt(startPair.substring(0, startPair.indexOf(":"))), Integer.parseInt(startPair.substring(startPair.indexOf(":") + 1, startPair.length())));
						cpAreaEnd        = new Coord(Integer.parseInt(endPair.substring(0, endPair.indexOf(":"))), Integer.parseInt(endPair.substring(endPair.indexOf(":") + 1, endPair.length())));
					}
					catch(Exception e)
					{
						System.out.println("ERROR: Room template area pairs incorrectly formatted");
						System.exit(2);
					}
					// load actual template
					template = new char[roomHeight][roomWidth];
					int counter = 0;
					for(int y = 0; y < roomHeight; y++)
					{
						lineIn = br.readLine();
						if(lineIn == null)
						{
							System.out.println("ERROR: Insufficient data file lines for template");
							System.exit(2);
						}
						if(lineIn.length() < template[y].length)
						{
							System.out.println("ERROR: Data file line " + (counter + 1) + " is too short for template");
							System.exit(2);
						}
						for(int x = 0; x < template[y].length; x++)
						{
							template[y][x] = lineIn.charAt(x);
						}
					}
					Globals.PLANBOOK.add(new Floorplan(template, cpAreaStart, cpAreaEnd));
				}
			} while (lineIn != null);
			br.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error initialising from file (" + "floorplans" + Globals.DATAEXTN + ") : "  + ioe.getMessage());
			System.exit(1);
		}
	}

	public static void parseMonsterDefinitions()
	{
		Globals.BESTIARY.clear();
		Globals.INDEX_BESTIARY.clear();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(getModulePath() + "monsterdefs" + Globals.DATAEXTN));
			String lineIn = "";
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// comment line, do not parse
				}
				else if(lineIn.startsWith(FILEKEY_MONSTERDEF) || lineIn.startsWith(FILEKEY_MONSTERUNIQ))
				{
					// MonsterDef data
					boolean unique = lineIn.startsWith(FILEKEY_MONSTERUNIQ);
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String name           = stParse.nextToken();
					int level             = Integer.parseInt(stParse.nextToken());
					int defense           = Integer.parseInt(stParse.nextToken());
					int attack            = Integer.parseInt(stParse.nextToken());
					int maxDmg            = Integer.parseInt(stParse.nextToken());
					int resistance        = Integer.parseInt(stParse.nextToken());
					int mobility          = Integer.parseInt(stParse.nextToken());
					int negotiation       = Integer.parseInt(stParse.nextToken());
					int speed             = Integer.parseInt(stParse.nextToken());
					int expGrant          = Integer.parseInt(stParse.nextToken());
					int maxGroup          = Integer.parseInt(stParse.nextToken());
					int sound             = Integer.parseInt(stParse.nextToken());
					int effectPer         = Integer.parseInt(stParse.nextToken());
					boolean effectRanged  = stParse.nextToken().startsWith("Y");
					String fxblock        = stParse.nextToken();
					Effect effect         = Effect.createFromString(fxblock.substring(fxblock.indexOf("(") + 1, fxblock.indexOf(")")));
					String imageName      = name.toLowerCase().replaceAll(" ", "_");
					imageName             = imageName.replaceAll("'", "");
					Image imgNormal       = loadModuleImage("monster_" + imageName + "_normal" + Globals.IMAGEXTN);
					Image imgAttack       = loadModuleImage("monster_" + imageName + "_attack" + Globals.IMAGEXTN);
					Image imgEffect       = loadModuleImage("monster_" + imageName + "_effect" + Globals.IMAGEXTN);
					if(unique)
					{
						String mobtype = stParse.nextToken();
						Globals.BESTIARY.put(name, new MonsterDef(name, level, defense, attack, maxDmg, resistance, mobility, negotiation, speed, expGrant, maxGroup, sound, true, mobtype, effect, effectPer, effectRanged, imgNormal, imgAttack, imgEffect));
					}
					else
					{
						Globals.BESTIARY.put(name, new MonsterDef(name, level, defense, attack, maxDmg, resistance, mobility, negotiation, speed, expGrant, maxGroup, sound, effect, effectPer, effectRanged, imgNormal, imgAttack, imgEffect));
					}
					Globals.INDEX_BESTIARY.add(name);
				}
				else
				{
					// unknown file entry
				}
			} while (lineIn != null);
			br.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading monster definitions from file (" + "monsterdefs" + Globals.DATAEXTN + ") : "  + ioe.getMessage());
			System.exit(1);
		}
		catch(NumberFormatException nfe)
		{
			System.out.println("Error loading monster definitions from file (" + "monsterdefs" + Globals.DATAEXTN + ") : "  + nfe.getMessage());
			System.exit(1);
		}
	}

	public static void parseItems()
	{
		Globals.CATALOGUE.clear();
		Globals.COLLECTUM.clear();
		Globals.ARSENAL.clear();
		Globals.ARMOURY.clear();
		Globals.AMMOLIST.clear();
		Globals.FOUNTFX.clear();
		Globals.TRAPDEX.clear();
		Globals.TRAPODDS.clear();
		Globals.INDEX_CATALOGUE.clear();
		Globals.INDEX_COLLECTUM.clear();
		Globals.INDEX_ARSENAL.clear();
		Globals.INDEX_ARMOURY.clear();
		Globals.INDEX_AMMOLIST.clear();
		Vector vcItemClasses = new Vector();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(getModulePath() + "items" + Globals.DATAEXTN));
			String lineIn = "";
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// comment line, do not parse
				}
				else if(lineIn.startsWith(FILEKEY_WEAPON))
				{
					// Weapon object definition
					String weaponCategory = lineIn.substring(0, 3);
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String  name       = stParse.nextToken();
					int     baseDmg    = Integer.parseInt(stParse.nextToken());
					int     cost       = Integer.parseInt(stParse.nextToken());
					boolean ranged     = stParse.nextToken().toUpperCase().equals("Y");
					int     ammo       = Integer.parseInt(stParse.nextToken());
					String  permits    = stParse.nextToken();
					boolean throwable  = stParse.nextToken().toUpperCase().equals("Y");
					int     soundNum   = Integer.parseInt(stParse.nextToken());
					String  projectile = stParse.nextToken();
					int     projNum    = Globals.PROJECTILE_NONE;
					Color   projClr    = Globals.PROJCLR_OUTLINE;
					if(!(projectile.equals("N")) && projectile.indexOf("|") > 0)
					{
						projNum = Integer.parseInt(projectile.substring(0, projectile.indexOf("|")));
						projClr = new Color(Integer.parseInt(projectile.substring(projectile.indexOf("|") + 1), 16));
					}
					String  imageBase  = stParse.nextToken();
					Image   imgNormal  = loadModuleImage(imageBase + Globals.IMAGEXTN);
					Image   imgAttack  = loadModuleImage(imageBase + "_attack" + Globals.IMAGEXTN);
					if(weaponCategory.equals(FILEKEY_WEAPON_NORM))
					{
						// Normal Weapon object definition
						Globals.ARSENAL.put(name, new Weapon(name, baseDmg, cost, ranged, ammo, permits, throwable, imgNormal, imgAttack, projNum, projClr, soundNum));
						Globals.INDEX_ARSENAL.add(name);
					}
					else if(weaponCategory.equals(FILEKEY_WEAPON_DEF))
					{
						// Default Weapon object definition
						Globals.WEAPON_DEFAULT = new Weapon(name, baseDmg, cost, ranged, ammo, permits, throwable, imgNormal, imgAttack, projNum, projClr, soundNum);
					}
					else if(weaponCategory.equals(FILEKEY_WEAPON_LGND))
					{
						// Legendary Weapon object definition (unique)
						Globals.ARSENAL.put(name, new Weapon(name, baseDmg, cost, ranged, ammo, permits, throwable, imgNormal, imgAttack, projNum, projClr, soundNum, true));
						Globals.INDEX_ARSENAL.add(name);
					}
				}
				else if(lineIn.startsWith(FILEKEY_ARMOR))
				{
					// Armor object definition
					String armorCategory = lineIn.substring(0, 3);
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String name        = stParse.nextToken();
					int baseProt       = Integer.parseInt(stParse.nextToken());
					int cost           = Integer.parseInt(stParse.nextToken());
					boolean bodyarmor  = stParse.nextToken().toUpperCase().equals("Y");
					String permits     = stParse.nextToken();
					boolean changeable = stParse.nextToken().toUpperCase().equals("Y");
					Image imgNormal    = loadModuleImage(stParse.nextToken());
					if(armorCategory.equals(FILEKEY_ARMOR_NORM))
					{
						// Normal Armor object definition
						Globals.ARMOURY.put(name, new Armor(name, baseProt, cost, bodyarmor, permits, changeable, imgNormal));
						Globals.INDEX_ARMOURY.add(name);
					}
					else if(armorCategory.equals(FILEKEY_ARMOR_DEF))
					{
						// Default Armor object definitions
						if(bodyarmor)
						{
							Globals.ARMOR_BODY_DEFAULT = new Armor(name, baseProt, cost, bodyarmor, permits, changeable, imgNormal);
						}
						else
						{
							Globals.ARMOR_SPEC_DEFAULT = new Armor(name, baseProt, cost, bodyarmor, permits, changeable, imgNormal);
						}
					}
					else if(armorCategory.equals(FILEKEY_ARMOR_LGND))
					{
						// Legendary Armor object definition (unique)
						Globals.ARMOURY.put(name, new Armor(name, baseProt, cost, bodyarmor, permits, changeable, imgNormal, true));
						Globals.INDEX_ARMOURY.add(name);
					}
				}
				else if(lineIn.startsWith(FILEKEY_AMMO_ITEM))
				{
					// Ammo object definition
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String name = stParse.nextToken();
					int    cost = Integer.parseInt(stParse.nextToken());
					int    qnty = Integer.parseInt(stParse.nextToken());
					Globals.AMMOLIST.add(new Ammo(name, cost, qnty));
					Globals.INDEX_AMMOLIST.add(name);
				}
				else if(lineIn.startsWith(FILEKEY_ITEMCLASS))
				{
					// Universal ItemClass entry
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String globalClass = stParse.nextToken();
					String classKey    = stParse.nextToken();
					String name        = stParse.nextToken();
					String permissions = stParse.nextToken();
					Image  imgPicture  = loadModuleImage(stParse.nextToken());
					Image  imgIcon     = loadModuleImage(stParse.nextToken());
					int    sortSeq     = Integer.parseInt(stParse.nextToken());
					if(globalClass.equals("MAP"))
					{
						Globals.ITEMCLASS_MAP = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
					else if(globalClass.equals("CURRENCY"))
					{
						Globals.ITEMCLASS_CURRENCY = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
					else if(globalClass.equals("RATION"))
					{
						Globals.ITEMCLASS_RATION = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
					else if(globalClass.equals("WPNMELEE"))
					{
						Globals.ITEMCLASS_WPNMEL = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
					else if(globalClass.equals("WPNRANGE"))
					{
						Globals.ITEMCLASS_WPNRNG = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
					else if(globalClass.equals("ARMRBODY"))
					{
						Globals.ITEMCLASS_ARMBDY = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
					else if(globalClass.equals("ARMRSPEC"))
					{
						Globals.ITEMCLASS_ARMSPC = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
					else if(globalClass.equals("AMMO"))
					{
						Globals.ITEMCLASS_AMMO = new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq);
					}
				}
				else if(lineIn.startsWith(FILEKEY_ITEM_UNIV))
				{
					// Universal Item entry
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String classKey = stParse.nextToken();
					String name     = stParse.nextToken();
					if(classKey.equals(Globals.ITEMCLASS_MAP.getClassKey()))
					{
						Globals.ITEM_MAP = new Item(Globals.ITEMTYPE_MAP, Globals.ITEMCLASS_MAP, name, 1, Globals.EFFECT_SHOW_MAP, 0, true, true, 0, true);
					}
					else if(classKey.equals(Globals.ITEMCLASS_CURRENCY.getClassKey()))
					{
						Globals.ITEM_CURRENCY = new Item(Globals.ITEMTYPE_CURRENCY, Globals.ITEMCLASS_CURRENCY, name, 1, Globals.EFFECT_CRCY_GAIN, 0, true, true, 0, true);
					}
					else if(classKey.equals(Globals.ITEMCLASS_RATION.getClassKey()))
					{
						Globals.ITEM_RATION = new Item(Globals.ITEMTYPE_RATIONS, Globals.ITEMCLASS_RATION, name, 1, Globals.EFFECT_RTNS_GAIN, 0, true, true, 0, true);
					}
				}
				else if(lineIn.startsWith(FILEKEY_ITEM_MODULE))
				{
					// Module-specific ItemClass entry
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String classKey    = stParse.nextToken();
					String name        = stParse.nextToken();
					String permissions = stParse.nextToken();
					Image  imgPicture  = loadModuleImage(stParse.nextToken());
					Image  imgIcon     = loadModuleImage(stParse.nextToken());
					int    sortSeq     = Integer.parseInt(stParse.nextToken());
					Globals.INDEX_CATALOGUE.add(classKey);
					Globals.CATALOGUE.put(classKey, new ItemClass(classKey, name, permissions, imgPicture, imgIcon, sortSeq));
					Globals.COLLECTUM.put(classKey, new Hashtable<String, Item>());
				}
				else if(lineIn.startsWith(FILEKEY_ITEM))
				{
					// Module-specific Item entry
					lineIn = lineIn.substring(3, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, ",", false);
					String name     = stParse.nextToken();
					String classKey = stParse.nextToken();
					String fxstr    = stParse.nextToken();
					fxstr           = name + fxstr.substring(fxstr.indexOf("(") + 1, fxstr.indexOf(")"));
					Effect effect   = Effect.createFromString(fxstr);
					int maxCharges  = Integer.parseInt(stParse.nextToken());
					int cost        = Integer.parseInt(stParse.nextToken());
					Hashtable<String, Item> htSub = (Hashtable<String, Item>)(Globals.COLLECTUM.get(classKey));
					htSub.put(name, new Item(Globals.CATALOGUE.get(classKey), name, Math.max(maxCharges, 0), effect, effect.getPower(), (maxCharges <= 0 ? true : false), false, cost));
					Globals.INDEX_COLLECTUM.add(name);
				}
				else if(lineIn.startsWith(FILEKEY_ITEMCAP_WPN))
				{
					lineIn = lineIn.substring(4, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, "|", false);
					int shopLevel = 0;
					while(stParse.hasMoreTokens())
					{
						Globals.SHOP_CAPS_WPNS[shopLevel] = Integer.parseInt(stParse.nextToken());
						shopLevel++;
					}
				}
				else if(lineIn.startsWith(FILEKEY_ITEMCAP_ARM))
				{
					lineIn = lineIn.substring(4, lineIn.length());
					StringTokenizer stParse = new StringTokenizer(lineIn, "|", false);
					int shopLevel = 0;
					while(stParse.hasMoreTokens())
					{
						Globals.SHOP_CAPS_ARMR[shopLevel] = Integer.parseInt(stParse.nextToken());
						shopLevel++;
					}
				}
				else if(lineIn.startsWith(FILEKEY_ITEM_RATION))
				{
					// special Rations item values
					Globals.SHOP_RATIONS_QUANTITY = Integer.parseInt(lineIn.substring(3, lineIn.indexOf("|")));
					Globals.SHOP_RATIONS_COST     = Integer.parseInt(lineIn.substring(lineIn.indexOf("|") + 1, lineIn.length()));
				}
				else if(lineIn.startsWith(FILEKEY_ITEM_HEAL))
				{
					// shop healing power and cost
					Globals.EFFECT_SHOP_HEAL.setName(Globals.getDungeonText("shopHealing"));
					Globals.EFFECT_SHOP_HEAL.setPower(Integer.parseInt(lineIn.substring(3, lineIn.indexOf("|"))));
					Globals.SHOP_HEALING_COST       = Integer.parseInt(lineIn.substring(lineIn.indexOf("|") + 1));
				}
				else if(lineIn.startsWith(FILEKEY_ITEM_MODIF))
				{
					// Item Modification limiting amount
					Globals.MAXIMUM_CHANGE = Integer.parseInt(lineIn.substring(3, lineIn.length()));
				}
				else if(lineIn.startsWith(FILEKEY_TRAPDEF))
				{
					// Trap Effect object definition
					String fxstr = lineIn.substring(3, lineIn.indexOf("["));
					int    odds  = Integer.parseInt(lineIn.substring(lineIn.indexOf("[") + 1, lineIn.indexOf("]")));
					Globals.TRAPDEX.add(Effect.createFromString(fxstr));
					Globals.TRAPODDS.add(new Integer(odds));
				}
				else if(lineIn.startsWith(FILEKEY_EFFECT))
				{
					// Effect object definition
					Globals.FOUNTFX.add(Effect.createFromString(lineIn.substring(3)));
				}
				else
				{
					// unknown file entry
				}
			} while (lineIn != null);
			br.close();
			if(Globals.FOUNTFX.size() < 1)
			{
				Globals.FOUNTFX.add(new Effect(Globals.NO_EFFECT));
			}
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading item data from file (" + "items" + Globals.DATAEXTN + ") : "  + ioe.getMessage());
			System.exit(1);
		}
		catch(NumberFormatException nfe)
		{
			System.out.println("Error loading item data from file (" + "items" + Globals.DATAEXTN + ") : "  + nfe.getMessage());
			System.exit(1);
		}
	}

	public static Rectangle parseRectangle(String rcoords)
	{
		StringTokenizer stTokens = new StringTokenizer(rcoords, ",", false);
		int rx = Integer.parseInt(stTokens.nextToken());
		int ry = Integer.parseInt(stTokens.nextToken());
		int rw = Integer.parseInt(stTokens.nextToken());
		int rh = Integer.parseInt(stTokens.nextToken());
		return new Rectangle(rx, ry, rw, rh);
	}

	/* Parse name of source module from file */
	public static String getModuleName(String filename)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String lineIn = "";
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// ignore comments
				}
				else if(lineIn.startsWith(FILEKEY_MODULENAME))
				{
					return lineIn.substring(3);
				}
			}while(lineIn != null);
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading module name from file (" + filename + ") : "  + ioe.getMessage());
			return (String)null;
		}
		return (String)null;
	}

	/* Parse description of game module from file */
	public static String getModuleDescription(String filename)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String lineIn = "";
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// ignore comments
				}
				else if(lineIn.startsWith(FILEKEY_MODULEDESCR))
				{
					return lineIn.substring(3);
				}
			}while(lineIn != null);
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading module description from file (" + filename + ") : "  + ioe.getMessage());
			System.exit(1);
		}
		return new String();
	}

	/* Parse Quest objects from game module file */
	public static void parseModuleQuests()
	{
		Globals.QUESTLOG.clear();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(Globals.MODSPATH + File.separator + getModuleName() + Globals.DATAEXTN));
			String lineIn = "";
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// ignore comments
				}
				else if(lineIn.startsWith(FILEKEY_MODULEQUEST))
				{
					StringTokenizer stTokens = new StringTokenizer(lineIn.substring(3), "|", false);
					String  questName        = stTokens.nextToken();
					boolean questRequired    = stTokens.nextToken().equals("Y");
					boolean questPartial     = stTokens.nextToken().equals("Y");
					int     questTurns       = Integer.parseInt(stTokens.nextToken());
					int     questLevel       = Integer.parseInt(stTokens.nextToken());
					Coord   questRoom        = new Coord(Globals.CP_INVALID_LOC);
					String  questFixedLevel  = stTokens.nextToken();
					String  questImageName   = stTokens.nextToken();
					Image   questImage       = loadModuleImage(questImageName);
					String  questDesc        = stTokens.nextToken();
					String  questComplete    = stTokens.nextToken();
					String  questFinished    = stTokens.nextToken();
					String  questFailed      = stTokens.nextToken();
					Globals.QUESTLOG.add(new Quest(questName, questDesc, questTurns, questRequired, questPartial, questLevel, questRoom, questFixedLevel, questImage, questImageName, questComplete, questFinished, questFailed));
				}
			}while(lineIn != null);
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading module quests from file (" + Globals.MODSPATH + File.separator + getModuleName() + Globals.DATAEXTN + ") : "  + ioe.getMessage());
			System.exit(1);
		}
	}

	public static String[] getSaveDescriptor(String filename)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String lineIn = "";
			Vector vcLines = new Vector();
			int partySize = 0;
			int turns  = 0;
			int rounds = 0;
			int depth  = 0;
			int money  = 0;
			int rations = 0;
			int difficulty = Globals.DIFF_NORMAL;
			boolean completed = false;
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// ignore comments
				}
				else if(lineIn.startsWith(FILEKEY_PARTY))
				{
					Party ptTmp = Party.createFromString(lineIn.substring(3));
					depth       = ptTmp.getDepth();
					money       = ptTmp.getCurrency();
					rations     = ptTmp.getRations();
					turns       = ptTmp.getTurnsElapsed();
					rounds      = ptTmp.getRoundsElapsed();
					completed   = ptTmp.getGameCompleted();
					ptTmp       = null;
				}
				else if(lineIn.startsWith(FILEKEY_PLAYER))
				{
					partySize++;
				}
				else if(lineIn.startsWith(FILEKEY_DIFFICULTY))
				{
					difficulty = Integer.parseInt(lineIn.substring(FILEKEY_DIFFICULTY.length()));
				}
			}while(lineIn != null && !(lineIn.startsWith(FILEKEY_BOARD_NAME)));
			String[] sDesc = new String[7];
			sDesc[0] = "PLAYERS    : " + partySize;
			sDesc[1] = "TURNS      : " + turns + "." + rounds;
			sDesc[2] = "DEPTH      : " + depth;
			sDesc[3] = "CURRENCY   : " + money;
			sDesc[4] = "RATIONS    : " + rations;
			sDesc[5] = "DIFFICULTY : " + (difficulty == Globals.DIFF_EASY ? "EASY" : (difficulty == Globals.DIFF_NORMAL ? "MEDIUM" : (difficulty == Globals.DIFF_HARD ? "HARD" : "EPIC" )));
			sDesc[6] = "STATUS     : " + (completed ? "COMPLETED" : "IN PROGRESS");
			return sDesc;
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading save data from file (" + filename + ") : "  + ioe.getMessage());
			System.exit(1);
		}
		return new String[0];
	}

	public static boolean isCompletedGame(String filename)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String lineIn = "";
			Vector vcLines = new Vector();
			do
			{
				lineIn = br.readLine();
				if(lineIn == null)
				{
					break;
				}
				else if(lineIn.startsWith("*"))
				{
					// ignore comments
				}
				else if(lineIn.startsWith(FILEKEY_PARTY))
				{
					Party ptTmp = Party.createFromString(lineIn.substring(3));
					boolean cmp = ptTmp.getGameCompleted();
					ptTmp       = null;
					return cmp;
				}
			}while(lineIn != null && !(lineIn.startsWith(FILEKEY_BOARD_NAME)));
		}
		catch(IOException ioe)
		{
			System.out.println("Error loading save data from file (" + filename + ") : "  + ioe.getMessage());
			System.exit(1);
		}
		return false;
	}

	public static String zeroPad(int value, int max)
	{
		String returnVal = value + "";
		int valdigits = 1;
		while(value > 9)
		{
			valdigits++;
			value = value / 10;
		}
		int paddigits = 1;
		while(max > 9)
		{
			paddigits++;
			max = max / 10;
		}
		while(paddigits > valdigits)
		{
			returnVal = "0" + returnVal;
			paddigits--;
		}
		return returnVal;
	}

	public static void saveGameFile(String filename, String moduleName, Dungeon dungeon, Party party)
	{
		// store all data in one StringBuffer object for writing to file
		StringBuffer sbData = new StringBuffer();

		// append Module data
		sbData.append(FILEKEY_MODULENAME + moduleName + "\n");

		// append Difficulty level
		sbData.append(FILEKEY_DIFFICULTY + dungeon.getDifficulty() + "\n");

		// append Party data
		sbData.append(FILEKEY_PARTY + party.getSaveString() + "\n");

		// append Party members
		for(int i = 0; i < party.getSize(); i++)
		{
			sbData.append(FILEKEY_PLAYER + party.getPlayer(i).getSaveString() + "\n");
		}

		// append Quest data
		for(int q = 0; q < dungeon.getQuestCount(); q++)
		{
			sbData.append(FILEKEY_QUEST_SAVED + dungeon.getQuest(q).getSaveString() + "\n");
		}

		// append Levels and Rooms data
		for(int v = 0; v < dungeon.getLevelCount(); v++)
		{
			Board tmpBoard = dungeon.getLevel(v);
			sbData.append(FILEKEY_BOARD_NAME + tmpBoard.getName() + "\n");
			sbData.append(FILEKEY_LEVEL_COORD + tmpBoard.getWidth() + "/" + tmpBoard.getHeight() + "\n");
			if(tmpBoard.hasLevelGraphics())
			{
				sbData.append(FILEKEY_LEVEL_GRPHX + tmpBoard.getLevelGraphicName() + "|" + (tmpBoard.hasOpenCeiling() ? "Y" : "N") + (tmpBoard.hasOpenFloor() ? "Y" : "N") + "\n");
			}
			for(int y = 0; y < tmpBoard.getHeight(); y++)
			{
				sbData.append(FILEKEY_LEVEL_GRID);
				for(int x = 0; x < tmpBoard.getWidth(); x++)
				{
					sbData.append((tmpBoard.getGridValue(x, y) < 10 ? (char)(tmpBoard.getGridValue(x, y) + 48) : (char)(tmpBoard.getGridValue(x, y) + 55)) + "");
					sbData.append((tmpBoard.isVisited(x, y) ? Globals.MAPPED_CHAR_VISITED : (tmpBoard.isMapped(x, y) ? Globals.MAPPED_CHAR_MAPPED : (tmpBoard.isSeen(x, y) ? Globals.MAPPED_CHAR_SEEN : Globals.MAPPED_CHAR_NONE))));
				}
				sbData.append("\n");
			}
			for(Enumeration e = tmpBoard.getRooms().keys(); e.hasMoreElements();)
			{
				String roomkey = (String)(e.nextElement());
				Room tmpRoom   = tmpBoard.getRoom(roomkey);
				sbData.append(roomkey);
				sbData.append("" + tmpRoom.getStairType());
				sbData.append("" + tmpRoom.getFeatureType());
				sbData.append("" + tmpRoom.getPlanKey());
				if(tmpRoom.getMonsterCount() > 0)
				{
					int monsterindex = Globals.INDEX_BESTIARY.indexOf(tmpRoom.getMonster(0).getName());
					int oldindex = -1;
					for(int ind = 0; ind < tmpRoom.getMonsterCount(); ind++)
					{
						monsterindex = Globals.INDEX_BESTIARY.indexOf(tmpRoom.getMonster(ind).getName());
						if(monsterindex != oldindex)
						{
							int thisCount = 0;
							for(int subind = 0; subind < tmpRoom.getMonsterCount(); subind++)
							{
								if(Globals.INDEX_BESTIARY.indexOf(tmpRoom.getMonster(subind).getName()) == monsterindex)
								{
									thisCount++;
								}
							}
							sbData.append("[" + (monsterindex < 10 ? "0" : "") + monsterindex + thisCount + "]");
							oldindex = monsterindex;
						}
					}
				}
				if(tmpRoom.getItemCount() > 0)
				{
					sbData.append("{");
					for(int i = 0; i < tmpRoom.getItemCount(); i++)
					{
						if(i > 0)
						{
							sbData.append(":");
						}
						sbData.append(tmpRoom.getItem(i).getSaveString());
					}
					sbData.append("}");
				}
				if(tmpRoom.hasVault())
				{
					sbData.append(tmpRoom.getVault().getSaveString());
				}
				else if(tmpRoom.hasChest())
				{
					sbData.append(tmpRoom.getChest().getSaveString());
				}
				sbData.append("\n");
			}
			if(tmpBoard.isMapFound())
			{
				sbData.append(FILEKEY_LEVEL_MAP);
				sbData.append("\n");
			}
			sbData.append(FILEKEY_BOARD_TERM + tmpBoard.getName());
			sbData.append("\n");
		}
		// save data to file
		try
		{
			FileWriter fw = new FileWriter(Globals.SAVEPATH + filename + Globals.SAVEEXTN);
			fw.write(sbData.toString(), 0, sbData.length());
			fw.flush();
			fw.close();
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}
}
	