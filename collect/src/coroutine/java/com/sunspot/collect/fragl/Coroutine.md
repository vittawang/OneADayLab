# Kotlin 协程

配合 `CoroutineFragment` 的分块 demo 一起看。协程的很多概念都能在 `Thread.md`（线程复习）里找到锚点，本篇尽量用线程做对照。

## 1. 协程是什么

一句话心智模型：**协程是一段"可以暂停、之后再从原地继续"的代码，它不是线程。**

对比线程：

- 线程是操作系统调度的，创建、切换有开销，数量多了扛不住
- 协程是**语言层面**的封装，成千上万个协程可以跑在少数几个线程上。它"暂停"时不占用线程，那个线程能立刻去干别的活

最经典的 Android 痛点：网络请求不能在主线程做（会 ANR），过去要开线程 + Handler 切回主线程更新 UI，代码被切成好几段回调。协程能让你**用写同步代码的方式，写异步逻辑**。

```kotlin
lifecycleScope.launch {
    val user = fetchUser()             // 挂起，等网络返回（不卡主线程）
    binding.nameText.text = user.name  // 回来直接更新 UI，从上到下一条直线
}
```

## 2. 挂起 vs 阻塞（协程的灵魂）

这是协程和线程最本质的区别。

- **阻塞（block）**：线程停在这里，啥也干不了，干等。`Thread.sleep(2000)` 就是——这 2 秒线程被占死、废掉了
- **挂起（suspend）**：协程停在这里，但**它所在的线程被释放出去**，能去执行别的协程。等条件满足了，协程再找个线程从原地继续

用 Android 场景对比：

```kotlin
delay(2000)         // 挂起：主线程空闲，能响应点击/刷动画 → 不会 ANR
Thread.sleep(2000)  // 阻塞：主线程被占死 2 秒 → 界面冻结，有交互就 ANR
```

**关键点**：`delay` 挂起的是"协程"，不是"线程"。协程在那等的期间，主线程完全空闲。这就是为什么一个 App 能同时跑成百上千个协程还不卡——它们大部分时间都在挂起，并不真占线程。

### delay 为什么不会 ANR（深入）

ANR 的触发条件是**主线程被卡住、连续几秒处理不了消息**，最常见是"输入分发超时（约 5s）"——得真有输入事件没被处理。

- `delay(10000)`：记下恢复点 + 挂一个"10s 后恢复"的定时任务，然后立刻把主线程交还。这 10s 主线程空闲，能正常刷 UI、响应输入 → 不会 ANR
- `Thread.sleep(10000)`：真实阻塞主线程 10s。期间有点击/滑动 → 5s 内派发不出去 → ANR；期间不做任何交互 → 不弹 ANR，但主线程被占死、界面冻结（照样是 bug）

### 线程的阻塞状态（广义）

线程离开 `RUNNABLE`、不占 CPU、`run()` 代码暂停，包括三种：

| 状态 | 触发 | 说明 |
|------|------|------|
| `BLOCKED` | 等 `synchronized` 锁 | 严格意义的"阻塞"只指这个 |
| `WAITING` | `wait()` / `join()`（无参） | 无限期等唤醒 |
| `TIMED_WAITING` | `sleep(n)` / `wait(n)` | 带超时的等待 |

注意：`RUNNABLE` 是"线程状态"，`Runnable` 是"任务"，别混；且 **IO 阻塞时 Java 仍算 RUNNABLE**（BLOCKED 只指等锁）。

### suspend 函数

能"挂起"的函数用 `suspend` 修饰。它**只能在协程里、或另一个 suspend 函数里调用**。

```kotlin
suspend fun fetchUser(): User {
    delay(1000)
    return User("小明")
}
```

注意：`suspend` 只是"可能挂起"的标记，**它自己不切线程**。真实阻塞 IO 还得靠 `withContext(Dispatchers.IO)` 切走（见第 4 节）。

## 3. 启动协程：launch / async

`launch` / `async` 都是**立刻 fork 一条新的执行线路（协程线），调用方点火即走不等待**。

- `launch { }`：点火即走，**不要返回值**，返回 `Job`（遥控器）。对标 `new Thread{}.start()`
- `async { }`：要**返回值**，返回 `Deferred<T>`（取货凭证）。对标 `submit(Callable)`

