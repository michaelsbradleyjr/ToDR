package com.dreamcodex.todr.util;

import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import com.dreamcodex.todr.util.TI99FileFinder;

/** TODeditor (C) Howard Kistler
  * "Tunnels of Doom" game data editor utility
  *
  * @author Howard Kistler
  * @version 1.0
  * @creationdate 2/27/2010
  */

public class TODeditor extends JFrame implements WindowListener
{
// Components ----------------------------------------------------------------/

	private JTabbedPane jtabMain;
	private JTextArea jtxtDesc;
	private JTable jtblPlayers;
	private JTable jtblClasses;
	private JTable jtblMonsters;
	private JTable jtblEffects;
	private JTable jtblMelee;
	private JTable jtblRange;
	private JTable jtblArmor;
	private JTable jtblArmorSpec;
	private JTable jtblArtiClasses;
	private JTable jtblArtifacts;
	private JTable jtblQuestItems;
	private JTable jtblTraps;
	private JTable jtblLabels;

// Variables -----------------------------------------------------------------/

	private StringBuffer sbOutput;
	private DataInputStream disReader;
	private int currOffset;

	private Font fntMonoPlain;
	private Font fntMonoBold;
	private int FONTSIZE = 14;

	private int dataRow = 32;

	// Data File Storage Array

	private byte[] dataFile = new byte[13056]; //13056 = size of ToD dungeon file

	// Data Objects In File (used for convenience, could read straight from file array as well)

	private byte[] dataDesc = new byte[dataRow * 12];

// Constructor ---------------------------------------------------------------/

