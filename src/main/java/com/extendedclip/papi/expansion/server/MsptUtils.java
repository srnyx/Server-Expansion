package com.extendedclip.papi.expansion.server;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class MsptUtils implements Listener {
    // MSPT will be calculated by tracking the MSPT of 20 ticks and averaging those
    @SuppressWarnings("CanBeFinal")
    static long[] msptTick = new long[20 * 60 * 10];

    /**
     * @param   ticks   The number of ticks to get the average MSPT for. Must be between 0 and 12,000, inclusive.
     * @return          The average MSPT of the last 20 ticks.
     */
    public static double getMspt(int ticks) {
        // Verify that the 'ticks' is between 0 and 12,000, if not, correct it
        final int ticksFix = Math.max(0, Math.min(ticks, 12000));
        if (ticksFix == 0L) return 0;

        boolean first = true;
        long total = 0;
        int ticksRecorded = 0;
        for (long mspt : msptTick) {
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
        return (double) total / ticksRecorded;
    }

    /**
     * @param   seconds The number of seconds to get the average MSPT from. Numbers less than 1 will return 0 and numbers grater than 600 will only return 600 seconds worth of data.
     *
     * @return          The average MSPT of the last {@code seconds} seconds.
     */
    public static double getMsptSeconds(int seconds) {
        return getMspt(seconds * 20);
    }


    // Listeners for tick timings

    @EventHandler(priority = EventPriority.LOWEST)
    void onTickStart(ServerTickStartEvent event) {
        // Shift the array right
        System.arraycopy(msptTick, 0, msptTick, 1, msptTick.length - 1);

        // Set the first element to the current time in ms (the time the tick started)
        // Later calculate the actual tick duration by subtracting this time from the epoc of the time the tick ends
        msptTick[0] = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onTickEnd(ServerTickEndEvent e) {
        // Set the first value in the array to the time it took to complete the tick
        msptTick[0] = System.currentTimeMillis() - msptTick[0];
    }
}