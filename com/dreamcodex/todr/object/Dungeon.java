package com.dreamcodex.todr.object;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.dreamcodex.todr.object.Board;
import com.dreamcodex.util.MP3Clip;

/** Dungeon
  * Class containing the master dungeon properties
  *
  * @author Howard Kistler
  */

public class Dungeon
{
	// Level Definitions
	protected int   currentLevel;
	protected int[] levelIndices;

	// Level Collection
	Vector<Board> vcLevels = new Vector<Board>();

	// Images
	protected Image[] imgFloor;
	protected Image[] imgSolid;
	protected Image[] imgColumn;
	protected Image[] imgWallH;
	protected Image[] imgWallV;
	protected Image[] imgDoorH;
	protected Image[] imgDoorV;
	protected Image[] imgStairsU;
	protected Image[] imgStairsD;
	protected Image[] imgFountain;
	protected Image[] imgStatue;
	protected Image[] imgShop;
	protected Image[] imgChest;
	protected Image[] imgVault;
	protected Image   imgHallFountain;
	protected Image   imgCurrency;
	protected Image   imgRations;
	protected Image   imgTurns;
	protected Image   imgRounds;
	protected Image   imgVaultDigits;
	protected Image   imgDeadPlayer;
	protected Image   imgDeadMonster;
	protected Image   imgMagicGood;
	protected Image   imgMagicEvil;
	protected Image   imgMagicVault;
	protected Image   imgInterface;
	protected Image   imgCardinals;
	protected Image   imgMapTiles;
	protected Image   imgBackCreate;
	protected Image   imgBackExplore;
	protected Image   imgBackParty;
	protected Image   imgBackPartyUnit;
	protected Image   imgBackPlayer;
	protected Image   imgBackMonster;
	protected Image   imgBackQuest;
	protected Image   imgBackMap;
	protected Image   imgBackShop;
	protected Image   imgBackOrder;
	protected Image   imgBackOrderCursor;
	protected Image   imgBackItemManage;
	protected Image   imgBackVault;
	protected Image   imgReticule;
	protected Image   imgHighlighter;
	protected Image   imgBattlegems;
	protected Image   imgInvNotAllowed;
	protected Image   imgInvNotUsable;
	protected Image   imgInvFull;
	protected Image   imgGameOverSlain;
	protected Image   imgGameOverFailed;
	protected Image   imgGameOverPartial;
	protected Image   imgGameOverVictory;

	// Sound Clips
	protected Hashtable<String, MP3Clip> sndEnviron = new Hashtable<String, MP3Clip>();
	protected Vector<MP3Clip>            sndMonster = new Vector<MP3Clip>();
	protected Vector<MP3Clip>            sndWeapon  = new Vector<MP3Clip>();

	// Color Definitions
	//   Hall module colors
	protected Color   clrHallBkgr       = new Color(0, 0, 0);
	protected Color   clrHallWall       = new Color(196, 196, 196);
	protected Color   clrHallFloor      = new Color(255, 160, 160);
	protected Color   clrHallCeiling    = new Color(128, 160, 255);
	protected Color   clrHallOutline    = new Color(0, 0, 0);
	protected Color   clrHallDoor       = new Color(255, 212, 164);
	protected Color[] clrLevelHallBkgr;
	protected Color[] clrLevelHallWall;
	protected Color[] clrLevelHallFloor;
	protected Color[] clrLevelHallCeiling;
	protected Color[] clrLevelHallOutline;
	protected Color[] clrLevelHallDoor;
	//   Room module colors
	protected Color clrRoomBkgr   = new Color(0, 0, 0);
	//   Map module colors
	protected Color clrMapBkgr    = new Color(255, 255, 192);
	protected Color clrMapMapped  = new Color(128, 128, 255);
	protected Color clrMapVisited = new Color(0, 0, 0);
	protected Color clrMapParty   = new Color(255, 128, 128);
	//   Info display colors
	protected Color clrMovingPartyMember = new Color(128, 255, 128);
	protected Color clrActivePartyMember = new Color(255, 128, 128);
	//   Message display colors
	protected Color clrMsgWindow  = new Color(128, 128, 128);
	//   Text box colors
	protected Color clrTextBoxOutline = new Color(0, 0, 0);
	protected Color clrTextBoxFill    = new Color(255, 255, 255);
	//   Notification box colors
	protected Color clrNotificationOutline = new Color(0, 0, 0);
	protected Color clrNotificationFill    = new Color(255, 255, 192);
	//   Damage point colors
	protected Color clrDamageOutline = new Color(0, 0, 0);
	protected Color clrDamageFill    = new Color(255, 128, 128);
	protected Color clrHealingFill   = new Color(128, 255, 128);
	//   Hitpoint display colors
	protected Color clrHPsFull = new Color(128, 255, 128);
	protected Color clrHPsSome = new Color(255, 255, 128);
	protected Color clrHPsCrit = new Color(255, 128, 128);
	protected Color clrHPsNone = new Color(128, 128, 128);
	//   Font colors
	protected Color[] colorBank =
	{
		new Color(Integer.parseInt("000000", 16)),
		new Color(Integer.parseInt("202040", 16)),
		new Color(Integer.parseInt("404080", 16)),
		new Color(Integer.parseInt("CCCCAA", 16)),
		new Color(Integer.parseInt("007700", 16)),
		new Color(Integer.parseInt("999999", 16)),
		new Color(Integer.parseInt("FF8080", 16)),
		new Color(Integer.parseInt("4080C0", 16)),
		new Color(Integer.parseInt("7F7F7F", 16)),
		new Color(Integer.parseInt("FFFFDD", 16)),
		new Color(Integer.parseInt("788050", 16))
	};
	protected Color clrMapMonster = new Color(255, 64, 64);

