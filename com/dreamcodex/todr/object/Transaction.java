package com.dreamcodex.todr.object;

import com.dreamcodex.todr.object.BaseItem;
import com.dreamcodex.todr.object.Transactor;

public class Transaction
{
	protected Transactor giver;
	protected Transactor receiver;
	protected BaseItem   baseitem;
	protected BaseItem   rtnItem;
	protected boolean    success;

	public Transaction(Transactor giver, Transactor receiver, BaseItem baseitem)
	{
		this.giver    = giver;
		this.receiver = receiver;
		this.baseitem = baseitem;
		rtnItem = (BaseItem)null;
		success = false;
	}

	public BaseItem getReturnItem() { return rtnItem; }
	public boolean  isSuccess()     { return success; }

	public void performTransaction()
	{
		if(baseitem != null)
		{
			if(giver != null)
			{
				this.success = giver.loseItem(baseitem);
			}
			if(receiver != null)
			{
				rtnItem = receiver.gainItem(baseitem);
				if(rtnItem == baseitem)
				{
					this.success = false;
				}
			}
		}
	}
}