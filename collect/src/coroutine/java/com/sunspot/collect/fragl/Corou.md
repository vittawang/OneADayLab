# Kotlin 协程（图解 · 我的理解版）

配合 `CoroutineFragment` 的分块 demo 看。每节结构：**结论先行 → 图/表 → 💡我的理解 → 分点细节**。

---

## 1. 协程是啥

**结论：协程是 Kotlin 的一个封装类，封装了"方法能暂停、再原地继续"的机制。他不是线程，很轻。**

```
线程模型（重）：                 协程模型（轻）：
 线程1 ── 任务1                  线程1 ── 协程1 / 协程2 / 协程3 … （成百上千）
 线程2 ── 任务2                          某个协程挂起 → 让出线程 → 线程去跑别的协程
 线程3 ── 任务3
 线程一多，系统吃不消              协程很轻，一条线程能扛一大堆
```

| | 线程 Thread | 协程 Coroutine |
|---|---|---|
| 谁调度 | 操作系统 | 语言层（Kotlin 封装类） |
| 开销 | 大，数量多扛不住 | 小，成千上万都行 |
| 暂停时 | 占着线程 | 让出线程 |

> 💡 **我的理解**：协程就是个封装类，无数个都没事，可以一堆协程跑在一条线程上。他暂停时不占线程，那条线程立马能去干别的活——靠的是系统的暂停机制，比线程的暂停/切换省 CPU 多了。

**一句话**：用写同步代码的方式，写异步逻辑。以前"子线程请求 + Handler 抛回主线程刷 UI"要切成好几段 callback，协程一条直线撸到底。

---

## 2. 挂起 vs 阻塞（协程的灵魂）

**结论：挂起让出线程，阻塞占死线程。同样是"等"，`delay` 不卡主线程，`Thread.sleep` 卡死主线程。**

```
阻塞 Thread.sleep(2s)：
 主线程 ●━━━━━━ 被占死 2s，啥也干不了 ━━━━━━●    → UI 冻结

挂起 delay(2s)：
 协程   ●┅┅┅ 挂起(打标记，2s后原地恢复) ┅┅┅●
 主线程 ●──── 让出去，继续刷UI/响应点击 ────●    → 不卡
```

| | 阻塞 `Thread.sleep` | 挂起 `delay` |
|---|---|---|
| 线程 | 被占死 | 让出去 |
| 主线程 | 冻结 → 会 ANR | 空闲 → 不 ANR |
| 能干别的吗 | 不能 | 能（跑别的协程/刷 UI） |

> 💡 **我的理解**：delay 相当于给协程这个封装类**打个标记**——"暂停这方法，X 秒后原地恢复"，主线程打完标记立马能去刷 UI。sleep 是真把线程占死了。协程里优先用 delay 别用 sleep。

**① delay 为什么不 ANR**

ANR 触发条件：主线程卡住、输入事件 5s 内派发不出去。

- `delay(10000)`：打标记 + 挂个"10s 后恢复"的定时任务，主线程立刻交还 → 空闲 → 不 ANR
- `Thread.sleep(10000)`：主线程被占死 10s
  - 这 10s 点了别的 UI → 5s 内 dispatch 不完 → **ANR**
  - 这 10s 啥也不点 → 不弹 ANR，但主线程占死、刷 UI 就崩

**② 线程的三种阻塞态**（离开 RUNNABLE、不占 CPU、run 不执行）

```
sleep ─────────▶ TIMED_WAITING   （定时等）
抢 synchronized 锁 ▶ BLOCKED       （等锁）
wait ──────────▶ WAITING          （无限等）
```

> 注意：`RUNNABLE` 是线程状态，`Runnable` 是任务，别混；IO 阻塞时 Java 还算 RUNNABLE。

---

## 3. 启动与等待：launch/join、async/await

**结论：`launch`/`async` = 立刻 fork 一条新执行线、点火即走；`join`/`await` = 汇合，等叉出去的线跑完并回来。launch↔join（不要结果），async↔await（要结果）。**

### 3.1 启动：launch / async

```
调用线 ●──── launch ────▶ 点火即走，立刻继续往下
              │ fork
              ▼
新协程线 ●━━━━━ 独立跑 ━━━━━▶
```

| | launch | async |
|---|---|---|
| 返回 | `Job`（遥控器） | `Deferred<T>`（取货凭证） |
| 要不要结果 | 不要 | 要，用 `await()` 取 |
| 对标线程 | `new Thread{}.start()` | `submit(Callable)` |

