package com.sunspot.collect.fragl;

import android.os.Handler;
import android.util.Log;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Java 经典线程 API 的场景化示例。
 *
 * <p>所有方法都由 Android 主线程发起，但阻塞操作只发生在名称明确的业务线程中。</p>
 */
public final class JavaThreadEx {

    private static final String TAG = "JavaThreadEx";

    private final Logger logger;
    private final Handler mainHandler;
    //它唯一的用处在 cancelAll()
    private final Set<Thread> activeThreads = ConcurrentHashMap.newKeySet();
    private final Set<ExecutorService> activeExecutors = ConcurrentHashMap.newKeySet();
    private final Set<Runnable> pendingMainActions = ConcurrentHashMap.newKeySet();

    public JavaThreadEx(Logger logger, Handler mainHandler) {
        this.logger = logger;
        this.mainHandler = mainHandler;
    }

    public interface Logger {
        void log(String message);
    }

    /**
     * 场景：开启一个专门的线程下载用户头像。
     */
    public void thread() {
        Thread downloadThread = new Thread("avatar-download") {
            @Override
            public void run() {
                log("开始下载用户头像");
                if (sleepSafely(300)) {
                    log("用户头像下载完成");
                }
            }
        };
        activeThreads.add(downloadThread);
        downloadThread.start();
        log("主线程调用 start() 后立即继续执行");
    }

    /**
     * 场景：同一个“下载图片”任务交给不同线程执行。
     */
    public void runnable() {
        Runnable downloadTask = new Runnable() {
            @Override
            public void run() {
                log("执行图片下载任务");
                if (sleepSafely(250)) {
                    log("图片下载任务完成");
                }
            }
        };

        Thread coverThread = new Thread(downloadTask, "cover-download");
        Thread thumbnailThread = new Thread(downloadTask, "thumbnail-download");
        activeThreads.add(coverThread);
        activeThreads.add(thumbnailThread);
        coverThread.start();
        thumbnailThread.start();
    }

    /**
     * 场景：压缩图片时，对比直接调用 run() 和调用 start()。
     * run() 在当前主线程执行；start() 才会启动新线程。
     */
    public void startVersusRun() {
        Runnable compressTask = new Runnable() {
            @Override
            public void run() {
                log("正在压缩图片");
            }
        };

        log("直接调用 run()：");
        new Thread(compressTask, "compress-run").run();

        log("调用 start()：");
        Thread compressThread = new Thread(compressTask, "compress-start");
        activeThreads.add(compressThread);
        compressThread.start();
    }

    /**
     * 场景：观察下载线程从创建到结束的状态。
     * 状态由主线程定时读取，不使用 sleep() 或 join() 阻塞主线程。
     */
    public void threadStates() {
        Thread downloadThread = new Thread(() -> {
            log("下载线程开始运行，内部状态=" + Thread.currentThread().getState());
            sleepSafely(400);
        }, "state-download");

        log("只创建还未启动：" + downloadThread.getState());
        activeThreads.add(downloadThread);
        downloadThread.start();

        postDelayedTracked(
                () -> log("模拟网络等待期间：" + downloadThread.getState()),
                50
        );
        postDelayedTracked(
                () -> log("下载执行结束：" + downloadThread.getState()),
                500
        );
    }

    /**
     * 场景：第一次下载失败，稍后在下载线程中自动重试。
     */
    public void sleepRetry() {
        Thread downloadRetryThread = new Thread(() -> {
            try {
                log("第 1 次下载失败");
                log("当前下载线程等待 800ms 后重试");
                Thread.sleep(800);
                log("第 2 次开始下载");
                Thread.sleep(250);
                log("第 2 次下载成功");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log("下载重试被取消");
            }
        }, "downloadRetryThread");

        activeThreads.add(downloadRetryThread);
        downloadRetryThread.start();
    }

    /**
     * 场景：文件必须下载完成后才能解压。
     * unZipThread 调用 join()，因此等待的是 unZipThread，不是主线程。
     */
    public void joinDownloadThenUnzip() {
        Thread downloadThread = new Thread(() -> {
            log("安装包开始下载");
            if (sleepSafely(600)) {
                log("安装包下载完成");
            }
        }, "downloadThread");

        Thread unZipThread = new Thread(() -> {
            try {
                log("解压依赖下载结果，调用 downloadThread.join()");
                downloadThread.join();
                log("join() 返回，开始解压安装包");
                Thread.sleep(200);
                log("安装包解压完成");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log("解压任务被取消");
            }
        }, "unZipThread");

        activeThreads.add(downloadThread);
        activeThreads.add(unZipThread);
        downloadThread.start();
        unZipThread.start();
    }

