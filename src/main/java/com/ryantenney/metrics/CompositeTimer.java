/**
 * Copyright (C) 2012 Ryan W Tenney (ryan@10e.us)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ryantenney.metrics;

import com.codahale.metrics.Timer;

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
