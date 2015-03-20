package com.dreamcodex.todr.object;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import com.dreamcodex.todr.object.Item;
import com.dreamcodex.todr.object.Weapon;
import com.dreamcodex.todr.object.Armor;
import com.dreamcodex.todr.object.Ammo;
import com.dreamcodex.todr.object.Effect;
import com.dreamcodex.util.Coord;

public class Globals
{
	// app environment constants
	public static       String BASEPATH = "";
	public static       String RSRCPATH = BASEPATH + "assets" + File.separator;
	public static       String MODSPATH = RSRCPATH + "modules" + File.separator;
	public static       String SAVEPATH = RSRCPATH + "savegame" + File.separator;
	public static final String DATAEXTN = ".todr";
	public static final String SAVEEXTN = ".save";
	public static final String IMAGEXTN = ".png";

	// public randomizer instance
	public static Random rnd = new Random(System.currentTimeMillis());

	// screen display values
	public static int TILESCALE    = 16;
	public static int HALFSCALE    = TILESCALE / 2;
	public static int SPRITESCALE  = TILESCALE * 2;
	public static int ENLARGESCALE = 15; // factor by which sprites and tiles are enlarged for "big graphic" effect

	public static int GRIDWIDTH  = 42;
	public static int GRIDHEIGHT = 28;
	public static int SCRWIDTH   = 800;
	public static int SCRHEIGHT  = 600;

	public static int SCRDEPTH   = 16;
	public static int SCRREFRESH = 60;
	public static boolean FULLSCREEN  = false;

	// canvas size constants
	public static int CANVAS_HALL_X        = GRIDWIDTH;
	public static int CANVAS_HALL_Y        = GRIDHEIGHT;
	public static int CANVAS_MSG_X         = GRIDWIDTH;
	public static int CANVAS_MSG_Y         = 5;
	public static int CANVAS_HALL_OFFSET_X = (SCRWIDTH - (CANVAS_HALL_X * TILESCALE)) / 2;
	public static int CANVAS_HALL_OFFSET_Y = 20;
	public static int CANVAS_MSG_OFFSET_X  = (SCRWIDTH - (CANVAS_MSG_X * TILESCALE)) / 2;
	public static int CANVAS_MSG_OFFSET_Y  = ((CANVAS_HALL_Y + 1) * TILESCALE) + CANVAS_HALL_OFFSET_Y + (TILESCALE / 2);

	// system colors
	public static final Color SYS_COLOR_MENU = new Color(0, 0, 0);
	public static final Color SYS_COLOR_ACTV = new Color(0, 119, 0);
	public static final Color SYS_COLOR_INAC = new Color(153, 153, 153);
	public static final Color SYS_COLOR_HEAD = new Color(64, 64, 128);
	public static final Color SYS_COLOR_WARN = new Color(255, 128, 128);
	public static final Color SYS_COLOR_HIGH = new Color(32, 96, 160);
	public static final Color SYS_COLOR_UNIQ = new Color(120, 128, 80);
	public static final Color SYS_COLOR_BOXO = new Color(0, 0, 0);
	public static final Color SYS_COLOR_BOXF = new Color(255, 255, 255);

	// system data box constants
	public static final int SYS_FILELIST_COL    = 10;
	public static final int SYS_FILELIST_ROW    = 5;
	public static final int SYS_FILELIST_WIDTH  = 17;
	public static final int SYS_FILELIST_HEIGHT = 12;
	public static final int SYS_FILEINFO_COL    = 29;
	public static final int SYS_FILEINFO_ROW    = 5;
	public static final int SYS_FILEINFO_WIDTH  = 42;
	public static final int SYS_FILEINFO_HEIGHT = 12;

	// dungeon constants & variables
	public static final int MAX_LEVEL_COUNT     = 11; // max 10 levels + 1 starting shop level (at entry 0)
	public static final int MAX_LEVEL_WIDTH     = 28;
	public static final int MAX_LEVEL_HEIGHT    = 19;
	public static       int MIN_ROOMS           =  1;
	public static       int MAX_ROOMS           = 48;
	public static    Vector<String> LEVEL_NAMES = new Vector<String>(MAX_LEVEL_COUNT);
	public static    double ROOM_MONSTER_RATIO  = 0.6; // minimum percentage of rooms that should have monsters
	public static    double ROOM_ITEM_RATIO     = 0.6; // minimum percentage of rooms that should have items
	public static       int STARTING_GOLD       = 55;
	public static       int STARTING_RATIONS    = 5;

	// dungeon difficulty contsants
	public static int   DIFF_EASY        = 0;
	public static int   DIFF_NORMAL      = 1;
	public static int   DIFF_HARD        = 2;
	public static int   DIFF_EPIC        = 3;
	public static int[] DIFFMOD_MNSTODDS = {  -5,  0,  10,  20 }; // adjusts odds of rooms having monsters by difficulty
	public static int[] DIFFMOD_ITEMODDS = {  10,  0, -10, -15 }; // adjusts odds of rooms having items by difficulty
	public static int[] DIFFMOD_TRAPODDS = { -10,  0,  10,  25 }; // adjusts odds of chests having traps by difficulty
	public static int[] DIFFMOD_MNSTGRUP = {  -1,  0,   2,   3 }; // adjusts size of monster groups by difficulty
	public static int[] DIFFMOD_MNSTCLAS = {  -5,  0,   5,  10 }; // adjusts distribution of monsters by class by difficulty
	public static int[] DIFFMOD_GOLDPILE = {   3,  2,   1,   1 }; // adjusts amount of gold in chests by difficulty

	// font bank data
	public static Font FONT_MAIN      = new Font("monospaced", Font.PLAIN, 16);
	public static Font FONT_SMALL     = new Font("monospaced", Font.PLAIN, 8);
	public static int FONTSIZE        = 16;
	public static int FONTWIDTH       = 8;
	public static int FONTHEIGHT      = 16;
	public static int FONTRATIO       = TILESCALE / FONTWIDTH;
	public static int FONTLINE        = TILESCALE / FONTHEIGHT;
	public static int FONTSIZESMALL   = 8;
	public static int FONTWIDTHSMALL  = 8;
	public static int FONTHEIGHTSMALL = 8;
	public static int FONT_COLOR_MENU = 0;
	public static int FONT_COLOR_STAT = 1;
	public static int FONT_COLOR_HEAD = 2;
	public static int FONT_COLOR_TEXT = 3;
	public static int FONT_COLOR_ACTV = 4;
	public static int FONT_COLOR_INAC = 5;
	public static int FONT_COLOR_WARN = 6;
	public static int FONT_COLOR_HIGH = 7;
	public static int FONT_COLOR_INFO = 8;
	public static int FONT_COLOR_HOTK = 9;
	public static int FONT_COLOR_UNIQ = 10;