**点火即走（fire-and-forget）**：启动协程后调用方立刻继续，不等它跑完。

```kotlin
log("A")
lifecycleScope.launch {
    delay(1000)
    log("B")        // 1 秒后
}
log("C")            // 立刻
// 输出顺序：A、C、(1s后)B
```

要"等"，得用挂起点 `join()` / `await()`。

**注意区分**：
- `launch`/`async` = 分叉出新线路
- 在一条协程里直接调 suspend 函数 = 不分叉，仍是单线路顺序执行（挂起只是暂停这条线、让出线程，不开新线）

## 4. Dispatchers 调度器

协程最终要落到真实线程上跑，Dispatcher 决定"跑在哪个线程"：

| Dispatcher | 跑在哪 | 用来干什么 | 对标线程 |
|------------|--------|-----------|---------|
| `Dispatchers.Main` | 主线程 | 更新 UI | Handler(mainLooper) |
| `Dispatchers.IO` | 大线程池(默认64+) | 网络/数据库/文件（阻塞 IO） | IO 线程池 |
| `Dispatchers.Default` | ≈CPU 核数的线程池 | 纯计算 | 计算线程池 |

`lifecycleScope.launch` 默认在 `Main`，所以能直接更新 UI。

### withContext 切线程（核心工具）

`withContext(X) { }` = 把这块代码切到 X 调度器跑，**跑完自动切回原来的线程**，还能返回值。

```kotlin
lifecycleScope.launch {                          // ① Main
    val result = withContext(Dispatchers.IO) {   // ③ 切到 IO
        api.getUser()                            //    IO：阻塞请求
    }                                            // ④ 自动切回 Main
    binding.nameText.text = result.toString()    // ⑤ Main：安全更新 UI
}
```

一条直线，实际在 Main→IO→Main 跳了两次，却不用写任何 `handler.post`。`withContext` 也是挂起，切走期间 Main 被释放不卡。

**误区**：整个 `launch(Dispatchers.IO)` 里直接碰 UI 会崩：

```kotlin
lifecycleScope.launch(Dispatchers.IO) {
    binding.tv.text = "x"   // 💥 CalledFromWrongThreadException：非主线程不能碰 UI
}
```

正确姿势：**协程起在 Main，只把耗时段 withContext(IO) 切出去，UI 更新留在外面。**

### 子协程继承父协程的上下文

子协程默认沿用父的 Dispatcher，除非显式覆盖：

```kotlin
lifecycleScope.launch(Dispatchers.Default) {   // 父在 Default
    launch { ... }                             // 子没写调度器 → 继承 Default
}
```

> 一句 log 打印出什么线程，取决于它写在哪个协程体里：写在父(Main)里就是 [main]，写在子 launch(Default) 里才是 [worker-x]。

## 5. 并行与等待：async/await、join

### fork-join 图（launch + join）

```
              launch = fork 一条新线                    job.join()
              │                                        挂起等 B 汇合
父协程  ●━━━━━━┳━━━━━━━(点火即走，可继续做别的)━━━━━━━━━━━●━━━━━●━▶ 已结束
              ┃                                              ▲
              ┃ fork                                         ┃ B 跑完，汇合回父线
              ┗━▶ 子协程B  ●━━━━━━━ 独立并发执行 ━━━━━━━━━━━━━┛
```

`join` 的本意是**汇合**（fork-join 模型）：叉出去的线跑完、并回主线，"等"是汇合的副作用。对标 `Thread.join()`，但 join 是挂起不阻塞。

### async/await 并行图

```
              async      async                     await A   await B
              fork A     fork B                     拿结果    拿结果
父协程  ●━━━━━━┳━━━━━━━━━━┳━━━━━━━(可继续)━━━━━━━━━━━━●━━━━━━━━●━━●━▶ 更新UI
              ┃          ┃                          ▲        ▲
        fork  ┃          ┃  fork                    ┃        ┃  两个结果汇合
              ▼          ▼                          ┃        ┃
子协程A        ●━━━━(耗时1s)━━━●───────────────────────┘        ┃
子协程B        ●━━━━━━━(耗时1.5s)━━━━━━━●───────────────────────┘

          A、B 同时开跑 → 总耗时 ≈ 1.5s（取最慢），不是 2.5s
```