	public TODeditor(String diskName, String fileName, String fileOut)
	{
		super("TOD Editor");

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		try
		{
			String FONTNAME = "CONSOLA.TTF";
			FileInputStream isFontA = new FileInputStream("com" + File.separator + "dreamcodex" + File.separator + "todr" + File.separator + "util" + File.separator + FONTNAME);
			Font fntLoadA = Font.createFont(Font.TRUETYPE_FONT, isFontA);
			fntMonoPlain = fntLoadA.deriveFont(Font.PLAIN, FONTSIZE);
			fntMonoBold = fntLoadA.deriveFont(Font.BOLD, FONTSIZE);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			JButton jbtnJunk = new JButton();
			fntMonoPlain = jbtnJunk.getFont().deriveFont(Font.PLAIN, FONTSIZE);
			fntMonoBold = jbtnJunk.getFont().deriveFont(Font.BOLD, FONTSIZE);
		}

		try
		{
			dataFile = TI99FileFinder.getFileAsBytes(diskName, fileName, dataFile.length);

			if(dataFile.length > 0)
			{
				fetchByteBlock(dataDesc, 0);
				String strDesc = new String(dataDesc);

				String[] headersPlayer = new String[]{ "NAME", "HPs", "WNDS", "ARM#", "ARMV", "SHL#", "SHLV", "WPN1", "DMG1", "AMO1", "WPN2", "DMG2", "AMO2", "????", "WBNS", "LUCK", "????", "EXP1", "????", "EXP2", " LVL", "CLAS", "????", "COPY", "????", "????", "MAG1", "MCH1", "MAG2", "MCH2", "MAG3", "MCH3", "MAG4", "MCH4", "MAG5", "MCH5", "MAG6", "MCH6", "MAG7", "MCH7", "MAG8", "MCH8", "MAG9", "MCH9", "MAG0", "MCH0" };
				ArrayList arrayPlayer = getFieldData("PLAYERS", headersPlayer, 2560, 60, 4, 15);

				String[] headersClasses = new String[]{ "NAME", "BHPS", "COPY", "????", "????", "????", "????", "????", "????", "SITM", "SCHG", "????", "????" };
				ArrayList arrayClasses = getFieldData("CLASSES", headersClasses, 2930, 22, 4, 10);

				String[] headersMonster = new String[]{ "NAME", "HD6s", "DEF", "RANGED?", "ATT", "MAX DMG", "EFFECT %", "EFFECT RANGED?", "EFFECT #", "EFFECT DMG", "SOUND #", "IMAGE #", "RESIST", "MOBIL", "NEGO", "????", "SPEED" };
				ArrayList arrayMonster = getFieldData("MONSTERS", headersMonster, 3274, 22, 56, 12);

				String[] headersEffects = new String[]{ "NAME", "F/X#" };
				ArrayList arrayEffects = getFieldData("SPECIAL ATTACKS", headersEffects, 4506, 16, 20, 15);

				String[] headersMelee = new String[]{ "NAME", "DMG", "GOLD", "PERM" };
				ArrayList arrayMelee = getFieldData("HAND WEAPONS", headersMelee, 8440, 18, 8, 15);

				String[] headersRange = new String[]{ "NAME", " DMG", "GOLD", "PERM", "AMO#", "WTYP", "AMO$", "AMMO" };
				ArrayList arrayRange = getFieldData("RANGED WEAPONS", headersRange, 8584, 34, 6, 15, 7, 13);

				String[] headersArmor = new String[]{ "NAME", "ARMR", "GOLD", "PERM" };
				ArrayList arrayArmor = getFieldData("ARMOR", headersArmor, 8856, 18, 8, 15);

				String[] headersArmorSpec = new String[]{ "NAME", "ARMR", "GOLD", "PERM" };
				ArrayList arrayArmorSpec = getFieldData("SPEC ARMOR", headersArmorSpec, 9000, 18, 6, 15);

				String[] headersArtiClass = new String[]{ "NAME", "IMG1", "IMG2", "IMG3", "IMG4", "PERM" };
				ArrayList arrayArtiClass = getFieldData("ARTIFACT CLASSES", headersArtiClass, 9108, 16, 8, 11);

				String[] headersArtifacts = new String[]{ "NAME", "F/X#", "CHRG", "ELVL" };
				ArrayList arrayArtifacts = getFieldData("ARTIFACTS", headersArtifacts, 9236, 18, 40, 15);

				String[] headersQuestItems = new String[]{ "NAME", "????", "DPTH", "IMG1", "IMG2", "IMG3", "IMG4", "????", "TURN" };
				ArrayList arrayQuestItems = getFieldData("QUEST ITEMS", headersQuestItems, 10006, 19, 8, 11);

				String[] headersTraps = new String[]{ "NAME", "F/X", "DIFFICULTY" };
				ArrayList arrayTraps = getFieldData("TRAPS", headersTraps, 10182, 12, 10, 10);

				String[] headersLabels = new String[]{ "NAME" };
				ArrayList arrayLabels = getFieldData("LABELS", headersLabels, 11344, 16, 36, 16);

				jtabMain = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

				jtxtDesc = getTextComponent(getShowText(strDesc), 12, dataRow);
				jtabMain.addTab("Description", getScrollingTextComponent("Module Description", jtxtDesc));

				jtblPlayers = getTableComponent(headersPlayer, arrayPlayer);
				jtabMain.addTab("Players", getScrollingTableComponent(jtblPlayers));

				jtblClasses = getTableComponent(headersClasses, arrayClasses);
				jtabMain.addTab("Classes", getScrollingTableComponent(jtblClasses));

				jtblMonsters = getTableComponent(headersMonster, arrayMonster);
				jtabMain.addTab("Monsters", getScrollingTableComponent(jtblMonsters));

				jtblEffects = getTableComponent(headersEffects, arrayEffects);
				jtabMain.addTab("Effects", getScrollingTableComponent(jtblEffects));

				jtblMelee = getTableComponent(headersMelee, arrayMelee);
				jtabMain.addTab("Melee Wpns", getScrollingTableComponent(jtblMelee));

				jtblRange = getTableComponent(headersRange, arrayRange);
				jtabMain.addTab("Ranged Wpns", getScrollingTableComponent(jtblRange));

				jtblArmor = getTableComponent(headersArmor, arrayArmor);
				jtabMain.addTab("Armor", getScrollingTableComponent(jtblArmor));

				jtblArmorSpec = getTableComponent(headersArmorSpec, arrayArmorSpec);
				jtabMain.addTab("Spec Armor", getScrollingTableComponent(jtblArmorSpec));

				jtblArtiClasses = getTableComponent(headersArtiClass, arrayArtiClass);
				jtabMain.addTab("Item Types", getScrollingTableComponent(jtblArtiClasses));

				jtblArtifacts = getTableComponent(headersArtifacts, arrayArtifacts);
				jtabMain.addTab("Artifacts", getScrollingTableComponent(jtblArtifacts));

				jtblQuestItems = getTableComponent(headersQuestItems, arrayQuestItems);
				jtabMain.addTab("Quest Items", getScrollingTableComponent(jtblQuestItems));

				jtblTraps = getTableComponent(headersTraps, arrayTraps);
				jtabMain.addTab("Traps", getScrollingTableComponent(jtblTraps));

				jtblLabels = getTableComponent(headersLabels, arrayLabels);
				jtabMain.addTab("Labels", getScrollingTableComponent(jtblLabels));

				this.setLayout(new BorderLayout());
				this.add(jtabMain, BorderLayout.CENTER);
			}
		}
		catch(Exception e) { e.printStackTrace(System.out); }

		this.addWindowListener(this);
		this.pack();
		this.setVisible(true);
/*

		System.exit(0);

		try
		{
			int filestart = TI99FileFinder.getFileEntryStart(diskName, fileName);
			if(filestart > -1)
			{
				sbOutput = new StringBuffer(1024);
				disReader = new DataInputStream(new FileInputStream(new File(diskName)));
				currOffset = 0;
				printLine("TUNNELS OF DOOM DATA FILE PARSE");
				String[] headersPlayer = new String[]{ "NAME", " HPs", "WNDS", "ARM#", "ARMV", "SHL#", "SHLV", "WPN1", "DMG1", "AMO1", "WPN2", "DMG2", "AMO2", "    ", "WBNS", "LUCK", "    ", "EXP1", "    ", "EXP2", " LVL", "CLAS", "    ", "COPY", "    ", "    ", "MAG1", "MCH1", "MAG2", "MCH2", "MAG3", "MCH3", "MAG4", "MCH4", "MAG5", "MCH5", "MAG6", "MCH6", "MAG7", "MCH7", "MAG8", "MCH8", "MAG9", "MCH9", "MAG0", "MCH0" };
				parseField("PLAYERS", headersPlayer, filestart + 2560, 60, 4, 15);
				String[] headersClasses = new String[]{ "NAME", "BHPS", "COPY", "    ", "    ", "    ", "    ", "    ", "    ", "SITM", "SCHG", "    ", "    "};
				parseField("CLASSES", headersClasses, filestart + 2930, 22, 4, 10);
				String[] headersMonsters = new String[]{ "NAME", "HD6s", "DEFN", "ARNG", "ATTK", "MDMG", "SPC%", "SRNG", "SPC#", "SDMG", "SOUN", "IMG#", "RSTN", "MOBL", "NEGO", "????", "SPED" };
				parseField("MONSTERS", headersMonsters, filestart + 3274, 22, 56, 12);
				String[] headersSpecAttacks = new String[]{ "NAME", "F/X#" };
				parseField("SPECIAL ATTACKS", headersSpecAttacks, currOffset, 16, 20, 15); // must follow monster parsing, offset is based on monster list size
				String[] headersWeaponsHand = new String[]{ "NAME", " DMG", "GOLD", "PERM" };
				parseField("HAND WEAPONS", headersWeaponsHand, filestart + 8440, 18, 8, 15);
				String[] headersWeaponsRanged = new String[]{ "NAME", " DMG", "GOLD", "PERM", "AMO#", "WTYP", "AMO$", "AMMO" };
				parseField("RANGED WEAPONS", headersWeaponsRanged, filestart + 8440, 34, 6, 15, 7, 13);
				printLine(""); printLine("WTYP : 0 = infinite ammo ranged  -1 = standard ranged  -2 = throwable");
				String[] headersArmor = new String[]{ "NAME", "ARMR", "GOLD", "PERM" };
				parseField("ARMOR", headersArmor, filestart + 8712, 18, 8, 15);
				String[] headersArmorSpecial = new String[]{ "NAME", "ARMR", "GOLD", "PERM" };
				parseField("SPECIAL ARMOR", headersArmorSpecial, filestart + 8856, 18, 6, 15);
				String[] headersMagicItems = new String[]{ "NAME", "IMG1", "IMG2", "IMG3", "IMG4", "    " };
				parseField("ARTIFACT CLASSES", headersMagicItems, filestart + 8964, 16, 8, 11);
				String[] headersArtifacts = new String[]{ "NAME", "F/X#", "CHRG", "ELVL" };
				parseField("ARTIFACTS", headersArtifacts, currOffset, 18, 40, 15);
				String[] headersQuestItems = new String[]{ "NAME", "    ", "DPTH", "IMG1", "IMG2", "IMG3", "IMG4", "    ", "TURN" }; // DPTH = preferred item depth, TURN = # of turns per dungeon level to find this item (0 == unlimited)
				parseField("QUEST ITEMS", headersQuestItems, filestart + 9862, 19, 8, 11);
				String[] headersTraps = new String[]{ "NAME", "FX#?", "    " };
				parseField("TRAPS", headersTraps, filestart + 10038, 12, 10, 10);
				String[] headersLabels = new String[]{ "NAME" };
				parseField("LABELS", headersLabels, filestart + 11264, 12, 36, 12);
				if(!(new File(fileOut).exists())) { new File(fileOut).createNewFile(); }
				FileWriter fw = new FileWriter(fileOut);
				fw.write(sbOutput.toString(), 0, sbOutput.length());
				fw.flush();
				fw.close();
			}
			else
			{
				System.out.println("Cannot find ToD game file with the name " + fileName);
				System.exit(1);
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		System.exit(0);
*/
	}

// Component Methods ---------------------------------------------------------/

