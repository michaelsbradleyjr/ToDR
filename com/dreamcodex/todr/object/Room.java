package com.dreamcodex.todr.object;

import java.util.Collections;
import java.util.Vector;

import com.dreamcodex.todr.object.BaseItem;
import com.dreamcodex.todr.object.ItemContainer;
import com.dreamcodex.todr.object.Chest;
import com.dreamcodex.todr.object.Vault;
import com.dreamcodex.todr.object.Monster;
import com.dreamcodex.todr.object.Transactor;

public class Room implements Transactor
{
	protected int              stairs;
	protected int              feature;
	protected int              planKey;
	protected Vector<Monster>  vcMonsters;
	protected Vector<BaseItem> vcItems;
	protected ItemContainer    itmcon;
	protected boolean          bTransient; // if true, this is a "temporary" room, like a hallway segment

	@SuppressWarnings(value = "unchecked")
	public Room(int stairType, int featureType, int floorplan, Vector<Monster> m, Vector<BaseItem> i, ItemContainer ic, boolean temp)
	{
		stairs     = stairType;
		feature    = featureType;
		planKey    = floorplan;
		vcMonsters = new Vector<Monster>(m);
		vcItems    = new Vector<BaseItem>(i);
		if(vcItems != null && vcItems.size() > 1) { Collections.sort(vcItems); }
		itmcon     = ic;
		bTransient = temp;
	}

	public Room(int stairType, int featureType, int floorplan, Vector<Monster> m, Vector<BaseItem> i, ItemContainer ic)
	{
		this(stairType, featureType, floorplan, m, i, ic, false);
	}

	public Room(int stairType, int featureType, int floorplan, Vector<Monster> m, Vector<BaseItem> i, boolean temp)
	{
		this(stairType, featureType, floorplan, m, i, (ItemContainer)null, temp);
	}

	public Room(int stairType, int featureType, int floorplan, Vector<Monster> m, Vector<BaseItem> i)
	{
		this(stairType, featureType, floorplan, m, i, (ItemContainer)null);
	}

	public Room(int stairType, int featureType, Vector<Monster> m, Vector<BaseItem> i, ItemContainer ic)
	{
		this(stairType, featureType, 1, m, i, ic);
	}

	public Room(int stairType, int featureType, Vector<Monster> m, Vector<BaseItem> i)
	{
		this(stairType, featureType, m, i, (ItemContainer)null);
	}

	public Room(int stairType, int featureType)
	{
		this(stairType, featureType, new Vector<Monster>(), new Vector<BaseItem>());
	}

	public Room()
	{
		this(Globals.STAIRS_NONE, Globals.FEATURE_NONE);
	}

	public int              getStairType()     { return stairs; }
	public int              getFeatureType()   { return feature; }
	public int              getPlanKey()       { return planKey; }
	public Vector<Monster>  getMonsters()      { return vcMonsters; }
	public Monster          getMonster(int i)  { if(i >= 0 && i <= vcMonsters.size()) { return vcMonsters.elementAt(i);} else { return (Monster)null; } }
	public int              getMonsterCount()  { return vcMonsters.size(); }
	public Vector<BaseItem> getItems()         { return vcItems; }
	public BaseItem         getItem(int i)     { if(i >= 0 && i <= vcItems.size()) { return vcItems.elementAt(i);} else { return (BaseItem)null; } }
	public int              getItemCount()     { return vcItems.size(); }
	public boolean          hasItemContainer() { return (itmcon != null); }
	public ItemContainer    getItemContainer() { return itmcon; }
	public boolean          hasChest()         { return (itmcon != null && itmcon instanceof Chest); }
	public Chest            getChest()         { return (Chest)itmcon; }
	public boolean          hasVault()         { return (itmcon != null && itmcon instanceof Vault); }
	public Vault            getVault()         { return (Vault)itmcon; }
	public boolean          isTransient()      { return bTransient; }

	public void addItemContainer(ItemContainer ic)  { itmcon = ic; }

	public void setStairType(int i)   { stairs = i; }
	public void setFeatureType(int i) { feature = i; }
	public void setChestTrap(int i)   { ((Chest)itmcon).setTrapNum(i); }
	public void setVaultCode(int i)   { ((Vault)itmcon).setCode(i); }

	/* Transactor interface methods */
	@SuppressWarnings(value = "unchecked")
	public BaseItem gainItem(BaseItem item)
	{
		if(item == null)
		{
			return item;
		}
		if(vcItems == null)
		{
			vcItems = new Vector<BaseItem>();
		}
		if(vcItems.size() < Globals.MAX_FLOOR_STACK)
		{
			// if "mergeable" item, like Currency or Ration, check for other "mergeable" items and combine
			if(vcItems.size() > 0 && (item.getType() == Globals.ITEMTYPE_CURRENCY || item.getType() == Globals.ITEMTYPE_RATIONS))
			{
				for(int i = 0; i < vcItems.size(); i++)
				{
					BaseItem biPtr = vcItems.elementAt(i);
					if(biPtr.getType() == item.getType())
					{
						((Item)biPtr).getEffect().setPower(((Item)biPtr).getEffect().getPower() + ((Item)item).getEffect().getPower());
						i = vcItems.size();
						item = (BaseItem)null;
					}
				}
				if(item != null)
				{
					vcItems.add(item);
				}
			}
			else
			{
				vcItems.add(item);
			}
			Collections.sort(vcItems);
			return (BaseItem)null;
		}
		else
		{
			return item;
		}
	}

	@SuppressWarnings(value = "unchecked")
	public boolean loseItem(BaseItem item)
	{
		if(vcItems.contains(item))
		{
			vcItems.remove(item);
			Collections.sort(vcItems);
			return true;
		}
		else
		{
			return false;
		}
	}

	public void addMonsters(Vector<Monster> m) { vcMonsters.addAll(m); }
	public void addMonster(Monster m)
	{
		if(vcMonsters == null)
		{
			vcMonsters = new Vector<Monster>();
		}
		vcMonsters.add(m);
	}
	public void delMonster(int index) { vcMonsters.removeElementAt(index); }

	public Vector addItems(Vector vc)
	{
		BaseItem biTemp = (BaseItem)null;
		while(vc.size() > 0 && biTemp == null)
		{
			biTemp = this.gainItem((BaseItem)(vc.elementAt(0)));
			vc.removeElementAt(0);
		}
		return vc;
	}

	public boolean hasActiveMonsters()
	{
		if(vcMonsters == null || vcMonsters.size() < 1)
		{
			return false;
		}
		for(int m = 0; m < vcMonsters.size(); m++)
		{
			if(vcMonsters.elementAt(m).isAlive())
			{
				return true;
			}
		}
		return false;
	}

	public void openItemContainer()
	{
		if(itmcon != null)
		{
			boolean roomToDrop = true;
			while(itmcon.getContentCount() > 0 && roomToDrop)
			{
				// add new item to room
				BaseItem biTest = this.gainItem(itmcon.getContentItem(0));
				if(biTest != null)
				{
					// no more room for items, so end loop
					roomToDrop = false;
				}
				else
				{
					// remove item from contents
					itmcon.delContentItem(0);
				}
			}
			// remove chest from room if empty
			if(itmcon.getContentCount() <= 0)
			{
				itmcon = (ItemContainer)null;
			}
		}
	}
}