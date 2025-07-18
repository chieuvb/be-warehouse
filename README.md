# Warehouse Management API

This API provides endpoints for managing warehouse operations such as user authentication, product and category management, warehouse and zone operations, inventory tracking, and audit logging.

---

## 📌 Technologies Used

- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Security + JWT**
- **Spring Data JPA (Hibernate)**
- **MySQL**
- **Lombok**
- **Springdoc OpenAPI (Swagger UI)**
- **JUnit 5**

---

## 🔧 Server Configuration

### 🔗 Base URL

http://localhost:8080/api

### 🧠 Admin Account

- **Username:** `admin`  
- **Password:** `iamadmin`

---

### ⚙️ application.properties

```properties
# ===================================================================
# SERVER CONFIGURATION
# ===================================================================
server.port=8080
server.servlet.context-path=/api

# ===================================================================
# DATABASE CONFIGURATION (MySQL)
# ===================================================================
spring.datasource.url=jdbc:mysql://localhost:3306/warehouse?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=#your_username
spring.datasource.password=#your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===================================================================
# JPA / HIBERNATE CONFIGURATION
# ===================================================================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# ===================================================================
# JWT, CORS, and API Documentation
# ===================================================================
jwt.secret.key=#your_jwt_secret_key
jwt.expiration.ms=3600000
app.cors.allowed-origins=#ypur_allowed_origins

springdoc.api-docs.enabled=false
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=false
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.title=Warehouse Management API
springdoc.swagger-ui.description=API documentation for the Warehouse Management System.
springdoc.swagger-ui.version=1.0.0
````

> ⚠️ **Security Note:** Move database credentials and JWT secrets to environment variables in production!

---

## 🔐 Authentication

| Method | Endpoint         | Description                            |
|--------|------------------|----------------------------------------|
| POST   | `/auth/login`    | Authenticate user and return JWT token |
| POST   | `/auth/register` | Register a new user                    |

---

## 👤 User Management

| Method | Endpoint            | Description                   |
|--------|---------------------|-------------------------------|
| GET    | `/users`            | Get list of users (paginated) |
| POST   | `/users`            | Create a new user             |
| GET    | `/users/{id}`       | Get user by ID                |
| PUT    | `/users/{id}`       | Update user by ID             |
| DELETE | `/users/{id}`       | Delete user by ID             |
| PUT    | `/users/{id}/roles` | Update user roles             |

---

## 🏷️ Role Management

| Method | Endpoint                     | Description             |
|--------|------------------------------|-------------------------|
| GET    | `/roles`                     | Get all roles           |
| POST   | `/roles`                     | Create a new role       |
| GET    | `/roles/{id}`                | Get role by ID          |
| PUT    | `/roles/{id}`                | Update role             |
| DELETE | `/roles/{id}`                | Delete role             |
| POST   | `/roles/{id}/users`          | Assign users to role    |
| DELETE | `/roles/{id}/users/{userId}` | Unassign user from role |

---

## 🏬 Warehouse Management

| Method | Endpoint           | Description                     |
|--------|--------------------|---------------------------------|
| GET    | `/warehouses`      | List all warehouses (paginated) |
| POST   | `/warehouses`      | Create a warehouse              |
| GET    | `/warehouses/{id}` | Get warehouse by ID             |
| PUT    | `/warehouses/{id}` | Update warehouse                |
| DELETE | `/warehouses/{id}` | Delete warehouse                |

---

## 📦 Zone Management

| Method | Endpoint                                   | Description                |
|--------|--------------------------------------------|----------------------------|
| GET    | `/warehouses/{warehouseId}/zones`          | Get all zones in warehouse |
| POST   | `/warehouses/{warehouseId}/zones`          | Create zone in warehouse   |
| GET    | `/warehouses/{warehouseId}/zones/{zoneId}` | Get zone by ID             |
| PUT    | `/warehouses/{warehouseId}/zones/{zoneId}` | Update zone                |
| DELETE | `/warehouses/{warehouseId}/zones/{zoneId}` | Delete zone                |

---

## 📁 Product Category

| Method | Endpoint                          | Description          |
|--------|-----------------------------------|----------------------|
| POST   | `/product-categories`             | Create category      |
| GET    | `/product-categories/{id}`        | Get category by ID   |
| PUT    | `/product-categories/{id}`        | Update category      |
| DELETE | `/product-categories/{id}`        | Delete category      |
| GET    | `/product-categories/tree`        | Get category tree    |
| GET    | `/product-categories/name/{name}` | Get category by name |

---

## 🛒 Product Management

| Method | Endpoint                      | Description                  |
|--------|-------------------------------|------------------------------|
| GET    | `/products`                   | Get all products (paginated) |
| POST   | `/products`                   | Create a new product         |
| GET    | `/products/{id}`              | Get product by ID            |
| PUT    | `/products/{id}`              | Update product               |
| DELETE | `/products/{id}`              | Delete product               |
| GET    | `/products/sku/{sku}`         | Get product by SKU           |
| GET    | `/products/barcode/{barcode}` | Get product by barcode       |

---

## 📏 Unit of Measure

| Method | Endpoint                 | Description    |
|--------|--------------------------|----------------|
| GET    | `/units-of-measure`      | Get all units  |
| POST   | `/units-of-measure`      | Create unit    |
| GET    | `/units-of-measure/{id}` | Get unit by ID |
| PUT    | `/units-of-measure/{id}` | Update unit    |
| DELETE | `/units-of-measure/{id}` | Delete unit    |

---

## 🧮 Inventory Management

| Method | Endpoint                             | Description                    |
|--------|--------------------------------------|--------------------------------|
| GET    | `/inventories`                       | List all inventory (paginated) |
| POST   | `/inventories/move`                  | Move inventory between zones   |
| POST   | `/inventories/adjust`                | Adjust inventory quantities    |
| GET    | `/inventories/{inventoryId}/history` | Get inventory adjustment logs  |

---

## 📝 Audit Logs

| Method | Endpoint              | Description                    |
|--------|-----------------------|--------------------------------|
| GET    | `/audit-logs`         | Get all audit logs (paginated) |
| GET    | `/audit-logs/{actor}` | Get logs by actor name         |

---

## 👤 Profile

| Method | Endpoint              | Description              |
|--------|-----------------------|--------------------------|
| GET    | `/me`                 | Get current user profile |
| POST   | `/me/change-password` | Change user password     |

---

## 🔐 Security

All endpoints (except authentication) require a valid JWT Bearer token in the `Authorization` header:

```
Authorization: Bearer <your_token_here>
```

---

## 📄 API Specification

* **OpenAPI Version:** 3.1.0
* **Version:** 1.0
* **Format:** JSON

---

## 📫 Contact

For questions or issues, please contact me.