	// Level Keys
	protected String[] levelNames;
	protected String[] levelGraphics;
	protected String[] levelMusic;

	// Text Mappings
	protected Properties textMappings = new Properties();

	// Other variables
	protected double  shadeStrength = 30.0;
	protected boolean bOutline      = false;
	protected int offsetCardinalX   = ((Globals.CANVAS_HALL_X * Globals.TILESCALE) / 2) - (Globals.TILESCALE / 2);
	protected int offsetCardinalY   = Globals.TILESCALE;
	protected int sizeCardinalX     = Globals.TILESCALE;
	protected int sizeCardinalY     = Globals.TILESCALE;

	protected int levelWidth  = 28;
	protected int levelHeight = 19;
	protected int roomsMin    = 12;
	protected int roomsMax    = 24;
	protected int difficulty  = Globals.DIFF_NORMAL;
	protected int monsterOdds = 66;
	protected int itemOdds    = 33;
	protected int chestOdds   = 50;
	protected int vaultOdds   = 50;
	protected int trapOdds    = 25;
	protected int wanderOdds  = 1;

	protected int[] fountainLevelOdds = new int[Globals.MAX_LEVEL_COUNT];
	protected int   fountainBestOdds  = 100;
	protected int   fountainWorstOdds = 0;

	protected int partyIfaceX = 47;
	protected int partyIfaceY = 4;
	protected int levelIfaceX = 420;
	protected int levelIfaceY = 476;
	protected int monstIfaceX = 186;
	protected int monstIfaceY = 476;
	protected int vaultCharX  = 12;
	protected int vaultCharY  = 20;
	protected int vaultHPsX   = 12;
	protected int vaultHPsY   = 23;

	protected Rectangle notificationBox = new Rectangle(43,  4, 18, 20);
	protected Rectangle fileListBox     = new Rectangle(10,  5, 17, 10);
	protected Rectangle fileInfoBox     = new Rectangle(29,  5, 42, 10);
	protected Rectangle playerInfoBox   = new Rectangle(43,  4, 18, 20);
	protected Rectangle monsterInfoBox  = new Rectangle(43,  4, 18, 20);
	protected Rectangle itemManageBox   = new Rectangle( 7,  4, 17, 20);
	protected Rectangle itemPlayerBox   = new Rectangle(26,  4, 17, 20);
	protected Rectangle shoppingBox     = new Rectangle(24,  5, 17, 10);
	protected Rectangle questListBox    = new Rectangle( 1,  5, 17, 10);
	protected Rectangle questTurnBox    = new Rectangle(21,  5,  4, 10);
	protected Rectangle questInfoBox    = new Rectangle(28,  5, 45, 10);
	protected Rectangle doorBounds      = new Rectangle(14, 14,  8,  8);

	protected Vector<Quest> questLog = new Vector<Quest>();

	protected Vector<String> musicCombat = new Vector<String>();

	protected String musicDeath = (String)null;

	public Dungeon(int indices)
	{
		levelIndices  = new int[Globals.MAX_LEVEL_COUNT];
		levelNames    = new String[Globals.MAX_LEVEL_COUNT];
		levelGraphics = new String[Globals.MAX_LEVEL_COUNT];
		levelMusic    = new String[Globals.MAX_LEVEL_COUNT];
		for(int i = 0; i < Globals.MAX_LEVEL_COUNT; i++)
		{
			levelIndices[i]  = 0;
			levelNames[i]    = "LEVEL " + i;
			levelGraphics[i] = Globals.NO_LVLGRFX;
			levelMusic[i]    = Globals.NO_MUSIC;
		}
		imgFloor = new Image[indices];
		imgSolid = new Image[indices];
		imgColumn = new Image[indices];
		imgWallH = new Image[indices];
		imgWallV = new Image[indices];
		imgDoorH = new Image[indices];
		imgDoorV = new Image[indices];
		imgStairsU = new Image[indices];
		imgStairsD = new Image[indices];
		imgFountain = new Image[indices];
		imgStatue = new Image[indices];
		imgShop = new Image[indices];
		imgChest = new Image[indices];
		imgVault = new Image[indices];
		clrLevelHallBkgr = new Color[Globals.MAX_LEVEL_COUNT];
		clrLevelHallWall = new Color[Globals.MAX_LEVEL_COUNT];
		clrLevelHallFloor = new Color[Globals.MAX_LEVEL_COUNT];
		clrLevelHallCeiling = new Color[Globals.MAX_LEVEL_COUNT];
		clrLevelHallOutline = new Color[Globals.MAX_LEVEL_COUNT];
		clrLevelHallDoor = new Color[Globals.MAX_LEVEL_COUNT];
	}

	public int  getCurrentLevel()        { return currentLevel; }
	public void setCurrentLevel(int lvl) { currentLevel = lvl; }

	public int[] getLevelIndices()           { return levelIndices; }
	public void  setLevelIndices(int[] vals) { levelIndices = vals; }

	public int  getLevelIndex(int index)          { return levelIndices[index]; }
	public void setLevelIndex(int index, int val) { levelIndices[index] = val; }

