package com.dreamcodex.todr.object;

import com.dreamcodex.todr.object.BaseItem;

public abstract interface Transactor
{
	public BaseItem gainItem(BaseItem item);
	public boolean  loseItem(BaseItem item);
}