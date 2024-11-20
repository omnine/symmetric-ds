package com.jumpmind.symmetric.console.ui.common;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Helper {
	
	public static Gson getMonitorEventGson() {
	  return new GsonBuilder().setDateFormat("MMM dd, yyyy, HH:mm:ss").create();
	}

}