package com.dreamcodex.todr.util;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Properties;
import javax.imageio.ImageIO;

import com.dreamcodex.todr.util.TI99FileFinder;
import com.dreamcodex.todr.util.TODimager;

/** TODconverter (C) Howard Kistler
  * ToD original game data -> ToDR conversion utility
  *
  * @author Howard Kistler
  * @version 1.0
  * @creationdate 10/15/2008
  */

/*
	Items left to do:
	- Set module names for Rations, Healing, Item Classes, and any other customised name property
	- Sounds could be set in the same way as images - create a pool up front of number samples and then map them based on their settings in the game file

	User items:
	1) Weapons default to one sound per class, will need to assign other sounds if desired
	2) All ranged weapons and items default to the same projectile, users will need to customise
*/

public class TODconverter implements ImageObserver
{
	private StringBuffer sbOutput;
	private DataInputStream disReader;
	private int currOffset;
	private Hashtable<String, String> htItemClassKey = new Hashtable<String, String>();
	private Vector<String> vcItemClass = new Vector<String>();
	private Vector<String> vcArmorBod  = new Vector<String>();
	private Vector<String> vcArmorSpc  = new Vector<String>();
	private Vector<String> vcAmmo      = new Vector<String>();
	private Vector<String> vcAmmoName  = new Vector<String>();
	private Vector<String> vcMonsters  = new Vector<String>();
	private Vector<String> vcMonstName = new Vector<String>();
	private Vector<String> vcEffects   = new Vector<String>();

	private final String[] CHARKEY = { "F", "W", "R", "H" };
	private final String[] CHARVAL =
	{
		"4,2,0,20,10,0,0,0,5,0,2,1,1,4,1,5,1,6,1,5,1,1,0",
		"4,2,0,0,20,0,0,0,10,10,2,1,3,1,1,10,1,8,1,10,1,1,0",
		"4,2,0,15,15,0,0,0,5,20,2,1,1,4,1,8,1,5,1,8,5,1,0",
		"5,2,0,15,15,0,0,0,10,20,2,1,2,2,1,6,1,6,1,6,3,1,1"
	};
	private final int[] EXP = { 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 650, 750, 850, 1000 };

	private Hashtable<String, String> htEffectMaps = new Hashtable<String, String>();
	{
		htEffectMaps.put(  "0", "PHPS|+|S");
		htEffectMaps.put(  "1", "PHPS|-|P");
		htEffectMaps.put(  "4", "PDMG|+|P");
		htEffectMaps.put(  "5", "PDMG|-|P");
		htEffectMaps.put(  "8", "PPRT|+|P");
		htEffectMaps.put(  "9", "PPRT|-|P");
		htEffectMaps.put( "12", "PWDM|+|P");
		htEffectMaps.put( "13", "PWDM|-|P");
		htEffectMaps.put( "15", "PWDM|-|P");
		htEffectMaps.put( "16", "PABN|+|P");
		htEffectMaps.put( "17", "PLUK|-|A");
		htEffectMaps.put( "20", "PMBN|+|P");
		htEffectMaps.put( "21", "PLUK|-|P");
		htEffectMaps.put( "24", "PLUK|+|P");
		htEffectMaps.put( "25", "PLUK|-|P");
		htEffectMaps.put( "28", "PEXP|+|S");
		htEffectMaps.put( "29", "PEXP|-|S");
		htEffectMaps.put( "32", "PEXP|+|P");
		htEffectMaps.put( "33", "PEXP|-|P");
		htEffectMaps.put( "40", "PDMG|+|A");
		htEffectMaps.put( "41", "PDMG|-|A");
		htEffectMaps.put( "49", "POLY|0|P");
		htEffectMaps.put( "52", "WAVL|+|@");
		htEffectMaps.put( "60", "PSPD|+|A");
		htEffectMaps.put( "64", "WNDR|+|@");
		htEffectMaps.put( "65", "WNDR|-|@");
		htEffectMaps.put( "68", "RTNI|+|@");
		htEffectMaps.put( "73", "HLGI|-|@");
		htEffectMaps.put( "76", "MDEF|+|M");
		htEffectMaps.put( "77", "MDEF|-|Z");
		htEffectMaps.put( "80", "MATC|+|M");
		htEffectMaps.put( "81", "MATC|-|Z");
		htEffectMaps.put( "89", "MPER|-|Z");
		htEffectMaps.put( "92", "MBRB|+|Z");
		htEffectMaps.put( "97", "MMOB|-|Z");
		htEffectMaps.put("101", "MMRS|-|Z");
		htEffectMaps.put("109", "MDMG|+|Z");
		htEffectMaps.put("112", "MDMG|-|S");
		htEffectMaps.put("113", "MDMG|+|M");
		htEffectMaps.put("116", "SHWM|0|@");
		htEffectMaps.put("120", "SHWT|0|@");
		htEffectMaps.put("121", "RMVT|0|@");
	}

	private final String[] ITEMTYPES = { "T", "L", "P", "S", "S", "W", "S", "S" };

	MediaTracker mtrack;