	// view mode constants
	public static final int VIEW_WINDOWED   = 0;
	public static final int VIEW_FLOATING   = 1;
	public static final int VIEW_FULLSCREEN = 2;
	public static       int VIEWTYPE        = VIEW_FLOATING;
	public static       int MARGINX         = 0;
	public static       int MARGINY         = 0;

	// cardinal direction constants
	public static final int NONE  = -1;
	public static final int NORTH = 0;
	public static final int EAST  = 1;
	public static final int SOUTH = 2;
	public static final int WEST  = 3;
	public static final int BAD   = -9;
	public static Vector<String> CARDINALS_HALL = new Vector<String>();
	static
	{
		CARDINALS_HALL.add("N");
		CARDINALS_HALL.add("E");
		CARDINALS_HALL.add("S");
		CARDINALS_HALL.add("W");
	};
	public static Coord[] CPVECTS = new Coord[4];
	static {
		CPVECTS[NORTH] = new Coord( 0, -1);
		CPVECTS[EAST]  = new Coord( 1,  0);
		CPVECTS[SOUTH] = new Coord( 0,  1);
		CPVECTS[WEST]  = new Coord(-1,  0);
	};

	// cardinal movement constants
	public static final int MOVE_NONE      = -1;
	public static final int MOVE_NORTH     = 0;
	public static final int MOVE_NORTHEAST = 1;
	public static final int MOVE_EAST      = 2;
	public static final int MOVE_SOUTHEAST = 3;
	public static final int MOVE_SOUTH     = 4;
	public static final int MOVE_SOUTHWEST = 5;
	public static final int MOVE_WEST      = 6;
	public static final int MOVE_NORTHWEST = 7;
	public static Coord[] CPMOVEVECTS = new Coord[8];
	static {
		CPMOVEVECTS[MOVE_NORTH]     = new Coord( 0, -1);
		CPMOVEVECTS[MOVE_NORTHEAST] = new Coord( 1, -1);
		CPMOVEVECTS[MOVE_EAST]      = new Coord( 1,  0);
		CPMOVEVECTS[MOVE_SOUTHEAST] = new Coord( 1,  1);
		CPMOVEVECTS[MOVE_SOUTH]     = new Coord( 0,  1);
		CPMOVEVECTS[MOVE_SOUTHWEST] = new Coord(-1,  1);
		CPMOVEVECTS[MOVE_WEST]      = new Coord(-1,  0);
		CPMOVEVECTS[MOVE_NORTHWEST] = new Coord(-1, -1);
	};

	// cardinal direction constants
	public static final int CARDINAL_MIN = 0;
	public static final int CARDINAL_MAX = CARDINALS_HALL.size() - 1;

	// facing directional constants
	public static final byte[] FACINGS         = { 1, 2, 4, 8 };
	public static final byte[] INVERSE_FACINGS = { 4, 8, 1, 2 };

	public static final int VISION_DEPTH = 8;

	public static Coord CP_VALID_START = new Coord( 1,  1);
	public static Coord CP_INVALID_LOC = new Coord(-1, -1);

	public static final int ROUNDS_PER_TURN = 10;

	// map type constants
	public static final int MAP_SOLID    = 0;
	public static final int MAP_FOUNTAIN = 16;
	public static final int MAP_ROOM     = 32;

	// room mapped state constants
	public static final int  MAPPED_NONE         = 0;
	public static final int  MAPPED_SEEN         = 1;
	public static final int  MAPPED_MAPPED       = 2;
	public static final int  MAPPED_VISITED      = 3;
	public static final char MAPPED_CHAR_NONE    = '.';
	public static final char MAPPED_CHAR_SEEN    = '~';
	public static final char MAPPED_CHAR_MAPPED  = '-';
	public static final char MAPPED_CHAR_VISITED = '+';

	// special room elements
	public static final int STAIRS_NONE        = 0;
	public static final int STAIRS_UP          = 1;
	public static final int STAIRS_DOWN        = 2;
	public static final int FEATURE_NONE       = 0;
	public static final int FEATURE_FOUNTAIN   = 1;
	public static final int FEATURE_STATUE     = 2;
	public static final int FEATURE_SHOP       = 3;

	// map bitmap reference numbers
	public static final int MAPIMAGE_UNKNOWN  = 0;
	public static final int MAPIMAGE_NORMAL   = 1;
	public static final int MAPIMAGE_UP       = 2;
	public static final int MAPIMAGE_DOWN     = 3;
	public static final int MAPIMAGE_MONSTERS = 4;
	public static final int MAPIMAGE_FOUNTAIN = 5;
	public static final int MAPIMAGE_STATUE   = 6;
	public static final int MAPIMAGE_SHOP     = 7;
	public static final int MAPIMAGE_VAULT    = 8;
	public static final int MAPIMAGE_PARTY    = 11;

	// Color Definitions
	//   Main display colors
	public static Color COLOR_MAIN_BACKGROUND = new Color(0, 0, 0);

	// room movement-space type constants
	public static final byte ROOM_OFFMAP      = -1;
	public static final byte ROOM_IMPASS      =  0;
	public static final byte ROOM_OPEN_ALL    =  1;
	public static final byte ROOM_OPEN_PLAYER =  2;
	public static final byte ROOM_DOOR        =  4;

	// game mode mappings (identifies what the player is currently doing)
	public static final int MODE_MENU          = 0;
	public static final int MODE_EXPLORE       = 1;
	public static final int MODE_COMBAT        = 2;
	public static final int MODE_USE           = 3;
	public static final int MODE_SHOP          = 4;
	public static final int MODE_ITEM          = 5;
	public static final int MODE_TRADE         = 6;
	public static final int MODE_VIEW_MAP      = 7;
	public static final int MODE_TARGET        = 8;
	public static final int MODE_ORDER         = 9;
	public static final int MODE_VAULT         = 10;
	public static final int MODE_ASK           = 11;
	public static final int MODE_NEGOTIATE     = 12;
	public static final int MODE_INFO_PLAYER   = 20;
	public static final int MODE_INV_PLAYER    = 21;
	public static final int MODE_INFO_PARTY    = 22;
	public static final int MODE_INFO_MONSTER  = 23;
	public static final int MODE_INFO_QUESTS   = 24;
	public static final int MODE_CREATE_GAME   = 30;
	public static final int MODE_FILE_SAVE     = 40;
	public static final int MODE_SELECT_PLAYER = 50;
	public static final int MODE_SHOW_NOTICE   = 60;
	public static final int MODE_INTRO         = 70;
	public static final int MODE_ENDING        = 90;

