package com.ryantenney.metrics;

import com.codahale.metrics.Timer;

/**
 *
 */
public class CompositeTimer
{
    private final Timer totalTimer;
    private final Timer successTimer;
    private final Timer failureTimer;

    public CompositeTimer(final Timer totalTimer, final Timer successTimer, final Timer failureTimer)
    {
        this.totalTimer = totalTimer;
        this.successTimer = successTimer;
        this.failureTimer = failureTimer;
    }

    public Timer getTotalTimer()
    {
        return totalTimer;
    }

    public Timer getSuccessTimer()
    {
        return successTimer;
    }

    public Timer getFailureTimer()
    {
        return failureTimer;
    }
}
