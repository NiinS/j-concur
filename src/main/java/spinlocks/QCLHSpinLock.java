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

import java.util.concurrent.atomic.AtomicReference;

/**
 * Queue based lock approach proposed by Craig, Landin and Hagerston (CLH).
 * ftp://ftp.cs.washington.edu/tr/1993/02/UW-CSE-93-02-02.pdf
 *
 * Benefit of queue based spin locking is that requesting threads don't spin
 * on a single shared lock instead they spin on the lock held by previous
 * requester hence forming a queue like structure. A requesting thread acquires
 * the tail of the queue (lock held by predecessor) and sets itself as tail.
 *
 * This queue based approach ensures lock fairness and unlike timeout based approaches
 * threads don't unnecessarily wait too little or too long.
 *
 * There is no upper bound on how many threads can try to acquire. They will always
 * request the lock in a queued fashion.
 *
 * @author Nitin S (sin.nitins@gmail.com)
 */
public class QCLHSpinLock implements ISpinLock {

    /**
     * The tail of queue representing the lock held by last requester.
     */
    private final AtomicReference<LockSlot> tail;

    /**
     * Reference to predecessor's lock from current requester's point of view.
     * Current requester spins on this lock.
     */
    private final ThreadLocal<LockSlot> predecessor;

    /**
     * The lock owned by current requester. Next requester will spin on this lock.
     */
    private final ThreadLocal<LockSlot> self;

    public QCLHSpinLock() {
        tail = new AtomicReference<LockSlot>(new LockSlot());

        predecessor = new ThreadLocal<LockSlot>(){
            @Override
            protected LockSlot initialValue() {
                return null;
            }
        };

        self = new ThreadLocal<LockSlot>(){
            @Override
            protected LockSlot initialValue() {
                return new LockSlot();
            }
        };
    }

    @Override
    public void lock() {
        LockSlot slot = self.get();
        slot.isLocked = true;

        // get the predecessor's lock (tail) and set self
        // as the tail of the queue
        LockSlot predecessorSlot = tail.getAndSet(slot);
        predecessor.set(predecessorSlot);

        // spin on the predecessor's lock until the predecessor
        // releases it.
        while (predecessorSlot.isLocked)
            continue; //spin until predecessor releases
    }

    @Override
    public void unlock() {
        LockSlot slot = self.get();
        slot.isLocked = false; // whoever is spinning on tail, can now proceed
        self.set(predecessor.get()); // recycle for future use
    }

    private static class LockSlot {
        volatile boolean isLocked;
    }
}