> 💡 **我的理解**：launch 点火即走——他立刻返回，函数继续往下执行。所以别指望在 launch 后面那行拿到协程里的结果（那时候还没跑呢）。要结果就用 async + await，要等就用 join。

**注意区分**：
- `launch`/`async` = 分叉出新线路
- 在一条协程里直接调 suspend 函数 = 不分叉，还是单线路顺序走（挂起只是暂停这条线、让出线程，不开新线）

### 3.2 等待：join（launch）/ await（async）

`join`/`await` 的本意都是**汇合**（fork-join 模型）：叉出去的线跑完、并回主线，"等"是汇合的副作用。

**launch + join（fork 一条，join 汇合）**

```
           launch=fork新线                    job.join() 挂起等汇合
父协程 ●━━━━┳━━━(点火即走,可先干别的)━━━━━━━━━━●━━━━●─▶ 已结束
           ┃                                      ▲
           ┗━▶ 子协程B ●━━━ 独立跑 ━━━━━━━━━━━━━━━┛（跑完汇合回父线）
```

**async + await（fork 两条，并行，两个 await 汇合）**

```
        async  async                      await A  await B
父协程 ●━┳━━━━━┳━━━(可先干别的)━━━━━━━━━━━━━●━━━━━━━●━●─▶ 更新UI
         ┃     ┃                           ▲       ▲
   fork A┃     ┃fork B                      ┃       ┃ 两个结果汇合
         ▼     ▼                           ┃       ┃
子协程A   ●━━(1s)━━●─────────────────────────┘       ┃
子协程B   ●━━━━(1.5s)━━━━━●───────────────────────────┘
        A、B 同时跑 → 总耗时≈1.5s（取最慢），不是 2.5s
```

```kotlin
// 串行：上一个完全跑完，下一个才开跑 → 1000+1500=2500ms
val user = fetchUser()
val orders = fetchOrders()

// 并行：两个 async 先都起，再分别 await → max=1500ms
val ud = async { fetchUser() }
val od = async { fetchOrders() }
val user = ud.await(); val orders = od.await()
```

> 💡 **我的理解**：
> - 串行时两个挂起函数在一个协程里，就是模拟代码执行顺序——**上一个完全执行完，下一个才开跑**，所以慢。
> - `async` 返回 `Deferred<T>`（延迟的 Data），`await()` 返回 `T`（就是 Data）。
> - `join` 就是**汇合**：launch 分叉出一条支线，join 等它跑完并回主线。launch 是发起、join 是等汇合。

**易错点**：`async{}.await()` 连着写 = 起一个立刻等 = 又变回串行了。必须先把两个 async 都起出来再 await。

**关键认知：线程是结果，不是原因**

- 决定串行/并行的是**代码结构**（顺序调用 vs 先都 async）
- 串行时任务不重叠 → 线程池复用同一条线程；并行时任务重叠 → 才多派一条线程
- 所以"都是 worker-1"是串行的结果，不是原因

**等一组子协程**（join/await 只能在协程里用）：

```kotlin
lifecycleScope.launch {
    val jobs = List(100) { launch(Dispatchers.Default) { ... } }
    jobs.joinAll()                    // 或 coroutineScope { } 自动等所有子协程
}
```

---

## 4. Dispatchers 调度器

**结论：Dispatcher 决定协程跑在哪条线程。范式是"协程起在 Main，耗时段 withContext(IO) 切出去"。**

```
Main 碰 UI  │  IO 等阻塞(网络/DB/文件)  │  Default 烧 CPU(计算)
```

| Dispatcher | 线程 | 干什么 | 对标 |
|---|---|---|---|
| `Main` | 主线程 | 刷 UI | Handler(mainLooper) |
| `IO` | 大线程池 | 网络/DB/文件 | IO 线程池 |
| `Default` | ≈CPU核数 | 纯计算 | 计算线程池 |

**withContext 切线程**——一条直线，实际跳了两次：

```
协程 ●─Main─┐                                   ┌─Main─● 更新UI
            │ withContext(IO){ 耗时活 }          │ 自动切回
            └──────── IO 线程 ─────────────────┘
```

```kotlin
lifecycleScope.launch {                          // Main
    val result = withContext(Dispatchers.IO) {   // 切 IO
        api.getUser()                            // 阻塞请求随便做
    }                                            // 自动切回 Main
    binding.tv.text = result.toString()          // Main：安全刷 UI
}
```

