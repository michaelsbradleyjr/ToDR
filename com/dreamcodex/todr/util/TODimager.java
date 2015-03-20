package com.dreamcodex.todr.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;

import com.dreamcodex.todr.util.TI99FileFinder;
import com.dreamcodex.todr.util.TIGlobals;

/** TODimager (C) Howard Kistler
  * "Tunnels of Doom" game image extraction utility
  *
  * @author Howard Kistler
  * @version 1.0
  * @creationdate 6/17/2004
  */

public class TODimager
{
	public boolean processing = false;

	public TODimager(String fileIn, String fileName, String imageOut)
	{
		int filestart = TI99FileFinder.getFileStartPosition(fileIn, fileName);
		if(filestart > -1)
		{
			processing = true;
			processImageBlock(fileIn, imageOut + "full",        filestart, 26, 32, 8);
			processTileBlock(fileIn,  imageOut + "party",       filestart + (256 * 2), 8, 8);
			processImageBlock(fileIn, imageOut + "font",        filestart + (256 * 3), 2, 32, 8);
			processImageBlock(fileIn, imageOut + "maptiles",    filestart + (256 * 5), 1, 12, 8);
			processTileBlock(fileIn,  imageOut + "charclasses", filestart + (256 * 11) + (25 * 8) + 2, 8, 8);
			processQuadUnits(fileIn,  imageOut + "charclass",   filestart + (256 * 11) + (25 * 8) + 2, 8, 8);
			processTileBlock(fileIn,  imageOut + "monsters",    filestart + (256 * 17) + (59 * 8) + 2, 32, 8);
			processPairUnits(fileIn,  imageOut + "monster",     filestart + (256 * 17) + (59 * 8) + 2, 32, 8);
			processTileBlock(fileIn,  imageOut + "items",       filestart + (256 * 40) + (18 * 8), 28, 8);
			processTileUnits(fileIn,  imageOut + "item",        filestart + (256 * 40) + (18 * 8), 28, 8);
			processTilePiece(fileIn,  imageOut + "itempiece",   filestart + (256 * 40) + (18 * 8), 112, 8);
			processing = false;
		}
		else
		{
			System.out.println("Cannot find ToD game file with the name " + fileName);
			System.exit(1);
		}
	}

