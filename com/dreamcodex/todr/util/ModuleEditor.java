package com.dreamcodex.todr.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

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

/** ModuleEditor
  * ToDR quest module editor
  *
  * @author Howard Kistler
  */

public class ModuleEditor extends JFrame implements WindowListener, WindowStateListener, KeyListener, ActionListener, ChangeListener
{
//  Local Constants -------------------------------------------------------------------------/

	private static final int NODE_NONE  = 0;
	private static final int NODE_ROOT  = 1;
	private static final int NODE_WORLD = 2;
	private static final int NODE_BOARD = 3;

//  Current Instance Variables --------------------------------------------------------------/

	private int LEVEL_WIDTH;
	private int LEVEL_HEIGHT;
	private int LEVEL_ROOMS_MIN;
	private int LEVEL_ROOMS_MAX;

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
	private Image imgDeadPlayer;
	private Image imgInterface;
	private Image imgCardinals;
	private Image imgBackParty;

	// Color Definitions
	//   Main display colors
	private Color clrMainBkgr       = new Color(0, 0, 0);
	//   Hall module colors
	private Color clrHallBkgr       = new Color(0, 0, 0);
	private Color clrHallWall       = new Color(196, 196, 196);
	private Color clrHallFloor      = new Color(255, 160, 160);
	private Color clrHallCeiling    = new Color(128, 160, 255);
	private Color clrHallOutline    = new Color(0, 0, 0);
	private Color clrHallDoor       = new Color(255, 212, 164);
	//   Room module colors
	private Color clrRoomBkgr       = new Color(0, 0, 0);
	//   Map module colors
	private Color clrMapBkgr        = new Color(255, 255, 192);
	private Color clrMapMapped      = new Color(128, 128, 255);
	private Color clrMapVisited     = new Color(0, 0, 0);
	private Color clrMapParty       = new Color(255, 128, 128);
	//   Menu display colors
	private Color clrMenuBkgr       = new Color(255, 255, 192);
	private Color clrMenuSelect     = new Color(192, 192, 255);
	//   Info display colors
	private Color clrInfoBkgr       = new Color(255, 255, 192);
	//   Message display colors
	private Color clrMsgWindow      = new Color(128, 128, 128);
	private Color clrMsgText        = new Color(255, 255, 255);
	//   Selection box color
	private Color clrActiveMember   = new Color(0, 128, 255);
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

	private String  currModule           = "";
	private Properties textGlobals       = new Properties();
	private Properties textMappings      = new Properties();
	private Properties dungeonProperties = new Properties();

//  Class Instance Pseudo-Constants ---------------------------------------------------------/

	private Coord cpMIN;
	private Coord cpMAX;
	private Coord cpGlobalSize;
	private Coord cpViewSize;

	private long executeTime = System.currentTimeMillis();

//  Components ------------------------------------------------------------------------------/

	private BufferedImage bufferBackground;

	private JTabbedPane tabEditors;

	private JList jlistModules;

	private JTextField txtColorMainBackDefault;
	private JLabel     lblColorMainBackDefault;
	private JTextField txtFontMain;
	private JTextField txtFontMainSize;
	private JTextField txtFontMainWidth;
	private JTextField txtFontMainHeight;
	private JTextField txtFontSmall;
	private JTextField txtFontSmallSize;
	private JTextField txtFontSmallWidth;
	private JTextField txtFontSmallHeight;

	private JTextField txtLevelWidth;
	private JTextField txtLevelHeight;
	private JTextField txtRoomsMin;
	private JTextField txtRoomsMax;
	private JList      jlistLevelNames;

	// Tables and associated objects
	private CustomTableModel ctmCharDefs;
	private CustomJTable     jtblCharDefs;
	private JScrollPane      jscrlCharDefsTable;
	private CustomTableModel ctmClassDiffs;
	private CustomJTable     jtblClassDiffs;
	private JScrollPane      jscrlClassDiffsTable;

	private CustomTableModel ctmItemDefs;
	private CustomJTable     jtblItemClasses;
	private JScrollPane      jscrlItemClassesTable;

	private CustomTableModel ctmItemCollect;
	private CustomJTable     jtblItems;
	private JScrollPane      jscrlItemsTable;

	private CustomTableModel ctmWeapons;
	private CustomJTable     jtblWeapons;
	private JScrollPane      jscrlWeaponsTable;

	private CustomTableModel ctmArmors;
	private CustomJTable     jtblArmors;
	private JScrollPane      jscrlArmorsTable;

	private CustomTableModel ctmMonDefs;
	private CustomJTable jtblMonsterDefs;
	private JScrollPane jscrlMonsterDefsTable;

//  Constructors ----------------------------------------------------------------------------/

