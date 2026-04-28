# PayPlus 面试复习笔记

这份笔记不是给别人看的，是给我自己面试前快速复习用的。重点是把“我做了什么、为什么这么做、源码怎么串起来、面试官会问什么”讲清楚。

## 1. 项目一句话介绍

这是一个虚拟商品订单与支付系统，主要实现了：

- 商品下单
- 支付宝沙箱支付
- 支付异步回调验签
- 订单状态更新
- 超时关单
- 漏回调补偿
- 微信扫码登录

同一套业务做了两版：

- `payplus-mvc`：传统 MVC 分层
- `payplus-ddd`：基于 DDD 的重构版

如果面试官问“这个项目最大的特点是什么”，答案就是：

`不是只接了一个支付宝 SDK，而是把订单、支付、回调、补偿、登录这几个完整业务场景串起来，并且做了 MVC -> DDD 的重构实践。`

## 2. 我简历上该怎么讲

我可以这样说：

这是一个基于 `Spring Boot + MyBatis + MySQL` 的虚拟商品订单与支付系统，我主要负责订单创建、支付下单、支付宝异步回调验签、订单状态更新，以及超时关单和支付补偿机制的实现。另外项目还做了 MVC 到 DDD 的重构，我参与了订单、支付和回调逻辑的拆分，把核心流程抽象成领域服务、仓储、策略工厂和 Port 适配层，提升了系统的可维护性和后续扩展能力。

## 3. 技术栈怎么说

核心技术：

- Spring Boot
- MyBatis
- MySQL
- Alipay SDK
- Retrofit2
- Guava Cache
- Guava EventBus
- Docker Compose

一定要记住：

- 当前业务代码里真正用的是 `Guava Cache`
- 没有真正把 Redis 落到业务读写里
- 虽然 `docker-compose` 里有 Redis，代码注释里也提过 Redis，但项目实际缓存实现还是本地缓存

如果面试官问“用了 Redis 吗”，要回答：

`没有真正业务化接入，当前登录态和 access_token 缓存使用的是 Guava 本地缓存，Redis 只是环境预留。`

## 4. 业务模块拆分

这个项目可以拆成四块：

### 4.1 订单模块

负责：

- 根据用户和商品创建订单
- 写入数据库
- 维护订单状态

核心表：

- `pay_order`

核心字段：

- `order_id`
- `user_id`
- `product_id`
- `status`
- `pay_url`
- `pay_time`

### 4.2 支付模块

负责：

- 调用支付宝生成支付页面
- 接收支付宝异步回调
- 验签
- 更新订单支付状态

### 4.3 补偿模块

负责：

- 主动扫描未支付订单
- 调用支付宝查单
- 修复漏回调导致的状态不一致
- 关闭长时间未支付订单

### 4.4 登录模块

负责：

- 微信二维码 ticket 生成
- 微信扫码事件处理
- 登录态缓存
- 前端轮询登录状态

## 5. 最重要的支付闭环

面试时最重要的一段，就是把“从下单到支付成功”讲顺。

完整流程如下：

1. 用户传入 `userId` 和 `productId` 发起下单
2. 系统先查询当前用户是否已经有同商品的未支付订单
3. 如果有未支付订单，直接复用原订单，避免重复下单
4. 如果没有，则查询商品信息并创建本地订单
5. 系统调用支付宝生成支付页面，把 `payUrl` 写回订单
6. 用户跳转支付宝完成支付
7. 支付宝异步回调系统
8. 服务端校验 `trade_status`
9. 服务端使用支付宝公钥对回调做 RSA 验签
10. 验签通过后，把订单状态改成已支付

一句话总结：

`这是一个“订单创建 -> 支付下单 -> 异步回调 -> 验签 -> 改订单状态”的完整交易闭环。`

## 6. 为什么要先查未支付订单

这个点源码里有，讲出来会显得你不是只会背流程。

在创建订单时，系统会先查数据库里当前用户同一商品的最近订单：

- 如果状态已经是 `PAY_WAIT`，说明已经有待支付订单，直接返回原来的支付链接
- 如果状态还是 `CREATE`，说明订单建了但支付单没建成功，就补建支付单
- 只有都不存在时，才真正创建新订单

这样做的目的：

- 避免重复下单
- 避免因为前端重复点击产生多笔脏订单
- 支持支付单丢失后的恢复

源码位置：