	public TODconverter(String diskName, String fileName, String pathOut)
	{
//		htItemClassKey.put("TOUCHSTONE", "T");
//		htItemClassKey.put("LANTERN", "L");
//		htItemClassKey.put("POTION", "P");
//		htItemClassKey.put("SCROLL", "S");
//		htItemClassKey.put("WAND", "W");

		mtrack = new MediaTracker(new Frame());

		TODimager todimg = new TODimager(diskName, fileName, pathOut + "rawimages" + (pathOut.length() > 0 ? pathOut.charAt(pathOut.length() - 1) : File.separator));

		while(todimg.isProcessing()) { try { Thread.sleep(250); } catch(InterruptedException ie) {} }

		try
		{
			int filestart = TI99FileFinder.getFileStartPosition(diskName, fileName);
			if(filestart > -1)
			{
				sbOutput = new StringBuffer(1024);
				disReader = new DataInputStream(new FileInputStream(new File(diskName)));
				currOffset = 0;
				printLine(sbOutput, "TUNNELS OF DOOM DATA FILE PARSE");

				StringBuffer sbQuest = new StringBuffer(1024);
				parseQuestDescription(sbQuest, filestart);

//				String[] headersPlayer = new String[]{ "NAME", " HPs", "WNDS", "ARM#", "ARMV", "SHL#", "SHLV", "WPN1", "DMG1", "AMO1", "WPN2", "DMG2", "AMO2", "    ", "WBNS", "LUCK", "    ", "EXP1", "    ", "EXP2", " LVL", "CLAS", "    ", "COPY", "    ", "    ", "MAG1", "MCH1", "MAG2", "MCH2", "MAG3", "MCH3", "MAG4", "MCH4", "MAG5", "MCH5", "MAG6", "MCH6", "MAG7", "MCH7", "MAG8", "MCH8", "MAG9", "MCH9", "MAG0", "MCH0" };
				parseField(sbOutput, "PLAYER:", filestart + 2560, 60, 4, 15);

//				String[] headersClasses = new String[]{ "NAME", "BHPS", "COPY", "    ", "    ", "    ", "    ", "    ", "    ", "SITM", "SCHG", "    ", "    "};
				StringBuffer sbCharDefs = new StringBuffer(1024);
				parseCharDefs(sbCharDefs, "CC", filestart + 2930, 22, 4, 10, fileName);
				FileWriter fwm1 = new FileWriter(new File(pathOut + "chardefs.todr"));
				fwm1.write(sbCharDefs.toString(), 0, sbCharDefs.length());
				String charLabel = "CA:Color" + System.getProperty("line.separator");
				fwm1.write(charLabel, 0, charLabel.length());
				String charDist1 = "CL:blue,Blue" + System.getProperty("line.separator");
				fwm1.write(charDist1, 0, charDist1.length());
				String charDist2 = "CL:green,Green" + System.getProperty("line.separator");
				fwm1.write(charDist2, 0, charDist2.length());
				String charDist3 = "CL:red,Red" + System.getProperty("line.separator");
				fwm1.write(charDist3, 0, charDist3.length());
				String charDist4 = "CL:purple,Purple" + System.getProperty("line.separator");
				fwm1.write(charDist4, 0, charDist4.length());
				fwm1.flush();
				fwm1.close();

//				String[] headersMonsters = new String[]{ "NAME", "HD6s", "DEFN", "ARNG", "ATTK", "MDMG", "SPC%", "SRNG", "SPC#", "SDMG", "SOUN", "IMG#", "RSTN", "MOBL", "NEGO", "????", "SPED" };
				parseMonsters("MD", filestart + 3274, 22, 56, 12, fileName);

				StringBuffer sbItems = new StringBuffer(1024);
//				String[] headersSpecAttacks = new String[]{ "NAME", "F/X#" };
				parseEffects(filestart + 4506, 16, 20, 15);

				// write out the monsters with their effect values from the effect list
				FileWriter fwm2 = new FileWriter(new File(pathOut + "monsterdefs.todr"));
				for(int i = 0; i < vcMonsters.size(); i++)
				{
					String lineout = vcMonsters.elementAt(i) + System.getProperty("line.separator");
					String effectCode = vcMonsters.elementAt(i).substring(vcMonsters.elementAt(i).lastIndexOf("("));
					if(!(effectCode.equals("(-)")))
					{
						int    effectNum   = Integer.parseInt(effectCode.substring(1, effectCode.indexOf("|"))) - 1;
						int    effectPower = Integer.parseInt(effectCode.substring(effectCode.indexOf("|") + 1, effectCode.indexOf(")")));
						String effectName = vcEffects.elementAt(effectNum).substring(0, vcEffects.elementAt(effectNum).indexOf(","));
						int    effectInt  = Integer.parseInt(vcEffects.elementAt(effectNum).substring(vcEffects.elementAt(effectNum).indexOf(",") + 1));
						String effectMap    = htEffectMaps.get(effectInt + "");
						if(effectMap.startsWith("PEXP|")) { effectPower = effectPower * 100; } // I didn't code the x100 assumption in my game logic, so needs to be done here
						lineout = vcMonsters.elementAt(i).substring(0, vcMonsters.elementAt(i).lastIndexOf("(")) + "(" + effectName + "|" + effectMap + "|" + effectPower + ")" + System.getProperty("line.separator");
					}
					fwm2.write(lineout, 0, lineout.length());
				}
				fwm2.flush();
				fwm2.close();

				// fixed values
				printLine(sbItems, "* Item Constants & Shop Settings");
				printLine(sbItems, "IM:5");
				printLine(sbItems, "IPW:60|60|60|60|150|150|150|150|160|160|160");
				printLine(sbItems, "IPA:100|100|100|100|150|150|150|150|200|200|200");
				printLine(sbItems, "IR:10|20");
				printLine(sbItems, "IH:40|100");
				printLine(sbItems, "* Base Item Classes");
				printLine(sbItems, "IG:MAP,M,Map,*,itemclass_map.png,icon.png,1");
				printLine(sbItems, "IG:CURRENCY,C,Currency,*,itemclass_currency.png,icon.png,2");
				printLine(sbItems, "IG:RATION,R,Ration,*,itemclass_rations.png,icon.png,3");
				printLine(sbItems, "IG:WPNMELEE,0,Melee Weapon,*,itemclass_weapon_melee.png,icon_weapon_melee.png,4");
				printLine(sbItems, "IG:WPNRANGE,1,Ranged Weapon,*,itemclass_weapon_ranged.png,icon_weapon_ranged.png,5");
				printLine(sbItems, "IG:ARMRBODY,2,Body Armor,*,itemclass_armor_body.png,icon_armor_body.png,8");
				printLine(sbItems, "IG:ARMRSPEC,3,Special Armor,*,itemclass_armor_special.png,icon_armor_special.png,7");
				printLine(sbItems, "IG:AMMO,X,Ammo,*,itemclass_ammo.png,icon_ammo.png,6");

				printLine(sbItems, "* Standard Item Classes");
				printLine(sbItems, "IU:M,Map");
				printLine(sbItems, "IU:C,Gold");
				printLine(sbItems, "IU:R,Ration");

//				String[] headersWeaponsHand = new String[]{ "NAME", " DMG", "GOLD", "LVL?" };
				printLine(sbItems, "* Default Weapon");
				printLine(sbItems, "WD:Fists,2,-1,N,-1,*,N,1,N,[none]");
				printLine(sbItems, "* Weapons");
				parseMeleeWeapons(sbItems, "WN", filestart + 8440, 18, 8, 15);
				parseRangedWeapons(sbItems, "WN", filestart + 8440, 34, 6, 15, 7, 13);
//				String[] headersArmor = new String[]{ "NAME", "ARMR", "GOLD", "LVL?" };
				printLine(sbItems, "* Ammo");
				for(String sammo : vcAmmo)
				{
					printLine(sbItems, sammo);
				}

				printLine(sbItems, "* Default Armors");
				printLine(sbItems, "AD:Clothes,0,0,Y,*,N,[none]");
				printLine(sbItems, "AD:(none),0,0,N,*,N,[none]");

				printLine(sbItems, "* Armor");
				parseBodyArmor(sbItems, filestart + 8712, 18, 8, 15);
//				String[] headersArmorSpecial = new String[]{ "NAME", "ARMR", "GOLD", "LVL?" };
				parseSpecialArmor(sbItems, filestart + 8856, 18, 6, 15);
				boolean printSpec = true;
				while(vcArmorSpc.size() > 0 || vcArmorBod.size() > 0)
				{
					if(printSpec && vcArmorSpc.size() > 0)
					{
						printLine(sbItems, vcArmorSpc.elementAt(0));
						vcArmorSpc.removeElementAt(0);
					}
					else if(vcArmorBod.size() > 0)
					{
						printLine(sbItems, vcArmorBod.elementAt(0));
						vcArmorBod.removeElementAt(0);
					}
					printSpec = !printSpec;
				}


				printLine(sbItems, "* Module Item Classes");
//				String[] headersMagicItems = new String[]{ "NAME", "IMG1", "IMG2", "IMG3", "IMG4", "    " };
				parseItemClasses(fileName, sbItems, filestart + 8964, 16, ITEMTYPES, 11);
//				String[] headersArtifacts = new String[]{ "NAME", "F/X#", "CHRG", "ELVL" };
				printLine(sbItems, "* Artifacts");
				parseItems(sbItems, "IT", currOffset, 18, 40, 15);

//				String[] headersQuestItems = new String[]{ "NAME", "    ", "DPTH", "IMG1", "IMG2", "IMG3", "IMG4", "    ", "TURN" }; // DPTH = preferred item depth, TURN = # of turns per dungeon level to find this item (0 == unlimited)
				parseQuestItems(fileName, sbQuest, "MQ", filestart + 9862, 19, 8, 11);

//				String[] headersTraps = new String[]{ "NAME", "FX#?", "    " };
				printLine(sbItems, "* Traps");
				parseTraps(sbItems, "TD", filestart + 10038, 12, 10, 10);

				printLine(sbItems, "* Fountain Effect definitions");
				printLine(sbItems, "FX:Healing|PDMG|-|P|4");
				printLine(sbItems, "FX:Healing|PDMG|-|P|6");
				printLine(sbItems, "FX:Healing|PDMG|-|P|8");
				printLine(sbItems, "FX:Healing|PDMG|-|P|10");
				printLine(sbItems, "FX:Healing|PDMG|-|P|12");
				printLine(sbItems, "FX:Foulness|PDMG|+|P|4");
				printLine(sbItems, "FX:Foulness|PDMG|+|P|6");
				printLine(sbItems, "FX:Foulness|PDMG|+|P|8");
				printLine(sbItems, "FX:Foulness|PDMG|+|P|10");
				printLine(sbItems, "FX:Foulness|PDMG|+|P|12");
				printLine(sbItems, "FX:Fortune|PLUK|+|P|3");
				printLine(sbItems, "FX:Illomen|PLUK|-|P|3");
				printLine(sbItems, "FX:Wisdom|PEXP|+|P|10");
				printLine(sbItems, "FX:Lethe|PEXP|-|P|10");
				printLine(sbItems, "FX:Vitality|PHPS|+|P|3");
				printLine(sbItems, "FX:Ague|PHPS|-|P|3");

				FileWriter fwm3 = new FileWriter(new File(pathOut + "items.todr"));
				fwm3.write(sbItems.toString(), 0, sbItems.length());
				fwm3.flush();
				fwm3.close();

				// scale up raw images to their ingame counterparts

				outputImageFile(fileName, "item_0.png", "room_fountain.png");
				outputImageFile(fileName, "item_1.png", "room_statue.png");
				outputImageFile(fileName, "item_2.png", "room_stairsup.png");
				outputImageFile(fileName, "item_3.png", "room_stairsdown.png");
				outputImageFile(fileName, "item_4.png", "room_shop.png");
				outputImageFile(fileName, "item_5.png", "item_vault.png");
				outputImageFile(fileName, "item_10.png", "itemclass_armor_body.png");
				outputImageFile(fileName, "item_10.png", "icon_armor_body.png", 1);
				outputImageFile(fileName, "item_11.png", "itemclass_armor_special.png");
				outputImageFile(fileName, "item_11.png", "icon_armor_special.png", 1);
				outputImageFile(fileName, "item_12.png", "itemclass_map.png");

				outputImageFile(fileName, "itempiece_88.png", "room_wallvert.png");
				outputImageFile(fileName, "itempiece_89.png", "room_wallhorz.png");
				outputImageFile(fileName, "itempiece_90.png", "room_doorvert.png");
				outputImageFile(fileName, "itempiece_91.png", "room_doorhorz.png");
				outputImageFile(fileName, "itempiece_92.png", "room_column.png");
				outputImageFile(fileName, "itempiece_93.png", "room_solid.png");

				outputAssembledImageFile(fileName, "itempiece", "itemclass_weapon_melee.png", 2, 96, 97, 251, 251);
				outputAssembledImageFile(fileName, "itempiece", "icon_weapon_melee.png", 1, 96, 97, 251, 251);
				outputAssembledImageFile(fileName, "itempiece", "itemclass_weapon_ranged.png", 2, 251, 251, 98, 99);
				outputAssembledImageFile(fileName, "itempiece", "icon_weapon_ranged.png", 1, 251, 251, 98, 99);
				outputAssembledImageFile(fileName, "itempiece", "item_chest.png", 2, 80, 58, 81, 59);
//				outputAssembledImageFile(fileName, "itempiece", "item_currency.png", 2, 251, 68, 251, 251);
				outputAssembledImageFile(fileName, "itempiece", "itemclass_currency.png", 2, 251, 68, 251, 251);
				outputAssembledImageFile(fileName, "itempiece", "dungeon-fountain.png", 16, 0, 1, 2, 3);
				outputAssembledImageFile(fileName, "itempiece", "dead.png", 2, 251, 84, 251, 85);

				// NOTE: Some games use labels to rename items
				// Here are some known label numbers that are renamed in existing games:
				//   11 - RATION
				//   30 - GOLD
				//   31 - HITPOINTS
				/* for this to work, you need to script the copying of the default textmappings.properties to the destination and then selectively replace entries */
				/*
				Properties textmapProperties = new Properties();
				textmapProperties.load(new FileInputStream(new File(pathOut + "textmappings.properties")));
				parseLabels(textmapProperties, filestart + 11264, 12, 36, 12);
				*/
				StringBuffer sbLabels = new StringBuffer(1024);
				parseField(sbLabels, "", filestart + 11264, 12, 36, 12);
				FileWriter fwm4 = new FileWriter(new File(pathOut + "labels.todr"));
				fwm4.write(sbLabels.toString(), 0, sbLabels.length());
				fwm4.flush();
				fwm4.close();

				if(!(new File(pathOut + "quest.todr").exists())) { new File(pathOut + "quest.todr").createNewFile(); }
				FileWriter fw = new FileWriter(pathOut + "quest.todr");
				fw.write(sbQuest.toString(), 0, sbQuest.length());
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
	}

//  Interface Methods -----------------------------------------------------------------------/

	/* ImageObserver */
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		return true;
	}

//  Class Methods ---------------------------------------------------------------------------/

	private void parseField(StringBuffer sbuffer, String lineKey, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			printChars(sbuffer, lineKey);
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = sbName.toString().trim();
			printChars(sbuffer, name);
			for(int i = nameSize; i < entrySize; i++)
			{
				printChars(sbuffer, ",");
				printByte(sbuffer, dataBytes[i]);
			}
			printLine(sbuffer, "");
			currOffset += entrySize;
		}
	}

