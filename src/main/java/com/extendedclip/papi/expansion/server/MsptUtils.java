package com.extendedclip.papi.expansion.server;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class MsptUtils implements Listener {
    // MSPT will be calculated by tracking the MSPT of 20 ticks and averaging those
    @SuppressWarnings("CanBeFinal")
    static long[] msptTick = new long[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static double getMspt() {
        long total = 0;
        for (final long mspt : msptTick) total += mspt;
        return (double) total / msptTick.length;
    }


    // Listeners for tick timings

    @EventHandler(priority = EventPriority.LOWEST)
    void onTickStart(ServerTickStartEvent event) {
        // Shift the array left
        System.arraycopy(msptTick, 1, msptTick, 0, msptTick.length - 1);

        // Set the last element to the current time in ms (the time the tick started)
        // Later calculate the actual tick duration by subtracting this time from the epoc of the time the tick ends
        msptTick[msptTick.length - 1] = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onTickEnd(ServerTickEndEvent e) {
        // Set the last value in the array to the time it took to complete the tick
        msptTick[msptTick.length - 1] = System.currentTimeMillis() - msptTick[msptTick.length - 1];
    }
}