### 串行 vs 并行

```kotlin
// 串行：一个 await 完再发下一个，总耗时 ≈ 1000+1500 = 2500ms
val user = fetchUser()        // 挂起等它完
val orders = fetchOrders()

// 并行：两个 async 先都启动，再分别 await，总耗时 ≈ max = 1500ms
val userDeferred = async { fetchUser() }      // 立刻开跑
val ordersDeferred = async { fetchOrders() }  // 立刻开跑（与上一个重叠）
val user = userDeferred.await()               // 现在才等
val orders = ordersDeferred.await()
```

**易错点**：`async{}.await()` 连着写 = 起一个立刻等 = 又变回串行了。必须**先把两个 async 都起出来，再去 await**。

### 关键认知：线程是结果，不是原因

- **决定串行/并行的是代码结构**：顺序 `a(); b()` 就是串行；先 async 都起再 await 就是并行
- **线程是结果**：串行时任务不重叠，线程池复用同一条线程；并行时任务重叠，线程池才多派一条
- delay 版能在 Main 单线程"并发"，也是这个道理——并发的根是"同时进行"，多线程只是其中一种实现

### join / async 只能在协程里用

`join()`/`await()` 是挂起函数，普通函数里调不了。要等一组子协程：

```kotlin
lifecycleScope.launch {
    val jobs = List(100) { launch(Dispatchers.Default) { ... } }
    jobs.joinAll()          // 或 coroutineScope { } 自动等所有子协程
    log("全部完成")
}
```

`coroutineScope { }` 特性：会等里面所有子协程完成才往下走（现成的 fork-join 封装）。

## 6. 取消：cancel

`job.cancel()` 和 `Thread.interrupt()` 一个套路——**都是"发信号"，不是"强制杀死"**，取消是**协作式**的。

```
父协程
   │  job.cancel()  ── 发出"取消"信号
   ↓  信号顺着父子树往下传
   ├── 子协程 A  →  在挂起点抛 CancellationException → 停
   └── 子协程 B  →  在挂起点抛 CancellationException → 停
```

工作原理：

1. `cancel()` 把协程标记为"取消中"（`isActive` 变 false）
2. `delay`、`withContext`、`await` 等挂起函数**都会检查取消状态**，一旦发现被取消就抛 `CancellationException`
3. 所以只要协程里有挂起点，取消就能及时生效

```kotlin
downloadJob = lifecycleScope.launch(Dispatchers.IO) {
    var progress = 0
    while (progress < 100) {
        progress += 10
        log("下载 $progress%")
        delay(300)          // 挂起点：会响应取消
    }
    log("下载完成")          // 中途被取消则不打印
}
// downloadJob?.cancel()
```

### CancellationException 是特殊的

取消时确实在挂起点抛了 `CancellationException`，但**框架会静默处理它**（不打印、不崩溃、不上报），所以你默认看不见。想看就 catch，但**看完必须 `throw e` 放它走**：

```kotlin
try {
    ...
} catch (e: CancellationException) {
    log("被取消了")
    throw e            // 必须重新抛出，否则取消"不干净"
} finally {
    log("清理资源")     // 善后放 finally
}
```

对标 Java：catch `InterruptedException` 后要 `Thread.currentThread().interrupt()` 补回标志——同一个哲学：**别把取消/中断信号吞掉**。也别用 `catch(Exception)` 一把抓（会连 CancellationException 一起吞）。

### 纯 CPU 循环取消不掉

没有挂起点的死循环，`cancel()` 停不了它（没人检查信号）。自己加检查（对标 `while(!isInterrupted())`）：

```kotlin
while (isActive && progress < 100) { progress++ }   // isActive：还没被取消才继续
// 或 ensureActive() —— 被取消就直接抛异常
```

### 真实请求：cancel 能保证什么

一个正在执行的阻塞请求（如 OkHttp `execute()` 卡在 socket 读），`cancel()` 半路掐不断它（那段没挂起点）。但 cancel 仍保证：

- **结果作废、不乱刷 UI**（这已解决"退页面后回调 crash / 刷脏数据"）
- 但那次网络开销和线程会跑到结束，没真正省下

想连底层请求都真断掉，靠**库配合**：