> 💡 **我的理解**：这是 Google 范式写法——保证调用方 Main 安全，方法跑完自动切回 Main，很稳。而且 `withContext` 块的**最后一行就是返回值**（`result = 那个值`），直接 return 回来，不用 callback，比 Handler 优雅。切回线程是自动的，不用手动 handler.post。

**误区**：整个 `launch(Dispatchers.IO)` 里碰 UI → 💥 `CalledFromWrongThreadException`（非主线程不能碰 UI）。

**子协程继承父的上下文**：

```kotlin
lifecycleScope.launch(Dispatchers.Default) {   // 父在 Default
    launch { ... }                             // 子没写 → 继承 Default
}
```

> 💡 **我的理解**：一句 log 打印出什么线程，取决于他写在哪个协程体里——写在父(Main)里就是 [main]，写在子 launch(Default) 里才是 [worker-x]。之前我以为子协程"没切线程"，其实是我把 log 写在父协程里了。

---

## 5. 取消：cancel

**结论：`cancel()` 和 `interrupt()` 一样是"发信号"不是"强制杀"，协作式——要有挂起点才停得下来。**

```
父协程
   │ cancel() 发信号，顺着父子树往下传
   ├─▶ 子A → 到挂起点(delay) 抛 CancellationException → 停
   └─▶ 子B → 到挂起点 抛 CancellationException → 停
```

> 💡 **我的理解**：cancel 就是立即把协程标记成"取消中"，执行到下一个挂起点(如 delay)时自动检查这个状态，然后取消。**没有下一个挂起点就取消不了**，这时候得自己手动判断 `isActive`（跟线程 `while(!isInterrupted())` 一个套路）。

**CancellationException 是特殊的**：取消时在挂起点抛了它，但框架会**静默处理**（不打印不崩），所以默认看不见。想看就 catch，但**看完必须 `throw e` 放行**：

```kotlin
try { ... }
catch (e: CancellationException) { log("被取消"); throw e }  // 必须重新抛，别吞
finally { log("清理善后") }
```

> 对标 Java：catch `InterruptedException` 后要补 `interrupt()`——同一个哲学，别把信号吞掉。

**真实请求 cancel 断得掉吗？**

```
接口正在阻塞请求中（没挂起点）── cancel() ──▶ 半路掐不断那个 socket
                                             但保证：结果作废、不乱刷UI
```

> 💡 **我的理解（当时的疑问 + 解答）**：接口正请求着退页面，cancel 也掐不断正在执行的请求（没挂起点）。但：
> 1. Retrofit 这种协程友好的网络库，会在协程 cancel 时把 OkHttp 的 `Call` 也 cancel 掉，能真断；
> 2. 就算断不掉，`withContext` 往主线程切的那一刻就是挂起点，至少保证**不乱刷 UI**，只是那条线程会把请求跑完、占点资源。

---

## 6. 超时：withTimeout

**结论：本质就是"到点自动 cancel"。抛的 `TimeoutCancellationException` 是 CancellationException 的子类，同样协作式。**

```
withTimeoutOrNull(2s) { 耗时3s }
 ●━━━━━━ 跑到 2s ━━━━━━╳ 到点自动cancel → 返回 null
```

| 版本 | 超时行为 |
|---|---|
| `withTimeout(ms)` | 抛异常 |
| `withTimeoutOrNull(ms)` | 返回 null（业务友好） |

```kotlin
val result = withTimeoutOrNull(2000) {
    delay(3000); "用户数据"      // 3s > 2s，超时，走不到
}
if (result == null) binding.tv.text = "网络超时，请重试"
```

> 💡 **我的理解**：`withTimeoutOrNull` 最多等 2s，返回值就是 block 块的返回值，超时就给 null，判空提示就行，不用自己 try/catch。

---

## 7. 线程安全：Mutex

**结论：协程跑在多线程调度器(Default/IO)上就是真并行，共享变量照样有竞态。协程里加锁用 `Mutex`（挂起等锁），别用 synchronized（阻塞线程）。**

```
100个子协程 ─┬─▶ result++  ┐
             ├─▶ result++  ├─ 非原子读改写，交错 → 丢更新 → 结果 < 100000
             └─▶ result++  ┘

加 mutex.withLock：同一时刻只放一个进来 → 稳定 100000
```

| | synchronized | Mutex |
|---|---|---|
| 等锁时 | 阻塞线程(占死) | 挂起协程(让出) |
| 协程里 | 不推荐 | 推荐 |