    /**
     * 场景：用户稍后点击“取消下载”。
     * 主线程只发送 interrupt()，不调用 join() 等待。
     */
    public void interruptDownload() {
        Thread downloadThread = new Thread(() -> {
            try {
                for (int progress = 10; progress <= 100; progress += 10) {
                    Thread.sleep(100);
                    log("下载进度：" + progress + "%");
                }
            } catch (InterruptedException exception) {
                log("收到取消请求，删除临时文件并结束下载");
                Thread.currentThread().interrupt();
            }
        }, "downloadThread");

        activeThreads.add(downloadThread);
        downloadThread.start();
        postDelayedTracked(() -> {
            log("用户点击取消下载，主线程调用 interrupt()");
            downloadThread.interrupt();
        }, 350);
    }

    /**
     * 场景：两个售票窗口同时卖同一批票。
     */
    public void unsafeTicketSale() {
        class TicketOffice {
            private int remainingTickets = 6;

            boolean sellOne() {
                if (remainingTickets <= 0) {
                    return false;
                }
                int ticketNumber = remainingTickets;
                if (!sleepSafely(30)) {
                    return false;
                }
                remainingTickets = ticketNumber - 1;
                log("售出 " + ticketNumber + " 号票，剩余 " + remainingTickets);
                return true;
            }
        }

        TicketOffice office = new TicketOffice();
        AtomicInteger finishedWindows = new AtomicInteger();
        Runnable sellTask = () -> {
            while (office.sellOne()) {
                // 继续售票
            }
            if (finishedWindows.incrementAndGet() == 2) {
                log("售票结束：观察是否出现重复票号");
            }
        };

        Thread windowAThread = new Thread(sellTask, "售票窗口-A");
        Thread windowBThread = new Thread(sellTask, "售票窗口-B");
        activeThreads.add(windowAThread);
        activeThreads.add(windowBThread);
        windowAThread.start();
        windowBThread.start();
    }

    /**
     * 场景：使用 synchronized 修复多个窗口重复售票。
     */
    public void synchronizedTicketSale() {
        class TicketOffice {
            private int remainingTickets = 6;

            synchronized boolean sellOne() {
                if (remainingTickets <= 0) {
                    return false;
                }
                int ticketNumber = remainingTickets;
                if (!sleepSafely(30)) {
                    return false;
                }
                remainingTickets = ticketNumber - 1;
                log("安全售出 " + ticketNumber + " 号票，剩余 " + remainingTickets);
                return true;
            }
        }

        TicketOffice office = new TicketOffice();
        AtomicInteger finishedWindows = new AtomicInteger();
        Runnable sellTask = () -> {
            while (office.sellOne()) {
                // 继续售票
            }
            if (finishedWindows.incrementAndGet() == 2) {
                log("售票结束：每个票号只会被卖出一次");
            }
        };

        Thread safeWindowAThread = new Thread(sellTask, "安全窗口-A");
        Thread safeWindowBThread = new Thread(sellTask, "安全窗口-B");
        activeThreads.add(safeWindowAThread);
        activeThreads.add(safeWindowBThread);
        safeWindowAThread.start();
        safeWindowBThread.start();
    }

    /**
     * 场景：主线程修改停止标记，后台同步线程及时看见。
     */
    public void volatileStopFlag() {
        class SyncTask implements Runnable {
            private volatile boolean running = true;

            @Override
            public void run() {
                int batch = 1;
                while (running && !Thread.currentThread().isInterrupted()) {
                    log("正在同步第 " + batch++ + " 批数据");
                    sleepSafely(100);
                }
                log("读取到停止信号，结束同步");
            }

            void stop() {
                running = false;
            }
        }

        SyncTask syncTask = new SyncTask();
        Thread syncThread = new Thread(syncTask, "data-sync");
        activeThreads.add(syncThread);
        syncThread.start();

        postDelayedTracked(() -> {
            log("主线程把 volatile running 设置为 false");
            syncTask.stop();
        }, 320);
    }