	public ModuleEditor(String moduleName)
	{
		super("Game Editor");

		Globals.BASEPATH = "com" + File.separator + "dreamcodex" + File.separator + "todr" + File.separator;
		Globals.RSRCPATH = Globals.BASEPATH + "assets" + File.separator;
		Globals.MODSPATH = Globals.RSRCPATH + "modules" + File.separator;
		Globals.SAVEPATH = Globals.RSRCPATH + "savegame" + File.separator;

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

		GridBagConstraints gbc = new GridBagConstraints();

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
		jlistModules = new JList(vcModules);
		jlistModules.setVisibleRowCount(12);
		JScrollPane jscrlModules = new JScrollPane(jlistModules, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		addNewFormElement(jpnlEditorModuleList, jscrlModules, gbc);

		JPanel jpnlFloatModuleList = new JPanel();
		jpnlFloatModuleList.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatModuleList.add(jpnlEditorModuleList);

		// Character Definitions Editor
		JPanel jpnlEditorCharDefs = new JPanel();
		jpnlEditorCharDefs.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		// Add table of CharacterClass definitions
		Vector<String> vcHeaders = new Vector<String>();
		vcHeaders.add("Key");
		vcHeaders.add("Name");
		vcHeaders.add("Exp Level Up");
		vcHeaders.add("Base HP");
		vcHeaders.add("HP Level Gain");
		vcHeaders.add("Base Speed");
		vcHeaders.add("Solo Only?");
		ctmCharDefs = new CustomTableModel(vcHeaders, new Object[0][0]);

		addNewFormTitle(jpnlEditorCharDefs, "Character Classes", gbc);
		jtblCharDefs = new CustomJTable(ctmCharDefs);
		jscrlCharDefsTable = new JScrollPane(jtblCharDefs);
		addNewFormElement(jpnlEditorCharDefs, jscrlCharDefsTable, gbc);

		TableColumn tcClassSolo = jtblCharDefs.getColumnModel().getColumn(6);
		tcClassSolo.setCellEditor(new cellEditorCheckBox());
		tcClassSolo.setCellRenderer(new cellRendererCheckBox());

		// Display class differentiator definitions and data
		addNewFormTitle(jpnlEditorCharDefs, "Class Differentiator", gbc);

		JTextField jtxtClassDiff = new JTextField(Globals.CHARACTER_DIFF_DESC, 16);
		addNewFormElement(jpnlEditorCharDefs, jtxtClassDiff, gbc);

		Vector<String> vcHeaders2 = new Vector<String>();
		vcHeaders2.add("Key");
		vcHeaders2.add("Association");
		ctmClassDiffs = new CustomTableModel(vcHeaders2, new Object[0][0]);

		addNewFormTitle(jpnlEditorCharDefs, "Differentiator Entries", gbc);
		jtblClassDiffs = new CustomJTable(ctmClassDiffs);
		jscrlClassDiffsTable = new JScrollPane(jtblClassDiffs);
		addNewFormElement(jpnlEditorCharDefs, jscrlClassDiffsTable, gbc);

		JPanel jpnlFloatCharDefs = new JPanel();
		jpnlFloatCharDefs.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatCharDefs.add(jpnlEditorCharDefs);

		// ItemClass Collection Editor
		JPanel jpnlEditorItemClasses = new JPanel();
		jpnlEditorItemClasses.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorItemClasses, "Catalogue", gbc);

		// Add table of ItemClass definitions
		Vector<String> vcItemClassHeaders = new Vector<String>();
		vcItemClassHeaders.add("Name");
		vcItemClassHeaders.add("Key");
		vcItemClassHeaders.add("Permissions");
		vcItemClassHeaders.add("Image");
		ctmItemDefs = new CustomTableModel(vcItemClassHeaders, new Object[0][0]);

		addNewFormTitle(jpnlEditorItemClasses, "Item Classes", gbc);
		jtblItemClasses = new CustomJTable(ctmItemDefs);
		jscrlItemClassesTable = new JScrollPane(jtblItemClasses);
		addNewFormElement(jpnlEditorItemClasses, jscrlItemClassesTable, gbc);

		JPanel jpnlFloatItemClasses = new JPanel();
		jpnlFloatItemClasses.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatItemClasses.add(jpnlEditorItemClasses);

		// Item Collection Editor
		JPanel jpnlEditorItems = new JPanel();
		jpnlEditorItems.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorItems, "Collectum", gbc);

		// Add table of Item definitions
		Vector<String> vcItemHeaders = new Vector<String>();
//		vcItemHeaders.add("Type");  // Always 0 for Items
		vcItemHeaders.add("Base Class");
		vcItemHeaders.add("Name");
		vcItemHeaders.add("Cost");
		vcItemHeaders.add("Permissions");
		vcItemHeaders.add("Image");
		vcItemHeaders.add("Charges");
		vcItemHeaders.add("Effect");
		vcItemHeaders.add("Immediate");
		ctmItemCollect = new CustomTableModel(vcItemHeaders, new Object[0][0]);

		addNewFormTitle(jpnlEditorItems, "Items", gbc);
		jtblItems = new CustomJTable(ctmItemCollect, 20);
		jscrlItemsTable = new JScrollPane(jtblItems);
		addNewFormElement(jpnlEditorItems, jscrlItemsTable, gbc);

		String[] srcItemTypes = { "" };
		TableColumn tcItemType = jtblItems.getColumnModel().getColumn(0);
		tcItemType.setCellEditor(new cellEditorComboBox(srcItemTypes));
		tcItemType.setCellRenderer(new cellRendererComboBox(srcItemTypes));

		TableColumn tcItemImmed = jtblItems.getColumnModel().getColumn(7);
		tcItemImmed.setCellEditor(new cellEditorCheckBox());
		tcItemImmed.setCellRenderer(new cellRendererCheckBox());

