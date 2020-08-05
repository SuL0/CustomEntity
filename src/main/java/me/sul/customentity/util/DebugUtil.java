package me.sul.customentity.util;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class DebugUtil {
    public static void printStackTrace() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            Bukkit.getLogger().log(Level.WARNING, "§c" + stElements[i].toString());
        }
    }
}