	protected JTextArea getTextComponent(String txt, int rows, int cols)
	{
		JTextArea jtxtComp = new JTextArea(txt);
		jtxtComp.setRows(rows);
		jtxtComp.setColumns(cols);
		jtxtComp.setLineWrap(true);
		jtxtComp.setWrapStyleWord(true);
		jtxtComp.setOpaque(true);
//		jtxtComp.setBackground(bkgrnd);
//		jtxtComp.setBorder(BORDER_GRID);
		jtxtComp.setPreferredSize(new Dimension(jtxtComp.getPreferredSize().width + 8, jtxtComp.getPreferredSize().height + 8));
		if(fntMonoPlain != null) { jtxtComp.setFont(fntMonoPlain); }
		return jtxtComp;
	}

	protected JScrollPane getScrollingTextComponent(String label, JTextArea jtxt)
	{
		JPanel jpnlLayout = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill       = GridBagConstraints.NONE;
		gbc.anchor     = GridBagConstraints.NORTH;
		gbc.weightx    = 0.0;
		gbc.weighty    = 0.0;
		gbc.gridx      = 1;
		gbc.gridy      = 1;
		gbc.gridwidth  = 1;
		gbc.gridheight = 1;
		JLabel jlblTitle = new JLabel(label);
		if(fntMonoBold != null) { jlblTitle.setFont(fntMonoBold); }
		jpnlLayout.add(jlblTitle, gbc);
		gbc.gridy      = 2;
		jpnlLayout.add(jtxt, gbc);
		gbc.fill       = GridBagConstraints.BOTH;
		gbc.weightx    = 2.0;
		gbc.weighty    = 2.0;
		gbc.gridx      = 2;
		gbc.gridy      = 1;
		gbc.gridheight = 2;
		gbc.gridwidth  = GridBagConstraints.REMAINDER;
		jpnlLayout.add(new JLabel(""), gbc);
		gbc.gridx      = 1;
		gbc.gridy      = 3;
		gbc.gridwidth  = 2;
		gbc.gridheight = 1;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		return new JScrollPane(jpnlLayout);
	}