		JPanel jpnlFloatItems = new JPanel();
		jpnlFloatItems.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatItems.add(jpnlEditorItems);

		// Weapon Collection Editor
		JPanel jpnlEditorWeapons = new JPanel();
		jpnlEditorWeapons.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorWeapons, "Arsenal", gbc);

		// Add table of Weapon definitions
		Vector<String> vcWeaponHeaders = new Vector<String>();
//		vcWeaponHeaders.add("Type"); // Always 1 for Weapons
		vcWeaponHeaders.add("Name");
		vcWeaponHeaders.add("Cost");
		vcWeaponHeaders.add("Permissions");
		vcWeaponHeaders.add("Image");
		vcWeaponHeaders.add("Base Dmg");
		vcWeaponHeaders.add("Ranged");
		vcWeaponHeaders.add("Changeable");
		vcWeaponHeaders.add("Ammo Type");
		vcWeaponHeaders.add("Ammo Cost");
		vcWeaponHeaders.add("Ammo Qty");
		ctmWeapons = new CustomTableModel(vcWeaponHeaders, new Object[0][0]);

		addNewFormTitle(jpnlEditorWeapons, "Weapons", gbc);
		jtblWeapons = new CustomJTable(ctmWeapons, 20);
		jscrlWeaponsTable = new JScrollPane(jtblWeapons);
		addNewFormElement(jpnlEditorWeapons, jscrlWeaponsTable, gbc);

		JPanel jpnlFloatWeapons = new JPanel();
		jpnlFloatWeapons.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatWeapons.add(jpnlEditorWeapons);

		// Armor Collection Editor
		JPanel jpnlEditorArmors = new JPanel();
		jpnlEditorArmors.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorArmors, "Armoury", gbc);

		// Add table of Armor definitions
		Vector<String> vcArmorHeaders = new Vector<String>();
//		vcArmorHeaders.add("Type"); // Always 2 for Armors
		vcArmorHeaders.add("Name");
		vcArmorHeaders.add("Cost");
		vcArmorHeaders.add("Permissions");
		vcArmorHeaders.add("Image");
		vcArmorHeaders.add("Base Prot");
		vcArmorHeaders.add("Body Slot");
		vcArmorHeaders.add("Changeable");
		ctmArmors = new CustomTableModel(vcArmorHeaders, new Object[0][0]);

		addNewFormTitle(jpnlEditorArmors, "Armors", gbc);
		jtblArmors = new CustomJTable(ctmArmors, 20);
		jscrlArmorsTable = new JScrollPane(jtblArmors);
		addNewFormElement(jpnlEditorArmors, jscrlArmorsTable, gbc);

		JPanel jpnlFloatArmors = new JPanel();
		jpnlFloatArmors.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatArmors.add(jpnlEditorArmors);

		// MonsterDef Collection Editor
		JPanel jpnlEditorMonsterDefs = new JPanel();
		jpnlEditorMonsterDefs.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorMonsterDefs, "Bestiary", gbc);

		// Add table of MonsterDef definitions
		Vector<String> vcMonsterDefHeaders = new Vector<String>();
		vcMonsterDefHeaders.add("INDEX");
		vcMonsterDefHeaders.add("Name");
		vcMonsterDefHeaders.add("Specie");
		vcMonsterDefHeaders.add("Hit Dice");
		vcMonsterDefHeaders.add("Attack");
		vcMonsterDefHeaders.add("Ranged");
		vcMonsterDefHeaders.add("Defense");
		vcMonsterDefHeaders.add("Speed");
		vcMonsterDefHeaders.add("Mobility");
		vcMonsterDefHeaders.add("Resistance");
		vcMonsterDefHeaders.add("Negotiation");