	public Vector getLevels()          { return vcLevels; }
	public void   setLevels(Vector<Board> vc) { vcLevels = new Vector<Board>(vc); }
	public int    getLevelCount()      { return vcLevels.size(); }
	public void   clearLevels()        { vcLevels.removeAllElements(); }

	public Board getLevel(int index)          { return vcLevels.elementAt(index); }
	public void  setLevel(Board b, int index) { vcLevels.setElementAt(b, index); }

	public int getLevelIndexByName(String levelname)
	{
		int iReturn = -1;
		for(int i = 0; i < levelNames.length; i++)
		{
			if(levelNames[i].equals(levelname))
			{
				iReturn = i;
			}
		}
		return iReturn;
	}

	// dungeon settings

	public int  getLevelWidth()       { return levelWidth; }
	public int  getLevelHeight()      { return levelHeight; }
	public int  getRoomsMin()         { return roomsMin; }
	public int  getRoomsMax()         { return roomsMax; }
	public void setLevelWidth(int i)  { if(i > Globals.MAX_LEVEL_WIDTH)  { levelWidth = Globals.MAX_LEVEL_WIDTH; }   else { levelWidth = i; } }
	public void setLevelHeight(int i) { if(i > Globals.MAX_LEVEL_HEIGHT) { levelHeight = Globals.MAX_LEVEL_HEIGHT; } else { levelHeight = i; } }
	public void setRoomsMin(int i)    { if(i < Globals.MIN_ROOMS)        { roomsMin = Globals.MIN_ROOMS; }           else { roomsMin = i; } }
	public void setRoomsMax(int i)    { if(i > Globals.MAX_ROOMS)        { roomsMax = Globals.MAX_ROOMS; }           else { roomsMax = i; } }

	// chance percentiles

	public int  getMonsterOdds()      { return monsterOdds; }
	public int  getItemOdds()         { return itemOdds; }
	public int  getChestOdds()        { return chestOdds; }
	public int  getVaultOdds()        { return vaultOdds; }
	public int  getTrapOdds()         { return trapOdds; }
	public int  getWanderOdds()       { return wanderOdds; }

	public void setMonsterOdds(int i) { monsterOdds = (i < 0 ? 0 : (i > 100 ? 100 : i)); }
	public void setItemOdds(int i)    { itemOdds    = (i < 0 ? 0 : (i > 100 ? 100 : i)); }
	public void setChestOdds(int i)   { chestOdds   = (i < 0 ? 0 : (i > 100 ? 100 : i)); }
	public void setVaultOdds(int i)   { vaultOdds   = (i < 0 ? 0 : (i > 100 ? 100 : i)); }
	public void setTrapOdds(int i)    { trapOdds    = (i < 0 ? 0 : (i > 100 ? 100 : i)); }
	public void setWanderOdds(int i)  { wanderOdds  = (i < 0 ? 0 : (i > 100 ? 100 : i)); }

	public int[] getFountainOdds()                    { return fountainLevelOdds; }
	public int   getFountainOdds(int index)           { return fountainLevelOdds[index]; }
	public int   getFountainBest()                    { return fountainBestOdds; }
	public int   getFountainWorst()                   { return fountainWorstOdds; }
	public void  setFountainOdds(int index, int odds) { fountainLevelOdds[index] = odds; }
	public void  setFountainOdds(int[] odds)          { fountainLevelOdds = odds; }
	public void  setFountainBest(int odds)            { fountainBestOdds = odds; }
	public void  setFountainWorst(int odds)           { fountainWorstOdds = odds; }

	// dungeon difficulty setting

	public int  getDifficulty()      { return difficulty; }
	public void setDifficulty(int i) { difficulty  = i; }

	// dungeon rendering settings

	public double getShadeStrength()           { return shadeStrength; }
	public void   setShadeStrength(double val) { shadeStrength = val; }

	public boolean getOutlinesShown()          { return bOutline; }
	public void    setOutlinesShown(boolean b) { bOutline = b; }

	// display offsets

	public int  getOffsetCardinalX()      { return offsetCardinalX; }
	public int  getOffsetCardinalY()      { return offsetCardinalY; }
	public int  getSizeCardinalX()        { return sizeCardinalX; }
	public int  getSizeCardinalY()        { return sizeCardinalY; }
	public void setOffsetCardinalX(int x) { offsetCardinalX = x; }
	public void setOffsetCardinalY(int y) { offsetCardinalY = y; }
	public void setSizeCardinalX(int x)   { sizeCardinalX   = x; }
	public void setSizeCardinalY(int y)   { sizeCardinalY   = y; }

	// other setting variables

	public int  getPartyInterfaceX()      { return partyIfaceX; }
	public int  getPartyInterfaceY()      { return partyIfaceY; }
	public int  getLevelInterfaceX()      { return levelIfaceX; }
	public int  getLevelInterfaceY()      { return levelIfaceY; }
	public int  getMonstInterfaceX()      { return monstIfaceX; }
	public int  getMonstInterfaceY()      { return monstIfaceY; }
	public int  getVaultCharX()           { return vaultCharX; }
	public int  getVaultCharY()           { return vaultCharY; }
	public int  getVaultHPsX()            { return vaultHPsX; }
	public int  getVaultHPsY()            { return vaultHPsY; }
	public void setPartyInterfaceX(int x) { partyIfaceX = x; }
	public void setPartyInterfaceY(int y) { partyIfaceY = y; }
	public void setLevelInterfaceX(int x) { levelIfaceX = x; }
	public void setLevelInterfaceY(int y) { levelIfaceY = y; }
	public void setMonstInterfaceX(int x) { monstIfaceX = x; }
	public void setMonstInterfaceY(int y) { monstIfaceY = y; }
	public void setVaultCharX(int x)      { vaultCharX = x; }
	public void setVaultCharY(int y)      { vaultCharY = y; }
	public void setVaultHPsX(int x)       { vaultHPsX  = x; }
	public void setVaultHPsY(int y)       { vaultHPsY  = y; }