	// game submode mappings (identifies specific actions within the broad major action categories)
	public static final int SUBMODE_NONE                    = 0;
	public static final int SUBMODE_MENU_MAIN               = 1;
	public static final int SUBMODE_MENU_NEWGAME            = 2;
	public static final int SUBMODE_MENU_LOADGAME           = 3;
	public static final int SUBMODE_MENU_RESTOCK            = 4;
	public static final int SUBMODE_MENU_OPTIONS            = 5;
	public static final int SUBMODE_SHOW_STATS              = 1;
	public static final int SUBMODE_SHOW_INV                = 2;
	public static final int SUBMODE_SHOP_MAIN               = 1;
	public static final int SUBMODE_SHOP_WEAPONS            = 2;
	public static final int SUBMODE_SHOP_RANGED             = 3;
	public static final int SUBMODE_SHOP_BODYARM            = 4;
	public static final int SUBMODE_SHOP_SPECARM            = 5;
	public static final int SUBMODE_COMBAT_BASIC            = 1;
	public static final int SUBMODE_COMBAT_FIRE_TARGET      = 2;
	public static final int SUBMODE_ITEM_PICKUP             = 1;
	public static final int SUBMODE_ITEM_DROP               = 2;
	public static final int SUBMODE_ITEM_DESTROY            = 3;
	public static final int SUBMODE_ITEM_DESTROY_CONFIRM    = 4;
	public static final int SUBMODE_TRADE_SELECT_PLAYER     = 1;
	public static final int SUBMODE_TRADE_SELECT_TRADER     = 2;
	public static final int SUBMODE_TRADE_SELECT_ITEM       = 3;
	public static final int SUBMODE_TRADE_SELECT_SWAP       = 4;
	public static final int SUBMODE_TARGET_ENEMY            = 1;
	public static final int SUBMODE_ORDER_SELECT            = 1;
	public static final int SUBMODE_ORDER_ASSIGN            = 2;
	public static final int SUBMODE_VAULT_GUESS             = 1;
	public static final int SUBMODE_CREATE_LEVELS           = 1;
	public static final int SUBMODE_CREATE_PARTY            = 2;
	public static final int SUBMODE_CREATE_PLAYER_SETCLASS  = 3;
	public static final int SUBMODE_CREATE_PLAYER_SETDIFF   = 4;
	public static final int SUBMODE_CREATE_PLAYER_SETNAME   = 5;
	public static final int SUBMODE_CREATE_DUNGEON_DIFF     = 6;
	public static final int SUBMODE_SAVE_FILE               = 1;
	public static final int SUBMODE_SAVE_CREATE             = 2;
	public static final int SUBMODE_USE_SELECT              = 1;
	public static final int SUBMODE_USE_ASSIGN              = 2;
	public static final int SUBMODE_USE_COMBAT_SELECT       = 3;
	public static final int SUBMODE_PLAYER_USE              = 1;
	public static final int SUBMODE_PLAYER_CHEST            = 2;
	public static final int SUBMODE_PLAYER_DRINK            = 3;
	public static final int SUBMODE_PLAYER_ASK              = 4;
	public static final int SUBMODE_PLAYER_VAULT            = 5;
	public static final int SUBMODE_ASK_SELECT_ITEM         = 1;
	public static final int SUBMODE_ASK_INPUT_PAYMENT       = 2;
	public static final int SUBMODE_NEGOTIATE_INPUT_PAYMENT = 1;
	public static final int SUBMODE_INTRO                   = 1;
	public static final int SUBMODE_ENDING_SLAIN            = 1;
	public static final int SUBMODE_ENDING_FAILED           = 2;
	public static final int SUBMODE_ENDING_PARTIAL_VICTORY  = 3;
	public static final int SUBMODE_ENDING_COMPLETE_VICTORY = 4;

	// key mappings (fixed)
	public static final int KEY_TOGGLE_VIEW   = KeyEvent.VK_F5; // switch between fullscreen and window mode
	public static final int KEY_SUBEXIT       = KeyEvent.VK_F9;
	public static final int KEY_HELP          = KeyEvent.VK_SLASH;
	public static final int KEY_PLAYERINFO    = KeyEvent.VK_1;
	public static final int KEY_PARTYINFO     = KeyEvent.VK_2;
	public static final int KEY_MONSTERINFO   = KeyEvent.VK_3;
	public static final int KEY_QUESTINFO     = KeyEvent.VK_4;
	public static final int KEY_GAME_LOAD     = KeyEvent.VK_F1;
	public static final int KEY_GAME_SAVE     = KeyEvent.VK_F2;
	public static final int KEY_GAME_EXIT     = KeyEvent.VK_F12;
	public static final int KEY_MENU_EXIT     = KeyEvent.VK_ESCAPE;
	public static final int KEY_MENU_NEW      = KeyEvent.VK_1;
	public static final int KEY_MENU_LOAD     = KeyEvent.VK_2;
	public static final int KEY_MENU_RESTOCK  = KeyEvent.VK_3;
	public static final int KEY_MENU_SAVE     = KeyEvent.VK_4;
	public static final int KEY_MENU_OPTIONS  = KeyEvent.VK_X;
	public static final int KEY_MENU_UP       = KeyEvent.VK_UP;
	public static final int KEY_MENU_DOWN     = KeyEvent.VK_DOWN;
	public static final int KEY_MENU_LEFT     = KeyEvent.VK_LEFT;
	public static final int KEY_MENU_RIGHT    = KeyEvent.VK_RIGHT;
	public static final int KEY_MENU_SELECT   = KeyEvent.VK_ENTER;
	public static final int KEY_MENU_DELETE   = KeyEvent.VK_DELETE;
	public static final int KEY_SHOP_WEAPONS  = KeyEvent.VK_1;
	public static final int KEY_SHOP_RANGED   = KeyEvent.VK_2;
	public static final int KEY_SHOP_BODYARM  = KeyEvent.VK_3;
	public static final int KEY_SHOP_SPECARM  = KeyEvent.VK_4;
	public static final int KEY_SHOP_HEALING  = KeyEvent.VK_5;
	public static final int KEY_SHOP_RATIONS  = KeyEvent.VK_6;
	public static final int KEY_CLEAR_INPUT   = KeyEvent.VK_DELETE;
	public static final int KEY_BKSPACE_INPUT = KeyEvent.VK_BACK_SPACE;
	public static final int KEY_CONFIRM_INPUT = KeyEvent.VK_ENTER;
	public static final int KEY_CANCEL_INPUT  = KeyEvent.VK_ESCAPE;
	// key mappings (reassignable)
	public static int KEY_UP          = KeyEvent.VK_UP;
	public static int KEY_DOWN        = KeyEvent.VK_DOWN;
	public static int KEY_LEFT        = KeyEvent.VK_LEFT;
	public static int KEY_RIGHT       = KeyEvent.VK_RIGHT;
	public static int KEY_SELECT      = KeyEvent.VK_ENTER;
	public static int KEY_USE         = KeyEvent.VK_U;
	public static int KEY_FIRE_SEQ    = KeyEvent.VK_F;
	public static int KEY_FIRE_ACT    = KeyEvent.VK_SPACE;
	public static int KEY_SWAP        = KeyEvent.VK_W;
	public static int KEY_SEARCH      = KeyEvent.VK_S;
	public static int KEY_LISTEN      = KeyEvent.VK_L;
	public static int KEY_BREAK       = KeyEvent.VK_B;
	public static int KEY_MAP         = KeyEvent.VK_M;
	public static int KEY_STAIRS      = KeyEvent.VK_PERIOD;
	public static int KEY_GET         = KeyEvent.VK_G;
	public static int KEY_DROP        = KeyEvent.VK_D;
	public static int KEY_DESTROY     = KeyEvent.VK_X;
	public static int KEY_INVENTORY   = KeyEvent.VK_I;
	public static int KEY_TRADE       = KeyEvent.VK_T;
	public static int KEY_TRADE_AMMO  = KeyEvent.VK_Q; // (Q)uiver balance
	public static int KEY_OPEN_CHEST  = KeyEvent.VK_C;
	public static int KEY_OPEN_VAULT  = KeyEvent.VK_V;
	public static int KEY_DRINK_FOUNT = KeyEvent.VK_F; // (F)ountain
	public static int KEY_ASK_STATUE  = KeyEvent.VK_A; // (A)sk
	public static int KEY_NEGOTIATE   = KeyEvent.VK_N;
	public static int KEY_END_TURN    = KeyEvent.VK_Z;
	public static int KEY_SHOP        = KeyEvent.VK_P; // P(urchase)
	public static int KEY_ORDER       = KeyEvent.VK_O; // Order party formation
	public static int KEY_ANSWER_YES  = KeyEvent.VK_Y;
	public static int KEY_ANSWER_NO   = KeyEvent.VK_N;

