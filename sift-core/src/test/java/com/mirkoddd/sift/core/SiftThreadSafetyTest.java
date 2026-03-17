/*
 * Copyright 2026 Mirko Dimartino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mirkoddd.sift.core;

import static com.mirkoddd.sift.core.SiftPatterns.literal;

import com.mirkoddd.sift.core.dsl.Connector;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.Root;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiftThreadSafetyTest {

    @Test
    @DisplayName("Concurrent execution from a shared builder base should not corrupt state")
    void shouldBeThreadSafeWhenBranchingFromSharedBase() throws InterruptedException {
        // 1. Create a SINGLE base instance to be shared across threads
        Connector<Root> sharedBase = Sift.fromStart().digits();

        int threadCount = 1000; // is that even enough?
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Starting gate to hold all threads until they are all ready
        CountDownLatch startGate = new CountDownLatch(1);
        // Ending gate to wait for all threads to finish
        CountDownLatch endGate = new CountDownLatch(threadCount);

        // Thread-safe list to collect results
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final String threadSpecificData = String.valueOf(i);

            executor.submit(() -> {
                try {
                    startGate.await(); // All threads wait here for the green light

                    // CRASH TEST: 1000 threads simultaneously call methods on the shared object
                    // and invoke shake() which involves temporary state manipulation
                    String regex = sharedBase.followedBy(literal(threadSpecificData)).andNothingElse().shake();
                    results.add(regex);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // 3... 2... 1... GO! Unblock all threads simultaneously
        startGate.countDown();

        // Wait for all 1000 threads to finish (with a safety timeout of 10 seconds)
        boolean completed = endGate.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "Threads did not complete in the expected timeframe");
        executor.shutdown();

        // 2. VERIFY RESULTS
        assertEquals(threadCount, results.size(), "All threads must have completed the operation");

        // Verify each thread got exactly its expected result without mixing states
        for (int i = 0; i < threadCount; i++) {
            String expectedRegex = "^[0-9]" + i + "$";
            assertTrue(results.contains(expectedRegex),
                    "Missing expected result: " + expectedRegex + ". State corruption occurred!");
        }
    }

    @Test
    void shouldCoverDoubleCheckedLockingBranches() throws InterruptedException {
        // Use a sufficiently high number of threads to create genuine contention
        int threads = 50;
        int attempts = 100; // Repeat the race 100 times to guarantee statistical coverage for Jacoco

        for (int attempt = 0; attempt < attempts; attempt++) {
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch readyLatch = new CountDownLatch(threads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threads);

            // Create NEW, uninitialized instances on each iteration.
            // They must be effectively final to be used inside the lambda.
            SiftPattern<Fragment> builder = Sift.fromAnywhere().digits();
            SiftPattern<Fragment> memoized = SiftPatterns.literal("race");
            SiftPattern<Fragment> atomic = SiftPatterns.literal("race").preventBacktracking();

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        readyLatch.countDown(); // Thread signals it's ready
                        startLatch.await();     // Wait for the starting gunshot

                        // RACE ZONE: All threads call shake() and sieve() simultaneously!
                        builder.shake();

                        memoized.shake();

                        atomic.shake();

                        try (
                                SiftCompiledPattern compiledMemoized = memoized.sieve();
                                SiftCompiledPattern compiledAtomic = atomic.sieve()
                        ) {
                            assertNotNull(compiledMemoized.getRawRegex());
                            assertNotNull(compiledAtomic.getRawRegex());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            readyLatch.await(); // Wait until all 50 threads are warm and waiting
            startLatch.countDown(); // GO! Release the lock for everyone instantly
            doneLatch.await(); // Wait for all threads to finish the race

            executor.shutdown();
        }

        // A simple final assertion to ensure the logic didn't break
        assertEquals("[0-9]", Sift.fromAnywhere().digits().shake());
    }
}