```kotlin
val mutex = Mutex()
coroutineScope {
    repeat(100) { launch(Dispatchers.Default) {
        repeat(1000) { mutex.withLock { safe++ } }
    } }
}
```

> 💡 **我的理解**：启 100 个子协程，一下 fork 出 100 条支线，每条都对 result +1 一千次，期望 100000。无锁时是非原子读写，值肯定达不到预期。关键：**子协程默认继承父协程的上下文(含 Dispatcher)，所以父设 Default，子也在 Default 多线程跑，才有竞态**。加锁就是"同时只能一个协程进来执行 → 原子读写"。

> 单个变量原子自增用 `AtomicInteger`（CAS 更轻）；一段复合逻辑要互斥才用 Mutex。

---

## 8. Channel

**结论：协程间的管道。一头 send 塞、一头 receive/for-in 取，挂起协调。对标 BlockingQueue，但不阻塞。**

```
生产者协程 ──send(x)──▶ [ Channel 管道 ] ──for(x in channel)──▶ 消费者协程
                        容量0 = 一手交钱一手交货(背压)
```

```kotlin
val channel = Channel<Int>()
launch { for (i in 1..5) { channel.send(i); delay(300) }; channel.close() } // 生产
launch { for (v in channel) { log("消费 $v"); delay(500) } }                 // 消费
```

> 💡 **我的理解**：默认容量 0，send 必须等有人 receive 才放行，所以生产消费节奏被拖到慢的那个（背压）。**忘了 close()，消费者的 for 会永远挂着等，卡死**。想解耦就给缓冲 `Channel<Int>(capacity=3)`。

**Android 实际用途**（日常直接手写不多，但这几处非它不可）：

| 场景 | 说明 |
|---|---|
| 一次性事件(导航/Toast) | `Channel` + `receiveAsFlow()`，消费一次不重放 |
| 任务串行化(蓝牙/socket/DB写) | 单消费者逐条处理 |
| 回调 API 转流 | `callbackFlow` 底层就是 Channel |

> 选型：**数据/状态用 Flow，事件/任务队列用 Channel**（Channel 每个元素只被一个消费者拿走一次）。

---

## 9. Flow（基础）

**结论：Flow 是冷数据流——`flow{}` 定义、`collect` 才启动、`emit` 发值、操作符(map/filter)中间转换。没人 collect 就不产数据。**

```
flow{ emit(1);emit(2)… } ──▶ [ map/filter ] ──▶ collect{ }
   数据源(冷,collect才跑)       中间加工          终点(触发运行)
```

```kotlin
flow { for (i in 1..5) { delay(300); emit(i) } }   // 定义，还没跑
    .map { it * 10 }
    .collect { log("收到 $it") }                    // 这一步才启动
```

| 类型 | 冷/热 | 用途 |
|---|---|---|
| `Flow` | 冷 | 一次性数据流：DB查询、网络 |
| `StateFlow` | 热 | UI 状态(持有当前值) |
| `SharedFlow` | 热 | 事件广播 |

> 💡 **我的理解**：冷 = 点播，你点(collect)了才播，每人从头看；热(StateFlow) = 直播，你啥时候进来看到的都是当前画面。

> 响应式/MVVM 完整用法（StateFlow、操作符体系、repeatOnLifecycle、完整数据链）留到单独一篇。

---

## 10. 异常与生命周期

**结论：默认一损俱损(子协程崩会连累父和兄弟)。要隔离用 `supervisorScope`。lifecycleScope/viewModelScope 自动回收，不用手动取消。**

```
默认(普通Job)：              隔离(SupervisorJob/supervisorScope)：
父                          父(Supervisor)
├─ 子A ✅                    ├─ 子A ✅（不受影响）
└─ 子B 💥 ─▶ 连累父和A       └─ 子B 💥（只自己挂）
```

**try/catch 的边界（坑）**

| 写法 | 能抓吗 |
|---|---|
| `try { val u = requestUser() }` 包挂起调用 | ✅ 能 |
| `try { launch { throw ... } }` 包一个 launch | ❌ 抓不到(子协程并发跑) |

- launch 兜底用 `CoroutineExceptionHandler`（对 async 无效）
- async 的异常在 `await()` 处抛，在那 try/catch

**生命周期 scope —— scope 回收的真相**

```
退出页面 ─▶ lifecycleScope 自动 cancel ─▶ 顺父子树把所有协程全停 ─▶ 不泄漏
```

