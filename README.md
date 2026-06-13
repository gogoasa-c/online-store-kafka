# Online Store Kafka Pipeline

A three-service, event-driven order pipeline built with Java 25, Spring Boot 4.0, Apache Kafka (KRaft mode), JAXB, and XSLT.

## Architecture

```
HTTP POST /orders
      │
      ▼
┌─────────────────┐   XML via topic     ┌──────────────────────┐   XML via topic     ┌─────────────────────┐
│  Order Service  │  "order.placed"     │ Fulfillment Service  │  "order.fulfilled"  │ Notification Service│
│   (port 8081)   │ ──────────────────► │    (port 8082)       │ ──────────────────► │    (port 8083)      │
└─────────────────┘                     └──────────────────────┘                     └─────────────────────┘
```

### Message Flow

| Step | Topic | Message Type | Transformation |
|------|-------|-------------|----------------|
| 1 | `order.placed` | `OrderRequest` XML | JSON → JAXB → XML |
| 2 | _(internal)_ | `FulfillmentWorkItem` XML | `order-to-fulfillment.xsl` |
| 3 | `order.fulfilled` | `FulfillmentEvent` XML | JAXB → XML |
| 4 | _(log output)_ | `NotificationPayload` XML | `fulfillment-to-notification.xsl` |

## Prerequisites

- Java 25
- Maven 3.9+
- Docker & Docker Compose

## Running the Pipeline

### 1. Start Kafka (KRaft mode, no Zookeeper)

```bash
docker compose up -d
```

Wait for the health check to pass (~15 seconds):

```bash
docker compose ps
```

### 2. Build all services

```bash
mvn clean package -DskipTests
```

### 3. Start each service in a separate terminal

```bash
# Terminal 1
cd order-service && mvn spring-boot:run

# Terminal 2
cd fulfillment-service && mvn spring-boot:run

# Terminal 3
cd notification-service && mvn spring-boot:run
```

### 4. Place an order

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C-001",
    "customerEmail": "diana@example.com",
    "items": [
      {"sku": "SKU-LAPTOP", "qty": 1, "price": 999.99},
      {"sku": "SKU-MOUSE",  "qty": 2, "price": 29.99}
    ],
    "shippingAddress": {
      "street": "123 Main Street",
      "city": "Bucharest",
      "zip": "010101"
    },
    "paymentStatus": "APPROVED"
  }'
```

### 5. Observe the logs

**Fulfillment Service** will log:
```
Processing order <uuid>... simulating warehouse packing
FulfillmentWorkItem XML:
<?xml version="1.0"?><fulfillmentWorkItem>...
Dispatching FulfillmentEvent for order <uuid>
```

**Notification Service** will log:
```
[MOCK EMAIL DISPATCH]
<?xml version="1.0"?><notificationPayload>
  <recipientEmail>diana@example.com</recipientEmail>
  <subject>Your order <uuid> has shipped!</subject>
  ...
</notificationPayload>
```

## Running Tests

```bash
# All modules
mvn test

# Single module
cd order-service && mvn test
```

## Project Structure

```
online-store-kafka/
├── docker-compose.yml
├── pom.xml                         ← parent POM
├── sample-messages/                ← example XML for each Kafka message
├── order-service/                  ← REST API + Kafka producer (port 8081)
├── fulfillment-service/            ← Kafka consumer/producer + XSLT (port 8082)
└── notification-service/           ← Kafka consumer + XSLT + mock email (port 8083)
```

## XML Schemas

### OrderRequest (topic: `order.placed`)
```xml
<orderRequest>
  <orderId>550e8400-e29b-41d4-a716-446655440000</orderId>
  <customerId>C-001</customerId>
  <customerEmail>diana@example.com</customerEmail>
  <items>
    <item><sku>SKU-LAPTOP</sku><qty>1</qty><price>999.99</price></item>
  </items>
  <shippingAddress>
    <street>123 Main Street</street>
    <city>Bucharest</city>
    <zip>010101</zip>
  </shippingAddress>
  <paymentStatus>APPROVED</paymentStatus>
  <timestamp>2026-06-13T10:00:00Z</timestamp>
</orderRequest>
```

### FulfillmentEvent (topic: `order.fulfilled`)
```xml
<fulfillmentEvent>
  <orderId>550e8400-e29b-41d4-a716-446655440000</orderId>
  <customerEmail>diana@example.com</customerEmail>
  <warehouseId>WH-BUC-01</warehouseId>
  <dispatchTimestamp>2026-06-13T10:01:30Z</dispatchTimestamp>
  <trackingCode>TRK-550E8400</trackingCode>
  <estimatedDelivery>2026-06-16</estimatedDelivery>
</fulfillmentEvent>
```

## Key Design Decisions

- **Raw XML on the wire**: Kafka messages are UTF-8 XML strings (StringSerializer/Deserializer). No Jackson for Kafka messages.
- **JAXB with Jakarta EE**: Uses `jakarta.xml.bind.annotation.*` (not `javax.xml.bind`).
- **XSLT via JDK**: `javax.xml.transform.TransformerFactory` (namespace unchanged in Jakarta EE).
- **customerEmail propagation**: Added to `OrderRequest` and `FulfillmentEvent` so the Notification Service XSLT can populate `recipientEmail`.
- **KRaft mode**: No Zookeeper required. Single-broker setup sufficient for development.
