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

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static java.lang.System.out;
import static junit.framework.TestCase.assertTrue;

/**
 * Generic template to check if various implementations of {@link ISpinLock}s
 * are working fine.
 *
 * @author Nitin S (sin.nitins@gmail.com)
 */
public abstract class SpinLockVerificationTemplate {

    /**
     * Returns the concrete implementation of {@link ISpinLock}
     * which has to be verified.
     */
    abstract ISpinLock lockUnderTest();

    /**
     * This template logic creates N threads as requested by a concrete test and
     * those threads then attempt to acquire the designated lock. The logic eventually
     * verifies whether locking and unlocking happened in natural order.
     *
     * @param N number of threads to attempt lock acquisition
     */
    protected void doVerifyLockSanity(int N) {

        final ISpinLock lock = lockUnderTest();

        final CriticalSection criticalSection = new CriticalSection(lock);

        final Random random = new Random();

        CountDownLatch barrier = new CountDownLatch(N);

        IntStream.rangeClosed(1, N).forEach(x -> {
            new Thread(() ->
            {
                criticalSection.enter();
                doSthRandom(random);
                criticalSection.exit();
                barrierDown(barrier);
            },
            ("Thread"+x)).start();
        });

        awaitBarrier(barrier);

        out.printf("\n%d - Entry history : %s || Exit history: %s", System.currentTimeMillis(),
                criticalSection.entryHistory(), criticalSection.exitHistory());

        assertTrue(criticalSection.compareLockOrder());
    }

    private void doSthRandom(Random random) {
        long randomDelay = Math.abs(random.nextInt(100));
        out.printf("\n%d - %s -- busy working for %d ms", System.currentTimeMillis(), Thread.currentThread().getName(),
                randomDelay);
        waitWithoutRelinquishingLock(randomDelay);
    }

    private void waitWithoutRelinquishingLock(long randomDelay) {
        try {
            Thread.sleep(randomDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();// TODO: can be more distinctive here with thread id etc..
        }
    }

    private void barrierDown(CountDownLatch latch) {
        latch.countDown();
    }

    private void awaitBarrier(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();// TODO: can be more distinctive here with thread id etc..
        }
    }

}
