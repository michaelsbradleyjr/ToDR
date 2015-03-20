package com.dreamcodex.todr.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import com.dreamcodex.todr.ToDR;
import com.dreamcodex.todr.object.Board;
import com.dreamcodex.todr.object.CharacterClass;
import com.dreamcodex.todr.object.Globals;
import com.dreamcodex.todr.object.Lifeform;
import com.dreamcodex.todr.object.Monster;
import com.dreamcodex.todr.object.MonsterDef;
import com.dreamcodex.todr.object.Party;
import com.dreamcodex.todr.object.Player;
import com.dreamcodex.todr.object.Room;
import com.dreamcodex.todr.object.ItemClass;
import com.dreamcodex.todr.object.Item;
import com.dreamcodex.todr.object.Armor;
import com.dreamcodex.todr.object.Weapon;
import com.dreamcodex.todr.object.Effect;
import com.dreamcodex.util.Coord;

/** AppEditor
  * ToDR application settings module editor
  *
  * @author Howard Kistler
  */

public class AppEditor extends JFrame implements WindowListener, KeyListener, ActionListener, ChangeListener
{
//  Local Constants ------------------------------------------------------------------------->

	private static final int NODE_NONE  = 0;
	private static final int NODE_ROOT  = 1;
	private static final int NODE_WORLD = 2;
	private static final int NODE_BOARD = 3;

//  Current Instance Variables -------------------------------------------------------------->

	private int oldGridX = -1;
	private int oldGridY = -1;

	// Common Game Images
	private Image imgFloor;
	private Image imgSolid;
	private Image imgColumn;
	private Image imgWallH;
	private Image imgWallV;
	private Image imgDoorH;
	private Image imgDoorV;
	private Image imgStairsU;
	private Image imgStairsD;
	private Image imgFountain;
	private Image imgStatue;
	private Image imgShop;
	private Image imgInterface;
	private Image imgCardinals;
	private Image imgBackParty;
	// Color Definitions
	//   Main display colors
	private Color clrMainBkgr    = new Color(0, 0, 0);
	//   Hall module colors
	private Color clrHallBkgr    = new Color(0, 0, 0);
	private Color clrHallWall    = new Color(196, 196, 196);
	private Color clrHallFloor   = new Color(255, 160, 160);
	private Color clrHallCeiling = new Color(128, 160, 255);
	private Color clrHallOutline = new Color(0, 0, 0);
	private Color clrHallDoor    = new Color(255, 212, 164);
	//   Room module colors
	private Color clrRoomBkgr   = new Color(0, 0, 0);
	//   Map module colors
	private Color clrMapBkgr    = new Color(255, 255, 192);
	private Color clrMapMapped  = new Color(128, 128, 255);
	private Color clrMapVisited = new Color(0, 0, 0);
	private Color clrMapParty   = new Color(255, 128, 128);
	//   Menu display colors
	private Color clrMenuBkgr   = new Color(255, 255, 192);
	private Color clrMenuSelect = new Color(192, 192, 255);
	//   Info display colors
	private Color clrInfoBkgr   = new Color(255, 255, 192);
	//   Message display colors
	private Color clrMsgWindow  = new Color(128, 128, 128);
	private Color clrMsgText    = new Color(255, 255, 255);
	//   Selection box color
	private Color clrSelectionBox = new Color(0, 128, 255);
	//   Text box colors
	private Color clrTextBoxOutline = new Color(0, 0, 0);
	private Color clrTextBoxFill    = new Color(255, 255, 255);
	// pseudo-constants
	private int     hallCenterX = Globals.CANVAS_HALL_X * Globals.TILESCALE;
	private int     hallCenterY = Globals.CANVAS_HALL_Y * Globals.TILESCALE;
	private double  scalingIncrement = 1.5;
	private double  shadeStrength = 30.0;
	private boolean bOutline = false;
	private double  scale1 = 1.5;
	private double  scale2 = scale1 * 2;
	private double  scale3 = scale2 * 2;
	private double  aspectX = 1.0;
	private double  aspectY = 1.0;
	private int     offsetCardinalX = ((Globals.CANVAS_HALL_X * Globals.TILESCALE) / 2) - (Globals.TILESCALE / 2);
	private int     offsetCardinalY = Globals.TILESCALE;
	private int     sizeCardinalX = Globals.TILESCALE;
	private int     sizeCardinalY = Globals.TILESCALE;