	// lifeform type constants
	public static final int LIFEFORM_DEFAULT = 0;
	public static final int LIFEFORM_PLAYER  = 1;
	public static final int LIFEFORM_MONSTER = 2;

	// player type constants
	public static final int        MAX_CHAR_CLASSES      = 9;
	public static CharacterClass[] CHARACTER_CLASSES     = new CharacterClass[0];
	public static String           CHARACTER_DIFF_DESC   = "Color"; // if the CHARACTER_DIFF_ASSOCS array is in use, this is reloaded with a description of what the differentiator represents (Culture, Nationality, Fantasy Race, etc)
	public static String[]         CHARACTER_DIFF_KEYS   = new String[0];
	public static String[]         CHARACTER_DIFF_ASSOCS = new String[0];

	// item type constants
	public static final int ITEMTYPE_UNDEFINED = 0;
	public static final int ITEMTYPE_WEAPON    = 1;
	public static final int ITEMTYPE_ARMOR     = 2;
	public static final int ITEMTYPE_AMMO      = 3;
	public static final int ITEMTYPE_ITEM      = 4;
	public static final int ITEMTYPE_MAP       = 5;
	public static final int ITEMTYPE_CURRENCY  = 6;
	public static final int ITEMTYPE_RATIONS   = 7;
	public static final int ITEMTYPE_QUEST     = 8;

	// object type collections
	public static Hashtable<String, ItemClass>               CATALOGUE = new Hashtable<String, ItemClass>(); // dictionary of ItemClass objects, keyed on ItemClass.keychar
	public static Hashtable<String, Hashtable<String, Item>> COLLECTUM = new Hashtable<String, Hashtable<String, Item>>(); // collection of dictionaries, keyed on ItemClass.keychar, containing dictionaries of Item objects, keyed on Item.name
	public static Hashtable<String, Weapon>                  ARSENAL   = new Hashtable<String, Weapon>(); // dictionary of Weapon objects, keyed on Weapon.name
	public static Hashtable<String, Armor>                   ARMOURY   = new Hashtable<String, Armor>(); // dictionary of Armor objects, keyed on Armor.name
	public static Vector<Ammo>                               AMMOLIST  = new Vector<Ammo>();    // collection of Ammo objects
	public static Hashtable<String, MonsterDef>              BESTIARY  = new Hashtable<String, MonsterDef>(); // dictionary of MonsterDef objects, keyed on MonsterDef.name
	public static Vector<Quest>                              QUESTLOG  = new Vector<Quest>();    // collection of Quest objects for Dungeon definition
	public static Vector<Floorplan>                          PLANBOOK  = new Vector<Floorplan>();    // collection of Floorplan objects for current game
	public static Vector<Effect>                             FOUNTFX   = new Vector<Effect>();    // library of fountain effects
	public static Vector<Effect>                             TRAPDEX   = new Vector<Effect>();    // collection of trap effect objects for chests
	public static Vector<Integer>                            TRAPODDS  = new Vector<Integer>();    // collection of default trap odds, used during level creation only

	// object type collection indices
	public static Vector<String> INDEX_CATALOGUE = new Vector<String>(); // ordered list of CATALOGUE entries
	public static Vector<String> INDEX_COLLECTUM = new Vector<String>(); // ordered list of COLLECTUM entries
	public static Vector<String> INDEX_ARSENAL   = new Vector<String>(); // ordered list of ARSENAL entries
	public static Vector<String> INDEX_ARMOURY   = new Vector<String>(); // ordered list of ARMOURY entries
	public static Vector<String> INDEX_AMMOLIST  = new Vector<String>(); // ordered list of AMMO names
	public static Vector<String> INDEX_BESTIARY  = new Vector<String>(); // ordered list of BESTIARY entries

	// current Dungeon object
	public static Dungeon CURR_DUNGEON = new Dungeon(1);

	// overhead volume (room and hallway) objects
	public static Floorplan HALL_BASE_PLAN;

	// equipment constants values
	public static char   ALL_CLASSES            = '*'; // glyph that represents "all character classes may use this item"
	public static String PERMIT_ALL             = new String(ALL_CLASSES + "");
	public static int    NOT_FOR_SALE           = 0;
	public static int    MAXIMUM_CHANGE         = 5; // the most a weapon or armor can raised
	public static int    AMMO_NOT_REQUIRED      = -1;
	public static int    AMMO_INFINITE          = -1;
	public static int    AMMO_ONE_SHOT          = -2;
	public static int    MAX_INVENTORY          = 10;
	public static int    MAX_DAMAGE             = 999;
	public static int    MAX_PROTECTION         = 999;
	public static int    MAX_AMMO               = 120;
	public static int    MAX_FLOOR_STACK        = 10;
	public static int    MAX_SHOW_VALUE         = 9999;
	public static String MAX_SHOW_STR           = "++++";
	public static String DEFAULT_ITEM_STRING    = "[def]";
	public static String EMPTY_INVENTORY_STRING = "{}";
	public static String EMPTY_AMMOBAG_STRING   = "()";
	public static String VAULT_CODE_MATCH       = "[MATCH]";
	public static String NO_EFFECT_KEY          = "-";