> 💡 **我的理解**：用 lifecycleScope / viewModelScope 就**不用手写 onDestroyView 取消协程**，框架自动回收。只有自己 `CoroutineScope(SupervisorJob()+Main)` 建的 scope，才必须手动 `cancel()`，不然泄漏。

---

## 11. 协程 ↔ 线程 对照总表

**用线程做锚点记协程，一一对应：**

| 线程 Thread | 协程 | 差异 |
|---|---|---|
| `new Thread{}.start()` | `launch { }` | 协程轻，挂起不占线程 |
| `Callable`+`Future.get()` | `async{}`+`await()` | await 挂起不阻塞 |
| `Thread.sleep()` | `delay()` | delay 不占线程 |
| `Thread.join()` | `Job.join()` | 都是"汇合" |
| `Thread.interrupt()` | `Job.cancel()` | 都是协作式发信号 |
| catch InterruptedException 补 interrupt | catch CancellationException 补 `throw e` | 别吞信号 |
| 线程池 | `Dispatchers.IO/Default` | 调度器复用线程 |
| `Handler.post` 回主线程 | `withContext(Main)` | 切线程 |
| `synchronized`/`ReentrantLock` | `Mutex.withLock` | Mutex 挂起等锁 |
| `wait/notify`、`BlockingQueue` | `Channel` | send/receive 挂起 |
| `CountDownLatch` | `joinAll`/`awaitAll`/`coroutineScope` | 等一组 |
| 自己管 activeThreads 逐个 interrupt | `lifecycleScope` 自动取消整棵树 | 结构化并发 |

---

## 12. 一句话记忆清单

- **协程** = 能暂停再原地续的封装类，不是线程，很轻
- **挂起 ≠ 阻塞**：挂起让出线程(delay)，阻塞占死线程(sleep)——最重要一条
- **suspend** 只是"可能挂起"的标记，自己不切线程
- **launch/async** = 立刻 fork 新线，点火即走；要等用 join/await
- **串行/并行看代码结构**不看线程数；async 要"先都起再都 await"才并行
- **Dispatchers**：Main 碰 UI、IO 等阻塞、Default 烧 CPU；范式=起 Main、耗时段 withContext(IO)
- **cancel/withTimeout** 协作式：要有挂起点；纯循环用 isActive
- **CancellationException** catch 后 `throw e` 放行，别吞
- **Mutex** 护共享状态；数据用 Flow、事件/任务队列用 Channel
- **异常默认一损俱损**，隔离用 supervisorScope；launch 兜底 CoroutineExceptionHandler
- **lifecycleScope/viewModelScope 自动回收**，自建 scope 才手动 cancel

---

## 13. 深入：launch 的执行顺序（"点火即走"的细节）

**结论："点火即走" = 调用方不阻塞等协程完成；但第一个挂起点之前，协程体可能会内联同步跑完（lifecycleScope 的 Main.immediate + 无挂起点时）。B 和 C 谁先，看"第一个挂起点"和调度器。**

两个只差一行、输出却不同：

```
① 无挂起点                          ② 有 delay 挂起点
log("A")                            log("A")
launch { log("B") }                 launch { delay(1000); log("B") }
log("C")                            log("C")
输出：A → B → C                     输出：A → C →(1s)→ B
```

**为什么①是 A→B→C**

```
launch → (Main.immediate + 无挂起点) → 协程体一口气同步跑完(B) → 才回去打 C
```

- 协程体没挂起点
- lifecycleScope 用 `Dispatchers.Main.immediate`：已在主线程就不重新排队，直接内联执行
- → 协程体同步跑完 B，再打 C

**为什么②是 A→C→B**

```
launch → 内联跑到 delay → 挂起,交还控制权 → 打 C →(1s)→ 恢复打 B
```

- `delay` 是挂起点，一遇到就交还控制权 → 先打 C

三个变体：

```kotlin
launch { log("B") }                       // 无挂起点+immediate → 内联跑完 → A B C
launch { delay(1000); log("B") }          // 有挂起点 → 挂起交还 → A C (1s) B
launch(Dispatchers.Default) { log("B") }  // 换线程派发出去 → A C … B(worker线程)
```

> 💡 **我的理解**：我一开始以为 launch 后面的代码(C)一定先跑，其实不一定。"点火即走"真正保证的是**调用方不会卡着等协程干完**——一旦协程里有耗时(delay/网络)，调用方绝对立刻继续(C 先)。只有"没挂起点 + Main.immediate"这种恰好同步跑完的情况，才会 B 先于 C。