- Retrofit / Ktor（协程原生库）：取消协程自动 cancel 底层 HTTP call
- 老阻塞 API：用 `suspendCancellableCoroutine + invokeOnCancellation { call.cancel() }` 手动桥接

```kotlin
suspend fun fetchUser(): User = suspendCancellableCoroutine { cont ->
    val call = okHttpClient.newCall(request)
    cont.invokeOnCancellation { call.cancel() }   // 协程取消 → 断请求
    call.enqueue(/* onResponse resume / onFailure resumeWithException */)
}
```

## 7. 超时：withTimeout

本质就是"到点自动 cancel"，抛的 `TimeoutCancellationException` 是 `CancellationException` 的子类，同样协作式（block 里要有挂起点才掐得断）。

- `withTimeout(ms) { }`：超时**抛异常**
- `withTimeoutOrNull(ms) { }`：超时**返回 null**（业务友好）

```kotlin
val result = withTimeoutOrNull(2000) {   // 最多等 2 秒
    delay(3000)                          // 耗时 3 秒 → 超时
    "用户数据"                            // 走不到
}
if (result == null) {
    binding.tvUser.text = "网络超时，请重试"
}
```

## 8. 线程安全：Mutex

误区："协程不都在 Main 单线程吗，哪来的安全问题？" 一旦协程跑在 `Dispatchers.Default/IO` 这种**多线程**调度器上，就是真并行，共享可变变量照样有竞态（和卖票 bug 一样）。

`Mutex` 是协程版的锁：

| | synchronized | Mutex |
|---|---|---|
| 等锁时 | **阻塞线程**（占死） | **挂起协程**（让出线程） |
| 用法 | `synchronized(x){ }` | `mutex.withLock { }` |
| 协程里 | 不推荐 | 推荐 |

```kotlin
// 无锁：100 个协程各自 +1 共 1000 次，结果 < 100000（丢更新）
var counter = 0
coroutineScope {
    repeat(100) { launch(Dispatchers.Default) { repeat(1000) { counter++ } } }
}

// 加锁：稳定 100000
val mutex = Mutex()
var safe = 0
coroutineScope {
    repeat(100) { launch(Dispatchers.Default) { repeat(1000) { mutex.withLock { safe++ } } } }
}
```

选择逻辑同线程那边：
- 单变量原子自增 → `AtomicInteger`（CAS 更轻）
- 一段复合逻辑要互斥 → `Mutex`（协程）/ `synchronized`（线程）

## 9. Channel

协程之间的**管道通信**，对标阻塞队列 + wait/notify 的生产者-消费者，但 send/receive 是挂起不阻塞。

```
生产者协程 ──send(x)──▶ [ Channel 管道 ] ──for(x in channel)──▶ 消费者协程
```

```kotlin
val channel = Channel<Int>()   // 默认容量0：send 必须等有人 receive 才放行

launch(Dispatchers.Default) {          // 生产者
    for (i in 1..5) {
        channel.send(i)                // 挂起，直到消费者接走
        delay(300)
    }
    channel.close()                    // 生产完毕，关闭（关键！忘了会让消费者卡死）
}

launch(Dispatchers.Default) {          // 消费者
    for (value in channel) {           // 挂起等，直到 close 才跳出
        log("消费 $value")
        delay(500)                     // 消费比生产慢
    }
}
```

要点：
- 默认容量 0 = "一手交钱一手交货"，`send` 等 `receive` → **背压**（生产者不会淹没消费者）
- `Channel<Int>(capacity = 3)`：缓冲 3 个，生产者先囤不干等；`Channel.UNLIMITED` 无限缓冲
- **忘了 `close()` 消费者的 for 循环会永远挂起**

### Android 实际用途

日常直接手写不多，但这几处非它不可：

- **ViewModel → UI 的一次性事件**（导航/Toast/SnackBar）：`Channel` + `receiveAsFlow()`，元素消费一次不重放，替代 SingleLiveEvent
- **任务串行化 / actor**：蓝牙/串口/socket 写入、DB 批量写要顺序执行——所有请求 send 进 channel，单个消费者逐条处理
- **回调 API 转流**：`callbackFlow` 底层就是 Channel
- **快生产慢消费的背压**

> 选型：**"数据/状态"用 Flow，"事件/任务队列"用 Channel**。Channel 的杀手锏是"每个元素只被一个消费者拿走一次"。

