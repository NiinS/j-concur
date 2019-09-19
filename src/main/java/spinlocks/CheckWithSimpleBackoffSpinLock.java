/*
 * MIT License
 *
 * Copyright (c) 2019 NitinS (sin.nitins@gmail.com)
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static spinlocks.SpinLockShared.*;

/**
 * A spinning check followed by a back-off lock.
 *
 * This lock requester thread spins if the lock is not free and then backs-off
 * for a while in order to try again.
 *
 * @author Nitin S (sin.nitins@gmail.com)
 */
public class CheckWithSimpleBackoffSpinLock implements ISpinLock {

    /**
     * A true value of this lock means lock has been acquired.
     */
    private final AtomicBoolean lock = new AtomicBoolean();

    /**
     * Back off gap in millisecs.
     */
    private long backOffGapMs;

    public CheckWithSimpleBackoffSpinLock(long backOffGapMs) {
        this.backOffGapMs = backOffGapMs;
    }

    @Override
    public void lock() {
        while(true){
            while(getCurrentLockStateWithProbableCacheMiss(lock) == SpinLockShared.ALREADY_OWNED)
                continue; // locally spin on cached state from now on

            if(getLockStateWithAcquisitionAttemptWhileCausingCCN(lock, true))
                return; // means this thread is owner now
            else
                LockSupport.parkNanos(MILLISECONDS.toNanos(backOffGapMs)); // back off

            // retry from scratch..
        }

    }

    @Override
    public void unlock() {
        setLockStateWhileCausingCCN(lock, false); // release the lock
    }
}
