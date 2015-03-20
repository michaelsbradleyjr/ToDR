package com.dreamcodex.todr.object;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

import com.dreamcodex.todr.object.Item;
import com.dreamcodex.todr.object.Armor;
import com.dreamcodex.todr.object.Weapon;
import com.dreamcodex.todr.object.Transactor;
import com.dreamcodex.todr.util.ObjectParser;
import com.dreamcodex.util.MP3Clip;

public class Player extends Lifeform implements Transactor
{
	protected final int transpixel = 0x00000000;

	protected int           characterClassNum;
	protected int           characterDiff;
	protected int           luck;
	protected int           meleeBonus;
	protected int           rangeBonus;
	protected int           armorBonus;
	protected int           trapResist;
	protected Weapon[]      weapons = new Weapon[2];
	protected int           activeWeapon;
	protected Armor         armorSlotBody;
	protected Armor         armorSlotSpecial;
	protected Ammo[]        ammoSatchel;
	protected Vector<Item>  vcInventory;
	protected BufferedImage bimgRender; // for building a displayable version of this player

	public Player(int cc, int diff, String name, int lvl, int exp, int hp, int wnd, int luk, int mbns, int rbns, int abns, int trap, int spd, int res)
	{
		super(Globals.LIFEFORM_PLAYER, name, lvl, exp, hp, wnd, 0, 0, spd, res);
		characterClassNum = cc;
		characterDiff     = diff;
		luck              = luk;
		meleeBonus        = mbns;
		rangeBonus        = rbns;
		armorBonus        = abns;
		trapResist        = trap;
		for(int w = 0; w < 2; w++)
		{
			weapons[w] = Globals.WEAPON_DEFAULT;
		}
		activeWeapon      = 0;
		armorSlotBody     = Globals.ARMOR_BODY_DEFAULT;
		armorSlotSpecial  = Globals.ARMOR_SPEC_DEFAULT;
		ammoSatchel       = new Ammo[Globals.AMMOLIST.size()];
		vcInventory       = new Vector<Item>();
		bimgRender        = new BufferedImage(Globals.TILESCALE * 2, Globals.TILESCALE * 2, BufferedImage.TYPE_INT_ARGB);
		while(bimgRender == null) { try { Thread.sleep(20); } catch(Exception e) {} }
		for(int am = 0; am < ammoSatchel.length; am++) { ammoSatchel[am] = (Ammo)null; }
	}

	public Player(int cc, int diff, String name)
	{
		super(Globals.LIFEFORM_PLAYER, name, 1, 0, Globals.CHARACTER_CLASSES[cc].getBaseHitpoints(), 0, 0, 0, Globals.CHARACTER_CLASSES[cc].getBaseSpeed(), Globals.CHARACTER_CLASSES[cc].getBaseResistance());
		characterClassNum = cc;
		characterDiff     = diff;
		luck              = Globals.CHARACTER_CLASSES[cc].getBaseLuck();
		meleeBonus        = Globals.CHARACTER_CLASSES[cc].getBaseMeleeBonus();
		rangeBonus        = Globals.CHARACTER_CLASSES[cc].getBaseRangeBonus();
		armorBonus        = Globals.CHARACTER_CLASSES[cc].getBaseArmorBonus();
		trapResist        = Globals.CHARACTER_CLASSES[cc].getBaseTrapResist();
		for(int w = 0; w < 2; w++)
		{
			weapons[w] = Globals.WEAPON_DEFAULT;
		}
		activeWeapon      = 0;
		armorSlotBody     = Globals.ARMOR_BODY_DEFAULT;
		armorSlotSpecial  = Globals.ARMOR_SPEC_DEFAULT;
		ammoSatchel       = new Ammo[Globals.AMMOLIST.size()];
		vcInventory       = new Vector<Item>();
		for(int am = 0; am < ammoSatchel.length; am++) { ammoSatchel[am] = (Ammo)null; }
	}

