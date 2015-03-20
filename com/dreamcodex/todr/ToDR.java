package com.dreamcodex.todr;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Random;

import javax.swing.Timer;

import com.dreamcodex.todr.object.Ammo;
import com.dreamcodex.todr.object.Armor;
import com.dreamcodex.todr.object.BaseItem;
import com.dreamcodex.todr.object.Board;
import com.dreamcodex.todr.object.Chest;
import com.dreamcodex.todr.object.Dungeon;
import com.dreamcodex.todr.object.Effect;
import com.dreamcodex.todr.object.Floorplan;
import com.dreamcodex.todr.object.Globals;
import com.dreamcodex.todr.object.Item;
import com.dreamcodex.todr.object.Lifeform;
import com.dreamcodex.todr.object.MonsterDef;
import com.dreamcodex.todr.object.Monster;
import com.dreamcodex.todr.object.Party;
import com.dreamcodex.todr.object.Player;
import com.dreamcodex.todr.object.Quest;
import com.dreamcodex.todr.object.QuestItem;
import com.dreamcodex.todr.object.Room;
import com.dreamcodex.todr.object.Transaction;
import com.dreamcodex.todr.object.Transactor;
import com.dreamcodex.todr.object.Vault;
import com.dreamcodex.todr.object.Weapon;
import com.dreamcodex.todr.util.ObjectParser;
import com.dreamcodex.todr.util.LevelMaker;
import com.dreamcodex.todr.util.TodrFilenameFilter;
import com.dreamcodex.util.Coord;
import com.dreamcodex.util.MP3Clip;
import com.dreamcodex.util.MP3Streamer;

/** ToDR (C) Howard Kistler
  * Main game execution class
  *
  * @author Howard Kistler
  * @version 1.2b
  * @creationdate 6/17/2004
  * @modificationdate 08/29/2010
  */

/*
The MIT License (MIT)

Tunnels of Doom Reboot
Copyright (c) 2008-2015 Howard Kistler/Dream Codex (www.dreamcodex.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

public class ToDR extends Frame implements WindowListener, KeyListener, FocusListener, MouseListener, MouseMotionListener
{

//  Objects & Variables ------------------------------------------------------/

	// Graphical Display Components
	private GraphicsDevice defScrn;
	private DisplayMode    originalMode;
	private BufferStrategy bufferStrategy;

	// Image Buffers
	private BufferedImage bufferRoomBase; // image buffer for static layer of overhead rooms & hallway arenas
	private BufferedImage bufferRoomComp; // image buffer for composited final of overhead rooms & hallway arenas
	private BufferedImage bufferEnviron;  // image buffer for 3D hallways & map
	private BufferedImage bufferMessage;  // image buffer for message window

	// Randomizer
	private Random rnd = new Random(System.currentTimeMillis());

	//   Menu display colors
	private Color clrMenuBkgr   = new Color(255, 255, 192);
	private Color clrMenuSelect = new Color(192, 192, 255);

	// "current" objects & pointers (for holding currently selected player, current game mode, current state information, etc)
	private Board        currBoard        = (Board)null;
	private Room         currRoom         = (Room)null;
	private Floorplan    currFloorplan    = (Floorplan)null;
	private Vector<Integer> vcGameMode       = new Vector<Integer>();
	private Vector<Integer> vcGameSubMode    = new Vector<Integer>();
	private int          currGameMode     = Globals.MODE_MENU;
	private int          currGameSubMode  = Globals.SUBMODE_MENU_MAIN;
	private int          currMenuIndex    = 0;
	private String       currMenuItem     = "";
	private int          currPlayerPtr    = 0;  // general player pointer used in actions sections (such as combat)
	private int          currInfoPlayer   = 0;  // which player in the party shows up on the next INFO screen
	private int          currShopPlayer   = 0;  // which player in the party is currently shopping for items
	private int          currItemPlayer   = -1; // which player in the party is currently active in an item action
	private int          currItemIndex    = -1; // which item is currently active in an item action
	private int          currItemCmbtUse  = -1; // which item is to be used in a combat situation
	private int          currAllyPtr      = -1; // which player is currently the target of a player or effect
	private int          currEnemyPtr     = -1; // which monster is currently the target of a player or effect
	private int          currTradePlayer  = 0;  // which player in the party is being traded with
	private int          currTradeItem    = 0;  // which item is being traded for
	private int          currMenuOpts     = 0;
	private Party        party            = (Party)null;
	private Player       currPlayer       = (Player)null;
	private BaseItem     currItem         = (BaseItem)null;
	private String       buildString      = new String();
	private boolean      resetPositions   = true; // place players in default position (normally used when entering rooms)
	private Vector<BaseItem> vcShopList       = new Vector<BaseItem>();
	private boolean      gameInProgress   = false;
	private String       currModule       = "";
	private String       currSaveFile     = "testsave";
	private StringBuffer currInputBuffer  = new StringBuffer();
	private String       currSelNotice    = (String)null;
	private String       currNotification = "";
	private Image        currNotificationImage = (Image)null;
	private Vector<String> currMsgs         = new Vector<String>(Globals.MAX_MESSAGES);
	private Color        currMsgColor     = new Color(0, 0, 0);
	private Coord        cpItemPos        = new Coord(Globals.CP_INVALID_LOC); // where to draw item stack in room
	private Coord        cpItemConPos     = new Coord(Globals.CP_INVALID_LOC); // where to draw chest in room
	private Transaction  currTransact     = (Transaction)null;
	private Properties   appProperties    = new Properties();
	private Properties   textGlobals      = new Properties();
	private int          charClassVal     = 0; // used during character creation
	private int          charDiffVal      = 0; // used during character creation
	private StringBuffer sbPlayerName     = new StringBuffer();
	private int          currHealInterval = 20;
	private int          currRtnInterval  = 20;
	private int          currHealCounter  = 0;
	private int          currRtnCounter   = 0;
	private boolean      breakMove        = false;
	private boolean      espMode          = false;
	private StringBuffer sbVaultGuess     = new StringBuffer();
	private Vector<String> vcVaultGuesses = new Vector<String>();
	private StringBuffer currGoldOffering = new StringBuffer();
	private MonsterDef   currMonsterType  = (MonsterDef)null;

	// constants and pseudo-constants
	private final double scalingIncrement = 1.5;
	private final int    rowLength        = 1; // used to be a var when we had variable scan widths in the draw loop
	private int     hallCenterX = Globals.CANVAS_HALL_X * Globals.TILESCALE;
	private int     hallCenterY = Globals.CANVAS_HALL_Y * Globals.TILESCALE;
	private double  scale1 = 1.8;
	private double  scale2 = scale1 * 2;
	private double  scale3 = scale2 * 2;
	private double  aspectX = 1.0;
	private double  aspectY = 1.0;

	// audio settings
	private boolean bSoundOn    = false;
	private boolean bMusicOn    = false;
	private boolean bMusicIntro = false;
	private int     volSound    = 25;
	private int     volMusic    = 25;

	// general app settings
	private boolean showIntro       = false;
	private long    introTimer      = 0l;
	private boolean introPlaying    = false;
	private boolean showHighlighter = false;

	// standard musics
	private String musicTheme = new String(Globals.NO_MUSIC);
	private String musicMenus = new String(Globals.NO_MUSIC);

	// high turnover drawing variables
	private int    actualX;
	private int    actualY;
	private int    shadeEffect;
	private int[]  polyX = new int[4];
	private int[]  polyY = new int[4];
	private Coord  cpFrontUL;
	private Coord  cpFrontUR;
	private Coord  cpFrontLL;
	private Coord  cpFrontLR;
	private Coord  cpBackUL;
	private Coord  cpBackUR;
	private Coord  cpBackLL;
	private Coord  cpBackLR;
	private double newDist     = 0.0;
	private double oldDist     = 0.0;
	private int    drawpointX  = 0;
	private int    drawpointY  = 0;
	private int    refreshTime = 500;
	private long   refreshTrig = System.currentTimeMillis();

	private Image       introScrnA = (Image)null;
	private Image       introScrnB = (Image)null;
	private MP3Streamer songTheme  = (MP3Streamer)null;
	private MP3Streamer songBkgrnd = (MP3Streamer)null;
	private MP3Streamer songCombat = (MP3Streamer)null;
	private MP3Clip     testVolume = (MP3Clip)null;

	private long lastValidKeyTime = 0l;

	private boolean showMessageWindow = true;

	private int   mouseX   = 0;
	private int   mouseY   = 0;
	private Point scrnpos = new Point(0, 0);

	private boolean DEBUG = true;

	private final String VERSION = "1.2b";

//  Constructors -------------------------------------------------------------/

	public ToDR(boolean debug)
	{
		super("Tunnels of Doom Reboot");

		DEBUG = debug;

		// image loader tracker
		ObjectParser.setMediaTracker(new MediaTracker(this));

		// obtain application properties
		appProperties = new Properties();
		try
		{
			FileInputStream fis = new FileInputStream(new File(Globals.BASEPATH + "app.properties"));
			appProperties.load(fis);
			fis.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception loading app properties : " + e.toString());
			System.exit(1);
		}

		// View Mode settings
		//     NOTE: still problems with WINDOWED mode (doesn't account for window element insets)
		Globals.VIEWTYPE = (appProperties.getProperty("viewtype") != null && appProperties.getProperty("viewtype").length() > 0 ? Integer.parseInt(appProperties.getProperty("viewtype")) : Globals.VIEW_FLOATING);

		// Audio settings
		bSoundOn   = appProperties.getProperty("soundon").equals("1");
		volSound   = Integer.parseInt(appProperties.getProperty("soundvol"));
		bMusicOn   = appProperties.getProperty("musicon").equals("1");
		volMusic   = Integer.parseInt(appProperties.getProperty("musicvol"));
		if(appProperties.getProperty("testsound") != null && appProperties.getProperty("testsound").length() > 0)
		{
			testVolume = ObjectParser.loadSound(appProperties.getProperty("testsound"));
		}

		// App settings
		showIntro       = appProperties.getProperty("showintro").equals("1");
		showHighlighter = appProperties.getProperty("showhighlighter").equals("1");

		// Standard game music
		musicTheme = (appProperties.getProperty("thememusic") != null && appProperties.getProperty("thememusic").length() > 0 ? appProperties.getProperty("thememusic") : Globals.NO_MUSIC);
		musicMenus = (appProperties.getProperty("menusmusic") != null && appProperties.getProperty("menusmusic").length() > 0 ? appProperties.getProperty("menusmusic") : Globals.NO_MUSIC);

		// Fonts and font settings
		try
		{
			Globals.FONTSIZE   = Integer.parseInt(appProperties.getProperty("fontsize"));
			Globals.FONTWIDTH  = Integer.parseInt(appProperties.getProperty("fontwidth"));
			Globals.FONTHEIGHT = Integer.parseInt(appProperties.getProperty("fontheight"));
			Globals.FONTRATIO  = Globals.TILESCALE / Globals.FONTWIDTH;
			Globals.FONTLINE   = Globals.TILESCALE / Globals.FONTHEIGHT;
			FileInputStream isFontA = new FileInputStream(Globals.RSRCPATH + appProperties.getProperty("fontmainttf"));
			Font fntLoadA = Font.createFont(Font.TRUETYPE_FONT, isFontA);
			Globals.FONT_MAIN = fntLoadA.deriveFont(Font.PLAIN, Globals.FONTSIZE);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			System.exit(1);
		}

		// Load Global Data (universal regardless of module)
		//   global text strings 
		loadGlobalTextMappings();
		//   global graphical settings
		loadGlobalGraphicProperties();

		// visualisation buffers
		bufferRoomBase = new BufferedImage(Globals.CANVAS_HALL_X * Globals.TILESCALE, Globals.CANVAS_HALL_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		bufferRoomComp = new BufferedImage(Globals.CANVAS_HALL_X * Globals.TILESCALE, Globals.CANVAS_HALL_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		bufferEnviron  = new BufferedImage(Globals.CANVAS_HALL_X * Globals.TILESCALE, Globals.CANVAS_HALL_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		bufferMessage  = new BufferedImage(Globals.CANVAS_MSG_X * Globals.TILESCALE, Globals.CANVAS_MSG_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);

		// grid drawing variables
		hallCenterX = bufferEnviron.getWidth()  / 2;
		hallCenterY = bufferEnviron.getHeight() / 2;

		this.addWindowListener(this);
		this.addKeyListener(this);
		this.addFocusListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		if(showIntro)
		{
			introScrnA = ObjectParser.loadImage("intro_logo.png");
			introScrnB = ObjectParser.loadImage("intro_title_orig.png");
		}

		// get graphics environment
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		defScrn = ge.getDefaultScreenDevice();
		originalMode = defScrn.getDisplayMode();
		if(Globals.VIEWTYPE == Globals.VIEW_FULLSCREEN && defScrn.isFullScreenSupported())
		{
			this.setUndecorated(true);
			this.setFocusable(true);
			defScrn.setFullScreenWindow(this);
			if(defScrn.isDisplayChangeSupported())
			{
				try
				{
					DisplayMode dm = getOptimalMode(Globals.SCRWIDTH, Globals.SCRHEIGHT, Globals.SCRDEPTH, Globals.SCRREFRESH);
					defScrn.setDisplayMode(dm);
					if(DEBUG) { System.out.println("Optimal Mode : " + dm.getWidth() + "x" + dm.getHeight() + ", " + dm.getBitDepth() + " (" + dm.getRefreshRate() + ")"); }
				}
				catch(Exception e) { ; }
			}
		}
		else
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			int screenX = tk.getScreenSize().width;
			if(screenX < Globals.SCRWIDTH) { System.out.println("Current screen too narrow - must be at least " + (Globals.SCRWIDTH) + "x" + (Globals.SCRHEIGHT) + " pixels in size."); System.exit(1); }
			int screenY = tk.getScreenSize().height;
			if(screenY < Globals.SCRHEIGHT) { System.out.println("Current screen too short - must be at least " + (Globals.SCRWIDTH) + "x" + (Globals.SCRHEIGHT) + " pixels in size."); System.exit(1); }
			this.setUndecorated(Globals.VIEWTYPE != Globals.VIEW_WINDOWED);
			this.setFocusable(true);
			this.setSize(Globals.SCRWIDTH, Globals.SCRHEIGHT);
			if(Globals.VIEWTYPE == Globals.VIEW_WINDOWED)
			{
				// base size first on game canvas
				// this is done before the pack(), as the pack seems to mangle these values
				int finalWidth  = Globals.SCRWIDTH;
				int finalHeight = Globals.SCRHEIGHT;
				// pack, which causes the default AWT components to report their preferred bounds correctly
				this.pack();
				// add window insets to overall size
				finalWidth  += (this.getInsets().left + this.getInsets().right);
				finalHeight += (this.getInsets().top + this.getInsets().bottom);
				this.setSize(finalWidth, finalHeight);
			}
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}

		this.setIgnoreRepaint(true);

		while(!(this.isVisible())) { try { Thread.sleep(100); } catch(InterruptedException ie) {} }

		// create buffer strategy
		this.createBufferStrategy(2);
		bufferStrategy = this.getBufferStrategy();
		while(bufferStrategy == null) { try { Thread.sleep(100); } catch(InterruptedException ie) {} } // wait for buffers to be built

		setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_MAIN);
		if(showIntro)
		{
			setCurrGameMode(Globals.MODE_INTRO, Globals.SUBMODE_INTRO);
		}
		else
		{
			songBkgrnd = playContextMusic();
		}

		// create the refresh timer, so that the screen redraws every (refreshTime) milliseconds
		ActionListener alScreenRefresh = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if(System.currentTimeMillis() - refreshTrig > refreshTime)
				{
					updateScreen();
					refreshTrig = System.currentTimeMillis();
				}
			}
		};

		// start the refresh timer
		Timer refreshTimer = new Timer(refreshTime, alScreenRefresh);
		refreshTimer.start();

		clearMessageWindow();
		updateScreen();
	}

	public ToDR()
	{
		this(false);
	}

//  Listeners ----------------------------------------------------------------/

	/* WindowListener methods */
	public void windowClosing(WindowEvent we)     { this.dispose(); System.exit(0); }
	public void windowClosed(WindowEvent we)      { }
	public void windowOpened(WindowEvent we)      { }
	public void windowIconified(WindowEvent we)   { }
	public void windowDeiconified(WindowEvent we) { }
	public void windowActivated(WindowEvent we)   { }
	public void windowDeactivated(WindowEvent we) { }

	/* KeyListener methods */
	public void keyReleased(KeyEvent ke)
	{
		if(ke.getWhen() < lastValidKeyTime)
		{
			ke.consume();
		}
		else
		{
			refreshTrig = System.currentTimeMillis();
			processGameAction(ke);
		}
	}
	public void keyPressed(KeyEvent ke) { }
	public void keyTyped(KeyEvent ke)   { }

	/* FocusListener methods */
	public void focusGained(FocusEvent fe)
	{
		this.requestFocus();
		if(bufferStrategy != null)
		{
			updateScreen();
		}
	}
	public void focusLost(FocusEvent fe) { }

	/* MouseListener methods */
	public void mousePressed(MouseEvent me)  { mouseX = me.getX(); mouseY = me.getY(); }
	public void mouseReleased(MouseEvent me) { }
	public void mouseClicked(MouseEvent me)  { }
	public void mouseEntered(MouseEvent me)  { }
	public void mouseExited(MouseEvent me)   { }

	/* MouseMotionListener methods */
	public void mouseDragged(MouseEvent me)
	{
		if(Globals.VIEWTYPE == Globals.VIEW_FLOATING)
		{
			scrnpos = this.getLocation(scrnpos);
			int offsetX = scrnpos.x - mouseX + me.getX();
			int offsetY = scrnpos.y - mouseY + me.getY();
			this.setLocation(offsetX, offsetY);
		}
	}
	public void mouseMoved(MouseEvent me) { }

//  Primary Game Logic Method ------------------------------------------------/

	public void processGameAction(KeyEvent ke)
	{
		// reset temporary turn vars
		resetTurnVars();
		// intercept Fullscreen/Windowed toggle key before anything else
		if(ke.getKeyCode() == Globals.KEY_TOGGLE_VIEW)
		{
			toggleScreenMode();
			return;
		}
		// clear message window
		clearCurrentMessage();
		// universal key actions
		if(ke.getKeyCode() == Globals.KEY_GAME_EXIT)
		{
			quitGame();
		}
		else if(ke.getKeyCode() == Globals.KEY_HELP)
		{
			displayHelp();
		}
		else if(ke.getKeyCode() == Globals.KEY_GAME_LOAD)
		{
			// IMPLEMENT - check if game already underway before loading, and if so, prompt to save first
			currMenuIndex = 0;
			currMenuItem  = "";
			Vector<String> vcSavegames = getSaveGameFilenames();
			for(int m = 0; m < vcSavegames.size(); m++)
			{
				if(vcSavegames.elementAt(m).equals(currSaveFile))
				{
					currMenuIndex = m; // defaults load selection to last saved game, handy for when losing and wanting to reload
				}
			}
			setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_LOADGAME);
		}
		else if(ke.getKeyCode() == Globals.KEY_GAME_SAVE && gameInProgress && !inCombat())
		{
			Vector<String> vcSavegames = getSaveGameFilenames();
			currMenuIndex = 0;
			currMenuItem  = "";
			for(int m = 0; m < vcSavegames.size(); m++)
			{
				if(vcSavegames.elementAt(m).equals(currSaveFile))
				{
					currMenuIndex = m + 1; // add a 1 because the "(new save)" item will push all other entries down 1
				}
			}
			setCurrGameMode(Globals.MODE_FILE_SAVE, Globals.SUBMODE_SAVE_FILE);
		}
		else
		{
			// key actions specific to the current game mode
			// MAIN MENU -----------------------------------/
			if(currGameMode == Globals.MODE_MENU)
			{
				if(currGameSubMode == Globals.SUBMODE_MENU_MAIN)
				{
					currMenuIndex = 0;
					currMenuItem  = "";
					// IMPLEMENT - check if game already underway before undertaking these actions, and if so, prompt to save first
					if(ke.getKeyCode() == Globals.KEY_MENU_NEW)
					{
						currMenuIndex = 0;
						currSaveFile = (String)null;
						setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_NEWGAME);
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_LOAD)
					{
						currMenuIndex = 0;
						Vector<String> vcSavegames = getSaveGameFilenames();
						for(int m = 0; m < vcSavegames.size(); m++)
						{
							if(vcSavegames.elementAt(m).equals(currSaveFile))
							{
								currMenuIndex = m; // defaults load selection to last saved game, handy for when losing and wanting to reload
							}
						}
						setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_LOADGAME);
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_RESTOCK)
					{
						setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_RESTOCK);
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_SAVE && gameInProgress && !inCombat())
					{
						currMenuIndex = 0;
						Vector<String> vcSavegames = getSaveGameFilenames();
						for(int m = 0; m < vcSavegames.size(); m++)
						{
							if(vcSavegames.elementAt(m).equals(currSaveFile))
							{
								currMenuIndex = m + 1; // add a 1 because the "(new save)" item will push all other entries down 1
							}
						}
						setCurrGameMode(Globals.MODE_FILE_SAVE, Globals.SUBMODE_SAVE_FILE);
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_OPTIONS)
					{
						setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_OPTIONS);
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						if(gameInProgress)
						{
							getPrevGameMode();
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_MENU_NEWGAME)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_DOWN)
					{
						currMenuIndex++;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_UP)
					{
						currMenuIndex--;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_SELECT)
					{
						if(gameInProgress)
						{
							// IMPLEMENT - prompt to save current game before starting new one
						}
						gameInProgress = false;
						// load currently selected module and begin game creation
						loadGameModule(currMenuItem);
						replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PARTY);
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_MENU_LOADGAME)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_DOWN)
					{
						currMenuIndex++;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_UP)
					{
						currMenuIndex--;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_SELECT)
					{
						// load saved game and resume play
						currSaveFile = new String(currMenuItem);
						loadSaveGameData(currSaveFile, false);
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_MENU_RESTOCK)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_DOWN)
					{
						currMenuIndex++;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_UP)
					{
						currMenuIndex--;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_SELECT)
					{
						// load saved game and resume play
						currSaveFile = new String(currMenuItem);
						loadSaveGameData(currSaveFile, true);
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_MENU_OPTIONS)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						saveSettings();
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_DOWN)
					{
						currMenuOpts++;
						if(currMenuOpts > 6) { currMenuOpts = 0; }
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_UP)
					{
						currMenuOpts--;
						if(currMenuOpts < 0) { currMenuOpts = 6; }
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_LEFT || ke.getKeyCode() == Globals.KEY_MENU_RIGHT)
					{
						if(currMenuOpts == 0)
						{
							bSoundOn = !bSoundOn;
							appProperties.setProperty("soundon", (bSoundOn ? "1" : "0"));
						}
						else if(currMenuOpts == 1)
						{
							if(ke.getKeyCode() == Globals.KEY_MENU_LEFT)
							{
								volSound--;
								volSound = Math.max(volSound, 0);
								playSound(testVolume);
							}
							else if(ke.getKeyCode() == Globals.KEY_MENU_RIGHT)
							{
								volSound++;
								volSound = Math.min(volSound, 100);
								playSound(testVolume);
							}
							appProperties.setProperty("soundvol", "" + volSound);
						}
						else if(currMenuOpts == 2)
						{
							bMusicOn = !bMusicOn;
							appProperties.setProperty("musicon", (bMusicOn ? "1" : "0"));
							if(bMusicOn)
							{
								songBkgrnd = playContextMusic();
							}
							else
							{
								stopMusic(songBkgrnd);
							}
						}
						else if(currMenuOpts == 3)
						{
							if(ke.getKeyCode() == Globals.KEY_MENU_LEFT)
							{
								volMusic--;
								volMusic = Math.max(volMusic, 0);
								adjustMusic(songBkgrnd, volMusic);
							}
							else if(ke.getKeyCode() == Globals.KEY_MENU_RIGHT)
							{
								volMusic++;
								volMusic = Math.min(volMusic, 100);
								adjustMusic(songBkgrnd, volMusic);
							}
							appProperties.setProperty("musicvol", "" + volMusic);
						}
						else if(currMenuOpts == 4)
						{
							toggleScreenMode();
						}
						else if(currMenuOpts == 5)
						{
							showHighlighter = !showHighlighter;
							appProperties.setProperty("showhighlighter",  (showHighlighter ? "1" : "0"));
						}
						else if(currMenuOpts == 6)
						{
							showIntro = !showIntro;
							appProperties.setProperty("showintro", (showIntro ? "1" : "0"));
						}
					}
				}
				else
				{
					// currently viewing submenu, return to main menu
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
				}
			}
			// CREATING SAVE FILE --------------------------/
			else if(currGameMode == Globals.MODE_FILE_SAVE)
			{
				if(currGameSubMode == Globals.SUBMODE_SAVE_FILE)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
						if(currGameMode == Globals.MODE_CREATE_GAME)
						{
							getPrevGameMode();
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_DOWN)
					{
						currMenuIndex++;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_UP)
					{
						currMenuIndex--;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_SELECT)
					{
						if(currMenuItem.equals(getTextMessage("menuSaveNewSaveOption")))
						{
							currInputBuffer.delete(0, currInputBuffer.length());
							replaceCurrGameMode(Globals.MODE_FILE_SAVE, Globals.SUBMODE_SAVE_CREATE);
						}
						else
						{
							currNotification = getTextMessage("savingMessage");
							drawScreen();
							drawNotificationBox(currNotification.length());
							bufferStrategy.show();
							currSaveFile = new String(currMenuItem);
							ObjectParser.saveGameFile(currSaveFile, currModule, Globals.CURR_DUNGEON, party);
							currNotification = "";
							if(!gameInProgress)
							{
								loadSaveGameData(currSaveFile, false);
							}
							else
							{
								getPrevGameMode();
							}
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_SAVE_CREATE)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_CANCEL_INPUT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_CONFIRM_INPUT)
					{
						if(currInputBuffer.length() > 0)
						{
							currSaveFile = currInputBuffer.toString();
							ObjectParser.saveGameFile(currSaveFile, currModule, Globals.CURR_DUNGEON, party);
							setCurrentMessage(getTextMessage("notifyGameSaved"));
							getPrevGameMode();
							if(currGameMode == Globals.MODE_CREATE_GAME)
							{
								replaceCurrGameMode(Globals.MODE_EXPLORE, Globals.SUBMODE_NONE);
							}
							if(!gameInProgress)
							{
								loadSaveGameData(currSaveFile, false);
							}
						}
						else
						{
							setCurrentMessage(getTextMessage("errorBlankFileName"));
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_CLEAR_INPUT)
					{
						currInputBuffer.delete(0, currInputBuffer.length());
					}
					else if(ke.getKeyCode() == Globals.KEY_BKSPACE_INPUT)
					{
						if(currInputBuffer.length() > 0)
						{
							currInputBuffer.delete(currInputBuffer.length() - 1, currInputBuffer.length());
						}
					}
					else if(isLegalFileChar(ke))
					{
						if(currInputBuffer.length() < Globals.MAX_FILENAME_LENGTH)
						{
							currInputBuffer.append(ke.getKeyChar());
						}
						else
						{
							setCurrentMessage(getTextMessage("errorFileNameMaxLength"));
						}
					}
					else
					{
						if(ke.getKeyCode() != KeyEvent.VK_SHIFT)
						{
							setCurrentMessage(getTextMessage("errorFileNameIllegalCharacter"));
						}
					}
				}
			}
			// CREATING NEW GAME ---------------------------/
			else if(currGameMode == Globals.MODE_CREATE_GAME)
			{
				// CREATING PARTY
				if(currGameSubMode == Globals.SUBMODE_CREATE_PARTY)
				{
					// currently viewing submenu, return to main menu
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyChar() >= '1' && ke.getKeyChar() <= '4')
					{
						// create new party of specified size
						createNewParty(ke.getKeyCode() - KeyEvent.VK_0);
						currPlayerPtr = 0;
						replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PLAYER_SETCLASS);
					}
				}
				// CREATING PLAYER - CHOOSING CLASS
				else if(currGameSubMode == Globals.SUBMODE_CREATE_PLAYER_SETCLASS)
				{
					charClassVal = 0;
					charDiffVal = 0;
					sbPlayerName.delete(0, sbPlayerName.length());
					// currently viewing submenu, return to main menu
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_SUBEXIT)
					{
						replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PARTY);
					}
					else if((ke.getKeyChar() >= '1' && ke.getKeyChar() <= '9'))
					{
						charClassVal = ke.getKeyCode() - KeyEvent.VK_1;
						if(charClassVal >= Globals.CHARACTER_CLASSES.length)
						{
							setCurrentMessage(getTextMessage("errorInvalidClass"));
							charClassVal = 0;
						}
						else if(Globals.CHARACTER_CLASSES[charClassVal].isSoloModeChar() && party.getSize() > 1)
						{
							setCurrentMessage(getTextMessage("errorInvalidClassSoloOnly"));
							charClassVal = 0;
						}
						else
						{
							replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PLAYER_SETDIFF);
						}
					}
				}
				// CREATING PLAYER - CHOOSING DIFFERENTIATOR
				else if(currGameSubMode == Globals.SUBMODE_CREATE_PLAYER_SETDIFF)
				{
					// currently viewing submenu, return to main menu
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_SUBEXIT)
					{
						replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PLAYER_SETCLASS);
					}
					else if((ke.getKeyChar() >= '1' && ke.getKeyChar() <= '9'))
					{
						charDiffVal = ke.getKeyCode() - KeyEvent.VK_1;
						if(charDiffVal >= Globals.CHARACTER_DIFF_ASSOCS.length)
						{
							setCurrentMessage(getTextMessage("errorInvalidDiff"));
							charDiffVal = 0;
						}
						else
						{
							boolean okayChoice = true;
							for(int i = 0; i < currPlayerPtr; i++)
							{
								if((party.getPlayer(i).getCharacterClassNum() == charClassVal) && (party.getPlayer(i).getCharacterDiff() == charDiffVal))
								{
									okayChoice = false;
									i = currPlayerPtr;
									String[] msgs = new String[2];
									msgs[0] = getTextMessage("errorDupeDiffWarn");
									msgs[0] = substituteInMessage(msgs[0], "$CHARDIFF", Globals.CHARACTER_DIFF_ASSOCS[charDiffVal]);
									msgs[0] = substituteInMessage(msgs[0], "$CHARCLASS", Globals.CHARACTER_CLASSES[charClassVal].getName());
									msgs[1] = getTextMessage("errorDupeDiffResolve");
									msgs[1] = substituteInMessage(msgs[1], "$DIFFDEF", Globals.CHARACTER_DIFF_DESC);
									setCurrentMessage(msgs);
									charDiffVal = 0;
								}
							}
							if(okayChoice)
							{
								replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PLAYER_SETNAME);
							}
						}
					}
				}
				// CREATING PLAYER - ENTERING NAME
				else if(currGameSubMode == Globals.SUBMODE_CREATE_PLAYER_SETNAME)
				{
					// currently viewing submenu, return to main menu
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_SUBEXIT)
					{
						replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PLAYER_SETCLASS);
					}
					else
					{
						if(ke.getKeyCode() == KeyEvent.VK_ENTER)
						{
							if(sbPlayerName.length() < 1)
							{
								setCurrentMessage(getTextMessage("errorBlankPlayerName"));
							}
							else
							{
								// add completed player
								party.setPlayer(new Player(charClassVal, charDiffVal, new String(sbPlayerName.toString())), currPlayerPtr);
								// add starting inventory items for player by class
								Vector startInv = Globals.CHARACTER_CLASSES[charClassVal].getStartInv();
								for(int i = 0; i < startInv.size(); i++)
								{
									Item itm = ((Item)(startInv.elementAt(i))).getInstance(((Item)(startInv.elementAt(i))).getCharges());
//									itm.identify();
									party.getPlayer(currPlayerPtr).gainItem(itm);
								}
								// clear creation vars
								charClassVal = 0;
								charDiffVal = 0;
								sbPlayerName.delete(0, sbPlayerName.length());
								// advance to creating next player, if any
								currPlayerPtr++;
								if(currPlayerPtr < party.getSize())
								{
									replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_PLAYER_SETCLASS);
								}
								else
								{
									replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_DUNGEON_DIFF);
								}
							}
						}
						else if(ke.getKeyCode() == Globals.KEY_BKSPACE_INPUT)
						{
							if(sbPlayerName.length() > 0)
							{
								sbPlayerName.deleteCharAt(sbPlayerName.length() - 1);
							}
						}
						else if(ke.getKeyCode() == Globals.KEY_CLEAR_INPUT)
						{
							sbPlayerName.delete(0, sbPlayerName.length());
						}
						else if(isLegalTextChar(ke))
						{
							if(sbPlayerName.length() < Globals.MAX_NAME_LENGTH)
							{
								sbPlayerName.append(ke.getKeyChar());
							}
							else
							{
								setCurrentMessage("Name is at maximum length!");
							}
						}
						else
						{
							if(ke.getKeyCode() != KeyEvent.VK_SHIFT)
							{
								setCurrentMessage("Character not allow in name!");
							}
						}
					}
				}
				// CHOOSING DUNGEON DIFFICULTY
				else if(currGameSubMode == Globals.SUBMODE_CREATE_DUNGEON_DIFF)
				{
					// currently viewing submenu, return to main menu
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if((ke.getKeyChar() >= '1' && ke.getKeyChar() <= '9'))
					{
						int difficulty = -1;
						if(ke.getKeyChar() == '1')
						{
							difficulty = Globals.DIFF_EASY;
						}
						else if(ke.getKeyChar() == '2')
						{
							difficulty = Globals.DIFF_NORMAL;
						}
						else if(ke.getKeyChar() == '3')
						{
							difficulty = Globals.DIFF_HARD;
						}
						else if(ke.getKeyChar() == '4' && party.getGameCompleted())
						{
							difficulty = Globals.DIFF_EPIC;
						}
						if(difficulty != -1)
						{
							Globals.CURR_DUNGEON.setDifficulty(difficulty);
							replaceCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_LEVELS);
						}
					}
				}
				// CREATING LEVELS
				else if(currGameSubMode == Globals.SUBMODE_CREATE_LEVELS)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						replaceCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_MAIN);
					}
					else if((ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9') || (ke.getKeyCode() == KeyEvent.VK_R))
					{
						int levels = 0;
						if(ke.getKeyCode() == KeyEvent.VK_R)
						{
							levels = rnd.nextInt(10) + 1;
						}
						else
						{
							levels = ke.getKeyCode() - KeyEvent.VK_0;
						}
						if(levels == 0) { levels = 10; }
						Globals.CURR_DUNGEON.setLevels(LevelMaker.makeDungeon(Globals.CURR_DUNGEON.getLevelWidth(), Globals.CURR_DUNGEON.getLevelHeight(), levels, Globals.CURR_DUNGEON.getRoomsMin(), Globals.CURR_DUNGEON.getRoomsMax(), Globals.CURR_DUNGEON.getDifficulty(), party.getSize(), Globals.CURR_DUNGEON.getMonsterOdds(), Globals.CURR_DUNGEON.getItemOdds(), Globals.CURR_DUNGEON.getChestOdds(), Globals.CURR_DUNGEON.getTrapOdds(), Globals.CURR_DUNGEON.getVaultOdds(), Globals.CURR_DUNGEON.getLevelNames(), Globals.CURR_DUNGEON.getLevelGraphics()));
						Globals.CURR_DUNGEON.setQuests(LevelMaker.makeQuests(levels));
						// get starting room from levels (prefer a SHOP room if available)
						Coord cpLoc = new Coord(-1, -1);
						Board tmpBoard = Globals.CURR_DUNGEON.getLevel(0);
						Room tmpRoom = (Room)null;
						for(int y = 0; y < tmpBoard.getHeight(); y++)
						{
							for(int x = 0; x < tmpBoard.getWidth(); x++)
							{
								if(isLocationRoom(tmpBoard, x, y))
								{
									tmpRoom = tmpBoard.getRoom(x, y);
									if(tmpRoom != null)
									{
										if(tmpRoom.getFeatureType() == Globals.FEATURE_SHOP)
										{
											cpLoc.setLocation(x, y);
											x = tmpBoard.getWidth();
											y = tmpBoard.getHeight();
										}
									}
								}
							}
						}
						// if didn't find room with SHOP, settle for first room you find
						if(cpLoc.getX() < 0 || cpLoc.getY() < 0)
						{
							for(int y = 0; y < tmpBoard.getHeight(); y++)
							{
								for(int x = 0; x < tmpBoard.getWidth(); x++)
								{
									if(isLocationRoom(tmpBoard, x, y))
									{
										tmpRoom = tmpBoard.getRoom(x, y);
										if(tmpRoom != null)
										{
											cpLoc.setLocation(x, y);
											x = tmpBoard.getWidth();
											y = tmpBoard.getHeight();
										}
									}
								}
							}
						}
						party.setLocation(cpLoc);
						party.setDepth(0);
						party.setTurnsElapsed(0);
						party.setRoundsElapsed(0);
						party.setRationsCounter(0);
						party.setWanderingOdds(Globals.CURR_DUNGEON.getWanderOdds());
						party.setShopCapBonus(0);
						party.setGameCompleted(false);
						for(int p = 0; p < party.getSize(); p++)
						{
							party.getPlayer(p).setWounds(0);
						}
						setCurrGameMode(Globals.MODE_FILE_SAVE, Globals.SUBMODE_SAVE_FILE);
						Vector<String> vcSavegames = getSaveGameFilenames();
						currMenuIndex = 0;
						currMenuItem  = "";
						for(int m = 0; m < vcSavegames.size(); m++)
						{
							if(vcSavegames.elementAt(m).equals(currSaveFile))
							{
								currMenuIndex = m + 1; // add a 1 because the "(new save)" item will push all other entries down 1
							}
						}
						updateScreen();
						return;
					}
				}
			}
			// VIEWING MAP ---------------------------------/
			else if(currGameMode == Globals.MODE_VIEW_MAP)
			{
				// currently viewing map screen, only change if EXIT key is pressed
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_MAP)
				{
					getPrevGameMode();
				}
			}
			// VIEWING PLAYER INFORMATION ------------------/
			else if(currGameMode == Globals.MODE_INFO_PLAYER)
			{
				// currently viewing player info
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_PLAYERINFO)
				{
					// exit player info display
					getPrevGameMode();
				}
				else if(ke.getKeyCode() == Globals.KEY_LEFT)
				{
					if(party.getSize() > 1)
					{
						// display previous player info
						currInfoPlayer--;
						if(currInfoPlayer < 0)
						{
							currInfoPlayer = party.getSize() - 1;
						}
						while(party.getPlayer(currInfoPlayer) == null)
						{
							currInfoPlayer--;
							if(currInfoPlayer < 0)
							{
								currInfoPlayer = party.getSize() - 1;
							}
						}
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_RIGHT)
				{
					if(party.getSize() > 1)
					{
						// display next player info
						currInfoPlayer++;
						if(currInfoPlayer > party.getSize() - 1)
						{
							currInfoPlayer = 0;
						}
						while(party.getPlayer(currInfoPlayer) == null)
						{
							currInfoPlayer++;
							if(currInfoPlayer > party.getSize() - 1)
							{
								currInfoPlayer = 0;
							}
						}
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_SWAP)
				{
					party.getPlayer(currInfoPlayer).swapActiveWeapon();
				}
				else if(ke.getKeyCode() == Globals.KEY_INVENTORY || ke.getKeyCode() == Globals.KEY_USE)
				{
					currItemIndex = 0;
					replaceCurrGameMode(Globals.MODE_INV_PLAYER, (ke.getKeyCode() == Globals.KEY_USE ? Globals.SUBMODE_USE_SELECT : Globals.SUBMODE_NONE));
				}
			}
			// VIEWING PLAYER INVENTORY ------------------/
			else if(currGameMode == Globals.MODE_INV_PLAYER)
			{
				// currently viewing player info
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_PLAYERINFO)
				{
					// exit player info display
					getPrevGameMode();
				}
				else if(ke.getKeyCode() == Globals.KEY_UP)
				{
					currItemIndex--;
					if(currItemIndex < 0)
					{
						currItemIndex = Math.max(0, party.getPlayer(currInfoPlayer).getInventorySize() - 1);
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_DOWN)
				{
					currItemIndex++;
					if(currItemIndex >= party.getPlayer(currInfoPlayer).getInventorySize())
					{
						currItemIndex = 0;
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_LEFT)
				{
					if(party.getSize() > 1)
					{
						currItemIndex = 0;
						// display previous player info
						currInfoPlayer--;
						if(currInfoPlayer < 0)
						{
							currInfoPlayer = party.getSize() - 1;
						}
						while(party.getPlayer(currInfoPlayer) == null)
						{
							currInfoPlayer--;
							if(currInfoPlayer < 0)
							{
								currInfoPlayer = party.getSize() - 1;
							}
						}
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_RIGHT)
				{
					if(party.getSize() > 1)
					{
						currItemIndex = 0;
						// display next player info
						currInfoPlayer++;
						if(currInfoPlayer > party.getSize() - 1)
						{
							currInfoPlayer = 0;
						}
						while(party.getPlayer(currInfoPlayer) == null)
						{
							currInfoPlayer++;
							if(currInfoPlayer > party.getSize() - 1)
							{
								currInfoPlayer = 0;
							}
						}
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_INVENTORY)
				{
					replaceCurrGameMode(Globals.MODE_INFO_PLAYER, Globals.SUBMODE_NONE);
				}
			}
			// VIEWING PARTY INFORMATION -------------------/
			else if(currGameMode == Globals.MODE_INFO_PARTY)
			{
				// currently viewing party info
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_PARTYINFO)
				{
					getPrevGameMode();
				}
			}
			// VIEWING MONSTER INFORMATION -----------------/
			else if(currGameMode == Globals.MODE_INFO_MONSTER)
			{
				// currently viewing monster info, only change if EXIT key is pressed
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_MONSTERINFO)
				{
					getPrevGameMode();
				}
			}
			// VIEWING QUEST INFORMATION -----------------/
			else if(currGameMode == Globals.MODE_INFO_QUESTS)
			{
				// currently viewing quest info, only change if EXIT key is pressed
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_QUESTINFO)
				{
					getPrevGameMode();
				}
				else if(ke.getKeyCode() == Globals.KEY_UP)
				{
					currMenuIndex--;
					if(currMenuIndex < 0)
					{
						currMenuIndex = Math.max(0, Globals.QUESTLOG.size() - 1);
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_DOWN)
				{
					currMenuIndex++;
					if(currMenuIndex > Globals.QUESTLOG.size() - 1)
					{
						currMenuIndex = 0;
					}
				}
			}
			// CHANGING PARTY ORDER ----------------------/
			else if(currGameMode == Globals.MODE_ORDER)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					getPrevGameMode();
				}
				else if(currGameSubMode == Globals.SUBMODE_ORDER_SELECT)
				{
					if(ke.getKeyCode() >= KeyEvent.VK_1 && ke.getKeyCode() <= KeyEvent.VK_4)
					{
						int keyNum = (ke.getKeyCode() - KeyEvent.VK_1);
						if(keyNum < party.getSize())
						{
							currPlayerPtr = keyNum;
							currGameSubMode = Globals.SUBMODE_ORDER_ASSIGN;
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_ORDER_ASSIGN)
				{
					if(ke.getKeyCode() >= KeyEvent.VK_1 && ke.getKeyCode() <= KeyEvent.VK_4)
					{
						int keyNum = (ke.getKeyCode() - KeyEvent.VK_1);
						if(keyNum < party.getSize())
						{
							if(keyNum != currPlayerPtr)
							{
								party.swapPositions(currPlayerPtr, keyNum);
								resetPositions = true;
							}
							currPlayerPtr = -1;
							currGameSubMode = Globals.SUBMODE_ORDER_SELECT;
						}
					}
				}
			}
			// OPENING VAULT -----------------------------/
			else if(currGameMode == Globals.MODE_VAULT)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					getPrevGameMode();
				}
				else if(currGameSubMode == Globals.SUBMODE_VAULT_GUESS)
				{
					if(ke.getKeyCode() >= KeyEvent.VK_1 && ke.getKeyCode() <= KeyEvent.VK_9)
					{
						int keyNum = (ke.getKeyCode() - KeyEvent.VK_0);
						if(keyNum <= Globals.VAULT_VALUES_PER_LEVEL[party.getDepth()])
						{
							if(sbVaultGuess.length() < Globals.VAULT_DIGITS_PER_LEVEL[party.getDepth()])
							{
								sbVaultGuess.append("" + keyNum);
							}
							else
							{
								setCurrentMessage(Globals.getDungeonText("vaultGuessFull"));
							}
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_BKSPACE_INPUT)
					{
						if(sbVaultGuess.length() > 0)
						{
							sbVaultGuess.deleteCharAt(sbVaultGuess.length() - 1);
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_SELECT)
					{
						if(sbVaultGuess.length() == Globals.VAULT_DIGITS_PER_LEVEL[party.getDepth()])
						{
							// test if match
							if(Integer.parseInt(sbVaultGuess.toString()) == currRoom.getVault().getCode())
							{
								// guess is correct, open vault and return to explore mode
								openVault(party.getPlayer(currItemPlayer));
								getPrevGameMode();
							}
							else
							{
								// guess is wrong, get hi/lo and number of accurate digits
								if(vcVaultGuesses.size() > 9)
								{
									vcVaultGuesses.removeElementAt(0);
								}
								vcVaultGuesses.add(new String(sbVaultGuess.toString()));
								// penalize player with damage for wrong guesses (sometimes)
								int numberWrong = Globals.VAULT_DIGITS_PER_LEVEL[party.getDepth()] - getCodeMatches(currRoom.getVault().getCode() + "", sbVaultGuess.toString());
								if(numberWrong > (rnd.nextInt(Globals.VAULT_DIGITS_PER_LEVEL[party.getDepth()]) + 1))
								{
									// penalty attempt, check player for saving throw
									if(party.getPlayer(currItemPlayer).getTrapResist() >= (rnd.nextInt(100) + 1))
									{
										// player avoided taking damage
									}
									else
									{
										// get base damage penalty by of depth
										int penalty = rnd.nextInt(numberWrong + party.getDepth()) + 1;
										if(penalty > 0)
										{
											String sMessage = Globals.getDungeonText("vaultPenalty");
											sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currItemPlayer).getName());
											sMessage = substituteInMessage(sMessage, "$AMOUNT", penalty);
											setCurrentMessage(sMessage);
											showVaultEffect();
											party.getPlayer(currItemPlayer).adjustWounds(penalty);
											checkPartyStatus();
											if(!(party.getPlayer(currItemPlayer).isAlive()))
											{
												getPrevGameMode();
											}
										}
									}
								}
								sbVaultGuess.delete(0, sbVaultGuess.length());
							}
						}
						else
						{
							setCurrentMessage(Globals.getDungeonText("vaultGuessIncomplete"));
						}
					}
				}
			}
			// ASKING STATUE -----------------------------/
			else if(currGameMode == Globals.MODE_ASK)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					getPrevGameMode();
				}
				else if(currGameSubMode == Globals.SUBMODE_ASK_SELECT_ITEM)
				{
					if(ke.getKeyCode() == Globals.KEY_UP)
					{
						currItemIndex--;
						if(currItemIndex < 0)
						{
							currItemIndex = party.getPlayer(currItemPlayer).getInventorySize() - 1;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						currItemIndex++;
						if(currItemIndex >= party.getPlayer(currItemPlayer).getInventorySize())
						{
							currItemIndex = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_ASK_STATUE || ke.getKeyCode() == Globals.KEY_SELECT)
					{
						if(party.getPlayer(currItemPlayer).getInventoryItem(currItemIndex).isIdentified())
						{
							setCurrentMessage(Globals.getDungeonText("itemAlreadyIdentified"));
						}
						else
						{
							currGoldOffering.delete(0, currGoldOffering.length());
							replaceCurrGameMode(Globals.MODE_ASK, Globals.SUBMODE_ASK_INPUT_PAYMENT);
							setCurrentMessage(Globals.getDungeonText("statuePaymentText"));
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_ASK_INPUT_PAYMENT)
				{
					if(ke.getKeyCode() >= KeyEvent.VK_0 && ke.getKeyCode() <= KeyEvent.VK_9)
					{
						setCurrentMessage(Globals.getDungeonText("statuePaymentText"));
						int numberVal = ((int)ke.getKeyChar()) - KeyEvent.VK_0;
						if(Integer.parseInt(currGoldOffering.toString() + numberVal) <= party.getCurrency() && Integer.parseInt(currGoldOffering.toString() + numberVal) > 0)
						{
							currGoldOffering.append(numberVal + "");
						}
						else if(Integer.parseInt(currGoldOffering.toString() + numberVal) > party.getCurrency())
						{
							setCurrentMessage(Globals.getDungeonText("statuePaymentCantOfferAmount"));
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_BKSPACE_INPUT)
					{
						if(currGoldOffering.length() > 0)
						{
							currGoldOffering.deleteCharAt(currGoldOffering.length() - 1);
						}
						setCurrentMessage(Globals.getDungeonText("statuePaymentText"));
					}
					else if(ke.getKeyCode() == Globals.KEY_CONFIRM_INPUT || ke.getKeyCode() == Globals.KEY_SELECT)
					{
						if(currGoldOffering.length() < 1 || Integer.parseInt(currGoldOffering.toString()) < 1)
						{
							setCurrentMessage(Globals.getDungeonText("statuePaymentNoOfferAmount"));
						}
						else
						{
							int statueAskingPrice = (rnd.nextInt(10) + 1) * party.getDepth() * 5;
							int realGoldOffering = Integer.parseInt(currGoldOffering.toString());
							party.spendCurrency(realGoldOffering);
							if(statueAskingPrice <= realGoldOffering)
							{
								party.getPlayer(currItemPlayer).getInventoryItem(currItemIndex).identify();
								setCurrentMessage(Globals.getDungeonText("itemStatueIdentified"));
							}
							else
							{
								party.getPlayer(currItemPlayer).loseItem(party.getPlayer(currItemPlayer).getInventoryItem(currItemIndex));
								setCurrentMessage(Globals.getDungeonText("itemConsumedIdentified"));
							}
							getPrevGameMode();
						}
					}
				}
			}
			// NEGOTIATING -------------------------------/
			else if(currGameMode == Globals.MODE_NEGOTIATE)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					getPrevGameMode();
				}
				else if(ke.getKeyCode() >= KeyEvent.VK_0 && ke.getKeyCode() <= KeyEvent.VK_9)
				{
					setCurrentMessage(Globals.getDungeonText("negotiationPaymentText"));
					int numberVal = ((int)ke.getKeyChar()) - KeyEvent.VK_0;
					if(Integer.parseInt(currGoldOffering.toString() + numberVal) <= party.getCurrency() && Integer.parseInt(currGoldOffering.toString() + numberVal) > 0)
					{
						currGoldOffering.append(numberVal + "");
					}
					else if(Integer.parseInt(currGoldOffering.toString() + numberVal) > party.getCurrency())
					{
						setCurrentMessage(Globals.getDungeonText("neogtiationPaymentCantOfferAmount"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_BKSPACE_INPUT)
				{
					if(currGoldOffering.length() > 0)
					{
						currGoldOffering.deleteCharAt(currGoldOffering.length() - 1);
					}
					setCurrentMessage(Globals.getDungeonText("negotiationPaymentText"));
				}
				else if(ke.getKeyCode() == Globals.KEY_CONFIRM_INPUT || ke.getKeyCode() == Globals.KEY_SELECT)
				{
					if(currGoldOffering.length() < 1 || Integer.parseInt(currGoldOffering.toString()) < 1)
					{
						setCurrentMessage(Globals.getDungeonText("neogtiationPaymentNoOfferAmount"));
					}
					else
					{
						int monsterAskingPrice = (rnd.nextInt(currRoom.getMonster(0).getNegotiationAmount()) + 1) * party.getDepth() * currRoom.getMonsterCount() * rnd.nextInt(10);
						int realGoldOffering = Integer.parseInt(currGoldOffering.toString());
						if(monsterAskingPrice <= realGoldOffering)
						{
							party.spendCurrency(realGoldOffering);
							setCurrentMessage(Globals.getDungeonText("negotiationSucceeds"));
							getPrevGameMode();
							for(int i = currRoom.getMonsterCount() - 1; i >= 0; i--)
							{
								currRoom.delMonster(i);
							}
							getPrevGameMode();
							updateScreen();
							doShortWait();
						}
						else
						{
							for(int i = 0; i < currRoom.getMonsterCount(); i++)
							{
								currRoom.getMonster(i).setNegotiation(0);
							}
							setCurrentMessage(Globals.getDungeonText("negotiationFails"));
							for(int i = 0; i < party.getSize(); i++)
							{
								currPlayerPtr = i;
								party.getPlayer(currPlayerPtr).expendActionPoints(Globals.ACTION_COST_ALL);
							}
							getPrevGameMode();
							getNextActivePlayer();
						}
						updateScreen();
						return;
					}
				}
			}
			// SHOW NOTIFICATION ---------------------------/
			else if(currGameMode == Globals.MODE_SHOW_NOTICE)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_SELECT)
				{
					currNotification = "";
					currNotificationImage = (Image)null;
					getPrevGameMode();
				}
			}
			// VIEWING INTRO -------------------------------/
			else if(currGameMode == Globals.MODE_INTRO)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					introPlaying = false;
					stopMusic(songTheme);
					bMusicOn = bMusicIntro;
					songBkgrnd = playMusic(songBkgrnd, musicMenus, true, false);
					getPrevGameMode();
				}
			}
			// IN COMBAT -----------------------------------/
			else if(currGameMode == Globals.MODE_COMBAT)
			{
				// check to make sure there are really enemies present, in case the enemy kill code muffs it
				if(!currRoom.hasActiveMonsters())
				{
					concludeCombat(Globals.getDungeonText("combatVictory"), true);
					updateScreen();
					return;
				}
				// obtain current active player
				currPlayer = party.getPlayer(currPlayerPtr);
				// process combat based on current mode
				if(currGameSubMode == Globals.SUBMODE_COMBAT_BASIC)
				{
					// current move order assumes players always first, and all with same speed
					// will need to change when introducing creatures of a different speed, though players should always get the first turn (I think)
					// this "players first" can be done by starting all creatures with 0 action points left, which will then be reset to their full values at the end of the player phase
					// another alternative is to built a turns list, mixing everyone together, but this can be confusing to some players
					int moveAction = Globals.NONE;
					int actionCost = 0;
					// all movement needs to have a two unit buffer, since room movement is by twos
					if(ke.getKeyCode() == Globals.KEY_UP)
					{
						moveAction = Globals.NORTH;
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						moveAction = Globals.SOUTH;
					}
					else if(ke.getKeyCode() == Globals.KEY_LEFT)
					{
						moveAction = Globals.WEST;
					}
					else if(ke.getKeyCode() == Globals.KEY_RIGHT)
					{
						moveAction = Globals.EAST;
					}
					else if(ke.getKeyCode() == Globals.KEY_FIRE_SEQ)
					{
						// fire ready weapon
						if(playerIsInsideRoom(currPlayer))
						{
							if(currPlayer.getEquiptWeapon().isRanged() && currPlayer.getAmmoQuantityForWeapon(currPlayer.getEquiptWeaponIndex()) != 0)
							{
								setCurrGameMode(Globals.MODE_TARGET, Globals.SUBMODE_TARGET_ENEMY);
								currEnemyPtr = getNextTarget();
								setCurrentMessage(Globals.getDungeonText("pickTargetHelp"));
								updateScreen();
								return;
							}
							else if(currPlayer.getAlternWeapon().isRanged() && currPlayer.getAmmoQuantityForWeapon(currPlayer.getAlternWeaponIndex()) != 0)
							{
								party.getPlayer(currPlayerPtr).swapActiveWeapon();
								setCurrGameMode(Globals.MODE_TARGET, Globals.SUBMODE_TARGET_ENEMY);
								currEnemyPtr = getNextTarget();
								setCurrentMessage(Globals.getDungeonText("pickTargetHelp"));
								updateScreen();
								return;
							}
							else if(currPlayer.getEquiptWeapon().isRanged() || currPlayer.getAlternWeapon().isRanged())
							{
								setCurrentMessage(substituteInMessage(Globals.getDungeonText("combatRangedNoAmmo"), "$ATTACKER", currPlayer.getName()));
							}
							else
							{
								setCurrentMessage(substituteInMessage(Globals.getDungeonText("combatRangedWeaponNone"), "$ATTACKER", currPlayer.getName()));
							}
						}
						else
						{
							currSelNotice = substituteInMessage(Globals.getDungeonText("combatOutsideArea"), "$ATTACKER", currPlayer.getName());
							setCurrentMessage(currSelNotice);
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_USE)
					{
						// use item
						currInfoPlayer  = currPlayerPtr;
						currItemPlayer  = currPlayerPtr;
						currItemIndex   = -1;
						currItemCmbtUse = -1;
						setCurrGameMode(Globals.MODE_USE, Globals.SUBMODE_USE_COMBAT_SELECT);
					}
					else if(ke.getKeyCode() == Globals.KEY_SWAP)
					{
						// swap ready weapon
						party.getPlayer(currPlayerPtr).swapActiveWeapon();
						actionCost = Globals.ACTION_COST_SWAP;
					}
					else if(ke.getKeyCode() == Globals.KEY_TRADE_AMMO)
					{
						// redistribute ammo
						balanceAmmo();
						actionCost = Globals.ACTION_COST_AMMO_BALANCE;
					}
					else if(ke.getKeyCode() == Globals.KEY_PLAYERINFO)
					{
						// show current player info
						currInfoPlayer = currPlayerPtr;
						setCurrGameMode(Globals.MODE_INFO_PLAYER, Globals.SUBMODE_NONE);
						updateScreen();
						return;
					}
					else if(ke.getKeyCode() == Globals.KEY_PARTYINFO)
					{
						// show party info
						currItemPlayer = currPlayerPtr;
						currItemIndex = 0;
						setCurrGameMode(Globals.MODE_INFO_PARTY, Globals.SUBMODE_SHOW_STATS);
						updateScreen();
						return;
					}
					else if(ke.getKeyCode() == Globals.KEY_MONSTERINFO)
					{
						// show monster info (if monster present)
						if(currRoom != null && currRoom.hasActiveMonsters())
						{
							setCurrGameMode(Globals.MODE_INFO_MONSTER, Globals.SUBMODE_NONE);
							updateScreen();
							return;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_SEARCH)
					{
						searchForHiddenDoors();
						actionCost = Globals.ACTION_COST_SEARCH;
					}
					else if(ke.getKeyCode() == Globals.KEY_NEGOTIATE)
					{
						if(rnd.nextInt(100) < currRoom.getMonster(0).getNegotiation())
						{
							currGoldOffering.delete(0, currGoldOffering.length());
							setCurrGameMode(Globals.MODE_NEGOTIATE, Globals.SUBMODE_NEGOTIATE_INPUT_PAYMENT);
							setCurrentMessage(Globals.getDungeonText("negotiationPaymentText"));
							updateScreen();
							return;
						}
						else
						{
							setCurrentMessage(Globals.getDungeonText("negotiationFailed"));
							for(int i = 0; i < party.getSize(); i++)
							{
								currPlayerPtr = i;
								party.getPlayer(currPlayerPtr).expendActionPoints(Globals.ACTION_COST_ALL);
							}
							actionCost = Globals.ACTION_COST_ALL;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_END_TURN)
					{
						actionCost = Globals.ACTION_COST_ALL;
					}
					if(moveAction > Globals.NONE)
					{
						actionCost = movePlayerInCombat(currPlayer, moveAction);
					}
					currPlayer.expendActionPoints(actionCost);
					updateScreen();
					getNextActivePlayer();
				}
			}
			// TARGETTING ----------------------------------/
			else if(currGameMode == Globals.MODE_TARGET)
			{
				// the only submode is "target enemy", so we don't bother checking for it
				if(!currRoom.hasActiveMonsters())
				{
					concludeCombat(Globals.getDungeonText("combatVictory"), true);
					updateScreen();
				}
				else if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					getPrevGameMode();
				}
				else if(ke.getKeyCode() == Globals.KEY_FIRE_SEQ)
				{
/*
					currEnemyPtr++;
					if(currEnemyPtr >= currRoom.getMonsterCount())
					{
						 = 0;
					}
*/
					currEnemyPtr = getNextTarget(currRoom.getMonster(currEnemyPtr).getLocation().getX() + 1, currRoom.getMonster(currEnemyPtr).getLocation().getY());
				}
				else if(ke.getKeyCode() == Globals.KEY_FIRE_ACT)
				{
					// see if targetting is the result of item use, otherwise it's a ranged weapon
					if(currItemPlayer == currPlayerPtr && currItemCmbtUse > -1)
					{
						currPlayer.expendActionPoints(Globals.ACTION_COST_USE_COMBAT);
						getPrevGameMode();
						if(currGameMode == Globals.MODE_USE)
						{
							getPrevGameMode();
						}
						updateScreen();
						doUseItem(party.getPlayer(currPlayerPtr), currItemCmbtUse, currRoom.getMonster(currEnemyPtr));
						updateScreen();
						getNextActivePlayer();
					}
					else if(currPlayer.fireReadyWeapon())
					{
						performAttack(currPlayer, currRoom.getMonster(currEnemyPtr));
						if(currPlayer.getEquiptWeapon().isThrowable())
						{
							if(currRoom.getItemCount() < 1) { if(!isClearTile(cpItemPos)) { cpItemPos.setLocation(getClearTile()); } }
							currRoom.gainItem(currPlayer.getEquiptWeapon());
							currPlayer.loseItem(currPlayer.getEquiptWeapon());
							currPlayer.swapActiveWeapon();
						}
						currPlayer.expendActionPoints(Globals.ACTION_COST_ATTACK_RANGED);
						if(currGameMode == Globals.MODE_TARGET)
						{
							getPrevGameMode();
						}
						updateScreen();
						getNextActivePlayer();
					}
				}
			}
			// SHOPPING ------------------------------------/
			else if(currGameMode == Globals.MODE_SHOP)
			{
				// change current buying character with left/right cursor keys
				if(ke.getKeyCode() == KeyEvent.VK_RIGHT)
				{
					currShopPlayer++;
					if(currShopPlayer >= party.getSize())
					{
						currShopPlayer = 0;
					}
					updateScreen();
					return;
				}
				else if(ke.getKeyCode() == KeyEvent.VK_LEFT)
				{
					currShopPlayer--;
					if(currShopPlayer < 0)
					{
						currShopPlayer = party.getSize() - 1;
					}
					updateScreen();
					return;
				}
				// otherwise, process key action
				if(currGameSubMode == Globals.SUBMODE_SHOP_MAIN)
				{
					// exit shopping
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_SHOP_WEAPONS)
					{
						// purchase hand weapons
						setCurrGameMode(Globals.MODE_SHOP, Globals.SUBMODE_SHOP_WEAPONS);
						updateShoppingList(currGameSubMode);
					}
					else if(ke.getKeyCode() == Globals.KEY_SHOP_RANGED)
					{
						// purchase ranged weapons
						setCurrGameMode(Globals.MODE_SHOP, Globals.SUBMODE_SHOP_RANGED);
						updateShoppingList(currGameSubMode);
					}
					else if(ke.getKeyCode() == Globals.KEY_SHOP_BODYARM)
					{
						// purchase body armor
						setCurrGameMode(Globals.MODE_SHOP, Globals.SUBMODE_SHOP_BODYARM);
						updateShoppingList(currGameSubMode);
					}
					else if(ke.getKeyCode() == Globals.KEY_SHOP_SPECARM)
					{
						// purchase special armor
						setCurrGameMode(Globals.MODE_SHOP, Globals.SUBMODE_SHOP_SPECARM);
						updateShoppingList(currGameSubMode);
					}
					else if(ke.getKeyCode() == Globals.KEY_SHOP_RATIONS)
					{
						// purchase rations for party
						if((party.getRations() + Globals.SHOP_RATIONS_QUANTITY) <= Globals.RATIONS_MAX)
						{
							if(party.spendCurrency(getLevelCost(Globals.SHOP_RATIONS_COST)))
							{
								party.adjustRations(Globals.SHOP_RATIONS_QUANTITY);
								setCurrentMessage(Globals.getDungeonText("shopRations") + " " + Globals.getDungeonText("shopItemBought"));
							}
							else
							{
								setCurrentMessage(Globals.getDungeonText("shopInsufficientCurrency"));
							}
						}
						else
						{
							setCurrentMessage(Globals.getDungeonText("shopRationsMaxed"));
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_SHOP_HEALING)
					{
						// purchase healing for current shop player
						if(party.getPlayer(currShopPlayer).getWounds() > 0)
						{
							if(party.spendCurrency(getLevelCost(Globals.SHOP_HEALING_COST)))
							{
								processEffect(Globals.EFFECT_SHOP_HEAL, party.getPlayer(currShopPlayer));
							}
							else
							{
								setCurrentMessage(Globals.getDungeonText("shopInsufficientCurrency"));
							}
						}
						else
						{
							String sMessage = Globals.getDungeonText("shopHealingNotNeeded");
							sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currShopPlayer).getName());
							setCurrentMessage(sMessage);
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_SHOP_WEAPONS || currGameSubMode == Globals.SUBMODE_SHOP_RANGED || currGameSubMode == Globals.SUBMODE_SHOP_BODYARM || currGameSubMode == Globals.SUBMODE_SHOP_SPECARM)
				{
					// exit subscreen
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() >= KeyEvent.VK_0 && ke.getKeyCode() <= KeyEvent.VK_9)
					{
						int selectedItem = ((int)ke.getKeyChar()) - KeyEvent.VK_1;
						if(selectedItem == -1) { selectedItem = 9; }
						if(vcShopList.size() > selectedItem && vcShopList.elementAt(selectedItem) != null)
						{
							if(currGameSubMode == Globals.SUBMODE_SHOP_WEAPONS)
							{
								purchaseItem(((Weapon)(vcShopList.elementAt(selectedItem))).getInstance());
							}
							else if(currGameSubMode == Globals.SUBMODE_SHOP_RANGED)
							{
								if(vcShopList.elementAt(selectedItem) instanceof Weapon)
								{
									purchaseItem(((Weapon)(vcShopList.elementAt(selectedItem))).getInstance());
								}
								else
								{
									purchaseAmmo((Ammo)(vcShopList.elementAt(selectedItem)));
								}
							}
							else if(currGameSubMode == Globals.SUBMODE_SHOP_BODYARM || currGameSubMode == Globals.SUBMODE_SHOP_SPECARM)
							{
								purchaseItem(((Armor)(vcShopList.elementAt(selectedItem))).getInstance());
							}
						}
					}
				}
				else
				{
					// on a purchase subscreen of buyable items
					// exit subscreen
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
				}
			}
			// MANAGING ITEMS ------------------------------/
			else if(currGameMode == Globals.MODE_ITEM)
			{
				if(currGameSubMode == Globals.SUBMODE_ITEM_PICKUP)
				{
					if(currRoom == null || currRoom.getItemCount() < 1)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_UP)
					{
						currItemIndex--;
						if(currItemIndex < 0)
						{
							currItemIndex = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						currItemIndex++;
						if(currItemIndex >= currRoom.getItemCount())
						{
							currItemIndex = currRoom.getItemCount() - 1;
						}
					}
					else if(ke.getKeyCode() >= KeyEvent.VK_1 && ke.getKeyCode() <= KeyEvent.VK_4)
					{
						int keyNum = (ke.getKeyCode() - KeyEvent.VK_1);
						if(keyNum < party.getSize())
						{
							playerGetRoomItem(party.getPlayer(keyNum), currRoom.getItem(currItemIndex));
							if(currRoom.getItemCount() < 1 && currGameMode == Globals.MODE_ITEM)
							{
								getPrevGameMode();
							}
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_ITEM_DROP)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_LEFT)
					{
						currItemPlayer--;
						currItemIndex = 0;
						if(currItemPlayer < 0)
						{
							currItemPlayer = party.getSize() - 1;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_RIGHT)
					{
						currItemPlayer++;
						currItemIndex = 0;
						if(currItemPlayer >= party.getSize())
						{
							currItemPlayer = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_UP)
					{
						currItemIndex--;
						if(currItemIndex < 0)
						{
							currItemIndex = Math.max(0, party.getPlayer(currItemPlayer).getInventorySize() + 3);
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						currItemIndex++;
						if(currItemIndex >= party.getPlayer(currItemPlayer).getInventorySize() + 4)
						{
							currItemIndex = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DROP || ke.getKeyCode() == Globals.KEY_SELECT)
					{
						if(currItemIndex == 0)
						{
							if(party.getPlayer(currItemPlayer).getEquiptWeapon().equals(Globals.WEAPON_DEFAULT))
							{
								setCurrentMessage(Globals.getDungeonText("noDropDefault"));
							}
							else
							{
								currTransact = new Transaction((Transactor)(party.getPlayer(currItemPlayer)), (Transactor)currRoom, party.getPlayer(currItemPlayer).getEquiptWeapon());
								currTransact.performTransaction();
								currTransact = null;
							}
						}
						else if(currItemIndex == 1)
						{
							if(party.getPlayer(currItemPlayer).getAlternWeapon().equals(Globals.WEAPON_DEFAULT))
							{
								setCurrentMessage(Globals.getDungeonText("noDropDefault"));
							}
							else
							{
								currTransact = new Transaction((Transactor)(party.getPlayer(currItemPlayer)), (Transactor)currRoom, party.getPlayer(currItemPlayer).getAlternWeapon());
								currTransact.performTransaction();
								currTransact = null;
							}
						}
						else if(currItemIndex == 2)
						{
							if(party.getPlayer(currItemPlayer).getArmorSlotBody().equals(Globals.ARMOR_BODY_DEFAULT))
							{
								setCurrentMessage(Globals.getDungeonText("noDropDefault"));
							}
							else
							{
								currTransact = new Transaction((Transactor)(party.getPlayer(currItemPlayer)), (Transactor)currRoom, party.getPlayer(currItemPlayer).getArmorSlotBody());
								currTransact.performTransaction();
								currTransact = null;
							}
						}
						else if(currItemIndex == 3)
						{
							if(party.getPlayer(currItemPlayer).getArmorSlotSpec().equals(Globals.ARMOR_SPEC_DEFAULT))
							{
								setCurrentMessage(Globals.getDungeonText("noDropDefault"));
							}
							else
							{
								currTransact = new Transaction((Transactor)(party.getPlayer(currItemPlayer)), (Transactor)currRoom, party.getPlayer(currItemPlayer).getArmorSlotSpec());
								currTransact.performTransaction();
								currTransact = null;
							}
						}
						else
						{
							currTransact = new Transaction((Transactor)(party.getPlayer(currItemPlayer)), (Transactor)currRoom, party.getPlayer(currItemPlayer).getInventoryItem(currItemIndex - 4));
							currTransact.performTransaction();
							currTransact = null;
							currItemIndex--;
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_ITEM_DESTROY)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_LEFT)
					{
						currItemPlayer--;
						currItemIndex = 0;
						if(currItemPlayer < 0)
						{
							currItemPlayer = party.getSize() - 1;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_RIGHT)
					{
						currItemPlayer++;
						currItemIndex = 0;
						if(currItemPlayer >= party.getSize())
						{
							currItemPlayer = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_UP)
					{
						currItemIndex--;
						if(currItemIndex < 0)
						{
							currItemIndex = Math.max(0, party.getPlayer(currItemPlayer).getInventorySize() + 3);
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						currItemIndex++;
						if(currItemIndex >= party.getPlayer(currItemPlayer).getInventorySize() + 4)
						{
							currItemIndex = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DESTROY || ke.getKeyCode() == Globals.KEY_SELECT)
					{
						String sMessage = Globals.getDungeonText("destroyConfirm");
						sMessage = substituteInMessage(sMessage, "$ITEM", getPlayerTransactionItem(party.getPlayer(currItemPlayer), currItemIndex).getInventoryText());
						setCurrentMessage(sMessage);
						replaceCurrGameMode(Globals.MODE_ITEM, Globals.SUBMODE_ITEM_DESTROY_CONFIRM);
					}
				}
				else if(currItemIndex >= 0 && currGameSubMode == Globals.SUBMODE_ITEM_DESTROY_CONFIRM)
				{
					if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_ANSWER_YES)
					{
						String sItemName = getPlayerTransactionItem(party.getPlayer(currItemPlayer), currItemIndex).getInventoryText();
						if(currItemIndex == 0)
						{
							party.getPlayer(currItemPlayer).loseItem(party.getPlayer(currItemPlayer).getEquiptWeapon());
						}
						else if(currItemIndex == 1)
						{
							party.getPlayer(currItemPlayer).loseItem(party.getPlayer(currItemPlayer).getAlternWeapon());
						}
						else if(currItemIndex == 2)
						{
							party.getPlayer(currItemPlayer).loseItem(party.getPlayer(currItemPlayer).getArmorSlotBody());
						}
						else if(currItemIndex == 3)
						{
							party.getPlayer(currItemPlayer).loseItem(party.getPlayer(currItemPlayer).getArmorSlotSpec());
						}
						else
						{
							party.getPlayer(currItemPlayer).loseItem(party.getPlayer(currItemPlayer).getInventoryItem(currItemIndex - 4));
						}
						String sMessage = Globals.getDungeonText("destroyPerformed");
						sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currItemPlayer).getName());
						sMessage = substituteInMessage(sMessage, "$ITEM", sItemName);
						setCurrentMessage(sMessage);
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_ANSWER_NO)
					{
						setCurrentMessage(Globals.getDungeonText("destroyCancelled"));
						getPrevGameMode();
					}
				}
				else
				{
					getPrevGameMode();
				}
			}
			// USING ITEMS ---------------------------------/
			else if(currGameMode == Globals.MODE_USE)
			{
				if(currGameSubMode == Globals.SUBMODE_USE_SELECT)
				{
					if(ke.getKeyCode() == Globals.KEY_UP)
					{
						// select previous available item
						currItemIndex--;
						if(currItemIndex < 0)
						{
							currItemIndex = party.getPlayer(currInfoPlayer).getInventorySize() - 1;
							if(currItemIndex < 0)
							{
								currItemIndex = 0;
							}
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						// select next available item
						currItemIndex++;
						if(currItemIndex >= party.getPlayer(currInfoPlayer).getInventorySize())
						{
							currItemIndex = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_LEFT)
					{
						// display previous player info
						currInfoPlayer--;
						if(currInfoPlayer < 0)
						{
							currInfoPlayer = party.getSize() - 1;
						}
						while(party.getPlayer(currInfoPlayer) == null)
						{
							currInfoPlayer--;
							if(currInfoPlayer < 0)
							{
								currInfoPlayer = party.getSize() - 1;
							}
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_RIGHT)
					{
						// display next player info
						currInfoPlayer++;
						if(currInfoPlayer > party.getSize() - 1)
						{
							currInfoPlayer = 0;
						}
						while(party.getPlayer(currInfoPlayer) == null)
						{
							currInfoPlayer++;
							if(currInfoPlayer > party.getSize() - 1)
							{
								currInfoPlayer = 0;
							}
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						// exit use menu
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_USE || ke.getKeyCode() == Globals.KEY_SELECT)
					{
						Item itemPtr = party.getPlayer(currInfoPlayer).getInventoryItem(currItemIndex);
						if(itemPtr != null)
						{
							if(Globals.isPermitted(party.getPlayer(currInfoPlayer).getCharacterClass(), itemPtr.getPermissions()))
							{
								if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_ONEPLAYER)
								{
									if(party.getSize() == 1)
									{
										getPrevGameMode();
										updateScreen();
										doUseItem(party.getPlayer(0), currItemIndex, party.getPlayer(0));
										if(inCombat())
										{
											currPlayer.expendActionPoints(Globals.ACTION_COST_USE_COMBAT);
											updateScreen();
											getNextActivePlayer();
										}
									}
									else
									{
										setCurrGameMode(Globals.MODE_SELECT_PLAYER, Globals.SUBMODE_PLAYER_USE);
										currSelNotice = Globals.getDungeonText("pickPlayerUseItemOn");
										currSelNotice = substituteInMessage(currSelNotice, "$ITEM", itemPtr.getInventoryText());
									}
								}
								else
								{
									getPrevGameMode();
									updateScreen();
									doUseItem(party.getPlayer(currInfoPlayer), currItemIndex);
								}
							}
							else
							{
								String sMsg = Globals.getDungeonText("itemNotPermitted");
								sMsg = substituteInMessage(sMsg, "$PLAYER", party.getPlayer(currInfoPlayer).getName());
								sMsg = substituteInMessage(sMsg, "$ITEM", itemPtr.getInventoryText());
								setCurrentMessage(sMsg);
							}
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_USE_COMBAT_SELECT)
				{
					if(ke.getKeyCode() == Globals.KEY_UP)
					{
						// select previous available item
						currItemIndex--;
						if(currItemIndex < 0)
						{
							currItemIndex = currPlayer.getInventorySize() - 1;
							if(currItemIndex < 0)
							{
								currItemIndex = 0;
							}
						}
						currItemCmbtUse = currItemIndex;
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						// select next available item
						currItemIndex++;
						if(currItemIndex >= currPlayer.getInventorySize())
						{
							currItemIndex = 0;
						}
						currItemCmbtUse = currItemIndex;
					}
					else if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
					{
						// exit use menu
						currItemCmbtUse = -1;
						getPrevGameMode();
					}
					else if(ke.getKeyCode() == Globals.KEY_USE || ke.getKeyCode() == Globals.KEY_SELECT)
					{
						if(currItemCmbtUse < 0 && currPlayer.getInventorySize() > 0)
						{
							currItemCmbtUse = 0;
						}
						if(currItemCmbtUse > -1)
						{
							Item itemPtr = currPlayer.getInventoryItem(currItemCmbtUse);
							if(itemPtr != null)
							{
								if(Globals.isPermitted(currPlayer.getCharacterClass(), itemPtr.getPermissions()))
								{
									if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_ONEPLAYER)
									{
										if(party.getSize() == 1)
										{
											getPrevGameMode();
											updateScreen();
											doUseItem(party.getPlayer(0), currItemIndex, party.getPlayer(0));
											if(inCombat())
											{
												currPlayer.expendActionPoints(Globals.ACTION_COST_USE_COMBAT);
												updateScreen();
												getNextActivePlayer();
											}
										}
										else
										{
											setCurrGameMode(Globals.MODE_SELECT_PLAYER, Globals.SUBMODE_PLAYER_USE);
											currSelNotice = Globals.getDungeonText("pickPlayerUseItemOn");
											currSelNotice = substituteInMessage(currSelNotice, "$ITEM", itemPtr.getInventoryText());
										}
									}
									else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_ONEMONSTER)
									{
										if(isInsideRoom(currPlayer.getLocation()))
										{
											setCurrGameMode(Globals.MODE_TARGET, Globals.SUBMODE_TARGET_ENEMY);
											currEnemyPtr = 0;
											currSelNotice = Globals.getDungeonText("pickMonsterUseItemOn");
											currSelNotice = substituteInMessage(currSelNotice, "$ITEM", itemPtr.getInventoryText());
											setCurrentMessage(currSelNotice);
										}
										else
										{
											currSelNotice = Globals.getDungeonText("combatOutsideArea");
											setCurrentMessage(currSelNotice);
										}
									}
									else
									{
										getPrevGameMode();
										updateScreen();
										doUseItem(currPlayer, currItemIndex);
										currPlayer.expendActionPoints(Globals.ACTION_COST_USE_COMBAT);
										updateScreen();
										getNextActivePlayer();
									}
								}
								else
								{
									String sMsg = Globals.getDungeonText("itemNotPermitted");
									sMsg = substituteInMessage(sMsg, "$PLAYER", currPlayer.getName());
									sMsg = substituteInMessage(sMsg, "$ITEM", itemPtr.getInventoryText());
									setCurrentMessage(sMsg);
								}
							}
						}
						else
						{
							getPrevGameMode();
						}
					}
				}
			}
			// MANAGING ITEMS ------------------------------/
			else if(currGameMode == Globals.MODE_TRADE)
			{
				if(party.getSize() <= 1)
				{
					setCurrentMessage(Globals.getDungeonText("noTradePartner"));
					return;
				}
				else if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					getPrevGameMode();
				}
				else if(currGameSubMode == Globals.SUBMODE_TRADE_SELECT_PLAYER)
				{
					if(ke.getKeyCode() >= KeyEvent.VK_1 && ke.getKeyCode() <= KeyEvent.VK_4)
					{
						currItemPlayer = (ke.getKeyCode() - KeyEvent.VK_1);
						replaceCurrGameMode(Globals.MODE_TRADE, Globals.SUBMODE_TRADE_SELECT_TRADER);
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_TRADE_SELECT_TRADER)
				{
					if(ke.getKeyCode() >= KeyEvent.VK_1 && ke.getKeyCode() <= KeyEvent.VK_4)
					{
						if((ke.getKeyCode() - KeyEvent.VK_1) != currItemPlayer)
						{
							currTradePlayer = (ke.getKeyCode() - KeyEvent.VK_1);
							setCurrGameMode(Globals.MODE_TRADE, Globals.SUBMODE_TRADE_SELECT_ITEM);
						}
					}
				}
				else if(currGameSubMode == Globals.SUBMODE_TRADE_SELECT_ITEM)
				{
					if(ke.getKeyCode() == Globals.KEY_UP)
					{
						currItemIndex--;
						if(currItemIndex < 0)
						{
							currItemIndex = Math.max(0, party.getPlayer(currItemPlayer).getInventorySize() + 3);
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						currItemIndex++;
						if(currItemIndex >= party.getPlayer(currItemPlayer).getInventorySize() + 4)
						{
							currItemIndex = 0;
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_TRADE || ke.getKeyCode() == Globals.KEY_SELECT)
					{
						currTransact = (Transaction)null;
						BaseItem bitm = getPlayerTransactionItem(party.getPlayer(currItemPlayer), currItemIndex);
						if(bitm.equals(Globals.WEAPON_DEFAULT) || bitm.equals(Globals.ARMOR_BODY_DEFAULT) || bitm.equals(Globals.ARMOR_SPEC_DEFAULT))
						{
							setCurrentMessage(Globals.getDungeonText("notForTrade"));
						}
						else
						{
							int itemFlag = getItemUsabilityFlag(party.getPlayer(currTradePlayer), bitm);
							if(itemFlag == Globals.ITEMSTATUS_NOTALLOWED || itemFlag == Globals.ITEMSTATUS_INVENTORYFULL)
							{
								setCurrentMessage(Globals.getDungeonText("noTradeAllowed"));
							}
							else
							{
								if(bitm == party.getPlayer(currItemPlayer).getArmorSlotBody())
								{
									if(getItemUsabilityFlag(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer).getArmorSlotBody()) == Globals.ITEMSTATUS_NOTALLOWED)
									{
										setCurrentMessage(Globals.getDungeonText("noTradeAllowed"));
									}
									else
									{
										currTransact = new Transaction(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer), bitm);
									}
								}
								else if(bitm == party.getPlayer(currItemPlayer).getArmorSlotSpec())
								{
									if(getItemUsabilityFlag(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer).getArmorSlotSpec()) == Globals.ITEMSTATUS_NOTALLOWED)
									{
										setCurrentMessage(Globals.getDungeonText("noTradeAllowed"));
									}
									else
									{
										currTransact = new Transaction(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer), bitm);
									}
								}
								else if(bitm == party.getPlayer(currItemPlayer).getEquiptWeapon())
								{
									if(getItemUsabilityFlag(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer).getAlternWeapon()) == Globals.ITEMSTATUS_NOTALLOWED)
									{
										setCurrentMessage(Globals.getDungeonText("noTradeAllowed"));
									}
									else
									{
										currTransact = new Transaction(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer), bitm);
									}
								}
								else if(bitm == party.getPlayer(currItemPlayer).getAlternWeapon())
								{
									if(getItemUsabilityFlag(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer).getAlternWeapon()) == Globals.ITEMSTATUS_NOTALLOWED)
									{
										setCurrentMessage(Globals.getDungeonText("noTradeAllowed"));
									}
									else
									{
										currTransact = new Transaction(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer), bitm);
									}
								}
								else
								{
									currTransact = new Transaction(party.getPlayer(currItemPlayer), party.getPlayer(currTradePlayer), bitm);
								}
								if(currTransact != null)
								{
									currTransact.performTransaction();
									if(currTransact.isSuccess() && currTransact.getReturnItem() != null)
									{
										party.getPlayer(currItemPlayer).gainItem(currTransact.getReturnItem());
									}
									currItemIndex = 0;
								}
							}
						}
					}
				}
			}
			// SELECT PLAYER FOR ACTION --------------------/
			else if(currGameMode == Globals.MODE_SELECT_PLAYER)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					currSelNotice = (String)null;
					getPrevGameMode();
				}
				else if(ke.getKeyCode() >= KeyEvent.VK_0 && ke.getKeyCode() <= KeyEvent.VK_9)
				{
					if((((int)ke.getKeyChar()) - KeyEvent.VK_1) < party.getSize())
					{
						currItemPlayer = (((int)ke.getKeyChar()) - KeyEvent.VK_1);
						if(party.getPlayer(currItemPlayer) != null)
						{
							currSelNotice = (String)null;
							if(currGameSubMode == Globals.SUBMODE_PLAYER_CHEST)
							{
								// OPENING CHEST -------------------------------/
								if(party.getPlayer(currItemPlayer).isAlive())
								{
									openChest(party.getPlayer(currItemPlayer));
									getPrevGameMode();
								}
								else
								{
									String sMessage = Globals.getDungeonText("chestCannotOpen");
									sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currItemPlayer).getName());
									setCurrentMessage(sMessage);
									getPrevGameMode();
								}
							}
							else if(currGameSubMode == Globals.SUBMODE_PLAYER_DRINK)
							{
								// DRINKING FROM FOUNTAIN ----------------------/
								drinkFromFountain(party.getPlayer(currItemPlayer));
								getPrevGameMode();
							}
							else if(currGameSubMode == Globals.SUBMODE_PLAYER_ASK)
							{
								// ASKING STATUE -------------------------------/
								if(party.getPlayer(currItemPlayer).getInventorySize() > 0)
								{
									currItemIndex = 0;
									currGoldOffering.delete(0, currGoldOffering.length());
									replaceCurrGameMode(Globals.MODE_ASK, Globals.SUBMODE_ASK_SELECT_ITEM);
								}
								else
								{
									String sMessage = Globals.getDungeonText("inventoryEmpty");
									sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currItemPlayer).getName());
									setCurrentMessage(sMessage);
									getPrevGameMode();
								}
							}
							else if(currGameSubMode == Globals.SUBMODE_PLAYER_VAULT)
							{
								// OPENING VAULT -------------------------------/
								if(party.getPlayer(currItemPlayer).isAlive())
								{
									sbVaultGuess.delete(0, sbVaultGuess.length());
									vcVaultGuesses.clear();
									replaceCurrGameMode(Globals.MODE_VAULT, Globals.SUBMODE_VAULT_GUESS);
								}
								else
								{
									String sMessage = Globals.getDungeonText("vaultCannotOpen");
									sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currItemPlayer).getName());
									setCurrentMessage(sMessage);
									getPrevGameMode();
								}
							}
							else if(currGameSubMode == Globals.SUBMODE_PLAYER_USE)
							{
								// USE ITEM ON PLAYER --------------------------/
								Item itemPtr = party.getPlayer(currInfoPlayer).getInventoryItem(currItemIndex);
								if(itemPtr != null)
								{
									getPrevGameMode();
									getPrevGameMode();
									updateScreen();
									if(Globals.isPermitted(party.getPlayer(currInfoPlayer).getCharacterClass(), itemPtr.getPermissions()))
									{
										doUseItem(party.getPlayer(currInfoPlayer), currItemIndex, party.getPlayer(currItemPlayer));
									}
									else
									{
										String sMsg = Globals.getDungeonText("itemNotPermitted");
										sMsg = substituteInMessage(sMsg, "$PLAYER", party.getPlayer(currInfoPlayer).getName());
										sMsg = substituteInMessage(sMsg, "$ITEM", itemPtr.getInventoryText());
										setCurrentMessage(sMsg);
									}
									if(inCombat())
									{
										currPlayer.expendActionPoints(Globals.ACTION_COST_USE_COMBAT);
										//if(party.getSize() == 1) { getPrevGameMode(); }
										clearCurrentMessage();
										getNextActivePlayer();
									}
								}
							}
						}
					}
				}
			}
			// VIEWING ENDING SCREEN -----------------------/
			else if(currGameMode == Globals.MODE_ENDING)
			{
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT || ke.getKeyCode() == Globals.KEY_GAME_EXIT)
				{
					getPrevGameMode();
				}
			}
			// EXPLORING DUNGEON ---------------------------/
			else
			{
				// assume mode is MODE_EXPLORE at all other times
				if(currGameMode != Globals.MODE_EXPLORE || currGameSubMode != Globals.SUBMODE_NONE)
				{
					setCurrGameMode(Globals.MODE_EXPLORE, Globals.SUBMODE_NONE);
				}
				if(ke.getKeyCode() == Globals.KEY_MENU_EXIT)
				{
					setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_MAIN);
				}
				else if(ke.getKeyCode() == Globals.KEY_GAME_EXIT)
				{
					quitGame();
				}
				else if(ke.getKeyCode() == Globals.KEY_MAP)
				{
					setCurrGameMode(Globals.MODE_VIEW_MAP, Globals.SUBMODE_NONE);
				}
				else if(ke.getKeyCode() == Globals.KEY_USE)
				{
					currInfoPlayer = 0;
					currItemIndex  = 0;
					setCurrGameMode(Globals.MODE_USE, Globals.SUBMODE_USE_SELECT);
				}
				else if(ke.getKeyCode() == Globals.KEY_TRADE)
				{
					if(party.getSize() > 1)
					{
						currItemPlayer  = 0;
						currItemIndex   = 0;
						currTradePlayer = 0;
						currTradeItem   = 0;
						setCurrGameMode(Globals.MODE_TRADE, Globals.SUBMODE_TRADE_SELECT_PLAYER);
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noTradePartner"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_TRADE_AMMO)
				{
					balanceAmmo();
				}
				else if(ke.getKeyCode() == Globals.KEY_PLAYERINFO)
				{
					currInfoPlayer = 0;
					currItemIndex  = 0;
					setCurrGameMode(Globals.MODE_INFO_PLAYER, Globals.SUBMODE_SHOW_STATS);
				}
				else if(ke.getKeyCode() == Globals.KEY_INVENTORY)
				{
					currInfoPlayer = 0;
					currItemIndex  = 0;
					setCurrGameMode(Globals.MODE_INV_PLAYER, Globals.SUBMODE_NONE);
				}
				else if(ke.getKeyCode() == Globals.KEY_PARTYINFO)
				{
					currItemPlayer = 0;
					currItemIndex  = 0;
					setCurrGameMode(Globals.MODE_INFO_PARTY, Globals.SUBMODE_SHOW_STATS);
				}
				else if(ke.getKeyCode() == Globals.KEY_MONSTERINFO)
				{
					if(currRoom != null && currRoom.hasActiveMonsters())
					{
						setCurrGameMode(Globals.MODE_INFO_MONSTER, Globals.SUBMODE_NONE);
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_QUESTINFO)
				{
					if(Globals.QUESTLOG.size() > 0)
					{
						currMenuIndex = 0;
						setCurrGameMode(Globals.MODE_INFO_QUESTS, Globals.SUBMODE_NONE);
					}
					else
					{
						setCurrentMessage(getTextMessage("errorNoQuestsAvailable"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_SEARCH)
				{
					searchForHiddenDoors();
					elapseRound();
					checkForRandomEncounter();
				}
				else if(ke.getKeyCode() == Globals.KEY_GET)
				{
					if(currRoom != null && currRoom.getItemCount() > 0)
					{
						currItem = currRoom.getItem(0);
						if(currItem.getType() == Globals.ITEMTYPE_QUEST)
						{
							getQuestItem(((QuestItem)currItem).getQuestNumber());
							currRoom.loseItem(currItem);
							currItem = null;
						}
						else if(currItem.getType() >= Globals.ITEMTYPE_ITEM)
						{
							if(((Item)currItem).isGroupPickUp())
							{
								processEffect(((Item)currItem).getEffect());
								currRoom.loseItem(currItem);
								currItem = null;
							}
						}
						if(currItem != null)
						{
							currItemIndex = 0;
							setCurrGameMode(Globals.MODE_ITEM, Globals.SUBMODE_ITEM_PICKUP);
						}
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noItemToPickUp"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_DROP)
				{
					if(currRoom != null && currRoom.getItemCount() < Globals.MAX_FLOOR_STACK)
					{
						currItemPlayer = 0;
						currItemIndex = 0;
						setCurrGameMode(Globals.MODE_ITEM, Globals.SUBMODE_ITEM_DROP);
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("dropNoRoom"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_DESTROY)
				{
					currItemPlayer = 0;
					currItemIndex = 0;
					setCurrGameMode(Globals.MODE_ITEM, Globals.SUBMODE_ITEM_DESTROY);
				}
				else if(ke.getKeyCode() == Globals.KEY_OPEN_CHEST)
				{
					if(currRoom != null && currRoom.hasChest())
					{
						if(party.getSize() == 1)
						{
							openChest(party.getPlayer(0));
						}
						else
						{
							currSelNotice = Globals.getDungeonText("pickPlayerOpenChest");
							setCurrGameMode(Globals.MODE_SELECT_PLAYER, Globals.SUBMODE_PLAYER_CHEST);
						}
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noChest"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_DRINK_FOUNT)
				{
					if(
						currBoard.hasFountain(party.getLocationX(), party.getLocationY()) ||
						(currRoom != null && (currRoom.getFeatureType() == Globals.FEATURE_FOUNTAIN))
					)
					{
						if(party.getSize() == 1)
						{
							drinkFromFountain(party.getPlayer(0));
						}
						else
						{
							// select player to drink from fountain
							currSelNotice = Globals.getDungeonText("pickPlayerDrinkFountain");
							setCurrGameMode(Globals.MODE_SELECT_PLAYER, Globals.SUBMODE_PLAYER_DRINK);
						}
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noFountain"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_ASK_STATUE)
				{
					if(currRoom != null && (currRoom.getFeatureType() == Globals.FEATURE_STATUE))
					{
						if(party.getSize() == 1)
						{
							currItemPlayer = 0;
							currItemIndex = 0;
							currGoldOffering.delete(0, currGoldOffering.length());
							setCurrGameMode(Globals.MODE_ASK, Globals.SUBMODE_ASK_SELECT_ITEM);
						}
						else
						{
							// select player to ask statue about item
							currSelNotice = Globals.getDungeonText("pickPlayerAskStatue");
							setCurrGameMode(Globals.MODE_SELECT_PLAYER, Globals.SUBMODE_PLAYER_ASK);
						}
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noStatue"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_OPEN_VAULT)
				{
					if(currRoom != null && currRoom.hasVault())
					{
						if(party.getSize() == 1)
						{
							currItemPlayer = 0;
							sbVaultGuess.delete(0, sbVaultGuess.length());
							vcVaultGuesses.clear();
							setCurrGameMode(Globals.MODE_VAULT, Globals.SUBMODE_VAULT_GUESS);
						}
						else
						{
							// select player to open vault
							currSelNotice = Globals.getDungeonText("pickPlayerOpenVault");
							setCurrGameMode(Globals.MODE_SELECT_PLAYER, Globals.SUBMODE_PLAYER_VAULT);
						}
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noVault"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_SHOP)
				{
					if(currRoom != null && currRoom.getFeatureType() == Globals.FEATURE_SHOP)
					{
						currShopPlayer = 0;
						setCurrGameMode(Globals.MODE_SHOP, Globals.SUBMODE_SHOP_MAIN);
						setCurrentMessage(Globals.getDungeonText("shopChangeCharNav"));
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_ORDER)
				{
					if(party.getSize() > 1)
					{
						currPlayerPtr = -1;
						setCurrGameMode(Globals.MODE_ORDER, Globals.SUBMODE_ORDER_SELECT);
					}
				}
				else if(ke.getKeyCode() == Globals.KEY_MENU_SAVE && gameInProgress && !inCombat())
				{
					setCurrGameMode(Globals.MODE_FILE_SAVE, Globals.SUBMODE_SAVE_FILE);
					Vector<String> vcSavegames = getSaveGameFilenames();
					currMenuIndex = 0;
					currMenuItem  = "";
					for(int m = 0; m < vcSavegames.size(); m++)
					{
						if(vcSavegames.elementAt(m).equals(currSaveFile))
						{
							currMenuIndex = m + 1; // add a 1 because the "(new save)" item will push all other entries down 1
						}
					}
				}
				// some key, particularly movement keys, work differently in hallways, rooms, and combat situations
				else if(currBoard.getGridValue(party.getLocationX(), party.getLocationY()) > Globals.MAP_SOLID && currBoard.getGridValue(party.getLocationX(), party.getLocationY()) < Globals.MAP_ROOM)
				{
					// hallway movement
					if(ke.getKeyCode() == Globals.KEY_UP)
					{
						performHallwayForwardMove();
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						party.setFacing(party.getFacing() + (Globals.CARDINALS_HALL.size() / 2));
						if(party.getFacing() > Globals.CARDINAL_MAX)
						{
							party.setFacing(party.getFacing() - Globals.CARDINALS_HALL.size());
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_LEFT)
					{
						party.setFacing(party.getFacing() - 1);
						if(party.getFacing() < Globals.CARDINAL_MIN)
						{
							party.setFacing(Globals.CARDINAL_MAX);
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_RIGHT)
					{
						party.setFacing(party.getFacing() + 1);
						if(party.getFacing() > Globals.CARDINAL_MAX)
						{
							party.setFacing(Globals.CARDINAL_MIN);
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_LISTEN)
					{
						if(bSoundOn)
						{
							// checks if facing door
							int newX = party.getLocationX() + Globals.CPVECTS[party.getFacing()].getX();
							int newY = party.getLocationY() + Globals.CPVECTS[party.getFacing()].getY();
							if(newX >= 0 && newX < currBoard.getWidth() && newY >= 0 && newY < currBoard.getHeight())
							{
								int sqrType = currBoard.getGridValue(party.getLocationX(), party.getLocationY());
								if((sqrType & Globals.FACINGS[party.getFacing()]) == Globals.FACINGS[party.getFacing()])
								{
									if(isLocationRoom(currBoard, newX, newY))
									{
										Room tmpRoom = currBoard.getRoom(newX, newY);
										if(tmpRoom.getMonsterCount() > 0)
										{
											// play monster sample for creatures in room
											playSound(Globals.CURR_DUNGEON.getMonsterSound(tmpRoom.getMonster(0).getSoundNumber()));
										}
										else
										{
											setCurrentMessage(Globals.getDungeonText("listenHearNothing"));
										}
									}
									else
									{
										setCurrentMessage(Globals.getDungeonText("listenNotFacingRoom"));
									}
								}
								else
								{
									setCurrentMessage(Globals.getDungeonText("listenNotFacingRoom"));
								}
							}
							else
							{
								setCurrentMessage(Globals.getDungeonText("listenNotFacingRoom"));
							}
							elapseRound();
						}
						else
						{
							setCurrentMessage(Globals.getDungeonText("noListenWithoutSound"));
						}
					}
					else if(ke.getKeyCode() == Globals.KEY_BREAK)
					{
						// checks if facing door
						int newX = party.getLocationX() + Globals.CPVECTS[party.getFacing()].getX();
						int newY = party.getLocationY() + Globals.CPVECTS[party.getFacing()].getY();
						if(newX >= 0 && newX < currBoard.getWidth() && newY >= 0 && newY < currBoard.getHeight())
						{
							int sqrType = currBoard.getGridValue(party.getLocationX(), party.getLocationY());
							if((sqrType & Globals.FACINGS[party.getFacing()]) == Globals.FACINGS[party.getFacing()])
							{
								// if entering a room, set initial party member positions as well
								if(isLocationRoom(currBoard, newX, newY))
								{
									party.setLocation(newX, newY);
									performRoomEnterMove(true);
									elapseRound();
								}
								else
								{
									setCurrentMessage(Globals.getDungeonText("noDoorToBreak"));
								}
							}
						}
					}
				}
				else if(isLocationRoom(currBoard, party.getLocation()))
				{
					// room movement
					if(ke.getKeyCode() == Globals.KEY_UP)
					{
						performRoomExitMove(Globals.NORTH);
					}
					else if(ke.getKeyCode() == Globals.KEY_DOWN)
					{
						performRoomExitMove(Globals.SOUTH);
					}
					else if(ke.getKeyCode() == Globals.KEY_LEFT)
					{
						performRoomExitMove(Globals.WEST);
					}
					else if(ke.getKeyCode() == Globals.KEY_RIGHT)
					{
						performRoomExitMove(Globals.EAST);
					}
					else if(ke.getKeyCode() == Globals.KEY_STAIRS)
					{
						if(currRoom.getStairType() == Globals.STAIRS_DOWN)
						{
							if(party.getDepth() == 0 || currBoard.isMapFound())
							{
								party.setDepth(party.getDepth() + 1);
								currBoard = Globals.CURR_DUNGEON.getLevel(party.getDepth());
								if(isLocationRoom(currBoard, party.getLocation()))
								{
									resetPositions = true;
									performRoomEnterMove();
								}
								songBkgrnd = playMusic(songBkgrnd, Globals.CURR_DUNGEON.getBackgroundMusic(party.getDepth()), true, true);
								elapseRound();
							}
							else
							{
								setCurrentMessage(Globals.getDungeonText("noDescendWithoutMap"));
							}
						}
						else if(currRoom.getStairType() == Globals.STAIRS_UP)
						{
							party.setDepth(party.getDepth() - 1);
							currBoard = Globals.CURR_DUNGEON.getLevel(party.getDepth());
							if(isLocationRoom(currBoard, party.getLocation()))
							{
								resetPositions = true;
								performRoomEnterMove();
							}
							if(party.getDepth() == 0)
							{
								checkAllQuestStatus();
							}
							songBkgrnd = playMusic(songBkgrnd, Globals.CURR_DUNGEON.getBackgroundMusic(party.getDepth()), true, true);
							elapseRound();
						}
						else
						{
							// no down stairs, so ignore move
						}
					}
				}
			}
		}
		updateScreen();
	}

//  Exploration Mode Methods -------------------------------------------------/

	public boolean isLocationRoom(Board board, int x, int y)
	{
		return (board.getGridValue(x, y) & Globals.MAP_ROOM) == Globals.MAP_ROOM;
	}

	public boolean isLocationRoom(Board board, Coord cp)
	{
		return isLocationRoom(board, cp.getX(), cp.getY());
	}

	public void performHallwayForwardMove()
	{
		// checks if exiting current tile in facing direction
		int newX = party.getLocationX() + Globals.CPVECTS[party.getFacing()].getX();
		int newY = party.getLocationY() + Globals.CPVECTS[party.getFacing()].getY();
		if(newX >= 0 && newX < currBoard.getWidth() && newY >= 0 && newY < currBoard.getHeight())
		{
			int sqrType = currBoard.getGridValue(party.getLocationX(), party.getLocationY());
			if((sqrType & Globals.FACINGS[party.getFacing()]) == Globals.FACINGS[party.getFacing()])
			{
				party.setLocation(newX, newY);
				// if entering a room, set initial party member positions as well
				elapseRound();
				if(isLocationRoom(currBoard, party.getLocation()))
				{
					resetPositions = true;
					performRoomEnterMove();
				}
				else
				{
					playSound((rnd.nextInt(2) == 0 ? Globals.CURR_DUNGEON.getEnvironSound(Globals.SOUND_STEP1) : Globals.CURR_DUNGEON.getEnvironSound(Globals.SOUND_STEP2)));
					checkForRandomEncounter();
				}
			}
		}
		currBoard.setConditionalVisited(party.getLocationX(), party.getLocationY());
	}

	public void performRoomEnterMove(boolean isBreak)
	{
		currBoard.setConditionalVisited(party.getLocationX(), party.getLocationY());
		if(currBoard.getRoom(party.getLocationX(), party.getLocationY()).hasActiveMonsters())
		{
			popUpMonster(currBoard.getRoom(party.getLocationX(), party.getLocationY()).getMonster(0).getMonsterDef());
		}
		if(isLocationRoom(currBoard, party.getLocation()))
		{
			currRoom = currBoard.getRoom(party.getLocationX(), party.getLocationY());
			analyseFloorplan();
			breakMove = isBreak;
			cpItemPos.setLocation(getClearTile());
			if(currRoom.hasActiveMonsters())
			{
				beginCombatSequence();
				updateScreen();
			}
			else
			{
				resetPositions = true;
				drawRoomLayers(currRoom);
				updateScreen();
			}
		}
	}

	public void performRoomEnterMove()
	{
		performRoomEnterMove(currBoard.getRoom(party.getLocationX(), party.getLocationY()).hasActiveMonsters() ? false : true);
	}

	public void performRoomExitMove(int dir, boolean isNormalMove)
	{
		int sqrType = currBoard.getGridValue(party.getLocationX() + Globals.CPVECTS[dir].getX(), party.getLocationY() + Globals.CPVECTS[dir].getY());
		if(
			party.getLocationY() <= 0 ||
			party.getLocationY() >= (currBoard.getHeight() - 1) ||
			party.getLocationX() <= 0 ||
			party.getLocationX() >= (currBoard.getWidth() - 1) ||
			sqrType == Globals.MAP_SOLID ||
//			(!isLocationRoom(currBoard, party.getLocation()) && ((sqrType & Globals.INVERSE_FACINGS[dir]) == 0))
			(isNormalMove && (sqrType & Globals.INVERSE_FACINGS[dir]) == 0)
		)
		{
			// no exit in that direction, so ignore move
		}
		else
		{
			// move party to location corresponding to chosen exit
			party.setFacing(dir);
			party.setLocation(party.getLocationX() + Globals.CPVECTS[party.getFacing()].getX(), party.getLocationY() + Globals.CPVECTS[party.getFacing()].getY());
			elapseRound();
			// update party location status
			int planType = currBoard.getGridValue(party.getLocationX(), party.getLocationY());
			currBoard.setConditionalVisited(party.getLocationX(), party.getLocationY());
			if(planType > Globals.MAP_SOLID && planType < Globals.MAP_ROOM)
			{
				currRoom = (Room)null;
				drawHallPlan();
			}
			else if(isLocationRoom(currBoard, party.getLocation()))
			{
				performRoomEnterMove();
			}
		}
	}

	public void performRoomExitMove(int dir)
	{
		performRoomExitMove(dir, true);
	}

	public void searchForHiddenDoors()
	{
		if(currRoom != null)
		{
			if(currRoom.isTransient())
			{
				// use hallway search to expose hidden doors in transient rooms
				if(searchHallForHiddenDoors(party.getLocationX(), party.getLocationY()))
				{
					setCurrentMessage(Globals.getDungeonText("foundHiddenDoor"));
				}
				else
				{
					setCurrentMessage(Globals.getDungeonText("foundNoHiddenDoor"));
				}
				drawRoomPlan();
				updateScreen();
			}
			else
			{
				// search current room for hidden doors
				searchRoomForSecretDoors(party.getLocationX(), party.getLocationY());
			}
		}
		else
		{
			// search visible hallway for hidden doors
			simpleMapVisible(true);
		}
	}

	public void searchRoomForSecretDoors(int x, int y)
	{
		boolean foundHiddenDoor = false;
		if(currBoard.getGridValue(x, y - 1) != 0)
		{
			if((currBoard.getGridValue(x, y - 1) & Globals.FACINGS[Globals.SOUTH]) == 0)
			{
				currBoard.setGridValue(x, y - 1, currBoard.getGridValue(x, y - 1) | Globals.FACINGS[Globals.SOUTH]);
//				currBoard.setGridValue(x, y,     currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.NORTH]);
				foundHiddenDoor = true;
			}
		}
		if(currBoard.getGridValue(x, y + 1) != 0)
		{
			if((currBoard.getGridValue(x, y + 1) & Globals.FACINGS[Globals.NORTH]) == 0)
			{
				currBoard.setGridValue(x, y + 1, currBoard.getGridValue(x, y + 1) | Globals.FACINGS[Globals.NORTH]);
//				currBoard.setGridValue(x, y,     currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.SOUTH]);
				foundHiddenDoor = true;
			}
		}
		if(currBoard.getGridValue(x - 1, y) != 0)
		{
			if((currBoard.getGridValue(x - 1, y) & Globals.FACINGS[Globals.EAST]) == 0)
			{
				currBoard.setGridValue(x - 1, y, currBoard.getGridValue(x - 1, y) | Globals.FACINGS[Globals.EAST]);
//				currBoard.setGridValue(x,     y, currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.WEST]);
				foundHiddenDoor = true;
			}
		}
		if(currBoard.getGridValue(x + 1, y) != 0)
		{
			if((currBoard.getGridValue(x + 1, y) & Globals.FACINGS[Globals.WEST]) == 0)
			{
				currBoard.setGridValue(x + 1, y, currBoard.getGridValue(x + 1, y) | Globals.FACINGS[Globals.WEST]);
//				currBoard.setGridValue(x,     y, currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.EAST]);
				foundHiddenDoor = true;
			}
		}
		if(foundHiddenDoor)
		{
			setCurrentMessage(Globals.getDungeonText("foundHiddenDoor"));
			drawRoomPlan();
		}
		else
		{
			setCurrentMessage(Globals.getDungeonText("foundNoHiddenDoor"));
		}
		updateScreen();
	}

	public boolean searchHallForHiddenDoors(int x, int y)
	{
		boolean foundHiddenDoor = false;
		if(isLocationRoom(currBoard, x, y - 1))
		{
			if((currBoard.getGridValue(x, y) & Globals.FACINGS[Globals.NORTH]) == 0)
			{
				currBoard.setGridValue(x, y,     currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.NORTH]);
//				currBoard.setGridValue(x, y - 1, currBoard.getGridValue(x, y - 1) | Globals.FACINGS[Globals.SOUTH]);
				foundHiddenDoor = true;
			}
		}
		if(isLocationRoom(currBoard, x, y + 1))
		{
			if((currBoard.getGridValue(x, y) & Globals.FACINGS[Globals.SOUTH]) == 0)
			{
				currBoard.setGridValue(x, y,     currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.SOUTH]);
//				currBoard.setGridValue(x, y + 1, currBoard.getGridValue(x, y + 1) | Globals.FACINGS[Globals.NORTH]);
				foundHiddenDoor = true;
			}
		}
		if(isLocationRoom(currBoard, x - 1, y))
		{
			if((currBoard.getGridValue(x, y) & Globals.FACINGS[Globals.WEST]) == 0)
			{
				currBoard.setGridValue(x,     y, currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.WEST]);
//				currBoard.setGridValue(x - 1, y, currBoard.getGridValue(x - 1, y) | Globals.FACINGS[Globals.EAST]);
				foundHiddenDoor = true;
			}
		}
		if(isLocationRoom(currBoard, x + 1, y))
		{
			if((currBoard.getGridValue(x, y) & Globals.FACINGS[Globals.EAST]) == 0)
			{
				currBoard.setGridValue(x, y, currBoard.getGridValue(x, y) | Globals.FACINGS[Globals.EAST]);
//				currBoard.setGridValue(x + 1, y, currBoard.getGridValue(x + 1, y) | Globals.FACINGS[Globals.WEST]);
				foundHiddenDoor = true;
			}
		}
		if(foundHiddenDoor) { setCurrentMessage(Globals.getDungeonText("foundHiddenDoor")); }
		return foundHiddenDoor;
	}

	public boolean isInsideRoom(int posX, int posY)
	{
		return ((posX >= currFloorplan.getAreaStartX()) && (posX <= currFloorplan.getAreaEndX()) && (posY >= currFloorplan.getAreaStartY()) && (posY <= currFloorplan.getAreaEndY()));
	}

	public boolean isInsideRoom(Coord cp)
	{
		return isInsideRoom(cp.getX(), cp.getY());
	}

	public void elapseRound()
	{
		// handle party round events
		party.elapseRound();
		// update quest timer status
		boolean questStatusChange = false;
		if(Globals.CURR_DUNGEON.getQuestCount() > 0)
		{
			for(int i = 0; i < Globals.CURR_DUNGEON.getQuestCount(); i++)
			{
				Quest quest = Globals.CURR_DUNGEON.getQuest(i);
				if(quest.isPartial() || (quest.getStatus() != Globals.QUEST_ONGOING) || (quest.getTurns() == Globals.QUEST_NOTIMELIMIT))
				{
					// don't bother checking for time expiry
				}
				else
				{
					if((quest.getTurns() - party.getTurnsElapsed()) - (party.getRoundsElapsed() > 0 ? 1 : 0) < 0)
					{
						quest.setStatus(Globals.QUEST_FAILED);
						checkNoQuestsOngoing();
						currNotification = quest.getNotificationString();
						currNotificationImage = quest.getImage();
						setCurrentMessage(Globals.getDungeonText("notificationClose"));
						setCurrGameMode(Globals.MODE_SHOW_NOTICE, Globals.SUBMODE_NONE);
						expireQuestItem(quest, i);
					}
				}
			}
		}
	}

	public void expireQuestItem(Quest quest, int questNum)
	{
		Room  rPtr = Globals.CURR_DUNGEON.getLevel(quest.getLevel()).getRoom(quest.getRoom().getX(), quest.getRoom().getY());
		for(int rmItems = rPtr.getItemCount() - 1; rmItems >= 0; rmItems--)
		{
			BaseItem bitemPtr = rPtr.getItem(rmItems);
			if(bitemPtr instanceof QuestItem)
			{
				if(((QuestItem)bitemPtr).getQuestNumber() == questNum)
				{
					rPtr.loseItem(bitemPtr);
				}
			}
		}
	}

//  Enemy Action Processor ---------------------------------------------------/

	public void performMonsterActions()
	{
		// return if no longer in room (this includes transient rooms created by wandering monster encounters)
		if(currRoom == null) { return; }
		// return if no more active monsters
		if(!currRoom.hasActiveMonsters()) { return; }
		// process monster actions
		int     actionChoice = Globals.ACTION_DECIDE;
		int     speed        = 0;
		boolean isAdjacent   = false; // true if monster is next to one or more player
		int[]   playerDist   = new int[party.getSize()];
		boolean mWillMove    = false; // will monster will move this turn, based on probability of moving
		for(int i = 0; i < currRoom.getMonsterCount(); i++)
		{
			Monster mPtr = currRoom.getMonster(i);
			// locally store monster's speed
			speed = mPtr.getCurrSpeed();
			if(mPtr.isAlive())
			{
				while(speed > 0)
				{
					// get chance of moving this turn
					mWillMove = (rnd.nextInt(100) < mPtr.getMobility());
					// reset action status variables
					actionChoice = Globals.ACTION_DECIDE;
					isAdjacent   = false;
					// get distance from this monster to each player
					playerDist = getPlayerDistances(mPtr.getLocation());
					// see if monster is adjacent to any player
					for(int p = 0; p < playerDist.length; p++)
					{
						if(playerDist[p] <= 1 && playerDist[p] != Globals.OUT_OF_RANGE) { isAdjacent = true; }
					}
					// first, possibly move monsters if not adjacent to a player and either regular or special attack is non-ranged
					if(!isAdjacent && mWillMove && (speed > 1) && (!(mPtr.isRanged()) || !(mPtr.isEffectRanged())))
					{
						actionChoice = Globals.ACTION_MOVE;
					}
					// otherwise, see if monster performs their special action as their first choice
					else if(rnd.nextInt(100) < mPtr.getEffectPer())
					{
						if(isAdjacent || mPtr.isEffectRanged())
						{
							actionChoice = Globals.ACTION_EFFECT;
						}
					}
					if(actionChoice == Globals.ACTION_DECIDE)
					{
						// see if monster can attack
						if(isAdjacent || (mPtr.isRanged() && (rnd.nextInt(200) > mPtr.getMobility())))
						{
							actionChoice = Globals.ACTION_ATTACK;
						}
						else
						{
							// move, if it makes its MOBILITY roll
							if(mWillMove && speed > 1)
							{
								actionChoice = Globals.ACTION_MOVE;
							}
							else
							{
								actionChoice = Globals.ACTION_NONE;
							}
						}
					}
					if(actionChoice == Globals.ACTION_NONE)
					{
						speed = 0;
					}
					else if(actionChoice == Globals.ACTION_ATTACK)
					{
						doMonsterAttack(mPtr, playerDist);
						speed -= (mPtr.isRanged() ? Globals.ACTION_COST_ATTACK_RANGED : Globals.ACTION_COST_ATTACK_MELEE);
					}
					else if(actionChoice == Globals.ACTION_MOVE)
					{
						int moveAmount = doMonsterMove(mPtr, speed, playerDist);
						speed -= Math.max((moveAmount * Globals.ACTION_COST_MOVE), 1);
					}
					else if(actionChoice == Globals.ACTION_EFFECT)
					{
						doMonsterEffect(mPtr, playerDist);
						speed -= Globals.ACTION_COST_EFFECT;
					}
					else
					{
						speed -= 1;
					}
				}
			}
		}
	}

	public boolean playerIsInsideRoom(Player player)
	{
		return (player != null && player.isAlive() && isInsideRoom(player.getLocation()));
	}

	public int[] getPlayerDistances(Coord cp)
	{
		int[] dists = new int[party.getSize()];
		for(int i = 0; i < party.getSize(); i++)
		{
			if(party.getPlayer(i).isAlive() && playerIsInsideRoom(party.getPlayer(i)))
			{
				int xDist = Math.abs(cp.getX() - party.getPlayer(i).getLocation().getX());
				int yDist = Math.abs(cp.getY() - party.getPlayer(i).getLocation().getY());
				// adjacent values are set to 0, monsters count diagonals as adjacent as well
				if(xDist <= 2 && yDist <= 2)
				{
					dists[i] = 0;
				}
				else
				{
					dists[i] = (xDist + yDist) / 2;
				}
			}
			else
			{
				dists[i] = Globals.OUT_OF_RANGE;
			}
		}
		return dists;
	}

	public int doMonsterMove(Monster monster, int speed, int[] playerDists)
	{
		int targetNum = 0;
		int moveCount = 0;
		boolean moving = true;
		int moveAction     = Globals.NONE;
		int moveActionAlt1 = Globals.NONE;
		int moveActionAlt2 = Globals.NONE;
		int xDiff = 0;
		int yDiff = 0;
		while(moving && moveCount < (speed - 1))
		{
			// Simple Targetting - goes after closest player, flips a coin if two equidistant
			targetNum = 0;
			for(int i = 1; i < playerDists.length; i++)
			{
				if(playerDists[i] < playerDists[targetNum])
				{
					// player is closest so far, make them the priority
					targetNum = i;
				}
				else if(playerDists[i] == playerDists[targetNum])
				{
					// player is as close as another, 50% of switching to this one instead
					if(rnd.nextInt(2) == 1)
					{
						targetNum = i;
					}
				}
			}
			// determine path to target player
			moveAction     = Globals.MOVE_NONE;
			moveActionAlt1 = Globals.MOVE_NONE;
			moveActionAlt2 = Globals.MOVE_NONE;
			xDiff = (monster.getLocation().getX() - party.getPlayer(targetNum).getLocation().getX());
			yDiff = (monster.getLocation().getY() - party.getPlayer(targetNum).getLocation().getY());
			if(Math.abs(xDiff) <= 1 && Math.abs(yDiff) <= 1)
			{
				moving = false;
			}
			else
			{
				int randomAlt = rnd.nextInt(2);
				if(xDiff > 0)
				{
					if(yDiff > 0)
					{
						moveAction     = Globals.MOVE_NORTHWEST;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_NORTH : Globals.MOVE_WEST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_WEST  : Globals.MOVE_NORTH);
					}
					else if(yDiff < 0)
					{
						moveAction     = Globals.MOVE_SOUTHWEST;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_SOUTH : Globals.MOVE_WEST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_WEST  : Globals.MOVE_SOUTH);
					}
					else
					{
						moveAction     = Globals.MOVE_WEST;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_NORTHWEST : Globals.MOVE_SOUTHWEST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_SOUTHWEST : Globals.MOVE_NORTHWEST);
					}
				}
				else if(xDiff < 0)
				{
					if(yDiff > 0)
					{
						moveAction     = Globals.MOVE_NORTHEAST;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_NORTH : Globals.MOVE_EAST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_EAST  : Globals.MOVE_NORTH);
					}
					else if(yDiff < 0)
					{
						moveAction     = Globals.MOVE_SOUTHEAST;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_SOUTH : Globals.MOVE_EAST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_EAST  : Globals.MOVE_SOUTH);
					}
					else
					{
						moveAction     = Globals.MOVE_EAST;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_NORTHEAST : Globals.MOVE_SOUTHEAST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_SOUTHEAST : Globals.MOVE_NORTHEAST);
					}
				}
				else
				{
					if(yDiff > 0)
					{
						moveAction     = Globals.MOVE_NORTH;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_NORTHEAST : Globals.MOVE_NORTHWEST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_NORTHWEST : Globals.MOVE_NORTHEAST);
					}
					else if(yDiff < 0)
					{
						moveAction     = Globals.MOVE_SOUTH;
						moveActionAlt1 = (randomAlt == 1 ? Globals.MOVE_SOUTHEAST : Globals.MOVE_SOUTHWEST);
						moveActionAlt2 = (randomAlt == 1 ? Globals.MOVE_SOUTHWEST : Globals.MOVE_SOUTHEAST);
					}
					else
					{
						// should never get here in reality
						moveAction     = Globals.MOVE_NONE;
						moveActionAlt1 = Globals.MOVE_NONE;
						moveActionAlt2 = Globals.MOVE_NONE;
						moving = false;
					}
				}
				if(checkMonsterMove(monster, moveAction))
				{
					monster.setLocation(monster.getLocation().getX() + (Globals.CPMOVEVECTS[moveAction].getX() * 2), monster.getLocation().getY() + (Globals.CPMOVEVECTS[moveAction].getY() * 2));
				}
				else if(checkMonsterMove(monster, moveActionAlt1))
				{
					monster.setLocation(monster.getLocation().getX() + (Globals.CPMOVEVECTS[moveActionAlt1].getX() * 2), monster.getLocation().getY() + (Globals.CPMOVEVECTS[moveActionAlt1].getY() * 2));
				}
				else if(checkMonsterMove(monster, moveActionAlt2))
				{
					monster.setLocation(monster.getLocation().getX() + (Globals.CPMOVEVECTS[moveActionAlt2].getX() * 2), monster.getLocation().getY() + (Globals.CPMOVEVECTS[moveActionAlt2].getY() * 2));
				}
				moveCount++;
			}
		}
		return moveCount;
	}

	public void doMonsterAttack(Monster monster, int[] playerDists)
	{
		int targetNum  = -1;
		int targetTest = 0;
		if(monster.isRanged())
		{
			targetNum = rnd.nextInt(playerDists.length);
			while((!party.getPlayer(targetNum).isAlive() || playerDists[targetNum] == Globals.OUT_OF_RANGE) && targetTest < playerDists.length + 1)
			{
				targetNum++;
				if(targetNum >= playerDists.length) { targetNum = 0; }
				targetTest++;
			}
			if(targetTest >= playerDists.length + 1)
			{
				targetNum = -1;
			}
		}
		else
		{
			for(int i = 0; i < playerDists.length; i++)
			{
				if(party.getPlayer(i).isAlive() && playerDists[i] != Globals.OUT_OF_RANGE)
				{
					if(targetNum == -1)
					{
						targetNum = i;
					}
					else if(playerDists[i] < playerDists[targetNum])
					{
						// player is closest so far, make them the priority
						targetNum = i;
					}
					else if(playerDists[i] == playerDists[targetNum])
					{
						// player is as close as another, 50% of switching to this one instead
						if(rnd.nextInt(2) == 1)
						{
							targetNum = i;
						}
					}
				}
			}
			if(playerDists[targetNum] > 0)
			{
				targetNum = -1;
			}
		}
		if(targetNum > -1)
		{
			performAttack(monster, party.getPlayer(targetNum));
		}
	}

	public void doMonsterEffect(Monster monster, int[] playerDists)
	{
		monster.setRenderMode(Globals.RENDER_MODE_EFFECT);
		if(monster.getEffect().getTarget() == Globals.FX_TARGET_NONE || monster.getEffect().getTarget() == Globals.FX_TARGET_SYSTEM)
		{
			processEffect(monster.getEffect(), (Lifeform)null, monster);
		}
		else if(monster.getEffect().getTarget() == Globals.FX_TARGET_SELF)
		{
			processEffect(monster.getEffect(), monster);
		}
		else if(monster.getEffect().getTarget() == Globals.FX_TARGET_ALLPLAYERS)
		{
			for(int i = party.getSize() - 1; i >= 0; i--)
			{
				processEffect(monster.getEffect(), party.getPlayer(i), monster);
			}
		}
		else if(monster.getEffect().getTarget() == Globals.FX_TARGET_ALLMONSTERS)
		{
			for(int i = currRoom.getMonsterCount() - 1; i >= 0; i--)
			{
				processEffect(monster.getEffect(), currRoom.getMonster(i), monster);
			}
		}
		else if(monster.getEffect().getTarget() == Globals.FX_TARGET_EVERYONE)
		{
			for(int i = party.getSize() - 1; i >= 0; i--)
			{
				processEffect(monster.getEffect(), party.getPlayer(i), monster);
			}
			for(int i = currRoom.getMonsterCount() - 1; i >= 0; i--)
			{
				processEffect(monster.getEffect(), currRoom.getMonster(i), monster);
			}
		}
		else if(monster.getEffect().getTarget() == Globals.FX_TARGET_ONEPLAYER)
		{
			int targetNum  = -1;
			int targetTest = 0;
			if(monster.isEffectRanged())
			{
				targetNum = rnd.nextInt(playerDists.length);
				while((!party.getPlayer(targetNum).isAlive() || playerDists[targetNum] == Globals.OUT_OF_RANGE) && targetTest < playerDists.length + 1)
				{
					targetNum++;
					if(targetNum >= playerDists.length) { targetNum = 0; }
					targetTest++;
				}
				if(targetTest >= playerDists.length + 1)
				{
					targetNum = -1;
				}
			}
			else
			{
				for(int i = 0; i < playerDists.length; i++)
				{
					if(party.getPlayer(i).isAlive() && playerDists[i] != Globals.OUT_OF_RANGE)
					{
						if(targetNum == -1)
						{
							targetNum = i;
						}
						else if(playerDists[i] < playerDists[targetNum])
						{
							// player is closest so far, make them the priority
							targetNum = i;
						}
						else if(playerDists[i] == playerDists[targetNum])
						{
							// player is as close as another, 50% of switching to this one instead
							if(rnd.nextInt(2) == 1)
							{
								targetNum = i;
							}
						}
					}
				}
				if(playerDists[targetNum] > 0)
				{
					targetNum = -1;
				}
			}
			if(targetNum > -1)
			{
				processEffect(monster.getEffect(), party.getPlayer(targetNum), monster);
			}
		}
		else if(monster.getEffect().getTarget() == Globals.FX_TARGET_ONEMONSTER)
		{
			int targetNum  = -1;
			if(monster.isEffectRanged())
			{
				targetNum = rnd.nextInt(currRoom.getMonsterCount());
			}
			else
			{
				for(int i = 0; i < currRoom.getMonsterCount(); i++)
				{
					int xDist = Math.abs(monster.getLocation().getX() - currRoom.getMonster(i).getLocation().getX());
					int yDist = Math.abs(monster.getLocation().getY() - currRoom.getMonster(i).getLocation().getY());
					// adjacent values are set to 0, monsters count diagonals as adjacent as well
					if(!(xDist == 0 && yDist == 0) && xDist <= 2 && yDist <= 2)
					{
						targetNum = i;
					}
				}
			}
			if(targetNum > -1)
			{
				processEffect(monster.getEffect(), currRoom.getMonster(targetNum), monster);
			}
		}
		monster.setRenderMode(Globals.RENDER_MODE_NORMAL);
	}

	/** Player room movement check
	  */
	public int movePlayerInCombat(Player player, int moveAction)
	{
		int actionCost = Globals.ACTION_COST_MOVE;
		int nextSpaceX = player.getLocation().getX() + (Globals.CPVECTS[moveAction].getX() * 2);
		int nextSpaceY = player.getLocation().getY() + (Globals.CPVECTS[moveAction].getY() * 2);
		int openCount = 0;
		int doorCount = 0;
		int wallCount = 0;
		for(int y = nextSpaceY; y <= nextSpaceY + 1; y++)
		{
			for(int x = nextSpaceX; x <= nextSpaceX + 1; x++)
			{
				if(currFloorplan.getLayoutType(x, y) == Globals.ROOM_IMPASS)
				{
					wallCount++;
				}
				else if(currFloorplan.getLayoutType(x, y) == Globals.ROOM_DOOR)
				{
					doorCount++;
				}
				if(currFloorplan.getLayoutType(x, y) == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(x, y) == Globals.ROOM_OPEN_PLAYER)
				{
					openCount++;
				}
			}
		}
		if(wallCount > 0)
		{
			// illegal move, end here
		}
		else if(doorCount > 0)
		{
			// note - doors work two ways - if space is occupied on other side, do nothing, but if not occupied, player has to move another tilespace, so that they aren't on the door itself
			nextSpaceX = nextSpaceX + Globals.CPVECTS[moveAction].getX();
			nextSpaceY = nextSpaceY + Globals.CPVECTS[moveAction].getY();
			openCount = 0;
			doorCount = 0;
			wallCount = 0;
			for(int y = nextSpaceY; y <= nextSpaceY + 1; y++)
			{
				for(int x = nextSpaceX; x <= nextSpaceX + 1; x++)
				{
					if(currFloorplan.getLayoutType(x, y) == Globals.ROOM_IMPASS)
					{
						wallCount++;
					}
					else if(currFloorplan.getLayoutType(x, y) == Globals.ROOM_DOOR)
					{
						doorCount++;
					}
					if(currFloorplan.getLayoutType(x, y) == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(x, y) == Globals.ROOM_OPEN_PLAYER)
					{
						openCount++;
					}
				}
			}
			if(wallCount > 0)
			{
				// illegal move, end here
			}
			else if(doorCount > 0)
			{
				// illegal move, end here
			}
			else if(openCount == 4)
			{
				// check if space is occupied
				//   if occupied by item, do nothing
				if(checkIfItemBlockingSpace(nextSpaceX, nextSpaceY))
				{
					// bumped into items, wasted move
				}
				else
				{
					//   if occupied by lifeform, cannot enter
					//   if occupied by friend, do nothing
					Lifeform blocker = checkIfLifeformOccupies(nextSpaceX, nextSpaceY);
					if(blocker == null)
					{
						if(player.canPerformMove())
						{
							player.setLocation(nextSpaceX, nextSpaceY);
						}
					}
					else
					{
						String sMessage = Globals.getDungeonText("moveDoorwayBlocked");
						sMessage = substituteInMessage(sMessage, "$PLAYER", player.getName());
						setCurrentMessage(sMessage);
					}
				}
			}
			else
			{
				// should never happen, but if it does, no move allowed
			}
		}
		else if(openCount == 4)
		{
			// check if space is occupied
			//   if occupied by enemy, attack
			//   if occupied by friend, do nothing
			Lifeform blocker = checkIfLifeformOccupies(nextSpaceX, nextSpaceY);
			if(blocker == null)
			{
				// if occupied by item, do nothing
				if(checkIfItemBlockingSpace(nextSpaceX, nextSpaceY))
				{
					// bumped into items, wasted move
				}
				else
				{
					if(player.canPerformMove())
					{
						player.setLocation(nextSpaceX, nextSpaceY);
					}
				}
			}
			else if(blocker.getType() == Globals.LIFEFORM_PLAYER)
			{
				// bumped into teammate, wasted move
			}
			else if(blocker.getType() == Globals.LIFEFORM_MONSTER)
			{
				// FIGHT!
				if(!player.getEquiptWeapon().isRanged())
				{
					performAttack(player, blocker);
					actionCost = Globals.ACTION_COST_ATTACK_MELEE;
				}
				else if(!player.getAlternWeapon().isRanged())
				{
					party.getPlayer(currPlayerPtr).swapActiveWeapon();
					performAttack(player, blocker);
					actionCost = Globals.ACTION_COST_ATTACK_MELEE;
				}
				else
				{
					String sMessage = Globals.getDungeonText("combatNoMeleeWeapon");
					sMessage = substituteInMessage(sMessage, "$ATTACKER", player.getName());
					setCurrentMessage(sMessage);
				}
			}
		}
		else
		{
			// should never happen, but if it does, no move allowed
		}
		return actionCost;
	}

	/** Monster room movement logic
	  *   Note that this is significantly different from how Players move (especially with regards to doors),
	  *   so it gets its own method, rather than cluttering up a common method with a bunch of type() tests and branching ifs
	  */
	public boolean checkMonsterMove(Monster monster, int moveAction)
	{
		int nextSpaceX = monster.getLocation().getX() + (Globals.CPMOVEVECTS[moveAction].getX() * 2);
		int nextSpaceY = monster.getLocation().getY() + (Globals.CPMOVEVECTS[moveAction].getY() * 2);
		for(int y = nextSpaceY; y <= nextSpaceY + 1; y++)
		{
			for(int x = nextSpaceX; x <= nextSpaceX + 1; x++)
			{
				if(currFloorplan.getLayoutType(x, y) != Globals.ROOM_OPEN_ALL)
				{
					// illegal move, end here
					return false;
				}
			}
		}
		// can't move onto chests or other items
		if(checkIfItemBlockingSpace(nextSpaceX, nextSpaceY)) { return false; }
		// check if space is occupied
		Lifeform blocker = checkIfLifeformOccupies(nextSpaceX, nextSpaceY);
		if(blocker == null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// update the current player pointer to the first active player who has actions left
	public void getNextActivePlayer()
	{
		while(	currPlayerPtr < (party.getSize() - 1) &&
				currPlayer != null &&
				(
					!(currPlayer.hasActionsLeft()) ||
					!(currPlayer.isAlive())
				)
			)
		{
			currPlayerPtr++;
			currPlayer = party.getPlayer(currPlayerPtr);
		}
		// check to see if that was the last player to leave room
		int playersInRoom = 0;
		for(int i = 0; i < party.getSize(); i++)
		{
			if(playerIsInsideRoom(party.getPlayer(i)))
			{
				playersInRoom++;
			}
		}
		if(playersInRoom == 0)
		{
			// check that all players left by the same door
			int exitCardinal = Globals.NONE;
			for(int i = 0; i < party.getSize(); i++)
			{
				int testCardinal = Globals.NONE;
				if(party.getPlayer(i).isAlive())
				{
					if     (party.getPlayer(i).getLocation().getX() < currFloorplan.getAreaStartX()) { testCardinal = Globals.WEST; }
					else if(party.getPlayer(i).getLocation().getX() > currFloorplan.getAreaEndX())   { testCardinal = Globals.EAST; }
					else if(party.getPlayer(i).getLocation().getY() < currFloorplan.getAreaStartY()) { testCardinal = Globals.NORTH; }
					else if(party.getPlayer(i).getLocation().getY() > currFloorplan.getAreaEndY())   { testCardinal = Globals.SOUTH; }
					if(testCardinal != Globals.NONE)
					{
						if(exitCardinal == Globals.NONE || exitCardinal == testCardinal)
						{
							exitCardinal = testCardinal;
						}
						else
						{
							exitCardinal = Globals.BAD;
						}
					}
				}
			}
			if(exitCardinal != Globals.NONE && exitCardinal != Globals.BAD)
			{
				concludeCombat(Globals.getDungeonText("combatEscape"), false);
				performRoomExitMove(exitCardinal, !(currRoom.isTransient()));
				updateScreen();
				return;
			}
		}
		// if we looped through whole party and didn't find an active player with actions left,
		// we reset everyone's actions for the next round
		if(!(currPlayer.hasActionsLeft()) || !(currPlayer.isAlive()))
		{
			currPlayerPtr = -1;
			performMonsterActions();
			initPlayersForCombat();
			updateScreen();
		}
	}

//  Combat Methods -----------------------------------------------------------/

	// reset all players to full action points, and set current pointer to first active player
	public void initPlayersForCombat()
	{
		currPlayerPtr = -1;
		for(int p = 0; p < party.getSize(); p++)
		{
			if(party.getPlayer(p) != null && party.getPlayer(p).isAlive())
			{
				party.getPlayer(p).resetActionPoints();
				if(currPlayerPtr == -1)
				{
					currPlayerPtr = p;
				}
			}
		}
	}

	public void beginCombatSequence()
	{
		resetPositions = true;
		initPlayersForCombat();
		setCurrGameMode(Globals.MODE_COMBAT, Globals.SUBMODE_COMBAT_BASIC);
		currMonsterType = currRoom.getMonster(0).getMonsterDef();
		pauseMusic(songBkgrnd);
		int whichCombatTheme = rnd.nextInt(Globals.CURR_DUNGEON.getCombatMusicCount());
		songCombat = playMusic(songCombat, Globals.CURR_DUNGEON.getCombatMusic(whichCombatTheme), true, true);
	}

	public void performAttack(Lifeform attacker, Lifeform defender)
	{
		boolean showPause = true;
		int baseChanceToHit = getChanceToHit(attacker, defender);
		attacker.setRenderMode(Globals.RENDER_MODE_ATTACK);
		// do projectile effect if attack is projectile based
		if(attacker.getProjectileType() != Globals.PROJECTILE_NONE && defender.isAlive())
		{
			showProjectile(attacker.getProjectileType(), attacker.getProjectileColor(), (attacker.getLocation().getX() * Globals.TILESCALE) + Globals.TILESCALE + drawpointX, (attacker.getLocation().getY() * Globals.TILESCALE) + Globals.TILESCALE + drawpointY, (defender.getLocation().getX() * Globals.TILESCALE) + Globals.TILESCALE + drawpointX, (defender.getLocation().getY() * Globals.TILESCALE) + Globals.TILESCALE + drawpointY);
		}
		// clear targetting reticule, if any
		currEnemyPtr = -1;
		int roll = rnd.nextInt(100);
		if(roll < baseChanceToHit)
		{
			int damageDone = attacker.getAttackValue();
			if(damageDone > 0)
			{
				// notify player of results
				String sMessage = Globals.getDungeonText("combatHitSuccess");
				sMessage = substituteInMessage(sMessage, "$ATTACKER", attacker.getName());
				sMessage = substituteInMessage(sMessage, "$DEFENDER", defender.getName());
				sMessage = substituteInMessage(sMessage, "$DAMAGEVAL", damageDone);
				setCurrentMessage(sMessage);
				updateScreen();
				// plot hit text
				if(currRoom != null)
				{
					playSound(attacker.getAttackSound());
					showDamageEffect((damageDone > 0), defender.getLocation(), damageDone);
					showPause = false;
				}
				resolveAttackDamage(attacker, defender, damageDone);
			}
			else
			{
				// notify player of results
				playSound(Globals.CURR_DUNGEON.getEnvironSound(Globals.SOUND_MISS));
				String sMessage = Globals.getDungeonText("combatHitNoDamage");
				sMessage = substituteInMessage(sMessage, "$ATTACKER", attacker.getName());
				sMessage = substituteInMessage(sMessage, "$DEFENDER", defender.getName());
				sMessage = substituteInMessage(sMessage, "$DAMAGEVAL", damageDone);
				setCurrentMessage(sMessage);
				updateScreen();
			}
		}
		else
		{
			// notify player of results
			playSound(Globals.CURR_DUNGEON.getEnvironSound(Globals.SOUND_MISS));
			String sMessage = Globals.getDungeonText("combatHitMiss");
			sMessage = substituteInMessage(sMessage, "$ATTACKER", attacker.getName());
			sMessage = substituteInMessage(sMessage, "$DEFENDER", defender.getName());
			setCurrentMessage(sMessage);
			updateScreen();
		}
		// visual pause
		if(showPause)
		{
			doShortWait();
		}
		attacker.setRenderMode(attacker.isAlive() ? Globals.RENDER_MODE_NORMAL : Globals.RENDER_MODE_DEAD);
		updateScreen();
	}

	public int getChanceToHit(Lifeform attacker, Lifeform defender)
	{
		int baseChanceToHit = 50 + ((attacker.getAttackRank() - defender.getDefendRank()) * 5);
		if(attacker instanceof Player)
		{
			if(((Player)attacker).getEquiptWeapon().isRanged())
			{
				baseChanceToHit += (((Player)attacker).getRangeBonus() + (((Player)attacker).getLuck() / 5) + ((Player)attacker).getRangeAdd());
			}
			else
			{
				baseChanceToHit += (((Player)attacker).getMeleeBonus() + (((Player)attacker).getLuck() / 5) + ((Player)attacker).getMeleeAdd());
			}
		}
		if(defender instanceof Player)
		{
			baseChanceToHit -= (((Player)defender).getCurrentDefense() + (((Player)defender).getLuck() / 5));
		}
		// chance to hit never gets over 95%, or below 5%
		baseChanceToHit = Math.min(baseChanceToHit, 95);
		baseChanceToHit = Math.max(baseChanceToHit,  5);
		return baseChanceToHit;
	}

	public void resolveAttackDamage(Lifeform attacker, Lifeform defender, int damageAmount)
	{
		defender.adjustWounds(damageAmount);
		if(!defender.isAlive())
		{
			if(defender.getType() == Globals.LIFEFORM_MONSTER)
			{
				if(attacker.getType() == Globals.LIFEFORM_PLAYER)
				{
					((Player)attacker).adjustExp(defender.getExperience());
					updateScreen();
//					bufferStrategy.show();
					doShortWait();
				}
			}
			checkCombatConclusion();
		}
	}

	public void checkPartyStatus()
	{
		boolean partyLost = true;
		for(int i = 0; i < party.getSize(); i++)
		{
			if(party.getPlayer(i).isAlive())
			{
				partyLost = false;
			}
		}
		if(partyLost)
		{
			endGame(Globals.SUBMODE_ENDING_SLAIN);
		}
	}

	public void checkCombatConclusion()
	{
		// check if any players are still alive
		checkPartyStatus();
		// clean up any dead monsters
		for(int i = currRoom.getMonsterCount() - 1; i >= 0; i--)
		{
			if(!(currRoom.getMonster(i).isAlive()))
			{
				currRoom.delMonster(i);
			}
		}
		if(!currRoom.hasActiveMonsters())
		{
			concludeCombat(Globals.getDungeonText("combatVictory"), true);
		}
	}

	public void concludeCombat(String msg, boolean isVictory)
	{
		for(int i = 0; i < party.getSize(); i++)
		{
			party.getPlayer(i).resetSpeed();
		}
		setCurrentMessage(msg);
		if(currRoom.isTransient() && currRoom.getItemCount() > 0)
		{
			currItemIndex = 0;
			replaceCurrGameMode(Globals.MODE_ITEM, Globals.SUBMODE_ITEM_PICKUP);
		}
		else
		{
			while(currGameMode != Globals.MODE_EXPLORE)
			{
				getPrevGameMode();
			}
			if(isVictory)
			{
				checkGoldDrop(currMonsterType);
				currMonsterType = (MonsterDef)null;
			}
		}
		stopMusic(songCombat);
		resumeMusic(songBkgrnd);
	}

	public int getNextTarget(int x, int y)
	{
		boolean bLooking = true;
		int iReturn = 0;
		while(bLooking)
		{
			for(int i = 0; i < currRoom.getMonsterCount(); i++)
			{
				if(currRoom.getMonster(i).getLocation().getX() == x && currRoom.getMonster(i).getLocation().getY() == y)
				{
					bLooking = false;
					iReturn = i;
				}
			}
			x++;
			if(x > currFloorplan.getAreaEndX())
			{
				x = currFloorplan.getAreaStartX();
				y++;
				if(y > currFloorplan.getAreaEndY())
				{
					y = currFloorplan.getAreaStartY();
				}
			}
		}
		return iReturn;
	}

	public int getNextTarget()
	{
		if(currRoom != null && currFloorplan != null)
		{
			return getNextTarget(currFloorplan.getAreaStartX(), currFloorplan.getAreaStartY());
		}
		return 0;
	}

	public void checkGoldDrop(MonsterDef mdef)
	{
		if(mdef != null && currRoom != null && !(currRoom.isTransient()))
		{
			int dropOdds = 0;
			for(int i = 0; i < party.getSize(); i++)
			{
				dropOdds += party.getPlayer(i).getLuck();
			}
			dropOdds = (int)(dropOdds / party.getSize()) + Globals.CURRENCY_DROP_ODDS;
			if(Globals.getRandomValue(100) <= Math.max(10, dropOdds))
			{
				int dropQnt = Globals.getRandomValue((party.getDepth() + mdef.getLevel() + mdef.getAttack() + mdef.getDefend()) * 5 * Globals.DIFFMOD_GOLDPILE[Globals.CURR_DUNGEON.getDifficulty()], party.getDepth() * 5 * Globals.DIFFMOD_GOLDPILE[Globals.CURR_DUNGEON.getDifficulty()]);
				if(currRoom.getItemCount() < 1) { if(!isClearTile(cpItemPos)) { cpItemPos.setLocation(getClearTile()); } }
				currRoom.gainItem(Globals.ITEM_CURRENCY.getInstance(1, dropQnt));
				setCurrentMessage(Globals.getDungeonText("monsterDroppedCurrency"));
			}
		}
	}

	public void endGame(int endingType)
	{
		flushGameModeBuffer();
		setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_MAIN);
		if(endingType == Globals.SUBMODE_ENDING_SLAIN)
		{
			gameInProgress = false;
			stopMusic(songCombat);
			if(Globals.CURR_DUNGEON.getDeathMusic() != null)
			{
				songBkgrnd = playMusic(songBkgrnd, Globals.CURR_DUNGEON.getDeathMusic(), false, true);
			}
			else
			{
				songBkgrnd = playMusic(songBkgrnd, musicMenus, true, false);
			}
		}
		else
		{
			party.setGameCompleted(true);
			setCurrGameMode(Globals.MODE_EXPLORE, Globals.SUBMODE_NONE);
			currRoom.setStairType(Globals.STAIRS_NONE);
			drawRoomPlan();
		}
		setCurrGameMode(Globals.MODE_ENDING, endingType);
		updateScreen();
	}

	public boolean inCombat()
	{
		return (currRoom != null && currRoom.hasActiveMonsters());
	}

	public void checkForRandomEncounter()
	{
		if((rnd.nextInt(100) + 1) <= party.getWanderingOdds())
		{
			if(!isLocationRoom(currBoard, party.getLocation()) && !inCombat())
			{
				doRandomEncounter();
			}
		}
	}

	public void doRandomEncounter()
	{
		Vector<Monster> vcMonsters = new Vector<Monster>();
		MonsterDef mdef;
		do
		{
			mdef = Globals.getMonsterForDepth(party.getDepth(), Globals.CURR_DUNGEON.getDifficulty());
		}while(mdef.isUnique());
		int randQuant = Math.max(1, ((mdef.getMaxGroup() - 4) + party.getSize()));
		int monsterQuant = rnd.nextInt(randQuant) + 1;
		for(int i = 0; i < monsterQuant; i++)
		{
			vcMonsters.add(mdef.getInstance());
		}
		currRoom = new Room(Globals.STAIRS_NONE, Globals.FEATURE_NONE, 0, vcMonsters, new Vector<BaseItem>(), true);

		String sMessage = Globals.getDungeonText("wanderingMonsterAttack");
		sMessage = substituteInMessage(sMessage, "$MONSTER", mdef.getName());
		setCurrentMessage(sMessage);
		drawMessageWindow();

		popUpMonster(mdef);

		breakMove = true;
		beginCombatSequence();
		updateScreen();

		lastValidKeyTime = System.currentTimeMillis();
	}

	public void popUpMonster(MonsterDef mdef)
	{
		int xoffst = (int)(((Globals.CANVAS_HALL_X - Globals.ENLARGESCALE) / 2.0) * Globals.TILESCALE);
		int yoffst = (int)(((Globals.CANVAS_HALL_Y - Globals.ENLARGESCALE) / 2.0) * Globals.TILESCALE);
		bufferEnviron.getGraphics().drawImage(mdef.getImgNormal(), xoffst, yoffst, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
 		bufferStrategy.getDrawGraphics().drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
 		bufferStrategy.show();
 		playSound(Globals.CURR_DUNGEON.getMonsterSound(mdef.getSoundNumber()));
 		doLongWait();
	}

//  Item & Object Methods ----------------------------------------------------/

	public void doUseItem(Player player, int itemNum, Lifeform target)
	{
		Item itemPtr = player.getInventoryItem(itemNum);
		if(itemPtr != null)
		{
			if(!(Globals.isPermitted(player.getCharacterClass(), itemPtr.getPermissions())))
			{
				String sMsg = Globals.getDungeonText("itemNotPermitted");
				sMsg = substituteInMessage(sMsg, "$PLAYER", player.getName());
				sMsg = substituteInMessage(sMsg, "$ITEM", itemPtr.getInventoryText());
				setCurrentMessage(sMsg);
				return;
			}
			if(itemPtr.getCharges() <= 0)
			{
				player.loseItem(itemPtr);
			}
			else
			{
				boolean bUsed = false;
				if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_SYSTEM)
				{
					processEffect(itemPtr.getEffect());
					bUsed = true;
				}
				else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_SELF)
				{
					processEffect(itemPtr.getEffect(), player);
					bUsed = true;
				}
				else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_ONEPLAYER)
				{
					if(target != null && target instanceof Player)
					{
						processEffect(itemPtr.getEffect(), target, player);
						bUsed = true;
					}
				}
				else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_ALLPLAYERS)
				{
					for(int i = party.getSize() - 1; i >= 0; i--)
					{
						processEffect(itemPtr.getEffect(), party.getPlayer(i), player);
					}
					bUsed = true;
				}
				else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_ONEMONSTER)
				{
					if(target != null && target instanceof Monster && isInsideRoom(player.getLocation()))
					{
						processEffect(itemPtr.getEffect(), target, player);
						bUsed = true;
					}
				}
				else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_ALLMONSTERS && isInsideRoom(player.getLocation()))
				{
					if(currRoom != null && currRoom.hasActiveMonsters())
					{
						for(int i = currRoom.getMonsterCount() - 1; i >= 0 ; i--)
						{
							processEffect(itemPtr.getEffect(), currRoom.getMonster(i), player);
						}
						bUsed = true;
					}
				}
				else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_EVERYONE)
				{
					for(int i = party.getSize() - 1; i >= 0; i--)
					{
						processEffect(itemPtr.getEffect(), party.getPlayer(i), player);
					}
					if(currRoom != null && currRoom.hasActiveMonsters() && isInsideRoom(player.getLocation()))
					{
						for(int i = currRoom.getMonsterCount() - 1; i >= 0 ; i--)
						{
							processEffect(itemPtr.getEffect(), currRoom.getMonster(i), player);
						}
					}
					bUsed = true;
				}
				else if(itemPtr.getEffect().getTarget() == Globals.FX_TARGET_NONE)
				{
					// these don't do anything, it's a placeholder constant
					bUsed = true;
				}
				if(bUsed)
				{
					itemPtr.expendCharge();
					if(itemPtr.getCharges() <= 0)
					{
						player.loseItem(itemPtr);
					}
					else
					{
						itemPtr.identify();
					}
				}
				else
				{
					String sMsg = Globals.getDungeonText("itemNotUsed");
					sMsg = substituteInMessage(sMsg, "$ITEM", itemPtr.getInventoryText());
					setCurrentMessage(sMsg);
				}
			}
		}
		if(currItemCmbtUse > -1)
		{
			currItemCmbtUse = -1;
		}
	}

	public void doUseItem(Player player, int itemNum)
	{
		doUseItem(player, itemNum, (Lifeform)null);
	}

	public void openChest(Player playerOpener)
	{
		String[] resultMsgs = new String[2];
		int strPtr = 0;
		if(currRoom != null && currRoom.hasChest())
		{
			Chest chestPtr = currRoom.getChest();
			if(chestPtr.hasTrap())
			{
				int baseOdds = chestPtr.getTrapOdds() - (playerOpener.getTrapResist() + playerOpener.getLuck());
				if((rnd.nextInt(100) + 1) > baseOdds)
				{
					// chest opened safely
					resultMsgs[strPtr] = Globals.getDungeonText("chestTrapAvoided");
					strPtr++;
				}
				else
				{
					Effect trap = chestPtr.getTrap();
					if(trap.getTarget() == Globals.FX_TARGET_ONEPLAYER)
					{
						processEffect(trap, playerOpener);
					}
					else if(trap.getTarget() == Globals.FX_TARGET_ALLPLAYERS || trap.getTarget() == Globals.FX_TARGET_EVERYONE)
					{
						for(int i = 0; i < party.getSize(); i++)
						{
							processEffect(trap, party.getPlayer(i));
						}
					}
					String msgTemp = Globals.getDungeonText("chestTrapTriggered");
					msgTemp = substituteInMessage(msgTemp, "$EFFECT", chestPtr.getTrap().getName());
					resultMsgs[strPtr] = new String(msgTemp);
					strPtr++;
				}
			}
			// get new clear space for items
			if(!isClearTile(cpItemPos)) { cpItemPos.setLocation(getClearTile()); }
			// open Room's item container, in this case an instance of Chest
			currRoom.openItemContainer();
			// inform player of results
			if(currRoom.hasChest())
			{
				resultMsgs[strPtr] = Globals.getDungeonText("chestNotEmpty");
				if(strPtr < 1) { resultMsgs[strPtr + 1] = ""; }
			}
			else
			{
				resultMsgs[strPtr] = Globals.getDungeonText("chestOpened");
				if(strPtr < 1) { resultMsgs[strPtr + 1] = ""; }
			}
			setCurrentMessage(resultMsgs);
		}
	}

	public void openVault(Player playerOpener)
	{
		if(currRoom != null && currRoom.hasVault())
		{
			cpItemPos.setLocation(getClearTile());
			currRoom.openItemContainer();
			if(currRoom.hasVault())
			{
				setCurrentMessage(Globals.getDungeonText("vaultNotEmpty"));
			}
			else
			{
				String sMessage = Globals.getDungeonText("vaultOpened");
				sMessage = substituteInMessage(sMessage, "$PLAYER", playerOpener.getName());
				setCurrentMessage(sMessage);
			}
		}
	}

	public void drinkFromFountain(Player player)
	{
		if(currBoard.hasFountain(party.getLocationX(), party.getLocationY()) || (currRoom != null && (currRoom.getFeatureType() == Globals.FEATURE_FOUNTAIN)))
		{
			int     oddsGood = Globals.CURR_DUNGEON.getFountainOdds(party.getDepth()) + player.getLuck();
			oddsGood         = Math.min(Globals.CURR_DUNGEON.getFountainBest(), Math.max(Globals.CURR_DUNGEON.getFountainWorst(), oddsGood));
			boolean isBadFx  = rnd.nextInt(100) >= oddsGood;
			int     fxnum    = rnd.nextInt(Globals.FOUNTFX.size());
			while(isBadFx != Globals.isBadEffect(Globals.FOUNTFX.elementAt(fxnum)))
			{
				fxnum = rnd.nextInt(Globals.FOUNTFX.size());
			}
			processEffect(Globals.FOUNTFX.elementAt(fxnum), player, false);
			elapseRound();
		}
	}

	public void balanceAmmo()
	{
		if(party.getSize() > 1)
		{
			// first determine what ammos are in demand by the weapons players have equipt
			// also determine in this loop who wants those ammos
			boolean[] ammoWanted      = new boolean[Globals.INDEX_AMMOLIST.size()];
			boolean[] playerWantsAmmo = new boolean[party.getSize()];
			int[]     numberWhoWant   = new int[Globals.INDEX_AMMOLIST.size()];
			for(int a = 0; a < Globals.INDEX_AMMOLIST.size(); a++)
			{
				ammoWanted[a] = false;
				numberWhoWant[a] = 0;
				for(int p = 0; p < party.getSize(); p++)
				{
					if(party.getPlayer(p).getActiveWeapon().getAmmoType() == a || party.getPlayer(p).getAlternWeapon().getAmmoType() == a)
					{
						playerWantsAmmo[p] = true;
						ammoWanted[a] = true;
						numberWhoWant[a]++;
					}
					else
					{
						playerWantsAmmo[p] = false;
					}
				}
			}
			// now cycle through the desired ammos and see how many people want them
			for(int a = 0; a < Globals.INDEX_AMMOLIST.size(); a++)
			{
				if(ammoWanted[a])
				{
					int totalAmmo = 0;
					for(int p = 0; p < party.getSize(); p++)
					{
						totalAmmo += party.getPlayer(p).getAmmoQuantity(a);
					}
					int ammoPerPerson = totalAmmo / numberWhoWant[a];
					int ammoRemainder = totalAmmo - (ammoPerPerson * numberWhoWant[a]);
					int ammoLeftover  = 0;
					for(int p = 0; p < party.getSize(); p++)
					{
						party.getPlayer(p).setAmmoLevel(a, 0);
						if(party.getPlayer(p).getActiveWeapon().getAmmoType() == a || party.getPlayer(p).getAlternWeapon().getAmmoType() == a)
						{
							ammoLeftover += Math.max(0, ((ammoPerPerson + ammoRemainder) - Globals.MAX_AMMO));
							party.getPlayer(p).setAmmoLevel(a, Math.min(Globals.MAX_AMMO, ammoPerPerson + ammoRemainder));
							ammoRemainder = 0;
						}
					}
					if(ammoLeftover > 0)
					{
						for(int p = 0; p < party.getSize(); p++)
						{
							int ammoAdd = party.getPlayer(p).getAmmoQuantity(a);
							ammoAdd = Math.min(ammoLeftover, (Globals.MAX_AMMO - ammoAdd));
							party.getPlayer(p).setAmmoLevel(a, party.getPlayer(p).getAmmoQuantity(a) + ammoAdd);
							ammoLeftover -= ammoAdd;
						}
					}
				}
			}
		}
		setCurrentMessage(Globals.getDungeonText("ammoBalanced"));
	}

	public void getQuestItem(int questNumber)
	{
		Quest quest = Globals.CURR_DUNGEON.getQuest(questNumber);
		if(quest.getStatus() == Globals.QUEST_ONGOING)
		{
			if(quest.isPartial() && ((quest.getTurns() - party.getTurnsElapsed()) - (party.getRoundsElapsed() > 0 ? 1 : 0) < 0))
			{
				quest.setStatus(Globals.QUEST_FINISHED);
				checkNoQuestsOngoing();
			}
			else
			{
				quest.setStatus(Globals.QUEST_SUCCESS);
				checkNoQuestsOngoing();
			}
			currNotification = quest.getNotificationString();
			currNotificationImage = quest.getImage();
			setCurrentMessage(Globals.getDungeonText("notificationClose"));
			setCurrGameMode(Globals.MODE_SHOW_NOTICE, Globals.SUBMODE_NONE);
			quest.setLevel(-1);
		}
	}

	public void checkNoQuestsOngoing()
	{
		for(int i = 0; i < Globals.CURR_DUNGEON.getQuestCount(); i++)
		{
			if(Globals.CURR_DUNGEON.getQuest(i).getStatus() == Globals.QUEST_ONGOING)
			{
				return;
			}
		}
		// last quest is done (one way or another), set new wandering odds as party tries to escape the dungeon
		party.setWanderingOdds(Math.max(party.getWanderingOdds(), Globals.ESC_WANDERING_CHANCE));
	}

	public void checkAllQuestStatus()
	{
		boolean bFailed    = false;
		boolean bFinished  = false;
		boolean bSucceeded = false;
		for(int i = 0; i < Globals.CURR_DUNGEON.getQuestCount(); i++)
		{
			if(Globals.CURR_DUNGEON.getQuest(i).getStatus() == Globals.QUEST_ONGOING)
			{
				return;
			}
			else if(Globals.CURR_DUNGEON.getQuest(i).getStatus() == Globals.QUEST_SUCCESS)
			{
				bSucceeded = true;
			}
			else if(Globals.CURR_DUNGEON.getQuest(i).getStatus() == Globals.QUEST_FINISHED)
			{
				bFinished = true;
			}
			else if(Globals.CURR_DUNGEON.getQuest(i).getStatus() == Globals.QUEST_FAILED)
			{
				bFailed = true;
			}
		}
		if(bFinished || (bSucceeded && bFailed))
		{
			endGame(Globals.SUBMODE_ENDING_PARTIAL_VICTORY);
		}
		else if(bFailed)
		{
			endGame(Globals.SUBMODE_ENDING_FAILED);
		}
		else if(bSucceeded)
		{
			endGame(Globals.SUBMODE_ENDING_COMPLETE_VICTORY);
		}
		else
		{
			return; // should never be reached, except in games with no Quests
		}
	}

//  Game Mode Methods --------------------------------------------------------/

	public void getPrevGameMode()
	{
		if(vcGameMode.size() > 0)
		{
			vcGameMode.removeElementAt(vcGameMode.size() - 1);
			vcGameSubMode.removeElementAt(vcGameSubMode.size() - 1);
		}
		if(vcGameMode.size() > 0)
		{
			currGameMode = ((Integer)(vcGameMode.lastElement())).intValue();
			currGameSubMode = ((Integer)(vcGameSubMode.lastElement())).intValue();
		}
		else
		{
			setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_MAIN);
		}
		if(DEBUG)
		{
			System.out.println("(GET) Mode Queue contains " + vcGameMode.size() + " entries");
			for(int i = vcGameMode.size() - 1; i >= 0; i--)
			{
				System.out.println("  " + vcGameMode.elementAt(i) + " : " + vcGameSubMode.elementAt(i));
			}
		}
	}

	public void setCurrGameMode(int currMode, int currSubMode)
	{
		vcGameMode.add(new Integer(currMode));
		vcGameSubMode.add(new Integer(currSubMode));
		currGameMode = currMode;
		currGameSubMode = currSubMode;
		if(DEBUG)
		{
			System.out.println("(SET) Mode Queue contains " + vcGameMode.size() + " entries");
			for(int i = vcGameMode.size() - 1; i >= 0; i--)
			{
				System.out.println("  " + vcGameMode.elementAt(i) + " : " + vcGameSubMode.elementAt(i));
			}
		}
	}

	public void replaceCurrGameMode(int currMode, int currSubMode)
	{
		vcGameMode.removeElementAt(vcGameMode.size() - 1);
		vcGameSubMode.removeElementAt(vcGameSubMode.size() - 1);
		vcGameMode.add(new Integer(currMode));
		vcGameSubMode.add(new Integer(currSubMode));
		currGameMode = currMode;
		currGameSubMode = currSubMode;
		if(DEBUG)
		{
			System.out.println("(REPLACE) Mode Queue contains " + vcGameMode.size() + " entries");
			for(int i = vcGameMode.size() - 1; i >= 0; i--)
			{
				System.out.println("  " + vcGameMode.elementAt(i) + " : " + vcGameSubMode.elementAt(i));
			}
		}
	}

	public void flushGameModeBuffer()
	{
		vcGameMode.removeAllElements();
		vcGameSubMode.removeAllElements();
		if(DEBUG)
		{
			System.out.println("(FLUSH) Mode Queue contains " + vcGameMode.size() + " entries");
			for(int i = vcGameMode.size() - 1; i >= 0; i--)
			{
				System.out.println("  " + vcGameMode.elementAt(i) + " : " + vcGameSubMode.elementAt(i));
			}
		}
	}

	// clear all temporary vars that expire when any other action is taken
	public void resetTurnVars()
	{
		espMode = false;
	}

//  Visualisation Methods ----------------------------------------------------/

	private void prepScreenForDrawing()
	{
		// prepare buffer strategy for drawing
		Graphics g2 = bufferStrategy.getDrawGraphics();
		g2.setColor(Globals.COLOR_MAIN_BACKGROUND);
		g2.fillRect(0, 0, Globals.SCRWIDTH, Globals.SCRHEIGHT);
		if(Globals.CURR_DUNGEON.getImageInterface() != null)
		{
			g2.drawImage(Globals.CURR_DUNGEON.getImageInterface(), 0, 0, this);
		}
		g2.dispose();
		if(bufferEnviron == null)
		{
			bufferEnviron = new BufferedImage(Globals.CANVAS_HALL_X * Globals.TILESCALE, Globals.CANVAS_HALL_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		}
		// prepare graphical canvas for drawing
		Graphics g = bufferEnviron.getGraphics();
		g.setColor(clrMenuBkgr);
		g.fillRect(0, 0, bufferEnviron.getWidth(this), bufferEnviron.getHeight(this));
		g.dispose();
	}

	public void drawScreen()
	{
		try
		{
			prepScreenForDrawing();

			// draw interface layer
			if(party != null && gameInProgress)
			{
				drawCharInterface();
			}

			// see if we are in the special "select a party member" submode, and if so, draw the visuals for that
			if(currGameMode == Globals.MODE_SELECT_PLAYER && currSelNotice != null)
			{
				String[] selMsgs = new String[party.getSize() + 1];
				selMsgs[0] = currSelNotice;
				for(int i = 0; i < party.getSize(); i++)
				{
					if(party.getPlayer(i) != null)
					{
						selMsgs[i + 1] = (i + 1) + ") " + party.getPlayer(i).getName();
					}
				}
				setCurrentMessage(selMsgs);
			}

			showMessageWindow = true;

			// process main game mode visuals
			if(currGameMode == Globals.MODE_EXPLORE || currGameMode == Globals.MODE_SHOW_NOTICE)
			{
				int planType = currBoard.getGridValue(party.getLocationX(), party.getLocationY());
				if(planType > Globals.MAP_SOLID && planType < Globals.MAP_ROOM)
				{
					currRoom = (Room)null;
					drawHallPlan();
				}
				else if(isLocationRoom(currBoard, party.getLocation()))
				{
					// obtain current Room object
					currRoom = currBoard.getRoom(party.getLocationX(), party.getLocationY());
					drawRoomLayers(currRoom);
					if(currRoom.hasActiveMonsters())
					{
						beginCombatSequence();
						updateScreen();
						return;
					}
				}
				if(currGameMode == Globals.MODE_SHOW_NOTICE)
				{
					drawNotificationBox(Globals.CURR_DUNGEON.getNotificationCol(), Globals.CURR_DUNGEON.getNotificationRow(), Globals.CURR_DUNGEON.getNotificationWid(), Globals.CURR_DUNGEON.getNotificationHgt());
					setCurrentMessage(Globals.getDungeonText("notificationClose"));
				}
			}
			else if(currGameMode == Globals.MODE_COMBAT)
			{
				drawRoomLayers(currRoom);
			}
			else if(currGameMode == Globals.MODE_TARGET)
			{
				drawRoomLayers(currRoom);
			}
			else if(currGameMode == Globals.MODE_MENU)
			{
				showMenuScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_FILE_SAVE)
			{
				showSaveScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_CREATE_GAME)
			{
				showCreationScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_USE)
			{
				showItemUseScreen(currInfoPlayer, currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_ITEM)
			{
				showItemManagementScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_TRADE)
			{
				showItemTradeScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_SHOP)
			{
				showShoppingScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_ORDER)
			{
				showPartyFormationScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_VAULT)
			{
				showVaultScreen();
			}
			else if(currGameMode == Globals.MODE_ASK)
			{
				showAskScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_NEGOTIATE)
			{
				showNegotiationScreen(currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_VIEW_MAP)
			{
				showMapScreen();
			}
			else if(currGameMode == Globals.MODE_INFO_PLAYER)
			{
				showPlayerInfoScreen(currInfoPlayer, currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_INV_PLAYER)
			{
//				currItemIndex = 0;
				showPlayerInventoryScreen(currInfoPlayer, currGameSubMode);
			}
			else if(currGameMode == Globals.MODE_INFO_PARTY)
			{
				showPartyInfoScreen();
			}
			else if(currGameMode == Globals.MODE_INFO_MONSTER)
			{
				showMonsterInfoScreen();
			}
			else if(currGameMode == Globals.MODE_INFO_QUESTS)
			{
				showQuestInfoScreen();
			}
			else if(currGameMode == Globals.MODE_SELECT_PLAYER)
			{
				if(currRoom != null)
				{
					drawRoomLayers(currRoom);
				}
				else
				{
					drawHallPlan();
				}
			}
			else if(currGameMode == Globals.MODE_ENDING)
			{
				showEndingScreen(currGameSubMode);
				//showMessageWindow = false; // enable when using picture game overs? or do they fit in the main area instead?
			}
			else if(currGameMode == Globals.MODE_INTRO)
			{
				showIntroduction(currGameSubMode);
				showMessageWindow = false;
				return;
			}
			if(showMessageWindow) { drawMessageWindow(); }

			lastValidKeyTime = System.currentTimeMillis();
		}
		catch(IllegalStateException ise) { System.out.println("IllegalStateException : " + ise.getMessage()); }
		catch(Exception e) { System.out.println("Exception : " + e.getMessage()); }
	}

	public void updateScreen()
	{
		if(introPlaying) { return; }
		drawScreen();
		bufferStrategy.show();
	}

	public void drawRoomLayers(Room room)
	{
		Graphics g = bufferStrategy.getDrawGraphics();

		// obtain current Floorplan object
		currFloorplan = (Floorplan)(Globals.PLANBOOK.elementAt(room.getPlanKey()));

		if(resetPositions)
		{
			// rebuild static room image layer
			drawRoomPlan();
			// initialize positions of all player, entities, and objects in room
			initializePlayerPositions();
			if(room.hasActiveMonsters())
			{
				initializeMonsterPositions(room);
			}
			if(room.hasChest() || room.hasVault())
			{
				cpItemConPos.setLocation(getClearTile());
			}
			if(currRoom.getItemCount() > 0)
			{
				cpItemPos.setLocation(getClearTile());
			}
			resetPositions = false;
		}

		// initialise draw buffer
		if(bufferRoomComp == null)
		{
			bufferRoomComp = new BufferedImage(currFloorplan.getWidth() * Globals.TILESCALE, currFloorplan.getHeight() * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g2 = bufferRoomComp.getGraphics();

		// build composite layer, starting with static room floor image
		drawRoomBase(g2);

		// add item layer
		drawRoomLayerItems(g2, room);

		// add monster layer
		drawRoomLayerMonsters(g2, room);

		// add party layer
		drawRoomLayerParty(g2);

		if(showHighlighter)
		{
			if((currGameMode == Globals.MODE_COMBAT || currGameMode == Globals.MODE_TARGET) && currPlayerPtr > -1)
			{
				g2.drawImage(Globals.CURR_DUNGEON.getImageHighlighter(), party.getPlayer(currPlayerPtr).getLocation().getX() * Globals.TILESCALE, party.getPlayer(currPlayerPtr).getLocation().getY() * Globals.TILESCALE, this);
			}
		}

		// add targetting reticule if in target mode
		if(currGameMode == Globals.MODE_TARGET)
		{
			if(currEnemyPtr > -1)
			{
				Monster targetMonster = currRoom.getMonster(currEnemyPtr);
				if(targetMonster != null && targetMonster.isAlive())
				{
					g2.drawImage(Globals.CURR_DUNGEON.getImageReticule(), targetMonster.getLocation().getX() * Globals.TILESCALE, targetMonster.getLocation().getY() * Globals.TILESCALE, this);
				}
			}
		}

		// draw composite room image onto buffer
		if(Globals.CURR_DUNGEON.getImageBackExplore() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackExplore(), Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		}
		drawpointX = (((Globals.CANVAS_HALL_X - currFloorplan.getWidth()) * Globals.TILESCALE) / 2) + Globals.CANVAS_HALL_OFFSET_X;
		drawpointY = (((Globals.CANVAS_HALL_Y - currFloorplan.getHeight()) * Globals.TILESCALE) / 2) + Globals.CANVAS_HALL_OFFSET_Y;
		g.drawImage(
			bufferRoomComp,
			drawpointX,
			drawpointY,
			drawpointX + (currFloorplan.getWidth() * Globals.TILESCALE),
			drawpointY + (currFloorplan.getHeight() * Globals.TILESCALE),
			0,
			0,
			currFloorplan.getWidth() * Globals.TILESCALE,
			currFloorplan.getHeight() * Globals.TILESCALE,
			this);

		g.dispose();
		g2.dispose();
	}

	public void drawHallPlan()
	{
		Graphics g2 = bufferStrategy.getDrawGraphics();

		if(bufferEnviron == null)
		{
			bufferEnviron = new BufferedImage(Globals.CANVAS_HALL_X * Globals.TILESCALE, Globals.CANVAS_HALL_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g = bufferEnviron.getGraphics();

		g.setColor(Globals.CURR_DUNGEON.getCurrColorHallBackground());
		g.fillRect(0, 0, bufferEnviron.getWidth(this), bufferEnviron.getHeight(this));

		if(currBoard.hasOpenCeiling())
		{
			g.drawImage(currBoard.getLevelGraphic(Globals.GRFX_CEILING), 0, 0, this);
		}
		if(currBoard.hasOpenFloor())
		{
			g.drawImage(currBoard.getLevelGraphic(Globals.GRFX_FLOOR), 0, bufferEnviron.getHeight(this) / 2, this);
		}

		actualX = -1;
		actualY = -1;

		/*
			Cardinal view calculation formulae
			NORTH		EAST		SOUTH		WEST
			y-n -> y	x+n -> x	y+n -> y	x-n -> x
			x-r -> x+r	y-r -> y+r	x+r -> x-r	y+r -> y-r
		*/

		newDist = 0.0;
		oldDist = 0.0;
		double olderDist = 0.0;
		for(int distanceVar = Globals.VISION_DEPTH; distanceVar >= -1; distanceVar--)
		{
			newDist = (oldDist * scalingIncrement) + 1.0;
			// draw in from left
			for(int sweepVar = -(rowLength + 1); sweepVar < 0; sweepVar++)
			{
				if(party.getFacing() == Globals.NORTH)
				{
					actualX = party.getLocationX() + sweepVar;
					actualY = party.getLocationY() - distanceVar;
				}
				else if(party.getFacing() == Globals.SOUTH)
				{
					actualX = party.getLocationX() - sweepVar;
					actualY = party.getLocationY() + distanceVar;
				}
				else if(party.getFacing() == Globals.WEST)
				{
					actualX = party.getLocationX() - distanceVar;
					actualY = party.getLocationY() - sweepVar;
				}
				else if(party.getFacing() == Globals.EAST)
				{
					actualX = party.getLocationX() + distanceVar;
					actualY = party.getLocationY() + sweepVar;
				}
				else
				{
					actualX = -1;
					actualY = -1;
				}
				if(actualX >= 0 && actualX < currBoard.getWidth() && actualY >= 0 && actualY < currBoard.getHeight())
				{
					drawFloorUnit(g, sweepVar, distanceVar, oldDist, newDist, currBoard.getGridValue(actualX, actualY));
				}
			}
			// draw in from right
			for(int sweepVar = (rowLength + 1); sweepVar > 0; sweepVar--)
			{
				if(party.getFacing() == Globals.NORTH)
				{
					actualX = party.getLocationX() + sweepVar;
					actualY = party.getLocationY() - distanceVar;
				}
				else if(party.getFacing() == Globals.SOUTH)
				{
					actualX = party.getLocationX() - sweepVar;
					actualY = party.getLocationY() + distanceVar;
				}
				else if(party.getFacing() == Globals.WEST)
				{
					actualX = party.getLocationX() - distanceVar;
					actualY = party.getLocationY() - sweepVar;
				}
				else if(party.getFacing() == Globals.EAST)
				{
					actualX = party.getLocationX() + distanceVar;
					actualY = party.getLocationY() + sweepVar;
				}
				else
				{
					actualX = -1;
					actualY = -1;
				}
				if(actualX >= 0 && actualX < currBoard.getWidth() && actualY >= 0 && actualY < currBoard.getHeight())
				{
					drawFloorUnit(g, sweepVar, distanceVar, oldDist, newDist, currBoard.getGridValue(actualX, actualY));
				}
			}
			// draw center
			if(party.getFacing() == Globals.NORTH)
			{
				actualX = party.getLocationX();
				actualY = party.getLocationY() - (distanceVar + 1);
			}
			else if(party.getFacing() == Globals.SOUTH)
			{
				actualX = party.getLocationX();
				actualY = party.getLocationY() + (distanceVar + 1);
			}
			else if(party.getFacing() == Globals.WEST)
			{
				actualX = party.getLocationX() - (distanceVar + 1);
				actualY = party.getLocationY();
			}
			else if(party.getFacing() == Globals.EAST)
			{
				actualX = party.getLocationX() + (distanceVar + 1);
				actualY = party.getLocationY();
			}
			else
			{
				actualX = -1;
				actualY = -1;
			}
			if(actualX >= 0 && actualX < currBoard.getWidth() && actualY >= 0 && actualY < currBoard.getHeight())
			{
				drawFloorUnit(g, 0, distanceVar + 1, olderDist, oldDist, currBoard.getGridValue(actualX, actualY));
			}

			olderDist = oldDist;
			oldDist = newDist;
		}

		if(espMode)
		{
			drawEspView((Graphics2D)g);
		}

		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g2.setColor(Color.black);
		if(Globals.CURR_DUNGEON.getImageCardinals() != null)
		{
			g2.drawImage(Globals.CURR_DUNGEON.getImageCardinals(), Globals.CURR_DUNGEON.getOffsetCardinalX(), Globals.CURR_DUNGEON.getOffsetCardinalY(), Globals.CURR_DUNGEON.getOffsetCardinalX() + Globals.CURR_DUNGEON.getSizeCardinalX(), Globals.CURR_DUNGEON.getOffsetCardinalY() + Globals.CURR_DUNGEON.getSizeCardinalY(), (party.getFacing() * Globals.CURR_DUNGEON.getSizeCardinalX()), 0, (party.getFacing() * Globals.CURR_DUNGEON.getSizeCardinalX()) + Globals.CURR_DUNGEON.getSizeCardinalX(), Globals.CURR_DUNGEON.getSizeCardinalY(), this);
		}
		else
		{
			g2.drawString((String)(Globals.CARDINALS_HALL.elementAt(party.getFacing())), Globals.CURR_DUNGEON.getOffsetCardinalX(), Globals.CURR_DUNGEON.getOffsetCardinalY());
		}

		simpleMapVisible(false);
		g.dispose();
		g2.dispose();
	}

	private void drawFloorUnit(Graphics g, int x, int y, double dist0, double dist1, int val)
	{
		if(val >= Globals.MAP_ROOM) { val = Globals.MAP_ROOM; }
		if(currBoard.hasLevelGraphics())
		{
			drawFloorUnitImage(g, x, y, dist0, dist1, val);
		}
		else
		{
			drawFloorUnitShaded(g, x, y, dist0, dist1, val);
		}
	}

	private void drawFloorUnitShaded(Graphics g, int x, int y, double dist0, double dist1, int val)
	{
		// calculate cubic corners with perspective applied
		cpFrontUL = transformPoint((x * scale3) - scale2, -scale2, dist1 * scale1);
		cpFrontUR = transformPoint((x * scale3) + scale2, -scale2, dist1 * scale1);
		cpFrontLL = transformPoint((x * scale3) - scale2,  scale2, dist1 * scale1);
		cpFrontLR = transformPoint((x * scale3) + scale2,  scale2, dist1 * scale1);
		cpBackUL  = transformPoint((x * scale3) - scale2, -scale2, dist0 * scale1);
		cpBackUR  = transformPoint((x * scale3) + scale2, -scale2, dist0 * scale1);
		cpBackLL  = transformPoint((x * scale3) - scale2,  scale2, dist0 * scale1);
		cpBackLR  = transformPoint((x * scale3) + scale2,  scale2, dist0 * scale1);

		// calculate light fall-off for this distance
		// (the "bonus" for room tiles is so that they don't shade differently than hall walls)
		shadeEffect = (int)(((Math.abs(x) + Math.abs(y) - (val == Globals.MAP_ROOM ? 1.0 : 0.0)) / 2.0) * Globals.CURR_DUNGEON.getShadeStrength());

		// floor and ceiling mirror each other, so only assign the X values once for both
		polyX[0] = cpBackLL.getX();
		polyX[1] = cpBackLR.getX();
		polyX[2] = cpFrontLR.getX();
		polyX[3] = cpFrontLL.getX();

		// draw floor
		polyY[0] = cpBackLL.getY();
		polyY[1] = cpBackLR.getY();
		polyY[2] = cpFrontLR.getY();
		polyY[3] = cpFrontLR.getY();
		g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallFloor().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallFloor().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallFloor().getBlue() - shadeEffect, 0)));
		g.fillPolygon(polyX, polyY, 4);
		if(Globals.CURR_DUNGEON.getOutlinesShown())
		{
			g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
			g.drawPolygon(polyX, polyY, 4);
		}

		// draw ceiling
		polyY[0] = cpBackUL.getY();
		polyY[1] = cpBackUR.getY();
		polyY[2] = cpFrontUR.getY();
		polyY[3] = cpFrontUR.getY();
		g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallCeiling().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallCeiling().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallCeiling().getBlue() - shadeEffect, 0)));
		g.fillPolygon(polyX, polyY, 4);
		if(Globals.CURR_DUNGEON.getOutlinesShown())
		{
			g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
			g.drawPolygon(polyX, polyY, 4);
		}

		boolean hasBackWall  = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (val & Globals.FACINGS[party.getFacing()]) == 0);
		boolean hasLeftWall  = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (val & Globals.FACINGS[(party.getFacing() - 1 < Globals.CARDINAL_MIN ? Globals.CARDINAL_MAX : party.getFacing() - 1)]) == 0);
		boolean hasRightWall = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (val & Globals.FACINGS[(party.getFacing() + 1 > Globals.CARDINAL_MAX ? Globals.CARDINAL_MIN : party.getFacing() + 1)]) == 0);
//		boolean hasFrontWall = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (!(x == 0 && y == 0) && (val & INVERSE_FACINGS[party.getFacing()]) == 0));
		boolean hasFrontWall = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM); // doesn't need a wall check like above, as long as floorplan is correct in every detail
		boolean hasFountain  = (val & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN;

		if(hasBackWall)
		{
			// draw back wall
			polyX[0] = cpBackLL.getX();
			polyX[1] = cpBackLR.getX();
			polyX[2] = cpBackUR.getX();
			polyX[3] = cpBackUL.getX();
			polyY[0] = cpBackLL.getY();
			polyY[1] = cpBackLR.getY();
			polyY[2] = cpBackUR.getY();
			polyY[3] = cpBackUL.getY();
			g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getBlue() - shadeEffect, 0)));
			g.fillPolygon(polyX, polyY, 4);
			if(Globals.CURR_DUNGEON.getOutlinesShown())
			{
				g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
				g.drawPolygon(polyX, polyY, 4);
			}
			if(val == Globals.MAP_ROOM)
			{
				// draw back door
				polyX[0] = (int)(cpBackLL.getX() + ((cpBackLR.getX() - cpBackLL.getX()) * 0.25));
				polyX[1] = (int)(cpBackLR.getX() - ((cpBackLR.getX() - cpBackLL.getX()) * 0.25));
				polyX[2] = (int)(cpBackUR.getX() - ((cpBackLR.getX() - cpBackLL.getX()) * 0.25));
				polyX[3] = (int)(cpBackUL.getX() + ((cpBackLR.getX() - cpBackLL.getX()) * 0.25));
				polyY[0] = (int)(cpBackLL.getY());
				polyY[1] = (int)(cpBackLR.getY());
				polyY[2] = (int)(cpBackUR.getY() - ((cpBackUR.getY() - cpBackLR.getY()) * 0.25));
				polyY[3] = (int)(cpBackUL.getY() - ((cpBackUL.getY() - cpBackLL.getY()) * 0.25));
				g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getBlue() - shadeEffect, 0)));
				g.fillPolygon(polyX, polyY, 4);
				if(Globals.CURR_DUNGEON.getOutlinesShown())
				{
					g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
					g.drawPolygon(polyX, polyY, 4);
				}
			}
		}
		if(x < 0)
		{
			if(hasLeftWall)
			{
				// draw left wall
				polyX[0] = cpFrontLL.getX();
				polyX[1] = cpBackLL.getX();
				polyX[2] = cpBackUL.getX();
				polyX[3] = cpFrontUL.getX();
				polyY[0] = cpFrontLL.getY();
				polyY[1] = cpBackLL.getY();
				polyY[2] = cpBackUL.getY();
				polyY[3] = cpFrontUL.getY();
				g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getBlue() - shadeEffect, 0)));
				g.fillPolygon(polyX, polyY, 4);
				if(Globals.CURR_DUNGEON.getOutlinesShown())
				{
					g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
					g.drawPolygon(polyX, polyY, 4);
				}
				if(val == Globals.MAP_ROOM)
				{
					// draw side door
					polyX[0] = (int)(cpFrontLL.getX() - ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyX[1] = (int)(cpBackLL.getX()  + ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyX[2] = (int)(cpBackUL.getX()  + ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyX[3] = (int)(cpFrontUL.getX() - ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyY[0] = (int)(cpFrontLL.getY() + ((cpFrontUL.getY() - cpBackUL.getY()) * 0.33));
					polyY[1] = (int)(cpBackLL.getY()  - ((cpFrontUL.getY() - cpBackUL.getY()) * 0.33));
					polyY[2] = (int)(cpBackUL.getY()  - ((cpFrontUL.getY() - cpBackUL.getY()) * 0.66));
					polyY[3] = (int)(cpFrontUL.getY() - ((cpFrontUL.getY() - cpBackUL.getY()) * 1.33));
					g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getBlue() - shadeEffect, 0)));
					g.fillPolygon(polyX, polyY, 4);
					if(Globals.CURR_DUNGEON.getOutlinesShown())
					{
						g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
						g.drawPolygon(polyX, polyY, 4);
					}
				}
			}
			if(hasRightWall)
			{
				// draw right wall
				polyX[0] = cpFrontLR.getX();
				polyX[1] = cpBackLR.getX();
				polyX[2] = cpBackUR.getX();
				polyX[3] = cpFrontUR.getX();
				polyY[0] = cpFrontLR.getY();
				polyY[1] = cpBackLR.getY();
				polyY[2] = cpBackUR.getY();
				polyY[3] = cpFrontUR.getY();
				g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getBlue() - shadeEffect, 0)));
				g.fillPolygon(polyX, polyY, 4);
				if(Globals.CURR_DUNGEON.getOutlinesShown())
				{
					g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
					g.drawPolygon(polyX, polyY, 4);
				}
				if(val == Globals.MAP_ROOM)
				{
					// draw side door
					polyX[0] = (int)(cpFrontLR.getX() - ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyX[1] = (int)(cpBackLR.getX()  + ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyX[2] = (int)(cpBackUR.getX()  + ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyX[3] = (int)(cpFrontUR.getX() - ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyY[0] = (int)(cpFrontLR.getY() + ((cpFrontUR.getY() - cpBackUR.getY()) * 0.33));
					polyY[1] = (int)(cpBackLR.getY()  - ((cpFrontUR.getY() - cpBackUR.getY()) * 0.33));
					polyY[2] = (int)(cpBackUR.getY()  - ((cpFrontUR.getY() - cpBackUR.getY()) * 0.66));
					polyY[3] = (int)(cpFrontUR.getY() - ((cpFrontUR.getY() - cpBackUR.getY()) * 1.33));
					g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getBlue() - shadeEffect, 0)));
					g.fillPolygon(polyX, polyY, 4);
					if(Globals.CURR_DUNGEON.getOutlinesShown())
					{
						g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
						g.drawPolygon(polyX, polyY, 4);
					}
				}
			}
		}
		else
		{
			if(hasRightWall)
			{
				// draw right wall
				polyX[0] = cpFrontLR.getX();
				polyX[1] = cpBackLR.getX();
				polyX[2] = cpBackUR.getX();
				polyX[3] = cpFrontUR.getX();
				polyY[0] = cpFrontLR.getY();
				polyY[1] = cpBackLR.getY();
				polyY[2] = cpBackUR.getY();
				polyY[3] = cpFrontUR.getY();
				g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getBlue() - shadeEffect, 0)));
				g.fillPolygon(polyX, polyY, 4);
				if(Globals.CURR_DUNGEON.getOutlinesShown())
				{
					g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
					g.drawPolygon(polyX, polyY, 4);
				}
				if(val == Globals.MAP_ROOM)
				{
					// draw side door
					polyX[0] = (int)(cpFrontLR.getX() - ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyX[1] = (int)(cpBackLR.getX()  + ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyX[2] = (int)(cpBackUR.getX()  + ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyX[3] = (int)(cpFrontUR.getX() - ((cpFrontLR.getX() - cpBackLR.getX()) * 0.33));
					polyY[0] = (int)(cpFrontLR.getY() + ((cpFrontUR.getY() - cpBackUR.getY()) * 0.33));
					polyY[1] = (int)(cpBackLR.getY()  - ((cpFrontUR.getY() - cpBackUR.getY()) * 0.33));
					polyY[2] = (int)(cpBackUR.getY()  - ((cpFrontUR.getY() - cpBackUR.getY()) * 0.66));
					polyY[3] = (int)(cpFrontUR.getY() - ((cpFrontUR.getY() - cpBackUR.getY()) * 1.33));
					g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getBlue() - shadeEffect, 0)));
					g.fillPolygon(polyX, polyY, 4);
					if(Globals.CURR_DUNGEON.getOutlinesShown())
					{
						g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
						g.drawPolygon(polyX, polyY, 4);
					}
				}
			}
			if(hasLeftWall)
			{
				// draw left wall
				polyX[0] = cpFrontLL.getX();
				polyX[1] = cpBackLL.getX();
				polyX[2] = cpBackUL.getX();
				polyX[3] = cpFrontUL.getX();
				polyY[0] = cpFrontLL.getY();
				polyY[1] = cpBackLL.getY();
				polyY[2] = cpBackUL.getY();
				polyY[3] = cpFrontUL.getY();
				g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getBlue() - shadeEffect, 0)));
				g.fillPolygon(polyX, polyY, 4);
				if(Globals.CURR_DUNGEON.getOutlinesShown())
				{
					g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
					g.drawPolygon(polyX, polyY, 4);
				}
				if(val == Globals.MAP_ROOM)
				{
					// draw side door
					polyX[0] = (int)(cpFrontLL.getX() - ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyX[1] = (int)(cpBackLL.getX()  + ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyX[2] = (int)(cpBackUL.getX()  + ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyX[3] = (int)(cpFrontUL.getX() - ((cpFrontLL.getX() - cpBackLL.getX()) * 0.33));
					polyY[0] = (int)(cpFrontLL.getY() + ((cpFrontUL.getY() - cpBackUL.getY()) * 0.33));
					polyY[1] = (int)(cpBackLL.getY()  - ((cpFrontUL.getY() - cpBackUL.getY()) * 0.33));
					polyY[2] = (int)(cpBackUL.getY()  - ((cpFrontUL.getY() - cpBackUL.getY()) * 0.66));
					polyY[3] = (int)(cpFrontUL.getY() - ((cpFrontUL.getY() - cpBackUL.getY()) * 1.33));
					g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getBlue() - shadeEffect, 0)));
					g.fillPolygon(polyX, polyY, 4);
					if(Globals.CURR_DUNGEON.getOutlinesShown())
					{
						g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
						g.drawPolygon(polyX, polyY, 4);
					}
				}
			}
		}
		if(hasFrontWall)
		{
			// draw front wall
			polyX[0] = cpFrontLL.getX();
			polyX[1] = cpFrontLR.getX();
			polyX[2] = cpFrontUR.getX();
			polyX[3] = cpFrontUL.getX();
			polyY[0] = cpFrontLL.getY();
			polyY[1] = cpFrontLR.getY();
			polyY[2] = cpFrontUR.getY();
			polyY[3] = cpFrontUL.getY();
			g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallWall().getBlue() - shadeEffect, 0)));
			g.fillPolygon(polyX, polyY, 4);
			if(Globals.CURR_DUNGEON.getOutlinesShown())
			{
				g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
				g.drawPolygon(polyX, polyY, 4);
			}
			if(val == Globals.MAP_ROOM)
			{
				// draw front door
				polyX[0] = (int)(cpFrontLL.getX() + ((cpFrontLR.getX() - cpFrontLL.getX()) * 0.25));
				polyX[1] = (int)(cpFrontLR.getX() - ((cpFrontLR.getX() - cpFrontLL.getX()) * 0.25));
				polyX[2] = (int)(cpFrontUR.getX() - ((cpFrontLR.getX() - cpFrontLL.getX()) * 0.25));
				polyX[3] = (int)(cpFrontUL.getX() + ((cpFrontLR.getX() - cpFrontLL.getX()) * 0.25));
				polyY[0] = (int)(cpFrontLL.getY());
				polyY[1] = (int)(cpFrontLR.getY());
				polyY[2] = (int)(cpFrontUR.getY() - ((cpFrontUR.getY() - cpFrontLR.getY()) * 0.25));
				polyY[3] = (int)(cpFrontUL.getY() - ((cpFrontUL.getY() - cpFrontLL.getY()) * 0.25));
				g.setColor(new Color(Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getRed() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getGreen() - shadeEffect, 0), Math.max(Globals.CURR_DUNGEON.getCurrColorHallDoor().getBlue() - shadeEffect, 0)));
				g.fillPolygon(polyX, polyY, 4);
				if(Globals.CURR_DUNGEON.getOutlinesShown())
				{
					g.setColor(Globals.CURR_DUNGEON.getCurrColorHallOutline());
					g.drawPolygon(polyX, polyY, 4);
				}
			}
		}

		if(hasFountain)
		{
			drawFlatScaledImage(g, Globals.CURR_DUNGEON.getImageHallFountain(), (cpBackUL.getX() + cpFrontUL.getX()) / 2, (cpBackUL.getY() + cpFrontUL.getY()) / 2, (cpBackLR.getX() + cpFrontLR.getX()) / 2, (cpBackLR.getY() + cpFrontLR.getY()) / 2);
		}
	}

	private void drawFloorUnitImage(Graphics g, int x, int y, double dist0, double dist1, int val)
	{
		// calculate cubic corners with perspective applied
		cpFrontUL = transformPoint((x * scale3) - scale2, -scale2, dist1 * scale1);
		cpFrontUR = transformPoint((x * scale3) + scale2, -scale2, dist1 * scale1);
		cpFrontLL = transformPoint((x * scale3) - scale2,  scale2, dist1 * scale1);
		cpFrontLR = transformPoint((x * scale3) + scale2,  scale2, dist1 * scale1);
		cpBackUL  = transformPoint((x * scale3) - scale2, -scale2, dist0 * scale1);
		cpBackUR  = transformPoint((x * scale3) + scale2, -scale2, dist0 * scale1);
		cpBackLL  = transformPoint((x * scale3) - scale2,  scale2, dist0 * scale1);
		cpBackLR  = transformPoint((x * scale3) + scale2,  scale2, dist0 * scale1);

		// calculate light fall-off for this distance
		// (the "bonus" for room tiles is so that they don't shade differently than hall walls)
		shadeEffect = (int)(((Math.abs(x) + Math.abs(y) - (val == Globals.MAP_ROOM ? 1.0 : 0.0)) / 2.0) * Globals.CURR_DUNGEON.getShadeStrength());

		// floor and ceiling mirror each other, so only assign the X values once for both
		polyX[0] = cpBackLL.getX();
		polyX[1] = cpBackLR.getX();
		polyX[2] = cpFrontLR.getX();
		polyX[3] = cpFrontLL.getX();

		// draw floor
		if(!currBoard.hasOpenFloor())
		{
			polyY[0] = cpBackLL.getY();
			polyY[1] = cpBackLR.getY();
			polyY[2] = cpFrontLR.getY();
			polyY[3] = cpFrontLL.getY();
			drawBottomScaledImage(g, currBoard.getLevelGraphic(Globals.GRFX_FLOOR), cpBackLL.getY(), cpFrontLL.getY() - cpBackLL.getY(), cpBackLL.getX(), cpBackLR.getX(), cpFrontLL.getX(), cpFrontLR.getX());
		}

		// draw ceiling
		if(!currBoard.hasOpenCeiling())
		{
			polyY[0] = cpBackUL.getY();
			polyY[1] = cpBackUR.getY();
			polyY[2] = cpFrontUR.getY();
			polyY[3] = cpFrontUL.getY();
			drawTopScaledImage(g, currBoard.getLevelGraphic(Globals.GRFX_CEILING), cpFrontUL.getY(), cpBackUL.getY() - cpFrontUL.getY(), cpFrontUL.getX(), cpFrontUR.getX(), cpBackUL.getX(), cpBackUR.getX());
		}

		boolean hasBackWall  = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (val & Globals.FACINGS[party.getFacing()]) == 0);
		boolean hasLeftWall  = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (val & Globals.FACINGS[(party.getFacing() - 1 < Globals.CARDINAL_MIN ? Globals.CARDINAL_MAX : party.getFacing() - 1)]) == 0);
		boolean hasRightWall = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (val & Globals.FACINGS[(party.getFacing() + 1 > Globals.CARDINAL_MAX ? Globals.CARDINAL_MIN : party.getFacing() + 1)]) == 0);
//		boolean hasFrontWall = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM || (!(x == 0 && y == 0) && (val & Globals.INVERSE_FACINGS[party.getFacing()]) == 0));
		boolean hasFrontWall = (val == Globals.MAP_SOLID || val == Globals.MAP_ROOM); // doesn't need a wall check like above, as long as floorplan is correct in every detail
		boolean hasFountain  = (val & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN;

		if(hasBackWall)
		{
			// draw back wall
/*
			polyX[0] = cpBackLL.getX();
			polyX[1] = cpBackLR.getX();
			polyX[2] = cpBackUR.getX();
			polyX[3] = cpBackUL.getX();
			polyY[0] = cpBackLL.getY();
			polyY[1] = cpBackLR.getY();
			polyY[2] = cpBackUR.getY();
			polyY[3] = cpBackUL.getY();
*/
			drawFlatScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), cpBackUL.getX(), cpBackUL.getY(), cpBackLR.getX(), cpBackLR.getY());
		}

		if(hasFountain)
		{
/*
			polyX[0] = (cpBackLL.getX() + cpFrontLL.getX()) / 2;
			polyX[1] = (cpBackLR.getX() + cpFrontLR.getX()) / 2;
			polyX[2] = (cpBackUR.getX() + cpFrontUR.getX()) / 2;
			polyX[3] = (cpBackUL.getX() + cpFrontUL.getX()) / 2;
			polyY[0] = (cpBackLL.getY() + cpFrontLL.getY()) / 2;
			polyY[1] = (cpBackLR.getY() + cpFrontLR.getY()) / 2;
			polyY[2] = (cpBackUR.getY() + cpFrontUR.getY()) / 2;
			polyY[3] = (cpBackUL.getY() + cpFrontUL.getY()) / 2;
*/
			drawFlatScaledImage(g, Globals.CURR_DUNGEON.getImageHallFountain(), (cpBackUL.getX() + cpFrontUL.getX()) / 2, (cpBackUL.getY() + cpFrontUL.getY()) / 2, (cpBackLR.getX() + cpFrontLR.getX()) / 2, (cpBackLR.getY() + cpFrontLR.getY()) / 2);
		}

		if(x < 0)
		{
			if(hasLeftWall)
			{
				// draw left wall
				polyX[0] = cpFrontLL.getX();
				polyX[1] = cpBackLL.getX();
				polyX[2] = cpBackUL.getX();
				polyX[3] = cpFrontUL.getX();
				polyY[0] = cpFrontLL.getY();
				polyY[1] = cpBackLL.getY();
				polyY[2] = cpBackUL.getY();
				polyY[3] = cpFrontUL.getY();
				drawPerspScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), polyX[2] - polyX[3], polyY[0] - polyY[3], polyY[2] - polyY[3], polyX[3], polyY[3]);
			}
			if(hasRightWall)
			{
				// draw right wall
				polyX[0] = cpFrontLR.getX();
				polyX[1] = cpBackLR.getX();
				polyX[2] = cpBackUR.getX();
				polyX[3] = cpFrontUR.getX();
				polyY[0] = cpFrontLR.getY();
				polyY[1] = cpBackLR.getY();
				polyY[2] = cpBackUR.getY();
				polyY[3] = cpFrontUR.getY();
				drawPerspScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), polyX[2] - polyX[3], polyY[0] - polyY[3], polyY[2] - polyY[3], polyX[3], polyY[3]);
			}
		}
		else if(x == 0)
		{
			if(hasLeftWall)
			{
				// draw left wall
				polyX[0] = cpFrontLL.getX();
				polyX[1] = cpBackLL.getX();
				polyX[2] = cpBackUL.getX();
				polyX[3] = cpFrontUL.getX();
				polyY[0] = cpFrontLL.getY();
				polyY[1] = cpBackLL.getY();
				polyY[2] = cpBackUL.getY();
				polyY[3] = cpFrontUL.getY();
				drawPerspScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), polyX[2] - polyX[3], polyY[0] - polyY[3], polyY[2] - polyY[3], polyX[3], polyY[3]);
			}
			if(hasRightWall)
			{
				// draw right wall
				polyX[0] = cpFrontLR.getX();
				polyX[1] = cpBackLR.getX();
				polyX[2] = cpBackUR.getX();
				polyX[3] = cpFrontUR.getX();
				polyY[0] = cpFrontLR.getY();
				polyY[1] = cpBackLR.getY();
				polyY[2] = cpBackUR.getY();
				polyY[3] = cpFrontUR.getY();
				drawPerspScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), polyX[3] - polyX[2], polyY[1] - polyY[2], polyY[3] - polyY[2], polyX[2], polyY[2]);
			}
		}
		else
		{
			if(hasRightWall)
			{
				// draw right wall
				polyX[0] = cpFrontLR.getX();
				polyX[1] = cpBackLR.getX();
				polyX[2] = cpBackUR.getX();
				polyX[3] = cpFrontUR.getX();
				polyY[0] = cpFrontLR.getY();
				polyY[1] = cpBackLR.getY();
				polyY[2] = cpBackUR.getY();
				polyY[3] = cpFrontUR.getY();
				drawPerspScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), polyX[3] - polyX[2], polyY[1] - polyY[2], polyY[3] - polyY[2], polyX[2], polyY[2]);
			}
			if(hasLeftWall)
			{
				// draw left wall
				polyX[0] = cpFrontLL.getX();
				polyX[1] = cpBackLL.getX();
				polyX[2] = cpBackUL.getX();
				polyX[3] = cpFrontUL.getX();
				polyY[0] = cpFrontLL.getY();
				polyY[1] = cpBackLL.getY();
				polyY[2] = cpBackUL.getY();
				polyY[3] = cpFrontUL.getY();
				drawPerspScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), polyX[3] - polyX[2], polyY[1] - polyY[2], polyY[3] - polyY[2], polyX[2], polyY[2]);
			}
		}
		if(hasFrontWall)
		{
			// draw front wall
			polyX[0] = cpFrontLL.getX();
			polyX[1] = cpFrontLR.getX();
			polyX[2] = cpFrontUR.getX();
			polyX[3] = cpFrontUL.getX();
			polyY[0] = cpFrontLL.getY();
			polyY[1] = cpFrontLR.getY();
			polyY[2] = cpFrontUR.getY();
			polyY[3] = cpFrontUL.getY();
			drawFlatScaledImage(g, (val == Globals.MAP_ROOM ? currBoard.getLevelGraphic(Globals.GRFX_DOOR) : currBoard.getLevelGraphic(Globals.GRFX_WALL)), cpFrontUL.getX(), cpFrontUL.getY(), cpFrontLR.getX(), cpFrontLR.getY());
		}
	}

	public void drawFlatScaledImage(Graphics g, Image imgBase, int cornerUX, int cornerUY, int cornerLX, int cornerLY)
	{
		g.drawImage(imgBase, cornerUX, cornerUY, cornerLX, cornerLY, 0, 0, imgBase.getWidth(this), imgBase.getHeight(this), this);
	}

	public void drawPerspScaledImage(Graphics g, Image imgBase, int newImageWidth, int newImageHeight, int depthDiff, int cornerX, int cornerY)
	{
		if(newImageWidth < 1 || newImageHeight < 1) { return; }
		double slopeIncr = (depthDiff * 1.0) / (newImageWidth * 1.0);
		double altitude = 0.0;
		double proportion = 256.0 / newImageWidth;
		for(int z = 0; z < newImageWidth; z++)
		{
			int altPixel = (int)(altitude);
			g.drawImage(imgBase, cornerX + z, altPixel + cornerY, (cornerX + z) + 1, (newImageHeight - altPixel) + cornerY, (int)(z * proportion), 0, (int)((z + 1) * proportion), 256, this);
			altitude += slopeIncr;
		}
	}

	public void drawTopScaledImage(Graphics g, Image imgBase, int imageStartY, int newImageHeight, int imageStartXTop, int imageEndXTop, int imageStartXBottom, int imageEndXBottom)
	{
		if(newImageHeight < 1) { return; }
		double proportion = 256.0 / newImageHeight;
		double xStartOffset = (1.0 * (imageStartXTop - imageStartXBottom)) / (newImageHeight * 1.0);
		double xEndOffset   = (1.0 * (imageEndXTop - imageEndXBottom)) / (newImageHeight * 1.0);
		double currXStart   = imageStartXTop * 1.0;
		double currXEnd     = imageEndXTop * 1.0;
		for(int z = 0; z < newImageHeight; z++)
		{
			g.drawImage(imgBase, (int)currXStart, imageStartY + z, (int)currXEnd, imageStartY + z + 1, 0, (int)(z * proportion), 256, (int)((z + 1) * proportion), this);
			currXStart -= xStartOffset;
			currXEnd   -= xEndOffset;
		}
	}

	public void drawBottomScaledImage(Graphics g, Image imgBase, int imageStartY, int newImageHeight, int imageStartXTop, int imageEndXTop, int imageStartXBottom, int imageEndXBottom)
	{
		if(newImageHeight < 1) { return; }
		double proportion = 256.0 / newImageHeight;
		double xStartOffset = (1.0 * (imageStartXTop - imageStartXBottom)) / (newImageHeight * 1.0);
		double xEndOffset   = (1.0 * (imageEndXTop - imageEndXBottom)) / (newImageHeight * 1.0);
		double currXStart   = imageStartXTop * 1.0;
		double currXEnd     = imageEndXTop * 1.0;
		for(int z = 0; z < newImageHeight; z++)
		{
			g.drawImage(imgBase, (int)currXStart, imageStartY + z, (int)currXEnd, imageStartY + z + 1, 0, (int)(z * proportion), 256, (int)((z + 1) * proportion), this);
			currXStart -= xStartOffset;
			currXEnd   -= xEndOffset;
		}
	}

	private void clearMessageWindow()
	{
		if(bufferMessage == null)
		{
			bufferMessage = new BufferedImage(Globals.CANVAS_MSG_X * Globals.TILESCALE, Globals.CANVAS_MSG_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g = bufferMessage.getGraphics();
		g.setColor(Globals.CURR_DUNGEON.getColorMessageWindow());
		g.fillRect(0, 0, bufferMessage.getWidth(), bufferMessage.getHeight());
		g.dispose();
	}

	private void drawMessageWindow()
	{
		Graphics g2 = bufferStrategy.getDrawGraphics();
		if(bufferMessage == null)
		{
			bufferMessage = new BufferedImage(Globals.CANVAS_MSG_X * Globals.TILESCALE, Globals.CANVAS_MSG_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		}
		clearMessageWindow();
		if(currMsgs.size() > 0)
		{
			for(int i = 0; i < currMsgs.size(); i++)
			{
				plotString(bufferMessage.getGraphics(), currMsgColor, currMsgs.elementAt(i), 1, i);
			}
		}
		g2.drawImage(bufferMessage, Globals.CANVAS_MSG_OFFSET_X, Globals.CANVAS_MSG_OFFSET_Y, this);
		g2.dispose();
	}

	private void drawNotificationBox(int x, int y, int w, int h)
	{
		Graphics g = bufferStrategy.getDrawGraphics();
		if(currNotificationImage != null)
		{
			plotTextBox(g, x, y, w, h + 3, Globals.CURR_DUNGEON.getColorNotificationOutline(), Globals.CURR_DUNGEON.getColorNotificationFill());
			g.drawImage(currNotificationImage, ((x * Globals.FONTWIDTH) - (Globals.FONTWIDTH / 2)) + Globals.FONTWIDTH, ((y * Globals.FONTHEIGHT) - (Globals.FONTHEIGHT / 2)) + (Globals.FONTHEIGHT / 2), this);
			plotLongString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), currNotification, x + 1, y + 3, (Color)null, w - 2);
		}
		else
		{
			plotTextBox(g, x, y, w, h, Globals.CURR_DUNGEON.getColorNotificationOutline(), Globals.CURR_DUNGEON.getColorNotificationFill());
			plotLongString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), currNotification, x + 1, y, (Color)null, w - 2);
		}
		g.dispose();
	}

	private void drawNotificationBox(int len)
	{
		drawNotificationBox((((Globals.SCRWIDTH / Globals.TILESCALE) * Globals.FONTRATIO) - len) / 2, (Globals.SCRHEIGHT / Globals.TILESCALE) / 2, len + 1, 1);
	}

	private void simpleMapVisible(boolean searchForHiddenDoors)
	{
		boolean foundHiddenDoors = false;
		if(searchForHiddenDoors) { setCurrentMessage(Globals.getDungeonText("foundNoHiddenDoor")); }
		// check current hall segment for hidden doors, if searching is requested
		if(searchForHiddenDoors)
		{
			foundHiddenDoors = searchHallForHiddenDoors(party.getLocation().getX(), party.getLocation().getY());
		}
		// only maps straight in front of player, but should be good enough (that's what original does, albeit with simpler dungeons)
		int x = party.getLocation().getX() + ((Globals.VISION_DEPTH + 1) * Globals.CPVECTS[party.getFacing()].getX());
		int y = party.getLocation().getY() + ((Globals.VISION_DEPTH + 1) * Globals.CPVECTS[party.getFacing()].getY());
		if(x < 0) { x = 0; }
		if(x >= currBoard.getWidth()) { x = currBoard.getWidth() - 1; }
		if(y < 0) { y = 0; }
		if(y >= currBoard.getHeight()) { y = currBoard.getHeight() - 1; }
		if(x == party.getLocation().getX())
		{
			int incr = (y > party.getLocation().getY() ? 1 : -1);
			for(int checkY = party.getLocation().getY() + incr; checkY != y;)
			{
				if(isLocationRoom(currBoard, x, checkY))
				{
					// rooms end sweep but get mapped, so "see" room and return
					currBoard.setConditionalSeen(x, checkY);
					return;
				}
				else if( currBoard.getGridValue(x, checkY) == Globals.MAP_SOLID ||
						(currBoard.getGridValue(x, checkY) & Globals.FACINGS[party.getFacing()]) == 0)
				{
					// solid grids and grids with a "back" (or far) wall end sweep
					if((currBoard.getGridValue(x, checkY) & Globals.INVERSE_FACINGS[party.getFacing()]) != 0)
					{
						// grid square has a "back" wall but not a "front", so "see" it before returing
						currBoard.setConditionalSeen(x, checkY);
						if(searchForHiddenDoors)
						{
							foundHiddenDoors = searchHallForHiddenDoors(x, checkY);
						}
					}
					return;
				}
				else if( checkY != party.getLocation().getY() &&
						(currBoard.getGridValue(x, checkY) & Globals.INVERSE_FACINGS[party.getFacing()]) == 0)
				{
					// grid squares (apart from the currently occupied one) end a sweep if they have a "front" (or near) wall
					return;
				}
				else
				{
					// view is clear to this grid square
					currBoard.setConditionalSeen(x, checkY);
					if(searchForHiddenDoors)
					{
						foundHiddenDoors = searchHallForHiddenDoors(x, checkY);
					}

				}
				if(incr == 0 || checkY < 0 || checkY >= currBoard.getHeight()) { return; } // failsafe to avoid infinite loops
				checkY += incr;
			}
		}
		else if(y == party.getLocation().getY())
		{
			int incr = (x > party.getLocation().getX() ? 1 : -1);
			for(int checkX = party.getLocation().getX() + incr; checkX != x;)
			{
				if(isLocationRoom(currBoard, checkX, y))
				{
					// rooms end sweep but get mapped, so "see" room and return
					currBoard.setConditionalSeen(checkX, y);
					return;
				}
				else if( currBoard.getGridValue(checkX, y) == Globals.MAP_SOLID ||
					(currBoard.getGridValue(checkX, y) & Globals.FACINGS[party.getFacing()]) == 0)
				{
					// solid grids and grids with a "back" (or far) wall end sweep
					if((currBoard.getGridValue(checkX, y) & Globals.INVERSE_FACINGS[party.getFacing()]) != 0)
					{
						// grid square has a "back" wall but not a "front", so "see" it before returing
						currBoard.setConditionalSeen(checkX, y);
						if(searchForHiddenDoors)
						{
							foundHiddenDoors = searchHallForHiddenDoors(checkX, y);
						}
					}
					return;
				}
				else if( checkX != party.getLocation().getX() &&
						(currBoard.getGridValue(checkX, y) & Globals.INVERSE_FACINGS[party.getFacing()]) == 0)
				{
					// grid squares (apart from the currently occupied one) end a sweep if they have a "front" (or near) wall
					return;
				}
				else
				{
					// view is clear to this grid square
					currBoard.setConditionalSeen(checkX, y);
					if(searchForHiddenDoors)
					{
						foundHiddenDoors = searchHallForHiddenDoors(checkX, y);
					}
				}
				if(incr == 0 || checkX < 0 || checkX >= currBoard.getWidth()) { return; } // failsafe to avoid infinite loops
				checkX += incr;
			}
		}
	}

	public void analyseFloorplan()
	{
		if(currRoom == null) { return; }

		currFloorplan = (Floorplan)(Globals.PLANBOOK.elementAt(currRoom.getPlanKey()));

		// process layout template based on current room settings (doorways, presence of features, etc)
		for(int y = 0; y < currFloorplan.getHeight(); y++)
		{
			for(int x = 0; x < currFloorplan.getWidth(); x++)
			{
				if(currFloorplan.getPlanChar(x, y) == '#')
				{
					// solid dungeon rock
					currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
				}
				else if(currFloorplan.getPlanChar(x, y) == '.' || currFloorplan.getPlanChar(x, y) == '@' || currFloorplan.getPlanChar(x, y) == 'F')
				{
					// open floor
					currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_ALL);
				}
				else if(currFloorplan.getPlanChar(x, y) == '=')
				{
					// horizontal wall
					currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
				}
				else if(currFloorplan.getPlanChar(x, y) == '|')
				{
					// vertical wall
					currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
				}
				else if(currFloorplan.getPlanChar(x, y) == '+')
				{
					// column
					currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
				}
				else if(currFloorplan.getPlanChar(x, y) == '-')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() - 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						// northern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no door, so draw horizontal wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// northern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '_')
				{
					if(party.getLocationY() < (currBoard.getHeight() - 1) && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() + 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						// southern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no door, so draw horizontal wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// southern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '`')
				{
					if(party.getLocationY() < (currBoard.getWidth() - 1) && isLocationRoom(currBoard, party.getLocationX() + 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						// eastern vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no door, so draw vertical wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// eastern vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '\'')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX() - 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no door, so draw vertical wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// western vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '^')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() - 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						// northern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no door, so draw horizontal wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// northern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '~')
				{
					if(party.getLocationY() < (currBoard.getHeight() - 1) && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() + 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						// southern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no door, so draw horizontal wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// southern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == ';')
				{
					if(party.getLocationX() < (currBoard.getWidth() - 1) && isLocationRoom(currBoard, party.getLocationX() + 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						// eastern vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no door, so draw vertical wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// eastern vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == ':')
				{
					if(party.getLocationX() > 0 && isLocationRoom(currBoard, party.getLocationX() - 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						// western vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no door, so draw vertical wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// western vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'N')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() - 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						// using room door, so draw solid frame
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no door, so draw horizontal wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// northern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'S')
				{
					if(party.getLocationY() < (currBoard.getHeight() - 1) && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() + 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						// using room door, so draw solid frame
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no door, so draw horizontal wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// southern horizontal door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'E')
				{
					if(party.getLocationX() < (currBoard.getWidth() - 1) && isLocationRoom(currBoard, party.getLocationX() + 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						// using room door, so draw solid frame
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no door, so draw vertical wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// eastern vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'W')
				{
					if(party.getLocationX() > 0 && isLocationRoom(currBoard, party.getLocationX() - 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						// using room door, so draw solid frame
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no door, so draw vertical wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// western vertical door
						currFloorplan.setLayoutType(x, y, Globals.ROOM_DOOR);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'n')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no northern door, so draw solid wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// northern door, so draw floor
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 's')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no southern door, so draw solid wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// southern door, so draw floor
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'e')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no eastern door, so draw solid wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// eastern door, so draw floor
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'w')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no western door, so draw solid wall
						currFloorplan.setLayoutType(x, y, Globals.ROOM_IMPASS);
					}
					else
					{
						// western door, so draw floor
						currFloorplan.setLayoutType(x, y, Globals.ROOM_OPEN_PLAYER);
					}
				}
			}
		}
		// plot any staircase in room
		if(currRoom.getStairType() != Globals.STAIRS_NONE)
		{
			for(int y = 0; y < currFloorplan.getHeight(); y++)
			{
				for(int x = 0; x < currFloorplan.getWidth(); x++)
				{
					if(currFloorplan.getPlanChar(x, y) == '@')
					{
						currFloorplan.setLayoutType(x,     y,     Globals.ROOM_IMPASS);
						currFloorplan.setLayoutType(x + 1, y,     Globals.ROOM_IMPASS);
						currFloorplan.setLayoutType(x,     y + 1, Globals.ROOM_IMPASS);
						currFloorplan.setLayoutType(x + 1, y + 1, Globals.ROOM_IMPASS);
						y = currFloorplan.getHeight();
						x = currFloorplan.getWidth();
					}
				}
			}
		}
		// plot any features in room
		if(currRoom.getFeatureType() != Globals.FEATURE_NONE)
		{
			for(int y = 0; y < currFloorplan.getHeight(); y++)
			{
				for(int x = 0; x < currFloorplan.getWidth(); x++)
				{
					if(currFloorplan.getPlanChar(x, y) == 'F')
					{
						currFloorplan.setLayoutType(x,     y,     Globals.ROOM_IMPASS);
						currFloorplan.setLayoutType(x + 1, y,     Globals.ROOM_IMPASS);
						currFloorplan.setLayoutType(x,     y + 1, Globals.ROOM_IMPASS);
						currFloorplan.setLayoutType(x + 1, y + 1, Globals.ROOM_IMPASS);
						y = currFloorplan.getHeight();
						x = currFloorplan.getWidth();
					}
				}
			}
		}
	}

	// floorplan drawing loop
	public void drawRoomPlan()
	{
		if(bufferRoomBase == null)
		{
			bufferRoomBase = new BufferedImage(currFloorplan.getWidth() * Globals.TILESCALE, currFloorplan.getHeight() * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		}

		Graphics g = bufferRoomBase.getGraphics();

		analyseFloorplan();

		g.setColor(Globals.CURR_DUNGEON.getColorRoomBackground());
		g.fillRect(0, 0, (currFloorplan.getWidth() * Globals.TILESCALE), (currFloorplan.getHeight() * Globals.TILESCALE));

		// process layout template based on current room settings (doorways, presence of features, etc)
		for(int y = 0; y < currFloorplan.getHeight(); y++)
		{
			for(int x = 0; x < currFloorplan.getWidth(); x++)
			{
				if(currFloorplan.getPlanChar(x, y) == '#')
				{
					// solid dungeon rock
					g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
				}
				else if(currFloorplan.getPlanChar(x, y) == '.' || currFloorplan.getPlanChar(x, y) == '@' || currFloorplan.getPlanChar(x, y) == 'F')
				{
					// open floor
					g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
				}
				else if(currFloorplan.getPlanChar(x, y) == '=')
				{
					// horizontal wall
					g.drawImage(Globals.CURR_DUNGEON.getImageWallH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
				}
				else if(currFloorplan.getPlanChar(x, y) == '|')
				{
					// vertical wall
					g.drawImage(Globals.CURR_DUNGEON.getImageWallV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
				}
				else if(currFloorplan.getPlanChar(x, y) == '+')
				{
					// column
					g.drawImage(Globals.CURR_DUNGEON.getImageColumn(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
				}
				else if(currFloorplan.getPlanChar(x, y) == '-')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() - 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						// northern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no door, so draw horizontal wall
						g.drawImage(Globals.CURR_DUNGEON.getImageWallH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// northern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '_')
				{
					if(party.getLocationY() < (currBoard.getHeight() - 1) && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() + 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						// southern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no door, so draw horizontal wall
						g.drawImage(Globals.CURR_DUNGEON.getImageWallH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// southern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '`')
				{
					if(party.getLocationY() < (currBoard.getWidth() - 1) && isLocationRoom(currBoard, party.getLocationX() + 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						// eastern vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no door, so draw vertical wall
						g.drawImage(Globals.CURR_DUNGEON.getImageWallV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// eastern vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '\'')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX() - 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						// western vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no door, so draw vertical wall
						g.drawImage(Globals.CURR_DUNGEON.getImageWallV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// western vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '^')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() - 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						// northern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no door, so draw horizontal wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// northern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == '~')
				{
					if(party.getLocationY() < (currBoard.getHeight() - 1) && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() + 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						// southern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no door, so draw horizontal wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// southern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == ';')
				{
					if(party.getLocationX() < (currBoard.getWidth() - 1) && isLocationRoom(currBoard, party.getLocationX() + 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						// eastern vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no door, so draw vertical wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// eastern vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == ':')
				{
					if(party.getLocationX() > 0 && isLocationRoom(currBoard, party.getLocationX() - 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						// western vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no door, so draw vertical wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// western vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'N')
				{
					if(party.getLocationY() > 0 && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() - 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						// using room door, so draw solid frame
						g.drawImage(Globals.CURR_DUNGEON.getImageWallH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no door, so draw horizontal wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// northern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'S')
				{
					if(party.getLocationY() < (currBoard.getHeight() - 1) && isLocationRoom(currBoard, party.getLocationX(), party.getLocationY() + 1) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						// using room door, so draw solid frame
						g.drawImage(Globals.CURR_DUNGEON.getImageWallH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no door, so draw horizontal wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// southern horizontal door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorH(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'E')
				{
					if(party.getLocationX() < (currBoard.getWidth() - 1) && isLocationRoom(currBoard, party.getLocationX() + 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						// using room door, so draw solid frame
						g.drawImage(Globals.CURR_DUNGEON.getImageWallV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no door, so draw vertical wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// eastern vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'W')
				{
					if(party.getLocationX() > 0 && isLocationRoom(currBoard, party.getLocationX() - 1, party.getLocationY()) && (currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						// using room door, so draw solid frame
						g.drawImage(Globals.CURR_DUNGEON.getImageWallV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no door, so draw vertical wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// western vertical door
						g.drawImage(Globals.CURR_DUNGEON.getImageDoorV(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'n')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						// northern door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() <= 0 || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() - 1) & Globals.INVERSE_FACINGS[Globals.NORTH]) == 0)
					{
						// no northern door, so draw solid wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// northern door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 's')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						// southern door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationY() >= (currBoard.getHeight() - 1) || (currBoard.getGridValue(party.getLocationX(), party.getLocationY() + 1) & Globals.INVERSE_FACINGS[Globals.SOUTH]) == 0)
					{
						// no southern door, so draw solid wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// southern door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'e')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						// eastern door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() >= (currBoard.getWidth() - 1) || (currBoard.getGridValue(party.getLocationX() + 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.EAST]) == 0)
					{
						// no eastern door, so draw solid wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// eastern door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
				else if(currFloorplan.getPlanChar(x, y) == 'w')
				{
					if((currBoard.getGridValue(party.getLocationX(), party.getLocationY()) & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						// western door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else if(party.getLocationX() <= 0 || (currBoard.getGridValue(party.getLocationX() - 1, party.getLocationY()) & Globals.INVERSE_FACINGS[Globals.WEST]) == 0)
					{
						// no western door, so draw solid wall
						g.drawImage(Globals.CURR_DUNGEON.getImageSolid(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
					else
					{
						// western door, so draw floor
						g.drawImage(Globals.CURR_DUNGEON.getImageFloor(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
					}
				}
			}
		}
		// plot any staircase in room
		if(currRoom.getStairType() != Globals.STAIRS_NONE)
		{
			for(int y = 0; y < currFloorplan.getHeight(); y++)
			{
				for(int x = 0; x < currFloorplan.getWidth(); x++)
				{
					if(currFloorplan.getPlanChar(x, y) == '@')
					{
						if(currRoom.getStairType() == Globals.STAIRS_UP)
						{
							// plot stairs up
							g.drawImage(Globals.CURR_DUNGEON.getImageStairsU(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
						}
						else if(currRoom.getStairType() == Globals.STAIRS_DOWN)
						{
							// plot stairs down
							g.drawImage(Globals.CURR_DUNGEON.getImageStairsD(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
						}
						y = currFloorplan.getHeight();
						x = currFloorplan.getWidth();
					}
				}
			}
		}
		// plot any features in room
		if(currRoom.getFeatureType() != Globals.FEATURE_NONE)
		{
			for(int y = 0; y < currFloorplan.getHeight(); y++)
			{
				for(int x = 0; x < currFloorplan.getWidth(); x++)
				{
					if(currFloorplan.getPlanChar(x, y) == 'F')
					{
						if(currRoom.getFeatureType() == Globals.FEATURE_FOUNTAIN)
						{
							// plot fountain
							g.drawImage(Globals.CURR_DUNGEON.getImageFountain(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
						}
						else if(currRoom.getFeatureType() == Globals.FEATURE_STATUE)
						{
							// plot living statue
							g.drawImage(Globals.CURR_DUNGEON.getImageStatue(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
						}
						else if(currRoom.getFeatureType() == Globals.FEATURE_SHOP)
						{
							// plot shop
							g.drawImage(Globals.CURR_DUNGEON.getImageShop(), x * Globals.TILESCALE, y * Globals.TILESCALE, this);
						}
						y = currFloorplan.getHeight();
						x = currFloorplan.getWidth();
					}
				}
			}
		}
		g.dispose();
	}

	public void drawRoomBase(Graphics g)
	{
		g.drawImage(bufferRoomBase, 0, 0, this);
	}

	public void drawRoomLayerItems(Graphics g, Room currRoom)
	{
		if(currRoom.getItemCount() > 0)
		{
			for(int i = currRoom.getItemCount() - 1; i >= 0; i--)
			{
				BaseItem thisItem = currRoom.getItem(i);
				if(thisItem instanceof QuestItem)
				{
					g.drawImage(((QuestItem)thisItem).getQuestImage(), cpItemPos.getX() * Globals.TILESCALE, cpItemPos.getY() * Globals.TILESCALE, this);
				}
				else
				{
					g.drawImage(thisItem.getItemClass().getImage(), cpItemPos.getX() * Globals.TILESCALE, cpItemPos.getY() * Globals.TILESCALE, this);
				}
			}
		}
		if(currRoom.hasChest())
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageChest(), cpItemConPos.getX() * Globals.TILESCALE, cpItemConPos.getY() * Globals.TILESCALE, this);
		}
		else if(currRoom.hasVault())
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageVault(), cpItemConPos.getX() * Globals.TILESCALE, cpItemConPos.getY() * Globals.TILESCALE, this);
		}
	}

	public void drawRoomLayerMonsters(Graphics g, Room currRoom)
	{
		for(int i = 0; i < currRoom.getMonsterCount(); i++)
		{
			Monster thisMonster = currRoom.getMonster(i);
			if(thisMonster != null)
			{
				if(thisMonster.getLocation().equals(Globals.CP_INVALID_LOC))
				{
					thisMonster.setLocation(getClearTile());
				}
				g.drawImage(thisMonster.getRenderModeImage(), thisMonster.getLocation().getX() * Globals.TILESCALE, thisMonster.getLocation().getY() * Globals.TILESCALE, this);
			}
		}
	}

	public void drawRoomLayerParty(Graphics g)
	{
		for(int i = 0; i < party.getSize(); i++)
		{
			Player thisPlayer = party.getPlayer(i);
			if(thisPlayer != null && thisPlayer.isAlive())
			{
				g.drawImage(thisPlayer.getCompositeImage(thisPlayer.getRenderMode(), this), thisPlayer.getLocation().getX() * Globals.TILESCALE, thisPlayer.getLocation().getY() * Globals.TILESCALE, this);
			}
		}
	}

	public void drawCharInterface()
	{
		Graphics g = bufferStrategy.getDrawGraphics();
		// show currency
		g.drawImage(Globals.CURR_DUNGEON.getImageCurrency(), 1 * Globals.TILESCALE, 4 * Globals.TILESCALE, this);
		plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (party.getCurrency() > Globals.MAX_SHOW_VALUE ? Globals.MAX_SHOW_STR : party.getCurrency() + ""), 1, 6, 2, 1);
		// show rations
		g.drawImage(Globals.CURR_DUNGEON.getImageRations(), 1 * Globals.TILESCALE, 7 * Globals.TILESCALE, this);
		plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (party.getRations() > Globals.MAX_SHOW_VALUE ? Globals.MAX_SHOW_STR : party.getRations() + ""), 1, 9, 2, 1);
		// show turns elapsed
		g.drawImage(Globals.CURR_DUNGEON.getImageTurns(), 1 * Globals.TILESCALE, 10 * Globals.TILESCALE, this);
		plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (party.getTurnsElapsed() > Globals.MAX_SHOW_VALUE ? Globals.MAX_SHOW_STR : party.getTurnsElapsed() + ""), 1, 12, 2, 1);
		// show rounds elapsed
		g.drawImage(Globals.CURR_DUNGEON.getImageRounds(), 1 * Globals.TILESCALE, 13 * Globals.TILESCALE, this);
		plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), party.getRoundsElapsed() + "", 1, 15, 2, 1);
		int localoffset = 0;
		for(int i = 0; i < party.getSize(); i++)
		{
			Player thisPlayer = party.getPlayer(i);
			localoffset = (3 * i) + Globals.CURR_DUNGEON.getPartyInterfaceY();
			if(thisPlayer != null)
			{
				// draw "active combatant" box if in combat and is this player's turn
				if((currGameMode == Globals.MODE_COMBAT || currGameMode == Globals.MODE_TARGET) && i == currPlayerPtr)
				{
					g.setColor((thisPlayer.canPerformMove() ?  Globals.CURR_DUNGEON.getColorMovingPartyMember() : Globals.CURR_DUNGEON.getColorActivePartyMember()));
					g.fillRect(Globals.CURR_DUNGEON.getPartyInterfaceX() * Globals.TILESCALE, localoffset * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE);
				}
				// draw player image
				g.drawImage(thisPlayer.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), Globals.CURR_DUNGEON.getPartyInterfaceX() * Globals.TILESCALE, localoffset * Globals.TILESCALE, this);
//				plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), thisPlayer.getCurrentHitPoints() + "", Globals.CURR_DUNGEON.getPartyInterfaceX(), localoffset + 2, 2, 1);
				plotOutlineText(g, thisPlayer.getCurrentHitPoints() + "", Globals.CURR_DUNGEON.getPartyInterfaceX(), localoffset + 2, 2, 1, (thisPlayer.getCurrentHitPoints() == thisPlayer.getHitPoints() ? Globals.CURR_DUNGEON.getColorHitpointsFull() : (thisPlayer.getCurrentHitPoints() <= 0 ? Globals.CURR_DUNGEON.getColorHitpointsNone() : (thisPlayer.getCurrentHitPoints() < (thisPlayer.getHitPoints() / 3) ? Globals.CURR_DUNGEON.getColorHitpointsCrit() : Globals.CURR_DUNGEON.getColorHitpointsSome()))), Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), false);
				// show player's battlegem
				if((currGameMode == Globals.MODE_COMBAT || currGameMode == Globals.MODE_TARGET) && currRoom != null && currRoom.hasActiveMonsters())
				{
					int[] gemNum = getBattlegemNumbers(thisPlayer, currRoom.getMonster(0));
					int   gemX   = ((Globals.CURR_DUNGEON.getPartyInterfaceX() - 1) * Globals.TILESCALE) + (Globals.TILESCALE / 4);
					int   gemY   = (localoffset * Globals.TILESCALE) + (Globals.TILESCALE - (Globals.TILESCALE / 2));
					g.drawImage(Globals.CURR_DUNGEON.getImageBattlegems(), gemX, gemY, gemX + Globals.HALFSCALE, gemY + Globals.TILESCALE, gemNum[0] * Globals.HALFSCALE, 0, (gemNum[0] + 1) * Globals.HALFSCALE, Globals.TILESCALE, this);
					g.drawImage(Globals.CURR_DUNGEON.getImageBattlegems(), gemX + (Globals.TILESCALE * 3), gemY, gemX + (Globals.TILESCALE * 3) + Globals.HALFSCALE, gemY + Globals.TILESCALE, gemNum[1] * Globals.HALFSCALE, 0, (gemNum[1] + 1) * Globals.HALFSCALE, Globals.TILESCALE, this);
				}		
			}
		}
		// add informational overlays
		if(currRoom != null && currRoom.hasActiveMonsters())
		{
			plotPreciseString(g, Globals.FONT_SMALL, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INFO), currRoom.getMonster(0).getName() + " [" + currRoom.getMonster(0).getSpeed() + "]", Globals.CURR_DUNGEON.getMonstInterfaceX(), Globals.CURR_DUNGEON.getMonstInterfaceY(), Globals.MAX_NAME_LENGTH + 4);
		}
		plotPreciseString(g, Globals.FONT_SMALL, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INFO), (party.getDepth() > 0 ? "(" + party.getDepth() + ") " : "") + currBoard.getName(), Globals.CURR_DUNGEON.getLevelInterfaceX(), Globals.CURR_DUNGEON.getLevelInterfaceY(), Globals.MAX_NAME_LENGTH + 4);
		g.dispose();
	}

	public void drawEspView(Graphics2D g)
	{
		int newX = party.getLocationX() + Globals.CPVECTS[party.getFacing()].getX();
		int newY = party.getLocationY() + Globals.CPVECTS[party.getFacing()].getY();
		String sMessage = Globals.getDungeonText("espNoResult");
		if(isLocationRoom(currBoard, newX, newY))
		{
			Room tmpRoom = currBoard.getRoom(newX, newY);
			if(tmpRoom.getMonsterCount() > 0)
			{
				MonsterDef mdef = (MonsterDef)(Globals.BESTIARY.get(tmpRoom.getMonster(0).getName()));
				int xoffst = (int)(((Globals.CANVAS_HALL_X - Globals.ENLARGESCALE) / 2.0) * Globals.TILESCALE);
				int yoffst = (int)(((Globals.CANVAS_HALL_Y - Globals.ENLARGESCALE) / 2.0) * Globals.TILESCALE);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				g.drawImage(mdef.getImgNormal(), xoffst, yoffst, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
				sMessage = Globals.getDungeonText("espShowMonster");
				sMessage = substituteInMessage(sMessage, "$MONSTER", tmpRoom.getMonster(0).getName());
			}
		}
		setCurrentMessage(sMessage);
	}

	private void doShortWait()
	{
		try { Thread.sleep(666); } catch(InterruptedException ie) { }
	}

	private void doLongWait()
	{
		try { Thread.sleep(1212); } catch(InterruptedException ie) { }
	}

//  Calculation Methods ------------------------------------------------------/

	private Coord transformPoint(double x, double y, double z)
	{
		int transX = (int)((x * z) * aspectX);
		int transY = (int)((y * z) * aspectY);
		return new Coord(transX + hallCenterX, transY + hallCenterY);
	}

	private boolean isFloorVisible(int x, int y)
	{
		int x1 = Math.min(x, party.getLocation().getX());
		int y1 = Math.min(y, party.getLocation().getY());
		int x2 = Math.max(x, party.getLocation().getX());
		int y2 = Math.max(y, party.getLocation().getY());
		int dx = x2 - x1;
		int dy = y2 - y1;
		int py = y1;
		int calc = 0;
		for(int px = x1; px <= x2; px++)
		{
			if( currBoard.getGridValue(px, py) == Globals.MAP_SOLID ||
				isLocationRoom(currBoard, px, py)  ||
				(currBoard.getGridValue(px, py) & Globals.FACINGS[party.getFacing()]) == Globals.FACINGS[party.getFacing()])
			{
				return false;
			}
			calc += dy;
			if((calc << 1) >= dx)
			{
				py++;
				calc -= dx;
			}
		}
		return true;
	}

	public void initializePlayerPositions()
	{
		int roomMedianX = currFloorplan.getWidth() / 2;
		int roomMedianY = currFloorplan.getHeight() / 2;
		int playerX = 0;
		int playerY = 0;
		int deepCount = 0;
		int countStart = 0;
		int lastSuccessEven = -1;
		int lastSuccessOdd  = -1;
		boolean currPlayerEven = false;
		int posvar      = 0;
		int highStop    = 0;
		int incrementer = 0;
		boolean loopOK  = true;
		int baseStart = 0;
		if(party.getFacing() == Globals.NORTH)
		{
			baseStart = currFloorplan.getAreaEndY() + (breakMove ? 0 : 3);
		}
		else if(party.getFacing() == Globals.SOUTH)
		{
			baseStart = currFloorplan.getAreaStartY() - (breakMove ? 0 : 3);
		}
		else if(party.getFacing() == Globals.EAST)
		{
			baseStart = currFloorplan.getAreaStartX() - (breakMove ? 0 : 3);
		}
		else if(party.getFacing() == Globals.WEST)
		{
			baseStart = currFloorplan.getAreaEndX() + (breakMove ? 0 : 3);
		}
		for(int ppos = 3; ppos >= 0; ppos--)
		{
			if(ppos % 2 == 0)
			{
				// even # players start on the left or top of the party grouping (players start numbered at 0)
				playerX = roomMedianX - 2;
				playerY = roomMedianY - 2;
				currPlayerEven = true;
				if(lastSuccessEven > -1)
				{
					countStart = lastSuccessEven + (incrementer * 2);
				}
				else
				{
					countStart = baseStart - (deepCount * incrementer * 2);
				}
			}
			else
			{
				// odd # players start on the right or bottom of the party grouping (players start numbered at 0)
				playerX = roomMedianX;
				playerY = roomMedianY;
				currPlayerEven = false;
				if(lastSuccessOdd > -1)
				{
					countStart = lastSuccessEven + (incrementer * 2);
				}
				else
				{
					countStart = baseStart - (deepCount * incrementer * 2);
				}
			}

			loopOK = true;
			posvar = countStart;
			if(party.getFacing() == Globals.NORTH || party.getFacing() == Globals.SOUTH)
			{

				highStop = currFloorplan.getHeight();
				if(party.getFacing() == Globals.NORTH)
				{
					// players entered from south, place along bottom
					incrementer = -1;
				}
				else if(party.getFacing() == Globals.SOUTH)
				{
					// players entered from north, place along top
					incrementer = 1;
				}
				while(posvar < highStop && posvar > -1 && loopOK)
				{
					if(
						(currFloorplan.getLayoutType(playerX,     posvar)     == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(playerX,     posvar)     == Globals.ROOM_OPEN_PLAYER) &&
						(currFloorplan.getLayoutType(playerX,     posvar + 1) == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(playerX,     posvar + 1) == Globals.ROOM_OPEN_PLAYER) &&
						(currFloorplan.getLayoutType(playerX + 1, posvar)     == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(playerX + 1, posvar)     == Globals.ROOM_OPEN_PLAYER) &&
						(currFloorplan.getLayoutType(playerX + 1, posvar + 1) == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(playerX + 1, posvar + 1) == Globals.ROOM_OPEN_PLAYER)
					)
					{
						if(party.getSize() > ppos && party.getPlayer(ppos) != null)
						{
							party.getPlayer(ppos).setLocation(playerX, posvar);
						}
						if(currPlayerEven)
						{
							lastSuccessEven = posvar;
						}
						else
						{
							lastSuccessOdd = posvar;
						}
						loopOK = false;
					}
					posvar += incrementer;
				}

			}
			else
			{

				highStop = currFloorplan.getWidth();
				if(party.getFacing() == Globals.WEST)
				{
					// players entered from west, place along left
					incrementer = -1;
				}
				else if(party.getFacing() == Globals.EAST)
				{
					// players entered from east, place along right
					incrementer = 1;
				}
				while(posvar < highStop && posvar > -1 && loopOK)
				{
					if(
						(currFloorplan.getLayoutType(posvar,     playerY)     == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(posvar,     playerY)     == Globals.ROOM_OPEN_PLAYER) &&
						(currFloorplan.getLayoutType(posvar,     playerY + 1) == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(posvar,     playerY + 1) == Globals.ROOM_OPEN_PLAYER) &&
						(currFloorplan.getLayoutType(posvar + 1, playerY)     == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(posvar + 1, playerY)     == Globals.ROOM_OPEN_PLAYER) &&
						(currFloorplan.getLayoutType(posvar + 1, playerY + 1) == Globals.ROOM_OPEN_ALL || currFloorplan.getLayoutType(posvar + 1, playerY + 1) == Globals.ROOM_OPEN_PLAYER)
					)
					{
						if(party.getSize() > ppos && party.getPlayer(ppos) != null)
						{
							party.getPlayer(ppos).setLocation(posvar, playerY);
						}
						if(currPlayerEven)
						{
							lastSuccessEven = posvar;
						}
						else
						{
							lastSuccessOdd = posvar;
						}
						loopOK = false;
					}
					posvar += incrementer;
				}

			}
			if(currPlayerEven) { deepCount++; }
		}
		breakMove = false;
	}

	public void initializeMonsterPositions(Room currRoom)
	{
		for(int i = 0; i < currRoom.getMonsterCount(); i++)
		{
			Monster thisMonster = currRoom.getMonster(i);
			if(thisMonster != null && thisMonster.isAlive())
			{
				thisMonster.setLocation(getClearTile());
			}
		}
	}

	public Coord getClearTile()
	{
		while(true)
		{
			int posX = (rnd.nextInt(currFloorplan.getAreaXPos()) * 2) + currFloorplan.getAreaStartX();
			int posY = (rnd.nextInt(currFloorplan.getAreaYPos()) * 2) + currFloorplan.getAreaStartY();
			if(isClearTile(posX, posY))
			{
				return new Coord(posX, posY);
			}
		}
	}

	public boolean isClearTile(int posX, int posY, Lifeform lifeform)
	{
		// check if location is within room
		if(!isInsideRoom(posX, posY))
		{
			return false;
		}
		// check if a player already occupies this position
		for(int i = 0; i < party.getSize(); i++)
		{
			Player checkPlayer = party.getPlayer(i);
			if(lifeform != null && lifeform instanceof Player && ((Player)lifeform) == checkPlayer)
			{
				// skip testing same player for own position
			}
			else
			{
				if(checkPlayer != null && checkPlayer.isAlive())
				{
					if(posX == checkPlayer.getLocation().getX() && posY == checkPlayer.getLocation().getY())
					{
						return false;
					}
				}
			}
		}
		// check if another monster already occupies this position
		for(int i = 0; i < currRoom.getMonsterCount(); i++)
		{
			Monster checkMonster = currRoom.getMonster(i);
			if(lifeform != null && lifeform instanceof Monster && ((Monster)lifeform) == checkMonster)
			{
				// skip testing same monster for own position
			}
			else
			{
				if(checkMonster != null && checkMonster.isAlive())
				{
					if(posX == checkMonster.getLocation().getX() && posY == checkMonster.getLocation().getY())
					{
						return false;
					}
				}
			}
		}
		// check floorplan for impassible tiles
		for(int y = posY; y <= posY + 1; y++)
		{
			for(int x = posX; x <= posX + 1; x++)
			{
				if(currFloorplan.getLayoutType(x, y) != Globals.ROOM_OPEN_ALL)
				{
					return false;
				}
			}
		}
		// check if floor items are already there
		if(currRoom.getItemCount() > 0)
		{
			if(posX == cpItemPos.getX() && posY == cpItemPos.getY())
			{
				return false;
			}
		}
		// check if chest is already there
		if(currRoom.hasChest())
		{
			if(posX == cpItemConPos.getX() && posY == cpItemConPos.getY())
			{
				return false;
			}
		}
		// finally, don't block any doors
		if(
			currFloorplan.getLayoutType(posX,     posY)     == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX,     posY - 1) == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX + 1, posY - 1) == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX,     posY + 2) == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX + 1, posY + 2) == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX - 1, posY    ) == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX - 1, posY + 1) == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX + 2, posY    ) == Globals.ROOM_DOOR ||
			currFloorplan.getLayoutType(posX + 2, posY + 1) == Globals.ROOM_DOOR
		)
		{
			return false;
		}
		return true;
	}

	public boolean isClearTile(int posX, int posY)
	{
		return isClearTile(posX, posY, (Lifeform)null);
	}

	public boolean isClearTile(Coord cp, Lifeform lifeform)
	{
		return isClearTile(cp.getX(), cp.getY(), lifeform);
	}

	public boolean isClearTile(Coord cp)
	{
		return isClearTile(cp, (Lifeform)null);
	}

	public Lifeform checkIfLifeformOccupies(int x, int y)
	{
		for(int p = 0; p < party.getSize(); p++)
		{
			if(party.getPlayer(p).isAlive())
			{
				if( (x == party.getPlayer(p).getLocation().getX() || x == party.getPlayer(p).getLocation().getX() + 1) &&
					(y == party.getPlayer(p).getLocation().getY() || y == party.getPlayer(p).getLocation().getY() + 1))
				{
					return (Lifeform)(party.getPlayer(p));
				}
			}
		}
		for(int m = 0; m < currRoom.getMonsters().size(); m++)
		{
			if(currRoom.getMonster(m).isAlive())
			{
				if( (x == currRoom.getMonster(m).getLocation().getX() || x == currRoom.getMonster(m).getLocation().getX() + 1) &&
					(y == currRoom.getMonster(m).getLocation().getY() || y == currRoom.getMonster(m).getLocation().getY() + 1))
				{
					return (Lifeform)(currRoom.getMonster(m));
				}
			}
		}
		return (Lifeform)null;
	}

	public boolean checkIfItemBlockingSpace(int x, int y)
	{
		boolean isBlocked = false;
		if(currRoom.getItemCount() > 0)
		{
			isBlocked = (x     == cpItemPos.getX() && y     == cpItemPos.getY()) ||
						(x + 1 == cpItemPos.getX() && y     == cpItemPos.getY()) ||
						(x     == cpItemPos.getX() && y + 1 == cpItemPos.getY()) ||
						(x + 1 == cpItemPos.getX() && y + 1 == cpItemPos.getY());
			if(isBlocked) { return true; }
		}
		if(currRoom.hasChest() || currRoom.hasVault())
		{
			isBlocked = (x     == cpItemConPos.getX() && y     == cpItemConPos.getY()) ||
						(x + 1 == cpItemConPos.getX() && y     == cpItemConPos.getY()) ||
						(x     == cpItemConPos.getX() && y + 1 == cpItemConPos.getY()) ||
						(x + 1 == cpItemConPos.getX() && y + 1 == cpItemConPos.getY());
			if(isBlocked) { return true; }
		}
		return false;
	}

	public boolean isLegalTextChar(KeyEvent ke)
	{
		return (
					ke.getKeyChar() == KeyEvent.VK_SPACE ||
					(ke.getKeyCode() >= KeyEvent.VK_A && ke.getKeyCode() <= KeyEvent.VK_Z) ||
					(ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9') ||
					ke.getKeyChar() == '\'' ||
					ke.getKeyChar() == '.' ||
					ke.getKeyChar() == ','
				);
	}

	public boolean isLegalFileChar(KeyEvent ke)
	{
		return ((ke.getKeyCode() >= KeyEvent.VK_A && ke.getKeyCode() <= KeyEvent.VK_Z) || (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9'));
	}

	public int getDiceRoll(int dicenum, int dicetyp)
	{
		int sum = 0;
		for(int i = 0; i < dicenum; i++)
		{
			sum += (rnd.nextInt(dicetyp) + 1);
		}
		return sum;
	}

//  Screens Display Methods --------------------------------------------------/

	public void showMapScreen()
	{
		Graphics g2 = bufferStrategy.getDrawGraphics();

		if(bufferEnviron == null)
		{
			bufferEnviron = new BufferedImage(Globals.CANVAS_HALL_X * Globals.TILESCALE, Globals.CANVAS_HALL_Y * Globals.TILESCALE, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g = bufferEnviron.getGraphics();

		if(Globals.CURR_DUNGEON.getImageBackMap() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackMap(), 0, 0, this);
		}
		else
		{
			g.setColor(Globals.CURR_DUNGEON.getColorMapBackground());
			g.fillRect(0, 0, bufferEnviron.getWidth(this), bufferEnviron.getHeight(this));
		}

		int mapOffsetX = (bufferEnviron.getWidth(this)  - (currBoard.getWidth()  * Globals.TILESCALE)) / 2;
		int mapOffsetY = (bufferEnviron.getHeight(this) - (currBoard.getHeight() * Globals.TILESCALE)) / 2;

		for(int y = 0; y < currBoard.getHeight(); y++)
		{
			for(int x = 0; x < currBoard.getWidth(); x++)
			{
				int tileVal = currBoard.getGridValue(x, y);
				int posX = (x * Globals.TILESCALE) + mapOffsetX;
				int posY = (y * Globals.TILESCALE) + mapOffsetY;
				if(currBoard.isAwareOfTile(x, y))
				{
					if(tileVal == Globals.MAP_SOLID)
					{
					}
					else if(isLocationRoom(currBoard, x, y))
					{
						int imageOffset = 0;
						int rowOffset   = 2;
						if(currBoard.isSeen(x, y))
						{
							// only draw room as square, as interior is unknown without being mapped or visited
							imageOffset = Globals.MAPIMAGE_UNKNOWN;
						}
						else
						{
							if(currBoard.getRoom(x, y).hasActiveMonsters())
							{
								imageOffset = Globals.MAPIMAGE_MONSTERS;
							}
							else if(currBoard.getRoom(x, y).getStairType() == Globals.STAIRS_UP)
							{
								imageOffset = Globals.MAPIMAGE_UP;
							}
							else if(currBoard.getRoom(x, y).getStairType() == Globals.STAIRS_DOWN)
							{
								imageOffset = Globals.MAPIMAGE_DOWN;
							}
							else if(currBoard.getRoom(x, y).getFeatureType() == Globals.FEATURE_FOUNTAIN)
							{
								imageOffset = Globals.MAPIMAGE_FOUNTAIN;
							}
							else if(currBoard.getRoom(x, y).getFeatureType() == Globals.FEATURE_STATUE)
							{
								imageOffset = Globals.MAPIMAGE_STATUE;
							}
							else if(currBoard.getRoom(x, y).getFeatureType() == Globals.FEATURE_SHOP)
							{
								imageOffset = Globals.MAPIMAGE_SHOP;
							}
							else if(currBoard.getRoom(x, y).hasVault())
							{
								imageOffset = Globals.MAPIMAGE_VAULT;
							}
							else
							{
								imageOffset = Globals.MAPIMAGE_NORMAL;
							}
							if(currBoard.isVisited(x, y))
							{
								rowOffset = 3;
							}
						}
						g.drawImage(Globals.CURR_DUNGEON.getImageMapTiles(), posX, posY, posX + Globals.TILESCALE, posY + Globals.TILESCALE, imageOffset * Globals.TILESCALE, Globals.TILESCALE * rowOffset, (imageOffset * Globals.TILESCALE) + Globals.TILESCALE, (rowOffset * Globals.TILESCALE) + Globals.TILESCALE, this);
					}
					else if((tileVal & Globals.MAP_FOUNTAIN) == Globals.MAP_FOUNTAIN)
					{
						int rowOffset = 2;
						if(currBoard.isVisited(x, y))
						{
							rowOffset = 3;
						}
						g.drawImage(Globals.CURR_DUNGEON.getImageMapTiles(), posX, posY, posX + Globals.TILESCALE, posY + Globals.TILESCALE, 15 * Globals.TILESCALE, Globals.TILESCALE * rowOffset, (15 * Globals.TILESCALE) + Globals.TILESCALE, (rowOffset * Globals.TILESCALE) + Globals.TILESCALE, this);
					}
					else
					{
						if(Globals.CURR_DUNGEON.getImageMapTiles() != null)
						{
							g.drawImage(Globals.CURR_DUNGEON.getImageMapTiles(), posX, posY, posX + Globals.TILESCALE, posY + Globals.TILESCALE, tileVal * Globals.TILESCALE, (currBoard.isVisited(x, y) ? 0 : Globals.TILESCALE), (tileVal * Globals.TILESCALE) + Globals.TILESCALE, (currBoard.isVisited(x, y) ? Globals.TILESCALE : Globals.TILESCALE * 2), this);
						}
						else
						{
							if((tileVal & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
							{
								g.fillRect(x * Globals.TILESCALE + (Globals.TILESCALE / 4) + (int)(Globals.TILESCALE / 8), y * Globals.TILESCALE, (Globals.TILESCALE / 4), Globals.HALFSCALE);
							}
							if((tileVal & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
							{
								g.fillRect(x * Globals.TILESCALE + (Globals.TILESCALE / 4) + (int)(Globals.TILESCALE / 8), y * Globals.TILESCALE + Globals.HALFSCALE, (Globals.TILESCALE / 4), Globals.HALFSCALE);
							}
							if((tileVal & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
							{
								g.fillRect(x * Globals.TILESCALE + Globals.HALFSCALE, y * Globals.TILESCALE + (Globals.TILESCALE / 4) + (Globals.TILESCALE / 8), Globals.HALFSCALE, (Globals.TILESCALE / 4));
							}
							if((tileVal & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
							{
								g.fillRect(posX, posY + (Globals.TILESCALE / 4) + (Globals.TILESCALE / 8), Globals.HALFSCALE, (Globals.TILESCALE / 4));
							}
						}
					}
				}
			}
		}
		// draw party indicator
		int posX = (party.getLocationX() * Globals.TILESCALE) + mapOffsetX;
		int posY = (party.getLocationY() * Globals.TILESCALE) + mapOffsetY;
		g.drawImage(Globals.CURR_DUNGEON.getImageMapTiles(), posX, posY, posX + Globals.TILESCALE, posY + Globals.TILESCALE, (Globals.MAPIMAGE_PARTY + party.getFacing()) * Globals.TILESCALE, 3 * Globals.TILESCALE, ((Globals.MAPIMAGE_PARTY + party.getFacing()) * Globals.TILESCALE) + Globals.TILESCALE, 4 * Globals.TILESCALE, this);
		// show map screen
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showMenuScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		if(whichMode == Globals.SUBMODE_MENU_MAIN)
		{
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("gameName"), -1, 1);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainNew"),      (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 3);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainLoadGame"), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 4);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainRestock"),  (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 5);
			if(party != null && gameInProgress)
			{
				plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainSave"),   (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 7);
				plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainResume"), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 9);
			}
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainOptions"),  (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 12);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainScreen"), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 16);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainQuit"), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 17);
			plotString(g, Globals.SYS_COLOR_WARN, "VERSION " + VERSION, -1, 26);
		}
		else if(whichMode == Globals.SUBMODE_MENU_NEWGAME)
		{
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuNewTitle"), -1, 1);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuNewLoadModule"), -1, 3);
			Vector vcModules = getModuleFilenames();
			if(vcModules.size() > 0)
			{
				if(currMenuIndex > (vcModules.size() - 1)) { currMenuIndex = 0; }
				else if(currMenuIndex < 0)                 { currMenuIndex = vcModules.size() - 1; }
				currMenuItem = (String)(vcModules.elementAt(currMenuIndex));
				plotTextBox(g, Globals.SYS_FILELIST_COL, Globals.SYS_FILELIST_ROW, Globals.SYS_FILELIST_WIDTH, Globals.SYS_FILELIST_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
				plotTextBox(g, Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW, Globals.SYS_FILEINFO_WIDTH, Globals.SYS_FILEINFO_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
				plotString(g, Globals.SYS_COLOR_HEAD, ObjectParser.getModuleName(Globals.MODSPATH + currMenuItem + Globals.DATAEXTN), Globals.SYS_FILEINFO_COL + 1, Globals.SYS_FILEINFO_ROW, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2, false);
				plotLongString(g, Globals.SYS_COLOR_MENU, ObjectParser.getModuleDescription(Globals.MODSPATH + currMenuItem + Globals.DATAEXTN), Globals.SYS_FILEINFO_COL + 1, Globals.SYS_FILEINFO_ROW + 2, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2);
				int menuOffset = Math.max(0, currMenuIndex - 9);
				for(int m = menuOffset; m < Math.min(menuOffset + 10, vcModules.size()); m++)
				{
					plotString(g, Globals.SYS_COLOR_MENU, (String)(vcModules.elementAt(m)), Globals.SYS_FILELIST_COL + 1, Globals.SYS_FILELIST_ROW + (m - menuOffset), (m == currMenuIndex ? clrMenuSelect : Globals.SYS_COLOR_BOXF), Globals.MAX_FILENAME_LENGTH, false);
				}
			}
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuNewSelect"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 2);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuNewExit"),   Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 3);
		}
		else if(whichMode == Globals.SUBMODE_MENU_LOADGAME)
		{
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuLoadTitle"), -1, 1);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuLoadLoadModule"), -1, 3);
			Vector<String> vcSavegames = getSaveGameFilenames();
			if(vcSavegames.size() > 0)
			{
				if(currMenuIndex > (vcSavegames.size() - 1)) { currMenuIndex = 0; }
				else if(currMenuIndex < 0)                   { currMenuIndex = vcSavegames.size() - 1; }
				currMenuItem = vcSavegames.elementAt(currMenuIndex);
				plotTextBox(g, Globals.SYS_FILELIST_COL, Globals.SYS_FILELIST_ROW, Globals.SYS_FILELIST_WIDTH, Globals.SYS_FILELIST_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
				plotTextBox(g, Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW, Globals.SYS_FILEINFO_WIDTH, Globals.SYS_FILEINFO_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
				int menuOffset = Math.max(0, currMenuIndex - 9);
				for(int m = menuOffset; m < Math.min(menuOffset + 10, vcSavegames.size()); m++)
				{
					plotString(g, (m == currMenuIndex ? Globals.SYS_COLOR_HIGH : Globals.SYS_COLOR_MENU), (m == currMenuIndex ? ">" : " ") + vcSavegames.elementAt(m), Globals.SYS_FILELIST_COL + 1, Globals.SYS_FILELIST_ROW + (m - menuOffset), Globals.MAX_FILENAME_LENGTH, false);
				}
				plotString(g, Globals.SYS_COLOR_MENU, ObjectParser.getModuleName(Globals.MODSPATH + ObjectParser.getModuleName(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN) + Globals.DATAEXTN), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2, false);
				String[] description = ObjectParser.getSaveDescriptor(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN);
				boolean isComplete = ObjectParser.isCompletedGame(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN);
				for(int d = 0; d < description.length; d++)
				{
					plotString(g, (isComplete ? Globals.SYS_COLOR_UNIQ : Globals.SYS_COLOR_MENU), description[d], Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + 2 + d, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2, false);
				}
			}
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuLoadSelect"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 2);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuLoadExit"),   Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 3);
		}
		else if(whichMode == Globals.SUBMODE_MENU_RESTOCK)
		{
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuRestockTitle"), -1, 1);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuRestockLoadModule"), -1, 3);
			Vector<String> vcSavegames = getSaveGameFilenames(true);
			if(vcSavegames.size() > 0)
			{
				if(currMenuIndex > (vcSavegames.size() - 1)) { currMenuIndex = 0; }
				else if(currMenuIndex < 0)                   { currMenuIndex = vcSavegames.size() - 1; }
				currMenuItem = vcSavegames.elementAt(currMenuIndex);
				plotTextBox(g, Globals.SYS_FILELIST_COL, Globals.SYS_FILELIST_ROW, Globals.SYS_FILELIST_WIDTH, Globals.SYS_FILELIST_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
				plotTextBox(g, Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW, Globals.SYS_FILEINFO_WIDTH, Globals.SYS_FILEINFO_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
				int menuOffset = Math.max(0, currMenuIndex - 9);
				for(int m = menuOffset; m < Math.min(menuOffset + 10, vcSavegames.size()); m++)
				{
					plotString(g, (m == currMenuIndex ? Globals.SYS_COLOR_HIGH : Globals.SYS_COLOR_MENU), (m == currMenuIndex ? ">" : " ") + vcSavegames.elementAt(m), Globals.SYS_FILELIST_COL + 1, Globals.SYS_FILELIST_ROW + (m - menuOffset), Globals.MAX_FILENAME_LENGTH, false);
				}
				plotString(g, Globals.SYS_COLOR_MENU, ObjectParser.getModuleName(Globals.MODSPATH + ObjectParser.getModuleName(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN) + Globals.DATAEXTN), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2, false);
				String[] description = ObjectParser.getSaveDescriptor(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN);
				for(int d = 0; d < description.length; d++)
				{
					plotString(g, Globals.SYS_COLOR_MENU, description[d], Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + 2 + d, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2, false);
				}
			}
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuRestockSelect"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 2);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuRestockExit"),   Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 3);
		}
		else if(whichMode == Globals.SUBMODE_MENU_OPTIONS)
		{
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuOptionsTitle"), -1, 1);
			plotString(g, (currMenuOpts == 0 ? Globals.SYS_COLOR_ACTV : Globals.SYS_COLOR_MENU), (currMenuOpts == 0 ? "> " : "  ") + getTextMessage("menuOptsSoundToggle") + " : " + (bSoundOn ? getTextMessage("toggleOn") : getTextMessage("toggleOff")), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 3);
			plotString(g, (currMenuOpts == 1 ? Globals.SYS_COLOR_ACTV : (bSoundOn ? Globals.SYS_COLOR_MENU : Globals.SYS_COLOR_INAC)), (currMenuOpts == 1 ? "> " : "  ") + getTextMessage("menuOptsSoundVolume") + " : " + volSound, (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 4);
			plotString(g, (currMenuOpts == 2 ? Globals.SYS_COLOR_ACTV : Globals.SYS_COLOR_MENU), (currMenuOpts == 2 ? "> " : "  ") + getTextMessage("menuOptsMusicToggle") + " : " + (bMusicOn ? getTextMessage("toggleOn") : getTextMessage("toggleOff")), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 6);
			plotString(g, (currMenuOpts == 3 ? Globals.SYS_COLOR_ACTV : (bMusicOn ? Globals.SYS_COLOR_MENU : Globals.SYS_COLOR_INAC)), (currMenuOpts == 3 ? "> " : "  ") + getTextMessage("menuOptsMusicVolume") + " : " + volMusic, (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 7);
			plotString(g, (currMenuOpts == 4 ? Globals.SYS_COLOR_ACTV : Globals.SYS_COLOR_MENU), (currMenuOpts == 4 ? "> " : "  ") + getTextMessage("menuOptsScreenMode") + " : " + (Globals.VIEWTYPE == Globals.VIEW_FULLSCREEN ? getTextMessage("toggleScreenFloat") : getTextMessage("toggleScreenFull")), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 9);
			plotString(g, (currMenuOpts == 5 ? Globals.SYS_COLOR_ACTV : Globals.SYS_COLOR_MENU), (currMenuOpts == 5 ? "> " : "  ") + getTextMessage("menuOptsPlayerHighlight") + " : " + (showHighlighter ? getTextMessage("toggleOn") : getTextMessage("toggleOff")), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 11);
			plotString(g, (currMenuOpts == 6 ? Globals.SYS_COLOR_ACTV : Globals.SYS_COLOR_MENU), (currMenuOpts == 6 ? "> " : "  ") + getTextMessage("menuOptsShowIntro") + " : " + (showIntro ? getTextMessage("toggleOn") : getTextMessage("toggleOff")), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 13);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuMainResume"), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 18);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuOptNavigate"), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 20);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuOptChange"), (Globals.CANVAS_HALL_X / 4) * Globals.FONTRATIO, 21);
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showCreationScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		if(Globals.CURR_DUNGEON.getImageBackCreate() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackCreate(), 0, 0, this);
		}
		if(whichMode == Globals.SUBMODE_CREATE_LEVELS)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createLevels"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createLevelsHowMany"), -1, 3);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "1 - 10", -1, 4);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createLevelsMessage"), -1, 6);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "ESC - " + getTextMessage("createLevelsExit"), -1, 17);
		}
		else if(whichMode == Globals.SUBMODE_CREATE_PARTY)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createParty"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPartyHowMany"), -1, 3);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "1 - 4", -1, 4);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPartyExit"), -1, 17);
		}
		else if(whichMode == Globals.SUBMODE_CREATE_PLAYER_SETCLASS)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayer"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayerWhich") + " " + (currPlayerPtr + 1), -1, 3);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayerClass"), -1, 4);
			int xIndent = 0;
			for(int i = 0; i < Globals.CHARACTER_CLASSES.length; i++)
			{
				if(Globals.CHARACTER_CLASSES[i].isSoloModeChar() && party.getSize() > 1)
				{
					xIndent = Math.max(xIndent, ((i + 1) + " - " + Globals.CHARACTER_CLASSES[i].getName() + " " + getTextMessage("createPlayerSoloOnly")).length());
				}
				else
				{
					xIndent = Math.max(xIndent, ((i + 1) + " - " + Globals.CHARACTER_CLASSES[i].getName()).length());
				}
			}
			xIndent = ((Globals.CANVAS_HALL_X * Globals.FONTRATIO) - xIndent) / 2;
			for(int i = 0; i < Globals.CHARACTER_CLASSES.length; i++)
			{
				if(Globals.CHARACTER_CLASSES[i].isSoloModeChar() && party.getSize() > 1)
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC), (i + 1) + " - " + Globals.CHARACTER_CLASSES[i].getName() + " " + getTextMessage("createPlayerSoloOnly"), xIndent, 6 + i);
				}
				else
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (i + 1) + " - " + Globals.CHARACTER_CLASSES[i].getName(), xIndent, 6 + i);
				}
			}
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("restartPartyCreation"), -1, 17);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createExit"), -1, 18);
		}
		else if(whichMode == Globals.SUBMODE_CREATE_PLAYER_SETDIFF)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayer"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayerWhich") + " " + (currPlayerPtr + 1), -1, 3);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.CHARACTER_CLASSES[charClassVal].getName(), -1, 4);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayerColor") + " " + Globals.CHARACTER_DIFF_DESC, -1, 5);
			int xIndent = 0;
			for(int i = 0; i < Globals.CHARACTER_CLASSES.length; i++)
			{
				xIndent = Math.max(xIndent, ((i + 1) + " - " + Globals.CHARACTER_DIFF_ASSOCS[i]).length());
			}
			xIndent = ((Globals.CANVAS_HALL_X * Globals.FONTRATIO) - xIndent) / 2;
			for(int i = 0; i < Globals.CHARACTER_DIFF_ASSOCS.length; i++)
			{
				plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (i + 1) + " - " + Globals.CHARACTER_DIFF_ASSOCS[i], xIndent, 7 + i);
			}
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("restartPlayerCreation"), -1, 17);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createExit"), -1, 18);
		}
		else if(whichMode == Globals.SUBMODE_CREATE_PLAYER_SETNAME)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayer"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayerWhich") + " " + (currPlayerPtr + 1), -1, 3);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.CHARACTER_CLASSES[charClassVal].getName(), -1, 4);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.CHARACTER_DIFF_ASSOCS[charDiffVal], -1, 5);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayerName"), -1, 7);
			String sPlayerName = sbPlayerName.toString();
			for(int i = sPlayerName.length(); i < Globals.MAX_NAME_LENGTH; i++)
			{
				sPlayerName = sPlayerName + "_";
			}
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), sPlayerName, -1, 9);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("acceptTextEntry"), -1, 15);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("clearPrevTextEntry"), -1, 16);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("clearAllTextEntry"), -1, 17);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("restartPlayerCreation"), -1, 18);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createPlayerExit"), -1, 19);
		}
		else if(whichMode == Globals.SUBMODE_CREATE_DUNGEON_DIFF)
		{
			int xIndent = (getTextMessage("createDifficultyEasy")).length();
			xIndent = Math.max(xIndent, (getTextMessage("createDifficultyNormal")).length());
			xIndent = Math.max(xIndent, (getTextMessage("createDifficultyHard")).length());
			if(party != null && party.getGameCompleted()) { xIndent = Math.max(xIndent, (getTextMessage("createDifficultyEpic")).length()); }
			xIndent = ((Globals.CANVAS_HALL_X * Globals.FONTRATIO) - xIndent) / 2;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createDifficulty"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createDifficultyEasy"), xIndent, 4);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createDifficultyNormal"), xIndent, 5);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createDifficultyHard"), xIndent, 6);
			if(party != null && party.getGameCompleted()) { plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createDifficultyEpic"), xIndent, 7); }
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("createDifficultyExit"), -1, 17);
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showSaveScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		plotTextBox(g, Globals.SYS_FILELIST_COL, Globals.SYS_FILELIST_ROW, Globals.SYS_FILELIST_WIDTH, Globals.SYS_FILELIST_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
		plotTextBox(g, Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW, Globals.SYS_FILEINFO_WIDTH, Globals.SYS_FILEINFO_HEIGHT, Globals.SYS_COLOR_BOXO, Globals.SYS_COLOR_BOXF);
		plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuSaveTitle"), -1, 1);
		plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuSaveExistingSaves"), Globals.SYS_FILELIST_COL, Globals.SYS_FILELIST_ROW - 2);
		Vector<String> vcSavegames = getSaveGameFilenames();
		vcSavegames.insertElementAt(getTextMessage("menuSaveNewSaveOption"), 0);
		if(currMenuIndex > (vcSavegames.size() - 1)) { currMenuIndex = 0; }
		else if(currMenuIndex < 0)                   { currMenuIndex = vcSavegames.size() - 1; }
		currMenuItem = vcSavegames.elementAt(currMenuIndex);
		if(vcSavegames.size() > 0)
		{
			int menuOffset = Math.max(0, currMenuIndex - 9);
			for(int m = menuOffset; m < Math.min(menuOffset + 10, vcSavegames.size()); m++)
			{
				plotString(g, (m == currMenuIndex ? Globals.SYS_COLOR_HIGH : Globals.SYS_COLOR_MENU), (m == currMenuIndex ? ">" : " ") + vcSavegames.elementAt(m), Globals.SYS_FILELIST_COL, Globals.SYS_FILELIST_ROW + (m - menuOffset), Globals.MAX_FILENAME_LENGTH, false);
			}
		}
		if(currMenuIndex > 0)
		{
			plotString(g, Globals.SYS_COLOR_MENU, ObjectParser.getModuleName(Globals.MODSPATH + ObjectParser.getModuleName(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN) + Globals.DATAEXTN), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2, false);
			String[] description = ObjectParser.getSaveDescriptor(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN);
			boolean isComplete = ObjectParser.isCompletedGame(Globals.SAVEPATH + currMenuItem + Globals.SAVEEXTN);
			for(int d = 0; d < description.length; d++)
			{
				plotString(g, (isComplete ? Globals.SYS_COLOR_UNIQ : Globals.SYS_COLOR_MENU), description[d], Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + 2 + d, (Color)null, Globals.SYS_FILEINFO_WIDTH - 2, false);
			}
		}
		if(whichMode == Globals.SUBMODE_SAVE_FILE)
		{
			plotString(g, Globals.SYS_COLOR_MENU, "ESC - " + getTextMessage("menuSaveExit"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 4);
		}
		else if(whichMode == Globals.SUBMODE_SAVE_CREATE)
		{
			String sNewFileName = getTextMessage("menuSaveNewFileName") + " : " + currInputBuffer.toString();
			for(int i = currInputBuffer.length(); i < Globals.MAX_FILENAME_LENGTH; i++)
			{
				sNewFileName = sNewFileName + "_";
			}
			plotString(g, Globals.SYS_COLOR_MENU, sNewFileName, Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 2, sNewFileName.length(), false);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("acceptTextEntry"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 4);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("clearPrevTextEntry"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 5);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("clearAllTextEntry"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 6);
			plotString(g, Globals.SYS_COLOR_MENU, getTextMessage("menuSaveCreateExit"), Globals.SYS_FILEINFO_COL, Globals.SYS_FILEINFO_ROW + Globals.SYS_FILEINFO_HEIGHT + 7);
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showItemUseScreen(int whichPlayer, int whichMode)
	{
		if(currItemIndex < 0)
		{
			currItemIndex = 0;
		}
		showPlayerInventoryScreen(whichPlayer, whichMode);
	}

	public void showItemManagementScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int colPtr  = Globals.CURR_DUNGEON.getItemSourceCol();
		int rowPtr  = Globals.CURR_DUNGEON.getItemSourceRow();
		int rowPtr2 = Globals.CURR_DUNGEON.getItemTargetRow();
		int colPtr2 = Globals.CURR_DUNGEON.getItemTargetCol();
		if(Globals.CURR_DUNGEON.getImageBackItemManage() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackItemManage(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr, rowPtr, Globals.CURR_DUNGEON.getItemSourceWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemSourceHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
			plotTextBox(g, colPtr2, rowPtr2, Globals.CURR_DUNGEON.getItemTargetWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		if(whichMode == Globals.SUBMODE_ITEM_PICKUP)
		{
			// draw list of items that can be picked up
			for(int itm = 0; itm < currRoom.getItemCount(); itm++)
			{
				if(itm == currItemIndex)
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_ACTV), "> ", colPtr, rowPtr, Globals.MAX_NAME_LENGTH, false);
				}
				String sItemName = currRoom.getItem(itm).getInventoryText();
				plotString(g, (itm == currItemIndex ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), sItemName, colPtr + 2, rowPtr, Globals.MAX_NAME_LENGTH, false);
				rowPtr++;
			}
			// get current item type, used to determine what player items are shown in player area, as well as permissions
			BaseItem bitm = (BaseItem)null;
			if(currItemIndex >= 0 && currItemIndex < currRoom.getItemCount())
			{
				bitm = currRoom.getItem(currItemIndex);
			}
			// draw list of players
			for(int p = 0; p < party.getSize(); p++)
			{
				Player playerPtr = party.getPlayer(p);
				g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
				plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (p + 1) + "", colPtr2 / Globals.FONTRATIO, rowPtr2 + 2, 2, 1);
				int itemFlag = getItemUsabilityFlag(playerPtr, bitm);
				if(itemFlag == Globals.ITEMSTATUS_INVENTORYFULL)
				{
					g.drawImage(Globals.CURR_DUNGEON.getImageInvFull(), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
				}
				else if(itemFlag == Globals.ITEMSTATUS_NOTUSABLE)
				{
					g.drawImage(Globals.CURR_DUNGEON.getImageInvNotUsable(), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
				}
				else if(itemFlag == Globals.ITEMSTATUS_NOTALLOWED)
				{
					g.drawImage(Globals.CURR_DUNGEON.getImageInvNotAllowed(), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
				}
				if(bitm != null && bitm.getType() == Globals.ITEMTYPE_WEAPON)
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), playerPtr.getEquiptWeapon().getInventoryText(), colPtr2 + 5, rowPtr2);
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), playerPtr.getAlternWeapon().getInventoryText(), colPtr2 + 5, rowPtr2 + 1);
				}
				else if(bitm != null && bitm.getType() == Globals.ITEMTYPE_ARMOR)
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), playerPtr.getArmorSlotBody().getInventoryText(),    colPtr2 + 5, rowPtr2);
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), playerPtr.getArmorSlotSpec().getInventoryText(), colPtr2 + 5, rowPtr2 + 1);
				}
				else
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("inventory"), colPtr2 + 5, rowPtr2);
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), playerPtr.getInventorySize() + "",   colPtr2 + 5, rowPtr2 + 1);
				}
				rowPtr2 += 4;
			}
		}
		else
		{
			Player playerPtr = party.getPlayer(currItemPlayer);
			g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getName(), colPtr, rowPtr + 2);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_ACTV), "> ", colPtr, rowPtr + 4 + currItemIndex);
			rowPtr += 4;
			g.drawImage(playerPtr.getEquiptWeapon().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 0 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getEquiptWeapon().getInventoryText(), colPtr + 4, rowPtr++);
			g.drawImage(playerPtr.getAlternWeapon().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 1 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getAlternWeapon().getInventoryText(), colPtr + 4, rowPtr++);
			g.drawImage(playerPtr.getArmorSlotBody().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 2 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getArmorSlotBody().getInventoryText(), colPtr + 4, rowPtr++);
			g.drawImage(playerPtr.getArmorSlotSpec().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 3 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getArmorSlotSpec().getInventoryText(), colPtr + 4, rowPtr++);
			for(int inv = 0; inv < playerPtr.getInventorySize(); inv++)
			{
				g.drawImage(playerPtr.getInventoryItem(inv).getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
				plotString(g, (currItemIndex == (inv + 4) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getInventoryItem(inv).getInventoryText(), colPtr + 4, rowPtr++);
			}
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showItemTradeScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int colPtr  = Globals.CURR_DUNGEON.getItemSourceCol();
		int rowPtr  = Globals.CURR_DUNGEON.getItemSourceRow();
		int rowPtr2 = Globals.CURR_DUNGEON.getItemTargetRow();
		int colPtr2 = Globals.CURR_DUNGEON.getItemTargetCol();
		if(Globals.CURR_DUNGEON.getImageBackItemManage() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackItemManage(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr, rowPtr, Globals.CURR_DUNGEON.getItemSourceWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemSourceHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
			plotTextBox(g, colPtr2, rowPtr2, Globals.CURR_DUNGEON.getItemTargetWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		if(whichMode == Globals.SUBMODE_TRADE_SELECT_PLAYER)
		{
			// draw party list for selecting first trader
			for(int p = 0; p < party.getSize(); p++)
			{
				Player playerPtr = party.getPlayer(p);
				g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
				plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (p + 1) + "", colPtr / Globals.FONTRATIO, rowPtr + 2, 2, 1);
				rowPtr += 4;
			}
		}
		else if(whichMode == Globals.SUBMODE_TRADE_SELECT_TRADER)
		{
			// draw the main trader
			Player playerPtr = party.getPlayer(currItemPlayer);
			g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			rowPtr += 3;
			g.drawImage(playerPtr.getEquiptWeapon().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 0 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getEquiptWeapon().getInventoryText(), colPtr + 4, rowPtr++);
			g.drawImage(playerPtr.getAlternWeapon().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 1 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getAlternWeapon().getInventoryText(), colPtr + 4, rowPtr++);
			rowPtr++;
			g.drawImage(playerPtr.getArmorSlotBody().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 2 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getArmorSlotBody().getInventoryText(), colPtr + 4, rowPtr++);
			g.drawImage(playerPtr.getArmorSlotSpec().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 3 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getArmorSlotSpec().getInventoryText(), colPtr + 4, rowPtr++);
			rowPtr++;
			for(int inv = 0; inv < playerPtr.getInventorySize(); inv++)
			{
				g.drawImage(playerPtr.getInventoryItem(inv).getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
				plotString(g, (currItemIndex == (inv + 4) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getInventoryItem(inv).getInventoryText(), colPtr + 4, rowPtr++);
			}
			// draw party list for selecting second trader
			for(int p = 0; p < party.getSize(); p++)
			{
				if(p != currItemPlayer)
				{
					Player playerPtr2 = party.getPlayer(p);
					g.drawImage(playerPtr2.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
					plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (p + 1) + "", colPtr2 / Globals.FONTRATIO, rowPtr2 + 2, 2, 1);
					rowPtr2 += 4;
				}
			}
		}
		else if(whichMode == Globals.SUBMODE_TRADE_SELECT_ITEM)
		{
			// draw the main trader
			Player playerPtr = party.getPlayer(currItemPlayer);
			g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			rowPtr += 3;
			g.drawImage(playerPtr.getEquiptWeapon().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 0 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getEquiptWeapon().getInventoryText(), colPtr + 4, rowPtr++);
			g.drawImage(playerPtr.getAlternWeapon().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 1 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getAlternWeapon().getInventoryText(), colPtr + 4, rowPtr++);
			rowPtr++;
			g.drawImage(playerPtr.getArmorSlotBody().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 2 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getArmorSlotBody().getInventoryText(), colPtr + 4, rowPtr++);
			g.drawImage(playerPtr.getArmorSlotSpec().getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (currItemIndex == 3 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getArmorSlotSpec().getInventoryText(), colPtr + 4, rowPtr++);
			rowPtr++;
			for(int inv = 0; inv < playerPtr.getInventorySize(); inv++)
			{
				g.drawImage(playerPtr.getInventoryItem(inv).getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
				plotString(g, (currItemIndex == (inv + 4) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getInventoryItem(inv).getInventoryText(), colPtr + 4, rowPtr++);
			}
			if(currItemIndex < 2)
			{
				currTradeItem = 1;
			}
			else if(currItemIndex > 3)
			{
				currTradeItem = -1;
			}
			else
			{
				currTradeItem = currItemIndex;
			}
			// draw the secondary trader
			Player playerPtr2 = party.getPlayer(currTradePlayer);
			g.drawImage(playerPtr2.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			rowPtr2 += 3;
			g.drawImage(playerPtr2.getEquiptWeapon().getClassIcon(), ((colPtr2 + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, this);
			plotString(g, (currTradeItem == 0 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr2.getEquiptWeapon().getInventoryText(), colPtr2 + 4, rowPtr2++);
			g.drawImage(playerPtr2.getAlternWeapon().getClassIcon(), ((colPtr2 + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, this);
			plotString(g, (currTradeItem == 1 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr2.getAlternWeapon().getInventoryText(), colPtr2 + 4, rowPtr2++);
			rowPtr2++;
			g.drawImage(playerPtr2.getArmorSlotBody().getClassIcon(), ((colPtr2 + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, this);
			plotString(g, (currTradeItem == 2 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr2.getArmorSlotBody().getInventoryText(), colPtr2 + 4, rowPtr2++);
			g.drawImage(playerPtr2.getArmorSlotSpec().getClassIcon(), ((colPtr2 + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, this);
			plotString(g, (currTradeItem == 3 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr2.getArmorSlotSpec().getInventoryText(), colPtr2 + 4, rowPtr2++);
			rowPtr2++;
			for(int inv = 0; inv < playerPtr2.getInventorySize(); inv++)
			{
				g.drawImage(playerPtr2.getInventoryItem(inv).getClassIcon(), ((colPtr2 + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, this);
				plotString(g, (currTradeItem == (inv + 4) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr2.getInventoryItem(inv).getInventoryText(), colPtr2 + 4, rowPtr2++);
			}
			if(playerPtr2.getInventorySize() < Globals.MAX_INVENTORY)
			{
				plotString(g, (currTradeItem == -1 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), Globals.getDungeonText("tradeIntoInv"), colPtr2 + 4, rowPtr2++);
			}
			int itemFlag1 = getItemUsabilityFlag(playerPtr2, getPlayerTransactionItem(playerPtr, currItemIndex));
			if(itemFlag1 == Globals.ITEMSTATUS_INVENTORYFULL)
			{
				g.drawImage(Globals.CURR_DUNGEON.getImageInvFull(), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetRow() * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			}
			else if(itemFlag1 == Globals.ITEMSTATUS_NOTUSABLE)
			{
				g.drawImage(Globals.CURR_DUNGEON.getImageInvNotUsable(), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetRow() * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			}
			else if(itemFlag1 == Globals.ITEMSTATUS_NOTALLOWED)
			{
				g.drawImage(Globals.CURR_DUNGEON.getImageInvNotAllowed(), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetRow() * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			}
			int itemFlag2 = getItemUsabilityFlag(playerPtr, getPlayerTransactionItem(playerPtr2, currTradeItem));
			if(itemFlag2 == Globals.ITEMSTATUS_INVENTORYFULL)
			{
				g.drawImage(Globals.CURR_DUNGEON.getImageInvFull(), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetRow() * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			}
			else if(itemFlag2 == Globals.ITEMSTATUS_NOTUSABLE)
			{
				g.drawImage(Globals.CURR_DUNGEON.getImageInvNotUsable(), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetRow() * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			}
			else if(itemFlag2 == Globals.ITEMSTATUS_NOTALLOWED)
			{
				g.drawImage(Globals.CURR_DUNGEON.getImageInvNotAllowed(), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetRow() * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
			}
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showPlayerInfoScreen(int whichPlayer, int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int rowPtr = Globals.CURR_DUNGEON.getPlayerInfoRow();
		int colPtr = Globals.CURR_DUNGEON.getPlayerInfoCol();
		if(Globals.CURR_DUNGEON.getImageBackPlayer() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackPlayer(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr, rowPtr, Globals.CURR_DUNGEON.getPlayerInfoWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getPlayerInfoHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		Player playerPtr = party.getPlayer(whichPlayer);
		g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), Globals.TILESCALE * 2, Globals.TILESCALE * 6, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
		plotString(g, Globals.FONT_MAIN, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), playerPtr.getName(), colPtr, rowPtr++);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), playerPtr.getCharacterClassName(), colPtr, rowPtr++);
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statLevel") + Globals.getDungeonText("statPadding") + playerPtr.getLevel(), colPtr, rowPtr++);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statExperience") + Globals.getDungeonText("statPadding") + playerPtr.getExperience() + " [" + (playerPtr.getExpForNextLevel() - playerPtr.getExperience()) + "]", colPtr, rowPtr++);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statHitpoints") + Globals.getDungeonText("statPadding") + (playerPtr.getHitPoints() - playerPtr.getWounds()) + "/" + playerPtr.getHitPoints(), colPtr, rowPtr++);
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HEAD), Globals.getDungeonText("statWeapons"), colPtr, rowPtr, Globals.MAX_NAME_LENGTH + 2, false);
		colPtr += Globals.MAX_NAME_LENGTH + 3;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HEAD), Globals.getDungeonText("statDamage"), colPtr, rowPtr, 3, false);
		colPtr += 4;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HEAD), Globals.getDungeonText("statAmmo"), colPtr, rowPtr++, 4, false);
		for(int w = 0; w < 2; w++)
		{
			colPtr = Globals.CURR_DUNGEON.getPlayerInfoCol();
			plotString(g, (w == playerPtr.getEquiptWeaponIndex() ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_ACTV) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC)), (w == playerPtr.getEquiptWeaponIndex() ? "> " : "  ") + playerPtr.getWeapon(w).getName(), colPtr, rowPtr);
			colPtr += Globals.MAX_NAME_LENGTH + 3;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "" + playerPtr.getWeapon(w).getCurrDmg(), colPtr, rowPtr, 3, true);
			colPtr += 4;
			if(playerPtr.getWeapon(w).requiresAmmo())
			{
				plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), (playerPtr.getAmmoQuantityForWeapon(w) < 0 ? "**" : playerPtr.getAmmoQuantityForWeapon(w) + ""), colPtr, rowPtr, 4, true);
			}
			else
			{
				plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), (playerPtr.getWeapon(w).isThrowable() ? ">>" : (playerPtr.getWeapon(w).isRanged() ? "++" : "--")), colPtr, rowPtr, 4, true);
			}
			rowPtr++;
		}
		rowPtr++;
		colPtr = Globals.CURR_DUNGEON.getPlayerInfoCol();
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HEAD), Globals.getDungeonText("statArmor"), colPtr, rowPtr, 17, false);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HEAD), Globals.getDungeonText("statProtection"), colPtr + Globals.MAX_NAME_LENGTH + 3, rowPtr, 3, false);
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "  " + playerPtr.getArmorSlotBody().getName(), colPtr, rowPtr);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "" + playerPtr.getArmorSlotBody().getCurrProt(), colPtr + Globals.MAX_NAME_LENGTH + 3, rowPtr, 3, true);
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "  " + playerPtr.getArmorSlotSpec().getName(), colPtr, rowPtr);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "" + playerPtr.getArmorSlotSpec().getCurrProt(), colPtr + Globals.MAX_NAME_LENGTH + 3, rowPtr, 3, true);
		rowPtr++;
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HEAD), Globals.getDungeonText("statBonuses"), colPtr, rowPtr, 17, false);
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "  " + Globals.getDungeonText("statBonusMelee") + ": " + playerPtr.getMeleeBonus() + "  " + Globals.getDungeonText("statBonusLuck") + ": " + playerPtr.getLuck(), colPtr, rowPtr);
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "  " + Globals.getDungeonText("statBonusRange") + ": " + playerPtr.getRangeBonus() + "  " + Globals.getDungeonText("statBonusResistance") + ": " + playerPtr.getResistance(), colPtr, rowPtr);
		rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), "  " + Globals.getDungeonText("statBonusArmor") + ": " + playerPtr.getArmorBonus() + "  " + Globals.getDungeonText("statBonusTrap") + ": " + playerPtr.getTrapResist(), colPtr, rowPtr);
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showPlayerInventoryScreen(int whichPlayer, int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int rowPtr = Globals.CURR_DUNGEON.getPlayerInfoRow();
		int colPtr = Globals.CURR_DUNGEON.getPlayerInfoCol();
		if(Globals.CURR_DUNGEON.getImageBackPlayer() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackPlayer(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr, rowPtr, Globals.CURR_DUNGEON.getPlayerInfoWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getPlayerInfoHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		Player playerPtr = party.getPlayer(whichPlayer);
		g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), Globals.TILESCALE * 2, Globals.TILESCALE * 6, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("inventory"), colPtr + ((28 - Globals.getDungeonText("inventory").length()) / 2), rowPtr);
		rowPtr++;
		rowPtr++;
		for(int inv = 0; inv < Globals.MAX_INVENTORY; inv++)
		{
			if(inv == currItemIndex)
			{
				plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_ACTV), "> ", colPtr, rowPtr, Globals.MAX_NAME_LENGTH, false);
			}
			if(inv >= playerPtr.getInventorySize())
			{
				plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("noInvItem"), colPtr + 2, rowPtr, Globals.MAX_NAME_LENGTH, false);
			}
			else
			{
				g.drawImage(playerPtr.getInventoryItem(inv).getClassIcon(), ((colPtr + 2) * Globals.HALFSCALE), rowPtr * Globals.TILESCALE, this); 
				plotString(g, (inv == currItemIndex ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), playerPtr.getInventoryItem(inv).getInventoryText(), colPtr + 5, rowPtr, Globals.MAX_NAME_LENGTH, false);
				if(inv == currItemIndex && playerPtr.getInventoryItem(inv).isIdentified())
				{
					String sEffectText = Globals.getDungeonText("effectDescr" + playerPtr.getInventoryItem(inv).getEffect().getEffect());
					sEffectText = substituteInMessage(sEffectText, "$POLAR", Globals.getDungeonText("effectDescr" + (playerPtr.getInventoryItem(inv).getEffect().isPositive() ? "Positive" : "Negative") + ((playerPtr.getInventoryItem(inv).getEffect().getTarget() == Globals.FX_TARGET_ALLPLAYERS || playerPtr.getInventoryItem(inv).getEffect().getTarget() == Globals.FX_TARGET_ALLMONSTERS) ? "All" : (playerPtr.getInventoryItem(inv).getEffect().getTarget() == Globals.FX_TARGET_EVERYONE ? "Every" : ""))));
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH), sEffectText, colPtr, Globals.CURR_DUNGEON.getPlayerInfoRow() + Globals.MAX_INVENTORY + 3, sEffectText.length(), false);
				}
			}
			rowPtr++;
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showPartyInfoScreen()
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();

		if(Globals.CURR_DUNGEON.getImageBackParty() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackParty(), 0, 0, this);
		}

		int memberOffset = 10;
		int printColumn  = 1;
		int printRow     = 1;
		int invColumn    = 0;

		for(int i = 0; i < party.getSize(); i++)
		{
			printColumn = ((i * memberOffset) * 2) + 1;
			printRow    = 1;
			invColumn   = printColumn + 2;
			Player playerPtr = party.getPlayer(i);
			if(playerPtr == null)
			{
				// ignore
			}
			else
			{
				if(Globals.CURR_DUNGEON.getImageBackPartyUnit() != null)
				{
					g.drawImage(Globals.CURR_DUNGEON.getImageBackPartyUnit(), Globals.TILESCALE * (i * memberOffset), 0, this);
				}
				g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), Globals.TILESCALE * ((i * memberOffset) + 1), Globals.TILESCALE * 3, this);
			}
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getName(), printColumn, printRow, true);
			printRow = 3;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getLevel() + "", printColumn + 6, printRow);
			printRow++;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getCurrentHitPoints() + "/" + playerPtr.getHitPoints(), printColumn + 6, printRow);
			printRow = 6;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getCharacterClassName(), printColumn, printRow, true);
			printRow = 9;
			g.drawImage(playerPtr.getEquiptWeapon().getClassIcon(), ((i * memberOffset) * Globals.TILESCALE) + Globals.HALFSCALE,     printRow * Globals.TILESCALE, this); plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getEquiptWeapon().getName(), invColumn, printRow, true); printRow++;
			g.drawImage(playerPtr.getAlternWeapon().getClassIcon(), ((i * memberOffset) * Globals.TILESCALE) + Globals.HALFSCALE,     printRow * Globals.TILESCALE, this); plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getAlternWeapon().getName(), invColumn, printRow, true); printRow++;
			g.drawImage(playerPtr.getArmorSlotBody().getClassIcon(), ((i * memberOffset) * Globals.TILESCALE) + Globals.HALFSCALE,    printRow * Globals.TILESCALE, this); plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getArmorSlotBody().getName(), invColumn, printRow, true); printRow++;
			g.drawImage(playerPtr.getArmorSlotSpec().getClassIcon(), ((i * memberOffset) * Globals.TILESCALE) + Globals.HALFSCALE, printRow * Globals.TILESCALE, this); plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getArmorSlotSpec().getName(), invColumn, printRow, true); printRow++;
			printRow = 15;
			for(int inv = 0; inv < Globals.MAX_INVENTORY; inv++)
			{
				if(inv < playerPtr.getInventorySize())
				{
					g.drawImage(playerPtr.getInventoryItem(inv).getClassIcon(), ((i * memberOffset) * Globals.TILESCALE) + Globals.HALFSCALE, printRow * Globals.TILESCALE, this);
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getInventoryItem(inv).getInventoryText(), invColumn, printRow, true);
				}
				printRow++;
			}
		}
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("currencyName") + " : " + party.getCurrency(), 1, 27, true);
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showMonsterInfoScreen()
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int colPtr = Globals.CURR_DUNGEON.getMonsterInfoCol();
		int rowPtr = Globals.CURR_DUNGEON.getMonsterInfoRow();
		if(Globals.CURR_DUNGEON.getImageBackMonster() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackMonster(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr, rowPtr, Globals.CURR_DUNGEON.getMonsterInfoWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getMonsterInfoHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		MonsterDef mDefinition = Globals.BESTIARY.get(currRoom.getMonster(0).getName());
		g.drawImage(mDefinition.getImgNormal(), Globals.TILESCALE * 2, Globals.TILESCALE * 6, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
		plotString(g, Globals.CURR_DUNGEON.getColorBank((mDefinition.isUnique() ? Globals.FONT_COLOR_UNIQ : Globals.FONT_COLOR_STAT)), mDefinition.getName(), colPtr, rowPtr); rowPtr++; rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statHitpoints") + Globals.getDungeonText("statPadding") + mDefinition.getLevel() + "-" + (mDefinition.getLevel() * 6) + "  " + Globals.getDungeonText("statExperience") + Globals.getDungeonText("statPadding") + mDefinition.getExpGrant(), colPtr, rowPtr); rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statAttack") + Globals.getDungeonText("statPadding") + mDefinition.getAttack() + (mDefinition.getRanged() ? " " + Globals.getDungeonText("statRanged") : ""), colPtr, rowPtr); rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statDefend") + Globals.getDungeonText("statPadding") + mDefinition.getDefend(), colPtr, rowPtr); rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statMaxDamage") + Globals.getDungeonText("statPadding") + mDefinition.getMaxDamage(), colPtr, rowPtr); rowPtr++; rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statSpeed") + Globals.getDungeonText("statPadding") + mDefinition.getSpeed(), colPtr, rowPtr); rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statMobility") + Globals.getDungeonText("statPadding") + mDefinition.getMobility() + "%", colPtr, rowPtr); rowPtr++; rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statResistance") + Globals.getDungeonText("statPadding") + mDefinition.getResistance() + "%", colPtr, rowPtr); rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statNegotiation") + Globals.getDungeonText("statPadding") + mDefinition.getNegotiation() + "%", colPtr, rowPtr); rowPtr++; rowPtr++;
		if(mDefinition.getEffect() != null && mDefinition.getEffect() != Globals.NO_EFFECT)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), Globals.getDungeonText("statSpecial") + Globals.getDungeonText("statPadding") + mDefinition.getEffect().getName() + (mDefinition.getEffectRanged() ? " " + Globals.getDungeonText("statRanged") : "") + " " + mDefinition.getEffectPer() + "%", colPtr, rowPtr); rowPtr++;
			String sEffectText = Globals.getDungeonText("effectDescr" + mDefinition.getEffect().getEffect());
			sEffectText = substituteInMessage(sEffectText, "$POLAR", Globals.getDungeonText("effectDescr" + (mDefinition.getEffect().isPositive() ? "Positive" : "Negative") + ((mDefinition.getEffect().getTarget() == Globals.FX_TARGET_ALLPLAYERS || mDefinition.getEffect().getTarget() == Globals.FX_TARGET_ALLMONSTERS) ? "All" : (mDefinition.getEffect().getTarget() == Globals.FX_TARGET_EVERYONE ? "Every" : ""))));
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_STAT), sEffectText, colPtr, rowPtr);
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showQuestInfoScreen()
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int colPtr1 = Globals.CURR_DUNGEON.getQuestListCol();
		int rowPtr1 = Globals.CURR_DUNGEON.getQuestListRow();
		int colPtr2 = Globals.CURR_DUNGEON.getQuestTurnCol();
		int rowPtr2 = Globals.CURR_DUNGEON.getQuestTurnRow();
		int colPtr3 = Globals.CURR_DUNGEON.getQuestInfoCol();
		int rowPtr3 = Globals.CURR_DUNGEON.getQuestInfoRow();
		if(Globals.CURR_DUNGEON.getImageBackQuest() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackQuest(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr1, rowPtr1, Globals.CURR_DUNGEON.getQuestListWid(), Globals.CURR_DUNGEON.getQuestListHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
			plotTextBox(g, colPtr2, rowPtr2, Globals.CURR_DUNGEON.getQuestTurnWid(), Globals.CURR_DUNGEON.getQuestTurnHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
			plotTextBox(g, colPtr3, rowPtr3, Globals.CURR_DUNGEON.getQuestInfoWid(), Globals.CURR_DUNGEON.getQuestInfoHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), getTextMessage("questScreenTitle"), -1, 1);
		if(Globals.CURR_DUNGEON.getQuestCount() > 0)
		{
			int countr = 0;
			for(int i = 0; i < Globals.CURR_DUNGEON.getQuestCount(); i++)
			{
				Quest quest = (Quest)(Globals.CURR_DUNGEON.getQuest(i));
				if(countr == currMenuIndex)
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_ACTV), "> ", colPtr1, rowPtr1 + countr, Globals.MAX_NAME_LENGTH, false);
				}
				plotString(g, Globals.QUEST_INDIC_COLORS[quest.getStatus()], quest.getName(), colPtr1 + 2, rowPtr1 + countr, (Color)null, Globals.MAX_FILENAME_LENGTH, false);
				String sQuestTime = (((quest.getTurns() - party.getTurnsElapsed()) - (party.getRoundsElapsed() > 0 ? 1 : 0) < 0) ? "X.X" : ((quest.getTurns() - party.getTurnsElapsed()) - (party.getRoundsElapsed() > 0 ? 1 : 0)) + "." + (party.getRoundsElapsed() == 0 ? 0 : 10 - party.getRoundsElapsed()));
				plotString(g, Globals.QUEST_INDIC_COLORS[quest.getStatus()], sQuestTime, colPtr2 + (Globals.CURR_DUNGEON.getQuestTurnWid() - sQuestTime.length()), rowPtr2 + countr, (Color)null, 5, false);
				if(countr == currMenuIndex)
				{
					plotLongString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), quest.getDescription(), colPtr3, rowPtr3, (Color)null, Globals.CURR_DUNGEON.getQuestInfoWid());
					plotString(g, Globals.QUEST_INDIC_COLORS[quest.getStatus()], Globals.getDungeonText("quest" + quest.getStatus()), colPtr3, Globals.CURR_DUNGEON.getQuestInfoRow() + Globals.CURR_DUNGEON.getQuestInfoHgt() - 1);
				}
				countr++;
			}
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showAskScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int colPtr  = Globals.CURR_DUNGEON.getItemSourceCol();
		int rowPtr  = Globals.CURR_DUNGEON.getItemSourceRow();
		int colPtr2 = Globals.CURR_DUNGEON.getItemTargetCol();
		int rowPtr2 = Globals.CURR_DUNGEON.getItemTargetRow();
		if(Globals.CURR_DUNGEON.getImageBackItemManage() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackItemManage(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr, rowPtr, Globals.CURR_DUNGEON.getItemSourceWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemSourceHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
			plotTextBox(g, colPtr2, rowPtr2, Globals.CURR_DUNGEON.getItemTargetWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		g.drawImage(Globals.CURR_DUNGEON.getImageStatue(), (colPtr2 * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr2 * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
		Player playerPtr = party.getPlayer(currItemPlayer);
		g.drawImage(playerPtr.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), (colPtr * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE, this);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), playerPtr.getName(), colPtr, rowPtr + 2);
		rowPtr += 4;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_ACTV), "> ", colPtr, rowPtr + currItemIndex);
		for(int inv = 0; inv < playerPtr.getInventorySize(); inv++)
		{
			g.drawImage(playerPtr.getInventoryItem(inv).getClassIcon(), ((colPtr + 1) * Globals.TILESCALE) / Globals.FONTRATIO, rowPtr * Globals.TILESCALE, this);
			plotString(g, (playerPtr.getInventoryItem(inv).isIdentified() ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC) : (inv == currItemIndex ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HIGH) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU))), playerPtr.getInventoryItem(inv).getInventoryText(), colPtr + 4, rowPtr++);
		}
		if(whichMode == Globals.SUBMODE_ASK_INPUT_PAYMENT)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("offeringLabel") + currGoldOffering.toString(), colPtr2, rowPtr2 + Globals.ENLARGESCALE + 2);
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showNegotiationScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		int colPtr1 = Globals.CURR_DUNGEON.getItemSourceCol();
		int rowPtr1 = Globals.CURR_DUNGEON.getItemSourceRow();
		int colPtr2 = Globals.CURR_DUNGEON.getItemTargetCol();
		int rowPtr2 = Globals.CURR_DUNGEON.getItemTargetRow();
		if(Globals.CURR_DUNGEON.getImageBackItemManage() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackItemManage(), 0, 0, this);
		}
		else
		{
			plotTextBox(g, colPtr1, rowPtr1, Globals.CURR_DUNGEON.getItemTargetWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemTargetHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
			plotTextBox(g, colPtr2, rowPtr2, Globals.CURR_DUNGEON.getItemSourceWid() * Globals.FONTRATIO, Globals.CURR_DUNGEON.getItemSourceHgt(), Globals.CURR_DUNGEON.getColorTextBoxOutline(), Globals.CURR_DUNGEON.getColorTextBoxFill());
		}
		g.drawImage(currPlayer.getCompositeImage(Globals.RENDER_MODE_NORMAL, this), colPtr1 * Globals.HALFSCALE, rowPtr1 * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
		g.drawImage(currRoom.getMonster(0).getImageNormal(), colPtr2 * Globals.HALFSCALE, rowPtr2 * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("negotiationPartyLabel") + party.getCurrency(), colPtr1, rowPtr1 + Globals.ENLARGESCALE + 2);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("negotiationEnemyLabel") + currGoldOffering.toString(), colPtr2, rowPtr2 + Globals.ENLARGESCALE + 2);
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showEndingScreen(int endingType)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		String endingText = Globals.getDungeonText("gameOver");
		Image endingImage = (Image)null;
		if(currGameSubMode == Globals.SUBMODE_ENDING_COMPLETE_VICTORY)
		{
			endingText  = Globals.getDungeonText("endingVictory");
			endingImage = Globals.CURR_DUNGEON.getImageGameOverVictory();
		}
		else if(currGameSubMode == Globals.SUBMODE_ENDING_PARTIAL_VICTORY)
		{
			endingText  = Globals.getDungeonText("endingMixed");
			endingImage = Globals.CURR_DUNGEON.getImageGameOverPartial();
		}
		else if(currGameSubMode == Globals.SUBMODE_ENDING_FAILED)
		{
			endingText  = Globals.getDungeonText("endingFailed");
			endingImage = Globals.CURR_DUNGEON.getImageGameOverFailed();
		}
		else if(currGameSubMode == Globals.SUBMODE_ENDING_SLAIN)
		{
			endingText  = Globals.getDungeonText("endingSlain");
			endingImage = Globals.CURR_DUNGEON.getImageGameOverSlain();
		}
		if(endingImage != null)
		{
			g.drawImage(endingImage, 0, 0, this);
			setCurrentMessage(endingText);
		}
		else
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), endingText, -1, 10);
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showIntroduction(int introType)
	{
		if(introPlaying) { return; } else { introPlaying = true; }

		BufferedImage bufferIntro = new BufferedImage(Globals.SCRWIDTH, Globals.SCRHEIGHT, BufferedImage.TYPE_INT_RGB);
		while(bufferIntro == null)
		{
			try { Thread.sleep(50); } catch(InterruptedException ie) {}
		}
		bufferIntro.getGraphics().drawImage(introScrnB, 0, 0, this);

		int[][] mosaic = new int[6][8];
		for(int y = 0; y < mosaic.length; y++)
		{
			for(int x = 0; x < mosaic[y].length; x++)
			{
				mosaic[y][x] = 0;
			}
		}
		int mx = Globals.rnd.nextInt(mosaic[0].length);
		int my = Globals.rnd.nextInt(mosaic.length);
		int mc = mosaic.length * mosaic[0].length;

		bMusicIntro = bMusicOn;
		bMusicOn = true;

		if(introTimer == 0l && (songTheme == null || !(songTheme.isPlaying())))
		{
			introTimer = System.currentTimeMillis();
			songTheme = playMusic(songTheme, musicTheme, false, false);
		}

		boolean loadA = true;
		boolean loadB = false;
		boolean past0 = false;

		while(introPlaying)
		{
			lastValidKeyTime = 0l;
			Graphics g2 = bufferStrategy.getDrawGraphics();
			if(songTheme.getFramePos() == 0 && past0)
			{
				introPlaying = false;
			}
			else if(songTheme.getFramePos() <= 86000l)
			{
				g2.drawImage(introScrnA, 0, 0, this);
				past0 = true;
				while(songTheme.getFramePos() == 0) try { Thread.sleep(10); } catch(InterruptedException ie) {}
			}
			else if(songTheme.getFramePos() <= 400000l)
			{
				g2.drawImage(introScrnB, 0, 0, this);
				if(loadA)
				{
					introScrnA = ObjectParser.loadImage("intro_title.png");
					loadA = false;
					loadB = true;
				}
			}
			else if(songTheme.getFramePos() <= 970000l)
			{
				while(mc > 0 && mosaic[my][mx] == 1)
				{
					mx = Globals.rnd.nextInt(mosaic[0].length);
					my = Globals.rnd.nextInt(mosaic.length);
				}
				mc = Math.max(mc - 1, 0);
				if(mosaic[my][mx] == 0)
				{
					bufferIntro.getGraphics().drawImage(introScrnA, mx * 100, my * 100, (mx * 100) + 100, (my * 100) + 100, mx * 100, my * 100, (mx * 100) + 100, (my * 100) + 100, this);
					mosaic[my][mx] = 1;
				}
				g2.drawImage(bufferIntro, 0, 0, this);
				if(loadB)
				{
					introScrnB = ObjectParser.loadImage("intro_creditblocks.png");
					loadB = false;
					loadA = true;
				}
			}
			else if(songTheme.getFramePos() <= 1500000l)
			{
				g2.setColor(Color.black);
				g2.fillRect(0, 0, Globals.SCRWIDTH, Globals.SCRHEIGHT);
				g2.drawImage(introScrnB, 0, 172, 800, 428, 0, 0, 800, 256, this);
				if(loadA)
				{
					introScrnA = ObjectParser.loadImage("intro_map.png");
					loadA = false;
				}
			}
			else if(songTheme.getFramePos() <= 1880000l)
			{
				g2.drawImage(introScrnA, 0, 0, this);
				loadA = true;
			}
			else if(songTheme.getFramePos() <= 2400000l)
			{
				g2.setColor(Color.black);
				g2.fillRect(0, 0, Globals.SCRWIDTH, Globals.SCRHEIGHT);
				g2.drawImage(introScrnB, 0, 172, 800, 428, 0, 256, 800, 512, this);
				if(loadA)
				{
					introScrnA = ObjectParser.loadImage("intro_treasure.png");
					loadA = false;
					loadB = true;
				}
			}
			else if(songTheme.getFramePos() <= 3080000l)
			{
				g2.drawImage(introScrnA, 0, 0, this);
				loadA = true;
			}
			else if(songTheme.getFramePos() <= 3620000l)
			{
				g2.setColor(Color.black);
				g2.fillRect(0, 0, Globals.SCRWIDTH, Globals.SCRHEIGHT);
				g2.drawImage(introScrnB, 0, 172, 800, 428, 0, 512, 800, 768, this);
				if(loadA)
				{
					introScrnA = ObjectParser.loadImage("intro_encounter.png");
					loadA = false;
					loadB = true;
				}
			}
			else if(songTheme.getFramePos() <= 4500000l)
			{
				g2.drawImage(introScrnA, 0, 0, this);
				loadA = true;
			}
			else if(songTheme.getFramePos() <= 5380000l)
			{
				g2.setColor(Color.black);
				g2.fillRect(0, 0, Globals.SCRWIDTH, Globals.SCRHEIGHT);
				g2.drawImage(introScrnB, 0, 172, 800, 428, 0, 768, 800, 1024, this);
				if(loadA)
				{
					introScrnA = ObjectParser.loadImage("intro_confrontation.png");
					loadA = false;
					loadB = true;
				}
			}
			else if(songTheme.getFramePos() <= 6250000l)
			{
				g2.drawImage(introScrnA, 0, 0, this);
				loadA = true;
			}
			else
			{
				g2.setColor(Color.black);
				g2.fillRect(0, 0, Globals.SCRWIDTH, Globals.SCRHEIGHT);
				g2.drawImage(introScrnB, 0, 172, 800, 428, 0, 1024, 800, 1280, this);
			}
			bufferStrategy.show();
			try { Thread.sleep(50); } catch(InterruptedException ie) {}
			g2.dispose();
		}

		stopMusic(songTheme);
		bMusicOn = bMusicIntro;
		songBkgrnd = playMusic(songBkgrnd, musicMenus, true, false);
		getPrevGameMode();
	}

	public void showShoppingScreen(int whichScreen)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		if(Globals.CURR_DUNGEON.getImageBackOrder() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackShop(), 0, 0, this);
		}
		g.drawImage(party.getPlayer(currShopPlayer).getCompositeImage(Globals.RENDER_MODE_NORMAL, this), Globals.TILESCALE * 2, Globals.TILESCALE * 6, Globals.ENLARGESCALE * Globals.TILESCALE, Globals.ENLARGESCALE * Globals.TILESCALE, this);
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shoppingForPlayer") + party.getPlayer(currShopPlayer).getName(), -1, 3);
		int rowPtr = Globals.CURR_DUNGEON.getShoppingRow();
		if(whichScreen == Globals.SUBMODE_SHOP_MAIN)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopWelcome"),             -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "1) " + Globals.getDungeonText("shopWeapons"),      Globals.CURR_DUNGEON.getShoppingCol(), rowPtr); rowPtr++;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "2) " + Globals.getDungeonText("shopRanged"),       Globals.CURR_DUNGEON.getShoppingCol(), rowPtr); rowPtr++;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "3) " + Globals.getDungeonText("shopBodyArmor"),    Globals.CURR_DUNGEON.getShoppingCol(), rowPtr); rowPtr++;
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "4) " + Globals.getDungeonText("shopSpecialArmor"), Globals.CURR_DUNGEON.getShoppingCol(), rowPtr); rowPtr++; rowPtr++;
			plotString(g, (getLevelCost(Globals.SHOP_HEALING_COST) > party.getCurrency() ?  Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_WARN) : (party.getPlayer(currShopPlayer).getWounds() > 0 ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC))), "5) " + Globals.getDungeonText("shopHealing") + " " + getLevelCost(Globals.SHOP_HEALING_COST) + Globals.getDungeonText("currencySymbol") + "  [" + party.getPlayer(currShopPlayer).getCurrentHitPoints() + "/" + party.getPlayer(currShopPlayer).getHitPoints() + "]", Globals.CURR_DUNGEON.getShoppingCol(), rowPtr); rowPtr++; rowPtr++;
			plotString(g, (getLevelCost(Globals.SHOP_RATIONS_COST) > party.getCurrency() ?  Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_WARN) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), "6) " + Globals.getDungeonText("shopRations") + " " + Globals.SHOP_RATIONS_QUANTITY + "/" + getLevelCost(Globals.SHOP_RATIONS_COST) + Globals.getDungeonText("currencySymbol") + "  [" + party.getRations() + "]", Globals.CURR_DUNGEON.getShoppingCol(), rowPtr);
		}
		else if(whichScreen == Globals.SUBMODE_SHOP_WEAPONS)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopWeapons"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopEquipt") + party.getPlayer(currShopPlayer).getEquiptWeapon().getInventoryText() + (party.getPlayer(currShopPlayer).getEquiptWeapon().requiresAmmo() ? " [" + party.getPlayer(currShopPlayer).getAmmoQuantityForWeapon(party.getPlayer(currShopPlayer).getEquiptWeaponIndex()) + "]" : "") + " / " + Globals.getDungeonText("shopAltern") + party.getPlayer(currShopPlayer).getAlternWeapon().getInventoryText() + (party.getPlayer(currShopPlayer).getAlternWeapon().requiresAmmo() ? " [" + party.getPlayer(currShopPlayer).getAmmoQuantityForWeapon(party.getPlayer(currShopPlayer).getAlternWeaponIndex()) + "]" : ""), -1, 5);
			for(int i = 0; i < vcShopList.size(); i++)
			{
				Weapon wpnTemp = (Weapon)(vcShopList.elementAt(i));
				if(wpnTemp != null)
				{
					plotString(g, (Globals.isPermitted(party.getPlayer(currShopPlayer).getCharacterClass(), wpnTemp.getPermissions()) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC)), ((i + 1) == 10 ? 0 : (i + 1)) + ") " + wpnTemp.getName(), Globals.CURR_DUNGEON.getShoppingCol(), 8 + i);
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "(" + wpnTemp.getBaseDmg() + ")", Globals.CURR_DUNGEON.getShoppingVal(), 8 + i, 4, true);
					plotString(g, (getLevelCost(wpnTemp.getCost()) > party.getCurrency() ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_WARN) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), getLevelCost(wpnTemp.getCost()) + Globals.getDungeonText("currencySymbol"), Globals.CURR_DUNGEON.getShoppingCst(), 8 + i, 6, true);
				}
			}
		}
		else if(whichScreen == Globals.SUBMODE_SHOP_RANGED)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopRanged"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopEquipt") + party.getPlayer(currShopPlayer).getEquiptWeapon().getInventoryText() + (party.getPlayer(currShopPlayer).getEquiptWeapon().requiresAmmo() ? " [" + party.getPlayer(currShopPlayer).getAmmoQuantityForWeapon(party.getPlayer(currShopPlayer).getEquiptWeaponIndex()) + "]" : "") + " / " + Globals.getDungeonText("shopAltern") + party.getPlayer(currShopPlayer).getAlternWeapon().getInventoryText() + (party.getPlayer(currShopPlayer).getAlternWeapon().requiresAmmo() ? " [" + party.getPlayer(currShopPlayer).getAmmoQuantityForWeapon(party.getPlayer(currShopPlayer).getAlternWeaponIndex()) + "]" : ""), -1, 5);
			for(int i = 0; i < vcShopList.size(); i = i + 2)
			{
				Weapon wpnTemp = (Weapon)(vcShopList.elementAt(i));
				if(wpnTemp != null)
				{
					plotString(g, (Globals.isPermitted(party.getPlayer(currShopPlayer).getCharacterClass(), wpnTemp.getPermissions()) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC)), ((i + 1) == 10 ? 0 : (i + 1)) + ") " + wpnTemp.getName(), Globals.CURR_DUNGEON.getShoppingCol(), 8 + i);
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "(" + wpnTemp.getBaseDmg() + ")", Globals.CURR_DUNGEON.getShoppingVal(), 8 + i, 4, true);
					plotString(g, (getLevelCost(wpnTemp.getCost()) > party.getCurrency() ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_WARN) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), getLevelCost(wpnTemp.getCost()) + Globals.getDungeonText("currencySymbol"), Globals.CURR_DUNGEON.getShoppingCst(), 8 + i, 6, true);
				}
				Ammo ammoTemp = (Ammo)(vcShopList.elementAt(i + 1));
				if(ammoTemp != null && getLevelCost(ammoTemp.getCost()) > 0)
				{
					plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (i + 2) + ")   " + ammoTemp.getName(), Globals.CURR_DUNGEON.getShoppingCol(), 9 + i);
					plotString(g, (getLevelCost(ammoTemp.getCost()) > party.getCurrency() ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_WARN) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), ammoTemp.getQuantity() + "/" + getLevelCost(ammoTemp.getCost()) + Globals.getDungeonText("currencySymbol"), Globals.CURR_DUNGEON.getShoppingCst(), 9 + i, 9, true);
				}
			}
		}
		else if(whichScreen == Globals.SUBMODE_SHOP_BODYARM)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopBodyArmor"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopEquipt") + party.getPlayer(currShopPlayer).getArmorSlotBody().getInventoryText(), -1, 5);
			for(int i = 0; i < vcShopList.size(); i++)
			{
				Armor armTemp = (Armor)(vcShopList.elementAt(i));
				plotString(g, (Globals.isPermitted(party.getPlayer(currShopPlayer).getCharacterClass(), armTemp.getPermissions()) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC)), ((i + 1) == 10 ? 0 : (i + 1)) + ") " + armTemp.getName(), Globals.CURR_DUNGEON.getShoppingCol(), 8 + i);
				plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "(" + armTemp.getBaseProt() + ")", Globals.CURR_DUNGEON.getShoppingVal(), 8 + i, 4, true);
				plotString(g, (getLevelCost(armTemp.getCost()) > party.getCurrency() ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_WARN) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), getLevelCost(armTemp.getCost()) + Globals.getDungeonText("currencySymbol"), Globals.CURR_DUNGEON.getShoppingCst(), 8 + i, 6, true);
			}
		}
		else if(whichScreen == Globals.SUBMODE_SHOP_SPECARM)
		{
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopSpecialArmor"), -1, 1);
			plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("shopEquipt") + party.getPlayer(currShopPlayer).getArmorSlotSpec().getInventoryText(), -1, 5);
			for(int i = 0; i < vcShopList.size(); i++)
			{
				Armor armTemp = (Armor)(vcShopList.elementAt(i));
				plotString(g, (Globals.isPermitted(party.getPlayer(currShopPlayer).getCharacterClass(), armTemp.getPermissions()) ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_INAC)), ((i + 1) == 10 ? 0 : (i + 1)) + ") " + armTemp.getName(), Globals.CURR_DUNGEON.getShoppingCol(), 8 + i);
				plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), "(" + armTemp.getBaseProt() + ")", Globals.CURR_DUNGEON.getShoppingVal(), 8 + i, 4, true);
				plotString(g, (getLevelCost(armTemp.getCost()) > party.getCurrency() ? Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_WARN) : Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU)), getLevelCost(armTemp.getCost()) + Globals.getDungeonText("currencySymbol"), Globals.CURR_DUNGEON.getShoppingCst(), 8 + i, 6, true);
			}
		}
		rowPtr = Globals.CURR_DUNGEON.getShoppingRow() + Globals.CURR_DUNGEON.getShoppingHgt() + 4;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText("currencyName") + " " + Globals.getDungeonText("shopCurrencyLeft") + party.getCurrency() + Globals.getDungeonText("currencySymbol"), -1, rowPtr); rowPtr++; rowPtr++;
		plotString(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), Globals.getDungeonText((whichScreen == Globals.SUBMODE_SHOP_MAIN ? "shopExitStore" : "shopMenuBack")), -1, rowPtr);
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showPartyFormationScreen(int whichMode)
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		if(Globals.CURR_DUNGEON.getImageBackOrder() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackOrder(), 0, 0, this);
		}
		for(int y = 0; y < party.getSize() / 2; y++)
		{
			for(int x = 0; x < 2; x++)
			{
				int cntr = (y * 2) + x;
				int offsetX = ((Globals.GRIDWIDTH / 2) - 4) + (x * 6);
				int offsetY = ((Globals.GRIDHEIGHT / 2) - 4) + (y * 6);
				if(cntr == currPlayerPtr)
				{
					if(Globals.CURR_DUNGEON.getImageBackOrderCursor() != null)
					{
						g.drawImage(Globals.CURR_DUNGEON.getImageBackOrderCursor(), (offsetX * Globals.TILESCALE) - ((Globals.CURR_DUNGEON.getImageBackOrderCursor().getWidth(this) / 2) - Globals.TILESCALE), (offsetY * Globals.TILESCALE) - ((Globals.CURR_DUNGEON.getImageBackOrderCursor().getHeight(this) / 2) - Globals.TILESCALE), this);
					}
					else
					{
						g.setColor(Globals.CURR_DUNGEON.getColorActivePartyMember());
						g.fillRect(offsetX * Globals.TILESCALE, offsetY * Globals.TILESCALE, Globals.SPRITESCALE, Globals.SPRITESCALE);
					}
				}
				g.drawImage(party.getPlayer(cntr).getCompositeImage(Globals.RENDER_MODE_NORMAL, this), offsetX * Globals.TILESCALE, offsetY * Globals.TILESCALE, this);
				plotLabel(g, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), (cntr + 1) + "", offsetX, offsetY + 2, 2, 1);
			}
		}
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		g.dispose();
		g2.dispose();
	}

	public void showVaultScreen()
	{
		Graphics g = bufferEnviron.getGraphics();
		Graphics g2 = bufferStrategy.getDrawGraphics();
		if(Globals.CURR_DUNGEON.getImageBackVault() != null)
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageBackVault(), 0, 0, this);
		}
		g.drawImage(party.getPlayer(currItemPlayer).getCompositeImage(Globals.RENDER_MODE_NORMAL, this), Globals.CURR_DUNGEON.getVaultCharX() * Globals.TILESCALE, Globals.CURR_DUNGEON.getVaultCharY() * Globals.TILESCALE, this);
		int counter = 0;
		for(int vg = vcVaultGuesses.size() - 1; vg >= 0; vg--)
		{
			plotVaultSeq(vcVaultGuesses.elementAt(vg), 23, (counter * 3) + 1, Globals.VAULT_DIGITS_PER_LEVEL[party.getDepth()], currRoom.getVault().getCode());
			counter++;
		}
		plotVaultSeq(sbVaultGuess.toString(), 8, 13, Globals.VAULT_DIGITS_PER_LEVEL[party.getDepth()]);
		plotOutlineText(g, party.getPlayer(currItemPlayer).getCurrentHitPoints() + "", Globals.CURR_DUNGEON.getVaultHPsX(), Globals.CURR_DUNGEON.getVaultHPsY(), 2, 1, (party.getPlayer(currItemPlayer).getCurrentHitPoints() == party.getPlayer(currItemPlayer).getHitPoints() ? Globals.CURR_DUNGEON.getColorHitpointsFull() : (party.getPlayer(currItemPlayer).getCurrentHitPoints() <= 0 ? Globals.CURR_DUNGEON.getColorHitpointsNone() : (party.getPlayer(currItemPlayer).getCurrentHitPoints() < (party.getPlayer(currItemPlayer).getHitPoints() / 3) ? Globals.CURR_DUNGEON.getColorHitpointsCrit() : Globals.CURR_DUNGEON.getColorHitpointsSome()))), Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_MENU), false);
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		String sMessage = Globals.getDungeonText("vaultGuessRange");
		sMessage = substituteInMessage(sMessage, "$MAX", Globals.VAULT_VALUES_PER_LEVEL[party.getDepth()]);
		setCurrentMessage(sMessage);
		g.dispose();
		g2.dispose();
	}

	public void plotVaultSeq(String seq, int x, int y, int len, int code)
	{
		Graphics g = bufferEnviron.getGraphics();
		for(int i = 0; i < Globals.MAX_VAULT_CODE_LEN; i++)
		{
			int val = 0;
			if(i < seq.length())
			{
				val = Integer.parseInt(seq.substring(i, i + 1));
			}
			else if(i >= len)
			{
				val = 10;
			}
			g.drawImage(Globals.CURR_DUNGEON.getImageVaultDigits(), (x + (i * 2)) * Globals.TILESCALE, y * Globals.TILESCALE, (x + (i * 2) + 2) * Globals.TILESCALE, (y + 2) * Globals.TILESCALE, val * (Globals.TILESCALE * 2), 0, (val + 1) * (Globals.TILESCALE * 2), Globals.TILESCALE * 2, this);
		}
		if(code > -1)
		{
			boolean isLower = true;
			int seqval = Integer.parseInt(seq);
			if(seqval > code) { isLower = false; }
			int digitMatches = getCodeMatches(seq, code + "");
			g.drawImage(Globals.CURR_DUNGEON.getImageVaultDigits(), (x + 11) * Globals.TILESCALE, y * Globals.TILESCALE, (x + 13) * Globals.TILESCALE, (y + 2) * Globals.TILESCALE, digitMatches * (Globals.TILESCALE * 2), Globals.TILESCALE * 2, (digitMatches + 1) * (Globals.TILESCALE * 2), Globals.TILESCALE * 4, this);
			g.drawImage(Globals.CURR_DUNGEON.getImageVaultDigits(), (x + 13) * Globals.TILESCALE, y * Globals.TILESCALE, (x + 15) * Globals.TILESCALE, (y + 2) * Globals.TILESCALE, (isLower ? 11 : 10) * (Globals.TILESCALE * 2), Globals.TILESCALE * 2, (isLower ? 12 : 11) * (Globals.TILESCALE * 2), Globals.TILESCALE * 4, this);
		}
		g.dispose();
	}

	public void plotVaultSeq(String seq, int x, int y, int len)
	{
		plotVaultSeq(seq, x, y, len, -1);
	}

	public int getCodeMatches(String code, String guess)
	{
		int digitMatches = 0;
		for(int d = 0; d < code.length(); d++)
		{
			if(code.charAt(d) == guess.charAt(d))
			{
				digitMatches++;
			}
		}
		return digitMatches;
	}

	public void displayHelp()
	{
		if(currGameMode == Globals.MODE_MENU) { return; }
		String[] messageLines = new String[Globals.MAX_MESSAGES];
		int messageRow = 0;
		messageLines[messageRow] = Globals.getDungeonText("helpMsgInfoPlayer") + "  " + Globals.getDungeonText("helpMsgInfoParty") + "  " + (currRoom != null && currRoom.hasActiveMonsters() ? Globals.getDungeonText("helpMsgInfoMonsters") + "  " : "") + Globals.getDungeonText("helpMsgInfoQuests");
		messageRow++;
		if(currRoom != null && currRoom.hasActiveMonsters())
		{
			messageLines[messageRow] = Globals.getDungeonText("helpMsgCombat");
			messageRow++;
		}
		else
		{
			messageLines[messageRow] = Globals.getDungeonText("helpMsgUse") + "  " + (party.getSize() > 1 ? Globals.getDungeonText("helpMsgTrade") + "  " + Globals.getDungeonText("helpMsgQuiver") + "  " : "") + (currRoom != null ? Globals.getDungeonText("helpMsgDrop") + "  " : "") + Globals.getDungeonText("helpMsgMap") + (party.getSize() > 1 ? "  " + Globals.getDungeonText("helpMsgOrder") : "");
			messageRow++;
		}
		if(currRoom != null)
		{
			if(currRoom.hasActiveMonsters())
			{
				messageLines[messageRow] = Globals.getDungeonText("helpMsgNegotiate");
				messageRow++;
			}
			else
			{
				if(currRoom.hasChest())
				{
					messageLines[messageRow] = Globals.getDungeonText("helpMsgChest") + (currRoom.getItemCount() > 0 ? "  " + Globals.getDungeonText("helpMsgGet") : "");
					messageRow++;
				}
				else if(currRoom.hasVault())
				{
					messageLines[messageRow] = Globals.getDungeonText("helpMsgVault") + (currRoom.getItemCount() > 0 ? "  " + Globals.getDungeonText("helpMsgGet") : "");
					messageRow++;
				}
				else if(currRoom.getItemCount() > 0)
				{
					messageLines[messageRow] = Globals.getDungeonText("helpMsgGet");
					messageRow++;
				}
				if(currRoom.getFeatureType() != Globals.FEATURE_NONE)
				{
					if(currRoom.getFeatureType() == Globals.FEATURE_FOUNTAIN)
					{
						messageLines[messageRow] = Globals.getDungeonText("helpMsgFountain");
						messageRow++;
					}
					else if(currRoom.getFeatureType() == Globals.FEATURE_STATUE)
					{
						messageLines[messageRow] = Globals.getDungeonText("helpMsgAsk");
						messageRow++;
					}
					else if(currRoom.getFeatureType() == Globals.FEATURE_SHOP)
					{
						messageLines[messageRow] = Globals.getDungeonText("helpMsgPurchase");
						messageRow++;
					}
				}
				if(currRoom.getStairType() != Globals.STAIRS_NONE)
				{
					if(currRoom.getStairType() == Globals.STAIRS_UP)
					{
						if(messageRow >= Globals.MAX_MESSAGES)
						{
							messageLines[Globals.MAX_MESSAGES - 1] = messageLines[Globals.MAX_MESSAGES - 1] + "  " + Globals.getDungeonText("helpMsgStairsUp");
						}
						else
						{
							messageLines[messageRow] = Globals.getDungeonText("helpMsgStairsUp");
							messageRow++;
						}
					}
					else if(currRoom.getStairType() == Globals.STAIRS_DOWN)
					{
						if(messageRow >= Globals.MAX_MESSAGES)
						{
							messageLines[Globals.MAX_MESSAGES - 1] = messageLines[Globals.MAX_MESSAGES - 1] + "  " + Globals.getDungeonText("helpMsgStairsDown");
						}
						else
						{
							messageLines[messageRow] = Globals.getDungeonText("helpMsgStairsDown");
							messageRow++;
						}
					}
				}
			}
		}
		else if(currBoard.hasFountain(party.getLocationX(), party.getLocationY()))
		{
			messageLines[messageRow] = Globals.getDungeonText("helpMsgFountain");
			messageRow++;
		}
		while(messageRow < Globals.MAX_MESSAGES)
		{
			messageLines[messageRow] = "";
			messageRow++;
		}
		setCurrentMessage(messageLines);
	}

//  Audio Methods ------------------------------------------------------------/

	private void playSound(MP3Clip sound)
	{
		if(bSoundOn && sound != null)
		{
			sound.setVolume((volSound - 50) / 5.0f);
			sound.startPlaying();
		}
	}

	private MP3Streamer playMusic(MP3Streamer stream, String mp3name, boolean loop, boolean ismodulesong)
	{
		if(DEBUG) { System.out.println("playMusic(" + mp3name + ")"); }
		if(mp3name == null || mp3name.equals(Globals.NO_MUSIC)) { return (MP3Streamer)null; }
		if(bMusicOn)
		{
			String fullName = (ismodulesong ? ObjectParser.getModulePath() : Globals.RSRCPATH) + "music" + File.separator + mp3name;
			if(stream != null && stream.isPlaying())
			{
				if(stream.getAudioFileName().equals(fullName))
				{
					// already queued and playing, leave it alone
					return stream;
				}
				else
				{
					stream.stopPlaying();
					stream = null;
				}
			}
			stream = new MP3Streamer();
			stream.queueAudioFile(fullName);
			stream.setVolume((volMusic - 75) / 5.0f);
			while(!(stream.isReady()) && stream.isOkay())
			{
				try { Thread.sleep(100); } catch(InterruptedException ie) { }
			}
			if(stream.isOkay())
			{
				stream.setLooping(loop);
				stream.startPlaying();
			}
			return stream;
		}
		return (MP3Streamer)null;
	}

	private MP3Streamer playContextMusic()
	{
		stopMusic(songBkgrnd);
		if(gameInProgress)
		{
			return playMusic(songBkgrnd, Globals.CURR_DUNGEON.getBackgroundMusic(party.getDepth()), true, true);
		}
		else
		{
			return playMusic(songBkgrnd, musicMenus, true, false);
		}
	}

	private void adjustMusic(MP3Streamer stream, float volume)
	{
		if(stream != null)
		{
			stream.setVolume((volume - 75) / 5.0f);
		}
	}

	private void stopMusic(MP3Streamer stream)
	{
		if(stream != null)
		{
			stream.stopPlaying();
			stream = (MP3Streamer)null;
		}
	}

	private void pauseMusic(MP3Streamer stream)
	{
		if(stream != null)
		{
			stream.pausePlaying();
		}
	}

	private void resumeMusic(MP3Streamer stream)
	{
		if(stream != null)
		{
			stream.resumePlaying();
		}
	}

//  Transaction Methods ------------------------------------------------------/

	private void updateShoppingList(int whichKind)
	{
		vcShopList.removeAllElements();
		if(whichKind == Globals.SUBMODE_SHOP_WEAPONS || whichKind == Globals.SUBMODE_SHOP_RANGED)
		{
			// build buyable weapons list
			for(int i = 0; i < Globals.INDEX_ARSENAL.size(); i++)
			{
				Weapon wpnTemp = Globals.ARSENAL.get(Globals.INDEX_ARSENAL.elementAt(i));
				// hand weapons
				if(whichKind == Globals.SUBMODE_SHOP_WEAPONS && !wpnTemp.isRanged())
				{
					if(wpnTemp.getCost() != Globals.NOT_FOR_SALE && wpnTemp.getCost() <= (Globals.SHOP_CAPS_WPNS[party.getDepth()] + party.getShopCapBonus()))
					{
						// add to list
						vcShopList.add(wpnTemp);
					}
				}
				// ranged weapons
				else if(whichKind == Globals.SUBMODE_SHOP_RANGED && wpnTemp.isRanged())
				{
					if(wpnTemp.getCost() != Globals.NOT_FOR_SALE && wpnTemp.getCost() <= (Globals.SHOP_CAPS_WPNS[party.getDepth()] + party.getShopCapBonus()))
					{
						vcShopList.add(wpnTemp);
						if(wpnTemp.getAmmoType() != Globals.AMMO_NOT_REQUIRED)
						{
							vcShopList.add(Globals.AMMOLIST.elementAt(wpnTemp.getAmmoType()));
						}
						else
						{
							vcShopList.add((BaseItem)null);
						}
					}
					else if(wpnTemp.getAmmoType() != Globals.AMMO_NOT_REQUIRED)
					{
						if(!(vcShopList.contains(Globals.AMMOLIST.elementAt(wpnTemp.getAmmoType()))))
						{
							vcShopList.add((Weapon)null);
							vcShopList.add(Globals.AMMOLIST.elementAt(wpnTemp.getAmmoType()));
						}
					}
				}
			}
		}
		else if(whichKind == Globals.SUBMODE_SHOP_BODYARM || whichKind == Globals.SUBMODE_SHOP_SPECARM)
		{
			// build buyable armor list
			for(Enumeration e = Globals.ARMOURY.elements(); e.hasMoreElements();)
			{
				Armor armTemp = (Armor)(e.nextElement());
				if(armTemp.getCost() <= Globals.SHOP_CAPS_ARMR[party.getDepth()])
				{
					if(whichKind == Globals.SUBMODE_SHOP_BODYARM && armTemp.isBodyArmor())
					{
						if(armTemp.getCost() != Globals.NOT_FOR_SALE)
						{
							// add to list, sorted by price
							boolean inserted = false;
							for(int i = 0; i < vcShopList.size(); i++)
							{
								if(armTemp.getCost() < ((Armor)(vcShopList.elementAt(i))).getCost())
								{
									vcShopList.insertElementAt(armTemp, i);
									inserted = true;
									i = vcShopList.size();
									break;
								}
							}
							if(!inserted)
							{
								vcShopList.add(armTemp);
							}
						}
					}
					else if(whichKind == Globals.SUBMODE_SHOP_SPECARM && !armTemp.isBodyArmor())
					{
						if(armTemp.getCost() != Globals.NOT_FOR_SALE)
						{
							// add to list, sorted by price
							boolean inserted = false;
							for(int i = 0; i < vcShopList.size(); i++)
							{
								if(armTemp.getCost() < ((Armor)(vcShopList.elementAt(i))).getCost())
								{
									vcShopList.insertElementAt(armTemp, i);
									inserted = true;
									i = vcShopList.size();
									break;
								}
							}
							if(!inserted)
							{
								vcShopList.add(armTemp);
							}
						}
					}
				}
			}
		}
	}

	private void purchaseItem(BaseItem item)
	{
		if(Globals.isPermitted(party.getPlayer(currShopPlayer).getCharacterClass(), item.getPermissions()))
		{
			if(party.getCurrency() >= getLevelCost(item.getCost()))
			{
				if(item.getType() == Globals.ITEMTYPE_WEAPON)
				{
					if(party.getPlayer(currShopPlayer).getEquiptWeapon().equals(Globals.WEAPON_DEFAULT) || party.getPlayer(currShopPlayer).getAlternWeapon().equals(Globals.WEAPON_DEFAULT))
					{
						party.spendCurrency(getLevelCost(item.getCost()));
						party.getPlayer(currShopPlayer).equipWeapon((Weapon)item);
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("equipAllSpacesUsed"));
					}
				}
				else if(item.getType() == Globals.ITEMTYPE_ARMOR)
				{
					Armor armReturn = party.getPlayer(currShopPlayer).equipArmor((Armor)item);
					if(
						armReturn != null &&
						((((Armor)item).isBodyArmor() && !(armReturn.equals(Globals.ARMOR_BODY_DEFAULT))) ||
							(!((Armor)item).isBodyArmor() && !(armReturn.equals(Globals.ARMOR_SPEC_DEFAULT))))
					)
					{
						setCurrentMessage(Globals.getDungeonText("equipAllSpacesUsed"));
						party.getPlayer(currShopPlayer).equipArmor(armReturn);
					}
					else
					{
						party.spendCurrency(getLevelCost(item.getCost()));
					}
				}
			}
			else
			{
				setCurrentMessage(Globals.getDungeonText("shopInsufficientCurrency"));
			}
		}
		else
		{
			String sMessage = Globals.getDungeonText("itemNotPermitted");
			sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currShopPlayer).getName());
			sMessage = substituteInMessage(sMessage, "$ITEM", item.getName());
			setCurrentMessage(sMessage);
		}
	}

	private void purchaseAmmo(Ammo ammo)
	{
		if(ammo.getCost() > 0)
		{
			if(party.getPlayer(currShopPlayer).getAmmoQuantity(ammo.getNumber()) < Globals.MAX_AMMO)
			{
				if(party.spendCurrency(getLevelCost(ammo.getCost())))
				{
					party.getPlayer(currShopPlayer).alterAmmoLevel(ammo.getNumber(), ammo.getQuantity());
				}
				else
				{
					setCurrentMessage(Globals.getDungeonText("shopInsufficientCurrency"));
				}
			}
			else
			{
				String sMessage = Globals.getDungeonText("ammoIsFull");
				sMessage = substituteInMessage(sMessage, "$PLAYER", party.getPlayer(currShopPlayer).getName());
				setCurrentMessage(sMessage);
			}
		}
		else
		{
			setCurrentMessage(Globals.getDungeonText("shopAmmoNotForSale"));
		}
	}

	private int getLevelCost(int baseCost)
	{
		return (baseCost * Math.max(party.getDepth(), 1));
	}

	public void playerGetRoomItem(Player player, BaseItem item)
	{
		if(item == null)
		{
			setCurrentMessage(Globals.getDungeonText("noItemToPickUp"));
			getPrevGameMode();
			return;
		}
		if(item instanceof Item && ((Item)item).isImmediate())
		{
			getPrevGameMode();
			updateScreen();
			processEffect(((Item)item).getEffect(), player);
			currRoom.loseItem(item);
			if(currItemIndex >= currRoom.getItemCount())
			{
				currItemIndex = currRoom.getItemCount() - 1;
			}
		}
		else
		{
			currTransact = new Transaction(currRoom, player, item);
			currTransact.performTransaction();
			if(currTransact.isSuccess())
			{
				String[] sMessage = new String[3];
				sMessage[0] = Globals.getDungeonText("obtainedItem");
				sMessage[0] = substituteInMessage(sMessage[0], "$PLAYER", player.getName());
				sMessage[0] = substituteInMessage(sMessage[0], "$ITEM", item.getInventoryText());
				item = currTransact.getReturnItem();
				if(item != null)
				{
					sMessage[1] = Globals.getDungeonText("dropPerformed");
					sMessage[1] = substituteInMessage(sMessage[1], "$PLAYER", player.getName());
					sMessage[1] = substituteInMessage(sMessage[1], "$ITEM", item.getInventoryText());
					item = currRoom.gainItem(item);
					if(item != null)
					{
						sMessage[2] = Globals.getDungeonText("destroyAutomatic");
						sMessage[2] = substituteInMessage(sMessage[2], "$ITEM", item.getInventoryText());
						item = (BaseItem)null;
					}
				}
				setCurrentMessage(sMessage);
				currTransact = (Transaction)null;
				currItemIndex = 0;
				return;
			}
			else
			{
				currRoom.gainItem(item);
				String sMessage = Globals.getDungeonText("itemNotAllowed");
				sMessage = substituteInMessage(sMessage, "$PLAYER", player.getName());
				sMessage = substituteInMessage(sMessage, "$ITEM", item.getInventoryText());
				setCurrentMessage(sMessage);
			}
			currTransact = (Transaction)null;
		}
	}

	public BaseItem getPlayerTransactionItem(Player plyr, int index)
	{
		BaseItem bitm = (BaseItem)null;
		if(index == 0)
		{
			bitm = plyr.getEquiptWeapon();
		}
		else if(index == 1)
		{
			bitm = plyr.getAlternWeapon();
		}
		else if(index == 2)
		{
			bitm = plyr.getArmorSlotBody();
		}
		else if(index == 3)
		{
			bitm = plyr.getArmorSlotSpec();
		}
		else if(index == -1)
		{
		}
		else if((index - 4) < plyr.getInventorySize())
		{
			bitm = plyr.getInventoryItem(index - 4);
		}
		return bitm;
	}

	public int getItemUsabilityFlag(Player plyr, BaseItem item)
	{
		if(item != null && item.getType() == Globals.ITEMTYPE_ITEM && !(((Item)item).isImmediate()) && plyr.getInventorySize() >= Globals.MAX_INVENTORY)
		{
			return Globals.ITEMSTATUS_INVENTORYFULL;
		}
		else if(item != null && item.getType() == Globals.ITEMTYPE_ITEM && !Globals.isPermitted(plyr.getCharacterClass(), item.getPermissions()))
		{
			return Globals.ITEMSTATUS_NOTUSABLE;
		}
		else if(item != null && !Globals.isPermitted(plyr.getCharacterClass(), item.getPermissions()))
		{
			return Globals.ITEMSTATUS_NOTALLOWED;
		}
		return Globals.ITEMSTATUS_OKAY;
	}

//  Effect Processing --------------------------------------------------------/

	public void processEffect(Effect fx, Lifeform target, Lifeform caster, boolean resistable)
	{
		if(fx.getProjectileType() != Globals.PROJECTILE_NONE && caster != null && target != null && target.isAlive())
		{
			showProjectile(fx.getProjectileType(), fx.getProjectileColor(), (caster.getLocation().getX() * Globals.TILESCALE) + Globals.TILESCALE + drawpointX, (caster.getLocation().getY() * Globals.TILESCALE) + Globals.TILESCALE + drawpointY, (target.getLocation().getX() * Globals.TILESCALE) + Globals.TILESCALE + drawpointX, (target.getLocation().getY() * Globals.TILESCALE) + Globals.TILESCALE + drawpointY);
		}

		int powerval = Globals.getScaledRandomValue(fx.getPower()) * (fx.isPositive() ? 1 : -1);
		boolean showPause = true;
		if(fx.getEffect().equals(Globals.FX_EFFECT_MAP))
		{
			currBoard.findMap();
			setCurrentMessage(Globals.getDungeonText("effectLevelMapped"));
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_CURRENCY))
		{
			int cAmount = fx.getPower() * (fx.isPositive() ? 1 : -1);
			if(caster != null)
			{
				cAmount = powerval;
			}
			party.adjustCurrency(cAmount);
			setCurrentMessage(Globals.getDungeonText(fx.isPositive() ? "" : ""));
			String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectCurrencyGained" : "effectCurrencyLost"));
			sMessage = substituteInMessage(sMessage, "$CURRENCY", Globals.ITEM_CURRENCY.getName());
			sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(cAmount));
			setCurrentMessage(sMessage);
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_RATIONS))
		{
			int cAmount = fx.getPower() * (fx.isPositive() ? 1 : -1);
			if(caster != null)
			{
				cAmount = powerval;
			}
			party.adjustRations(cAmount);
			setCurrentMessage(Globals.getDungeonText(fx.isPositive() ? "" : ""));
			String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectRationsGained" : "effectRationsLost"));
			sMessage = substituteInMessage(sMessage, "$RATION", Globals.ITEM_RATION.getName());
			sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(cAmount));
			setCurrentMessage(sMessage);
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_RATIONINTR))
		{
			party.adjustRationInterval(powerval);
			setCurrentMessage(Globals.getDungeonText(fx.isPositive() ? "effectRationIntervalIncreased" : "effectRationIntervalDecreased"));
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_HEALINGINTR))
		{
			party.adjustHealingInterval(powerval);
			setCurrentMessage(Globals.getDungeonText(fx.isPositive() ? "effectHealingIntervalIncreased" : "effectHealingIntervalDecreased"));
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_WANDERPROB))
		{
			party.adjustWanderingOdds(powerval);
			setCurrentMessage(Globals.getDungeonText(fx.isPositive() ? "effectWanderIncreased" : "effectWanderDecreased"));
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_WPNAVAIL))
		{
			party.setShopCapBonus(party.getShopCapBonus() + powerval);
			setCurrentMessage(Globals.getDungeonText(fx.isPositive() ? "effectWeaponAvailIncreased" : "effectWeaponAvailDecreased"));
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_SHOWTRAPS))
		{
			if(isLocationRoom(currBoard, party.getLocation()) && currRoom != null && currRoom.hasChest())
			{
				String sMessage = new String();
				if(currRoom.getChest().hasTrap())
				{
					sMessage = Globals.getDungeonText("chestShowTrap");
					sMessage = substituteInMessage(sMessage, "$EFFECT", currRoom.getChest().getTrap().getName());
				}
				else
				{
					sMessage = Globals.getDungeonText("chestShowNoTrap");
				}
				setCurrentMessage(sMessage);
			}
			else
			{
				setCurrentMessage(Globals.getDungeonText("noEffect"));
			}
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_REMOVETRAPS))
		{
			if(isLocationRoom(currBoard, party.getLocation()) && currRoom != null)
			{
				if(currRoom.hasChest())
				{
					currRoom.getChest().setTrapNum(Globals.TRAP_NONE);
					setCurrentMessage(Globals.getDungeonText("effectTrapRemoved"));
				}
			}
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_SHOWMONSTERS))
		{
			espMode = true;
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_IDENTIFY))
		{
			if(target instanceof Player)
			{
				for(int i = 0; i < ((Player)target).getInventorySize(); i++)
				{
					((Player)target).getInventoryItem(i).identify();
				}
				String sMessage = Globals.getDungeonText("effectPlayerIdentify");
				sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
				setCurrentMessage(sMessage);
				showMagicEffect(true, target.getLocation());
			}
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_UNIDENTIFY))
		{
			if(target instanceof Player)
			{
				if(resistable && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					for(int i = 0; i < ((Player)target).getInventorySize(); i++)
					{
						((Player)target).getInventoryItem(i).unidentify();
					}
					String sMessage = Globals.getDungeonText("effectPlayerUnidentify");
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					setCurrentMessage(sMessage);
					showMagicEffect(false, target.getLocation());
				}
			}
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_POLYMORPH))
		{
			if(target instanceof Player)
			{
				if(((Player)target).getInventorySize() < 1)
				{
					target = (Lifeform)null;
					for(int p = 0; p < party.getSize(); p++)
					{
						if(party.getPlayer(p).getInventorySize() > 0)
						{
							target = (Lifeform)(party.getPlayer(p));
							p = party.getSize();
						}
					}
				}
				if(target != null)
				{
					int invitem = rnd.nextInt(((Player)target).getInventorySize());
					boolean getBadItem  = fx.isNegative();
					if(fx.isNeutral()) { getBadItem = (rnd.nextInt(2) == 1); }
					boolean seekingItem = true;
					while(seekingItem)
					{
						Item iget = Globals.getCollectumItem(Globals.INDEX_COLLECTUM.elementAt(rnd.nextInt(Globals.INDEX_COLLECTUM.size())), -1);
						if(getBadItem == Globals.isBadEffect(iget.getEffect()) && !(iget.isImmediate()))
						{
							iget.unidentify();
							((Player)target).loseItem(((Player)target).getInventoryItem(invitem));
							((Player)target).gainItem(iget);
							seekingItem = false;
							String sMessage = Globals.getDungeonText("effectPlayerPolymorph");
							sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
							setCurrentMessage(sMessage);
							showMagicEffect(fx.isPositive(), target.getLocation());
							showPause = false;
						}
					}
				}
			}
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_LIFETRANSFER))
		{
			boolean revivedPlayer = false;
			if(target instanceof Player && !target.isAlive()) { revivedPlayer = true; }
			caster.adjustWounds(-powerval);
			target.adjustWounds(powerval);
			String sMessage = Globals.getDungeonText("effectLifeTransferred");
			sMessage = substituteInMessage(sMessage, "$CASTER", caster.getName());
			sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
			sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
			setCurrentMessage(sMessage);
			updateScreen();
			if(target instanceof Player && target.isAlive() && revivedPlayer && currRoom != null)
			{
				if(!isClearTile(target.getLocation(), target))
				{
					target.setLocation(getClearTile());
				}
			}
			playSound(Globals.CURR_DUNGEON.getEnvironSound(Globals.SOUND_MAGIC1));
			if(currRoom != null)
			{
				showDamageEffect(true, caster.getLocation(), powerval);
				showDamageEffect(false, target.getLocation(), powerval);
				showPause = false;
			}
			checkCombatConclusion();
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_LIFELEECH))
		{
			if(resistable && makeResistanceRoll(target))
			{
				String sMessage = Globals.getDungeonText("resistedEffect");
				sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
				sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
				setCurrentMessage(sMessage);
			}
			else
			{
				boolean revivedPlayer = false;
				if(caster instanceof Player && !caster.isAlive()) { revivedPlayer = true; }
				caster.adjustWounds(powerval);
				target.adjustWounds(-powerval);
				String sMessage = Globals.getDungeonText("effectLifeLeeched");
				sMessage = substituteInMessage(sMessage, "$CASTER", caster.getName());
				sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
				sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
				setCurrentMessage(sMessage);
				updateScreen();
				if(caster instanceof Player && caster.isAlive() && revivedPlayer && currRoom != null)
				{
					if(!isClearTile(caster.getLocation(), caster))
					{
						caster.setLocation(getClearTile());
					}
				}
				playSound(Globals.CURR_DUNGEON.getEnvironSound(Globals.SOUND_MAGIC2));
				if(currRoom != null)
				{
					showDamageEffect(true, target.getLocation(), powerval);
					showDamageEffect(false, caster.getLocation(), powerval);
					showPause = false;
				}
				checkCombatConclusion();
			}
		}
		else if(fx.getEffect().equals(Globals.FX_EFFECT_NONE))
		{
			return;
		}
		else if(target != null && target.getType() == Globals.LIFEFORM_PLAYER)
		{
			// Process a Player targetted effect
			if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERHPS))
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					boolean revivedPlayer = false;
					if(target instanceof Player && !target.isAlive()) { revivedPlayer = true; }
					target.adjustHitpoints(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerHPIncr" : "effectPlayerHPDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					if(target instanceof Player && target.isAlive() && revivedPlayer && currRoom != null)
					{
						if(!isClearTile(target.getLocation(), target))
						{
							target.setLocation(getClearTile());
						}
					}
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
					checkPartyStatus();
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERDMG))
			{
				if(resistable && fx.isPositive() && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					boolean revivedPlayer = false;
					if(target instanceof Player && !target.isAlive()) { revivedPlayer = true; }
					target.adjustWounds(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerDmgIncr" : "effectPlayerDmgDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					if(target instanceof Player && target.isAlive() && revivedPlayer && currRoom != null)
					{
						if(!isClearTile(target.getLocation(), target))
						{
							target.setLocation(getClearTile());
						}
					}
					playSound(Globals.CURR_DUNGEON.getEnvironSound((fx.isNegative() ? Globals.SOUND_MAGIC1 : Globals.SOUND_MAGIC2)));
					if(currRoom != null)
					{
						showDamageEffect(fx.isPositive(), target.getLocation(), powerval);
						showPause = false;
					}
					checkPartyStatus();
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYEREXP))
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Player)target).adjustExp(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerEXPIncr" : "effectPlayerEXPDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
					checkPartyStatus();
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERPROT))
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					String armName = (String)null;
					int rndArmor = rnd.nextInt(2); // randomly pick a 0 or 1
					for(int ac = 0; ac < 2; ac++)
					{
						if(armName == null)
						{
							if(ac == rndArmor && !(((Player)target).getArmorSlotBody().equals(Globals.ARMOR_BODY_DEFAULT)))
							{
								// enchant body armor if not default and not at maximum
								Armor armTemp = ((Player)target).getArmorSlotBody();
								if(powerval > 0 && (armTemp.getCurrProt() - armTemp.getBaseProt()) < Globals.MAXIMUM_CHANGE)
								{
									((Player)target).alterBodyArmor(powerval);
									armName = new String(armTemp.getName());
								}
								else if(powerval < 0)
								{
									((Player)target).alterBodyArmor(powerval);
									armName = new String(armTemp.getName());
								}
							}
							else if(ac != rndArmor && armName == null && !(((Player)target).getArmorSlotSpec().equals(Globals.ARMOR_SPEC_DEFAULT)))
							{
								// enchant special armor if not default and not at maximum
								Armor armTemp = ((Player)target).getArmorSlotSpec();
								if(powerval > 0 && (armTemp.getCurrProt() - armTemp.getBaseProt()) < Globals.MAXIMUM_CHANGE)
								{
									((Player)target).alterSpecialArmor(powerval);
									armName = new String(armTemp.getName());
								}
								else if(powerval < 0)
								{
									((Player)target).alterSpecialArmor(powerval);
									armName = new String(armTemp.getName());
								}
							}
						}
					}
					if(armName != null)
					{
						String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerArmorEnhanced" : "effectPlayerArmorWeakened"));
						sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
						sMessage = substituteInMessage(sMessage, "$ARMOR", armName);
						sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
						sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
						setCurrentMessage(sMessage);
						showMagicEffect(fx.isPositive(), target.getLocation());
						showPause = false;
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noEffect"));
					}
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERABNS))// Player Armor Bonus
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Player)target).alterArmorBonus(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerArmorBonusIncr" : "effectPlayerArmorBonusDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERWDMG))// Player Weapon Damage
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					String wpnName = (String)null;
					if(!(((Player)target).getEquiptWeapon().equals(Globals.WEAPON_DEFAULT)))
					{
						// modify equipt weapon if not default weapon
						((Player)target).alterEquiptWeapon(powerval);
						wpnName = new String(((Player)target).getEquiptWeapon().getName());
					}
					else if(!(((Player)target).getAlternWeapon().equals(Globals.WEAPON_DEFAULT)))
					{
						// otherwise modify alternate weapon
						((Player)target).alterAlternWeapon(powerval);
						wpnName = new String(((Player)target).getAlternWeapon().getName());
					}
					if(wpnName != null)
					{
						String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerWeaponEnhanced" : "effectPlayerWeaponWeakened"));
						sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
						sMessage = substituteInMessage(sMessage, "$WEAPON", wpnName);
						sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
						sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
						setCurrentMessage(sMessage);
						showMagicEffect(fx.isPositive(), target.getLocation());
						showPause = false;
					}
					else
					{
						setCurrentMessage(Globals.getDungeonText("noEffect"));
					}
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERMBNS))// Player Melee Bonus
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Player)target).alterMeleeBonus(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerMeleeBonusIncr" : "effectPlayerMeleeBonusDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERRBNS))// Player Range Bonus
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Player)target).alterRangeBonus(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerRangeBonusIncr" : "effectPlayerRangeBonusDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERLUCK))// Player Luck
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Player)target).alterLuck(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerLuckIncr" : "effectPlayerLuckDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_PLAYERSPEED))// Player Combat Speed
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Player)target).setCurrSpeed(((Player)target).getCurrSpeed() + powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectPlayerSpeedIncr" : "effectPlayerSpeedDecr"));
					sMessage = substituteInMessage(sMessage, "$PLAYER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
		}
		else if(target != null && target.getType() == Globals.LIFEFORM_MONSTER)
		{
			// Process a Monster targetted effect
			if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTATTC))// Monster Attack Class
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Monster)target).adjustAttack(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstAttkIncr" : "effectMonstAttkDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTDEFN))// Monster Defense Class
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Monster)target).adjustDefend(powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstProtIncr" : "effectMonstProtDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTDMG))// Monster Damage (Wounds)
			{
				if(resistable && fx.isPositive() && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstDmgIncr" : "effectMonstDmgDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					sMessage = substituteInMessage(sMessage, "$AMOUNT", Math.abs(powerval));
					setCurrentMessage(sMessage);
					playSound(Globals.CURR_DUNGEON.getEnvironSound((fx.isNegative() ? Globals.SOUND_MAGIC1 : Globals.SOUND_MAGIC2)));
					if(currRoom != null)
					{
						showDamageEffect(fx.isPositive(), target.getLocation(), powerval);
						showPause = false;
					}
					resolveAttackDamage(caster, (Monster)target, powerval);
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTBRIBE))// Monster Negotiation Chance
			{
				if(resistable && fx.isPositive() && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else if(fx.isPositive() && ((Monster)target).getNegotiation() < 1)
				{
					// cannot increase bribability on inherently unbribable enemies
					setCurrentMessage(Globals.getDungeonText("notNegotiable"));
				}
				else
				{
					((Monster)target).setNegotiation(((Monster)target).getNegotiation() + powerval);
					// we use a bit of a hack here - we always set monster(0)'s bribe rate as well on success, because that's the one checked in the negotiation routine
					if((Monster)target != currRoom.getMonster(0))
					{
						currRoom.getMonster(0).setNegotiation(((Monster)target).getNegotiation());
					}
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstNegoIncr" : "effectMonstNegoDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTSPEED))// Monster Combat Speed
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Monster)target).setCurrSpeed(((Monster)target).getCurrSpeed() + powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstSpeedIncr" : "effectMonstSpeedDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTMOBIL))// Monster Mobility Chance
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Monster)target).setMobility(((Monster)target).getMobility() + powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstMobilIncr" : "effectMonstMobilDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTMGCRES))// Monster Magic Resistance
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target, 200))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Monster)target).setResistance(((Monster)target).getResistance() + powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstResIncr" : "effectMonstResDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
			else if(fx.getEffect().equals(Globals.FX_EFFECT_MONSTSPCPER))// Monster Effect Percentile
			{
				if(resistable && !(fx.isPositive()) && makeResistanceRoll(target))
				{
					String sMessage = Globals.getDungeonText("resistedEffect");
					sMessage = substituteInMessage(sMessage, "$TARGET", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
				}
				else
				{
					((Monster)target).setEffectPer(((Monster)target).getEffectPer() + powerval);
					String sMessage = Globals.getDungeonText((fx.isPositive() ? "effectMonstSpecPerIncr" : "effectMonstSpecPerDecr"));
					sMessage = substituteInMessage(sMessage, "$MONSTER", target.getName());
					sMessage = substituteInMessage(sMessage, "$EFFECT", fx.getName());
					setCurrentMessage(sMessage);
					showMagicEffect(fx.isPositive(), target.getLocation());
					showPause = false;
				}
			}
		}
		else
		{
			setCurrentMessage(Globals.getDungeonText("noEffect"));
		}
		currSelNotice = (String)null;
		updateScreen();
		if(showPause)
		{
			doShortWait();
		}
	}

	public void processEffect(Effect fx, Lifeform target, Lifeform caster)
	{
		processEffect(fx, target, caster, true);
	}

	public void processEffect(Effect fx, Lifeform target, boolean resistable)
	{
		processEffect(fx, target, (Lifeform)null, resistable);
	}

	public void processEffect(Effect fx, Lifeform target)
	{
		processEffect(fx, target, (Lifeform)null, true);
	}

	public void processEffect(Effect fx)
	{
		processEffect(fx, (Lifeform)null);
	}

	public boolean makeResistanceRoll(Lifeform target, int limit)
	{
		return target.getResistance() >= (rnd.nextInt(limit) + 1);
	}

	public boolean makeResistanceRoll(Lifeform target)
	{
		return makeResistanceRoll(target, 100);
	}

	public void showDamageEffect(boolean isDamage, Coord loc, int qnt)
	{
		if(currRoom == null) { return; }
		drawScreen();
		plotOutlineText(bufferStrategy.getDrawGraphics(), Math.abs(qnt) + "", loc.getX(), loc.getY(), 2, 2, (isDamage ? Globals.CURR_DUNGEON.getColorDamageFill() : Globals.CURR_DUNGEON.getColorHealingFill()), Globals.CURR_DUNGEON.getColorDamageOutline(), true);
		bufferStrategy.show();
		doShortWait();
	}

	public void showMagicEffect(boolean isGoodEffect, Coord loc)
	{
		drawScreen();
		if(currRoom != null)
		{
			bufferStrategy.getDrawGraphics().drawImage((isGoodEffect ? Globals.CURR_DUNGEON.getImageMagicGood() : Globals.CURR_DUNGEON.getImageMagicEvil()), (loc.getX() * Globals.TILESCALE) + drawpointX, (loc.getY() * Globals.TILESCALE) + drawpointY, this);
		}
		bufferStrategy.show();
		playSound(Globals.CURR_DUNGEON.getEnvironSound((isGoodEffect ? Globals.SOUND_MAGIC1 : Globals.SOUND_MAGIC2)));
		doLongWait();
	}

	public void showVaultEffect()
	{
		drawScreen();
		bufferStrategy.getDrawGraphics().drawImage(Globals.CURR_DUNGEON.getImageMagicVault(), (Globals.CURR_DUNGEON.getVaultCharX() * Globals.TILESCALE) + Globals.CANVAS_HALL_OFFSET_X, (Globals.CURR_DUNGEON.getVaultCharY() * Globals.TILESCALE) + Globals.CANVAS_HALL_OFFSET_Y, this);
		bufferStrategy.show();
		playSound(Globals.CURR_DUNGEON.getEnvironSound(Globals.SOUND_VAULTSH));
		doLongWait();
	}

	public void showProjectile(int type, Color clr, int startX, int startY, int endX, int endY)
	{
		if(currRoom == null) { return; }

		updateScreen();

		// get steps
		double stepSizeX = (Math.abs(startX - endX) * 1.0) / Globals.PROJ_DRAW_STEPS;
		double stepSizeY = (Math.abs(startY - endY) * 1.0) / Globals.PROJ_DRAW_STEPS;
		double slope     = ((startX - endX) == 0 ? 1000.0 : (((startY - endY) * 1.0) / ((startX - endX) * 1.0)));
		int    dirX      = (startX == endX ? 0 : (startX > endX ? -1 : 1));
		int    dirY      = (startY == endY ? 0 : (startY > endY ? -1 : 1));
		int    oldX      = startX;
		int    oldY      = startY;
		int    newX      = startX;
		int    newY      = startY;
		float  drawThick = 1.5f;
		int    sizeLrg   = 12;
		int    sizeMed   = sizeLrg / 2;
		int    sizeSml   = sizeLrg / 3;
		int    stepper   = 0; // use for effects, such as size changes each loop
		int    stepdir   = 1; // use for effects, such as size changes each loop
		int    temp1     = 0;
		int    temp2     = 0;
		int    temp3     = 0;
		int    temp4     = 0;
		int    temp5     = 0;
		int    temp6     = 0;
		int    temp7     = 0;
		int    temp8     = 0;
		for(int s = 1; s < Globals.PROJ_DRAW_STEPS; s++)
		{
			Graphics2D g2D   = (Graphics2D)(bufferStrategy.getDrawGraphics());
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.drawImage(
				bufferRoomComp,
				drawpointX,
				drawpointY,
				drawpointX + (currFloorplan.getWidth() * Globals.TILESCALE),
				drawpointY + (currFloorplan.getHeight() * Globals.TILESCALE),
				0,
				0,
				currFloorplan.getWidth() * Globals.TILESCALE,
				currFloorplan.getHeight() * Globals.TILESCALE,
				this);
			// update vars
			newX = (int)(startX + (stepSizeX * dirX * s));
			newY = (int)(startY + (stepSizeY * dirY * s));
			// draw current projectile placement
			if(type == Globals.PROJECTILE_BOLT)
			{
				if     (Math.abs(slope) < 0.5) { oldX = newX + (16 * -dirX); oldY = newY; drawThick = 1.0f; }
				else if(Math.abs(slope) > 2.5) { oldX = newX; oldY = newY + (16 * -dirY); drawThick = 1.0f; }
				else                           { oldX = newX + (10 * -dirX); oldY = newY + (10 * -dirY); drawThick = 1.5f; }
				g2D.setColor(Globals.PROJCLR_OUTLINE);
				g2D.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.drawLine(oldX, oldY, newX, newY);
				g2D.setColor(clr);
				g2D.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.drawLine(oldX, oldY, newX, newY);
			}
			else if(type == Globals.PROJECTILE_DOT)
			{
				g2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				g2D.setColor(clr);
				g2D.fillOval(newX - (sizeMed / 2), newY - (sizeMed / 2), sizeMed, sizeMed);
				g2D.setColor(Globals.PROJCLR_OUTLINE);
				g2D.drawOval(newX - (sizeMed / 2), newY - (sizeMed / 2), sizeMed, sizeMed);
			}
			else if(type == Globals.PROJECTILE_RING)
			{
				g2D.setColor(Globals.PROJCLR_OUTLINE);
				g2D.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.drawOval(newX - ((sizeLrg + stepper) / 2), newY - ((sizeLrg + stepper) / 2), (sizeLrg + stepper), (sizeLrg + stepper));
				g2D.setColor(clr);
				g2D.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.drawOval(newX - ((sizeLrg + stepper) / 2), newY - ((sizeLrg + stepper) / 2), (sizeLrg + stepper), (sizeLrg + stepper));
				stepper += stepdir * 2;
				if(stepper < -4 || stepper > 4) { stepdir = -stepdir; }
			}
			else if(type == Globals.PROJECTILE_STAR)
			{
				g2D.setColor(Globals.PROJCLR_OUTLINE);
				g2D.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				g2D.drawLine(newX - ((sizeLrg + stepper) / 2), newY, newX + ((sizeLrg + stepper) / 2), newY);
				g2D.drawLine(newX, newY - ((sizeLrg - stepper) / 2), newX, newY + ((sizeLrg - stepper) / 2));
				g2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				g2D.drawLine(newX - ((sizeLrg + stepper) / 2), newY - ((sizeLrg + stepper) / 2), newX + ((sizeLrg + stepper) / 2), newY + ((sizeLrg + stepper) / 2));
				g2D.drawLine(newX - ((sizeLrg - stepper) / 2), newY + ((sizeLrg - stepper) / 2), newX + ((sizeLrg - stepper) / 2), newY - ((sizeLrg - stepper) / 2));
				g2D.setColor(clr);
				g2D.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				g2D.drawLine(newX - ((sizeLrg + stepper) / 2), newY, newX + ((sizeLrg + stepper) / 2), newY);
				g2D.drawLine(newX, newY - ((sizeLrg - stepper) / 2), newX, newY + ((sizeLrg - stepper) / 2));
				g2D.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				g2D.drawLine(newX - ((sizeLrg + stepper) / 2), newY - ((sizeLrg + stepper) / 2), newX + ((sizeLrg + stepper) / 2), newY + ((sizeLrg + stepper) / 2));
				g2D.drawLine(newX - ((sizeLrg - stepper) / 2), newY + ((sizeLrg - stepper) / 2), newX + ((sizeLrg - stepper) / 2), newY - ((sizeLrg - stepper) / 2));
				stepper += stepdir * 2;
				if(stepper < -4 || stepper > 4) { stepdir = -stepdir; }
			}
			else if(type == Globals.PROJECTILE_CUBE)
			{
				g2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				temp1 = stepper * 3;
				temp2 = (stepper + 1 > 4 ? stepper - 3 : stepper + 1) * 3;
				temp3 = (stepper + 2 > 4 ? stepper - 2 : stepper + 2) * 3;
				temp4 = (stepper + 3 > 4 ? stepper - 1 : stepper + 3) * 3;
				g2D.setColor(Globals.PROJCLR_OUTLINE);
				g2D.fillRect(newX - temp1, newY - temp1, temp1, temp1);
				g2D.fillRect(newX,         newY - temp2, temp2, temp2);
				g2D.fillRect(newX,         newY,         temp3, temp3);
				g2D.fillRect(newX - temp4, newY,         temp4, temp4);
				g2D.setColor(clr);
				g2D.fillRect((newX - temp1) + 1, (newY - temp1) + 1, temp1 - 2, temp1 - 2);
				g2D.fillRect((newX        ) + 1, (newY - temp2) + 1, temp2 - 2, temp2 - 2);
				g2D.fillRect((newX        ) + 1, (newY        ) + 1, temp3 - 2, temp3 - 2);
				g2D.fillRect((newX - temp4) + 1, (newY        ) + 1, temp4 - 2, temp4 - 2);
				g2D.fillRect(newX - 2, newY - 2, 5, 5);
				stepper++;
				if(stepper > 4 ) { stepper = 1; }
			}
			else if(type == Globals.PROJECTILE_GLIT)
			{
				g2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				for(int glitter = 0; glitter < 4; glitter++)
				{
					temp1 = newX + rnd.nextInt(sizeLrg * 2) - sizeLrg; // random x position for glitter spark
					temp2 = newY + rnd.nextInt(sizeLrg * 2) - sizeLrg; // random y position for glitter spark
					temp3 = rnd.nextInt(sizeLrg / 2) + 3; // random size position for glitter spark
					g2D.setColor(clr);
					g2D.fillOval(temp1 - (temp3 / 2), temp2 - (temp3 / 2), temp3, temp3);
					g2D.setColor(Globals.PROJCLR_OUTLINE);
					g2D.drawOval(temp1 - (temp3 / 2), temp2 - (temp3 / 2), temp3, temp3);
				}
			}
			else if(type == Globals.PROJECTILE_FLIP)
			{
				g2D.setColor(Globals.PROJCLR_OUTLINE);
				g2D.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.drawOval(newX - (sizeLrg / 4), newY - (sizeLrg / 4), sizeLrg / 2, sizeLrg / 2);
				if(stepper == 0)
				{
					g2D.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX, newY - (sizeLrg / 2), newX, newY + (sizeLrg / 2));
				}
				else if(stepper == 1)
				{
					g2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX - (sizeLrg / 2), newY + (sizeLrg / 2), newX + (sizeLrg / 2), newY - (sizeLrg / 2));
				}
				else if(stepper == 2)
				{
					g2D.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX - (sizeLrg / 2), newY, newX + (sizeLrg / 2), newY);
				}
				else if(stepper == 3)
				{
					g2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX - (sizeLrg / 2), newY - (sizeLrg / 2), newX + (sizeLrg / 2), newY + (sizeLrg / 2));
				}
				g2D.setColor(clr);
				g2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.fillOval(newX - (sizeLrg / 4), newY - (sizeLrg / 4), sizeLrg / 2, sizeLrg / 2);
				if(stepper == 0)
				{
					g2D.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX, newY - (sizeLrg / 2), newX, newY + (sizeLrg / 2));
				}
				else if(stepper == 1)
				{
					g2D.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX - (sizeLrg / 2), newY + (sizeLrg / 2), newX + (sizeLrg / 2), newY - (sizeLrg / 2));
				}
				else if(stepper == 2)
				{
					g2D.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX - (sizeLrg / 2), newY, newX + (sizeLrg / 2), newY);
				}
				else if(stepper == 3)
				{
					g2D.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2D.drawLine(newX - (sizeLrg / 2), newY - (sizeLrg / 2), newX + (sizeLrg / 2), newY + (sizeLrg / 2));
				}
				stepper++;
				if(stepper > 3 ) { stepper = 0; }
			}
			else if(type == Globals.PROJECTILE_SPIN)
			{
				temp1 = stepper;
				temp2 = (stepper + 1 > 8 ? stepper - 7 : stepper + 1);
				temp3 = (stepper + 2 > 8 ? stepper - 6 : stepper + 2);
				temp4 = (stepper + 3 > 8 ? stepper - 5 : stepper + 3);
				temp5 = (stepper + 4 > 8 ? stepper - 4 : stepper + 4);
				temp6 = (stepper + 5 > 8 ? stepper - 3 : stepper + 5);
				temp7 = (stepper + 6 > 8 ? stepper - 2 : stepper + 6);
				temp8 = (stepper + 7 > 8 ? stepper - 1 : stepper + 7);
				g2D.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.setColor(Globals.PROJCLR_OUTLINE);
				g2D.drawOval(newX - temp1, (newY - sizeMed) - temp1, temp1, temp1);
				g2D.drawOval((newX + sizeSml) - temp2, (newY - sizeSml) - temp2, temp2, temp2);
				g2D.drawOval((newX + sizeMed) - temp3, newY - temp3, temp3, temp3);
				g2D.drawOval((newX + sizeSml) - temp4, (newY + sizeSml) - temp4, temp4, temp4);
				g2D.drawOval(newX - temp5, (newY + sizeMed) - temp5, temp5, temp5);
				g2D.drawOval((newX - sizeSml) - temp6, (newY + sizeSml) - temp6, temp6, temp6);
				g2D.drawOval((newX - sizeMed) - temp7, newY - temp7, temp7, temp7);
				g2D.drawOval((newX - sizeSml) - temp8, (newY - sizeSml) - temp8, temp8, temp8);
				g2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2D.setColor(clr);
				g2D.fillOval(newX - temp1, (newY - sizeMed) - temp1, temp1, temp1);
				g2D.fillOval((newX + sizeSml) - temp2, (newY - sizeSml) - temp2, temp2, temp2);
				g2D.fillOval((newX + sizeMed) - temp3, newY - temp3, temp3, temp3);
				g2D.fillOval((newX + sizeSml) - temp4, (newY + sizeSml) - temp4, temp4, temp4);
				g2D.fillOval(newX - temp5, (newY + sizeMed) - temp5, temp5, temp5);
				g2D.fillOval((newX - sizeSml) - temp6, (newY + sizeSml) - temp6, temp6, temp6);
				g2D.fillOval((newX - sizeMed) - temp7, newY - temp7, temp7, temp7);
				g2D.fillOval((newX - sizeSml) - temp8, (newY - sizeSml) - temp8, temp8, temp8);
				stepper--; if(stepper < 1 ) { stepper = 8; }
			}
			// update display
			bufferStrategy.show();
			try { Thread.sleep(Globals.PROJ_DRAW_DELAY); } catch(InterruptedException ie) { }
			g2D.dispose();
		}
	}

//  Text Display Methods -----------------------------------------------------/

	/** Print single-line string to screen at precise pixel location
	*/
	private void plotPreciseString(Graphics g, Font font, Color colorbank, String text, int xOffset, int yOffset, int fieldLength)
	{
		if(fieldLength > 0)
		{
			xOffset += ((fieldLength - text.length()) * (font == Globals.FONT_SMALL ? Globals.FONTWIDTHSMALL : Globals.FONTWIDTH)) / 2;
		}
		g.setFont(font);
		g.setColor(colorbank);
		g.drawString(text, xOffset, yOffset + (font == Globals.FONT_SMALL ? Globals.FONTHEIGHTSMALL : Globals.FONTHEIGHT));
	}

	/** Print single-line string to screen
	*/
	private void plotString(Graphics g, Font fnt, Color colorbank, String text, int xOffset, int yOffset, Color clrBack, int fieldLength, boolean padOnLeft, boolean halfPad)
	{
		if(fieldLength > 0)
		{
			int padsize = fieldLength - text.length();
			StringBuffer sbTemp = new StringBuffer(text.length() + padsize);
			int strptr = 0;
			for(int i = 0; i < fieldLength; i++)
			{
				if(padOnLeft && i < padsize)
				{
					sbTemp.append(' ');
				}
				else if(!padOnLeft && i >= text.length())
				{
					sbTemp.append(' ');
				}
				else
				{
					sbTemp.append(text.charAt(strptr));
					strptr++;
				}
			}
			text = sbTemp.toString();
		}
		if(xOffset == -1)
		{
			// center string based on length
			xOffset = ((Globals.CANVAS_HALL_X * Globals.FONTRATIO) - text.length()) / 2;
		}
		if(clrBack != null)
		{
			g.setColor(clrBack);
			g.fillRect(xOffset * Globals.FONTWIDTH, yOffset * Globals.FONTHEIGHT, text.length() * Globals.FONTWIDTH, Globals.FONTHEIGHT);
		}
		g.setFont(fnt);
		g.setColor(colorbank);
		int charPos = 0;
		int charEnd = text.length();
		while(text.indexOf("{") >= charPos && text.indexOf("}") > charPos)
		{
			int hotPos = text.indexOf("{", charPos);
			int hotEnd = text.indexOf("}", hotPos);
			if(hotPos > charPos)
			{
				g.setColor(colorbank);
				g.drawString(text.substring(charPos, hotPos), (xOffset + charPos) * Globals.FONTWIDTH + (halfPad ? Globals.FONTWIDTH / 2 : 0), (yOffset + 1) * Globals.FONTHEIGHT);
			}
			String hotText = text.substring(hotPos + 1, hotEnd);
			g.setColor(Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_HOTK));
			g.drawString(hotText, (xOffset + hotPos) * Globals.FONTWIDTH + (halfPad ? Globals.FONTWIDTH / 2 : 0), (yOffset + 1) * Globals.FONTHEIGHT);
			text = text.substring(0, hotPos) + text.substring(hotPos + 1, hotEnd) + text.substring(hotEnd + 1);
			charPos = hotEnd - 1;
		}
		if(charPos < charEnd)
		{
			g.setColor(colorbank);
			g.drawString(text.substring(charPos), (xOffset + charPos) * Globals.FONTWIDTH + (halfPad ? Globals.FONTWIDTH / 2 : 0), (yOffset + 1) * Globals.FONTHEIGHT);
		}
	}

	private void plotString(Graphics g, Color colorbank, String text, int xOffset, int yOffset, Color clrBack, int fieldLength, boolean padOnLeft, boolean halfPad)
	{
		plotString(g, Globals.FONT_MAIN, colorbank, text, xOffset, yOffset, clrBack, fieldLength, padOnLeft, halfPad);
	}

	private void plotString(Graphics g, Color colorbank, String text, int xOffset, int yOffset, Color clrBack, int fieldLength, boolean padOnLeft)
	{
		plotString(g, colorbank, text, xOffset, yOffset, clrBack, fieldLength, padOnLeft, false);
	}

	private void plotString(Graphics g, Color colorbank, String text, int xOffset, int yOffset, Color clrBack, int fieldLength)
	{
		plotString(g, colorbank, text, xOffset, yOffset, clrBack, fieldLength, false);
	}

	private void plotString(Graphics g, Color colorbank, String text, int xOffset, int yOffset, int fieldLength, boolean padOnLeft)
	{
		plotString(g, colorbank, text, xOffset, yOffset, (Color)null, fieldLength, padOnLeft, false);
	}

	private void plotString(Graphics g, Color colorbank, String text, int xOffset, int yOffset, boolean halfPad)
	{
		plotString(g, colorbank, text, xOffset, yOffset, (Color)null, 0, false, halfPad);
	}

	private void plotString(Graphics g, Font fnt, Color colorbank, String text, int xOffset, int yOffset)
	{
		plotString(g, fnt, colorbank, text, xOffset, yOffset, (Color)null, 0, false, false);
	}

	private void plotString(Graphics g, Color colorbank, String text, int xOffset, int yOffset)
	{
		plotString(g, colorbank, text, xOffset, yOffset, (Color)null, 0, false, false);
	}

	/** Print multi-lined string to screen
	*/
	private void plotStrings(Graphics g, Color colorbank, String[] text, int xOffset, int yOffset, Color clrBack, int fieldLength)
	{
		for(int i = 0; i < text.length; i++)
		{
			plotString(g, colorbank, text[i], xOffset, yOffset + i, clrBack, fieldLength);
		}
	}

	/** Format and print long text to screen
	*/
	private void plotLongString(Graphics g, Color colorbank, String text, int xOffset, int yOffset, Color clrBack, int fieldLength)
	{
		if(text.length() <= fieldLength)
		{
			plotString(g, colorbank, text, xOffset, yOffset, clrBack, fieldLength);
		}
		else
		{
			Vector<String> vcString = new Vector<String>();
			// parse string based on hard returns ('_' char) first
			StringTokenizer stParse = new StringTokenizer(text, "_", false);
			while(stParse.hasMoreTokens())
			{
				vcString.add(stParse.nextToken());
			}
			// now format lines for length
			int countr = 0;
			int whitespace = -1;
			while(countr < vcString.size())
			{
				String sTemp = vcString.elementAt(countr);
				whitespace = -1;
				if(sTemp.length() > fieldLength)
				{
					for(int ws = fieldLength - 1; ws > 0; ws--)
					{
						if(sTemp.charAt(ws) == ' ')
						{
							whitespace = ws;
							ws = 0;
						}
					}
					if(whitespace == -1)
					{
						whitespace = fieldLength;
					}
					vcString.remove(countr);
					vcString.insertElementAt(sTemp.substring(0, whitespace), countr);
					vcString.insertElementAt(sTemp.substring(whitespace + 1), countr + 1);
				}
				countr++;
			}
			// convert to string array and pass to plotStrings() for printing
			String[] sText = new String[vcString.size()];
			for(int i = 0; i < vcString.size(); i++)
			{
				sText[i] = vcString.elementAt(i);
			}
			plotStrings(g, colorbank, sText, xOffset, yOffset, clrBack, fieldLength);
		}
	}

	/** Print a small text string in the center of the specified area
	*/
	private void plotLabel(Graphics g, Color colorbank, String text, int xCorner, int yCorner, int width, int height)
	{
//		g.setColor(new Color(212, 212, 255));
//		g.fillRect((xCorner * Globals.TILESCALE), (yCorner * Globals.TILESCALE), width * Globals.TILESCALE, height * Globals.TILESCALE);
		g.setFont(Globals.FONT_SMALL);
		g.setColor(colorbank);
		int startX = ((xCorner * Globals.TILESCALE) + ((width * Globals.TILESCALE) / 2)) - ((text.length() * Globals.FONTWIDTHSMALL) / 2);
		int startY = ((yCorner * Globals.TILESCALE) + ((height * Globals.TILESCALE) / 2)) - (Globals.FONTHEIGHTSMALL / 2) + Globals.FONTHEIGHTSMALL;
		if(startX < (xCorner * Globals.TILESCALE)) { startX = (xCorner * Globals.TILESCALE); }
		if(startY < (yCorner * Globals.TILESCALE)) { startY = (yCorner * Globals.TILESCALE); }
		g.drawString(text, startX, startY);
	}

	/** Print text with a colored outline
	  */
	private void plotOutlineText(Graphics g, String text, int xCorner, int yCorner, int width, int height, Color clrFont, Color clrOutline, boolean plotInRoom)
	{
		g.setFont(Globals.FONT_SMALL);
		int startX = ((xCorner * Globals.TILESCALE) + ((width * Globals.TILESCALE) / 2)) - ((text.length() * Globals.FONTWIDTHSMALL) / 2);
		int startY = ((yCorner * Globals.TILESCALE) + ((height * Globals.TILESCALE) / 2)) - (Globals.FONTHEIGHTSMALL / 2) + Globals.FONTHEIGHTSMALL;
		if(startX < (xCorner * Globals.TILESCALE)) { startX = (xCorner * Globals.TILESCALE); }
		if(startY < (yCorner * Globals.TILESCALE)) { startY = (yCorner * Globals.TILESCALE); }
		if(plotInRoom)
		{
			startX = startX + drawpointX;
			startY = startY + drawpointY;
		}
		for(int py = startY - 1; py <= startY + 1; py++)
		{
			for(int px = startX - 1; px <= startX + 1; px++)
			{
				g.setColor(clrOutline);
				g.drawString(text, px, py);
			}
		}
		g.setColor(clrFont);
		g.drawString(text, startX, startY);
	}

	private int[] getBattlegemNumbers(Lifeform player, Lifeform monster)
	{
		int basePlayerChance  = getChanceToHit(player, monster);
		int baseMonsterChance = getChanceToHit(monster, player);
		int[] gemNum = { 2, 2 };
		if     (basePlayerChance >=  80)  { gemNum[0] = 0; }
		else if(basePlayerChance >   60)  { gemNum[0] = 1; }
		else if(basePlayerChance <=  10)  { gemNum[0] = 4; }
		else if(basePlayerChance <   40)  { gemNum[0] = 3; }
		if     (baseMonsterChance >=  80) { gemNum[1] = 4; }
		else if(baseMonsterChance >   60) { gemNum[1] = 3; }
		else if(baseMonsterChance <=  10) { gemNum[1] = 0; }
		else if(baseMonsterChance <   40) { gemNum[1] = 1; }
		return gemNum;
	}

	/** Clear message display
	*/
	private void clearCurrentMessage()
	{
		currMsgs.removeAllElements();
	}

	/** Populate message display from array
	*/
	private void setCurrentMessage(String[] msgLines, Color msgColor)
	{
		clearCurrentMessage();
		for(int i = 0; i < Math.min(msgLines.length, Globals.MAX_MESSAGES); i++)
		{
			if(msgLines[i] != null)
			{
				currMsgs.add(new String(msgLines[i]));
			}
		}
		currMsgColor = msgColor;
	}

	private void setCurrentMessage(String[] msgLines)
	{
		setCurrentMessage(msgLines, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_TEXT));
	}

	/** Populate message display from single string
	*/
	private void setCurrentMessage(String msgLine, Color msgColor)
	{
		clearCurrentMessage();
		currMsgs.add(new String(msgLine));
		currMsgColor = msgColor;
	}

	private void setCurrentMessage(String msgLine)
	{
		setCurrentMessage(msgLine, Globals.CURR_DUNGEON.getColorBank(Globals.FONT_COLOR_TEXT));
	}

	private String substituteInMessage(String msgBody, String msgToken, String msgReplace)
	{
		if(msgBody.indexOf(msgToken) > -1)
		{
			return (msgBody.substring(0, msgBody.indexOf(msgToken)) + msgReplace + msgBody.substring(msgBody.indexOf(msgToken) + msgToken.length()));
		}
		else
		{
			return msgBody;
		}
	}

	private String substituteInMessage(String msgBody, String msgToken, int msgReplace)
	{
		return substituteInMessage(msgBody, msgToken, msgReplace + "");
	}

	/** Draw graphical box around text
	*   (automatically pads by half font size in each direction)
	*/
	private void plotTextBox(Graphics g, int gridX, int gridY, int unitWidth, int unitHeight, Color clrLine, Color clrBack)
	{
		int xStart = (gridX * Globals.FONTWIDTH) - (Globals.FONTWIDTH / 2);
		int yStart = (gridY * Globals.FONTHEIGHT) - (Globals.FONTHEIGHT / 2);
		int xEnd   = (unitWidth + 1) * Globals.FONTWIDTH;
		int yEnd   = (unitHeight + 1) * Globals.FONTHEIGHT;
		if(clrBack != null)
		{
			g.setColor(clrBack);
			g.fillRect(xStart, yStart, xEnd, yEnd);
		}
		g.setColor(clrLine);
		g.drawRect(xStart, yStart, xEnd, yEnd);
	}

//  Load/Save Methods --------------------------------------------------------/

	/** Load a game module from file
	*/
	public void loadGameModule(String moduleName)
	{
		Graphics g2 = bufferStrategy.getDrawGraphics();
		g2.drawImage(bufferEnviron, Globals.CANVAS_HALL_OFFSET_X, Globals.CANVAS_HALL_OFFSET_Y, this);
		currNotification = getTextMessage("loadingMessage");
		drawScreen();
		drawNotificationBox(currNotification.length());
		bufferStrategy.show();
		currModule = moduleName;
		ObjectParser.setModuleName(moduleName);
		ObjectParser.parseDungeonProperties();
		ObjectParser.parseModuleQuests();
		ObjectParser.parseFloorplanTemplates();
		ObjectParser.parseItems(); 
		ObjectParser.parseCharacterDefinitionFile();
		ObjectParser.parseMonsterDefinitions(); 
		currNotification = "";
	}

	/** Load an existing game from a file
	*/
	public void loadSaveGameData(String saveName, boolean restock)
	{
		currNotification = getTextMessage("loadingMessage");
		drawScreen();
		drawNotificationBox(currNotification.length());
		bufferStrategy.show();
		String fullSaveName = Globals.SAVEPATH + saveName + Globals.SAVEEXTN;
		//   load base dungeon info
		loadGameModule(ObjectParser.getModuleName(fullSaveName));
		//   load party
		party = ObjectParser.parsePartyFromFile(fullSaveName);
		//   load quests
		Globals.CURR_DUNGEON.setQuests(ObjectParser.parseQuestsFromFile(fullSaveName));
		//   load level data
		Globals.CURR_DUNGEON.clearLevels();
		currNotification = "";
		if(restock)
		{
			gameInProgress = false;
			flushGameModeBuffer();
			setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_MAIN);
			setCurrGameMode(Globals.MODE_CREATE_GAME, Globals.SUBMODE_CREATE_DUNGEON_DIFF);
		}
		else
		{
			Globals.CURR_DUNGEON.setLevels(ObjectParser.parseLevelsFromFile(fullSaveName));
			if(Globals.CURR_DUNGEON.getLevels() == null)
			{
				System.out.println("Error loading levels file");
				System.exit(2);
			}
			currBoard = Globals.CURR_DUNGEON.getLevel(party.getDepth());
			resetPositions = true;
			gameInProgress = true;
			flushGameModeBuffer();
			setCurrGameMode(Globals.MODE_MENU, Globals.SUBMODE_MENU_MAIN);
			setCurrGameMode(Globals.MODE_EXPLORE, Globals.SUBMODE_NONE);
			if(isLocationRoom(currBoard, party.getLocation()))
			{
				performRoomEnterMove();
			}
			songBkgrnd = playMusic(songBkgrnd, Globals.CURR_DUNGEON.getBackgroundMusic(party.getDepth()), true, true);
		}
	}

	// Obtain a list of game module filenames
	public Vector getModuleFilenames()
	{
		File modsDir = new File(Globals.MODSPATH);
		Vector<String> vcMods = new Vector<String>();
		File[] files = {};
		String parsename;
		if(modsDir.isDirectory())
		{
			files = modsDir.listFiles(new TodrFilenameFilter(Globals.DATAEXTN));
		}
		else
		{
			System.out.println("Modules path does not point to a directory.");
			System.exit(2);
		}
		for(int i = 0; i < files.length; i++)
		{
			parsename = files[i].getName();
			if(parsename.lastIndexOf(Globals.DATAEXTN) > -1)
			{
				parsename = parsename.substring(0, parsename.lastIndexOf(Globals.DATAEXTN));
			}
			for(int v = 0; v < vcMods.size(); v++)
			{
				if(parsename.charAt(0) < vcMods.elementAt(v).charAt(0))
				{
					vcMods.insertElementAt(new String(parsename), v);
					parsename = (String)null;
					v = vcMods.size() + 1;
				}
			}
			if(parsename != null)
			{
				vcMods.add(new String(parsename));
			}
		}
		return vcMods;
	}

	// Obtain a list of existing saved game filenames
	public Vector<String> getSaveGameFilenames(boolean completeOnly)
	{
		File modsDir = new File(Globals.SAVEPATH);
		Vector<String> vcSaves = new Vector<String>();
		File[] files = {};
		String parsename;
		if(modsDir.isDirectory())
		{
			files = modsDir.listFiles(new TodrFilenameFilter(Globals.SAVEEXTN));
		}
		else
		{
			System.out.println("Save path does not point to a directory.");
			System.exit(2);
		}
		for(int i = 0; i < files.length; i++)
		{
			parsename = files[i].getName();
			if(parsename.lastIndexOf(Globals.SAVEEXTN) > -1)
			{
				parsename = parsename.substring(0, parsename.lastIndexOf(Globals.SAVEEXTN));
			}
			if(completeOnly && !(ObjectParser.isCompletedGame(Globals.SAVEPATH + parsename + Globals.SAVEEXTN)))
			{
				// don't add incomplete games in this case
			}
			else
			{
				for(int v = 0; v < vcSaves.size(); v++)
				{
					if(parsename.charAt(0) < vcSaves.elementAt(v).charAt(0))
					{
						vcSaves.insertElementAt(new String(parsename), v);
						parsename = (String)null;
						v = vcSaves.size() + 1;
					}
				}
				if(parsename != null)
				{
					vcSaves.add(new String(parsename));
				}
			}
		}
		return vcSaves;
	}

	public Vector<String> getSaveGameFilenames()
	{
		return getSaveGameFilenames(false);
	}

//  Property File Methods ----------------------------------------------------/

	// Load global application text mappings
	private void loadGlobalTextMappings()
	{
		try
		{
			textGlobals.load(new FileInputStream(new File(Globals.RSRCPATH + "textglobals.properties")));
		}
		catch(Exception e)
		{
			System.out.println("Exception loading global text mappings : " + e.toString());
			System.exit(1);
		}
	}

	// Load global graphic properties
	private void loadGlobalGraphicProperties()
	{
		Properties dungeonProperties = new Properties();
		try
		{
			dungeonProperties.load(new FileInputStream(new File(Globals.RSRCPATH + "graphics.properties")));
			// hallway mathematical values
			aspectX = Double.parseDouble(dungeonProperties.getProperty("aspectx"));
			aspectY = Double.parseDouble(dungeonProperties.getProperty("aspecty"));
			// correct font display values again
			Globals.FONTRATIO = Globals.TILESCALE / Globals.FONTWIDTH;
			Globals.FONTLINE  = Globals.TILESCALE / Globals.FONTHEIGHT;
			// default colors
			Globals.COLOR_MAIN_BACKGROUND = new Color(Integer.parseInt(dungeonProperties.getProperty("maincolorback"),   16));
			clrMenuBkgr   = new Color(Integer.parseInt(dungeonProperties.getProperty("menucolorback"),   16));
			clrMenuSelect = new Color(Integer.parseInt(dungeonProperties.getProperty("menucolorselect"), 16));
		}
		catch(Exception e)
		{
			System.out.println("Exception loading global graphic properties : " + e.toString());
			System.exit(1);
		}
	}

	private String getTextMessage(String textKey)
	{
		if(textGlobals.containsKey(textKey))
		{
			return textGlobals.getProperty(textKey);
		}
		else
		{
			return textKey;
		}
	}

	private void saveSettings()
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(Globals.BASEPATH + "app.properties"));
			appProperties.store(fos, (String)null);
			fos.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(System.out);
		}
	}

// Display Mode Methods -----------------------------------------/

	public DisplayMode getOptimalMode(int targetWidth, int targetHeight, int targetBitplanes, int targetRefresh)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defScrn = ge.getDefaultScreenDevice();
		DisplayMode[] displayModes = defScrn.getDisplayModes();
		DisplayMode preferredMode = displayModes[0];
		for(int j = 0; j < displayModes.length; j++)
		{
//			System.out.println("Available Mode => " + displayModes[j].getWidth() + "x" + displayModes[j].getHeight() + " (" + displayModes[j].getBitDepth() + ") " + displayModes[j].getRefreshRate() + "Mhz");
			int oldWidthDiff    = targetWidth     - preferredMode.getWidth();
			int newWidthDiff    = targetWidth     - displayModes[j].getWidth();
			int oldHeightDiff   = targetHeight    - preferredMode.getHeight();
			int newHeightDiff   = targetHeight    - displayModes[j].getHeight();
			int oldBitplaneDiff = targetBitplanes - preferredMode.getBitDepth();
			int newBitplaneDiff = targetBitplanes - displayModes[j].getBitDepth();
			int oldRefreshDiff  = targetRefresh   - preferredMode.getRefreshRate();
			int newRefreshDiff  = targetRefresh   - displayModes[j].getRefreshRate();
			if(
				((Math.abs(newWidthDiff)    <= Math.abs(oldWidthDiff))    ) &&
				((Math.abs(newHeightDiff)   <= Math.abs(oldHeightDiff))   ) &&
				((Math.abs(newBitplaneDiff) <= Math.abs(oldBitplaneDiff)) ) &&
				((Math.abs(newRefreshDiff)  <= Math.abs(oldRefreshDiff))  )
			)
			{
				preferredMode = displayModes[j];
			}
		}
		return preferredMode;
	}

	public void toggleScreenMode()
	{
		if(Globals.VIEWTYPE == Globals.VIEW_FULLSCREEN)
		{
			Globals.VIEWTYPE = Globals.VIEW_FLOATING;
			defScrn.setFullScreenWindow(null);
			Toolkit tk = Toolkit.getDefaultToolkit();
			this.setFocusable(true);
			this.setSize(Globals.SCRWIDTH, Globals.SCRHEIGHT);
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			while(!(this.isVisible())) { try { Thread.sleep(100); } catch(InterruptedException ie) {} }
			this.createBufferStrategy(2);
			bufferStrategy = this.getBufferStrategy();
			while(bufferStrategy == null) { try { Thread.sleep(100); } catch(InterruptedException ie) {} } // wait for buffers to be built
			appProperties.setProperty("viewtype", "" + Globals.VIEWTYPE);
			saveSettings();
		}
		else if(defScrn.isFullScreenSupported())
		{
			Globals.VIEWTYPE = Globals.VIEW_FULLSCREEN;
			defScrn.setFullScreenWindow(this);
			if(defScrn.isDisplayChangeSupported())
			{
				try
				{
					DisplayMode dm = getOptimalMode(Globals.SCRWIDTH, Globals.SCRHEIGHT, Globals.SCRDEPTH, Globals.SCRREFRESH);
					defScrn.setDisplayMode(dm);
					appProperties.setProperty("viewtype", "" + Globals.VIEWTYPE);
					saveSettings();
				}
				catch(Exception e)
				{
					e.printStackTrace(System.out);
				}
			}
		}
	}

//  Game Creation Methods  ---------------------------------------------------/

	private void createNewParty(int partySize)
	{
		party = new Party(partySize, Globals.CP_INVALID_LOC, 0, Globals.NORTH, Globals.STARTING_GOLD * partySize, Globals.STARTING_RATIONS * partySize);
		party.setRationsInterval(Globals.DEF_INTERVAL_RATIONS);
		party.setHealingInterval(Globals.DEF_INTERVAL_HEALING);
		currPlayerPtr = 0;
	}

//  Exit Method  -------------------------------------------------------------/

	/** End game and exit program
	*/
	public void quitGame()
	{
		// IMPLEMENT - check if game underway
		// terminate program
		this.dispose();
		System.exit(0);
	}

// Main Method ---------------------------------------------------------------/

	public static void main(String[] args)
	{
		boolean dodebug = false;
		if(args.length > 0)
		{
			for(int a = 0; a < args.length; a++)
			{
				if(args[a].toLowerCase().equals("debug"))
				{
					dodebug = true;
				}
				else
				{
					Globals.BASEPATH = args[a];
					Globals.RSRCPATH = Globals.BASEPATH + "assets" + File.separator;
					Globals.MODSPATH = Globals.RSRCPATH + "modules" + File.separator;
					Globals.SAVEPATH = Globals.RSRCPATH + "savegame" + File.separator;
				}
			}
		}
		ToDR game = new ToDR();
	}

}
