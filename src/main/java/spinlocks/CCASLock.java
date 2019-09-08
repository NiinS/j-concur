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

import static spinlocks.SpinLockShared.ALREADY_OWNED;
import static spinlocks.SpinLockShared.FAILED_TO_ACQUIRE;
import static spinlocks.SpinLockShared.getCurrentLockStateWithProbableCacheMiss;
import static spinlocks.SpinLockShared.getLockStateWithAcquisitionAttemptWhileCausingCCN;
import static spinlocks.SpinLockShared.setLockStateWhileCausingCCN;

import java.util.concurrent.atomic.AtomicBoolean;

public class CCASLock implements ISpinLock {
    private final AtomicBoolean lock = new AtomicBoolean(false);

    @Override
    public void lock() {
       while(true){
           while(getCurrentLockStateWithProbableCacheMiss(lock) == ALREADY_OWNED)
               continue; // means someone else is still the owner

           if(getLockStateWithAcquisitionAttemptWhileCausingCCN(lock, true) != FAILED_TO_ACQUIRE)
               return; //means this thread is owner now

           //else this thread need to retry from scratch
       }
    }

    @Override
    public void unlock() {
        setLockStateWhileCausingCCN(lock, false);
    }
}
