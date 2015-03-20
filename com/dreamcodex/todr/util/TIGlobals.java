package com.dreamcodex.todr.util;

import java.awt.Color;

public class TIGlobals
{
	public static final Color TI_COLOR_TRANSPARENT = new Color(255, 255, 255, 0);
	public static final Color TI_COLOR_BLACK       = new Color(  0,   0,   0);//000000
	public static final Color TI_COLOR_GREY        = new Color(204, 204, 204);//CCCCCC
	public static final Color TI_COLOR_WHITE       = new Color(255, 255, 255);//FFFFFF
	public static final Color TI_COLOR_RED_LIT     = new Color(255, 121, 120);//FF7978
	public static final Color TI_COLOR_RED_MED     = new Color(252,  85,  84);//FC5554
	public static final Color TI_COLOR_RED_DRK     = new Color(212,  82,  77);//D4524D
	public static final Color TI_COLOR_YELLOW_LIT  = new Color(230, 206, 128);//E6CE80
	public static final Color TI_COLOR_YELLOW_DRK  = new Color(212, 193,  84);//D4C154
	public static final Color TI_COLOR_GREEN_LIT   = new Color( 94, 220, 120);//5EDC78
	public static final Color TI_COLOR_GREEN_MED   = new Color( 33, 200,  66);//21C842
	public static final Color TI_COLOR_GREEN_DRK   = new Color( 33, 176,  59);//21B03B
	public static final Color TI_COLOR_CYAN        = new Color( 66, 235, 245);//42EBF5
	public static final Color TI_COLOR_BLUE_LIT    = new Color(125, 118, 252);//7D76FC
	public static final Color TI_COLOR_BLUE_DARK   = new Color( 84,  85, 237);//5455ED
	public static final Color TI_COLOR_MAGENTA     = new Color(201,  91, 186);//C95BBA

	public static final Color[] TI_PALETTE =
	{
		TI_COLOR_TRANSPARENT,
		TI_COLOR_BLACK,
		TI_COLOR_GREEN_MED,
		TI_COLOR_GREEN_LIT,
		TI_COLOR_BLUE_DARK,
		TI_COLOR_BLUE_LIT,
		TI_COLOR_RED_DRK,
		TI_COLOR_CYAN,
		TI_COLOR_RED_MED,
		TI_COLOR_RED_LIT,
		TI_COLOR_YELLOW_DRK,
		TI_COLOR_YELLOW_LIT,
		TI_COLOR_GREEN_DRK,
		TI_COLOR_MAGENTA,
		TI_COLOR_GREY,
		TI_COLOR_WHITE
	};

	public static final String[] PLAYER_COLORS = { "blue", "green", "red", "purple" };
}