	private String  currModule       = "";
	private Properties textGlobals  = new Properties();
	private Properties textMappings = new Properties();

//  Class Instance Pseudo-Constants --------------------------------------------------------->

	private Coord cpMIN;
	private Coord cpMAX;
	private Coord cpGlobalSize;
	private Coord cpViewSize;

	private long executeTime = System.currentTimeMillis();

//  Components ------------------------------------------------------------------------------>

	private BufferedImage bufferBackground;

	private JTabbedPane tabEditors;

	private ButtonGroup bgrpTiles;

	private JTextField txtScreenWidth;
	private JTextField txtScreenHeight;
	private JTextField txtScreenDepth;
	private JTextField txtScreenRefresh;
	private JCheckBox  chkFullscreen;
	private JTextField txtCanvasMaxX;
	private JTextField txtCanvasMaxY;
	private JTextField txtScaleTileX;
	private JTextField txtScaleTileY;
	private JTextField txtScaleEntityX;
	private JTextField txtScaleEntityY;
	private JTextField txtGameSpeed;

	private JTextField txtViewType;
	private JTextField txtFontMain;
	private JTextField txtFontMainSize;
	private JTextField txtFontMainWidth;
	private JTextField txtFontMainHeight;

	private JTextField txtAspectX;
	private JTextField txtAspectY;
	private JTextField txtScaleDistort;
	private JTextField txtColorMainBackDefault;
	private JTextField txtColorMenuBackDefault;
	private JTextField txtColorMenuSelectDefault;
	private JTextField txtColorFontDefault;

	private JTextField txtMenuMainTitle;
	private JTextField txtMenuMainNew;
	private JTextField txtMenuMainLoad;
	private JTextField txtMenuMainRestock;
	private JTextField txtMenuMainSave;
	private JTextField txtMenuMainResume;
	private JTextField txtMenuMainExit;

	private JTextField jtxtHeirarchyName;
	private JButton jbtnNewWorld;
	private JButton jbtnNewBoard;
	private JButton jbtnMoveUp;
	private JButton jbtnMoveDown;

//  Constructors ---------------------------------------------------------------------------->

