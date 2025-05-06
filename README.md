# PayPlus
PayPlus: Payment Platform with MVC &amp; DDD Architecture

- Internal Network Tunneling & Real-Time Functional Testing: Utilized NATAPP for internal network tunneling, allowing secure external access to locally hosted payment interfaces. This enabled end-to-end functional testing under real-world conditions to ensure the system’s readiness for production deployment.

- WeChat Pay & Enhanced Security: Integrated WeChat QR code payment with user IP-based location monitoring to trigger secure template messages. This approach not only improves user account protection but also effectively reduces potential risks.

- Order Service Design: Developed a comprehensive order service system, enforcing strict validation on order data and ensuring reliable creation and persistent storage of orders. This design guarantees transaction integrity and consistency, supporting high availability of the system.

- Alipay Payment Integration: Seamlessly connected with the Alipay sandbox environment, completing the SDK configuration for payment. By establishing links from order creation to payment status callback notifications, the system ensures accurate and timely feedback on payment outcomes, resulting in a smooth and efficient transaction process.


domain: authentication, order, payment
RPC -> product (id, name, desc, price)
WeChat -> login (token, qr code)
Alipay -> prepay (notify, message)
