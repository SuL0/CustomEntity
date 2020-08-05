package me.sul.customentity.util;

import org.bukkit.Bukkit;

public class DebugUtil {
    public static void printStackTrace() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            Bukkit.getServer().broadcastMessage(stElements[i].toString());
        }
    }
}
