# CS202 – Online Food Ordering System
**Group 5:** Goktug Gokbulut & Alperen Cimen — Spring 2026

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17 or higher |
| Apache Maven | 3.8+ |
| MySQL Server | 8.0+ |

---

## 1. Database Setup

### 1a. Run the DDL script
`DDL.sql` creates the database automatically. Execute it from the project root:
```bash
mysql -u root -p < src/DDL.sql
```
Or open `src/DDL.sql` in MySQL Workbench and run it. This creates the `food_order_db` database, all tables, indexes, and triggers.

### 1b. Populate sample data
```bash
mysql -u root -p < src/DML.sql
```
Or open `src/DML.sql` in MySQL Workbench and run it.

---

## 2. Configure the Database Connection

Edit `resources/application.properties` to match your MySQL credentials:

```properties
db.url=jdbc:mysql://localhost:3306/food_order_db
db.username=root
db.password=your_password_here
```

---

## 3. Build and Run

```bash
# From the project root directory
mvn clean javafx:run
```

Alternatively, open the project in IntelliJ IDEA and run the `Launcher` class directly.

---

## 4. Default Test Accounts

### Restaurant Managers
| Username | Password | City | Manages |
|----------|----------|------|---------|
| `john_mgr` | `pass123` | Istanbul | Bosphorus Grill, Pasta Palace |
| `sarah_mgr` | `pass123` | Istanbul | Sushi World, Burger Hub |
| `mehmet_mgr` | `pass123` | Ankara | Ankara Kebap Evi |

### Customers
| Username | Password | City |
|----------|----------|------|
| `alice_cust` | `pass123` | Istanbul |
| `bob_cust` | `pass123` | Istanbul |
| `ceren_cust` | `pass123` | Istanbul |
| `dave_cust` | `pass123` | Istanbul |
| `elif_cust` | `pass123` | Ankara |

---

## 5. Application Walkthrough

### As a Customer
1. Log in with a customer account
2. **Browse Restaurants** — search by keyword (e.g. "kebap", "burger", "sushi"); results are city-filtered
3. Click **View Menu** on a restaurant card to browse items by category
4. Add items to your cart and click **Checkout**
5. Optionally apply a coupon code (e.g. `BOSPHORUS10`, `PASTA15`, `SUSHI20`, `BURGER5`, `KEBAP10`)
6. Click **Place Order** to send the order to the restaurant
7. Go to **My Orders** to track order status and rate after acceptance (within 24 hours)

### As a Restaurant Manager
1. Log in with a manager account
2. **Dashboard Overview** — view restaurant details and average rating
3. **My Restaurant** — create or update your restaurant profile and keywords
4. **Manage Menu** — add/edit/delete menu items, categories, and coupons
5. **Order Requests** — accept incoming customer orders
6. **Monthly Statistics** — view revenue, top customers, popular items, and more

---

## 6. Project Structure

```
src/
├── Main.java / Launcher.java       — Application entry points
├── controller/                     — JavaFX UI controllers
├── model/                          — Entity classes (User, Restaurant, Order, …)
├── repository/                     — JDBC data access layer (all SQL is hand-written)
├── service/                        — Business logic (OrderService, RatingService, …)
├── utils/                          — DatabaseConnectionManager, SceneManager, Session
├── DDL.sql                         — Schema definition
└── DML.sql                         — Sample data

resources/
├── application.properties          — Database connection settings
└── view/                           — JavaFX FXML layout files
```

---

## 7. Notes

- All SQL queries are hand-written using `PreparedStatement` — no ORM is used.
- The database schema is normalized to **3NF**. See the project report for ER diagram, functional dependencies, and normalization discussion.
- Passwords are stored as plain text for demonstration purposes.
- Menu item images should be placed in `resources/img/` matching the `image_url` values in the database.
