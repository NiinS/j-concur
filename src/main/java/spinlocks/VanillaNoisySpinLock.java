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

import java.util.concurrent.atomic.AtomicBoolean;

import static spinlocks.SpinLockShared.*;

/**
 * An extremely noisy spin lock.
 *
 * A simple test and set lock with all the baggage of spinning on a single
 * global lock state field. In its attempts to check the state of lock, the
 * requesting thread keeps sending "fetch latest value" requests over the bus
 * hence causing big noise on the shared cpu bus.
 *
 */
public class VanillaNoisySpinLock implements ISpinLock {

    /**
     * A true value of this lock means lock has been acquired.
     */
    private final AtomicBoolean lock = new AtomicBoolean();

    @Override
    public void lock() {
       while(getLockStateWithAcquisitionAttemptWhileCausingCCN(lock, true) == ALREADY_OWNED)
           continue; // keep checking while sending check requests on the shared cpu bus
    }

    @Override
    public void unlock() {
        setLockStateWhileCausingCCN(lock, false); //release the lock
    }

}