	// text boxes

	public Rectangle getNotificationBox() { return notificationBox; }
	public int       getNotificationCol() { return notificationBox.x; }
	public int       getNotificationRow() { return notificationBox.y; }
	public int       getNotificationWid() { return notificationBox.width; }
	public int       getNotificationHgt() { return notificationBox.height; }

	public Rectangle getFileListBox() { return fileListBox; }
	public int       getFileListCol() { return fileListBox.x; }
	public int       getFileListRow() { return fileListBox.y; }
	public int       getFileListWid() { return fileListBox.width; }
	public int       getFileListHgt() { return fileListBox.height; }

	public Rectangle getFileInfoBox() { return fileInfoBox; }
	public int       getFileInfoCol() { return fileInfoBox.x; }
	public int       getFileInfoRow() { return fileInfoBox.y; }
	public int       getFileInfoWid() { return fileInfoBox.width; }
	public int       getFileInfoHgt() { return fileInfoBox.height; }

	public Rectangle getPlayerInfoBox() { return playerInfoBox; }
	public int       getPlayerInfoCol() { return playerInfoBox.x; }
	public int       getPlayerInfoRow() { return playerInfoBox.y; }
	public int       getPlayerInfoWid() { return playerInfoBox.width; }
	public int       getPlayerInfoHgt() { return playerInfoBox.height; }

	public Rectangle getMonsterInfoBox() { return monsterInfoBox; }
	public int       getMonsterInfoCol() { return monsterInfoBox.x; }
	public int       getMonsterInfoRow() { return monsterInfoBox.y; }
	public int       getMonsterInfoWid() { return monsterInfoBox.width; }
	public int       getMonsterInfoHgt() { return monsterInfoBox.height; }

	public Rectangle getItemSourceBox() { return itemManageBox; }
	public int       getItemSourceCol() { return itemManageBox.x; }
	public int       getItemSourceRow() { return itemManageBox.y; }
	public int       getItemSourceWid() { return itemManageBox.width; }
	public int       getItemSourceHgt() { return itemManageBox.height; }

	public Rectangle getItemTargetBox() { return itemPlayerBox; }
	public int       getItemTargetCol() { return itemPlayerBox.x; }
	public int       getItemTargetRow() { return itemPlayerBox.y; }
	public int       getItemTargetWid() { return itemPlayerBox.width; }
	public int       getItemTargetHgt() { return itemPlayerBox.height; }

	public Rectangle getShoppingBox() { return shoppingBox; }
	public int       getShoppingCol() { return shoppingBox.x; }
	public int       getShoppingRow() { return shoppingBox.y; }
	public int       getShoppingWid() { return shoppingBox.width; }
	public int       getShoppingHgt() { return shoppingBox.height; }
	public int       getShoppingVal() { return getShoppingCol() + Globals.MAX_ITEM_LENGTH + 4; }
	public int       getShoppingCst() { return getShoppingVal() + 6; }

	public Rectangle getQuestListBox() { return questListBox; }
	public int       getQuestListCol() { return questListBox.x; }
	public int       getQuestListRow() { return questListBox.y; }
	public int       getQuestListWid() { return questListBox.width; }
	public int       getQuestListHgt() { return questListBox.height; }

	public Rectangle getQuestTurnBox() { return questTurnBox; }
	public int       getQuestTurnCol() { return questTurnBox.x; }
	public int       getQuestTurnRow() { return questTurnBox.y; }
	public int       getQuestTurnWid() { return questTurnBox.width; }
	public int       getQuestTurnHgt() { return questTurnBox.height; }

	public Rectangle getQuestInfoBox() { return questInfoBox; }
	public int       getQuestInfoCol() { return questInfoBox.x; }
	public int       getQuestInfoRow() { return questInfoBox.y; }
	public int       getQuestInfoWid() { return questInfoBox.width; }
	public int       getQuestInfoHgt() { return questInfoBox.height; }

	public Rectangle getDoorBounds()    { return doorBounds; }
	public int       getDoorBoundsCol() { return doorBounds.x; }
	public int       getDoorBoundsRow() { return doorBounds.y; }
	public int       getDoorBoundsWid() { return doorBounds.width; }
	public int       getDoorBoundsHgt() { return doorBounds.height; }

	public void setNotificationBox(Rectangle r) { notificationBox = new Rectangle(r); }
	public void setFileListBox(Rectangle r)     { fileListBox = new Rectangle(r); }
	public void setFileInfoBox(Rectangle r)     { fileInfoBox = new Rectangle(r); }
	public void setPlayerInfoBox(Rectangle r)   { playerInfoBox = new Rectangle(r); }
	public void setMonsterInfoBox(Rectangle r)  { monsterInfoBox = new Rectangle(r); }
	public void setItemManageBox(Rectangle r)   { itemManageBox = new Rectangle(r); }
	public void setItemPlayerBox(Rectangle r)   { itemPlayerBox = new Rectangle(r); }
	public void setShoppingBox(Rectangle r)     { shoppingBox = new Rectangle(r); }
	public void setQuestListBox(Rectangle r)    { questListBox = new Rectangle(r); }
	public void setQuestTurnBox(Rectangle r)    { questTurnBox = new Rectangle(r); }
	public void setQuestInfoBox(Rectangle r)    { questInfoBox = new Rectangle(r); }
	public void setDoorBounds(Rectangle r)      { doorBounds = new Rectangle(r); }

