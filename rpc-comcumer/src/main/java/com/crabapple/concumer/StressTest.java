package com.crabapple.concumer;

import com.crabapple.core.client.proxy.ClientProxy;
import com.crabapple.pojo.User;
import com.crabapple.service.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * RPC 框架压测工具
 * 
 * 压测前请先把 RateLimitProvider 中的限流调大或关闭：
 *   RateLimitProvider.java L14:
 *   new TokenBucketRateLimitImpl(1, 100000)  // rate=1ms 即 ~1000 QPS
 * 
 * 用法：
 *   1. 先启动 ProviderTest（服务端）
 *   2. 再运行本类（客户端）
 */
public class StressTest {

    // ═══════════════ 可调整参数 ═══════════════
    private static final int WARMUP_SECONDS = 3;       // 预热时间（秒）—— 给 JIT 编译、连接池预热
    private static final int TEST_SECONDS = 10;         // 正式压测时间（秒）
    private static final int[] THREAD_COUNTS = {1, 4, 8, 16, 32}; // 逐级增加的并发数
    // ═══════════════════════════════════════════

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     RPC 框架压力测试工具 v1.0        ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        ClientProxy clientProxy = new ClientProxy(0);
        UserService userService = clientProxy.getProxy(UserService.class);

        // 先做一次调用，触发所有懒加载（ZK连接、熔断器初始化等）
        System.out.println(">>> 初始化中：预热 ZK 连接、熔断器...");
        userService.getUserById(0);
        System.out.println(">>> 初始化完成\n");

        // 不同并发级别分别测试
        for (int threads : THREAD_COUNTS) {
            runScenario(userService, threads);
            Thread.sleep(2000); // 两级之间休息，让系统恢复
        }

        System.out.println("压测结束。");
    }

    /**
     * 单个并发场景的完整测试
     */
    private static void runScenario(UserService userService, int threadCount)
            throws InterruptedException {

        System.out.println("═══════════════════════════════════════");
        System.out.println("  并发数: " + threadCount + " 线程");
        System.out.println("═══════════════════════════════════════");

        // ═══════ 阶段0: 预热 ═══════
        System.out.println("  [预热] " + WARMUP_SECONDS + "秒 ...");
        runPhase(userService, threadCount, WARMUP_SECONDS, false);

        // ═══════ 阶段1: 正式测试 ═══════
        System.out.println("  [测试] " + TEST_SECONDS + "秒 ...");
        Metrics metrics = runPhase(userService, threadCount, TEST_SECONDS, true);

        // ═══════ 输出报告 ═══════
        printReport(threadCount, metrics);
        System.out.println();
    }

    /**
     * 执行一个测试阶段
     * @param record 是否记录延迟数据（预热阶段不记录）
     */
    private static Metrics runPhase(UserService userService, int threads,
                                     int durationSeconds, boolean record)
            throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);  // 发令枪
        CountDownLatch stopLatch = new CountDownLatch(threads);

        LongAdder totalRequests = new LongAdder();
        LongAdder totalErrors = new LongAdder();
        List<Long> latencies = record ? Collections.synchronizedList(new ArrayList<>()) : null;

        AtomicLong startTime = new AtomicLong();

        // 启动所有工作线程
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等发令枪
                    long deadline = startTime.get() + durationSeconds * 1000L;

                    while (System.currentTimeMillis() < deadline) {
                        long t1 = System.nanoTime();
                        try {
                            User user = userService.getUserById(1);
                            if (user == null) {
                                totalErrors.increment();
                            }
                        } catch (Exception e) {
                            totalErrors.increment();
                        }
                        long t2 = System.nanoTime();
                        totalRequests.increment();
                        if (record) {
                            latencies.add((t2 - t1) / 1000); // 纳秒 → 微秒
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stopLatch.countDown();
                }
            });
        }

        // 发令：所有线程同时开始
        startTime.set(System.currentTimeMillis());
        startLatch.countDown();

        // 等待所有线程完成
        stopLatch.await();
        executor.shutdown();

        Metrics m = new Metrics();
        m.totalRequests = totalRequests.sum();
        m.totalErrors = totalErrors.sum();
        m.durationSeconds = durationSeconds;
        m.threadCount = threads;

        if (record && latencies != null && !latencies.isEmpty()) {
            // 排序后计算分位数
            List<Long> sorted = new ArrayList<>(latencies);
            Collections.sort(sorted);
            m.latencies = sorted;
            m.min = sorted.get(0);
            m.max = sorted.get(sorted.size() - 1);
            m.avg = (long) sorted.stream().mapToLong(Long::longValue).average().orElse(0);
            m.p50 = percentile(sorted, 50);
            m.p90 = percentile(sorted, 90);
            m.p99 = percentile(sorted, 99);
            m.p999 = percentile(sorted, 99.9);
        }

        return m;
    }

    /** 计算分位数 */
    private static long percentile(List<Long> sorted, double p) {
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        idx = Math.max(0, Math.min(idx, sorted.size() - 1));
        return sorted.get(idx);
    }

    /** 打印测试报告 */
    private static void printReport(int threads, Metrics m) {
        double qps = m.totalRequests / (double) m.durationSeconds;
        double errorRate = m.totalErrors * 100.0 / Math.max(1, m.totalRequests);

        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.printf ("  │ 总请求数: %-26d │\n", m.totalRequests);
        System.out.printf ("  │ QPS:      %-26.0f │\n", qps);
        System.out.printf ("  │ 错误数:   %-26d │\n", m.totalErrors);
        System.out.printf ("  │ 错误率:   %-25.2f%% │\n", errorRate);
        System.out.println("  ├─────────────────────────────────────┤");
        if (m.latencies != null) {
            System.out.printf("  │ 平均延迟: %-23d μs │\n", m.avg);
            System.out.printf("  │ 最小延迟: %-23d μs │\n", m.min);
            System.out.printf("  │ P50:      %-23d μs │\n", m.p50);
            System.out.printf("  │ P90:      %-23d μs │\n", m.p90);
            System.out.printf("  │ P99:      %-23d μs │\n", m.p99);
            System.out.printf("  │ P999:     %-23d μs │\n", m.p999);
            System.out.printf("  │ 最大延迟: %-23d μs │\n", m.max);
        }
        System.out.println("  └─────────────────────────────────────┘");
    }
    
    /** 指标数据载体 */
    static class Metrics {
        long totalRequests;
        long totalErrors;
        int durationSeconds;
        int threadCount;
        long min, max, avg;
        long p50, p90, p99, p999;
        List<Long> latencies;
    }
}
