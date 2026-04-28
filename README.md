# PayPlus

PayPlus is a virtual-product order and payment system implemented twice in the same repository:

- `payplus-mvc`: a classic layered MVC version
- `payplus-ddd`: a DDD-style refactor of the same business flow

The project focuses on the full payment lifecycle rather than a single SDK integration: order creation, Alipay checkout, async callback verification, order state transition, timeout closing, missing-callback compensation, and WeChat QR-code login.

## Why This Repository Exists

This repository is useful for comparing how the same payment business can be modeled in two styles:

- MVC for straightforward feature delivery
- DDD for better separation of business rules, infrastructure, and extension points

The DDD version is the stronger reference if you want to understand how the payment flow was decomposed into domain services, repositories, strategy selection, and adapter ports.

## Business Scope

The current codebase covers these scenarios:

- Create an order for a virtual product
- Generate an Alipay page-payment request
- Accept Alipay async callbacks and verify the signature
- Update order status to paid after successful callback
- Scan unpaid orders on a schedule
- Close orders that stay unpaid for too long
- Compensate for missing or failed payment callbacks by actively querying Alipay
- Support WeChat QR-code login and cache login state locally

## Tech Stack

- Java 8
- Spring Boot 2.7.x
- MyBatis
- MySQL
- Alipay Java SDK
- Retrofit2
- Guava Cache
- Guava EventBus
- Docker Compose

Notes:

- The current implementation uses `Guava Cache` for local caching.
- Redis appears in deployment files, but Redis is not the active cache implementation in the business code.

## Repository Layout

```text
PayPlus/
├── payplus-ddd/
│   ├── payplus-ddd-api/             # API contracts and DTOs
│   ├── payplus-ddd-app/             # Spring Boot app, configs, MyBatis resources
│   ├── payplus-ddd-domain/          # Core domain model and domain services
│   ├── payplus-ddd-infrastructure/  # Repository impls, external gateways, payment ports
│   ├── payplus-ddd-trigger/         # Controllers, jobs, listeners
│   └── payplus-ddd-types/           # Shared constants, exceptions, events, SDK helpers
├── payplus-mvc/
│   ├── payplus-common/              # Shared constants, exceptions, utilities
│   ├── payplus-dao/                 # DAO interfaces
│   ├── payplus-domain/              # Request/response/domain objects
│   ├── payplus-service/             # Service layer
│   └── payplus-web/                 # Controllers, jobs, app bootstrap, resources
└── README.md
```

## Core Flow

### 1. Order creation

The system receives `userId` and `productId`, checks whether the user already has an unpaid order for the same product, and reuses that order if possible.

If no reusable order exists:

- query product info
- create a local order record in MySQL
- call Alipay to generate a payment page
- persist `payUrl` and move the order into waiting-for-payment status

### 2. Payment callback

After the user pays through Alipay:

- Alipay calls the async notify endpoint
- the server checks `trade_status`
- the callback signature is verified with Alipay's public key
- the local order is updated to paid

This is the key "order -> payment -> callback -> state update" business loop.

### 3. Compensation and timeout handling

The system does not rely on async callbacks alone.

- `NoPayNotifyOrderJob` scans orders that remain unpaid and actively queries Alipay
- `TimeoutCloseOrderJob` closes orders that have stayed unpaid for too long

This design reduces the risk of local order status drifting from the real payment result.

### 4. WeChat login

The WeChat login flow works like this:

- request a QR-code ticket from WeChat
- let the client display the QR code
- receive the scan event from the WeChat portal callback
- map `ticket -> openId` in local cache
- poll login status from the frontend

`access_token` and login state are cached with Guava.

## Architecture Comparison

### MVC Version

The MVC implementation keeps most business flow in the service layer:

- controllers accept requests
- services orchestrate order creation and payment logic
- DAOs read and write MySQL

This version is easier to read at first glance and is suitable for fast iteration, but payment-specific rules and integration logic tend to accumulate inside service classes.

### DDD Version

The DDD implementation separates responsibilities more explicitly:

- `trigger`: HTTP entrypoints, jobs, listeners
- `domain`: business rules, order service, payment service, aggregates, value objects
- `infrastructure`: DAO implementations, third-party gateways, port adapters
- `api/types/app`: contracts, configs, shared objects

