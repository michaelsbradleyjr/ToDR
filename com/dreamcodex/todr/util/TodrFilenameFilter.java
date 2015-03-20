package com.dreamcodex.todr.util;

import java.io.FilenameFilter;
import java.io.File;

import com.dreamcodex.todr.object.Globals;

public class TodrFilenameFilter implements FilenameFilter
{
	private String filterExtn = Globals.DATAEXTN;

	public TodrFilenameFilter(String type)
	{
		filterExtn = type;
	}

	public boolean accept(File dir, String name)
	{
		return name.endsWith(filterExtn);
	}
}