	public JTable getTableComponent(String[] columnHeads, ArrayList tableData)
	{
		JTable jtblData = new JTable(new CollateTableModel(columnHeads, tableData));
		jtblData.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		if(fntMonoPlain != null) { jtblData.setFont(fntMonoPlain); }
		return jtblData;
	}

	protected JScrollPane getScrollingTableComponent(JTable jtbl)
	{
		return new JScrollPane(jtbl);
	}

	protected JComboBox getSelector(String[] options, String selopt)
	{
		JComboBox jcmbSel = new JComboBox(options);
		jcmbSel.setSelectedItem(selopt);
//		jcmbSel.addItemListener(this);
		if(fntMonoPlain != null) { jcmbSel.setFont(fntMonoPlain); }
		return jcmbSel;
	}

// Parser Methods ------------------------------------------------------------/

	protected ArrayList getFieldData(String blockName, String[] headers, int blockStart, int entrySize, int entryCount, int nameSize, int namedSubFieldOffset, int namedSubFieldSize)
	{
		ArrayList arlAllRows = new ArrayList(entryCount);
		try
		{
			int subFields = entrySize - nameSize;
			byte[] dataBytes = new byte[entrySize * entryCount];
			fetchByteBlock(dataBytes, blockStart);
			int offset = 0;
			for(int l = 0; l < entryCount; l++)
			{
				ArrayList arlRow = new ArrayList();
				int subField = 1;
				int subFieldCount = 0;
				StringBuffer sbText = new StringBuffer();
				// weird variable size in LABELS - first four entries are 16 bytes, the rest are 12 bytes
				if(blockName.equals("LABELS") && l > 3)
				{
					entrySize = 12;
					nameSize = 12;
				}
				for(int i = 0; i < entrySize; i++)
				{
					if((i < (entrySize - subFields)))
					{
						sbText.append((char)(dataBytes[(offset) + i]));
					}
					else if(subField == namedSubFieldOffset)
					{
						if(subFieldCount == 0)
						{
							arlRow.add(new String(sbText.toString()));
							sbText.delete(0, sbText.length());
						}
						sbText.append((char)(dataBytes[(offset) + i]));
						subFieldCount++;
						if(subFieldCount >= namedSubFieldSize)
						{
							subField++;
						}
					}
					else
					{
						if(sbText.length() > 0)
						{
							arlRow.add(new String(sbText.toString()));
							sbText.delete(0, sbText.length());
						}
						if(blockName.equals("MONSTERS") && i == 14)
						{
							// get RANGED ATTACK flag (high bit)
							boolean rangedatt = ((dataBytes[(offset) + i] & 128) > 0);
							arlRow.add(new Boolean(rangedatt));
							// get ATTACK ranking
							int attackval = Math.abs(dataBytes[(offset) + i]);
							arlRow.add(new Integer(attackval));
						}
						else if(blockName.equals("MONSTERS") && i == 17)
						{
							// get RANGED SPECIAL flag (high bit)
							boolean rangedspc = ((dataBytes[(offset) + i] & 128) > 0);
							arlRow.add(new Boolean(rangedspc));
							// get SPECIAL ATTACK ranking
							int spcattval = Math.abs(dataBytes[(offset) + i]);
							arlRow.add(new Integer(spcattval));
						}
						else if(blockName.equals("MONSTERS") && i == 18)
						{
							// process special effect power level
							int spcpowlvl = Math.abs(dataBytes[(offset) + i]);
							arlRow.add(new Integer(spcpowlvl));
						}
						else if(blockName.equals("MONSTERS") && i == 19)
						{
							// get SOUND bits
							int soundbytes = (dataBytes[(offset) + i] & 15);
							arlRow.add(new Integer(soundbytes));
							// get IMAGE # bits
							int imgnbits = (dataBytes[(offset) + i] & 241);
							int imgnbyte = (imgnbits >> 4);
							arlRow.add(new Integer(imgnbyte));
						}
						else if(blockName.equals("MONSTERS") && i == 20)
						{
							// get RESISTANCE bits
							int resistance = (dataBytes[(offset) + i] & (byte)15) * 10;
							arlRow.add(new Integer(resistance));
							// get MOBILITY bits
							int basemob = (dataBytes[(offset) + i] & (byte)48);
							int mobility = ((basemob >> 4) * 25) + 25;
							arlRow.add(new Integer(mobility));
							// get NEGOTIATION bits
							int basenego = (dataBytes[(offset) + i] & 192);
							int negobytes = (basenego >> 6) * 25;
							arlRow.add(new Integer(basenego));
						}
						else if(blockName.equals("MONSTERS") && i == 21)
						{
							// get mystery bits
							int unknownbytes = (dataBytes[(offset) + i] & 15);
							arlRow.add(new Integer(unknownbytes));
							// get SPEED bits
							// high bit appears to be unused, as it is 0 in all original monster listings, but we check it anyway
							int basespeed = (dataBytes[(offset) + i] & 240);
							int speed = (basespeed >> 4);
							arlRow.add(new Integer(speed));
						}
						else if(blockName.equals("HAND WEAPONS") && i == 17)
						{
							// FIGHTERS & HEROES can use every weapon, so check only for ROGUE & WIZARD flags
							boolean rogueOK  = false;
							boolean wizardOK = false;
							if((dataBytes[(offset) + i] & 1) == 1) { rogueOK = true; }
							if((dataBytes[(offset) + i] & 128) == 128) { wizardOK = true; }
							if(rogueOK && wizardOK) { arlRow.add("*   "); }
							else if(rogueOK)        { arlRow.add("HFR "); }
							else if(wizardOK)       { arlRow.add("HFW "); }
							else                    { arlRow.add("HF  "); }
						}
						else if(blockName.equals("RANGED WEAPONS") && i == 17)
						{
							// FIGHTERS & HEROES can use every weapon, so check only for ROGUE & WIZARD flags
							boolean rogueOK  = false;
							boolean wizardOK = false;
							if((dataBytes[(offset) + i] & 1) == 1) { rogueOK = true; }
							if((dataBytes[(offset) + i] & 128) == 128) { wizardOK = true; }
							if(rogueOK && wizardOK) { arlRow.add("*   "); }
							else if(rogueOK)        { arlRow.add("HFR "); }
							else if( wizardOK)      { arlRow.add("HFW "); }
							else                    { arlRow.add("HF  "); }
						}
						else if(blockName.equals("ARMOR") && i == 17)
						{
							// FIGHTERS & HEROES can use every armor, so check only for ROGUE & WIZARD flags
							boolean rogueOK  = false;
							boolean wizardOK = false;
							if((dataBytes[(offset) + i] & 2) == 2) { rogueOK = true; }
							if((dataBytes[(offset) + i] & 128) == 128) { wizardOK = true; }
							if(rogueOK && wizardOK) { arlRow.add("*   "); }
							else if(rogueOK)        { arlRow.add("HFR "); }
							else if( wizardOK)      { arlRow.add("HFW "); }
							else                    { arlRow.add("HF  "); }
						}
						else if(blockName.equals("SPEC ARMOR") && i == 17)
						{
							// FIGHTERS & HEROES can use every armor, so check only for ROGUE & WIZARD flags
							boolean rogueOK  = false;
							boolean wizardOK = false;
							if((dataBytes[(offset) + i] & 64) == 64) { rogueOK = true; }
							if((dataBytes[(offset) + i] & 128) == 128) { wizardOK = true; }
							if(rogueOK && wizardOK) { arlRow.add("*   "); }
							else if(rogueOK)        { arlRow.add("HFR "); }
							else if( wizardOK)      { arlRow.add("HFW "); }
							else                    { arlRow.add("HF  "); }
						}
						else if(blockName.equals("ARTIFACT CLASSES") && i == 15)
						{
							// items are WIZARD & HERO only, or everyone
							boolean everyoneOK = true;
							if((dataBytes[(offset) + i] & 128) == 128) { everyoneOK = false; }
							if(everyoneOK) { arlRow.add("*   "); }
							else           { arlRow.add("HW  "); }
						}
						else
						{
							arlRow.add(dataBytes[(offset) + i]);
						}
						subField++;
					}
				}
				if(sbText.length() > 0)
				{
					arlRow.add(new String(sbText.toString()));
					sbText.delete(0, sbText.length());
				}
				arlAllRows.add(arlRow);
				offset += entrySize;
			}
		}
		catch(IOException ioe) { ioe.printStackTrace(System.out); System.exit(1); }
		return arlAllRows;
	}