/*
		public int     getEffect()        { return specialKey; }
		public int     getEffectPer()     { return specialPer; }
		public boolean getEffectRanged()  { return specialRanged; }
		public int     getExpGrant()      { return expGrant; }
		public Image   getImgNormal()     { return imgNormal; }
		public Image   getImgAttack()     { return imgAttack; }
		public Image   getImgSpecial()    { return imgSpecial; }
		public int     getMaxGroup()      { return maxGroup; }
*/
		ctmMonDefs = new CustomTableModel(vcMonsterDefHeaders, new Object[0][0]);

		addNewFormTitle(jpnlEditorMonsterDefs, "MonsterDefs", gbc);
		jtblMonsterDefs = new CustomJTable(ctmMonDefs, 20);
		jscrlMonsterDefsTable = new JScrollPane(jtblMonsterDefs);
		addNewFormElement(jpnlEditorMonsterDefs, jscrlMonsterDefsTable, gbc);

		JPanel jpnlFloatMonsterDefs = new JPanel();
		jpnlFloatMonsterDefs.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatMonsterDefs.add(jpnlEditorMonsterDefs);

		// Module Graphics Properties Editor
		JPanel jpnlEditorGraphics = new JPanel();
		jpnlEditorGraphics.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorGraphics, "Colors", gbc);
		txtColorMainBackDefault = new JTextField("", 8);
		lblColorMainBackDefault = new JLabel();
		addNewFormColorField(jpnlEditorGraphics, "Main Background Color", txtColorMainBackDefault, lblColorMainBackDefault, "000000", gbc);
		addNewFormTitle(jpnlEditorGraphics, "Font Settings", gbc);
		txtFontMain = new JTextField("", 16);
		addNewLabelledFormElement(jpnlEditorGraphics, "Main Font", txtFontMain, gbc);
		txtFontMainSize = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorGraphics, "Main Font Size (pts)", txtFontMainSize, gbc);
		txtFontMainWidth = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorGraphics, "Main Font Width (px)", txtFontMainWidth, gbc);
		txtFontMainHeight = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorGraphics, "Main Font Height (px)", txtFontMainHeight, gbc);
		txtFontSmall = new JTextField("", 16);
		addNewLabelledFormElement(jpnlEditorGraphics, "Small Font", txtFontSmall, gbc);
		txtFontSmallSize = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorGraphics, "Small Font Size (pts)", txtFontSmallSize, gbc);
		txtFontSmallWidth = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorGraphics, "Small Font Width (px)", txtFontSmallWidth, gbc);
		txtFontSmallHeight = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorGraphics, "Small Font Height (px)", txtFontSmallHeight, gbc);

		JPanel jpnlFloatGraphicProps = new JPanel();
		jpnlFloatGraphicProps.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatGraphicProps.add(jpnlEditorGraphics);

		// Module Level Properties Editor
		JPanel jpnlEditorLevelProps = new JPanel();
		jpnlEditorLevelProps.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridheight = 1; gbc.gridwidth = 1;
		gbc.weightx = 0; gbc.weighty = 0;
		gbc.ipadx = 2; gbc.ipady = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = -1;

		addNewFormTitle(jpnlEditorLevelProps, "Level Properties", gbc);

		txtLevelWidth = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorLevelProps, "Level Width", txtLevelWidth, gbc);
		txtLevelHeight = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorLevelProps, "Level Height", txtLevelHeight, gbc);
		txtRoomsMin = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorLevelProps, "Minimum Rooms", txtRoomsMin, gbc);
		txtRoomsMax = new JTextField("", 4);
		addNewLabelledFormElement(jpnlEditorLevelProps, "Maximum Rooms", txtRoomsMax, gbc);

		addNewFormTitle(jpnlEditorLevelProps, "Level Names", gbc);
		jlistLevelNames = new JList(new Vector(11));
		jlistLevelNames.setVisibleRowCount(11);
		JScrollPane jscrlLevelNames = new JScrollPane(jlistLevelNames, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		addNewFormElement(jpnlEditorLevelProps, jscrlLevelNames, gbc);

		JPanel jpnlFloatLevelProps = new JPanel();
		jpnlFloatLevelProps.setLayout(new FlowLayout(FlowLayout.LEADING));
		jpnlFloatLevelProps.add(jpnlEditorLevelProps);

		// Main Tabbed Component
		tabEditors = new JTabbedPane();
		tabEditors.add("Modules", jpnlFloatModuleList);
		tabEditors.add("Character Defs", jpnlFloatCharDefs);
		tabEditors.add("Item Classes", jpnlFloatItemClasses);
		tabEditors.add("Items", jpnlFloatItems);
		tabEditors.add("Weapons", jpnlFloatWeapons);
		tabEditors.add("Armors", jpnlFloatArmors);
		tabEditors.add("Monster Defs", jpnlFloatMonsterDefs);
		tabEditors.add("Graphic Properties", jpnlFloatGraphicProps);
		tabEditors.add("Level Properties", jpnlFloatLevelProps);
		tabEditors.addChangeListener(this);

		// Assemble the application
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tabEditors, BorderLayout.CENTER);
		this.getContentPane().add(jToolBar, BorderLayout.NORTH);
		this.addKeyListener(this);
		this.addWindowListener(this);
		this.addWindowStateListener(this);
		Dimension dDesktop = getToolkit().getScreenSize();
		this.setSize(dDesktop.width, dDesktop.height);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		this.pack();
		this.setVisible(true);
	}