	private void parseItemClasses(String fileName, StringBuffer sbOutput, int blockStart, int entrySize, String[] entryTypes, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryTypes.length; l++)
		{
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			int i = 0;
			for(i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = getCleanName(sbName.toString().trim());
			if(!(htItemClassKey.containsKey(name.toUpperCase())))
			{
				htItemClassKey.put(name.toUpperCase(), entryTypes[l]);
			}
			if(!(vcItemClass.contains(name.toUpperCase())))
			{
				int img1 = 128 + (int)dataBytes[i];
				int img2 = 128 + (int)dataBytes[i + 1];
				int img3 = 128 + (int)dataBytes[i + 2];
				int img4 = 128 + (int)dataBytes[i + 3];
				boolean everyoneOK = true;
				if((dataBytes[i + 4] & 128) == 128) { everyoneOK = false; }
				String perms = new String();
				if(everyoneOK) { perms = new String("*"); }
				else           { perms = new String("HW"); }
				String imageFileName = "itemclass_" + getGoodFileName(name.toLowerCase()) + ".png";
				outputAssembledImageFile(fileName, "itempiece", imageFileName, 2, img1, img2, img3, img4);
				String iconFileName = "icon_" + getGoodFileName(name.toLowerCase()) + ".png";
				outputAssembledImageFile(fileName, "itempiece", iconFileName, 1, img1, img2, img3, img4);
				int keyNum = 0;
				if     (entryTypes[l] == "P") { keyNum = 11; }
				else if(entryTypes[l] == "S") { keyNum = 10; }
				else if(entryTypes[l] == "W") { keyNum = 9; }
				else if(entryTypes[l] == "L") { keyNum = 12; }
				else if(entryTypes[l] == "T") { keyNum = 13; }
				String outString = "IC:" + entryTypes[l] + "," + name + "," + perms + "," + imageFileName + "," + "icon_" + getGoodFileName(name.toLowerCase()) + ".png" + "," + keyNum;
				printLine(sbOutput, outString);
			}
			vcItemClass.add(name.toUpperCase());
			currOffset += entrySize;
		}
	}