## 10. Flow（基础）

> 响应式编程 / MVVM 的完整用法（StateFlow、SharedFlow、操作符体系、repeatOnLifecycle、完整数据链）留到单独一篇笔记详解，这里只记协程内的 Flow 基础。

`Flow` 是**冷数据流**：`flow{}` 定义、`collect` 才启动、`emit` 发值、操作符（`map`/`filter`）中间转换。冷 = 没人 collect 就不产数据，每次 collect 从头跑一遍。

```
flow{ emit(1); emit(2)... }  ──▶  [ map / filter ]  ──▶  collect{ }
   数据源(冷,collect才启动)         中间转换            终点(触发运行)
```

```kotlin
val numbers = flow {
    for (i in 1..5) {
        delay(300)
        emit(i)              // 往下游发一个值
    }
}
// 此时流还没跑（冷）
numbers
    .map { it * 10 }         // 操作符：逐个转换
    .collect { value ->      // 终点：这一步才真正启动流
        log("收到 $value")
    }
```

### 冷流 vs 热流

| 类型 | 冷/热 | 特点 | 用途 |
|---|---|---|---|
| `Flow` | 冷 | 有人 collect 才跑，每人从头 | 一次性数据流：DB 查询、网络请求 |
| `StateFlow` | 热 | 持有当前值，新订阅立刻拿最新 | UI 状态（MVVM） |
| `SharedFlow` | 热 | 广播给多订阅者，可配重放 | 事件广播 |

一句话：**"随时间变化持续产出的数据"是 Flow 的主场**，Android 里 Room/DataStore/搜索联想/UI 状态都靠它。

## 11. 异常与生命周期

### 异常传播（默认一损俱损）

```
父协程
 ├── 子A ✅ 正常
 └── 子B 💥 抛异常  ──▶ 上传给父 ──▶ 父取消 ──▶ 兄弟A也被取消
```

一个子协程抛异常（非 CancellationException）会取消父和所有兄弟。想**隔离**用 `supervisorScope` / `SupervisorJob`：一个崩不连累其他。

### try/catch 的边界（关键坑）

- **能抓**：包住挂起调用 `try { val u = requestUser() } catch (e) { }`
- **不能抓**：try 包住一个 `launch { }`（子协程并发跑，异常走"传播给父"，不顺 try 的调用栈）

launch 的兜底用 `CoroutineExceptionHandler`（装在 scope/根协程上，**对 async 无效**）：

```kotlin
val handler = CoroutineExceptionHandler { _, e -> log("兜底：${e.message}") }
lifecycleScope.launch(handler) { throw RuntimeException("崩") }
```

`async` 的异常在 `await()` 处抛，要在 await 处 try/catch。

### 生命周期 scope —— scope 回收的真相

- `lifecycleScope`：绑 UI 生命周期，**页面销毁时自动取消**里面所有协程
- `viewModelScope`：绑 ViewModel，**onCleared 时自动取消**

用官方 scope 就**不用手写 onDestroyView 取消**——框架顺着父子树一 cancel 全停，不泄漏。

**只有自建 `CoroutineScope(SupervisorJob() + Dispatchers.Main)` 时**，才必须在合适时机手动 `scope.cancel()`，否则协程泄漏。

## 12. 协程 ↔ 线程 对照总表

学协程时用线程做锚点，一一对应：

| 线程（Thread） | 协程 | 关键差异 |
|---|---|---|
| `new Thread{}.start()` | `launch { }` | 协程轻，挂起不占线程 |
| `Callable` + `Future.get()` | `async { }` + `await()` | await 挂起，不阻塞 |
| `Thread.sleep()` 阻塞线程 | `delay()` 挂起协程 | delay 不占线程（灵魂差异） |
| `Thread.join()` 阻塞等待 | `Job.join()` 挂起等待 | 都是"汇合" |
| `Thread.interrupt()` | `Job.cancel()` | 都是协作式发信号 |
| catch `InterruptedException` 补 interrupt | catch `CancellationException` 补 `throw e` | 别吞掉信号 |
| 线程池 `ThreadPoolExecutor` | `Dispatchers.IO/Default` | 调度器复用线程 |
| `Handler.post` 回主线程 | `withContext(Dispatchers.Main)` | 切线程 |
| `synchronized` / `ReentrantLock` | `Mutex.withLock` | Mutex 挂起等锁不阻塞 |
| `AtomicInteger`（CAS） | `AtomicInteger` 仍适用 | 单变量原子 |
| `wait/notify`、`BlockingQueue` | `Channel` | send/receive 挂起 |
| `CountDownLatch` 等一组 | `joinAll` / `coroutineScope` / `awaitAll` | 等一组子协程 |
| 自己管 `activeThreads` + 逐个 interrupt | `lifecycleScope` 自动取消整棵树 | 结构化并发 |