	public AppEditor()
	{
		super("Game Editor");

		// obtain application properties
		Properties appProperties = new Properties();
		try
		{
			appProperties.load(new FileInputStream(new File(Globals.BASEPATH + "app.properties")));
		}
		catch(Exception e)
		{
			System.out.println("Exception loading app properties : " + e.toString());
			System.exit(1);
		}

		Globals.VIEWTYPE   = (appProperties.getProperty("viewtype") != null && appProperties.getProperty("viewtype").length() > 0 ? Integer.parseInt(appProperties.getProperty("viewtype")) : Globals.VIEW_FLOATING);
		// NOTE: still problems with WINDOWED mode (doesn't account for window element insets)
		Globals.FONTSIZE   = Integer.parseInt(appProperties.getProperty("fontsize"));
		Globals.FONTWIDTH  = Integer.parseInt(appProperties.getProperty("fontwidth"));
		Globals.FONTHEIGHT = Integer.parseInt(appProperties.getProperty("fontheight"));
		Globals.FONTRATIO  = Globals.TILESCALE / Globals.FONTWIDTH;
		Globals.FONTLINE   = Globals.TILESCALE / Globals.FONTHEIGHT;

		// obtain global graphics properties
		Properties ggrfxProperties = new Properties();
		try
		{
			ggrfxProperties.load(new FileInputStream(new File(Globals.RSRCPATH + "graphics.properties")));
		}
		catch(Exception e)
		{
			System.out.println("Exception loading global graphics properties : " + e.toString());
			System.exit(1);
		}

		// obtain global text strings
		Properties gtextProperties = new Properties();
		try
		{
			gtextProperties.load(new FileInputStream(new File(Globals.RSRCPATH + "textglobals.properties")));
		}
		catch(Exception e)
		{
			System.out.println("Exception loading global text strings : " + e.toString());
			System.exit(1);
		}

		ObjectParser.setMediaTracker(new MediaTracker(this));

		// Create the main button bar
		JToolBar jToolBar = new JToolBar(JToolBar.HORIZONTAL);
		jToolBar.setFloatable(false);
		JButton jbtnOpenData = new JButton(new ImageIcon(ObjectParser.loadImage("icon_open.gif"))); jbtnOpenData.setActionCommand("opendata"); jbtnOpenData.addActionListener(this); jbtnOpenData.setToolTipText("Load Data"); jToolBar.add(jbtnOpenData);
		JButton jbtnSaveData = new JButton(new ImageIcon(ObjectParser.loadImage("icon_save.gif"))); jbtnSaveData.setActionCommand("savedata"); jbtnSaveData.addActionListener(this); jbtnSaveData.setToolTipText("Save Data"); jToolBar.add(jbtnSaveData);
		jToolBar.add(new JToolBar.Separator());
		JButton jbtnExit     = new JButton(new ImageIcon(ObjectParser.loadImage("icon_exit.gif"))); jbtnExit.setActionCommand("exit"); jbtnExit.addActionListener(this); jbtnExit.setToolTipText("Exit"); jToolBar.add(jbtnExit);

		// Create the level editor button bar
		JToolBar jToolBarLevelEditor = new JToolBar(JToolBar.HORIZONTAL);
		jToolBarLevelEditor.setFloatable(false);
		JButton jbtnPrevWorld = new JButton(new ImageIcon(ObjectParser.loadImage("icon_bigprev.gif"))); jbtnPrevWorld.setActionCommand("prevworld"); jbtnPrevWorld.addActionListener(this); jbtnPrevWorld.setToolTipText("Previous World"); jToolBarLevelEditor.add(jbtnPrevWorld);
		JButton jbtnNextWorld = new JButton(new ImageIcon(ObjectParser.loadImage("icon_bignext.gif"))); jbtnNextWorld.setActionCommand("nextworld"); jbtnNextWorld.addActionListener(this); jbtnNextWorld.setToolTipText("Next World");     jToolBarLevelEditor.add(jbtnNextWorld);
		jToolBarLevelEditor.add(new JToolBar.Separator());
		JButton jbtnPrevBoard = new JButton(new ImageIcon(ObjectParser.loadImage("icon_prev.gif"))); jbtnPrevBoard.setActionCommand("prevboard"); jbtnPrevBoard.addActionListener(this); jbtnPrevBoard.setToolTipText("Previous Board"); jToolBarLevelEditor.add(jbtnPrevBoard);
		JButton jbtnNextBoard = new JButton(new ImageIcon(ObjectParser.loadImage("icon_next.gif"))); jbtnNextBoard.setActionCommand("nextboard"); jbtnNextBoard.addActionListener(this); jbtnNextBoard.setToolTipText("Next Board");     jToolBarLevelEditor.add(jbtnNextBoard);
		jToolBarLevelEditor.add(new JToolBar.Separator());

		// App Properties Editor
		JPanel jpnlEditorApp = new JPanel();
		jpnlEditorApp.setBackground(new Color(164, 164, 128));
		jpnlEditorApp.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;
		addNewFormTitle(jpnlEditorApp, "View Type", gbc);
		txtViewType = new JTextField(appProperties.getProperty("viewtype"), 16);
		addNewFormElement(jpnlEditorApp, "View Type", txtViewType, gbc);
		addNewFormTitle(jpnlEditorApp, "Font Settings", gbc);
		txtFontMain = new JTextField(appProperties.getProperty("fontmainttf"), 16);
		addNewFormElement(jpnlEditorApp, "Main Font", txtFontMain, gbc);
		txtFontMainSize = new JTextField(appProperties.getProperty("fontsize"), 4);
		addNewFormElement(jpnlEditorApp, "Main Font Size (pts)", txtFontMainSize, gbc);
		txtFontMainWidth = new JTextField(appProperties.getProperty("fontwidth"), 4);
		addNewFormElement(jpnlEditorApp, "Main Font Width (px)", txtFontMainWidth, gbc);
		txtFontMainHeight = new JTextField(appProperties.getProperty("fontheight"), 4);
		addNewFormElement(jpnlEditorApp, "Main Font Height (px)", txtFontMainHeight, gbc);

		JPanel jpnlFloat = new JPanel();
		jpnlFloat.setLayout(new FlowLayout());
		jpnlFloat.setBackground(new Color(164, 128, 128));
		jpnlFloat.add(jpnlEditorApp);

		// Global Graphics Properties Editor
		JPanel jpnlEditorGrfxGlobal = new JPanel();
		jpnlEditorGrfxGlobal.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorGrfxGlobal, "3D Settings", gbc);
		txtAspectX = new JTextField(ggrfxProperties.getProperty("aspectx"), 8);
		addNewFormElement(jpnlEditorGrfxGlobal, "Aspect Ratio X", txtAspectX, gbc);
		txtAspectY = new JTextField(ggrfxProperties.getProperty("aspecty"), 8);
		addNewFormElement(jpnlEditorGrfxGlobal, "Aspect Ratio Y", txtAspectY, gbc);
		txtScaleDistort = new JTextField(ggrfxProperties.getProperty("scaledistort"), 8);
		addNewFormElement(jpnlEditorGrfxGlobal, "Scale Distortion", txtScaleDistort, gbc);
		addNewFormTitle(jpnlEditorGrfxGlobal, "Default Application Colors", gbc);
		txtColorMainBackDefault = new JTextField(ggrfxProperties.getProperty("maincolorback"), 8);
		addNewFormColorField(jpnlEditorGrfxGlobal, "Main Background Color", txtColorMainBackDefault, ggrfxProperties.getProperty("maincolorback"), gbc);
		txtColorMenuBackDefault = new JTextField(ggrfxProperties.getProperty("menucolorback"), 8);
		addNewFormColorField(jpnlEditorGrfxGlobal, "Menu Background Color", txtColorMenuBackDefault, ggrfxProperties.getProperty("menucolorback"), gbc);
		txtColorMenuSelectDefault = new JTextField(ggrfxProperties.getProperty("menucolorselect"), 8);
		addNewFormColorField(jpnlEditorGrfxGlobal, "Menu Selection Color", txtColorMenuSelectDefault, ggrfxProperties.getProperty("menucolorselect"), gbc);
		txtColorFontDefault = new JTextField(ggrfxProperties.getProperty("fontcolor"), 8);
		addNewFormColorField(jpnlEditorGrfxGlobal, "Text Color", txtColorFontDefault, ggrfxProperties.getProperty("fontcolor"), gbc);

