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

public class SpinLockShared {

    /**
     * Checks the current state of the given lock and modifies it to given new value. Since getAndSet()
     * is a loop effectively, the call only returns when the state has been successfully modified. During its
     * course, the executing thread (cpu core) will cause noise as every getAndSet() call is a broadcast on
     * the shared CPU bus.
     *
     * 'CCN' here means 'Cache Coherence Noise'
     *
     * @param lock the lock under consideration
     * @param newValue the new value which needs to be set on the given lock
     * @return the current value of the lock (i.e. the value before it was set to new value)
     */
    // CCN = cache coherence noise on a shared bus CPU architecture
    public static boolean checkAndTrySetWhileCausingCCN(AtomicBoolean lock, boolean newValue) {
        return lock.getAndSet(newValue);
    }

    /**
     * Atomically sets the lock's state to given new value while invalidating the view of other
     * threads (cpu cores) who are also seeing this lock. This invocation causes traffic on shared cpu
     * bus.
     *
     * 'CCN' here means 'Cache Coherence Noise'
     *
     * @param lock
     * @param newValue
     */
    // CCN = cache coherence noise on a shared bus CPU architecture
    public static void setWhileCausingCCN(AtomicBoolean lock, boolean newValue) {
        lock.set(newValue);
    }

    /**
     * Fetches the current value of the lock with a possible cache miss if the
     * calling thread does not have the latest value in its cache. Subsequent calls
     * by the same thread will not see a cache miss until the value itself is invalidated
     * by some other thread due to "write of a new value"
     * @param lock the lock whose current state is to be retrieved
     * @return the current state of the given lock. A 'true' value means
     * some thread is owning this lock.
     */
    public static boolean fetchWithPossiblySingleCacheMiss(AtomicBoolean lock) {
        return lock.get();
    }
}