	public String[] getLevelNames()              { return levelNames; }
	public String[] getLevelGraphics()           { return levelGraphics; }
	public String[] getLevelMusics()             { return levelMusic; }
	public void     setLevelNames(String[] s)    { levelNames    = s; }
	public void     setLevelGraphics(String[] s) { levelGraphics = s; }
	public void     setLevelMusics(String[] s)   { levelMusic    = s; }

	// level specific names & graphics & music

	public String getLevelName(int index)              { return levelNames[index]; }
	public String getLevelGraphic(int index)           { return levelGraphics[index]; }
	public String getLevelMusic(int index)             { return levelMusic[index]; }
	public void   setLevelName(int index, String s)    { levelNames[index]    = new String(s); }
	public void   setLevelGraphic(int index, String s) { levelGraphics[index] = new String(s); }
	public void   setLevelMusic(int index, String s)   { levelMusic[index]    = new String(s); }

	// color accessors

	public Color getColorHallBackground()      { return clrHallBkgr; }
	public Color getColorHallWall()            { return clrHallWall; }
	public Color getColorHallFloor()           { return clrHallFloor; }
	public Color getColorHallCeiling()         { return clrHallCeiling; }
	public Color getColorHallOutline()         { return clrHallOutline; }
	public Color getColorHallDoor()            { return clrHallDoor; }
	public Color getCurrColorHallBackground()  { if(clrLevelHallBkgr[getLevelIndex(currentLevel)] != null) { return clrLevelHallBkgr[getLevelIndex(currentLevel)]; } else { return clrHallBkgr; } }
	public Color getCurrColorHallWall()        { if(clrLevelHallWall[getLevelIndex(currentLevel)] != null) { return clrLevelHallWall[getLevelIndex(currentLevel)]; } else { return clrHallWall; } }
	public Color getCurrColorHallFloor()       { if(clrLevelHallFloor[getLevelIndex(currentLevel)] != null) { return clrLevelHallFloor[getLevelIndex(currentLevel)]; } else { return clrHallFloor; } }
	public Color getCurrColorHallCeiling()     { if(clrLevelHallCeiling[getLevelIndex(currentLevel)] != null) { return clrLevelHallCeiling[getLevelIndex(currentLevel)]; } else { return clrHallCeiling; } }
	public Color getCurrColorHallOutline()     { if(clrLevelHallOutline[getLevelIndex(currentLevel)] != null) { return clrLevelHallOutline[getLevelIndex(currentLevel)]; } else { return clrHallOutline; } }
	public Color getCurrColorHallDoor()        { if(clrLevelHallDoor[getLevelIndex(currentLevel)] != null) { return clrLevelHallDoor[getLevelIndex(currentLevel)]; } else { return clrHallDoor; } }
	public Color getColorRoomBackground()      { return clrRoomBkgr; }
	public Color getColorMapBackground()       { return clrMapBkgr; }
	public Color getColorMapMapped()           { return clrMapMapped; }
	public Color getColorMapVisited()          { return clrMapVisited; }
	public Color getColorMapParty()            { return clrMapParty; }
	public Color getColorMovingPartyMember()   { return clrMovingPartyMember; }
	public Color getColorActivePartyMember()   { return clrActivePartyMember; }
	public Color getColorMessageWindow()       { return clrMsgWindow; }
	public Color getColorTextBoxOutline()      { return clrTextBoxOutline; }
	public Color getColorTextBoxFill()         { return clrTextBoxFill; }
	public Color getColorNotificationOutline() { return clrNotificationOutline; }
	public Color getColorNotificationFill()    { return clrNotificationFill; }
	public Color getColorDamageOutline()       { return clrDamageOutline; }
	public Color getColorDamageFill()          { return clrDamageFill; }
	public Color getColorHealingFill()         { return clrHealingFill; }
	public Color getColorHitpointsFull()       { return clrHPsFull; }
	public Color getColorHitpointsSome()       { return clrHPsSome; }
	public Color getColorHitpointsCrit()       { return clrHPsCrit; }
	public Color getColorHitpointsNone()       { return clrHPsNone; }
	public Color[] getColorBanks()             { return colorBank; }
	public Color getColorBank(int index)       { return colorBank[index]; }
	public Color getColorMapMonster()          { return clrMapMonster; }