## 13. 一句话记忆清单

- **协程** = 能暂停再原地续的代码，不是线程，很轻
- **挂起 ≠ 阻塞**：挂起让出线程（delay），阻塞占死线程（sleep）——最重要一条
- **suspend** 只是"可能挂起"的标记，自己不切线程
- **launch/async** = 立刻 fork 新执行线，点火即走；要等用 join/await
- **串行/并行看代码结构**，不看线程数；async 要"先都起、再都 await"才并行
- **Dispatchers**：Main 碰 UI、IO 等阻塞、Default 烧 CPU；范式=协程起 Main，耗时段 `withContext(IO)` 切走
- **cancel/withTimeout** 协作式：要有挂起点才生效；纯循环用 `isActive`
- **CancellationException** catch 后要 `throw e` 放行，别吞
- **Mutex** 保护协程里的共享状态；数据用 Flow、事件/任务队列用 Channel
- **异常默认一损俱损**，隔离用 `supervisorScope`；launch 兜底用 `CoroutineExceptionHandler`
- **lifecycleScope/viewModelScope 自动回收**，自建 scope 才手动 cancel

## 14. 深入：launch 的执行顺序（"点火即走"的细节）

先看两个只差一行的例子，输出顺序却不同：

```kotlin
// 例子①：协程体没有挂起点
log("A")
lifecycleScope.launch {
    log("启动了一个子协程")
    log("B")
}
log("C")
// 输出：A → 启动了一个子协程 → B → C
```

```kotlin
// 例子②：协程体里有 delay 挂起点
log("A")
lifecycleScope.launch {
    delay(1000)
    log("B")
}
log("C")
// 输出：A → C →（1s后）B
```

### 为什么例子①是 A→B→C（B 先于 C）

两个原因叠加：

1. **协程体里没有挂起点**（就两个 log，没有 delay/withContext）
2. **`lifecycleScope` 用的是 `Dispatchers.Main.immediate`**——"immediate"意思是：如果当前已经在主线程，就**不重新排队，直接内联执行**

所以：`launch` 启动后，协程体一口气**同步跑完**（打"启动了一个子协程"、B），才把控制权交还，再打 C。

### 为什么例子②是 A→C→B（C 先于 B）

因为 `delay(1000)` 是**挂起点**。协程内联跑到 `delay` 时**挂起**，控制权立刻交还调用方 → 打 C；1 秒后协程恢复 → 打 B。

### "点火即走"的准确含义（修正直觉）

**"点火即走" = 调用方不会阻塞/停下来等协程执行完毕**——而**不是**"协程体一定在后面的代码之后才跑"。

B 和 C 谁先，取决于两件事：

1. **第一个挂起点在哪**：一遇挂起点就交还控制权（→ 后续代码先跑）；没有挂起点就一口气跑完（→ 协程体先跑完）
2. **调度器**：`Main.immediate`（lifecycleScope）在主线程上不重新排队、内联执行；`Dispatchers.Default` 会派到别的线程 → 调用方立刻继续

三个变体对照：

```kotlin
launch { log("B") }                       // ① 无挂起点 + immediate → 内联跑完 → A B C
launch { delay(1000); log("B") }          // ② 有挂起点 → 挂起交还 → A C (1s) B
launch(Dispatchers.Default) { log("B") }  // ③ 换线程 → 派发出去 → A C … B(在worker线程)
```

### 一句话

**"点火即走" = 调用方不阻塞等待协程完成；但在第一个挂起点之前，协程体可能会内联同步执行**（尤其 lifecycleScope 的 `Main.immediate` + 无挂起点时）。谁先谁后看"第一个挂起点"和调度器——关键保证是：**一旦协程有耗时（delay/网络），调用方绝不会卡着等它。**