	private void parseItems(StringBuffer sbuffer, String lineKey, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			printChars(sbuffer, lineKey + ":");
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = sbName.toString().trim();
			printChars(sbuffer, name);
			printChars(sbuffer, ",");
			printChars(sbuffer, getItemKey(vcItemClass.elementAt((int)(Math.floor(l / 5)))) + ",");
			int effectPower = 0;
			printChars(sbuffer, "(|" + htEffectMaps.get("" + dataBytes[nameSize]) + "|" + Math.abs(dataBytes[nameSize + 2]) + "),"); // effect
			printChars(sbuffer, Math.abs(dataBytes[nameSize + 1]) + ",0"); // charges and cost (always 0)
			printLine(sbuffer, "");
			currOffset += entrySize;
		}
	}

	private String getItemKey(String smatch)
	{
		if(htItemClassKey.containsKey(smatch))
		{
			return htItemClassKey.get(smatch);
		}
		return "?";
	}

	private void parseCharDefs(StringBuffer sbuffer, String lineKey, int blockStart, int entrySize, int entryCount, int nameSize, String fileName)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			printChars(sbuffer, lineKey + ":"); // charclass name
			printChars(sbuffer, CHARKEY[l] + ","); // charclass key
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = getCleanName(sbName.toString().trim());
			printChars(sbuffer, name + ",");
			printChars(sbuffer, "100,"); // charclass levelup
			printChars(sbuffer, dataBytes[nameSize] + ","); // base HPs
			printChars(sbuffer, CHARVAL[l] + ","); // predefined charclass values for ToDR usage
			printChars(sbuffer, "{");
			if(dataBytes[nameSize + 8] > 0)
			{
				printChars(sbuffer, "I" + ((int)(dataBytes[nameSize + 8]) - 1) + "|"); // starting item ID
				printChars(sbuffer, dataBytes[nameSize + 9] + "|Y"); // starting item charges & item identified flag
			}
			printChars(sbuffer, "}");
			printLine(sbuffer, "");
			// create images
			String goodName = getGoodFileName(name);
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[0] + "_normal.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[0] + "_normal.png");
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[0] + "_attack.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[0] + "_attack.png");
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[1] + "_normal.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[1] + "_normal.png");
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[1] + "_attack.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[1] + "_attack.png");
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[2] + "_normal.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[2] + "_normal.png");
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[2] + "_attack.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[2] + "_attack.png");
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[3] + "_normal.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[3] + "_normal.png");
			outputImageFile(fileName, "charclass_" + l + "_" + TIGlobals.PLAYER_COLORS[3] + "_attack.png", "player_" + goodName + "_" + TIGlobals.PLAYER_COLORS[3] + "_attack.png");
			currOffset += entrySize;
		}
	}

	private void parseMonsters(String lineKey, int blockStart, int entrySize, int entryCount, int nameSize, String fileName)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		for(int l = 0; l < entryCount; l++)
		{
			StringBuffer sbMonster = new StringBuffer();
			int subField = 1;
			int subFieldCount = 0;
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = getCleanName(sbName.toString().trim());
			if(name.length() > 0)
			{
				if(vcMonstName.contains(name.toUpperCase()))
				{
					int appender = 2;
					String newName = name + appender;
					while(vcMonstName.contains(newName))
					{
						newName = name + appender;
						appender++;
					}
					name = newName;
				}
				vcMonstName.add(name.toUpperCase());
				printChars(sbMonster, lineKey + ":");
				printChars(sbMonster, name); // name
				printChar(sbMonster, ',');
				printByte(sbMonster, dataBytes[12]); // level
				printChar(sbMonster, ',');
				printByte(sbMonster, dataBytes[13]); // defense
				printChar(sbMonster, ',');
				boolean rangedatt = ((dataBytes[14] & 128) > 0); // attack ranged ?
				int attackval = Math.abs(dataBytes[14]); // attack
				printChars(sbMonster, (attackval * (rangedatt ? -1 : 1)) + ""); // combined attack value
				printChar(sbMonster, ',');
				printByte(sbMonster, dataBytes[15]); // max damage
				printChar(sbMonster, ',');
				int resistance = (dataBytes[20] & (byte)15) * 10;
				printChars(sbMonster, resistance + ","); // resistance
				int basemob = (dataBytes[20] & (byte)48);
				int mobility = ((basemob >> 4) * 25) + 25;
				printChars(sbMonster, mobility + ","); // mobility
				int basenego = (dataBytes[20] & 192);
				int negobytes = (basenego >> 6) * 25;
				printChars(sbMonster, negobytes + ","); // negotiation
				int basespeed = (dataBytes[21] & 240);
				int speed = (basespeed >> 4);
				printChars(sbMonster, speed + ","); // speed
				printChars(sbMonster, EXP[(int)(Math.floor(l / 4))] + ","); // experience
				printChars(sbMonster, "6,"); // max group
				printChars(sbMonster, (dataBytes[19] & 15) + ","); // sound number
				int imgnbits = (dataBytes[19] & 241);
				int imgnbyte = (imgnbits >> 4);
				//printChars(sbMonster, imgnbyte + "");  // image number - use this to create named image files in image dir from base numbered images
				int fxperc = Math.abs(dataBytes[16]);
				printChars(sbMonster, fxperc + ","); // special effect percent
				boolean rangedspc = ((dataBytes[17] & 128) > 0);
				printChars(sbMonster, (rangedspc ? 'Y' : 'N') + ","); // special effect  is ranged?
				printChars(sbMonster, "(" + (dataBytes[17] == 0 ? "-" : Math.abs(dataBytes[17]) + "|" + dataBytes[18]) + ")"); // special effect number code & power
				vcMonsters.add(sbMonster.toString());
				// create image files
				String goodName = getGoodFileName(name);
				outputImageFile(fileName, "monster_" + imgnbyte + "_normal.png", "monster_" + goodName + "_normal.png");
				outputImageFile(fileName, "monster_" + imgnbyte + "_attack.png", "monster_" + goodName + "_attack.png");
				if(dataBytes[17] != 0) { outputImageFile(fileName, "monster_" + imgnbyte + "_effect.png", "monster_" + goodName + "_effect.png"); }
			}
			currOffset += entrySize;
		}
	}

	private void parseEffects(int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			StringBuffer sbEffect = new StringBuffer();
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = sbName.toString().trim();
			printChars(sbEffect, name);
			for(int i = nameSize; i < entrySize; i++)
			{
				printChars(sbEffect, ",");
				printByte(sbEffect, dataBytes[i]);
			}
			vcEffects.add(sbEffect.toString());
			currOffset += entrySize;
		}
	}

	private void parseMeleeWeapons(StringBuffer sbuffer, String lineKey, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			if(sbName.toString().trim().length() > 0)
			{
				printChars(sbuffer, lineKey + ":");
				printChars(sbuffer, sbName.toString().trim());
				printChars(sbuffer, ",");
				printByte(sbuffer, dataBytes[nameSize]); // power
				printChars(sbuffer, ",");
				printChars(sbuffer, ((int)(dataBytes[nameSize + 1]) * 10) + ","); // cost
				printChars(sbuffer, "N,"); // is not ranged
				printChars(sbuffer, "-1,"); // ammo number
				// permissions
				boolean rogueOK  = false;
				boolean wizardOK = false;
				if((dataBytes[nameSize + 2] & 1) == 1) { rogueOK = true; }
				if((dataBytes[nameSize + 2] & 128) == 128) { wizardOK = true; }
				if(rogueOK && wizardOK) { printChars(sbuffer, "*"); }
				else if(rogueOK)        { printChars(sbuffer, "HFR"); }
				else if( wizardOK)      { printChars(sbuffer, "HFW"); }
				else                    { printChars(sbuffer, "HF"); }
				printChars(sbuffer, ",");
				printChars(sbuffer, "N,"); // throwable?
				printChars(sbuffer, "2,"); // sound (default melee sound)
				printChars(sbuffer, "N,"); // projectile effect
				printChars(sbuffer, "[none]");
				printLine(sbuffer, "");
			}
			currOffset += entrySize;
		}
	}

	private void parseRangedWeapons(StringBuffer sbuffer, String lineKey, int blockStart, int entrySize, int entryCount, int nameSize, int namedSubFieldOffset, int namedSubFieldSize)
	throws IOException
	{
		int subFields = entrySize - nameSize;
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			int subField = 1;
			int subFieldCount = 0;
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			if(sbName.toString().trim().length() > 0)
			{
				printChars(sbuffer, lineKey + ":");
				printChars(sbuffer, sbName.toString().trim() + ",");
				printByte(sbuffer, dataBytes[nameSize]); // power
				printChars(sbuffer, ",");
				printChars(sbuffer, ((int)(dataBytes[nameSize + 1]) * 10) + ","); // cost
				printChars(sbuffer, "Y,"); // is ranged

				int  ammoQuant = Math.abs(dataBytes[nameSize + 3]);
				byte ammoPrice = dataBytes[nameSize + 5];
				StringBuffer sbAmmoName = new StringBuffer();
				for(int i = nameSize + 6; i < entrySize; i++)
				{
					sbAmmoName.append((char)(dataBytes[i]));
				}
				String ammoName = sbAmmoName.toString().trim();
				if(ammoName.length() > 0)
				{
					StringBuffer sbAmmo = new StringBuffer();
					printChars(sbAmmo, "XA:");
					printChars(sbAmmo, ammoName + ",");
					printChars(sbAmmo, ammoQuant + ",");
					printChars(sbAmmo, ((int)(ammoPrice) * 10) + "");
					if(ammoQuant > 0 && !(vcAmmoName.contains(ammoName)))
					{
						vcAmmo.add(sbAmmo.toString());
						vcAmmoName.add(ammoName);
					}
				}

				printChars(sbuffer, (ammoQuant > 0 ? vcAmmoName.indexOf(ammoName) + "" : "-1")); // ammo number
				printChars(sbuffer, ",");
				// permissions
				boolean rogueOK  = false;
				boolean wizardOK = false;
				if((dataBytes[nameSize + 2] & 1) == 1) { rogueOK = true; }
				if((dataBytes[nameSize + 2] & 128) == 128) { wizardOK = true; }
				if(rogueOK && wizardOK) { printChars(sbuffer, "*"); }
				else if(rogueOK)        { printChars(sbuffer, "HFR"); }
				else if( wizardOK)      { printChars(sbuffer, "HFW"); }
				else                    { printChars(sbuffer, "HF"); }
				printChars(sbuffer, ",");
				printChars(sbuffer, (dataBytes[nameSize + 4] == -2 ? "Y" : "N") + ","); // thowable
				printChars(sbuffer, "3,"); // sound (default ranged sound)
				printChars(sbuffer, "2|C0C0C0,"); // default projectile graphic effect
				printChars(sbuffer, "[none]");
				printLine(sbuffer, "");
			}
			currOffset += entrySize;
		}
	}

	private void parseBodyArmor(StringBuffer sbuffer, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			if(sbName.toString().trim().length() > 0)
			{
				StringBuffer sbArmor = new StringBuffer();
				printChars(sbArmor, "AN:");
				printChars(sbArmor, sbName.toString().trim());
				printChars(sbArmor, ",");
				printByte(sbArmor, dataBytes[nameSize]);
				printChars(sbArmor, ",");
				printByte(sbArmor, dataBytes[nameSize + 1]);
				printChar(sbArmor, '0');
				printChar(sbArmor, ',');
				printChar(sbArmor, 'Y');
				printChar(sbArmor, ',');
				// permissions
				boolean rogueOK  = false;
				boolean wizardOK = false;
				if((dataBytes[nameSize + 2] & 2) == 2) { rogueOK = true; }
				if((dataBytes[nameSize + 2] & 128) == 128) { wizardOK = true; }
				if(rogueOK && wizardOK) { printChars(sbArmor, "*"); }
				else if(rogueOK)        { printChars(sbArmor, "HFR"); }
				else if( wizardOK)      { printChars(sbArmor, "HFW"); }
				else                    { printChars(sbArmor, "HF"); }
				printChar(sbArmor, ',');
				printChar(sbArmor, 'Y');
				printChar(sbArmor, ',');
				printChars(sbArmor, "[none]");
				vcArmorBod.add(sbArmor.toString());
			}
			currOffset += entrySize;
		}
	}

	private void parseSpecialArmor(StringBuffer sbuffer, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			if(sbName.toString().trim().length() > 0)
			{
				StringBuffer sbArmor = new StringBuffer();
				printChars(sbArmor, "AN:");
				printChars(sbArmor, sbName.toString().trim());
				printChars(sbArmor, ",");
				printByte(sbArmor, dataBytes[nameSize]);
				printChars(sbArmor, ",");
				printByte(sbArmor, dataBytes[nameSize + 1]);
				printChar(sbArmor, '0');
				printChar(sbArmor, ',');
				printChar(sbArmor, 'N');
				printChar(sbArmor, ',');
				// permissions
				boolean rogueOK  = false;
				boolean wizardOK = false;
				if((dataBytes[nameSize + 2] & 64) == 64) { rogueOK = true; }
				if((dataBytes[nameSize + 2] & 128) == 128) { wizardOK = true; }
				if(rogueOK && wizardOK) { printChars(sbArmor, "*"); }
				else if(rogueOK)        { printChars(sbArmor, "HFR"); }
				else if( wizardOK)      { printChars(sbArmor, "HFW"); }
				else                    { printChars(sbArmor, "HF"); }
				printChar(sbArmor, ',');
				printChar(sbArmor, 'Y');
				printChar(sbArmor, ',');
				printChars(sbArmor, "[none]");
				vcArmorSpc.add(sbArmor.toString());
			}
			currOffset += entrySize;
		}
	}

	private void parseTraps(StringBuffer sbuffer, String lineKey, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			printChars(sbuffer, lineKey + ":");
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = sbName.toString().trim();
			printChars(sbuffer, name + "|"); // trap name
			printChars(sbuffer, htEffectMaps.get(dataBytes[nameSize] + "")); // trap effect (need to match up with defined entries in game code)
			int trapPower = 5; // how to make appropriate in files?
			printChars(sbuffer, "|" + trapPower); // trap power (not in game sources, probably defined elsewhere along with effects)
			printChars(sbuffer, "[" + dataBytes[nameSize + 1] + "]"); // percent chance
			printLine(sbuffer, "");
			currOffset += entrySize;
		}
	}

	private void parseQuestDescription(StringBuffer sbuffer, int blockStart)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[32];
		for(int l = 0; l < 12; l++)
		{
			StringBuffer sbLine = new StringBuffer(32);
			disReader.read(dataBytes);
			for(int i = 0; i < dataBytes.length; i++)
			{
				if(dataBytes[i] >= 0)
				{
					printChar(sbLine, (char)(dataBytes[i]));
				}
			}
			if(l == 0)
			{
				printLine(sbuffer, "MN:" + sbLine.toString().trim());
				printChars(sbuffer, "MO:");
			}
			else
			{
				if(!(sbLine.toString().trim().equals("____________________________")))
				{
					printChars(sbuffer, sbLine.toString().trim() + "_");
				}
			}
			currOffset += dataBytes.length;
		}
		printLine(sbuffer, "");
	}

	private void parseQuestItems(String fileName, StringBuffer sbuffer, String lineKey, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = sbName.toString().trim();
			if(name.length() > 0)
			{
				printChars(sbuffer, lineKey + ":");
				printChars(sbuffer, name + "|");
				printChars(sbuffer, "Y|"); // required ?
				printChars(sbuffer, "N|"); // partial ?
				printByte(sbuffer, dataBytes[nameSize + 7]); // turns to find
				printChars(sbuffer, "|");
				printByte(sbuffer, dataBytes[nameSize + 1]); // pref level
				printChars(sbuffer, "|");
				printChars(sbuffer, "[none]|"); // fixed level

				String questImageName = "quest_" + getGoodFileName(getCleanName(name)) + ".png";
				outputAssembledImageFile(fileName, "itempiece", questImageName, 2, (128 + (int)(dataBytes[nameSize + 2])), (128 + (int)(dataBytes[nameSize + 3])), (128 + (int)(dataBytes[nameSize + 4])), (128 + (int)(dataBytes[nameSize + 5])));
				printChars(sbuffer, questImageName);

				printChars(sbuffer, "|");
				printChars(sbuffer, "Find the " + name + "|");
				printChars(sbuffer, "Success!_You have found the " + name + " in time!|");
				printChars(sbuffer, "Good work!_You have found the " + name + "|");
				printChars(sbuffer, "Failure!_You did not find the " + name + " in time!");
				printLine(sbuffer, "");
			}
			currOffset += entrySize;
		}
	}

