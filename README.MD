## ChatRoom

🚀🚀🚀基于NIO、单Rector多线程模型实现的聊天室，它可以帮助你：

- 快速回顾NIO编程模型；
- 快速回顾单Rector多线程模型；
- 快速掌握一个聊天室的基本功能。

🚀🚀🚀项目架构：

 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/structure.png)

👇

🔥提供优化方案架构（主从Reactor多线程）🔥

- 尽量减少接受连接线程的压力，使其更快的响应更多连接🚀，单独使用1个或多个SubReactor线程处理转发请求。
  - 如果存在多个SubReactor线程，accept获得的客户端SocketChannel可以存放在不同的BlockingQueue中，供多个SubReactor线程消费，以加快dispatch速度🚀。
- 工作任务线程池数量，建议采取可用处理器数*2，合适的线程数能够很好应对部分耗时的处理操作💥。
  - ```Runtime.getRuntime().availableProcessors()*2```

 ![Project Structure](https://github.com/MagicFollower/chatroom/blob/main/docs/img/update-structure.png)