	// constant game items (may be overwritten by data loaded from module)
	public static ItemClass ITEMCLASS_UNDEF    = new ItemClass("?", "[undefined]",   "*", (Image)null, (Image)null, 10);
	public static ItemClass ITEMCLASS_MAP      = new ItemClass("M", "Map",           "*", (Image)null, (Image)null, 1);
	public static ItemClass ITEMCLASS_CURRENCY = new ItemClass("C", "Currency",      "*", (Image)null, (Image)null, 2);
	public static ItemClass ITEMCLASS_RATION   = new ItemClass("R", "Ration",        "*", (Image)null, (Image)null, 3);
	public static ItemClass ITEMCLASS_AMMO     = new ItemClass("X", "Ammo",          "*", (Image)null, (Image)null, 6);
	public static ItemClass ITEMCLASS_WPNMEL   = new ItemClass("0", "Melee Weapon",  "*", (Image)null, (Image)null, 4);
	public static ItemClass ITEMCLASS_WPNRNG   = new ItemClass("1", "Ranged Weapon", "*", (Image)null, (Image)null, 5);
	public static ItemClass ITEMCLASS_ARMBDY   = new ItemClass("2", "Body Armor",    "*", (Image)null, (Image)null, 7);
	public static ItemClass ITEMCLASS_ARMSPC   = new ItemClass("3", "Special Armor", "*", (Image)null, (Image)null, 8);
	public static ItemClass ITEMCLASS_QUEST    = new ItemClass("Q", "Quest",         "*", (Image)null, (Image)null, 0);

	// constant equipment items (may be overwritten by data loaded from module)
	public static Weapon WEAPON_DEFAULT     = new Weapon("(none)", 0, NOT_FOR_SALE, false, AMMO_NOT_REQUIRED, PERMIT_ALL, false);
	public static Armor  ARMOR_BODY_DEFAULT = new Armor("(none)", 0, NOT_FOR_SALE, true, PERMIT_ALL, false);
	public static Armor  ARMOR_SPEC_DEFAULT = new Armor("(none)", 0, NOT_FOR_SALE, false, PERMIT_ALL, false);

	// item status flags
	public static final int ITEMSTATUS_OKAY          = 0;
	public static final int ITEMSTATUS_INVENTORYFULL = 1;
	public static final int ITEMSTATUS_NOTUSABLE     = 2;
	public static final int ITEMSTATUS_NOTALLOWED    = 3;

	// special shop items
	public static boolean[] SHOP_LEVELS           = new boolean[11];
	public static int       SHOP_RATIONS_QUANTITY = 0;   // how many rations you get per purchase
	public static int       SHOP_RATIONS_COST     = 0;   // how much a pack of rations costs
	public static int       SHOP_HEALING_COST     = 100; // how much a dose of healing costs
	public static int       RATIONS_MAX           = 100; // the most rations a party can buy
	public static int       RATIONS_MAX_ABS       = 999; // the most rations a party can carry

	// monster action decision constants
	public static final int ACTION_DECIDE = -1;
	public static final int ACTION_NONE   =  0;
	public static final int ACTION_MOVE   =  1;
	public static final int ACTION_ATTACK =  2;
	public static final int ACTION_EFFECT =  3;
	public static final int ACTION_FLEE   =  4;
	public static final int ACTION_USE    =  5;

	// action costs
	public static final int ACTION_COST_ALL           = -99;
	public static final int ACTION_COST_MOVE          =  1;
	public static final int ACTION_COST_ATTACK_MELEE  =  2;
	public static final int ACTION_COST_ATTACK_RANGED =  2;
	public static final int ACTION_COST_EFFECT        =  2;
	public static final int ACTION_COST_NEGOTIATE     =  1;
	public static final int ACTION_COST_FLEE          =  1;
	public static final int ACTION_COST_USE           =  1;
	public static final int ACTION_COST_USE_COMBAT    =  2;
	public static final int ACTION_COST_SWAP          =  0; // making it free since new combat uses auto-swap, so no point charging for it otherwise
	public static final int ACTION_COST_AMMO_BALANCE  =  1;
	public static final int ACTION_COST_SEARCH        =  1;
	public static final int ACTION_COST_SYS           =  0; // cost to perform system functions, like viewing map or info screens

	// player attribute limits
	public static int MIN_PLAYER_LEVEL = 0;
	public static int MAX_PLAYER_LEVEL = 99;
	public static int MAX_PLAYER_EXP   = 999999;
	public static int MAX_PLAYER_HP    = 999;
	public static int MAX_PLAYER_LUCK  = 50;

	// system limiter constants
	public static       int MAX_FILENAME_LENGTH = 16;
	public static       int MAX_NAME_LENGTH     = 20;
	public static       int MAX_ITEM_LENGTH     = 16;
	public static final int MAX_MESSAGES        = 5;

	// party constants
	public static int DEF_INTERVAL_RATIONS = 20;
	public static int DEF_INTERVAL_HEALING = 20;
	public static int DEF_HEALING_AMT_MIN  = 3;
	public static int DEF_HEALING_AMT_MAX  = 6;

	// quest constants
	public static final String  QUEST_NONE     = "[none]";
	public static final int     QUEST_ONGOING  = 0; // Quest is not completed, time still remains on it
	public static final int     QUEST_SUCCESS  = 1; // Quest completed and within time limit
	public static final int     QUEST_FINISHED = 2; // Quest completed but over the time limit (only allowed for partial==true Quests)
	public static final int     QUEST_FAILED   = 3; // Quest not completed, time elapsed, and quest cannot be completed
	public static       Color[] QUEST_INDIC_COLORS =
	{
		new Color(Integer.parseInt("404080", 16)),
		new Color(Integer.parseInt("408040", 16)),
		new Color(Integer.parseInt("808040", 16)),
		new Color(Integer.parseInt("804040", 16))
	};
	public static final int     QUEST_NOTIMELIMIT = -9; // Quest can be completed at any time

	// level rendering constants
	public static final int    GRFX_WALL       = 0;
	public static final int    GRFX_DOOR       = 1;
	public static final int    GRFX_FLOOR      = 2;
	public static final int    GRFX_CEILING    = 3;
	public static final String LVLGRFX_WALL    = "wall";
	public static final String LVLGRFX_DOOR    = "door";
	public static final String LVLGRFX_FLOOR   = "floor";
	public static final String LVLGRFX_CEILING = "ceiling";
	public static final String LVLGRFX_PREFIX  = "dungeon";
	public static final String LVLGRFX_SEP     = "-";
	public static final String NO_LVLGRFX      = "[none]";
	public static final String NO_MUSIC        = "[none]";

	// entity render state constants
	public static final int RENDER_MODE_DEAD   =  0;
	public static final int RENDER_MODE_NORMAL =  1;
	public static final int RENDER_MODE_ATTACK =  2;
	public static final int RENDER_MODE_EFFECT =  3;

	// dungeon limiter constants
	public static int MIN_WANDERING_CHANCE = 1;  // lowest wandering monster percent chance
	public static int MAX_WANDERING_CHANCE = 25; // highest wandering monster percent chance
	public static int ESC_WANDERING_CHANCE = 20; // escape wandering monster percent chance (set when final quest ends)

