package com.extendedclip.papi.expansion.server;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;


public class MsptUtils implements Listener {
    // MSPT will be calculated by tracking the MSPT of 20 ticks and averaging those
    private static final long[] msptTick = new long[20 * 60 * 10];

    /**
     * @param   ticks   The number of ticks to get the average MSPT for. Must be between 0 and 12,000, inclusive.
     * @return          The average MSPT of the last 20 ticks.
     */
    public static double getMspt(int ticks) {
        // Verify that the 'ticks' is between 0 and 12,000, if not, correct it
        final int ticksFix = Math.max(0, Math.min(ticks, 12000));
        if (ticksFix == 0) return 0;

        boolean first = true;
        long total = 0;
        int ticksRecorded = 0;
        for (final long mspt : msptTick) {
            if (first) {
                first = false;
                continue;
            }

            // Skip ticks that haven't been recorded yet
            if (mspt == 0) continue;

            total += mspt;
            ticksRecorded++;
            if (ticksRecorded >= ticksFix) break;
        }

        if (ticksRecorded == 0) return 0;
        return (double) total / ticksRecorded / 1000000;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTickStart(@NotNull ServerTickStartEvent event) {
        if (msptTick[0] > 100000000000L) msptTick[0] = 0;

        // Shift the array right
        System.arraycopy(msptTick, 0, msptTick, 1, msptTick.length - 1);

        // Set the first element to the current time in ns (the time the tick started)
        // Later calculate the actual tick duration by subtracting this time from the end timing of the tick
        msptTick[0] = System.nanoTime();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickEnd(@NotNull ServerTickEndEvent e) {
        // Set the first value in the array to the time it took to complete the tick
        msptTick[0] = System.nanoTime() - msptTick[0];
    }
}