		// Global Text Strings Editor
		JPanel jpnlEditorTextGlobal = new JPanel();
		jpnlEditorTextGlobal.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorTextGlobal, "Menu Strings", gbc);
		txtMenuMainTitle = new JTextField(gtextProperties.getProperty("gameName"), 16);
		addNewFormElement(jpnlEditorTextGlobal, "Main : Menu Title", txtMenuMainTitle, gbc);
		txtMenuMainNew = new JTextField(gtextProperties.getProperty("menuMainNew"), 16);
		addNewFormElement(jpnlEditorTextGlobal, "Main : New Game", txtMenuMainNew, gbc);
		txtMenuMainLoad = new JTextField(gtextProperties.getProperty("menuMainLoadGame"), 16);
		addNewFormElement(jpnlEditorTextGlobal, "Main : Load Game", txtMenuMainLoad, gbc);
		txtMenuMainRestock = new JTextField(gtextProperties.getProperty("menuMainRestock"), 16);
		addNewFormElement(jpnlEditorTextGlobal, "Main : Restock ", txtMenuMainRestock, gbc);
		txtMenuMainSave = new JTextField(gtextProperties.getProperty("menuMainSave"), 16);
		addNewFormElement(jpnlEditorTextGlobal, "Main : Save Game", txtMenuMainSave, gbc);
		txtMenuMainResume = new JTextField(gtextProperties.getProperty("menuMainResume"), 16);
		addNewFormElement(jpnlEditorTextGlobal, "Main : Resume Game", txtMenuMainResume, gbc);
		txtMenuMainExit = new JTextField(gtextProperties.getProperty("menuMainQuit"), 16);
		addNewFormElement(jpnlEditorTextGlobal, "Main : Exit Game", txtMenuMainExit, gbc);

