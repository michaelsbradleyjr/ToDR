package com.dreamcodex.todr.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;
import com.dreamcodex.todr.object.*;
import com.dreamcodex.todr.util.*;

public class ViewLevel extends Frame implements WindowListener, KeyListener
{
	private final int scale = 16;

	private Vector vcLevels;
	private int    currLevel = 0;

	private BufferedImage bufferDisplay;

	public ViewLevel(String mapfile)
	{
		vcLevels = ObjectParser.parseLevelsFile(mapfile);

		this.addWindowListener(this);
		this.addKeyListener(this);

		bufferDisplay = new BufferedImage(((Board)(vcLevels.elementAt(0))).getWidth() * scale, ((Board)(vcLevels.elementAt(0))).getHeight() * scale, BufferedImage.TYPE_INT_RGB);

		this.setSize(500, 400);
		this.show();

		showMapScreen(currLevel);
	}

	public ViewLevel()
	{
		this("com" + File.separator + "dreamcodex" + File.separator + "todr" + File.separator + "util" + File.separator + "plan.txt");
	}

	/* WindowListener methods */
	public void windowClosing(WindowEvent we)     { this.dispose(); System.exit(0); }
	public void windowClosed(WindowEvent we)      { ; }
	public void windowOpened(WindowEvent we)      { ; }
	public void windowIconified(WindowEvent we)   { ; }
	public void windowDeiconified(WindowEvent we) { ; }
	public void windowActivated(WindowEvent we)   { ; }
	public void windowDeactivated(WindowEvent we) { ; }

	/* KeyListener methods */
	public void keyPressed(KeyEvent ke)
	{
		if(ke.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			System.exit(0);
		}
		else if(ke.getKeyCode() == KeyEvent.VK_LEFT)
		{
			currLevel--;
			if(currLevel < 0)
			{
				currLevel = vcLevels.size() - 1;
			}
			showMapScreen(currLevel);
		}
		else if(ke.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			currLevel++;
			if(currLevel >= vcLevels.size())
			{
				currLevel = 0;
			}
			showMapScreen(currLevel);
		}
		else if(ke.getKeyCode() == KeyEvent.VK_SPACE)
		{
			showMapScreen(currLevel);
		}
	}
	public void keyReleased(KeyEvent ke) { ; }
	public void keyTyped(KeyEvent ke)    { ; }

	public void showMapScreen(int level)
	{
		if(bufferDisplay == null)
		{
			bufferDisplay = new BufferedImage(((Board)(vcLevels.elementAt(0))).getWidth() * scale, ((Board)(vcLevels.elementAt(0))).getHeight() * scale, BufferedImage.TYPE_INT_RGB);
		}

		Graphics g  = bufferDisplay.getGraphics();
		Graphics g2 = this.getGraphics();

		g.setColor(new Color(255, 255, 212));
		g.fillRect(0, 0, bufferDisplay.getWidth(this), bufferDisplay.getHeight(this));

		Board currBoard = (Board)(vcLevels.elementAt(level));
		Room  currRoom  = (Room)null;

		for(int y = 0; y < currBoard.getHeight(); y++)
		{
			for(int x = 0; x < currBoard.getWidth(); x++)
			{
				g.setColor(new Color(255, 255, 212));
				g.fillRect(x * scale, y * scale, scale, scale);
				int tileVal = currBoard.getGridValue(x, y);
				g.setColor(new Color(0, 0, 0));
				if(tileVal == Globals.MAP_SOLID)
				{
//					g.setColor(new Color(128, 128, 128));
//					g.fillRect(x * scale, y * scale, scale, scale);
				}
				else if(tileVal == Globals.MAP_ROOM)
				{
//					g.setColor(new Color(255, 192, 164));
					g.fillRect(x * scale, y * scale, scale, scale);
					g.setColor(new Color(255, 255, 128));
					g.fillRect(x * scale + (scale / 4), y * scale + (scale / 4), (scale / 2), (scale / 2));
					currRoom = currBoard.getRoom(x, y);
					if(currRoom != null)
					{
						if(currRoom.getStairType() == Globals.STAIRS_UP)
						{
							g.setColor(new Color(255, 128, 128));
							g.fillRect(x * scale + (scale / 4), y * scale + (scale / 4), (scale / 2), (scale / 2));
						}
						else if(currRoom.getStairType() == Globals.STAIRS_DOWN)
						{
							g.setColor(new Color(128, 255, 128));
							g.fillRect(x * scale + (scale / 4), y * scale + (scale / 4), (scale / 2), (scale / 2));
						}
						if(currRoom.getFeatureType() == Globals.FEATURE_VAULT)
						{
							g.setColor(new Color(128, 128, 128));
							g.fillRect(x * scale + (scale / 4), y * scale + (scale / 4), (scale / 2), (scale / 2));
						}
						else if(currRoom.getFeatureType() == Globals.FEATURE_FOUNTAIN)
						{
							g.setColor(new Color(128, 128, 255));
							g.fillRect(x * scale + (scale / 4), y * scale + (scale / 4), (scale / 2), (scale / 2));
						}
						else if(currRoom.getFeatureType() == Globals.FEATURE_STATUE)
						{
							g.setColor(new Color(255, 128, 255));
							g.fillRect(x * scale + (scale / 4), y * scale + (scale / 4), (scale / 2), (scale / 2));
						}
						else if(currRoom.getFeatureType() == Globals.FEATURE_SHOP)
						{
							g.setColor(new Color(128, 128, 32));
							g.fillRect(x * scale + (scale / 4), y * scale + (scale / 4), (scale / 2), (scale / 2));
						}
					}
				}
				else
				{
//					g.setColor(new Color(128, 128, 255));
//					g.fillRect(x * scale, y * scale, scale, scale);
					if((tileVal & Globals.FACINGS[Globals.NORTH]) == Globals.FACINGS[Globals.NORTH])
					{
						g.fillRect(x * scale + (scale / 4) + (int)(scale / 8), y * scale, (scale / 4), (scale / 2));
					}
					if((tileVal & Globals.FACINGS[Globals.SOUTH]) == Globals.FACINGS[Globals.SOUTH])
					{
						g.fillRect(x * scale + (scale / 4) + (int)(scale / 8), y * scale + (scale / 2), (scale / 4), (scale / 2));
					}
					if((tileVal & Globals.FACINGS[Globals.EAST]) == Globals.FACINGS[Globals.EAST])
					{
						g.fillRect(x * scale + (scale / 2), y * scale + (scale / 4) + (scale / 8), (scale / 2), (scale / 4));
					}
					if((tileVal & Globals.FACINGS[Globals.WEST]) == Globals.FACINGS[Globals.WEST])
					{
						g.fillRect(x * scale, y * scale + (scale / 4) + (scale / 8), (scale / 2), (scale / 4));
					}
				}
			}
		}

		g2.setColor(new Color(96, 128, 96));
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		g2.drawImage(bufferDisplay, (this.getWidth() - bufferDisplay.getWidth(this)) / 2, (this.getHeight() - bufferDisplay.getHeight(this)) / 2, this);
	}

	public static void main(String[] args)
	{
		if(args.length < 1)
		{
			ViewLevel vc = new ViewLevel();
		}
		else
		{
			ViewLevel vc = new ViewLevel(args[0]);
		}
	}
}