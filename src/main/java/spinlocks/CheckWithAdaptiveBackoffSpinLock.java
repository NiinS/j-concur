/*
 * MIT License
 *
 * Copyright (c) 2019 NitinS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package spinlocks;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import static java.lang.String.format;
import static spinlocks.SpinLockShared.*;

/**
 * A lock performing a spinning check followed by an adaptive back-off logic.
 *
 * This lock requester thread spins if the lock is not free and then backs-off
 * for a while in order to try again.
 *
 * @author Nitin S (sin.nitins@gmail.com)
 *
 */
public class CheckWithAdaptiveBackoffSpinLock implements ISpinLock {

    /**
     * A true value of this lock means lock has been acquired.
     */
    private final AtomicBoolean lock = new AtomicBoolean();

    /**
     * Back off time range in millisecs.
     */
    private int minDelay;
    private int maxDelay;

    public CheckWithAdaptiveBackoffSpinLock(int minDelay, int maxDelay) {
        if(minDelay >= maxDelay)
            throw new IllegalArgumentException(format("Min delay '{}' ms must be smaller than max delay '{}' ms",
                    minDelay, maxDelay));

        this.minDelay = minDelay > 0 && minDelay <= 5? minDelay : 1;
        this.maxDelay = maxDelay > 0 && maxDelay <= 100 ? maxDelay : 100;
    }

    @Override
    public void lock() {
        while(true){
            BackOffLogic backOffLogic = new BackOffLogic(minDelay, maxDelay);
            while(getCurrentLockStateWithProbableCacheMiss(lock) == SpinLockShared.ALREADY_OWNED)
                continue; // locally spin on cached state from now on

            if(getLockStateWithAcquisitionAttemptWhileCausingCCN(lock, true))
                return; // means this thread is owner now
            else
                backOffLogic.backOff();

            // retry from scratch..
        }

    }

    @Override
    public void unlock() {
        setLockStateWhileCausingCCN(lock, false); // release the lock
    }

    /**
     * Adaptive backoff logic which upon every next invocation
     * increases the time delay.
     */
    private static class BackOffLogic{
        private final Random random;
        private final int minDelay;
        private final int maxDelay;
        private int upperrBound;

        public BackOffLogic(int minDelay, int maxDelay) {
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
            this.upperrBound = minDelay;
            random = new Random();

        }

        public void backOff() {
           long delay = random.nextInt(upperrBound);
           upperrBound = Math.max(maxDelay, 2*upperrBound);
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(delay));
        }
    }
}
