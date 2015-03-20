package com.dreamcodex.todr.object;

import java.awt.Image;
import java.util.StringTokenizer;

import com.dreamcodex.todr.util.ObjectParser;
import com.dreamcodex.util.Coord;

public class Quest
{
	protected String  name;
	protected String  description;
	protected int     turns;
	protected boolean required; // is this quest required to appear every time this module is played?
	protected boolean partial; // if true, quest can still be partially completed after time runs out
	protected int     level;
	protected Coord   room;
	protected String  fixedlevel; // if not equal to Globals.QUEST_NONE, quest is only ever found on the level with this name
	protected Image   img;
	protected String  imgname;
	protected String  msgComplete;
	protected String  msgFinished;
	protected String  msgFailed;
	protected int     status;

	public static String NO_FIXED_LEVEL = "[none]";

	public Quest(String name, String description, int turns, boolean required, boolean partial, int level, Coord room, String fixedlevel, Image img, String imgname, String complete, String finished, String failed, int status)
	{
		this.name        = new String(name);
		this.description = new String(description);
		this.turns       = turns;
		this.required    = required;
		this.partial     = partial;
		this.level       = level;
		this.room        = new Coord(room);
		this.fixedlevel  = new String(fixedlevel);
		this.img         = img;
		this.imgname     = imgname;
		this.msgComplete = new String(complete);
		this.msgFinished = new String(finished);
		this.msgFailed   = new String(failed);
		this.status      = status;
	}

	public Quest(String name, String description, int turns, boolean required, boolean partial, int level, Coord room, String fixedlevel, Image img, String imgname, String complete, String finished, String failed)
	{
		this(name, description, turns, required, partial, level, room, fixedlevel, img, imgname, complete, finished, failed, Globals.QUEST_ONGOING);
	}

	public Quest(Quest q)
	{
		this(q.getName(), q.getDescription(), q.getTurns(), q.isRequired(), q.isPartial(), q.getLevel(), q.getRoom(), q.getFixedLevel(), q.getImage(), q.getImageName(), q.getMessageComplete(), q.getMessageFinished(), q.getMessageFailed(), q.getStatus());
	}

	public String  getName()            { return name; }
	public String  getDescription()     { return description; }
	public int     getTurns()           { return turns; }
	public boolean isRequired()         { return required; }
	public boolean isPartial()          { return partial; }
	public int     getLevel()           { return level; }
	public Coord   getRoom()            { return room; }
	public String  getFixedLevel()      { return fixedlevel; }
	public Image   getImage()           { return img; }
	public String  getImageName()       { return imgname; }
	public String  getMessageComplete() { return msgComplete; }
	public String  getMessageFinished() { return msgFinished; }
	public String  getMessageFailed()   { return msgFailed; }
	public int     getStatus()          { return status; }

	public void setName(String s)            { name = new String(s); }
	public void setDescription(String s)     { description = new String(s); }
	public void setTurns(int i)              { turns = i; }
	public void setRequired(boolean b)       { required = b; }
	public void setPartial(boolean b)        { partial = b; }
	public void setLevel(int i)              { level = i; }
	public void setRoom(Coord cp)            { room.setLocation(cp); }
	public void setFixedLevel(String s)      { fixedlevel = new String(s); }
	public void setImage(Image i)            { img = i; }
	public void setImageName(String s)       { imgname = s; }
	public void setMessageComplete(String s) { msgComplete = new String(s); }
	public void setMessageFinished(String s) { msgFinished = new String(s); }
	public void setMessageFailed(String s)   { msgFailed = new String(s); }
	public void setStatus(int i)             { status = i; }

	public String getNotificationString()
	{
		String baseNotification = name + "_ _" + Globals.getDungeonText("quest" + status) + "_ _";
		if(this.getStatus() == Globals.QUEST_SUCCESS)
		{
			return baseNotification + msgComplete;
		}
		else if(this.getStatus() == Globals.QUEST_FINISHED)
		{
			return baseNotification + msgFinished;
		}
		else if(this.getStatus() == Globals.QUEST_FAILED)
		{
			return baseNotification + msgFailed;
		}
		return baseNotification + description;
	}

	public String getSaveString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(this.getName() + "|");
		sb.append(this.getStatus() + "|");
		sb.append((this.isRequired() ? "Y" : "N") + "|");
		sb.append((this.isPartial() ? "Y" : "N") + "|");
		sb.append(this.getTurns() + "|");
		sb.append(this.getLevel() + "|");
		sb.append(this.getRoom().toString() + "|");
		sb.append(this.getFixedLevel() + "|");
		sb.append(this.getImageName() + "|");
		sb.append(this.getDescription() + "|");
		sb.append(this.getMessageComplete() + "|");
		sb.append(this.getMessageFinished() + "|");
		sb.append(this.getMessageFailed());
		return sb.toString();
	}

	public static Quest createFromString(String strq)
	{
		StringTokenizer stParse = new StringTokenizer(strq, "|", false);
		String  name        = stParse.nextToken();
		int     status      = Integer.parseInt(stParse.nextToken());
		boolean required    = stParse.nextToken().equals("Y");
		boolean partial     = stParse.nextToken().equals("Y");
		int     turns       = Integer.parseInt(stParse.nextToken());
		int     level       = Integer.parseInt(stParse.nextToken());
		Coord   room        = new Coord(stParse.nextToken());
		String  fixedlevel  = stParse.nextToken();
		String  imgname     = stParse.nextToken();
		Image   img         = ObjectParser.loadModuleImage(imgname);
		String  description = stParse.nextToken();
		String  complete    = stParse.nextToken();
		String  finished    = stParse.nextToken();
		String  failed      = stParse.nextToken();
		return new Quest(name, description, turns, required, partial, level, room, fixedlevel, img, imgname, complete, finished, failed, status);
	}
}