	public Player()
	{
		this(0, 0, "[default]");
	}

	public CharacterClass getCharacterClass()     { return Globals.CHARACTER_CLASSES[characterClassNum]; }
	public int            getCharacterClassNum()  { return characterClassNum; }
	public int            getCharacterDiff()      { return characterDiff; }
	public int            getMeleeAdd()           { return getCharacterClass().getMeleeAdd(); }
	public int            getRangeAdd()           { return getCharacterClass().getRangeAdd(); }
	public int            getLuck()               { return luck; }
	public int            getMeleeBonus()         { return meleeBonus; }
	public int            getRangeBonus()         { return rangeBonus; }
	public int            getArmorBonus()         { return armorBonus; }
	public int            getTrapResist()         { return trapResist; }
	public Weapon[]       getWeapons()            { return weapons; }
	public Weapon         getWeapon(int index)    { return weapons[index]; }
	public Weapon         getEquiptWeapon()       { return weapons[activeWeapon]; }
	public Weapon         getAlternWeapon()       { return weapons[1 - activeWeapon]; }
	public int            getEquiptWeaponIndex()  { return activeWeapon; }
	public int            getAlternWeaponIndex()  { return (1 - activeWeapon); }
	public Armor          getArmorSlotBody()      { return armorSlotBody; }
	public Armor          getArmorSlotSpec()      { return armorSlotSpecial; }
	public Item           getInventoryItem(int i) { return vcInventory.elementAt(i); }
	public int            getInventorySize()      { return vcInventory.size(); }

	public void setCharacterClassNum(int i)    { characterClassNum = i; }
	public void setCharacterDiff(int i)        { characterDiff = i; }
	public void setLuck(int i)                 { luck = i; }
	public void setMeleeBonus(int i)           { meleeBonus = i; }
	public void setRangeBonus(int i)           { rangeBonus = i; }
	public void setArmorBonus(int i)           { armorBonus = i; }
	public void setTrapResist(int i)           { trapResist = i; }
	public void setWeapons(Weapon[] ws)        { weapons = ws; }
	public void setWeapon(Weapon w, int index) { weapons[index] = w; }
	public void setArmorSlotBody(Armor a)      { armorSlotBody = a; }
	public void setArmorSlotSpecial(Armor a)   { armorSlotSpecial = a; }

	public String getCharacterClassName() { return getCharacterClass().getName(); }

	public int getCurrentAttack()  { return weapons[activeWeapon].getCurrDmg() + (weapons[activeWeapon].isRanged() ? rangeBonus : meleeBonus); }
	public int getCurrentDefense() { return getArmorSlotBody().getCurrProt() + getArmorSlotSpec().getCurrProt() + getArmorBonus(); }

	// override methods in Lifeform, for now, until we determine if this is really the case
	public int getAttackRank() { return level; }
	public int getDefendRank() { return level; }

	public MP3Clip getAttackSound() { return Globals.CURR_DUNGEON.getWeaponSound(getEquiptWeapon().getSoundNumber()); }

	public int getAttackValue() { return Globals.getDiceValue(getCurrentAttack()); }
	public int getDefendValue() { return getCurrentDefense(); }

	// override Lifeform methods, replaces innate values with Weapon values
	public int   getProjectileType()  { return this.getEquiptWeapon().getProjectileType(); }
	public Color getProjectileColor() { return this.getEquiptWeapon().getProjectileColor(); }

	public int getExpForLevel(int lvl)
	{
		int lvlTot = 0;
		for(int i = 1; i < lvl; i++)
		{
			lvlTot += i * 400;
		}
		return lvlTot;
	}

	public int getExpForNextLevel()
	{
		return getExpForLevel(this.getLevel() + 1);
	}

	public void alterLuck(int delta)
	{
		luck = Math.max(Math.min(luck + delta, Globals.MAX_PLAYER_LUCK), -Globals.MAX_PLAYER_LUCK);
	}