	protected ArrayList getFieldData(String blockName, String[] headers, int blockStart, int entrySize, int entryCount, int nameSize)
	{
		return getFieldData(blockName, headers, blockStart, entrySize, entryCount, nameSize, 0, 0);
	}

	protected void fetchByteBlock(byte[] storage, int blockStart)
	throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(dataFile);
		bais.reset();
		bais.skip(blockStart);
		bais.read(storage, 0, storage.length);
	}

	public String getShowText(String realtext)
	{
		if(realtext != null && realtext.length() > 0)
		{
			StringBuffer sbShow = new StringBuffer();
			int colcount = 0;
			for(int ch = 0; ch < realtext.length(); ch++)
			{
				sbShow.append(realtext.charAt(ch));
				colcount++;
				if(colcount == dataRow)
				{
					sbShow.append("\n\r");
					colcount = 0;
				}
			}
			return sbShow.toString();
		}
		return "";
	}

	public String getRealText(String showtext)
	{
		return showtext;
	}

// Listener Methods -----------------------------------------------------------/

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

// Inner Classes -------------------------------------------------------------/

	class CollateTableModel extends AbstractTableModel
	{
		private String[] columnNames;
		private ArrayList data;

		public CollateTableModel(String[] columnHeads, ArrayList tableData)
		{
			columnNames = columnHeads;
			data = tableData;
		}

		public CollateTableModel()
		{
			columnNames = new String[]{ "Include?", "Division", "Op Unit", "Submitter(s)", "Date" };
			ArrayList blankRow = new ArrayList(5);
			blankRow.add(0, new Boolean(false));
			blankRow.add(1, "[blank]");
			blankRow.add(2, "[blank]");
			blankRow.add(3, "[blank]");
			blankRow.add(4, "[blank]");
			data = new ArrayList(1);
			data.add(0, blankRow);
		}

		public int getColumnCount() { return columnNames.length; }
		public int getRowCount()    { return data.size(); }

		public String getColumnName(int col) { if(col < ((ArrayList)(data.get(0))).size()) { return columnNames[col]; } else { return "[undef]"; } }

		public Class getColumnClass(int col) { if(col < ((ArrayList)(data.get(0))).size()) { return getValueAt(0, col).getClass(); } else { return Object.class; } }

		public boolean isCellEditable(int row, int col) { return true; }

		public Object getValueAt(int row, int col) { if(row < data.size() && col < ((ArrayList)(data.get(row))).size())  { return ((ArrayList)(data.get(row))).get(col); } else { return "[outofbounds]"; } }

		public void setValueAt(Object val, int row, int col)
		{
			((ArrayList)(data.get(row))).set(col, val);
			fireTableCellUpdated(row, col);
		}

		public void purge()
		{
			data.clear();
		}
	}

// Main Method ---------------------------------------------------------------/

	public static void main(String[] args)
	{
		if(args.length < 3)
		{
			System.out.println("usage: java com.dreamcodex.todr.util.TODeditor DISK FILENAME OUTFILE");
			System.out.println("          DISK     = source .dsk disk image file");
			System.out.println("          FILENAME = name of ToD game/save file to parse");
			System.out.println("          OUTFILE  = parsed results file to create");
			System.exit(0);
		}
		else
		{
			TODeditor todeditor = new TODeditor(args[0], args[1], args[2]);
		}
	}
}