    /**
     * 场景：三个下载线程安全增加“已完成数量”。
     */
    public void atomicDownloadCount() {
        AtomicInteger completedCount = new AtomicInteger();

        for (int index = 1; index <= 3; index++) {
            int fileNumber = index;
            Thread downloadThread = new Thread(() -> {
                if (!sleepSafely(fileNumber * 100L)) {
                    return;
                }
                int count = completedCount.incrementAndGet();
                log("文件 " + fileNumber + " 下载完成，当前完成数=" + count);
                if (count == 3) {
                    log("全部下载完成，总数=" + count);
                }
            }, "file-" + fileNumber);
            activeThreads.add(downloadThread);
            downloadThread.start();
        }
    }

    /**
     * 场景：两个人同时从余额 100 元的账户取 80 元。
     */
    public void lockedBankWithdraw() {
        class BankAccount {
            private final ReentrantLock lock = new ReentrantLock();
            private int balance = 100;

            void withdraw(int amount) {
                lock.lock();
                try {
                    log("检查余额，当前余额=" + balance);
                    if (balance >= amount) {
                        if (!sleepSafely(100)) {
                            return;
                        }
                        balance -= amount;
                        log("取款成功，剩余余额=" + balance);
                    } else {
                        log("余额不足，取款失败");
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        BankAccount account = new BankAccount();
        Thread userAThread = new Thread(() -> account.withdraw(80), "用户-A");
        Thread userBThread = new Thread(() -> account.withdraw(80), "用户-B");
        activeThreads.add(userAThread);
        activeThreads.add(userBThread);
        userAThread.start();
        userBThread.start();
    }

    /**
     * 场景：外卖员等待，商家出餐后通知外卖员。
     */
    public void waitForMeal() {
        Object mealLock = new Object();
        boolean[] mealReady = {false};

        Thread courierThread = new Thread(() -> {
            synchronized (mealLock) {
                while (!mealReady[0]) {
                    try {
                        log("餐还没做好，外卖员调用 wait() 等待");
                        mealLock.wait();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        log("外卖员取消等待");
                        return;
                    }
                }
                log("外卖员收到通知，取餐离开");
            }
        }, "courierThread");

        Thread restaurantThread = new Thread(() -> {
            if (!sleepSafely(400)) {
                return;
            }
            synchronized (mealLock) {
                mealReady[0] = true;
                log("商家出餐，调用 notifyAll()");
                mealLock.notifyAll();
            }
        }, "restaurantThread");

        activeThreads.add(courierThread);
        activeThreads.add(restaurantThread);
        courierThread.start();
        restaurantThread.start();
    }

    /**
     * 场景：首页等待用户、广告、推荐三个接口完成。
     * 等待发生在 homeRenderThread，不阻塞主线程。
     */
    public void loadHomePage() {
        CountDownLatch allApiFinished = new CountDownLatch(3);

        startApiRequest("用户接口", 200, allApiFinished);
        startApiRequest("广告接口", 350, allApiFinished);
        startApiRequest("推荐接口", 500, allApiFinished);

        Thread homeRenderThread = new Thread(() -> {
            try {
                log("等待三个接口完成，调用 CountDownLatch.await()");
                allApiFinished.await();
                log("三个接口均完成，开始渲染首页");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log("首页等待被取消");
            }
        }, "homeRenderThread");
        activeThreads.add(homeRenderThread);
        homeRenderThread.start();
    }

    /**
     * 场景：后台计算订单总价，并通过 Future 取得结果。
     * Future.get() 发生在 orderPriceThread，不阻塞主线程。
     */
    public void calculateOrderPrice() {
        Thread orderPriceThread = new Thread(() -> {
            ExecutorService priceExecutor = Executors.newSingleThreadExecutor(
                    runnable -> new Thread(runnable, "priceCalculatorThread")
            );
            activeExecutors.add(priceExecutor);
            try {
                Callable<Integer> calculatePrice = () -> {
                    log("开始计算：商品 80 + 运费 10 - 优惠 20");
                    Thread.sleep(300);
                    return 80 + 10 - 20;
                };

                Future<Integer> priceFuture = priceExecutor.submit(calculatePrice);
                log("orderPriceThread 调用 Future.get() 等待价格");
                int finalPrice = priceFuture.get();
                log("订单最终价格=" + finalPrice);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log("订单价格计算被取消");
            } catch (Exception exception) {
                log("订单价格计算失败：" + exception.getMessage());
            } finally {
                priceExecutor.shutdownNow();
                activeExecutors.remove(priceExecutor);
            }
        }, "orderPriceThread");
        activeThreads.add(orderPriceThread);
        orderPriceThread.start();
    }

    /**
     * 场景：批量上传六张图片。
     * 提交任务和等待结果发生在 uploadResultThread，不阻塞主线程。
     */
    public void uploadImages() {
        Thread uploadResultThread = new Thread(() -> {
            CountDownLatch uploadsFinished = new CountDownLatch(6);
            AtomicInteger threadNumber = new AtomicInteger();
            ThreadPoolExecutor uploadPool = new ThreadPoolExecutor(
                    2,
                    3,
                    5,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(2),
                    runnable -> new Thread(
                            runnable,
                            "upload-" + threadNumber.incrementAndGet()
                    ),
                    (runnable, executor) -> {
                        log("线程和队列都满了，第六张图片由 uploadResultThread 上传");
                        runnable.run();
                    }
            );
            activeExecutors.add(uploadPool);

            try {
                log("线程池配置：核心 2、最大 3、等待队列 2");
                for (int index = 1; index <= 6; index++) {
                    int imageNumber = index;
                    uploadPool.execute(() -> {
                        try {
                            log("开始上传图片 " + imageNumber);
                            Thread.sleep(250);
                            log("图片 " + imageNumber + " 上传完成");
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                        } finally {
                            uploadsFinished.countDown();
                        }
                    });
                }
                uploadsFinished.await();
                log("六张图片全部上传完成");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log("批量上传被取消");
            } finally {
                uploadPool.shutdownNow();
                activeExecutors.remove(uploadPool);
            }
        }, "uploadResultThread");
        activeThreads.add(uploadResultThread);
        uploadResultThread.start();
    }

    /**
     * 场景：后台接口返回结果后，回到 Android 主线程更新页面。
     */
    public void updateUiWithHandler() {
        Thread apiThread = new Thread(() -> {
            log("后台线程请求用户信息");
            if (!sleepSafely(300)) {
                return;
            }
            log("接口返回，使用 Handler 切换到主线程");
            mainHandler.post(() -> log("当前已在主线程，可以更新 TextView"));
        }, "apiThread");
        activeThreads.add(apiThread);
        apiThread.start();
    }

    /**
     * 停止当前页面启动的业务线程、延迟操作和线程池。
     */
    public boolean cancelAll() {
        boolean hasRunningTask = !activeThreads.isEmpty()
                || !activeExecutors.isEmpty()
                || !pendingMainActions.isEmpty();

        for (Runnable action : pendingMainActions) {
            mainHandler.removeCallbacks(action);
        }
        pendingMainActions.clear();

        for (Thread thread : activeThreads) {
            thread.interrupt();
        }
        activeThreads.clear();

        for (ExecutorService executor : activeExecutors) {
            executor.shutdownNow();
        }
        activeExecutors.clear();
        return hasRunningTask;
    }

    private void startApiRequest(
            String apiName,
            long durationMillis,
            CountDownLatch latch
    ) {
        Thread apiThread = new Thread(() -> {
            try {
                log(apiName + "开始请求");
                Thread.sleep(durationMillis);
                log(apiName + "请求完成");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        }, apiName);
        activeThreads.add(apiThread);
        apiThread.start();
    }

    private void postDelayedTracked(Runnable action, long delayMillis) {
        Runnable[] wrapper = new Runnable[1];
        wrapper[0] = () -> {
            pendingMainActions.remove(wrapper[0]);
            action.run();
        };
        pendingMainActions.add(wrapper[0]);
        mainHandler.postDelayed(wrapper[0], delayMillis);
    }

    private boolean sleepSafely(long durationMillis) {
        try {
            Thread.sleep(durationMillis);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log("线程被中断");
            return false;
        }
    }

    private void log(String message) {
        String line = "[" + Thread.currentThread().getName() + "] " + message;
        Log.e(TAG, line);
        logger.log(line);
    }
}