	// chest & trap constants & variables
	public static       int MIN_CHEST_ITEMS =  1;
	public static       int MAX_CHEST_ITEMS =  3;
	public static       int MIN_VAULT_ITEMS =  2;
	public static       int MAX_VAULT_ITEMS =  5;
	public static final int TRAP_NONE       = -1;

	// found quantity constants
	public static int MAX_FOUND_CURRENCY = 25;
	public static int MAX_FOUND_RATIONS  = 5;
	public static int CURRENCY_DROP_ODDS = 10;

	// most expensive weapons & armors available at a shop at the corresponding level
	public static int[] SHOP_CAPS_WPNS = { 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100 };
	public static int[] SHOP_CAPS_ARMR = { 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100 };

	// Effect types
	public static final String FX_EFFECT_NONE         = "NONE"; // Placeholder non-power
	public static final String FX_EFFECT_MAP          = "MAPS"; // Reveal Map
	public static final String FX_EFFECT_CURRENCY     = "CRCY"; // Change Party Currency
	public static final String FX_EFFECT_RATIONS      = "RTNS"; // Change Party Rations
	public static final String FX_EFFECT_PLAYERHPS    = "PHPS"; // Player Hitpoints
	public static final String FX_EFFECT_PLAYERDMG    = "PDMG"; // Player Damage
	public static final String FX_EFFECT_PLAYERPROT   = "PPRT"; // Player Armor Protection
	public static final String FX_EFFECT_PLAYERABNS   = "PABN"; // Player Armor Bonus
	public static final String FX_EFFECT_PLAYERWDMG   = "PWDM"; // Player Weapon Damage
	public static final String FX_EFFECT_PLAYERMBNS   = "PMBN"; // Player Melee Bonus
	public static final String FX_EFFECT_PLAYERRBNS   = "PRBN"; // Player Range Bonus
	public static final String FX_EFFECT_PLAYERLUCK   = "PLUK"; // Player Luck
	public static final String FX_EFFECT_PLAYEREXP    = "PEXP"; // Player Experience
	public static final String FX_EFFECT_PLAYERSPEED  = "PSPD"; // Player Combat Speed
	public static final String FX_EFFECT_MONSTATTC    = "MATC"; // Monster Attack Class
	public static final String FX_EFFECT_MONSTDEFN    = "MDEF"; // Monster Defend Class
	public static final String FX_EFFECT_MONSTDMG     = "MDMG"; // Monster Hitpoints
	public static final String FX_EFFECT_MONSTBRIBE   = "MBRB"; // Monster Bribability
	public static final String FX_EFFECT_MONSTSPEED   = "MSPD"; // Monster Speed
	public static final String FX_EFFECT_MONSTMOBIL   = "MMOB"; // Monster Mobility
	public static final String FX_EFFECT_MONSTMGCRES  = "MMRS"; // Monster Magic Resistance
	public static final String FX_EFFECT_MONSTSPCPER  = "MPER"; // Monster Special Power Percentile
	public static final String FX_EFFECT_WPNAVAIL     = "WAVL"; // Weapon Availability
	public static final String FX_EFFECT_WANDERPROB   = "WNDR"; // Wandering Monster Probability
	public static final String FX_EFFECT_RATIONINTR   = "RTNI"; // Ration Consumption Interval
	public static final String FX_EFFECT_HEALINGINTR  = "HLGI"; // Healing Interval
	public static final String FX_EFFECT_SHOWMONSTERS = "SHWM"; // Show Monsters
	public static final String FX_EFFECT_SHOWTRAPS    = "SHWT"; // Show Traps
	public static final String FX_EFFECT_REMOVETRAPS  = "RMVT"; // Remove Traps
	public static final String FX_EFFECT_POLYMORPH    = "POLY"; // Polymorph Object
	public static final String FX_EFFECT_IDENTIFY     = "IDNT"; // Identify Items
	public static final String FX_EFFECT_UNIDENTIFY   = "UNID"; // Unidentify Items
	public static final String FX_EFFECT_LIFETRANSFER = "LFTR"; // Take HP From Self & Give To Target
	public static final String FX_EFFECT_LIFELEECH    = "LFLC"; // Take HP From Target & Give To Self

//  these are unnecessary, as the same can be achieved with existing effects and target scope settings
//	public static final String FX_EFFECT_PARTYDMG     = "TDMG"; // Party (Team) Damage 
//	public static final String FX_EFFECT_PARTYCSPEED  = "TSPD"; // Party (Team) Combat Speed 
//	public static final String FX_EFFECT_MONSTDMGALL  = "MAHP"; // All Monster Hitpoints

	// Special Effect effect targets
	public static final char FX_TARGET_NONE        = 'X';
	public static final char FX_TARGET_SYSTEM      = '@';
	public static final char FX_TARGET_SELF        = 'S';
	public static final char FX_TARGET_ONEPLAYER   = 'P';
	public static final char FX_TARGET_ALLPLAYERS  = 'A';
	public static final char FX_TARGET_ONEMONSTER  = 'M';
	public static final char FX_TARGET_ALLMONSTERS = 'Z';
	public static final char FX_TARGET_EVERYONE    = '*';

	// Special Effect effect polarities
	public static final char FX_POLAR_NONE     = '0';
	public static final char FX_POLAR_DECREASE = '-';
	public static final char FX_POLAR_INCREASE = '+';

	// Standard Effects
	public static Effect NO_EFFECT = new Effect("[none]", Globals.FX_EFFECT_NONE, Globals.FX_TARGET_NONE, Globals.FX_POLAR_NONE, 0);
	public static Effect EFFECT_SHOW_MAP  = new Effect("Map Reveal", Globals.FX_EFFECT_MAP, Globals.FX_TARGET_SYSTEM, Globals.FX_POLAR_NONE, 0);
	public static Effect EFFECT_CRCY_GAIN = new Effect("Currency", Globals.FX_EFFECT_CURRENCY, Globals.FX_TARGET_SYSTEM, Globals.FX_POLAR_INCREASE, 0);
	public static Effect EFFECT_CRCY_LOSE = new Effect("Currency", Globals.FX_EFFECT_CURRENCY, Globals.FX_TARGET_SYSTEM, Globals.FX_POLAR_DECREASE, 0);
	public static Effect EFFECT_RTNS_GAIN = new Effect("Foodstuffs", Globals.FX_EFFECT_RATIONS, Globals.FX_TARGET_SYSTEM, Globals.FX_POLAR_INCREASE, 0);
	public static Effect EFFECT_RTNS_LOSE = new Effect("Foodstuffs", Globals.FX_EFFECT_RATIONS, Globals.FX_TARGET_SYSTEM, Globals.FX_POLAR_DECREASE, 0);
	public static Effect EFFECT_SHOP_HEAL = new Effect("Healing", Globals.FX_EFFECT_PLAYERDMG, Globals.FX_TARGET_ONEPLAYER, Globals.FX_POLAR_DECREASE, 0);

