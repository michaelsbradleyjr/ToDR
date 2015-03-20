package com.dreamcodex.todr.object;

import java.awt.Image;

import com.dreamcodex.todr.object.Globals;
import com.dreamcodex.todr.object.ItemClass;

public class QuestItem extends BaseItem
{
	protected int   questNum; // corresponding Quest number
	protected Image questImg; // image to use in game

	public QuestItem(String name, int num, Image img)
	{
		super(Globals.ITEMTYPE_QUEST, name, Globals.ITEMCLASS_QUEST, 0, Globals.ITEMCLASS_QUEST.getPermissions());
		this.questNum = num;
		this.questImg = img;
	}

	public QuestItem(QuestItem questToClone)
	{
		this(questToClone.getName(), questToClone.getQuestNumber(), questToClone.getQuestImage());
	}

	public int   getQuestNumber() { return questNum; }
	public Image getQuestImage()  { return questImg; }

	public void setQuestNumber(int i)    { questNum = i; }
	public void setQuestImage(Image img) { questImg = img; }

	public Image getImage()
	{
		if(this.getQuestImage() != null)
		{
			return this.getQuestImage();
		}
		else
		{
			return this.getItemClass().getImage();
		}
	}

	public String getIngameName()
	{
		return this.getName();
	}

	public String getInventoryText()
	{
		return this.getIngameName();
	}

	public QuestItem getInstance()
	{
		return new QuestItem(this.getName(), this.getQuestNumber(), this.getQuestImage());
	}

	public String getSaveString()
	{
		return "Q" + questNum;
	}

	public static QuestItem createFromString(String strp)
	{
		int    qNum  = Integer.parseInt(strp);
		String qName = Globals.QUESTLOG.elementAt(qNum).getName();
		Image  qImg  = Globals.QUESTLOG.elementAt(qNum).getImage();
		return new QuestItem(qName, qNum, qImg);
	}
}