/*
menuNewTitle=New Game
menuNewLoadModule=Select Game Module
menuNewSelect=Play Selected Module
menuNewExit=Back To Main Menu
menuLoadTitle=Load Game
menuLoadLoadModule=Select Saved Game
menuLoadSelect=Load Game
menuLoadExit=Back To Main Menu
menuSaveTitle=Save Game
menuSaveExistingSaves=Saved Games
menuSaveExit=Cancel Save & Return
menuRestockTitle=Load Party & Restock Levels
menuRestockLoadModule=Select Saved Game
menuRestockSelect=Load Party
menuRestockExit=Back To Main Menu
*/

		// Module Browser
		JPanel jpnlEditorModuleList = new JPanel();
		jpnlEditorModuleList.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorModuleList, "Modules", gbc);
		Vector vcModules = getModuleFilenames();
		JList jlistModules = new JList(vcModules);
		jlistModules.setVisibleRowCount(12);
		JScrollPane jscrlModules = new JScrollPane(jlistModules, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		addNewFormElement(jpnlEditorModuleList, "Module List", jscrlModules, gbc);

		// Main Tabbed Component
		tabEditors = new JTabbedPane();
		tabEditors.add("App Properties", jpnlFloat);
		tabEditors.add("Global Graphics", jpnlEditorGrfxGlobal);
		tabEditors.add("Global Text", jpnlEditorTextGlobal);
		tabEditors.add("Modules", jpnlEditorModuleList);
		tabEditors.addChangeListener(this);

		// Assemble the application
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tabEditors, BorderLayout.CENTER);
		this.getContentPane().add(jToolBar, BorderLayout.NORTH);
		this.addKeyListener(this);
		this.addWindowListener(this);
		this.pack();
		Insets insets = this.getInsets();
		this.setSize(600, 600);
		this.setVisible(true);
	}

//  Listeners ------------------------------------------------------------------------------->

	/* KeyListener methods */
	public void keyPressed(KeyEvent ke)
	{
		if(ke.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			this.dispose();
			System.exit(0);
		}
	}
	public void keyReleased(KeyEvent ke) { ; }
	public void keyTyped(KeyEvent ke)    { ; }

	/* ActionListener methods */
	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			String command = ae.getActionCommand();
			if(command.equals("exit"))
			{
				this.dispose();
				System.exit(0);
			}
			else if(command.equals("nextboard"))
			{
			}
			else if(command.equals("prevboard"))
			{
			}
			else if(command.equals("nextworld"))
			{
			}
			else if(command.equals("prevworld"))
			{
			}
			else if(command.equals("addboard"))
			{
			}
			else if(command.equals("addworld"))
			{
			}
			else if(command.equals("moveup"))
			{
			}
			else if(command.equals("movedown"))
			{
			}
			else if(command.equals("rename"))
			{
			}
			else if(command.equals("savedata"))
			{
				saveInitData();
			}
		}
		catch(Exception e) { ; }
	}

	/* WindowListener methods */
	public void windowClosing(WindowEvent we)
	{
		this.dispose();
		System.exit(0);
	}
	public void windowOpened(WindowEvent we)      { ; }
	public void windowClosed(WindowEvent we)      { ; }
	public void windowActivated(WindowEvent we)   { ; }
	public void windowDeactivated(WindowEvent we) { ; }
	public void windowIconified(WindowEvent we)   { ; }
	public void windowDeiconified(WindowEvent we) { ; }

	/* ChangeListener methods */
	public void stateChanged(ChangeEvent ce)
	{
		if(ce.getSource() == tabEditors)
		{
			if(tabEditors.getSelectedIndex() == 1)
			{
//				describeUniverse();
			}
			else if(tabEditors.getSelectedIndex() == 2)
			{
				updateDisplay();
			}
		}
	}