	// Standard Items
	public static Item ITEM_MAP      = new Item(Globals.ITEMTYPE_MAP,      Globals.ITEMCLASS_MAP,      "Map",    1, Globals.EFFECT_SHOW_MAP,  0, true, true, 0, true);
	public static Item ITEM_CURRENCY = new Item(Globals.ITEMTYPE_CURRENCY, Globals.ITEMCLASS_CURRENCY, "Gold",   1, Globals.EFFECT_CRCY_GAIN, 0, true, true, 0, true);
	public static Item ITEM_RATION   = new Item(Globals.ITEMTYPE_RATIONS,  Globals.ITEMCLASS_RATION,   "Ration", 1, Globals.EFFECT_RTNS_GAIN, 0, true, true, 0, true);

	// Player order targetting arrays for monsters
	public static final int[][] TARGET_SEQ =
	{
		{ 1, 2, 3, 4 },
		{ 2, 3, 4, 1 },
		{ 3, 4, 1, 2 },
		{ 4, 1, 2, 3 }
	};
	public static final int OUT_OF_RANGE = 9999;

	// Vault puzzle settings
	public static int[] VAULT_DIGITS_PER_LEVEL = { 1, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5 };
	public static int[] VAULT_VALUES_PER_LEVEL = { 1, 3, 4, 5, 4, 5, 6, 5, 6, 7, 8 };
	public static int   MAX_VAULT_CODE_LEN     = 5;

	// Projectile effect constants
	public static final int PROJECTILE_NONE = 0;
	public static final int PROJECTILE_BOLT = 1;
	public static final int PROJECTILE_DOT  = 2;
	public static final int PROJECTILE_RING = 3;
	public static final int PROJECTILE_STAR = 4;
	public static final int PROJECTILE_CUBE = 5;
	public static final int PROJECTILE_GLIT = 6;
	public static final int PROJECTILE_FLIP = 7;
	public static final int PROJECTILE_SPIN = 8;
	public static final int PROJ_DRAW_STEPS = 24;
	public static final int PROJ_DRAW_DELAY = 480 / PROJ_DRAW_STEPS;
	public static Color     PROJCLR_OUTLINE = new Color(0, 0, 0);

	// Sound Effect constants
	public static int    SOUND_NONE    = -1;
	public static String SOUND_MISS    = "miss";
	public static String SOUND_STEP1   = "step1";
	public static String SOUND_STEP2   = "step2";
	public static String SOUND_DOOR1   = "door1";
	public static String SOUND_DOOR2   = "door2";
	public static String SOUND_STAIR1  = "stair1";
	public static String SOUND_STAIR2  = "stair2";
	public static String SOUND_MAGIC1  = "magic1";
	public static String SOUND_MAGIC2  = "magic2";
	public static String SOUND_VAULTSH = "vaultshock";
	public static String SOUND_MENU    = "menu";
	public static String SOUND_CHEST   = "chest";
	public static String SOUND_VAULT   = "vault";
	public static String SOUND_FOUNT   = "fount";
	public static String SOUND_STATUE  = "statue";
	public static String SOUND_GET     = "get";
	public static String SOUND_DROP    = "drop";
	public static String SOUND_DESTROY = "destroy";
	public static String SOUND_SHOP    = "shop";
	public static String SOUND_DEATH1  = "death1";
	public static String SOUND_DEATH2  = "death2";
	public static String SOUND_QUEST1  = "quest1";
	public static String SOUND_QUEST2  = "quest2";
	public static String SOUND_DEFEAT  = "defeat";
	public static String SOUND_VICTORY = "victory";

	// Globals Methods ---------------------------------------------

	// "room key" methods
	public static String getRoomKey(int x, int y)
	{
		return "R" + (x < 10 ? "0" : "") + x + (y < 10 ? "0" : "") + y;
	}
	public static int getRoomKeyX(String roomKey)
	{
		return Integer.parseInt(roomKey.substring(1, 3));
	}
	public static int getRoomKeyY(String roomKey)
	{
		return Integer.parseInt(roomKey.substring(3, 5));
	}
	public static Coord getRoomKeyCoord(String roomKey)
	{
		return new Coord(Integer.parseInt(roomKey.substring(1, 3)), Integer.parseInt(roomKey.substring(3, 5)));
	}

	public static String getDungeonText(String trans)
	{
		return Globals.CURR_DUNGEON.getText(trans);
	}

	// determine if an item or class of items is permitted to be used by a character class
	public static boolean isPermitted(CharacterClass cc, String permissions)
	{
		for(int p = 0; p < permissions.length(); p++)
		{
			if(permissions.charAt(p) == Globals.ALL_CLASSES || permissions.charAt(p) == cc.getClassKey())
			{
				return true;
			}
		}
		return false;
	}

	public static int getRandomValue(int maxValue, int minValue)
	{
		return (maxValue > 0 ? Globals.rnd.nextInt((maxValue - minValue) + 1) + minValue : minValue);
	}

	public static int getRandomValue(int maxValue)
	{
		return Globals.getRandomValue(maxValue, 1);
	}

	public static int getScaledRandomValue(int maxValue)
	{
		return getRandomValue(maxValue, (maxValue > 4 ? maxValue / 4 : 1));
	}

	public static int getDiceValue(int maxValue)
	{
		int minValue = Math.max(maxValue - 6, 0);
		int rndValue = minValue + (Globals.rnd.nextInt(Math.min(6, maxValue)) + 1);
		return rndValue;
	}

	public static int getIndexByDepth(int collectionSize, int depth)
	{
		double fxrangesize = collectionSize / 10.0;
		int    fxrangemin  = Math.max(0, (int)(fxrangesize * (depth - 2)));
		int    fxrangemax  = Math.min(collectionSize, (int)(fxrangesize * (depth + 1)));
		int    fxrangediff = fxrangemax - fxrangemin;
		return Globals.rnd.nextInt(fxrangediff) + fxrangemin;
	}

	/** returns a random index from a collection based on the size of the collection and the dungeon depth */
	public static int getRandomMonsterIndex(int collectionSize, int depth, int difficulty)
	{
		if(collectionSize < Globals.MAX_LEVEL_COUNT)
		{
			int maxSelection = Math.max(((collectionSize * depth) / Globals.MAX_LEVEL_COUNT) - 1, 2);
			if(maxSelection > collectionSize)
			{
				maxSelection = collectionSize;
			}
			return Globals.rnd.nextInt(maxSelection);
		}
		else
		{
			double selectionRange = collectionSize / (Globals.MAX_LEVEL_COUNT * 1.0);
			int    selectionStart = Math.max((int)(((depth - 1) * selectionRange) - (selectionRange / 2) + Globals.DIFFMOD_MNSTCLAS[difficulty]), 0);
			int    selectionEnd   = Math.min((int)(((depth + 1) * (selectionRange + 1)) + ((selectionRange + 1) / 2) + Globals.DIFFMOD_MNSTCLAS[difficulty]) + 1, collectionSize);
			int    selectionSize  = Math.min(Math.max(selectionEnd - selectionStart, 1), collectionSize);
			return Globals.rnd.nextInt(selectionSize) + selectionStart;
		}
	}