	public void setColorHallBackground(Color clr)      { clrHallBkgr = new Color(clr.getRGB()); }
	public void setColorHallWall(Color clr)            { clrHallWall = new Color(clr.getRGB()); }
	public void setColorHallFloor(Color clr)           { clrHallFloor = new Color(clr.getRGB()); }
	public void setColorHallCeiling(Color clr)         { clrHallCeiling = new Color(clr.getRGB()); }
	public void setColorHallOutline(Color clr)         { clrHallOutline = new Color(clr.getRGB()); }
	public void setColorHallDoor(Color clr)            { clrHallDoor = new Color(clr.getRGB()); }
	public void setLevelColorHallBackground(Color clr, int index) { clrLevelHallBkgr[index] = new Color(clr.getRGB()); }
	public void setLevelColorHallWall(Color clr, int index)       { clrLevelHallWall[index] = new Color(clr.getRGB()); }
	public void setLevelColorHallFloor(Color clr, int index)      { clrLevelHallFloor[index] = new Color(clr.getRGB()); }
	public void setLevelColorHallCeiling(Color clr, int index)    { clrLevelHallCeiling[index] = new Color(clr.getRGB()); }
	public void setLevelColorHallOutline(Color clr, int index)    { clrLevelHallOutline[index] = new Color(clr.getRGB()); }
	public void setLevelColorHallDoor(Color clr, int index)       { clrLevelHallDoor[index] = new Color(clr.getRGB()); }
	public void setColorRoomBackground(Color clr)      { clrRoomBkgr = new Color(clr.getRGB()); }
	public void setColorMapBackground(Color clr)       { clrMapBkgr = new Color(clr.getRGB()); }
	public void setColorMapMapped(Color clr)           { clrMapMapped = new Color(clr.getRGB()); }
	public void setColorMapVisited(Color clr)          { clrMapVisited = new Color(clr.getRGB()); }
	public void setColorMapParty(Color clr)            { clrMapParty = new Color(clr.getRGB()); }
	public void setColorMovingPartyMember(Color clr)   { clrMovingPartyMember = new Color(clr.getRGB()); }
	public void setColorActivePartyMember(Color clr)   { clrActivePartyMember = new Color(clr.getRGB()); }
	public void setColorMessageWindow(Color clr)       { clrMsgWindow = new Color(clr.getRGB()); }
	public void setColorTextBoxOutline(Color clr)      { clrTextBoxOutline = new Color(clr.getRGB()); }
	public void setColorTextBoxFill(Color clr)         { clrTextBoxFill = new Color(clr.getRGB()); }
	public void setColorNotificationOutline(Color clr) { clrNotificationOutline = new Color(clr.getRGB()); }
	public void setColorNotificationFill(Color clr)    { clrNotificationFill = new Color(clr.getRGB()); }
	public void setColorDamageOutline(Color clr)       { clrDamageOutline = new Color(clr.getRGB()); }
	public void setColorDamageFill(Color clr)          { clrDamageFill = new Color(clr.getRGB()); }
	public void setColorHealingFill(Color clr)         { clrHealingFill = new Color(clr.getRGB()); }
	public void setColorHitpointsFull(Color clr)       { clrHPsFull = new Color(clr.getRGB()); }
	public void setColorHitpointsSome(Color clr)       { clrHPsSome = new Color(clr.getRGB()); }
	public void setColorHitpointsCrit(Color clr)       { clrHPsCrit = new Color(clr.getRGB()); }
	public void setColorHitpointsNone(Color clr)       { clrHPsNone = new Color(clr.getRGB()); }
	public void setColorBanks(Color[] clrs)            { colorBank = clrs; }
	public void setColorBank(Color clr, int index)     { colorBank[index] = new Color(clr.getRGB()); }
	public void setColorMapMonster(Color clr)          { clrMapMonster = new Color(clr.getRGB()); }

	// image accessors

	public Image getImageFloor(int index)    { return imgFloor[index]; }
	public Image getImageSolid(int index)    { return imgSolid[index]; }
	public Image getImageColumn(int index)   { return imgColumn[index]; }
	public Image getImageWallH(int index)    { return imgWallH[index]; }
	public Image getImageWallV(int index)    { return imgWallV[index]; }
	public Image getImageDoorH(int index)    { return imgDoorH[index]; }
	public Image getImageDoorV(int index)    { return imgDoorV[index]; }
	public Image getImageStairsU(int index)  { return imgStairsU[index]; }
	public Image getImageStairsD(int index)  { return imgStairsD[index]; }
	public Image getImageFountain(int index) { return imgFountain[index]; }
	public Image getImageStatue(int index)   { return imgStatue[index]; }
	public Image getImageShop(int index)     { return imgShop[index]; }
	public Image getImageChest(int index)    { return imgChest[index]; }
	public Image getImageVault(int index)    { return imgVault[index]; }
	public Image getImageHallFountain()      { return imgHallFountain; }
	public Image getImageCurrency()          { return imgCurrency; }
	public Image getImageRations()           { return imgRations; }
	public Image getImageTurns()             { return imgTurns; }
	public Image getImageRounds()            { return imgRounds; }
	public Image getImageVaultDigits()       { return imgVaultDigits; }
	public Image getImageDeadPlayer()        { return imgDeadPlayer; }
	public Image getImageDeadMonster()       { return imgDeadMonster; }
	public Image getImageMagicGood()         { return imgMagicGood; }
	public Image getImageMagicEvil()         { return imgMagicEvil; }
	public Image getImageMagicVault()        { return imgMagicVault; }
	public Image getImageInterface()         { return imgInterface; }
	public Image getImageCardinals()         { return imgCardinals; }
	public Image getImageMapTiles()          { return imgMapTiles; }
	public Image getImageBackCreate()        { return imgBackCreate; }
	public Image getImageBackExplore()       { return imgBackExplore; }
	public Image getImageBackParty()         { return imgBackParty; }
	public Image getImageBackPartyUnit()     { return imgBackPartyUnit; }
	public Image getImageBackPlayer()        { return imgBackPlayer; }
	public Image getImageBackMonster()       { return imgBackMonster; }
	public Image getImageBackQuest()         { return imgBackQuest; }
	public Image getImageBackMap()           { return imgBackMap; }
	public Image getImageBackShop()          { return imgBackShop; }
	public Image getImageBackOrder()         { return imgBackOrder; }
	public Image getImageBackOrderCursor()   { return imgBackOrderCursor; }
	public Image getImageBackItemManage()    { return imgBackItemManage; }
	public Image getImageBackVault()         { return imgBackVault; }
	public Image getImageReticule()          { return imgReticule; }
	public Image getImageHighlighter()       { return imgHighlighter; }
	public Image getImageBattlegems()        { return imgBattlegems; }
	public Image getImageInvNotAllowed()     { return imgInvNotAllowed; }
	public Image getImageInvNotUsable()      { return imgInvNotUsable; }
	public Image getImageInvFull()           { return imgInvFull; }
	public Image getImageGameOverSlain()     { return imgGameOverSlain; }
	public Image getImageGameOverFailed()    { return imgGameOverFailed; }
	public Image getImageGameOverPartial()   { return imgGameOverPartial; }
	public Image getImageGameOverVictory()   { return imgGameOverVictory; }