//  File Reader Methods --------------------------------------------------------------------->

	public Vector getModuleFilenames()
	{
		File modsDir = new File(Globals.MODSPATH);
		Vector vcMods = new Vector();
		File[] files = {};
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
			for(int v = 0; v < vcMods.size(); v++)
			{
				if(files[i].getName().charAt(0) < ((String)(vcMods.elementAt(v))).charAt(0))
				{
					vcMods.insertElementAt(new String(files[i].getName()), v);
					files[i] = (File)null;
					v = vcMods.size() + 1;
				}
			}
			if(files[i] != null)
			{
				vcMods.add(new String(files[i].getName()));
			}
		}
		return vcMods;
	}

	private void saveInitData()
	{
		StringBuffer sbInitFile = new StringBuffer();
		sbInitFile.append("* Screen Settings\r\n");
		sbInitFile.append("PrefWidth:"    + txtScreenWidth.getText() + "\r\n");
		sbInitFile.append("PrefHeight:"   + txtScreenHeight.getText() + "\r\n");
		sbInitFile.append("PrefDepth:"    + txtScreenDepth.getText() + "\r\n");
		sbInitFile.append("PrefRefresh:"  + txtScreenRefresh.getText() + "\r\n");
		sbInitFile.append("FullScreen:"   + (chkFullscreen.isSelected() ? "true" : "false") + "\r\n");
		sbInitFile.append("* Canvas Settings\r\n");
		sbInitFile.append("MaxX:"         + txtCanvasMaxX.getText() + "\r\n");
		sbInitFile.append("MaxY:"         + txtCanvasMaxY.getText() + "\r\n");
		sbInitFile.append("* Scale Settings\r\n");
		sbInitFile.append("TileScaleX:"   + txtScaleTileX.getText() + "\r\n");
		sbInitFile.append("TileScaleY:"   + txtScaleTileY.getText() + "\r\n");
		sbInitFile.append("EntityScaleX:" + txtScaleEntityX.getText() + "\r\n");
		sbInitFile.append("EntityScaleY:" + txtScaleEntityY.getText() + "\r\n");
		sbInitFile.append("* Application Settings\r\n");
		sbInitFile.append("GameSpeed:"    + txtGameSpeed.getText() + "\r\n");
		writeFile(new File(Globals.RSRCPATH + "init" + Globals.DATAEXTN), sbInitFile.toString());
	}

	private void addToHashtable(Hashtable hashtable, Object key, Object value)
	{
		if(hashtable.containsKey(key))
		{
			hashtable.remove(key);
		}
		hashtable.put(key, value);
	}

//  Class Methods --------------------------------------------------------------------------->

	private void updateDisplay()
	{
		if(bufferBackground == null)
		{
//			buildBoard();
		}
	}

	private void writeFile(File filename, String filecontents)
	{
		try
		{
			if(filename == null)
			{
//				filename = getFileFromChooser(".", JFileChooser.SAVE_DIALOG, extsPOV, "POVray");
			}
			if(filename != null)
			{
				FileWriter fw = new FileWriter(filename);
				fw.write(filecontents, 0, filecontents.length());
				fw.flush();
				fw.close();
			}
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}

	private void addNewFormTitle(JPanel jpanel, String label, GridBagConstraints gbc)
	{
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.weightx = 1;
		JLabel jTitle = new JLabel(label);
		jTitle.setForeground(new Color(128, 64, 64));
		jpanel.add(jTitle, gbc);
	}

	private void addNewFormColorField(JPanel jpanel, String label, JTextField textfield, String clr, GridBagConstraints gbc)
	{
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.weightx = 1;
		jpanel.add(new JLabel(label), gbc);
		gbc.gridx = 1; gbc.weightx = 2;
		jpanel.add(textfield, gbc);
		gbc.gridx = 2; gbc.weightx = 2;
		JLabel jlblSwatch = new JLabel("--------");
		jlblSwatch.setOpaque(true);
		jlblSwatch.setBackground(new Color(Integer.parseInt(clr, 16)));
		jlblSwatch.setForeground(new Color(Integer.parseInt(clr, 16)));
		jlblSwatch.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		jpanel.add(jlblSwatch, gbc);
	}

	private void addNewFormElement(JPanel jpanel, String label, JComponent jelem, GridBagConstraints gbc)
	{
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.weightx = 1;
		jpanel.add(new JLabel(label), gbc);
		gbc.gridx = 1; gbc.weightx = 2;
		jpanel.add(jelem, gbc);
	}

// Convenience Methods ---------------------------------------------------------------------->

	private String getHashProperty(Hashtable htProperties, String key)
	{
		if(htProperties.containsKey(key))
		{
			return (String)(htProperties.get(key));
		}
		else
		{
			return "";
		}
	}

	private boolean getHashBoolean(Hashtable htProperties, String key)
	{
		if(htProperties.containsKey(key))
		{
			return ((String)(htProperties.get(key))).equalsIgnoreCase("true");
		}
		else
		{
			return false;
		}
	}

//  Main Method ----------------------------------------------------------------------------->

	public static void main(String[] args)
	{
		AppEditor ge = new AppEditor();
	}

}