	public void processImageBlock(String fileIn, String imageOut, int imageBankStart, int numberOfImageRows, int numberOfImageCols, int numberOfByteRows)
	{
		BufferedImage bufferOutput = new BufferedImage(8 * numberOfImageCols, numberOfByteRows * numberOfImageRows, BufferedImage.TYPE_INT_ARGB);
		if(bufferOutput == null)
		{
			bufferOutput = new BufferedImage(8 * numberOfImageCols, numberOfByteRows * numberOfImageRows, BufferedImage.TYPE_INT_ARGB);
		}
		try { Thread.sleep(500); } catch(InterruptedException ie) { System.out.println(ie.getMessage()); }
		if(bufferOutput == null)
		{
			System.out.println("Unable to initialize BufferedImage");
			System.exit(1);
		}
		try
		{
			Graphics2D g = (Graphics2D)(bufferOutput.getGraphics());
			g.setComposite(AlphaComposite.Clear);
			g.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
			g.fillRect(0, 0, bufferOutput.getWidth(), bufferOutput.getHeight());
			g.setComposite(AlphaComposite.SrcOver);
			g.setColor(TIGlobals.TI_COLOR_BLACK);
			DataInputStream disReader = new DataInputStream(new FileInputStream(new File(fileIn)));
			disReader.skipBytes(imageBankStart);
			for(int imagerows = 0; imagerows < numberOfImageRows; imagerows++)
			{
				for(int imagecols = 0; imagecols < numberOfImageCols; imagecols++)
				{
					byte[] dataBytes = new byte[8];
					disReader.read(dataBytes);
					for(int i = 0; i < dataBytes.length; i++)
					{
						byte b = dataBytes[i];
						int currBaseX = imagecols * 8;
						int currBaseY = (imagerows * 8) + i;
						if((b & 128) > 0) { g.fillRect(currBaseX,     currBaseY, 1, 1); }
						if((b &  64) > 0) { g.fillRect(currBaseX + 1, currBaseY, 1, 1); }
						if((b &  32) > 0) { g.fillRect(currBaseX + 2, currBaseY, 1, 1); }
						if((b &  16) > 0) { g.fillRect(currBaseX + 3, currBaseY, 1, 1); }
						if((b &   8) > 0) { g.fillRect(currBaseX + 4, currBaseY, 1, 1); }
						if((b &   4) > 0) { g.fillRect(currBaseX + 5, currBaseY, 1, 1); }
						if((b &   2) > 0) { g.fillRect(currBaseX + 6, currBaseY, 1, 1); }
						if((b &   1) > 0) { g.fillRect(currBaseX + 7, currBaseY, 1, 1); }
					}
				}
			}
			ImageIO.write(bufferOutput, "png", new File(imageOut + ".png"));
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	public void processTileBlock(String fileIn, String imageOut, int imageBankStart, int numberOfTiles, int numberOfByteRows)
	{
		BufferedImage bufferOutput = new BufferedImage(numberOfTiles * 16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		if(bufferOutput == null)
		{
			bufferOutput = new BufferedImage(numberOfTiles * 16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		try { Thread.sleep(500); } catch(InterruptedException ie) { System.out.println(ie.getMessage()); }
		if(bufferOutput == null)
		{
			System.out.println("Unable to initialize BufferedImage");
			System.exit(1);
		}
		try
		{
			Graphics2D g = (Graphics2D)(bufferOutput.getGraphics());
			g.setComposite(AlphaComposite.Clear);
			g.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
			g.fillRect(0, 0, bufferOutput.getWidth(), bufferOutput.getHeight());
			g.setComposite(AlphaComposite.SrcOver);
			g.setColor(TIGlobals.TI_COLOR_BLACK);
			DataInputStream disReader = new DataInputStream(new FileInputStream(new File(fileIn)));
			disReader.skipBytes(imageBankStart);
			for(int imagetiles = 0; imagetiles < numberOfTiles; imagetiles++)
			{
				for(int tilepiece = 0; tilepiece < 4; tilepiece++)
				{
					byte[] dataBytes = new byte[8];
					disReader.read(dataBytes);
					for(int i = 0; i < dataBytes.length; i++)
					{
						byte b = dataBytes[i];
						int currBaseX = imagetiles * 16 + ((tilepiece == 2) || (tilepiece == 3) ? 8 : 0);
						int currBaseY = ((tilepiece == 1) || (tilepiece == 3) ? 8 : 0) + i;
						if((b & 128) > 0) { g.fillRect(currBaseX,     currBaseY, 1, 1); }
						if((b &  64) > 0) { g.fillRect(currBaseX + 1, currBaseY, 1, 1); }
						if((b &  32) > 0) { g.fillRect(currBaseX + 2, currBaseY, 1, 1); }
						if((b &  16) > 0) { g.fillRect(currBaseX + 3, currBaseY, 1, 1); }
						if((b &   8) > 0) { g.fillRect(currBaseX + 4, currBaseY, 1, 1); }
						if((b &   4) > 0) { g.fillRect(currBaseX + 5, currBaseY, 1, 1); }
						if((b &   2) > 0) { g.fillRect(currBaseX + 6, currBaseY, 1, 1); }
						if((b &   1) > 0) { g.fillRect(currBaseX + 7, currBaseY, 1, 1); }
					}
				}
			}
			ImageIO.write(bufferOutput, "png", new File(imageOut + ".png"));
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	public void processTileUnits(String fileIn, String imageOut, int imageBankStart, int numberOfTiles, int numberOfByteRows)
	{
		BufferedImage bufferOutput  = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		if(bufferOutput == null)
		{
			bufferOutput = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		try { Thread.sleep(500); } catch(InterruptedException ie) { System.out.println(ie.getMessage()); }
		if(bufferOutput == null)
		{
			System.out.println("Unable to initialize BufferedImage");
			System.exit(1);
		}
		try
		{
			DataInputStream disReader = new DataInputStream(new FileInputStream(new File(fileIn)));
			disReader.skipBytes(imageBankStart);
			for(int imagetiles = 0; imagetiles < numberOfTiles; imagetiles++)
			{
				Graphics2D g = (Graphics2D)(bufferOutput.getGraphics());
				g.setComposite(AlphaComposite.Clear);
				g.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
				g.fillRect(0, 0, bufferOutput.getWidth(), bufferOutput.getHeight());
				g.setComposite(AlphaComposite.SrcOver);
				g.setColor(TIGlobals.TI_COLOR_BLACK);
				for(int tilepiece = 0; tilepiece < 4; tilepiece++)
				{
					byte[] dataBytes = new byte[8];
					disReader.read(dataBytes);
					for(int i = 0; i < dataBytes.length; i++)
					{
						byte b = dataBytes[i];
						int currBaseX = ((tilepiece == 2) || (tilepiece == 3) ? 8 : 0);
						int currBaseY = ((tilepiece == 1) || (tilepiece == 3) ? 8 : 0) + i;
						if((b & 128) > 0) { g.fillRect(currBaseX,     currBaseY, 1, 1); }
						if((b &  64) > 0) { g.fillRect(currBaseX + 1, currBaseY, 1, 1); }
						if((b &  32) > 0) { g.fillRect(currBaseX + 2, currBaseY, 1, 1); }
						if((b &  16) > 0) { g.fillRect(currBaseX + 3, currBaseY, 1, 1); }
						if((b &   8) > 0) { g.fillRect(currBaseX + 4, currBaseY, 1, 1); }
						if((b &   4) > 0) { g.fillRect(currBaseX + 5, currBaseY, 1, 1); }
						if((b &   2) > 0) { g.fillRect(currBaseX + 6, currBaseY, 1, 1); }
						if((b &   1) > 0) { g.fillRect(currBaseX + 7, currBaseY, 1, 1); }
					}
				}
				ImageIO.write(bufferOutput, "png", new File(imageOut + "_" + imagetiles + ".png"));
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	public void processTilePiece(String fileIn, String imageOut, int imageBankStart, int numberOfTiles, int numberOfByteRows)
	{
		BufferedImage bufferOutput = new BufferedImage(8, numberOfByteRows, BufferedImage.TYPE_INT_ARGB);
		if(bufferOutput == null)
		{
			bufferOutput = new BufferedImage(8, numberOfByteRows, BufferedImage.TYPE_INT_ARGB);
		}
		try { Thread.sleep(500); } catch(InterruptedException ie) { System.out.println(ie.getMessage()); }
		if(bufferOutput == null)
		{
			System.out.println("Unable to initialize BufferedImage");
			System.exit(1);
		}
		try
		{
			DataInputStream disReader = new DataInputStream(new FileInputStream(new File(fileIn)));
			disReader.skipBytes(imageBankStart);
			for(int imagetiles = 0; imagetiles < numberOfTiles; imagetiles++)
			{
				Graphics2D g = (Graphics2D)(bufferOutput.getGraphics());
				g.setComposite(AlphaComposite.Clear);
				g.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
				g.fillRect(0, 0, bufferOutput.getWidth(), bufferOutput.getHeight());
				g.setComposite(AlphaComposite.SrcOver);
				g.setColor(TIGlobals.TI_COLOR_BLACK);
				byte[] dataBytes = new byte[8];
				disReader.read(dataBytes);
				for(int i = 0; i < dataBytes.length; i++)
				{
					byte b = dataBytes[i];
					if((b & 128) > 0) { g.fillRect(0, i, 1, 1); }
					if((b &  64) > 0) { g.fillRect(1, i, 1, 1); }
					if((b &  32) > 0) { g.fillRect(2, i, 1, 1); }
					if((b &  16) > 0) { g.fillRect(3, i, 1, 1); }
					if((b &   8) > 0) { g.fillRect(4, i, 1, 1); }
					if((b &   4) > 0) { g.fillRect(5, i, 1, 1); }
					if((b &   2) > 0) { g.fillRect(6, i, 1, 1); }
					if((b &   1) > 0) { g.fillRect(7, i, 1, 1); }
				}
				ImageIO.write(bufferOutput, "png", new File(imageOut + "_" + imagetiles + ".png"));
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	public void processQuadUnits(String fileIn, String imageOut, int imageBankStart, int numberOfTiles, int numberOfByteRows)
	{
		BufferedImage bufferOutput1 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bufferOutput2 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bufferOutput3 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bufferOutput4 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		if(bufferOutput1 == null)
		{
			bufferOutput1 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		if(bufferOutput2 == null)
		{
			bufferOutput2 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		if(bufferOutput3 == null)
		{
			bufferOutput3 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		if(bufferOutput4 == null)
		{
			bufferOutput4 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		try { Thread.sleep(500); } catch(InterruptedException ie) { System.out.println(ie.getMessage()); }
		if(bufferOutput1 == null || bufferOutput2 == null || bufferOutput3 == null || bufferOutput4 == null)
		{
			System.out.println("Unable to initialize BufferedImage");
			System.exit(1);
		}
		try
		{
			DataInputStream disReader = new DataInputStream(new FileInputStream(new File(fileIn)));
			disReader.skipBytes(imageBankStart);
			for(int imagetiles = 0; imagetiles < numberOfTiles / 2; imagetiles++)
			{
				for(int imagepair = 0; imagepair < 2; imagepair++)
				{
					Graphics2D g = (Graphics2D)(bufferOutput1.getGraphics());
					g.setComposite(AlphaComposite.Clear);
					g.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
					g.fillRect(0, 0, bufferOutput1.getWidth(), bufferOutput1.getHeight());
					g.setComposite(AlphaComposite.SrcOver);
					g.setColor(TIGlobals.TI_COLOR_BLUE_DARK);

					Graphics2D g2 = (Graphics2D)(bufferOutput2.getGraphics());
					g2.setComposite(AlphaComposite.Clear);
					g2.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
					g2.fillRect(0, 0, bufferOutput2.getWidth(), bufferOutput2.getHeight());
					g2.setComposite(AlphaComposite.SrcOver);
					g2.setColor(TIGlobals.TI_COLOR_GREEN_DRK);

					Graphics2D g3 = (Graphics2D)(bufferOutput3.getGraphics());
					g3.setComposite(AlphaComposite.Clear);
					g3.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
					g3.fillRect(0, 0, bufferOutput3.getWidth(), bufferOutput3.getHeight());
					g3.setComposite(AlphaComposite.SrcOver);
					g3.setColor(TIGlobals.TI_COLOR_RED_DRK);

					Graphics2D g4 = (Graphics2D)(bufferOutput4.getGraphics());
					g4.setComposite(AlphaComposite.Clear);
					g4.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
					g4.fillRect(0, 0, bufferOutput4.getWidth(), bufferOutput4.getHeight());
					g4.setComposite(AlphaComposite.SrcOver);
					g4.setColor(TIGlobals.TI_COLOR_MAGENTA);

					for(int tilepiece = 0; tilepiece < 4; tilepiece++)
					{
						byte[] dataBytes = new byte[8];
						disReader.read(dataBytes);
						for(int i = 0; i < dataBytes.length; i++)
						{
							byte b = dataBytes[i];
							int currBaseX = ((tilepiece == 2) || (tilepiece == 3) ? 8 : 0);
							int currBaseY = ((tilepiece == 1) || (tilepiece == 3) ? 8 : 0) + i;
							if((b & 128) > 0) { g.fillRect(currBaseX,     currBaseY, 1, 1); g2.fillRect(currBaseX,     currBaseY, 1, 1); g3.fillRect(currBaseX,     currBaseY, 1, 1); g4.fillRect(currBaseX,     currBaseY, 1, 1); }
							if((b &  64) > 0) { g.fillRect(currBaseX + 1, currBaseY, 1, 1); g2.fillRect(currBaseX + 1, currBaseY, 1, 1); g3.fillRect(currBaseX + 1, currBaseY, 1, 1); g4.fillRect(currBaseX + 1, currBaseY, 1, 1); }
							if((b &  32) > 0) { g.fillRect(currBaseX + 2, currBaseY, 1, 1); g2.fillRect(currBaseX + 2, currBaseY, 1, 1); g3.fillRect(currBaseX + 2, currBaseY, 1, 1); g4.fillRect(currBaseX + 2, currBaseY, 1, 1); }
							if((b &  16) > 0) { g.fillRect(currBaseX + 3, currBaseY, 1, 1); g2.fillRect(currBaseX + 3, currBaseY, 1, 1); g3.fillRect(currBaseX + 3, currBaseY, 1, 1); g4.fillRect(currBaseX + 3, currBaseY, 1, 1); }
							if((b &   8) > 0) { g.fillRect(currBaseX + 4, currBaseY, 1, 1); g2.fillRect(currBaseX + 4, currBaseY, 1, 1); g3.fillRect(currBaseX + 4, currBaseY, 1, 1); g4.fillRect(currBaseX + 4, currBaseY, 1, 1); }
							if((b &   4) > 0) { g.fillRect(currBaseX + 5, currBaseY, 1, 1); g2.fillRect(currBaseX + 5, currBaseY, 1, 1); g3.fillRect(currBaseX + 5, currBaseY, 1, 1); g4.fillRect(currBaseX + 5, currBaseY, 1, 1); }
							if((b &   2) > 0) { g.fillRect(currBaseX + 6, currBaseY, 1, 1); g2.fillRect(currBaseX + 6, currBaseY, 1, 1); g3.fillRect(currBaseX + 6, currBaseY, 1, 1); g4.fillRect(currBaseX + 6, currBaseY, 1, 1); }
							if((b &   1) > 0) { g.fillRect(currBaseX + 7, currBaseY, 1, 1); g2.fillRect(currBaseX + 7, currBaseY, 1, 1); g3.fillRect(currBaseX + 7, currBaseY, 1, 1); g4.fillRect(currBaseX + 7, currBaseY, 1, 1); }
						}
					}
					if(imagepair == 0)
					{
						ImageIO.write(bufferOutput1, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[0] + "_normal.png"));
						ImageIO.write(bufferOutput2, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[1] + "_normal.png"));
						ImageIO.write(bufferOutput3, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[2] + "_normal.png"));
						ImageIO.write(bufferOutput4, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[3] + "_normal.png"));
					}
					else
					{
						ImageIO.write(bufferOutput1, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[0] + "_attack.png"));
						ImageIO.write(bufferOutput2, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[1] + "_attack.png"));
						ImageIO.write(bufferOutput3, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[2] + "_attack.png"));
						ImageIO.write(bufferOutput4, "png", new File(imageOut + "_" + imagetiles + "_" + TIGlobals.PLAYER_COLORS[3] + "_attack.png"));
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	public void processPairUnits(String fileIn, String imageOut, int imageBankStart, int numberOfTiles, int numberOfByteRows)
	{
		BufferedImage bufferOutput  = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		BufferedImage bufferOutput2 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		if(bufferOutput == null)
		{
			bufferOutput = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		if(bufferOutput2 == null)
		{
			bufferOutput2 = new BufferedImage(16, numberOfByteRows * 2, BufferedImage.TYPE_INT_ARGB);
		}
		try { Thread.sleep(500); } catch(InterruptedException ie) { System.out.println(ie.getMessage()); }
		if(bufferOutput == null || bufferOutput2 == null)
		{
			System.out.println("Unable to initialize BufferedImage");
			System.exit(1);
		}
		try
		{
			DataInputStream disReader = new DataInputStream(new FileInputStream(new File(fileIn)));
			disReader.skipBytes(imageBankStart);
			for(int imagetiles = 0; imagetiles < numberOfTiles / 2; imagetiles++)
			{
				for(int imagepair = 0; imagepair < 2; imagepair++)
				{
					Graphics2D g = (Graphics2D)(bufferOutput.getGraphics());
					g.setComposite(AlphaComposite.Clear);
					g.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
					g.fillRect(0, 0, bufferOutput.getWidth(), bufferOutput.getHeight());
					g.setComposite(AlphaComposite.SrcOver);
					g.setColor(TIGlobals.TI_COLOR_BLACK);
					Graphics2D g2 = (Graphics2D)(bufferOutput2.getGraphics());
					g2.setComposite(AlphaComposite.Clear);
					g2.setColor(TIGlobals.TI_COLOR_TRANSPARENT);
					g2.fillRect(0, 0, bufferOutput2.getWidth(), bufferOutput2.getHeight());
					g2.setComposite(AlphaComposite.SrcOver);
					g2.setColor(TIGlobals.TI_COLOR_MAGENTA);
					for(int tilepiece = 0; tilepiece < 4; tilepiece++)
					{
						byte[] dataBytes = new byte[8];
						disReader.read(dataBytes);
						for(int i = 0; i < dataBytes.length; i++)
						{
							byte b = dataBytes[i];
							int currBaseX = ((tilepiece == 2) || (tilepiece == 3) ? 8 : 0);
							int currBaseY = ((tilepiece == 1) || (tilepiece == 3) ? 8 : 0) + i;
							if((b & 128) > 0) { g.fillRect(currBaseX,     currBaseY, 1, 1); g2.fillRect(currBaseX,     currBaseY, 1, 1); }
							if((b &  64) > 0) { g.fillRect(currBaseX + 1, currBaseY, 1, 1); g2.fillRect(currBaseX + 1, currBaseY, 1, 1); }
							if((b &  32) > 0) { g.fillRect(currBaseX + 2, currBaseY, 1, 1); g2.fillRect(currBaseX + 2, currBaseY, 1, 1); }
							if((b &  16) > 0) { g.fillRect(currBaseX + 3, currBaseY, 1, 1); g2.fillRect(currBaseX + 3, currBaseY, 1, 1); }
							if((b &   8) > 0) { g.fillRect(currBaseX + 4, currBaseY, 1, 1); g2.fillRect(currBaseX + 4, currBaseY, 1, 1); }
							if((b &   4) > 0) { g.fillRect(currBaseX + 5, currBaseY, 1, 1); g2.fillRect(currBaseX + 5, currBaseY, 1, 1); }
							if((b &   2) > 0) { g.fillRect(currBaseX + 6, currBaseY, 1, 1); g2.fillRect(currBaseX + 6, currBaseY, 1, 1); }
							if((b &   1) > 0) { g.fillRect(currBaseX + 7, currBaseY, 1, 1); g2.fillRect(currBaseX + 7, currBaseY, 1, 1); }
						}
					}
					if(imagepair == 0)
					{
						ImageIO.write(bufferOutput, "png", new File(imageOut + "_" + imagetiles + "_normal.png"));
					}
					else
					{
						ImageIO.write(bufferOutput,  "png", new File(imageOut + "_" + imagetiles + "_attack.png"));
						ImageIO.write(bufferOutput2, "png", new File(imageOut + "_" + imagetiles + "_effect.png"));
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	public boolean isProcessing() { return processing; }

	public static void main(String[] args)
	{
		if(args.length < 2)
		{
			System.out.println("usage: java com.dreamcodex.todr.util.TODimager DISK FILENAME [IMAGEPREFIX]");
			System.out.println("          DISK        = source .dsk disk image file");
			System.out.println("          FILENAME    = name of ToD game/save file to parse");
			System.out.println("          IMAGEPREFIX = prefix to use when naming images (optional, if omitted will be FILENAME)");
			System.exit(0);
		}
		else
		{
			TODimager todim = new TODimager(args[0], args[1], (args.length > 2 ? args[2] : args[1]));
		}
	}
}