/*
	private void parseLabels(Properties props, int blockStart, int entrySize, int entryCount, int nameSize)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			printChars(sbuffer, lineKey + ":");
			disReader.read(dataBytes);
			StringBuffer sbName = new StringBuffer(nameSize);
			for(int i = 0; i < nameSize; i++)
			{
				printChar(sbName, (char)(dataBytes[i]));
			}
			String name = sbName.toString().trim();
			printChars(sbuffer, name);
			for(int i = nameSize; i < entrySize; i++)
			{
				printChars(sbuffer, ",");
				printByte(sbuffer, dataBytes[i]);
			}
			printLine(sbuffer, "");
			currOffset += entrySize;
		}
	}
*/
	private void printLine(StringBuffer sb, String text)
	{
		sb.append(text);
		String linesep = System.getProperty("line.separator");
		sb.append(linesep);
	}

	private void printChars(StringBuffer sb, String chars)
	{
		sb.append(chars);
	}

	private void printChar(StringBuffer sb, char character)
	{
		sb.append(character);
	}

	private void printBytes(StringBuffer sb, byte[] bytes)
	{
		sb.append(bytes);
	}

	private void printByte(StringBuffer sb, byte val)
	{
		sb.append(val);
	}

	private void promoteImageFile(String masterfile, String src, String dest)
	{
		try
		{
			FileInputStream  fis = new FileInputStream(new File("conversions" + File.separator + masterfile + File.separator + "rawimages" + File.separator + src));
			FileOutputStream fos = new FileOutputStream(new File("conversions" + File.separator + masterfile + File.separator + "images" + File.separator + dest));
			while(fis.available() > 0)
			{
				fos.write(fis.read());
			}
			fos.close();
			fis.close();
		}
		catch(Exception e) { e.printStackTrace(System.out); }
	}

	private void outputImageFile(String masterfile, String src, String dest, int scaling)
	{
		try
		{
			Image img = loadImage("conversions" + File.separator + masterfile + File.separator + "rawimages" + File.separator + src);
			int imgW = img.getWidth(this);
			int imgH = img.getHeight(this);
			BufferedImage bimg = new BufferedImage(imgW * scaling, imgH * scaling, BufferedImage.TYPE_4BYTE_ABGR);
			bimg.getGraphics().drawImage(img, 0, 0, imgW * scaling, imgH * scaling, 0, 0, imgW, imgH, this);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bimg, "png", baos);
			baos.flush();
			byte[] img2bytes = baos.toByteArray();
			baos.close();
			FileOutputStream fos = new FileOutputStream(new File("conversions" + File.separator + masterfile + File.separator + "images" + File.separator + dest));
			fos.write(img2bytes);
			fos.close();
		}
		catch(Exception e) { e.printStackTrace(System.out); }
	}

	private void outputImageFile(String masterfile, String src, String dest)
	{
		outputImageFile(masterfile, src, dest, 2);
	}

	private void outputAssembledImageFile(String masterfile, String src, String dest, int scaling, int tile1, int tile2, int tile3, int tile4)
	{
		try
		{
			int imgW = 8;
			int imgH = 8;
			BufferedImage bimg = new BufferedImage(imgW * 2 * scaling, imgH * 2 * scaling, BufferedImage.TYPE_4BYTE_ABGR);
			if(tile1 != 251) { Image img1 = loadImage("conversions" + File.separator + masterfile + File.separator + "rawimages" + File.separator + src + "_" + tile1 + ".png"); bimg.getGraphics().drawImage(img1, 0,              0,              imgW * scaling,     imgH * scaling,     0, 0, imgW, imgH, this); }
			if(tile2 != 251) { Image img2 = loadImage("conversions" + File.separator + masterfile + File.separator + "rawimages" + File.separator + src + "_" + tile2 + ".png"); bimg.getGraphics().drawImage(img2, 0,              imgH * scaling, imgW * scaling,     imgH * 2 * scaling, 0, 0, imgW, imgH, this); }
			if(tile3 != 251) { Image img3 = loadImage("conversions" + File.separator + masterfile + File.separator + "rawimages" + File.separator + src + "_" + tile3 + ".png"); bimg.getGraphics().drawImage(img3, imgW * scaling, 0,              imgW * 2 * scaling, imgH * scaling,     0, 0, imgW, imgH, this); }
			if(tile4 != 251) { Image img4 = loadImage("conversions" + File.separator + masterfile + File.separator + "rawimages" + File.separator + src + "_" + tile4 + ".png"); bimg.getGraphics().drawImage(img4, imgW * scaling, imgH * scaling, imgW * 2 * scaling, imgH * 2 * scaling, 0, 0, imgW, imgH, this); }
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bimg, "png", baos);
			baos.flush();
			byte[] img2bytes = baos.toByteArray();
			baos.close();
			FileOutputStream fos = new FileOutputStream(new File("conversions" + File.separator + masterfile + File.separator + "images" + File.separator + dest));
			fos.write(img2bytes);
			fos.close();
		}
		catch(Exception e) { e.printStackTrace(System.out); }
	}

	private Image loadImage(String imgname)
	{
		if(imgname == null || imgname.equals("") || mtrack == null) { return (Image)null; }
		Image imgTemp = Toolkit.getDefaultToolkit().getImage(imgname);
		mtrack.addImage(imgTemp, 1);
		try
		{
			mtrack.waitForID(1);
		}
		catch(InterruptedException ie)
		{
			System.err.println("Error loading image " + imgname + " - " + ie.getMessage());
		}
		return imgTemp;
	}

	private String getCleanName(String name)
	{
		return name.replaceAll("'", "");
	}

	private String getGoodFileName(String name)
	{
		return name.replaceAll(" ", "_").toLowerCase();
	}

	public static void main(String[] args)
	{
		if(args.length < 3)
		{
			System.out.println("usage: java com.dreamcodex.todr.util.TODconverter DISK FILENAME OUTFILE");
			System.out.println("          DISK     = source .dsk disk image file");
			System.out.println("          FILENAME = name of ToD game/save file to parse");
			System.out.println("          OUTFILE  = parsed results file to create");
			System.exit(0);
		}
		else
		{
			TODconverter todconv = new TODconverter(args[0], args[1], args[2]);
		}
	}
}