Important examples in the DDD version:

- `OrderService`: domain orchestration for order creation
- `PaymentService`: delegates prepay behavior to payment ports
- `PaymentStrategyFactory`: resolves the payment implementation by pay method
- `OrderRepository`: persistence and event publication
- `AlipayPort`: Alipay-specific prepay integration
- `LoginPort`: WeChat-specific gateway integration

This structure makes it easier to extend payment methods without rewriting core order logic.

## Key Files to Read First

If you want a fast code walkthrough, start here:

- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/AlipayController.java`
- `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/order/service/AbstractOrderService.java`
- `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/order/service/OrderService.java`
- `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/payment/service/PaymentService.java`
- `payplus-ddd/payplus-ddd-domain/src/main/java/com/payplus/domain/payment/service/strategy/PaymentStrategyFactory.java`
- `payplus-ddd/payplus-ddd-infrastructure/src/main/java/com/payplus/infrastructure/adapter/repository/OrderRepository.java`
- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/job/NoPayNotifyOrderJob.java`
- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/job/TimeoutCloseOrderJob.java`
- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/LoginController.java`
- `payplus-ddd/payplus-ddd-trigger/src/main/java/com/payplus/trigger/http/WeChatPortalController.java`

## Database

Both implementations use a single core table: `pay_order`.

Schema files:

- DDD: `payplus-ddd/docs/dev-ops/mysql/sql/payplus-mall.sql`
- MVC: `payplus-mvc/docs/dev-ops/mysql/sql/s-pay-mall.sql`

Important fields:

- `order_id`
- `user_id`
- `product_id`
- `status`
- `pay_url`
- `pay_time`

Typical status values used by the code:

- `CREATE`
- `PAY_WAIT`
- `PAY_SUCCESS`
- `CLOSE`

## Local Run

The project contains environment-specific YAML files and Docker resources, but before running locally you should review and replace all environment-dependent values.

### Prerequisites

- JDK 8
- Maven
- MySQL 8.x

### Basic steps

1. Create the database from the SQL file that matches the implementation you want to run.
2. Update the datasource in the corresponding `application-*.yml`.
3. Replace demo or local-only payment and WeChat configuration values.
4. Start either the MVC app or the DDD app.

Typical app entrypoints:

- MVC: `payplus-mvc/payplus-web/src/main/java/com/payplus/Application.java`
- DDD: `payplus-ddd/payplus-ddd-app/src/main/java/com/payplus/Application.java`

### Maven modules

- MVC root: `payplus-mvc/pom.xml`
- DDD root: `payplus-ddd/pom.xml`

## API Endpoints

Main HTTP endpoints in the DDD version:

- `POST /api/v1/alipay/create_pay_order`
- `POST /api/v1/alipay/alipay_notify_url`
- `GET /api/v1/login/wechat_qrcode_ticket`
- `GET /api/v1/login/check_login`
- `GET /api/v1/wechat/portal/receive`
- `POST /api/v1/wechat/portal/receive`

The MVC version exposes the same business endpoints with the same overall flow.

## Testing

The repository contains basic Spring Boot tests and a small Alipay sandbox test class.

Examples:

- `payplus-ddd/payplus-ddd-app/src/test/java/com/payplus/test/domain/OrderServiceTest.java`
- `payplus-mvc/payplus-web/src/test/java/com/payplus/test/web/AliPayTest.java`

The tests are more like development verification samples than a full automated regression suite.

## Interview / Learning Angles

This repository is a good example for discussing:

- how to build a payment order lifecycle
- why async callbacks need signature verification
- why callback-only status update is not enough
- how to design compensation jobs for eventual consistency
- how to compare MVC and DDD in a concrete business system
- how to decouple payment channels with a strategy factory and port abstraction

For interview preparation in Chinese, see `NOTE.md`.

## Caveats

- Configuration files contain environment-specific values and demo credentials; treat them as local examples and replace them before real use.
- Redis is not actively wired into the business flow even though related deployment files exist.
- Product data is currently mocked through a local RPC-style adapter rather than a real external product service.

## License

Apache License 2.0. See `LICENSE`.