//  Listeners -------------------------------------------------------------------------------/

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
			else if(command.equals("opendata"))
			{
				String modname = (String)(jlistModules.getSelectedValue());
				if(modname != null && modname.length() > 0)
				{
					loadGameModule(modname);
				}
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

	/* WindowStateListener methods */
	public void windowStateChanged(WindowEvent we)
	{
		if((we.getOldState() == 0 || we.getOldState() == 6) && (we.getNewState() == 0 || we.getNewState() == 6))
		{
//			rescaleAll();
		}
	}

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

//  File Reader Methods ---------------------------------------------------------------------/

	public Vector getModuleFilenames()
	{
		File modsDir = new File(Globals.MODSPATH);
		Vector<String> vcMods = new Vector<String>();
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
			String modName = files[i].getName().substring(0, files[i].getName().indexOf(Globals.DATAEXTN));
			for(int v = 0; v < vcMods.size(); v++)
			{
				if(modName.charAt(0) < (vcMods.elementAt(v).charAt(0)))
				{
					vcMods.insertElementAt(new String(modName), v);
					files[i] = (File)null;
					v = vcMods.size() + 1;
				}
			}
			if(files[i] != null)
			{
				vcMods.add(new String(modName));
			}
		}
		return vcMods;
	}

	public void loadGameModule(String moduleName)
	{
		currModule = moduleName;
		// Load Module-Specific Data (will eventually be moved to body of class, after player selects specific module to play)
		ObjectParser.setModuleName(moduleName);
		//   graphical settings particular to the selected module
		loadModuleDungeonProperties(ObjectParser.getModulePath() + "dungeon.properties");
		//   text mappings particular to the selected module
		loadModuleTextMappings(ObjectParser.getModulePath() + "textmappings.properties");
		//   load Dungeon properties
		ObjectParser.parseDungeonProperties();
		//   load Quests
		ObjectParser.parseModuleQuests();
		//   load Items
		ObjectParser.parseItems();
		//   load character class definitions
		ObjectParser.parseCharacterDefinitionFile();
		//   load monster definitions
		ObjectParser.parseMonsterDefinitions(); 
		// repopulate data fields
		repopulateForms(); // IMPLEMENT - resize tables after repopulation
	}

	// Load module-specific graphic properties
	private void loadModuleDungeonProperties(String propertiesFile)
	{
		try
		{
			dungeonProperties.load(new FileInputStream(new File(propertiesFile)));
			// level size parameters
			LEVEL_WIDTH     = Integer.parseInt(dungeonProperties.getProperty("levelwidth"));  if(LEVEL_WIDTH     > Globals.MAX_LEVEL_WIDTH)  { LEVEL_WIDTH     = Globals.MAX_LEVEL_WIDTH; }
			LEVEL_HEIGHT    = Integer.parseInt(dungeonProperties.getProperty("levelheight")); if(LEVEL_HEIGHT    > Globals.MAX_LEVEL_HEIGHT) { LEVEL_HEIGHT    = Globals.MAX_LEVEL_HEIGHT; }
			LEVEL_ROOMS_MIN = Integer.parseInt(dungeonProperties.getProperty("roomsmin"));    if(LEVEL_ROOMS_MIN < Globals.MIN_ROOMS)        { LEVEL_ROOMS_MIN = Globals.MIN_ROOMS; }
			LEVEL_ROOMS_MAX = Integer.parseInt(dungeonProperties.getProperty("roomsmax"));    if(LEVEL_ROOMS_MAX > Globals.MAX_ROOMS)        { LEVEL_ROOMS_MAX = Globals.MAX_ROOMS; }
			// level name collection (max is 1 overland level & 10 dungeon levels)
			Globals.LEVEL_NAMES.clear();
			for(int i = 0; i <= 10; i++)
			{
				String sKey = "lvl" + (i < 10 ? "0" : "") + i;
				if(dungeonProperties.getProperty(sKey) != null)
				{
					Globals.LEVEL_NAMES.add(dungeonProperties.getProperty(sKey));
				}
				else
				{
					Globals.LEVEL_NAMES.add("");
				}
			}
			// colors
			clrMainBkgr       = new Color(Integer.parseInt(dungeonProperties.getProperty("colormainback"), 16));
			clrRoomBkgr       = new Color(Integer.parseInt(dungeonProperties.getProperty("colorroomback"), 16));
			clrHallBkgr       = new Color(Integer.parseInt(dungeonProperties.getProperty("colorhallback"), 16));
			clrHallWall       = new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolorwall"), 16));
			clrHallDoor       = new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolordoor"), 16));
			clrHallFloor      = new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolorfloor"), 16));
			clrHallCeiling    = new Color(Integer.parseInt(dungeonProperties.getProperty("hallcolorceiling"), 16));
			clrHallOutline    = new Color(Integer.parseInt(dungeonProperties.getProperty("hallcoloroutline"), 16));
			clrActiveMember   = new Color(Integer.parseInt(dungeonProperties.getProperty("coloractivepartymember"), 16));
			clrTextBoxOutline = new Color(Integer.parseInt(dungeonProperties.getProperty("colortextboxoutline"), 16));
			clrTextBoxFill    = new Color(Integer.parseInt(dungeonProperties.getProperty("colortextboxfill"), 16));
			// visualisation options
			shadeStrength = Double.parseDouble(dungeonProperties.getProperty("shadestrength"));
			bOutline      = dungeonProperties.getProperty("outlines").toLowerCase().equals("true");
			// images
			imgFloor      = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgfloor"));
			imgSolid      = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgsolid"));
			imgColumn     = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgcolumn"));
			imgWallH      = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgwallh"));
			imgWallV      = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgwallv"));
			imgDoorH      = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgdoorh"));
			imgDoorV      = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgdoorv"));
			imgStairsU    = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgstairsu"));
			imgStairsD    = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgstairsd"));
			imgFountain   = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgfountain"));
			imgStatue     = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgstatue"));
			imgShop       = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgshop"));
			imgDeadPlayer = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgdeadplayer"));
			imgBackParty  = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgbackparty"));
			imgInterface  = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imginterface"));
			imgCardinals  = ObjectParser.loadModuleImage(dungeonProperties.getProperty("imgcardinals"));
			// Load font colors
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextmenu"), 16)), Globals.FONT_COLOR_MENU);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextstat"), 16)), Globals.FONT_COLOR_STAT);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortexttext"), 16)), Globals.FONT_COLOR_TEXT);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextactv"), 16)), Globals.FONT_COLOR_ACTV);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextinac"), 16)), Globals.FONT_COLOR_INAC);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortextwarn"), 16)), Globals.FONT_COLOR_WARN);
			Globals.CURR_DUNGEON.setColorBank(new Color(Integer.parseInt(dungeonProperties.getProperty("colortexthigh"), 16)), Globals.FONT_COLOR_HIGH);
			// Other values
			offsetCardinalX = Integer.parseInt(dungeonProperties.getProperty("offsetcardinalx"));
			offsetCardinalY = Integer.parseInt(dungeonProperties.getProperty("offsetcardinaly"));
			sizeCardinalX   = Integer.parseInt(dungeonProperties.getProperty("sizecardinalx"));
			sizeCardinalY   = Integer.parseInt(dungeonProperties.getProperty("sizecardinaly"));
		}
		catch(Exception e)
		{
//			System.out.println("Exception loading module graphics properties : " + e.toString());
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}

	// Load module-specific text mappings
	private void loadModuleTextMappings(String mappingsFile)
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

	// Load module-specific level parameters
	private void loadModuleLevelParameters(String moduleDataFile)
	{
		Properties moduleProperties = new Properties();
		try
		{
			moduleProperties.load(new FileInputStream(new File(moduleDataFile)));
			// parameters
		}
		catch(Exception e)
		{
			System.out.println("Exception loading module level parameters : " + e.toString());
			System.exit(1);
		}
	}

	private void repopulateForms()
	{
		Object[][] cdefRows = new Object[Globals.CHARACTER_CLASSES.length][7];
		for(int i = 0; i < Globals.CHARACTER_CLASSES.length; i++)
		{
			CharacterClass cc = Globals.CHARACTER_CLASSES[i];
			cdefRows[i][0] = cc.getClassKey() + "";
			cdefRows[i][1] = cc.getName();
			cdefRows[i][2] = cc.getLevelUpThreshold() + "";
			cdefRows[i][3] = cc.getBaseHitpoints() + "";
			cdefRows[i][4] = cc.getHPGainedPerLevel() + "";
			cdefRows[i][5] = cc.getBaseSpeed() + "";
			cdefRows[i][6] = cc.isSoloModeChar();
		}
		ctmCharDefs.setData(cdefRows);
		jtblCharDefs.rescale();

		Object[][] ckeyRows = new Object[Globals.CHARACTER_DIFF_KEYS.length][2];
		for(int i = 0; i < Globals.CHARACTER_DIFF_KEYS.length; i++)
		{
			ckeyRows[i][0] = Globals.CHARACTER_DIFF_KEYS[i];
			ckeyRows[i][1] = Globals.CHARACTER_DIFF_ASSOCS[i];
		}
		ctmClassDiffs.setData(ckeyRows);
		jtblClassDiffs.rescale();

		Object[][] itemRows = new Object[Globals.CATALOGUE.size()][4];
		int c = 0;
		for(Enumeration e = Globals.CATALOGUE.keys(); e.hasMoreElements();)
		{
			ItemClass ic = (ItemClass)(Globals.CATALOGUE.get(e.nextElement()));
			itemRows[c][0] = ic.getName();
			itemRows[c][1] = ic.getClassKey();
			itemRows[c][2] = ic.getPermissions();
			itemRows[c][3] = ic.getImage().toString();
			c++;
		}
		ctmItemDefs.setData(itemRows);
		jtblItemClasses.rescale();

		Object[][] itemList = new Object[Globals.INDEX_COLLECTUM.size()][8];
		c = 0;
//		for(Enumeration e = Globals.COLLECTUM.keys(); e.hasMoreElements();)
		for(int i = 0; i < Globals.INDEX_COLLECTUM.size(); i++)
		{
//			Item itm = (Item)(Globals.getCollectumItem(e.nextElement().toString(), -1));
			Item itm = Globals.getCollectumItem(Globals.INDEX_COLLECTUM.elementAt(i).toString(), -1);
			Vector vcItemTableRow = new Vector();
			itemList[c][0] = itm.getItemClass().getName();
			itemList[c][1] = itm.getName();
			itemList[c][2] = itm.getCost() + "";
			itemList[c][3] = itm.getPermissions();
			itemList[c][4] = itm.getImage() == null ? "" : itm.getImage().toString();
			itemList[c][5] = itm.getCharges() + "";
			itemList[c][6] = itm.getEffect() + "";
			itemList[c][7] = itm.isImmediate();
			c++;
		}
		ctmItemCollect.setData(itemList);

		String[] srcItemTypes = new String[itemRows.length];
		for(int i = 0; i < itemRows.length; i++)
		{
			srcItemTypes[i] = itemRows[i][0].toString();
		}
		TableColumn tcItemType = jtblItems.getColumnModel().getColumn(0);
		tcItemType.setCellEditor(new cellEditorComboBox(srcItemTypes));
		tcItemType.setCellRenderer(new cellRendererComboBox(srcItemTypes));

		jtblItems.rescale();

		Object[][] itemWpns = new Object[Globals.ARSENAL.size()][10];
		c = 0;
		for(Enumeration e = Globals.ARSENAL.keys(); e.hasMoreElements();)
		{
			Weapon wpn = (Weapon)(Globals.ARSENAL.get(e.nextElement()));
			itemWpns[c][0] = wpn.getName();
			itemWpns[c][1] = wpn.getCost() + "";
			itemWpns[c][2] = wpn.getPermissions();
			itemWpns[c][3] = wpn.getImage() == null ? "" : wpn.getImage().toString();
			itemWpns[c][4] = wpn.getBaseDmg() + "";
			itemWpns[c][5] = (wpn.isRanged() ? "Y" : "N");
			itemWpns[c][6] = (wpn.isThrowable() ? "Y" : "N");
			itemWpns[c][7] = wpn.getAmmoType();
			itemWpns[c][8] = Globals.AMMOLIST.elementAt(wpn.getAmmoType()).getCost() + "";
			itemWpns[c][9] = Globals.AMMOLIST.elementAt(wpn.getAmmoType()).getQuantity() + "";
			c++;
		}
		ctmWeapons.setData(itemWpns);
		jtblWeapons.rescale();

		Object[][] itemArms = new Object[Globals.ARMOURY.size()][7];
		c = 0;
		for(Enumeration e = Globals.ARMOURY.keys(); e.hasMoreElements();)
		{
			Armor arm = (Armor)(Globals.ARMOURY.get(e.nextElement()));
			itemArms[c][0] = arm.getName();
			itemArms[c][1] = arm.getCost() + "";
			itemArms[c][2] = arm.getPermissions();
			itemArms[c][3] = arm.getImage() == null ? "" : arm.getImage().toString();
			itemArms[c][4] = arm.getBaseProt() + "";
			itemArms[c][5] = (arm.isBodyArmor() ? "Y" : "N");
			itemArms[c][6] = (arm.isChangeable() ? "Y" : "N");
			c++;
		}
		ctmArmors.setData(itemArms);
		jtblArmors.rescale();

		Object[][] itemMdefs = new Object[Globals.INDEX_BESTIARY.size()][10];
		c = 0;
		for(int i = 0; i < Globals.INDEX_BESTIARY.size(); i++)
		{
			MonsterDef mdef = (MonsterDef)(Globals.BESTIARY.get(Globals.INDEX_BESTIARY.elementAt(i)));
			itemMdefs[c][0] = (i < 10 ? "0" : "") + i;
			itemMdefs[c][1] = mdef.getName();
			itemMdefs[c][2] = mdef.getLevel() + "";
			itemMdefs[c][3] = mdef.getAttack() + "";
			itemMdefs[c][4] = (mdef.getRanged() ? "Y" : "N");
			itemMdefs[c][5] = mdef.getDefend() + "";
			itemMdefs[c][6] = mdef.getSpeed() + "";
			itemMdefs[c][7] = mdef.getMobility() + "";
			itemMdefs[c][8] = mdef.getResistance() + "";
			itemMdefs[c][9] = mdef.getNegotiation() + "";
			c++;
		}
		ctmMonDefs.setData(itemMdefs);
		jtblMonsterDefs.rescale();

		txtColorMainBackDefault.setText(dungeonProperties.getProperty("colormainback"));
		updateSwatch(lblColorMainBackDefault, dungeonProperties.getProperty("colormainback"));
		txtFontMain.setText(dungeonProperties.getProperty("fontmainttf"));
		txtFontMainSize.setText(Integer.parseInt(dungeonProperties.getProperty("fontsize")) + "");
		txtFontMainWidth.setText(Integer.parseInt(dungeonProperties.getProperty("fontwidth")) + "");
		txtFontMainHeight.setText(Integer.parseInt(dungeonProperties.getProperty("fontheight")) + "");
		txtFontSmall.setText(dungeonProperties.getProperty("fontsmallttf"));
		txtFontSmallSize.setText(Integer.parseInt(dungeonProperties.getProperty("fontsizesmall")) + "");
		txtFontSmallWidth.setText(Integer.parseInt(dungeonProperties.getProperty("fontwidthsmall")) + "");
		txtFontSmallHeight.setText(Integer.parseInt(dungeonProperties.getProperty("fontheightsmall")) + "");

		txtLevelWidth.setText(LEVEL_WIDTH + "");
		txtLevelHeight.setText(LEVEL_HEIGHT + "");
		txtRoomsMin.setText(LEVEL_ROOMS_MIN + "");
		txtRoomsMax.setText(LEVEL_ROOMS_MAX + "");
		jlistLevelNames.setListData(Globals.LEVEL_NAMES);
	}

	private void saveInitData()
	{
		StringBuffer sbInitFile = new StringBuffer();
		sbInitFile.append("* Screen Settings\r\n");
		writeFile(new File(Globals.RSRCPATH + "init" + Globals.DATAEXTN), sbInitFile.toString());
	}

	private void addToHashtable(Hashtable<Object, Object> hashtable, Object key, Object value)
	{
		if(hashtable.containsKey(key))
		{
			hashtable.remove(key);
		}
		hashtable.put(key, value);
	}