	public void alterMeleeBonus(int delta)
	{
		if(delta < 0)
		{
			meleeBonus = Math.max(meleeBonus + delta, 0);
			meleeBonus = Math.max(meleeBonus, getCharacterClass().getBaseMeleeBonus() - Globals.MAXIMUM_CHANGE);
		}
		else
		{
			meleeBonus = Math.min(meleeBonus + delta, getCharacterClass().getBaseMeleeBonus() + Globals.MAXIMUM_CHANGE);
		}
	}

	public void alterRangeBonus(int delta)
	{
		if(delta < 0)
		{
			rangeBonus = Math.max(rangeBonus + delta, 0);
			rangeBonus = Math.max(rangeBonus, getCharacterClass().getBaseRangeBonus() - Globals.MAXIMUM_CHANGE);
		}
		else
		{
			rangeBonus = Math.min(rangeBonus + delta, getCharacterClass().getBaseRangeBonus() + Globals.MAXIMUM_CHANGE);
		}
	}

	public void alterArmorBonus(int delta)
	{
		if(delta < 0)
		{
			armorBonus = Math.max(armorBonus + delta, 0);
			armorBonus = Math.max(armorBonus, getCharacterClass().getBaseArmorBonus() - Globals.MAXIMUM_CHANGE);
		}
		else
		{
			armorBonus = Math.min(armorBonus + delta, getCharacterClass().getBaseArmorBonus() + Globals.MAXIMUM_CHANGE);
		}
	}

	public void alterTrapResist(int delta)
	{
		if(delta < 0)
		{
			trapResist = Math.max(trapResist + delta, 0);
		}
		else
		{
			trapResist = Math.min(trapResist + delta, 100);
		}
	}

	public void alterEquiptWeapon(int delta)
	{
		weapons[activeWeapon].alterDmg(delta);
		if(weapons[activeWeapon].getCurrDmg() <= 0)
		{
			this.setWeapon(Globals.WEAPON_DEFAULT, activeWeapon);
		}
	}
	public void alterAlternWeapon(int delta)
	{
		weapons[1 - activeWeapon].alterDmg(delta);
		if(weapons[1 - activeWeapon].getCurrDmg() <= 0)
		{
			this.setWeapon(Globals.WEAPON_DEFAULT, 1 - activeWeapon);
		}
	}
	public void alterBodyArmor(int delta)
	{
		armorSlotBody.alterProt(delta);
		if(armorSlotBody.getCurrProt() <= 0)
		{
			this.setArmorSlotBody(Globals.ARMOR_BODY_DEFAULT);
		}
	}
	public void alterSpecialArmor(int delta)
	{
		armorSlotSpecial.alterProt(delta);
		if(armorSlotSpecial.getCurrProt() <= 0)
		{
			this.setArmorSlotSpecial(Globals.ARMOR_SPEC_DEFAULT);
		}
	}

	/* Transactor interface methods */
	public BaseItem gainItem(BaseItem item)
	{
		if(item.getType() == Globals.ITEMTYPE_ARMOR && Globals.isPermitted(this.getCharacterClass(), item.getPermissions()))
		{
			return this.equipArmor((Armor)item);
		}
		else if(item.getType() == Globals.ITEMTYPE_WEAPON && Globals.isPermitted(this.getCharacterClass(), item.getPermissions()))
		{
			return this.equipWeapon((Weapon)item);
		}
		else if(item.getType() == Globals.ITEMTYPE_AMMO && Globals.isPermitted(this.getCharacterClass(), item.getPermissions()))
		{
			int ammoNum = Globals.INDEX_AMMOLIST.indexOf(((Ammo)item).getName());
			if((getAmmoQuantity(ammoNum) + ((Ammo)item).getQuantity()) > Globals.MAX_AMMO)
			{
				return item;
			}
			alterAmmoLevel(ammoNum, ((Ammo)item).getQuantity());
			return (BaseItem)null;
		}
		else if(item.getType() == Globals.ITEMTYPE_ITEM)
		{
			if(this.addItemToInventory(new Item((Item)item)))
			{
				return (BaseItem)null;
			}
			else
			{
				return item;
			}
		}
		else
		{
			return item;
		}
	}