- `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/order/service/AbstractOrderService.java`
- `payplus-mvc/payplus-service/src/main/java/com/payplus/service/impl/OrderServiceImpl.java`

## 7. 支付回调为什么要验签

标准答案：

因为回调接口是开放给第三方调用的，如果不验签，任何人都可以伪造一个 HTTP 请求，把本地订单直接改成支付成功，所以必须通过支付宝公钥验证回调内容确实来自支付宝。

这块代码在：

- `payplus-ddd/.../trigger/http/AlipayController.java`
- `payplus-mvc/.../controller/AliPayController.java`

关键动作：

- 读取请求参数
- 校验 `trade_status == TRADE_SUCCESS`
- 组装参数内容
- 用 `AlipaySignature.rsa256CheckContent(...)` 验签
- 验签通过后再调用 `changeOrderPaySuccess`

## 8. 为什么还要做补偿机制

这是支付系统里最容易被追问的问题。

不能只依赖支付平台的异步回调，因为可能出现：

- 网络波动
- 回调超时
- 服务端短暂不可用
- 回调处理异常

结果就是：

- 用户其实已经付钱了
- 但我们本地订单状态还是未支付

所以项目里加了“主动查单补偿”：

- 定时扫描超过 1 分钟还处于 `PAY_WAIT` 的订单
- 调用支付宝查单接口查询真实状态
- 如果支付宝返回已支付，则主动把本地订单修正成已支付

目的就是：

`保证订单状态最终一致，而不是完全依赖回调实时一致。`

源码：

- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/job/NoPayNotifyOrderJob.java`
- `payplus-mvc/payplus-web/src/main/java/com/payplus/job/NoPayNotifyOrderJob.java`

## 9. 超时关单在做什么

除了补偿任务，还有一个超时关单任务。

逻辑是：

- 定时扫描超过 30 分钟还未支付的订单
- 把这些订单改成关闭状态

它的作用不是修复支付结果，而是：

- 清理无效订单
- 让订单生命周期更完整
- 避免大量陈旧待支付订单长期堆积

源码：

- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/job/TimeoutCloseOrderJob.java`
- `payplus-mvc/payplus-web/src/main/java/com/payplus/job/TimeoutCloseOrderJob.java`

## 10. 微信扫码登录怎么讲

这部分不要讲太花，按流程讲清楚即可。

流程：

1. 客户端调用接口获取二维码 `ticket`
2. 服务端调用微信接口生成二维码 ticket
3. 前端展示二维码
4. 用户扫码后，微信把事件回调到我们配置的公众号接口
5. 服务端解析 XML，拿到 `ticket` 和 `openId`
6. 把 `ticket -> openId` 放到本地缓存里
7. 前端轮询 `check_login`，如果查到 `openId` 就说明登录成功

这里缓存了两类数据：

- `weChatAccessTokenCache`
- `openIdTokenCache`

源码：

- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/LoginController.java`
- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/WeChatPortalController.java`
- `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/auth/service/WeChatLoginService.java`
- `payplus-ddd/payplus-ddd-infrastructure/src/main/java/com/payplus/infrastructure/adapter/port/LoginPort.java`
- `payplus-ddd/payplus-ddd-app/src/main/java/com/payplus/config/GuavaConfig.java`

## 11. MVC 和 DDD 版本的区别

### 11.1 MVC 版本怎么理解

MVC 版就是传统三层/多层风格：

- Controller 接请求
- Service 写主要业务
- DAO 负责数据库

优点：

- 好上手
- 开发快

问题：

- 随着业务变复杂，支付、回调、补偿、登录逻辑容易都堆在 Service 层
- 业务规则和第三方依赖容易耦合在一起

### 11.2 DDD 版本怎么理解

DDD 版把职责拆得更清晰：

- `trigger`：控制器、定时任务、监听器
- `domain`：订单服务、支付服务、领域模型、聚合、值对象
- `infrastructure`：仓储实现、支付宝/微信适配器、DAO
- `api/types/app`：接口、配置、公共类型

面试里最重要的是别把 DDD 说成很玄学，直接说：

`我理解这个重构的重点，不是为了追求概念，而是为了把核心业务逻辑和数据库/第三方支付调用解耦，降低后续扩展新支付方式或调整业务规则的修改成本。`

## 12. DDD 重构到底重构了什么

你简历里写“重构订单、支付与回调逻辑”，面试官很可能会问“具体怎么重构”。

这里直接回答三点：

### 12.1 订单逻辑从具体实现里抽到了领域服务