	public void setImagesFloor(Image[] imgs)           { imgFloor = imgs; }
	public void setImagesSolid(Image[] imgs)           { imgSolid = imgs; }
	public void setImagesColumn(Image[] imgs)          { imgColumn = imgs; }
	public void setImagesWallH(Image[] imgs)           { imgWallH = imgs; }
	public void setImagesWallV(Image[] imgs)           { imgWallV = imgs; }
	public void setImagesDoorH(Image[] imgs)           { imgDoorH = imgs; }
	public void setImagesDoorV(Image[] imgs)           { imgDoorV = imgs; }
	public void setImagesStairsU(Image[] imgs)         { imgStairsU = imgs; }
	public void setImagesStairsD(Image[] imgs)         { imgStairsD = imgs; }
	public void setImagesFountain(Image[] imgs)        { imgFountain = imgs; }
	public void setImagesStatue(Image[] imgs)          { imgStatue = imgs; }
	public void setImagesShop(Image[] imgs)            { imgShop = imgs; }
	public void setImagesChest(Image[] imgs)           { imgChest = imgs; }
	public void setImagesVault(Image[] imgs)           { imgVault = imgs; }
	public void setImageFloor(Image img, int index)    { imgFloor[index] = img; }
	public void setImageSolid(Image img, int index)    { imgSolid[index] = img; }
	public void setImageColumn(Image img, int index)   { imgColumn[index] = img; }
	public void setImageWallH(Image img, int index)    { imgWallH[index] = img; }
	public void setImageWallV(Image img, int index)    { imgWallV[index] = img; }
	public void setImageDoorH(Image img, int index)    { imgDoorH[index] = img; }
	public void setImageDoorV(Image img, int index)    { imgDoorV[index] = img; }
	public void setImageStairsU(Image img, int index)  { imgStairsU[index] = img; }
	public void setImageStairsD(Image img, int index)  { imgStairsD[index] = img; }
	public void setImageFountain(Image img, int index) { imgFountain[index] = img; }
	public void setImageStatue(Image img, int index)   { imgStatue[index] = img; }
	public void setImageShop(Image img, int index)     { imgShop[index] = img; }
	public void setImageChest(Image img, int index)    { imgChest[index] = img; }
	public void setImageVault(Image img, int index)    { imgVault[index] = img; }
	public void setImageHallFountain(Image img)        { imgHallFountain = img; }
	public void setImageCurrency(Image img)            { imgCurrency = img; }
	public void setImageRations(Image img)             { imgRations = img; }
	public void setImageTurns(Image img)               { imgTurns = img; }
	public void setImageRounds(Image img)              { imgRounds = img; }
	public void setImageVaultDigits(Image img)         { imgVaultDigits = img; }
	public void setImageDeadPlayer(Image img)          { imgDeadPlayer = img; }
	public void setImageDeadMonster(Image img)         { imgDeadMonster = img; }
	public void setImageMagicGood(Image img)           { imgMagicGood = img; }
	public void setImageMagicEvil(Image img)           { imgMagicEvil = img; }
	public void setImageMagicVault(Image img)          { imgMagicVault = img; }
	public void setImageInterface(Image img)           { imgInterface = img; }
	public void setImageCardinals(Image img)           { imgCardinals = img; }
	public void setImageMapTiles(Image img)            { imgMapTiles = img; }
	public void setImageBackCreate(Image img)          { imgBackCreate = img; }
	public void setImageBackExplore(Image img)         { imgBackExplore = img; }
	public void setImageBackParty(Image img)           { imgBackParty = img; }
	public void setImageBackPartyUnit(Image img)       { imgBackPartyUnit = img; }
	public void setImageBackPlayer(Image img)          { imgBackPlayer = img; }
	public void setImageBackMonster(Image img)         { imgBackMonster = img; }
	public void setImageBackQuest(Image img)           { imgBackQuest = img; }
	public void setImageBackMap(Image img)             { imgBackMap = img; }
	public void setImageBackShop(Image img)            { imgBackShop = img; }
	public void setImageBackOrder(Image img)           { imgBackOrder = img; }
	public void setImageBackOrderCursor(Image img)     { imgBackOrderCursor = img; }
	public void setImageBackItemManage(Image img)      { imgBackItemManage = img; }
	public void setImageBackVault(Image img)           { imgBackVault = img; }
	public void setImageReticule(Image img)            { imgReticule = img; }
	public void setImageHighlighter(Image img)         { imgHighlighter = img; }
	public void setImageBattlegems(Image img)          { imgBattlegems = img; }
	public void setImageInvNotAllowed(Image img)       { imgInvNotAllowed = img; }
	public void setImageInvNotUsable(Image img)        { imgInvNotUsable = img; }
	public void setImageInvFull(Image img)             { imgInvFull = img; }
	public void setImageGameOverSlain(Image img)       { imgGameOverSlain = img; }
	public void setImageGameOverFailed(Image img)      { imgGameOverFailed = img; }
	public void setImageGameOverPartial(Image img)     { imgGameOverPartial = img; }
	public void setImageGameOverVictory(Image img)     { imgGameOverVictory = img; }

