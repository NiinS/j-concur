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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author Nitin S (sin.nitins@gmail.com)
 */

public class SpinLocksBenchmarks {

    private static final int N  = 8;

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("Vanilla")
    @GroupThreads(N)
    public void testVanilla(EffectiveLockImpl state) throws InterruptedException {
        state.vanillaLock.lock();
        Blackhole.consumeCPU(10);
        state.vanillaLock.unlock();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("ChecKCheck")
    @GroupThreads(N)
    public void testChechCheckLock(EffectiveLockImpl state) throws InterruptedException {
        state.checkCheckLock.lock();
        Blackhole.consumeCPU(10);
        state.checkCheckLock.unlock();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("SimpleBackoff")
    @GroupThreads(N)
    public void testSimpleBackoffLock(EffectiveLockImpl state) throws InterruptedException {
        state.simpleBackoffLock.lock();
        Blackhole.consumeCPU(10);
        state.simpleBackoffLock.unlock();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("AdaptiveBackoff")
    @GroupThreads(N)
    public void testAdaptiveBackoffLock(EffectiveLockImpl state) throws InterruptedException {
        state.adaptiveBackoffLock.lock();
        Blackhole.consumeCPU(10);
        state.adaptiveBackoffLock.unlock();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("CLH")
    @GroupThreads(N)
    public void testCLHQueueLock(EffectiveLockImpl state) throws InterruptedException {
        state.clhQLock.lock();
        Blackhole.consumeCPU(10);
        state.clhQLock.unlock();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("MCS")
    @GroupThreads(N)
    public void testMCSQueueLock(EffectiveLockImpl state) throws InterruptedException {
        state.mcsQLock.lock();
        Blackhole.consumeCPU(10);
        state.mcsQLock.unlock();
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println("cores = " + Runtime.getRuntime().availableProcessors());
        Options options = new OptionsBuilder()
                .include(SpinLocksBenchmarks.class.getSimpleName())
                .threads(N)
                .forks(1)
        .build();

        new Runner(options).run();
    }
}
