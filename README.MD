## ChatRoom

🚀🚀🚀基于NIO、单Rector多线程模型实现的聊天室，它可以帮助你：

- 快速回顾NIO编程模型；
- 快速回顾单Rector多线程模型；
- 快速掌握一个聊天室的基本功能。

🚀🚀🚀项目架构：

 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/structure.png)

🔥提供优化方案架构（二）🔥

- 主从Reactor多线程模型
- 尽量减少接受连接线程的压力，使其更快的响应更多连接，单独使用1个或多个SubReactor线程处理转发请求；
  - 如果存在多个SubReactor线程，accept获得的客户端SocketChannel可以存放在不同的BlockingQueue中，供多个SubReactor线程消费，以加快dispatch速度；
- 这种方案被证实存在设计问题😟，请参考架构方案三。

 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/update-structure.png)

🔥架构方案（三）🔥

- 将Worker需要执行的任务抽象为Task，由多个Dispatcher统一存放进入TaskQueue，Worker只需要关注于异步消费Task；
  - <u>Worker职能单一化，注册与监听功能下方到Dispatcher</u>；
  - 引入TaskQueue，异步消费Task，避免之前方案中出现任务耗时，阻塞整个注册在Worker线程Selector上所有客户端事件的情况💣
- 工作任务线程池数量，建议采取可用处理器数*2，合适的线程数能够很好应对部分耗时的处理操作。
  - ```Runtime.getRuntime().availableProcessors()*2```

 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/update-structure-2.png)

🔥优化思路🔥

- 将所有客户端事件全部放到一个TaskQueue中可能不是一个最佳的选择；
  - <u>我们可以为每个客户端设置单独的TaskQueue</u>，或者，<u>每个WorkerThread设置单独的TaskQueue（Netty）</u>；
  - 将客户端所有相关的信息，封装为Context（ctx），然后统一管理所有上下文；

- 由于每一个连接的客户端对应一个Context，统一管理ctx后，可以取消单独用于存储在线用户的数据结构；
- 客户端监听所有上下文或所有TaskQueue，完成事件的异步消费。

 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/update-structure-3.png)

🔥架构方案（四）🔥

- <u>每个WorkerThread设置单独的TaskQueue</u>；
- 提供多类型任务队列（CommonTaskQueue、ScheduledTaskQueue）；
- 工作流程概述
  - Dispatcher负责将所有读写事件封装为Task，放到Worker线程的TaskQueue中；
  - Worker线程监听TaskQueue，消费事件。

- 存在问题💣
  - 如果TaskQueue中存在耗时的操作，TaskQueue中的后续任务都将被阻塞。



 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/update-structure-4.png)

🔥架构方案（五）🔥

- 想法：
  - Dispatcher只需要将<u>raw读写事件（SelectionKey）</u>交给线程池，线程池自动分配线程完成注册、初始化ctx；
    - 减轻Dispatcher的负担，使其更快响应客户端接入；
  - 此时，Worker线程将不仅仅需要监听TaskQueue（这样做可以提供更多的hook，便于监控整个过程）
    - select监听注册的所有SocketChannel/客户端；
    - 将事件放入内部的TaskQueue；
    - 执行TaskQueue的事件；

 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/update-structure-6.png)
