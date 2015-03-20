package com.dreamcodex.todr.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.dreamcodex.todr.util.TI99FileFinder;

/** TODparser (C) Howard Kistler
  * "Tunnels of Doom" game data extraction utility
  *
  * @author Howard Kistler
  * @version 1.0
  * @creationdate 6/17/2004
  * @modificationdate 6/18/2005
  */

public class TODparser
{
	private StringBuffer sbOutput;
	private DataInputStream disReader;
	private int currOffset;

	public TODparser(String fileIn, String fileName, String fileOut)
	{
		try
		{
			int filestart = TI99FileFinder.getFileStartPosition(fileIn, fileName);
			if(filestart > -1)
			{
				sbOutput = new StringBuffer(1024);
				disReader = new DataInputStream(new FileInputStream(new File(fileIn)));
				currOffset = 0;
				printLine("TUNNELS OF DOOM DATA FILE PARSE");
				parseQuestDescription(filestart);
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
	}

	private void parseQuestDescription(int blockStart)
	throws IOException
	{
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[32];
		printLine("");
		printLine("********************************");
		printLine("* QUEST DESCRIPTION");
		printLine("********************************");
		printLine("");
		for(int l = 0; l < 12; l++)
		{
			disReader.read(dataBytes);
			printLine(new String(dataBytes));
			currOffset += dataBytes.length;
		}
	}

	private void parseField(String blockName, String[] headers, int blockStart, int entrySize, int entryCount, int nameSize, int namedSubFieldOffset, int namedSubFieldSize)
	throws IOException
	{
		int subFields = entrySize - nameSize;
		disReader.skipBytes(blockStart - currOffset);
		currOffset = blockStart;
		byte[] dataBytes = new byte[entrySize];
		// print block header
		printLine("");
		printLine("********************************");
		printLine("* " + blockName);
		printLine("********************************");
		printLine("");
		// print table header
		printChars(headers[0]);
		for(int p = 0; p < (nameSize - 4); p++)
		{
			printChars(" ");
		}
		printChars("|");
		for(int x = 1; x < headers.length; x++)
		{
			if(x == namedSubFieldOffset)
			{
				printChars(headers[x]);
				for(int z = 0; z < (namedSubFieldSize - headers[x].length()); z++)
				{
					printChars(" ");
				}
				printChars("|");
				x += namedSubFieldSize;
			}
			else
			{
				printChars(headers[x]);
				printChars("|");
			}
		}
		printLine("");
		// print table line
		for(int p2 = 0; p2 < (entrySize - subFields); p2++)
		{
			printChars("=");
		}
		printChars("|");
		for(int x = 1; x < headers.length; x++)
		{
			if(x == namedSubFieldOffset)
			{
				for(int z = 0; z < namedSubFieldSize; z++)
				{
					printChars("=");
				}
				printChars("|");
				x += namedSubFieldSize;
			}
			else
			{
				printChars("====|");
			}
		}
		printLine("");
		// print table entries
		for(int l = 0; l < entryCount; l++)
		{
			int subField = 1;
			int subFieldCount = 0;
			disReader.read(dataBytes);
			for(int i = 0; i < entrySize; i++)
			{
				if((i < (entrySize - subFields)))
				{
					printChar((char)(dataBytes[i]));
				}
				else if(subField == namedSubFieldOffset)
				{
					if(subFieldCount == 0)
					{
						printChars("|");
					}
					printChar((char)(dataBytes[i]));
					subFieldCount++;
					if(subFieldCount >= namedSubFieldSize)
					{
						subField++;
					}
				}
				else
				{
					if(blockName.equals("MONSTERS") && i == 14)
					{
						// get RANGED ATTACK flag (high bit)
						boolean rangedatt = ((dataBytes[i] & 128) > 0);
						printChars("|");
						printChars(" " + (rangedatt ? "XX" : "--") + " ");
						// get ATTACK ranking
//						int attackval = (dataBytes[i] & 15);
						int attackval = Math.abs(dataBytes[i]);
						int byteSize = (attackval > 99 ? 3 : (attackval > 9 ? 2 : 1));
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(attackval + "");
					}
					else if(blockName.equals("MONSTERS") && i == 17)
					{
						// get RANGED SPECIAL flag (high bit)
						boolean rangedspc = ((dataBytes[i] & 128) > 0);
						printChars("|");
						printChars(" " + (rangedspc ? "XX" : "--") + " ");
						// get SPECIAL ATTACK ranking
						int spcattval = Math.abs(dataBytes[i]);
						int byteSize = (spcattval > 99 ? 3 : (spcattval > 9 ? 2 : 1));
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(spcattval + "");
					}
					else if(blockName.equals("MONSTERS") && i == 18)
					{
						// process special effect power level
						int byteSize = 0;
						if(dataBytes[i] < 0) { byteSize++; }
						if(Math.abs(dataBytes[i]) > 99)
						{
							byteSize += 3;
						}
						else if(Math.abs(dataBytes[i]) > 9)
						{
							byteSize += 2;
						}
						else
						{
							byteSize += 1;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars("" + dataBytes[i]);
					}
					else if(blockName.equals("MONSTERS") && i == 19)
					{
						// get SOUND bits
						int byteSize = 0;
						int soundbytes = (dataBytes[i] & 15);
						if(soundbytes > 99)
						{
							byteSize = 3;
						}
						else if(soundbytes > 9)
						{
							byteSize = 2;
						}
						else
						{
							byteSize = 1;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(soundbytes + "");
						// get IMAGE # bits
						byteSize = 0;
						int imgnbits = (dataBytes[i] & 241);
						int imgnbyte = (imgnbits >> 4);
						if(imgnbyte > 99)
						{
							byteSize = 3;
						}
						else if(imgnbyte > 9)
						{
							byteSize = 2;
						}
						else
						{
							byteSize = 1;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(imgnbyte + "");
					}
					else if(blockName.equals("MONSTERS") && i == 20)
					{
						// get RESISTANCE bits
						int byteSize = 0;
						int resistance = (dataBytes[i] & (byte)15) * 10;
						if(resistance > 99)
						{
							byteSize = 4;
						}
						else if(resistance > 9)
						{
							byteSize = 3;
						}
						else
						{
							byteSize = 2;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(resistance + "%");
						// get MOBILITY bits
						byteSize = 0;
						int basemob = (dataBytes[i] & (byte)48);
						int mobility = ((basemob >> 4) * 25) + 25;
						if(mobility > 99)
						{
							byteSize = 4;
						}
						else if(mobility > 9)
						{
							byteSize = 3;
						}
						else
						{
							byteSize = 2;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(mobility + "%");
						// get NEGOTIATION bits
						byteSize = 0;
						int basenego = (dataBytes[i] & 192);
						int negobytes = (basenego >> 6) * 25;
						if(Math.abs(negobytes) > 99)
						{
							byteSize = 4;
						}
						else if(Math.abs(negobytes) > 9)
						{
							byteSize = 3;
						}
						else
						{
							byteSize = 2;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(negobytes + "%");
					}
					else if(blockName.equals("MONSTERS") && i == 21)
					{
						// get mystery bits
						int byteSize = 0;
						int unknownbytes = (dataBytes[i] & 15);
						if(unknownbytes > 99)
						{
							byteSize = 3;
						}
						else if(unknownbytes > 9)
						{
							byteSize = 2;
						}
						else
						{
							byteSize = 1;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(unknownbytes + "");
						// get SPEED bits
						// high bit appears to be unused, as it is 0 in all original monster listings, but we check it anyway
						byteSize = 0;
						int basespeed = (dataBytes[i] & 240);
						int speed = (basespeed >> 4);
						if(speed > 99)
						{
							byteSize = 3;
						}
						else if(speed > 9)
						{
							byteSize = 2;
						}
						else
						{
							byteSize = 1;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printChars(speed + "");
					}
					else if(blockName.equals("HAND WEAPONS") && i == 17)
					{
						printChars("|");
						// FIGHTERS & HEROES can use every weapon, so check only for ROGUE & WIZARD flags
						boolean rogueOK  = false;
						boolean wizardOK = false;
						if((dataBytes[i] & 1) == 1) { rogueOK = true; }
						if((dataBytes[i] & 128) == 128) { wizardOK = true; }
						if(rogueOK && wizardOK) { printChars("*   "); }
						else if(rogueOK)        { printChars("HFR "); }
						else if( wizardOK)      { printChars("HFW "); }
						else                    { printChars("HF  "); }
						printChars("|");
						printBits(dataBytes[i]);
					}
					else if(blockName.equals("RANGED WEAPONS") && i == 17)
					{
						printChars("|");
						// FIGHTERS & HEROES can use every weapon, so check only for ROGUE & WIZARD flags
						boolean rogueOK  = false;
						boolean wizardOK = false;
						if((dataBytes[i] & 1) == 1) { rogueOK = true; }
						if((dataBytes[i] & 128) == 128) { wizardOK = true; }
						if(rogueOK && wizardOK) { printChars("*   "); }
						else if(rogueOK)        { printChars("HFR "); }
						else if( wizardOK)      { printChars("HFW "); }
						else                    { printChars("HF  "); }
						printChars("|");
						printBits(dataBytes[i]);
					}
					else if(blockName.equals("ARMOR") && i == 17)
					{
						printChars("|");
						// FIGHTERS & HEROES can use every armor, so check only for ROGUE & WIZARD flags
						boolean rogueOK  = false;
						boolean wizardOK = false;
						if((dataBytes[i] & 2) == 2) { rogueOK = true; }
						if((dataBytes[i] & 128) == 128) { wizardOK = true; }
						if(rogueOK && wizardOK) { printChars("*   "); }
						else if(rogueOK)        { printChars("HFR "); }
						else if( wizardOK)      { printChars("HFW "); }
						else                    { printChars("HF  "); }
						printChars("|");
						printBits(dataBytes[i]);
					}
					else if(blockName.equals("SPECIAL ARMOR") && i == 17)
					{
						printChars("|");
						// FIGHTERS & HEROES can use every armor, so check only for ROGUE & WIZARD flags
						boolean rogueOK  = false;
						boolean wizardOK = false;
						if((dataBytes[i] & 64) == 64) { rogueOK = true; }
						if((dataBytes[i] & 128) == 128) { wizardOK = true; }
						if(rogueOK && wizardOK) { printChars("*   "); }
						else if(rogueOK)        { printChars("HFR "); }
						else if( wizardOK)      { printChars("HFW "); }
						else                    { printChars("HF  "); }
						printChars("|");
						printBits(dataBytes[i]);
					}
					else if(blockName.equals("ARTIFACT CLASSES") && i == 15)
					{
						printChars("|");
						// items are WIZARD & HERO only, or everyone
						boolean everyoneOK = true;
						if((dataBytes[i] & 128) == 128) { everyoneOK = false; }
						if(everyoneOK) { printChars("*   "); }
						else           { printChars("HW  "); }
						printChars("|");
						printBits(dataBytes[i]);
					}
					else
					{
						int byteSize = 0;
						if(dataBytes[i] < 0) { byteSize++; }
						if(Math.abs(dataBytes[i]) > 99)
						{
							byteSize += 3;
						}
						else if(Math.abs(dataBytes[i]) > 9)
						{
							byteSize += 2;
						}
						else
						{
							byteSize += 1;
						}
						printChars("|");
						for(int pad = byteSize; pad < 4; pad++) { printChars(" "); }
						printByte(dataBytes[i]);
					}
					subField++;
				}
			}
			printLine("|");
			currOffset += entrySize;
		}
	}

	private void parseField(String blockName, String[] headers, int blockStart, int entrySize, int entryCount, int subFields)
	throws IOException
	{
		parseField(blockName, headers, blockStart, entrySize, entryCount, subFields, 0, 0);
	}

	private void printLine(String text)
	{
//		System.out.println(text);
		sbOutput.append(text);
		String linesep = System.getProperty("line.separator");
		sbOutput.append(linesep);
	}

	private void printChars(String chars)
	{
//		System.out.print(chars);
		sbOutput.append(chars);
	}

	private void printChar(char character)
	{
//		System.out.print(character);
		sbOutput.append(character);
	}

	private void printBytes(byte[] bytes)
	{
//		System.out.print(bytes);
		sbOutput.append(bytes);
	}

	private void printByte(byte val)
	{
//		System.out.print(val);
		sbOutput.append(val);
	}

	private void printBits(byte val)
	{
		boolean getbit = ((val & 1) == 1);
		printChars((getbit ? "*" : "-"));
		getbit = ((val & 2) == 2);
		printChars((getbit ? "*" : "-"));
		getbit = ((val & 4) == 4);
		printChars((getbit ? "*" : "-"));
		getbit = ((val & 8) == 8);
		printChars((getbit ? "*" : "-"));
		getbit = ((val & 16) == 16);
		printChars((getbit ? "*" : "-"));
		getbit = ((val & 32) == 32);
		printChars((getbit ? "*" : "-"));
		getbit = ((val & 64) == 64);
		printChars((getbit ? "*" : "-"));
		getbit = ((val & 128) == 128);
		printChars((getbit ? "*" : "-"));
	}

	public static void main(String[] args)
	{
		if(args.length < 3)
		{
			System.out.println("usage: java com.dreamcodex.todr.util.TODParser DISK FILENAME OUTFILE");
			System.out.println("          DISK     = source .dsk disk image file");
			System.out.println("          FILENAME = name of ToD game/save file to parse");
			System.out.println("          OUTFILE  = parsed results file to create");
			System.exit(0);
		}
		else
		{
			TODparser todparser = new TODparser(args[0], args[1], args[2]);
		}
	}
}
