package com.jumpmind.symmetric.console.ui.common;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Helper {
	
	public static Gson getMonitorEventGson() {
	  return new GsonBuilder().setDateFormat("MMM dd, yyyy, HH:mm:ss").create();
	}
	public static String formatDurationFull(long timeInMs) {
		long days = timeInMs / 86400000L;
		timeInMs %= 86400000L;
		long hours = timeInMs / 3600000L;
		timeInMs %= 3600000L;
		long minutes = timeInMs / 60000L;
		timeInMs %= 60000L;
		long seconds = timeInMs / 1000L;
		String duration = "";
		duration = formatDurationFullAppend(duration, days, "day");
		duration = formatDurationFullAppend(duration, hours, "hour");
		duration = formatDurationFullAppend(duration, minutes, "minute");
		if (days == 0L && hours == 0L) {
			duration = formatDurationFullAppend(duration, seconds, "second");
		}

		return duration;
	}

	private static String formatDurationFullAppend(String str, long amount, String name) {
		if (amount > 0L) {
			if (!str.equals("")) {
				str = str + ", ";
			}

			str = str + amount + " " + name;
			if (amount > 1L) {
				str = str + "s";
			}
		}

		return str;
	}
}