//  Class Methods ---------------------------------------------------------------------------/

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
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 3; gbc.weightx = 1;
		JLabel jTitle = new JLabel(label);
		jTitle.setForeground(new Color(128, 64, 64));
		jpanel.add(jTitle, gbc);
	}

	private void addNewFormColorField(JPanel jpanel, String label, JTextField textfield, JLabel jlblSwatch, String clr, GridBagConstraints gbc)
	{
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.weightx = 1;
		jpanel.add(new JLabel(label), gbc);
		gbc.gridx = 1; gbc.weightx = 2;
		jpanel.add(textfield, gbc);
		gbc.gridx = 2; gbc.weightx = 2;
		jlblSwatch.setText("--------");
		jlblSwatch.setOpaque(true);
		jlblSwatch.setBackground(new Color(Integer.parseInt(clr, 16)));
		jlblSwatch.setForeground(new Color(Integer.parseInt(clr, 16)));
		jlblSwatch.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		jpanel.add(jlblSwatch, gbc);
	}

	private void addNewLabelledFormElement(JPanel jpanel, String sLabel, JComponent jelem, GridBagConstraints gbc)
	{
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1; gbc.weightx = 1;
		jpanel.add(new JLabel(sLabel), gbc);
		gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 2;
		jpanel.add(jelem, gbc);
	}

	private void addNewFormElement(JPanel jpanel, JComponent jelem, GridBagConstraints gbc)
	{
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 3; gbc.weightx = 2;
		jpanel.add(jelem, gbc);
	}

	private void updateSwatch(JLabel jlblSwatch, String clr)
	{
		jlblSwatch.setBackground(new Color(Integer.parseInt(clr, 16)));
		jlblSwatch.setForeground(new Color(Integer.parseInt(clr, 16)));
	}

