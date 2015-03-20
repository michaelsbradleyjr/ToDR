package com.dreamcodex.todr.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** TI99FileFinder (C) Howard Kistler
  * TI-99 disk image file locator
  *
  * @author Howard Kistler
  * @creationdate 6/17/2004
  */

public class TI99FileFinder
{
	public static int firstFilenameEntry  = Integer.parseInt("200", 16);
	public static int filenameEntryOffset = Integer.parseInt("100", 16);
	public static int lastFilenameEntry   = Integer.parseInt("1F00", 16);

	public TI99FileFinder()
	{
	}

	public static int getFileEntryAddress(String diskImage, String filename)
	{
		try
		{
			filename = filename.toUpperCase();
			int fileStart = firstFilenameEntry;
			byte[] inBytes = new byte[10];
			DataInputStream disReader = new DataInputStream(new FileInputStream(new File(diskImage)));
			disReader.skipBytes(fileStart);
			while(fileStart <= lastFilenameEntry)
			{
				disReader.read(inBytes);
				boolean isMatch = true;
				for(int i = 0; i < inBytes.length; i++)
				{
					if(filename.length() <= i)
					{
						if(((char)inBytes[i]) != ' ')
						{
							isMatch = false;
						}
					}
					else if(((char)inBytes[i]) != filename.charAt(i))
					{
						isMatch = false;
					}
					if(!isMatch) { i = inBytes.length; }
				}
				if(isMatch)
				{
					return fileStart;
				}
				else
				{
					fileStart += filenameEntryOffset;
					if(fileStart > lastFilenameEntry) { return -1; }
					disReader.skipBytes(filenameEntryOffset - 10);
				}
			}
		}
		catch(Exception e) { e.printStackTrace(System.out); }
		return -1;
	}

	public static String getFileStartAddress(String diskImage, String filename)
	{
		String newHexString = (String)null;
		filename = filename.toUpperCase();
		int fileTableAddr = getFileEntryAddress(diskImage, filename);
		if(fileTableAddr > -1)
		{
			try
			{
				byte[] inName  = new byte[10];
				byte[] inBytes = new byte[22];
				DataInputStream disReader = new DataInputStream(new FileInputStream(new File(diskImage)));
				disReader.skipBytes(fileTableAddr);
				disReader.read(inName);
				disReader.read(inBytes);
				String hexString = Integer.toHexString((int)(inBytes[19] & 0xFF)) + Integer.toHexString((int)(inBytes[18] & 0xFF));
/*
				String hexPartA = Integer.toHexString((int)(inBytes[19] & 0xFF));
				String hexPartB = Integer.toHexString((int)(inBytes[18] & 0xFF));
				if(hexPartA.length() == 1) { hexPartA = "0" + hexPartA; }
				if(hexPartB.length() == 1) { hexPartB = "0" + hexPartB; }
				String hexString = hexPartA + hexPartB;
*/
				String hexMain = "" + hexString.charAt(1) + hexString.charAt(2) + hexString.charAt(3);
				char hex4 = hexString.charAt(0);
				newHexString = Integer.toHexString(Integer.parseInt(hexMain, 16) + Integer.parseInt(hex4 + "", 16)) + "00";
			}
			catch(Exception e) { e.printStackTrace(System.out); }
			return newHexString;
		}
		else
		{
			return "";
		}
	}

	public static int getFileStartPosition(String diskImage, String filename)
	{
		String fileStartInHex = getFileStartAddress(diskImage, filename);
		if(fileStartInHex == null || fileStartInHex.length() < 1)
		{
			return -1;
		}
		else
		{
			return Integer.parseInt(fileStartInHex, 16);
		}
	}

	public static byte[] getFileAsBytes(String diskImage, String filename, int filelength)
	{
		byte[] bfile = new byte[filelength];
		int fileTableAddr = getFileStartPosition(diskImage, filename);
		if(fileTableAddr > -1)
		{
			try
			{
				DataInputStream disReader = new DataInputStream(new FileInputStream(new File(diskImage)));
				disReader.skipBytes(fileTableAddr);
				disReader.read(bfile, 0, filelength);
				disReader.close();
/*
				DataOutputStream dosWriter = new DataOutputStream(new FileOutputStream(new File("test.dsk")));
				dosWriter.write(bfile, 0, filelength);
				dosWriter.close();
*/
			}
			catch(Exception e) { e.printStackTrace(System.out); }
			return bfile;
		}
		return new byte[0];
	}

	public static void main(String[] args)
	{
		if(args.length >= 2)
		{
			int fileAddr = TI99FileFinder.getFileEntryAddress(args[0], args[1]);
			System.out.println(args[1] + " disktable entry at " + fileAddr);
			System.out.println("actual file starts at " + getFileStartAddress(args[0], args[1]));
		}
		else
		{
			System.out.println("You must specify a disk image name and a file name.");
		}
	}
}