	public boolean loseItem(BaseItem item)
	{
		if(item instanceof Armor)
		{
			if(((Armor)item).isBodyArmor() && this.getArmorSlotBody() == item)
			{
				this.setArmorSlotBody(Globals.ARMOR_BODY_DEFAULT);
				return true;
			}
			else if(!(((Armor)item).isBodyArmor()) && this.getArmorSlotSpec() == item)
			{
				this.setArmorSlotSpecial(Globals.ARMOR_SPEC_DEFAULT);
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(item instanceof Weapon)
		{
			if(((Weapon)item) == this.getAlternWeapon())
			{
				this.setWeapon(Globals.WEAPON_DEFAULT, 1 - activeWeapon);
				return true;
			}
			else if(((Weapon)item) == this.getEquiptWeapon())
			{
				this.setWeapon(Globals.WEAPON_DEFAULT, activeWeapon);
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			if(vcInventory.contains(item))
			{
				vcInventory.remove(item);
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public void adjustExp(int exp)
	{
		int newExp = this.getExperience() + exp;
		newExp = Math.max(newExp, 0);
		newExp = Math.min(newExp, Globals.MAX_PLAYER_EXP);
		this.setExperience(newExp);
		if(newExp >= getExpForNextLevel())
		{
			levelUp();
		}
	}

	public void levelUp()
	{
		// perform level increase
		level++;
		if(level > Globals.MAX_PLAYER_LEVEL) { level = Globals.MAX_PLAYER_LEVEL; }
		// perform hitpoint adds
		adjustHitpoints(Globals.getRandomValue(this.getCharacterClass().getHPGainedPerLevel()));
		// perform bonus adds
		if(this.getCharacterClass().getLuckIntv() > 0 && (level % this.getCharacterClass().getLuckIntv()) == 0)
		{
			alterLuck(Globals.getRandomValue(this.getCharacterClass().getLuckGain()));
		}
		if(this.getCharacterClass().getResistanceIntv() > 0 && (level % this.getCharacterClass().getResistanceIntv()) == 0)
		{
			alterResistance(Globals.getRandomValue(this.getCharacterClass().getResistanceGain()));
		}
		if(this.getCharacterClass().getMeleeBonusIntv() > 0 && (level % this.getCharacterClass().getMeleeBonusIntv()) == 0)
		{
			alterMeleeBonus(Globals.getRandomValue(this.getCharacterClass().getMeleeBonusGain()));
		}
		if(this.getCharacterClass().getRangeBonusIntv() > 0 && (level % this.getCharacterClass().getRangeBonusIntv()) == 0)
		{
			alterRangeBonus(Globals.getRandomValue(this.getCharacterClass().getRangeBonusGain()));
		}
		if(this.getCharacterClass().getArmorBonusIntv() > 0 && (level % this.getCharacterClass().getArmorBonusIntv()) == 0)
		{
			alterArmorBonus(Globals.getRandomValue(this.getCharacterClass().getArmorBonusGain()));
		}
		if(this.getCharacterClass().getTrapBonusIntv() > 0 && (level % this.getCharacterClass().getTrapBonusIntv()) == 0)
		{
			alterTrapResist(Globals.getRandomValue(this.getCharacterClass().getTrapBonusGain()));
		}
	}

	/* overridden methods */
	public int getAttack()  { return getCurrentAttack(); }
	public int getDefense() { return getCurrentDefense(); }

	public Weapon getActiveWeapon() { return weapons[activeWeapon]; }

	public void swapActiveWeapon()
	{
		activeWeapon = 1 - activeWeapon;
	}

	public Weapon equipWeapon(Weapon wpn)
	{
		if(Globals.isPermitted(this.getCharacterClass(), wpn.getPermissions()))
		{
			// first try to put the weapon in the next available empty slot
			for(int w = 0; w < 2; w++)
			{
				if(weapons[w] == Globals.WEAPON_DEFAULT)
				{
					setWeapon(wpn, w);
					return (Weapon)null;
				}
			}
			// if no empty slot is available, first try to replace "lesser" weapon of same class (melee/ranged/thrown)
			if(wpn.isRanged() == getAlternWeapon().isRanged() && wpn.isThrowable() == getAlternWeapon().isThrowable())
			{
				if(wpn.getCurrDmg() > getAlternWeapon().getCurrDmg() || wpn.getBaseDmg() > getAlternWeapon().getBaseDmg())
				{
					Weapon rtnWeapon = getAlternWeapon();
					setWeapon(wpn, getAlternWeaponIndex());
					return rtnWeapon;
				}
			}
			else if(wpn.isRanged() == getEquiptWeapon().isRanged() && wpn.isThrowable() == getEquiptWeapon().isThrowable())
			{
				if(wpn.getCurrDmg() > getEquiptWeapon().getCurrDmg() || wpn.getBaseDmg() > getEquiptWeapon().getBaseDmg())
				{
					Weapon rtnWeapon = getEquiptWeapon();
					setWeapon(wpn, getEquiptWeaponIndex());
					return rtnWeapon;
				}
			}
			// final choice is replace the alternate weapon with the new one
			Weapon rtnWeapon = getAlternWeapon();
			setWeapon(wpn, getAlternWeaponIndex());
			return rtnWeapon;
		}
		else
		{
			return wpn;
		}
	}

	public Armor equipArmor(Armor arm)
	{
		if(Globals.isPermitted(this.getCharacterClass(), arm.getPermissions()))
		{
			Armor armReturn = (Armor)null;
			if(arm.isBodyArmor())
			{
				if(getArmorSlotBody() != Globals.ARMOR_BODY_DEFAULT)
				{
					armReturn = getArmorSlotBody();
				}
				setArmorSlotBody(arm);
				return armReturn;
			}
			else
			{
				if(getArmorSlotSpec() != Globals.ARMOR_SPEC_DEFAULT)
				{
					armReturn = getArmorSlotSpec();
				}
				setArmorSlotSpecial(arm);
				return armReturn;
			}
		}
		else
		{
			return arm;
		}
	}

	@SuppressWarnings(value = "unchecked")
	public boolean addItemToInventory(Item item)
	{
		if(vcInventory.size() < Globals.MAX_INVENTORY)
		{
			vcInventory.add(new Item(item));
			Collections.sort(vcInventory);
			return true;
		}
		else
		{
			return false;
		}
	}

	public void removeItemFromInventory(int index)
	{
		if(vcInventory.size() > index)
		{
			vcInventory.removeElementAt(index);
		}
	}

	public boolean fireReadyWeapon()
	{
		if(getActiveWeapon().isRanged())
		{
			if(getActiveWeapon().requiresAmmo())
			{
				if(ammoSatchel[getActiveWeapon().getAmmoType()] != null && ammoSatchel[getActiveWeapon().getAmmoType()].getQuantity() > 0)
				{
					alterAmmoLevel(getActiveWeapon().getAmmoType(), -1);
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	public int getAmmoQuantity(int type)
	{
		if(ammoSatchel[type] != null)
		{
			return ammoSatchel[type].getQuantity();
		}
		else
		{
			return 0;
		}
	}

	public int getAmmoQuantityForWeapon(int index)
	{
		if(getWeapon(index).requiresAmmo())
		{
			if(getWeapon(index).getAmmoType() == Globals.AMMO_NOT_REQUIRED)
			{
				return Globals.AMMO_NOT_REQUIRED;
			}
			return getAmmoQuantity(getWeapon(index).getAmmoType());
		}
		return Globals.AMMO_NOT_REQUIRED;
	}

	public int getAmmoQuantityForEquiptWeapon()
	{
		return getAmmoQuantityForWeapon(activeWeapon);
	}

	public void alterAmmoLevel(int ammoNum, int amount)
	{
		if(ammoSatchel[ammoNum] != null)
		{
			int currAmmoLevel = ammoSatchel[ammoNum].getQuantity();
			currAmmoLevel += amount;
			ammoSatchel[ammoNum].setQuantity(Math.min(Math.max(currAmmoLevel, 0), Globals.MAX_AMMO));
		}
		else if(amount > 0)
		{
			ammoSatchel[ammoNum] = ((Ammo)(Globals.AMMOLIST.elementAt(ammoNum))).getInstance(Math.min(Math.max(amount, 0), Globals.MAX_AMMO));
		}
	}

	public void setAmmoLevel(int ammoNum, int amount)
	{
		if(ammoNum > ammoSatchel.length - 1)
		{
			System.out.println(this.getName() + " only has a satchel of " + ammoSatchel.length + " size");
		}
		else if(ammoSatchel[ammoNum] != null)
		{
			ammoSatchel[ammoNum].setQuantity(Math.min(Math.max(amount, 0), Globals.MAX_AMMO));
		}
		else if(amount > 0)
		{
			ammoSatchel[ammoNum] = ((Ammo)(Globals.AMMOLIST.elementAt(ammoNum))).getInstance(Math.min(Math.max(amount, 0), Globals.MAX_AMMO));
		}
	}

	public Image getCompositeImage(int mode, Component parent)
	{
		clearImage(bimgRender);
		Graphics g = bimgRender.getGraphics();
		if(!(this.isAlive()))
		{
			g.drawImage(Globals.CURR_DUNGEON.getImageDeadPlayer(), 0, 0, parent);
		}
		else if(mode == Globals.RENDER_MODE_NORMAL && this.getImageNormal() != null)
		{
			g.drawImage(this.getImageNormal(), 0, 0, parent);
			if(this.getArmorSlotBody().getImage() != null)
			{
				g.drawImage(this.getArmorSlotBody().getImage(), 0, 0, parent);
			}
			if(this.getArmorSlotSpec().getImage() != null)
			{
				g.drawImage(this.getArmorSlotSpec().getImage(), 0, 0, parent);
			}
			if(this.getEquiptWeapon().getImageNormal() != null)
			{
				g.drawImage(this.getEquiptWeapon().getImageNormal(), 0, 0, parent);
			}
		}
		else if(mode == Globals.RENDER_MODE_ATTACK && (this.getImageAttack() != null || this.getImageNormal() != null))
		{
			if(this.getImageAttack() != null)
			{
				g.drawImage(this.getImageAttack(), 0, 0, parent);
			}
			else
			{
				g.drawImage(this.getImageNormal(), 0, 0, parent);
			}
			if(this.getArmorSlotBody().getImage() != null)
			{
				g.drawImage(this.getArmorSlotBody().getImage(), 0, 0, parent);
			}
			if(this.getArmorSlotSpec().getImage() != null)
			{
				g.drawImage(this.getArmorSlotSpec().getImage(), 0, 0, parent);
			}
			if(this.getEquiptWeapon().getImageAttack() != null)
			{
				g.drawImage(this.getEquiptWeapon().getImageAttack(), 0, 0, parent);
			}
			else if(this.getEquiptWeapon().getImageNormal() != null)
			{
				g.drawImage(this.getEquiptWeapon().getImageNormal(), 0, 0, parent);
			}
		}
		return (Image)bimgRender;
	}

	protected void clearImage(BufferedImage bimg)
	{
		for(int y = 0; y < Globals.TILESCALE * 2; y++)
		{
			for(int x = 0; x < Globals.TILESCALE * 2; x++)
			{
				bimg.setRGB(x, y, transpixel);
			}
		}
	}

	public String getSaveString()
	{
		StringBuffer sbData = new StringBuffer();
		sbData.append(this.getCharacterClass().getClassKey());
		sbData.append(this.getCharacterDiff());
		sbData.append(ObjectParser.zeroPad(this.getLevel(), Globals.MAX_PLAYER_LEVEL));
		sbData.append(ObjectParser.zeroPad(this.getHitPoints(), Globals.MAX_PLAYER_HP));
		sbData.append(ObjectParser.zeroPad(this.getWounds(), Globals.MAX_PLAYER_HP));
		sbData.append(this.getSpeed());
		sbData.append(ObjectParser.zeroPad(this.getResistance(), 100));
		sbData.append("," + this.getName());
		sbData.append("," + this.getExperience());
		sbData.append("," + this.getLuck());
		sbData.append("," + this.getMeleeBonus());
		sbData.append("," + this.getRangeBonus());
		sbData.append("," + this.getArmorBonus());
		sbData.append("," + this.getTrapResist());
		Weapon wpnTemp = this.getWeapon(this.getEquiptWeaponIndex());
		if(wpnTemp.getName().equals(Globals.WEAPON_DEFAULT.getName()))
		{
			sbData.append("," + Globals.DEFAULT_ITEM_STRING);
		}
		else
		{
			sbData.append(",[");
			sbData.append(Globals.INDEX_ARSENAL.indexOf(wpnTemp.getName()) + "|");
			sbData.append((wpnTemp.getCurrDmg() - wpnTemp.getBaseDmg()));
			sbData.append("]");
		}
		wpnTemp = this.getWeapon(1 - this.getEquiptWeaponIndex());
		if(wpnTemp.getName().equals(Globals.WEAPON_DEFAULT.getName()))
		{
			sbData.append("," + Globals.DEFAULT_ITEM_STRING);
		}
		else
		{
			sbData.append(",[");
			sbData.append(Globals.INDEX_ARSENAL.indexOf(wpnTemp.getName()) + "|");
			sbData.append((wpnTemp.getCurrDmg() - wpnTemp.getBaseDmg()));
			sbData.append("]");
		}
		Armor armTemp = this.getArmorSlotBody();
		if(armTemp.getName().equals(Globals.ARMOR_BODY_DEFAULT.getName()))
		{
			sbData.append("," + Globals.DEFAULT_ITEM_STRING);
		}
		else
		{
			sbData.append(",[");
			sbData.append(Globals.INDEX_ARMOURY.indexOf(armTemp.getName()) + "|");
			sbData.append((armTemp.getCurrProt() - armTemp.getBaseProt()));
			sbData.append("]");
		}
		armTemp = this.getArmorSlotSpec();
		if(armTemp.getName().equals(Globals.ARMOR_SPEC_DEFAULT.getName()))
		{
			sbData.append("," + Globals.DEFAULT_ITEM_STRING);
		}
		else
		{
			sbData.append(",[");
			sbData.append(Globals.INDEX_ARMOURY.indexOf(armTemp.getName()) + "|");
			sbData.append((armTemp.getCurrProt() - armTemp.getBaseProt()));
			sbData.append("]");
		}
		sbData.append(",(");
		int acnt = 0;
		for(int am = 0; am < ammoSatchel.length; am++)
		{
			if(ammoSatchel[am] != null)
			{
				if(acnt > 0) { sbData.append(":"); }
				sbData.append(am + "|" + ammoSatchel[am].getQuantity());
				acnt++;
			}
		}
		sbData.append(")");
		sbData.append(",{");
		for(int inv = 0; inv < this.getInventorySize(); inv++)
		{
			if(inv > 0) { sbData.append(":"); }
			sbData.append(this.getInventoryItem(inv).getSaveString());
		}
		sbData.append("}");
		return sbData.toString();
	}

	public static Player createFromString(String strp)
	{
		StringTokenizer stParse = new StringTokenizer(strp, ",", false);
		String bigToken = stParse.nextToken();
		char classKey = bigToken.charAt(0);
		int cc = -1;
		for(int i = 0; i < Globals.CHARACTER_CLASSES.length; i++)
		{
			if(Globals.CHARACTER_CLASSES[i].getClassKey() == classKey)
			{
				cc = i;
				i = Globals.CHARACTER_CLASSES.length;
			}
		}
		if(cc == -1)
		{
			System.out.println("Unknown CharacterClass in saved characters file");
			System.exit(2);
		}
		int colorNum  = Integer.parseInt(bigToken.substring(1, 2));
		int lvl       = Integer.parseInt(bigToken.substring(2, 4));
		int hp        = Integer.parseInt(bigToken.substring(4, 7));
		int wnd       = Integer.parseInt(bigToken.substring(7, 10));
		int spd       = Integer.parseInt(bigToken.substring(10, 11));
		int res       = Integer.parseInt(bigToken.substring(11, 14));
		String name   = stParse.nextToken();
		int exp       = Integer.parseInt(stParse.nextToken());
		int luk       = Integer.parseInt(stParse.nextToken());
		int mbns      = Integer.parseInt(stParse.nextToken());
		int rbns      = Integer.parseInt(stParse.nextToken());
		int abns      = Integer.parseInt(stParse.nextToken());
		int trap      = Integer.parseInt(stParse.nextToken());
		String wpn1   = stParse.nextToken();
		String wpn2   = stParse.nextToken();
		String armb   = stParse.nextToken();
		String arms   = stParse.nextToken();
		String ammo   = stParse.nextToken();
		String inv    = stParse.nextToken();
		Player playerMake = new Player(cc, colorNum, name, lvl, exp, hp, wnd, luk, mbns, rbns, abns, trap, spd, res);
		if(!(wpn1.equals(Globals.DEFAULT_ITEM_STRING)))
		{
			playerMake.equipWeapon(Weapon.createFromString(wpn1.substring(wpn1.indexOf("[") + 1, wpn1.indexOf("]"))));
		}
		if(!(wpn2.equals(Globals.DEFAULT_ITEM_STRING)))
		{
			playerMake.setWeapon(Weapon.createFromString(wpn2.substring(wpn2.indexOf("[") + 1, wpn2.indexOf("]"))), 1);
		}
		if(!(armb.equals(Globals.DEFAULT_ITEM_STRING)))
		{
			playerMake.setArmorSlotBody(Armor.createFromString(armb.substring(armb.indexOf("[") + 1, armb.indexOf("]"))));
		}
		if(!(arms.equals(Globals.DEFAULT_ITEM_STRING)))
		{
			playerMake.setArmorSlotSpecial(Armor.createFromString(arms.substring(arms.indexOf("[") + 1, arms.indexOf("]"))));
		}
		if(!(ammo.equals(Globals.EMPTY_AMMOBAG_STRING)))
		{
			String ammolist = ammo.substring(1, ammo.length() - 1);
			stParse = new StringTokenizer(ammolist, ":", false);
			while(stParse.hasMoreTokens())
			{
				String ammostr = stParse.nextToken();
				int ammoID = Integer.parseInt(ammostr.substring(0, ammostr.indexOf("|")));
				int ammoQn = Integer.parseInt(ammostr.substring(ammostr.indexOf("|") + 1));
				playerMake.alterAmmoLevel(ammoID, ammoQn);
			}
		}
		if(!(inv.equals(Globals.EMPTY_INVENTORY_STRING)))
		{
			String invlist = inv.substring(1, inv.length() - 1);
			stParse = new StringTokenizer(invlist, ":", false);
			while(stParse.hasMoreTokens())
			{
				playerMake.addItemToInventory(Item.createFromString(stParse.nextToken()));
			}
		}
		return playerMake;
	}

}