	// special image accessors (they use the currentLevel var to return the image appropriate for the current level)

	public Image getImageFloor()    { return getImageFloor(getLevelIndex(currentLevel)); }
	public Image getImageSolid()    { return getImageSolid(getLevelIndex(currentLevel)); }
	public Image getImageColumn()   { return getImageColumn(getLevelIndex(currentLevel)); }
	public Image getImageWallH()    { return getImageWallH(getLevelIndex(currentLevel)); }
	public Image getImageWallV()    { return getImageWallV(getLevelIndex(currentLevel)); }
	public Image getImageDoorH()    { return getImageDoorH(getLevelIndex(currentLevel)); }
	public Image getImageDoorV()    { return getImageDoorV(getLevelIndex(currentLevel)); }
	public Image getImageStairsU()  { return getImageStairsU(getLevelIndex(currentLevel)); }
	public Image getImageStairsD()  { return getImageStairsD(getLevelIndex(currentLevel)); }
	public Image getImageFountain() { return getImageFountain(getLevelIndex(currentLevel)); }
	public Image getImageStatue()   { return getImageStatue(getLevelIndex(currentLevel)); }
	public Image getImageShop()     { return getImageShop(getLevelIndex(currentLevel)); }
	public Image getImageChest()    { return getImageChest(getLevelIndex(currentLevel)); }
	public Image getImageVault()    { return getImageVault(getLevelIndex(currentLevel)); }

	// sound accessors

	public MP3Clip getEnvironSound(String sndname) { return (sndEnviron.containsKey(sndname) ? sndEnviron.get(sndname) : (MP3Clip)null); }
	public MP3Clip getMonsterSound(int index)      { return (sndMonster.size() > index ? sndMonster.elementAt(index) : (MP3Clip)null); }
	public MP3Clip getWeaponSound(int index)       { return (sndWeapon.size() > index ? sndWeapon.elementAt(index) : (MP3Clip)null); }

	public void setEnvironSound(MP3Clip clip, String sndname) { sndEnviron.put(sndname, clip); }
	public void setMonsterSound(MP3Clip clip, int index)      { sndMonster.setElementAt(clip, index); }
	public void setWeaponSound(MP3Clip clip, int index)       { sndWeapon.setElementAt(clip, index); }

	public void addEnvironSound(MP3Clip clip, String sndname) { if(clip != null) { sndEnviron.put(sndname, clip); } }
	public void addMonsterSound(MP3Clip clip, int index)      { sndMonster.add(index, clip); }
	public void addWeaponSound(MP3Clip clip, int index)       { sndWeapon.add(index, clip); }

	public void addMonsterSound(MP3Clip clip) { sndMonster.add(clip); }
	public void addWeaponSound(MP3Clip clip)  { sndWeapon.add(clip); }

	public String getBackgroundMusic(int index) { return levelMusic[index]; }

	public String getCombatMusic(int index) { return musicCombat.elementAt(index); }
	public void   addCombatMusic(String s)  { musicCombat.add(new String(s)); }
	public int    getCombatMusicCount()     { return musicCombat.size(); }

	public String getDeathMusic()         { return musicDeath; }
	public void   setDeathMusic(String s) { musicDeath = new String(s); }

	// quest accessors

	public Vector getQuests()                 { return questLog; }
	public void   setQuests(Vector<Quest> ht) { questLog = new Vector<Quest>(ht); }

	public void addQuest(Quest q)          { questLog.add(q); }
	public void addQuestAt(Quest q, int i) { questLog.add(i, q); }

	public int    getQuestCount()           { return questLog.size(); }
	public Quest  getQuest(int index)       { return questLog.elementAt(index); }
	public String getQuestName(int index)   { return getQuest(index).getName(); }
	public int    getQuestTurns(int index)  { return getQuest(index).getTurns(); }
	public int    getQuestStatus(int index) { return getQuest(index).getStatus(); }

	public void   setQuestStatus(int index, int status) { questLog.elementAt(index).setStatus(status); }

	// text mapping methods

	public void setTextMappings(String mappingsFile)
	{
		try
		{
			textMappings.load(new FileInputStream(new File(mappingsFile)));
		}
		catch(Exception e)
		{
			System.out.println("Exception loading module text mappings : " + e.toString());
			System.exit(1);
		}
	}

	public String getText(String textKey)
	{
		if(textMappings.containsKey(textKey))
		{
			return textMappings.getProperty(textKey);
		}
		else
		{
			return textKey;
		}
	}
}