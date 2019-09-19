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

import java.util.concurrent.atomic.AtomicReference;

/**
 * Queue based spin locking approach proposed by Mellor, Crummey
 * and Scott (MCS).
 *
 * <br/><a href="http://web.mit.edu/6.173/www/currentsemester/readings/R06-scalable-synchronization-1991.pdf"><b>Link to paper</b></a>
 *
 * <p>
 * This kind of locking offers benefits similar to {@link Q_CLHSpinLock}. The difference here is
 * that a lock requester in MCS scheme adds itself as the last node in the queue, spins on its
 * "own" lock which is later released by the predecessor. So its the predecessor which is releasing
 * a successor's lock.
 * </p>
 *
 * @see Q_CLHSpinLock
 *
 * @author Nitin S (sin.nitins@gmail.com)
 */
public class Q_MCSSpinLock implements ISpinLock {

    private final AtomicReference<LockSlot> tail;
    private final ThreadLocal<LockSlot> self;

    public Q_MCSSpinLock() {
        tail = new AtomicReference<>(null);
        self = new ThreadLocal<LockSlot>(){
            @Override
            protected LockSlot initialValue() {
                return new LockSlot();
            }
        };
    }

    @Override
    public void lock() {
        LockSlot mySlot = self.get();

        // set self as the last node in the queue
        LockSlot predecessor = tail.getAndSet(mySlot);

        if(predecessor == null) // means I'm the only one here alone
            return;

        // Else someone else is already owning the tail
        // I'd let it know by becoming its successor

        // predecessor will set my lock to false
        // when predecessor is done with its work
        mySlot.locked = true;

        predecessor.next = mySlot; // I'm successor now

        while(mySlot.locked) // spin until predecessor releases me
            continue;
    }

    @Override
    public void unlock() {
        LockSlot mySLot = self.get();

        if(mySLot.next == null) // means I have no successor
        {
            if(tail.compareAndSet(mySLot, null)) {
                return; // means I was the only one here alone
            }

            // Guard
            // Being here means someone has added itself as tail
            // and is next in line to become my successor
            while(mySLot.next == null) // wait until successor reveals itself
                continue;
        }

        mySLot.next.locked = false; // let the successor know that I'm done
        mySLot.next = null; // recycle self for future requesters
    }

    private static class LockSlot {
        volatile boolean locked = false;
        volatile LockSlot next = null;
    }
}