	public static int getRandomMonsterIndex(int collectionSize, int depth)
	{
		return Globals.getRandomMonsterIndex(collectionSize, depth, Globals.DIFF_NORMAL);
	}

	public static MonsterDef getMonsterForDepth(int depth, int difficulty)
	{
		return Globals.BESTIARY.get((Globals.INDEX_BESTIARY.elementAt(Globals.getRandomMonsterIndex(Globals.INDEX_BESTIARY.size(), depth, difficulty))));
	}

	public static BaseItem getItemForDepth(int depth, int difficulty)
	{
		// select whether item is Currency, Weapon, Armor, Rations, or Item
		int whatType = Globals.rnd.nextInt(100);
		if(whatType < 30)
		{
			// Currency
			return Globals.ITEM_CURRENCY.getInstance(1, (((Globals.rnd.nextInt(Globals.MAX_FOUND_CURRENCY) + Globals.MAX_FOUND_CURRENCY) * (depth * DIFFMOD_GOLDPILE[difficulty])) + Globals.rnd.nextInt(Globals.MAX_FOUND_CURRENCY)));
		}
		else if(whatType < 45)
		{
			// Weapon
			Vector<String> subIndexes = new Vector<String>();
			boolean getRanged = (Globals.rnd.nextInt(100) > 66);
			for(int w = 0; w < Globals.INDEX_ARSENAL.size(); w++)
			{
				if(getRanged && Globals.ARSENAL.get(Globals.INDEX_ARSENAL.elementAt(w)).isRanged())
				{
					subIndexes.add(Globals.INDEX_ARSENAL.elementAt(w));
				}
				else if(!getRanged && !(Globals.ARSENAL.get(Globals.INDEX_ARSENAL.elementAt(w)).isRanged()))
				{
					subIndexes.add(Globals.INDEX_ARSENAL.elementAt(w));
				}
			}
			int whichWeapon = Globals.getIndexByDepth(subIndexes.size(), depth);
			return Globals.ARSENAL.get(subIndexes.elementAt(whichWeapon)).getInstance();
		}
		else if(whatType < 60)
		{
			// Armor
			return Globals.ARMOURY.get(Globals.INDEX_ARMOURY.elementAt(Globals.getIndexByDepth(Globals.INDEX_ARMOURY.size(), depth))).getInstance();
		}
		else if(whatType < 65)
		{
			// Ammo
			return Globals.AMMOLIST.get(Globals.rnd.nextInt(Globals.INDEX_AMMOLIST.size())).getInstance();
		}
		else if(whatType < 70)
		{
			// Rations
			return Globals.ITEM_RATION.getInstance(1, Globals.rnd.nextInt(Globals.MAX_FOUND_RATIONS) + 1);
		}
		else
		{
			// Item (new way, equal chance of any item)
			return getCollectumItem(Globals.INDEX_COLLECTUM.elementAt(Globals.rnd.nextInt(Globals.INDEX_COLLECTUM.size())), -1);
		}
	}

	public static BaseItem getVaultItemForDepth(int depth, int difficulty)
	{
		// select whether item is Currency, Weapon, Armor, or Item
		int whatType = Globals.rnd.nextInt(100);
		if(whatType < 50)
		{
			// Currency
			return Globals.ITEM_CURRENCY.getInstance(1, ((((Globals.rnd.nextInt(Globals.MAX_FOUND_CURRENCY / 5) + 1) * (Globals.MAX_FOUND_CURRENCY * 2)) * (depth * DIFFMOD_GOLDPILE[difficulty])) + Globals.rnd.nextInt(Globals.MAX_FOUND_CURRENCY * 2)));
		}
		else if(whatType < 60)
		{
			// Weapon
			Vector<String> subIndexes = new Vector<String>();
			boolean getRanged = (Globals.rnd.nextInt(100) > 66);
			for(int w = 0; w < Globals.INDEX_ARSENAL.size(); w++)
			{
				if(getRanged && Globals.ARSENAL.get(Globals.INDEX_ARSENAL.elementAt(w)).isRanged())
				{
					subIndexes.add(Globals.INDEX_ARSENAL.elementAt(w));
				}
				else if(!getRanged && !(Globals.ARSENAL.get(Globals.INDEX_ARSENAL.elementAt(w)).isRanged()))
				{
					subIndexes.add(Globals.INDEX_ARSENAL.elementAt(w));
				}
			}
			int whichWeapon = Globals.getIndexByDepth(subIndexes.size(), depth);
			return Globals.ARSENAL.get(subIndexes.elementAt(whichWeapon)).getInstance();
		}
		else if(whatType < 70)
		{
			// Armor
			return Globals.ARMOURY.get(Globals.INDEX_ARMOURY.elementAt(Globals.getIndexByDepth(Globals.INDEX_ARMOURY.size(), depth))).getInstance();
		}
		else
		{
			// Item (new way, equal chance of any item)
			return getCollectumItem(Globals.INDEX_COLLECTUM.elementAt(Globals.rnd.nextInt(Globals.INDEX_COLLECTUM.size())), -1);
		}
	}

	public static Item getCollectumItem(String name, int value)
	{
		if(Globals.INDEX_COLLECTUM.contains(name))
		{
			for(Enumeration e = Globals.COLLECTUM.keys(); e.hasMoreElements();)
			{
				Hashtable htPtr = (Hashtable)(Globals.COLLECTUM.get(e.nextElement().toString()));
				if(htPtr.containsKey(name))
				{
					if(value == -1)
					{
						return ((Item)(htPtr.get(name))).getInstance();
					}
					else
					{
						return ((Item)(htPtr.get(name))).getInstance(value);
					}
				}
			}
		}
		return (Item)null;
	}

	public static boolean isBadEffect(Effect effect)
	{
		if(effect.getEffect().equals(Globals.FX_EFFECT_CURRENCY)    && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_RATIONS)     && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERHPS)   && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERDMG)   && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERPROT)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERABNS)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERWDMG)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERMBNS)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERRBNS)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERLUCK)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYEREXP)   && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_PLAYERSPEED) && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTATTC)   && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTDEFN)   && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTDMG)    && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTBRIBE)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTSPEED)  && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTMOBIL)  && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTMGCRES) && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_MONSTSPCPER) && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_WPNAVAIL)    && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_WANDERPROB)  && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_RATIONINTR)  && effect.getPolarity() == Globals.FX_POLAR_DECREASE) { return true; }
		if(effect.getEffect().equals(Globals.FX_EFFECT_HEALINGINTR) && effect.getPolarity() == Globals.FX_POLAR_INCREASE) { return true; }
		return false;
	}
}