在 DDD 版里，创建订单的主要流程由 `AbstractOrderService / OrderService` 管理。

它负责：

- 判断是否复用未支付订单
- 构建订单聚合
- 调用仓储保存订单
- 调用支付服务生成支付单

### 12.2 支付能力通过策略工厂解耦

核心类：

- `PaymentService`
- `PaymentStrategyFactory`
- `IPaymentPort`

现在虽然主要接的是支付宝，但设计上已经变成：

- 业务层只说“我要预下单”
- 不直接依赖某个具体支付实现
- 由策略工厂根据 `payMethod` 选择具体实现

这个设计的好处是：

- 后续新增微信支付，不需要大改核心订单逻辑
- 只要新增一个实现 `IPaymentPort` 的适配器，再交给工厂注册即可

### 12.3 第三方依赖通过 Port 适配层隔离

比如：

- `AlipayPort` 负责支付宝预下单
- `LoginPort` 负责微信相关接口
- `ProductPort` 负责商品查询

这样领域层只依赖接口，不直接写第三方调用细节。

## 13. 仓储层在这个项目里负责什么

仓储层不要说成“就是 DAO”。

更准确地说：

仓储层负责把领域对象和数据库存储模型进行转换，并对外提供符合业务语义的数据访问能力。

在这个项目里，`OrderRepository` 主要负责：

- 保存订单聚合
- 查询未支付订单
- 更新支付信息
- 修改订单为支付成功
- 查询需补偿订单
- 查询超时订单

同时它还会在支付成功后通过 `EventBus` 发布事件。

源码：

- `payplus-ddd/payplus-ddd-infrastructure/src/main/java/com/payplus/infrastructure/adapter/repository/OrderRepository.java`

## 14. EventBus 在这里做了什么

支付成功后，项目会发一个支付成功事件，由监听器消费。

当前监听器实现比较轻，主要是日志和后续动作预留，但它表达的是一种设计思路：

- 支付成功后，不把所有后置逻辑都塞进支付主流程
- 而是通过事件机制解耦后续处理

典型后续动作可以是：

- 发货
- 开通会员
- 充值到账
- 发送通知

源码：

- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/listener/OrderPaySuccessListener.java`

## 15. 数据库表和状态流转要记住

表：`pay_order`

要记住的状态：

- `CREATE`：订单创建完成，但还没生成支付单
- `PAY_WAIT`：支付单已生成，等待支付
- `PAY_SUCCESS`：支付成功
- `CLOSE`：超时关闭

一个典型状态流转：

`CREATE -> PAY_WAIT -> PAY_SUCCESS`

超时场景：

`CREATE / PAY_WAIT -> CLOSE`

## 16. 我自己的贡献怎么讲

建议统一讲成这三块：

- 完成订单创建、支付下单、异步回调验签和订单状态更新等核心交易链路开发
- 实现超时关单和支付结果补偿机制，提升订单状态一致性
- 参与 MVC 到 DDD 的重构，将订单、支付、回调流程拆分为领域服务、仓储、策略工厂和 Port 适配层

## 17. 1 分钟项目介绍模板

这个项目是一个虚拟商品订单与支付系统，主要实现了订单创建、支付宝支付、微信扫码登录以及订单状态管理。技术上使用了 Spring Boot、MyBatis、MySQL、Alipay SDK 和 Guava Cache。我的主要工作是完成订单创建、支付下单、异步回调验签和支付成功后的订单状态更新，打通从下单到支付成功的完整闭环。因为支付系统不能只依赖第三方回调，我还实现了超时关单和支付补偿机制，通过定时任务主动扫描未完成订单并调用支付宝查单，保证订单状态最终一致。另外，这个项目还做了 MVC 到 DDD 的重构，我参与了订单、支付和回调逻辑的拆分，把核心流程抽象成领域服务、仓储、支付策略工厂和 Port 适配层，提高了系统的可维护性和扩展性。

## 18. 3 分钟展开版模板

如果面试官让展开讲，可以按这个顺序：

1. 先说业务目标
项目核心是做一个虚拟商品支付闭环，解决下单、支付、回调、订单状态维护这些问题。

2. 再说核心流程
用户选择商品后发起下单，系统先检查是否已有未支付订单，没有的话就创建订单并调用支付宝生成支付页面。用户支付成功后，支付宝异步回调系统，服务端先校验交易状态，再做 RSA 验签，最后把订单状态更新为已支付。

3. 再说为什么有补偿机制
因为支付回调可能丢失或处理失败，不能只依赖回调，所以项目还实现了定时扫描和主动查单，保证订单状态最终一致。

4. 再说架构亮点
项目做了 MVC 到 DDD 的重构，把订单服务、支付服务、仓储、策略工厂和第三方适配层拆开，降低业务与基础设施耦合。

5. 最后说个人贡献
我主要负责核心交易链路、支付补偿和 DDD 重构相关实现。

## 19. 高频面试题与回答

### 19.1 为什么支付回调要验签？

因为回调接口是外部可访问的，如果不验签，任何人都可以伪造回调请求把订单改成支付成功，所以必须通过支付宝公钥校验回调数据来源。

### 19.2 为什么不能只依赖异步回调？

因为回调可能因为网络、服务异常或处理失败而丢失，所以需要主动查单做补偿，保证最终一致性。

### 19.3 为什么创建订单前要查未支付订单？

为了避免重复下单，也为了复用已经创建但尚未支付的订单，减少脏数据和重复支付风险。

### 19.4 DDD 重构带来了什么收益？

核心收益有两个：

- 业务逻辑和基础设施解耦了
- 支付方式扩展更方便了

### 19.5 如果以后要接微信支付，你这个设计怎么扩展？

在 DDD 版里新增一个实现 `IPaymentPort` 的微信支付适配器，返回自己的支付方式标识，然后由 `PaymentStrategyFactory` 自动注册即可，核心订单流程基本不需要大改。

### 19.6 项目里用了 Redis 吗？

没有真正业务化接入。当前代码使用的是 Guava 本地缓存，Redis 只存在于环境配置或预留设计里。

### 19.7 这个项目最大的难点是什么？

我会答：

最大的难点是保证支付结果和本地订单状态的一致性，因为这不只是接 SDK，而是要考虑回调异常、重复下单、漏回调、超时订单这些真实交易场景。

## 20. 可以主动承认的不足

如果面试官问项目不足，可以这样答：

- 当前商品服务还是本地 mock，没有接真实商品中心
- 缓存目前是本地缓存，不适合多实例共享登录态
- 测试更多是验证型测试，自动化回归还不够完整
- 支付成功后的后置动作目前以事件解耦为主，业务深度还可以继续扩展

这种回答比硬说“没问题”更真实。

## 21. 面试前最后 30 秒复习点

面试前只记住这几个关键词：

- 订单闭环：下单 -> 预支付 -> 回调 -> 验签 -> 改状态
- 异步补偿：主动查单，保证最终一致
- 超时关单：清理无效订单
- 微信登录：ticket、扫码回调、openId、缓存
- MVC -> DDD：领域服务、仓储、策略工厂、Port 适配层
- 缓存：Guava，不是 Redis

## 22. 关键源码索引

### DDD 版

- 支付控制器：
  `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/AlipayController.java`
- 登录控制器：
  `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/LoginController.java`
- 微信回调控制器：
  `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/WeChatPortalController.java`
- 订单抽象服务：
  `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/order/service/AbstractOrderService.java`
- 订单领域服务：
  `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/order/service/OrderService.java`
- 支付领域服务：
  `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/payment/service/PaymentService.java`
- 支付策略工厂：
  `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/payment/service/strategy/PaymentStrategyFactory.java`
- 仓储实现：
  `payplus-ddd/payplus-ddd-infrastructure/src/main/java/com/payplus/infrastructure/adapter/repository/OrderRepository.java`
- 支付宝适配器：
  `payplus-ddd/payplus-ddd-infrastructure/src/main/java/com/payplus/infrastructure/adapter/port/AlipayPort.java`
- 微信适配器：
  `payplus-ddd/payplus-ddd-infrastructure/src/main/java/com/payplus/infrastructure/adapter/port/LoginPort.java`
- 补偿任务：
  `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/job/NoPayNotifyOrderJob.java`
- 超时关单任务：
  `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/job/TimeoutCloseOrderJob.java`

### MVC 版

- 订单服务：
  `payplus-mvc/payplus-service/src/main/java/com/payplus/service/impl/OrderServiceImpl.java`
- 支付控制器：
  `payplus-mvc/payplus-web/src/main/java/com/payplus/controller/AliPayController.java`
- 登录服务：
  `payplus-mvc/payplus-service/src/main/java/com/payplus/service/impl/WeChatLoginServiceImpl.java`

