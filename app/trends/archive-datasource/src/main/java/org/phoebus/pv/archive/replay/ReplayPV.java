package org.phoebus.pv.archive.replay;

import org.phoebus.archive.reader.ValueIterator;
import org.phoebus.pv.PV;
import org.phoebus.pv.archive.ArchiveReaderService;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/** Base for replay PVs
 *
 *  @author Kunal Shroff, Kay Kasemir, based on similar code in org.csstudio.utility.pv and diirt
 */
@SuppressWarnings("nls")
public class ReplayPV extends PV
{
    ArchiveReaderService service = ArchiveReaderService.getService();

    /** Timer for replaying updates */
    private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, target ->
    {
        final Thread thread = new Thread(target, "ReplayPV");
        thread.setDaemon(true);
        return thread;
    });

    /** Task that was submitted for periodic updates */
    private ScheduledFuture<?> task;

    private ValueIterator i;

    /**
     * 
     * @param completeName
     * @param name
     * @param start
     * @param end
     */
    public ReplayPV(String completeName, final String name, Instant start, Instant end)
    {
        super(completeName);

        // ReplayPV PVs are read-only
        notifyListenersOfPermissions(true);

        try {
            i = service.getReader().getRawValues(name, start, end);

            if (i.hasNext()) {
                notifyListenersOfValue(i.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        start(.01);
    }

    /** Start periodic updates
     *  @param update_seconds Update period in seconds
     */
    protected void start(final double update_seconds)
    {
        // Limit rate to 100 Hz
        final long milli = Math.round(Math.max(update_seconds, 0.01) * 1000);
        task = executor.scheduleAtFixedRate(this::update, milli, milli, TimeUnit.MILLISECONDS);
    }

    /** Called by periodic timer */
    protected void update() {
        try {
            if (i.hasNext()) {
                notifyListenersOfValue(i.next());
            } else {
                close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    protected void close()
    {
        if (! task.cancel(false))
            logger.log(Level.WARNING, "Cannot cancel updates for " + getName());
        super.close();
    }
}