// Convenience Methods ----------------------------------------------------------------------/

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

	private void rescaleAll()
	{
		jtblCharDefs.rescale();
		jtblClassDiffs.rescale();
		jtblItemClasses.rescale();
		jtblItems.rescale();
		jtblWeapons.rescale();
		jtblArmors.rescale();
		jtblMonsterDefs.rescale();
	}

//  Main Method -----------------------------------------------------------------------------/

	public static void main(String[] args)
	{
		ModuleEditor ge = new ModuleEditor("quest");
	}

//  Inner Classes ---------------------------------------------------------------------------/

	class CustomTableModel extends AbstractTableModel implements TableModelListener
	{
		private Vector<String> vcColumnNames;
		private Object[][] rowData;

		public CustomTableModel(Vector<String> vcColumnNames, Object[][] rowData)
		{
			this.vcColumnNames = new Vector<String>(vcColumnNames);
			this.rowData       = rowData;
			this.addTableModelListener(this);
		}

		// overridden methods
		public int     getRowCount()                            { return rowData.length; }
		public int     getColumnCount()                         { return vcColumnNames.size(); }
		public Object  getValueAt(int row, int col)             { return rowData[row][col]; }
		public String  getColumnName(int col)                   { return vcColumnNames.elementAt(col); }
		public boolean isCellEditable(int row, int col)         { return true; }
		public void    setValueAt(Object val, int row, int col) { rowData[row][col] = val; this.fireTableCellUpdated(row, col); }

		// new methods
		public void setData(Object[][] rows) { rowData = rows; this.fireTableDataChanged(); }
		public void appendData(Object[] row) { rowData[rowData.length] = row; this.fireTableDataChanged(); }

		// listener method
		public void tableChanged(TableModelEvent tme) {}
	}

	class CustomJTable extends JTable
	{
		int prefRows = 10;

		// Constructors
		public CustomJTable(TableModel tm, int pr) { super(tm); prefRows = pr; }
		public CustomJTable(TableModel tm)         { super(tm); }

		public void rescale(int width)
		{
			Dimension bestSize = new Dimension(width, this.getRowHeight() * Math.min(this.getRowCount(), prefRows));
			this.setPreferredScrollableViewportSize(bestSize);
		}

		public void rescale()
		{
			Dimension bestSize = new Dimension(this.getParent().getParent().getParent().getParent().getWidth() - 40, this.getRowHeight() * Math.min(this.getRowCount(), prefRows));
			this.setPreferredScrollableViewportSize(bestSize);
		}
	}

	public class cellRendererCheckBox extends JCheckBox implements TableCellRenderer
	{
		public cellRendererCheckBox()
		{
			super();
			setHorizontalAlignment(JLabel.CENTER);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(isSelected)
			{
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			}
			else
			{
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setSelected(value != null && ((Boolean)value).booleanValue());
			return this;
		}
	}

	public class cellEditorCheckBox extends DefaultCellEditor
	{
		public cellEditorCheckBox()
		{
			super(new JCheckBox());
			((JCheckBox)getComponent()).setHorizontalAlignment(JLabel.CENTER);
		}
	}

	public class cellRendererComboBox extends JComboBox implements TableCellRenderer
	{
		public cellRendererComboBox(String[] values)
		{
			super(values);
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(isSelected)
			{
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			}
			else
			{
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setSelectedItem(value);
			return this;
		}
	}

	public class cellEditorComboBox extends DefaultCellEditor
	{
		public cellEditorComboBox(String[] values)
		{
			super(new JComboBox(values));
		}
	}

}

