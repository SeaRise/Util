﻿- 一些工具类

- lock 
	- 分段锁
	- 分段读写锁
- time 
	- 时间缓存
- buffer 
	- 封装了byteBuffer: heap,direct,dataOperator,combine
	- 内存池: 位图分配
- cache
	- 引用计数
	- lru
- current
	- 并发容器,
		- AppendOnlyArrayList
		- 单生产者单消费者的ringbuffer
	- diskBaseQueue
- Common
	- 通用
	- thread safe堆外内存布隆过滤器
	- StringBuilder替代工具
	- 序列化工具
- thread
    - 可以伸缩线程数目的线程池