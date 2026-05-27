-- ============================================================
-- CS 202 – Online Food Ordering System
-- DDL.sql  –  Schema Definition
-- Group 5: Goktug Gokbulut & Alperen Cimen
-- Spring 2026
-- ============================================================

CREATE DATABASE IF NOT EXISTS food_order_db;
USE food_order_db;

-- ============================================================
-- DROP tables in reverse dependency order
-- (safe to re-run)
-- ============================================================
DROP TRIGGER  IF EXISTS trg_order_item_same_restaurant;
DROP TRIGGER  IF EXISTS trg_order_coupon_restaurant;

DROP TABLE IF EXISTS Order_Status_History;
DROP TABLE IF EXISTS Rating;
DROP TABLE IF EXISTS Order_Item;
DROP TABLE IF EXISTS `Order`;
DROP TABLE IF EXISTS Coupon;
DROP TABLE IF EXISTS Menu_Item;
DROP TABLE IF EXISTS Menu_Category;
DROP TABLE IF EXISTS Restaurant_Keyword;
DROP TABLE IF EXISTS Restaurant;
DROP TABLE IF EXISTS Restaurant_Manager;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS User_Address;
DROP TABLE IF EXISTS User_Phone;
DROP TABLE IF EXISTS User;


-- ============================================================
-- USER  (ISA supertype)
-- Attributes: username (PK), password, email, city
-- Multi-valued attributes (address, phone_number) are
-- decomposed into separate tables below (1NF).
-- ============================================================
CREATE TABLE User (
    username    VARCHAR(50)     NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    email       VARCHAR(100)    NOT NULL,
    city        VARCHAR(100)    NOT NULL,
    CONSTRAINT pk_user       PRIMARY KEY (username),
    CONSTRAINT uq_user_email UNIQUE      (email)
);


-- ============================================================
-- USER_ADDRESS
-- 1NF decomposition of the multi-valued attribute "address"
-- on User.  Primary key is (username, address).
-- ============================================================
CREATE TABLE User_Address (
    username    VARCHAR(50)     NOT NULL,
    address     VARCHAR(255)    NOT NULL,
    CONSTRAINT pk_user_address PRIMARY KEY (username, address),
    CONSTRAINT fk_ua_user FOREIGN KEY (username)
        REFERENCES User(username)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- ============================================================
-- USER_PHONE
-- 1NF decomposition of the multi-valued attribute
-- "phone_number" on User.  Primary key is (username, phone_number).
-- ============================================================
CREATE TABLE User_Phone (
    username        VARCHAR(50)  NOT NULL,
    phone_number    VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_user_phone PRIMARY KEY (username, phone_number),
    CONSTRAINT fk_up_user FOREIGN KEY (username)
        REFERENCES User(username)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- ============================================================
-- CUSTOMER  (ISA subtype of User)
-- No additional attributes; inherits all User attributes.
-- ============================================================
CREATE TABLE Customer (
    username    VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_customer      PRIMARY KEY (username),
    CONSTRAINT fk_customer_user FOREIGN KEY (username)
        REFERENCES User(username)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- ============================================================
-- RESTAURANT_MANAGER  (ISA subtype of User)
-- No additional attributes; inherits all User attributes.
-- ============================================================
CREATE TABLE Restaurant_Manager (
    username    VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_rm      PRIMARY KEY (username),
    CONSTRAINT fk_rm_user FOREIGN KEY (username)
        REFERENCES User(username)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- ============================================================
-- RESTAURANT
-- Attributes: restaurant_id (PK), name, cuisine_type,
--             address, city, manager_id (FK → Restaurant_Manager)
-- avg_rating and rating_count are DERIVED – computed at
-- query time; they are NOT stored as columns (3NF).
-- ============================================================
CREATE TABLE Restaurant (
    restaurant_id   INT             NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)    NOT NULL,
    cuisine_type    VARCHAR(100)    NOT NULL,
    address         VARCHAR(255)    NOT NULL,
    city            VARCHAR(100)    NOT NULL,
    manager_id      VARCHAR(50)     NOT NULL,
    CONSTRAINT pk_restaurant      PRIMARY KEY (restaurant_id),
    CONSTRAINT fk_rest_manager    FOREIGN KEY (manager_id)
        REFERENCES Restaurant_Manager(username)
        ON DELETE RESTRICT ON UPDATE CASCADE
);


-- ============================================================
-- RESTAURANT_KEYWORD
-- 1NF decomposition of the multi-valued attribute "keyword"
-- on Restaurant.  Primary key is (restaurant_id, keyword).
-- ============================================================
CREATE TABLE Restaurant_Keyword (
    restaurant_id   INT             NOT NULL,
    keyword         VARCHAR(100)    NOT NULL,
    CONSTRAINT pk_rk          PRIMARY KEY (restaurant_id, keyword),
    CONSTRAINT fk_rk_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES Restaurant(restaurant_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- ============================================================
-- MENU_CATEGORY
-- Attributes: category_id (PK), name, restaurant_id (FK)
-- Relationship: DEFINES (Restaurant 1 — N Menu_Category)
-- ============================================================
CREATE TABLE Menu_Category (
    category_id     INT             NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    restaurant_id   INT             NOT NULL,
    CONSTRAINT pk_menu_category   PRIMARY KEY (category_id),
    CONSTRAINT fk_mc_restaurant   FOREIGN KEY (restaurant_id)
        REFERENCES Restaurant(restaurant_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- ============================================================
-- MENU_ITEM
-- Attributes: item_id (PK), name, description, price,
--             image_url, category_id (FK)
-- restaurant_id is intentionally OMITTED: it is derived via
--   Menu_Item → Menu_Category → Restaurant  (avoids transitive
--   dependency item_id → category_id → restaurant_id).
-- Relationship: GROUPS (Menu_Category 1 — N Menu_Item)
-- ============================================================
CREATE TABLE Menu_Item (
    item_id         INT             NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)    NOT NULL,
    description     TEXT,
    price           DECIMAL(10, 2)  NOT NULL,
    image_url       VARCHAR(500),
    category_id     INT             NOT NULL,
    CONSTRAINT pk_menu_item     PRIMARY KEY (item_id),
    CONSTRAINT fk_mi_category   FOREIGN KEY (category_id)
        REFERENCES Menu_Category(category_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_mi_price     CHECK (price >= 0)
);


-- ============================================================
-- COUPON
-- Attributes: coupon_id (PK), code, discount_type,
--             discount_value, valid_from, valid_until,
--             is_active, restaurant_id (FK)
-- Relationship: CREATES (Restaurant 1 — N Coupon)
-- code is unique per restaurant (not globally).
-- ============================================================
CREATE TABLE Coupon (
    coupon_id       INT                         NOT NULL AUTO_INCREMENT,
    code            VARCHAR(50)                 NOT NULL,
    discount_type   ENUM('percentage','fixed')  NOT NULL,
    discount_value  DECIMAL(10, 2)              NOT NULL,
    valid_from      DATE                        NOT NULL,
    valid_until     DATE                        NOT NULL,
    is_active       BOOLEAN                     NOT NULL DEFAULT TRUE,
    restaurant_id   INT                         NOT NULL,
    CONSTRAINT pk_coupon            PRIMARY KEY (coupon_id),
    CONSTRAINT uq_coupon_code       UNIQUE (restaurant_id, code),
    CONSTRAINT fk_coupon_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES Restaurant(restaurant_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_coupon_dates     CHECK (valid_until >= valid_from),
    CONSTRAINT chk_coupon_value     CHECK (discount_value > 0)
);


-- ============================================================
-- ORDER
-- Attributes: order_id (PK), customer_id (FK), restaurant_id
--             (FK), coupon_id (FK, nullable), status,
--             created_at, total_amount, discount_applied
-- total_amount is a deliberate denormalization (see report
-- §5.5): it snapshots the charged amount at insert time.
-- Relationships: PLACES (Customer 1 — N Order)
--                RECEIVES (Restaurant 1 — N Order)
--                USES (Order N — 0..1 Coupon)
-- ============================================================
CREATE TABLE `Order` (
    order_id            INT                                 NOT NULL AUTO_INCREMENT,
    customer_id         VARCHAR(50)                         NOT NULL,
    restaurant_id       INT                                 NOT NULL,
    coupon_id           INT                                 DEFAULT NULL,
    status              ENUM('Preparing','Sent','Accepted') NOT NULL DEFAULT 'Preparing',
    created_at          DATETIME                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount        DECIMAL(10, 2)                      NOT NULL,
    discount_applied    DECIMAL(10, 2)                      NOT NULL DEFAULT 0.00,
    CONSTRAINT pk_order             PRIMARY KEY (order_id),
    CONSTRAINT fk_order_customer    FOREIGN KEY (customer_id)
        REFERENCES Customer(username)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_restaurant  FOREIGN KEY (restaurant_id)
        REFERENCES Restaurant(restaurant_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_coupon      FOREIGN KEY (coupon_id)
        REFERENCES Coupon(coupon_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_order_total      CHECK (total_amount    >= 0),
    CONSTRAINT chk_order_discount   CHECK (discount_applied >= 0)
);


-- ============================================================
-- ORDER_ITEM
-- Translation of the M:N CONTAINS relationship between
-- Order and Menu_Item.
-- Carries two relationship attributes:
--   quantity   – number of units ordered
--   unit_price – snapshot of Menu_Item.price at order time
--                (ensures historical accuracy if price changes;
--                 depends on the full composite key, so 2NF holds)
-- ============================================================
CREATE TABLE Order_Item (
    order_id    INT             NOT NULL,
    item_id     INT             NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  DECIMAL(10, 2)  NOT NULL,
    CONSTRAINT pk_order_item    PRIMARY KEY (order_id, item_id),
    CONSTRAINT fk_oi_order      FOREIGN KEY (order_id)
        REFERENCES `Order`(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_oi_item       FOREIGN KEY (item_id)
        REFERENCES Menu_Item(item_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_oi_quantity  CHECK (quantity    > 0),
    CONSTRAINT chk_oi_price     CHECK (unit_price >= 0)
);


-- ============================================================
-- ORDER_STATUS_HISTORY  (weak entity)
-- Identifying relationship: HAS_STATUS
-- Partial key (discriminator): status
-- Full key: (order_id, status)
-- Existence depends on Order → ON DELETE CASCADE.
-- Records the timestamp of every status transition, providing
-- a full audit trail and enabling the 24-hour rating window check.
-- ============================================================
CREATE TABLE Order_Status_History (
    order_id    INT                                 NOT NULL,
    status      ENUM('Preparing','Sent','Accepted') NOT NULL,
    time_stamp  DATETIME                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_osh       PRIMARY KEY (order_id, status),
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id)
        REFERENCES `Order`(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- ============================================================
-- RATING
-- Attributes: rating_id (PK), score (1–5), comment,
--             created_at, customer_id (FK), order_id (FK)
-- restaurant_id is intentionally OMITTED: it is derived via
--   Rating → Order → Restaurant  (avoids transitive dependency
--   rating_id → order_id → restaurant_id, preserving 3NF).
-- One rating per order enforced by UNIQUE (order_id).
-- Relationship: TRIGGERS (Order 1 — 0..1 Rating)
--               WRITES   (Customer 1 — N Rating)
-- ============================================================
CREATE TABLE Rating (
    rating_id   INT         NOT NULL AUTO_INCREMENT,
    score       INT         NOT NULL,
    comment     TEXT,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_id VARCHAR(50) NOT NULL,
    order_id    INT         NOT NULL,
    CONSTRAINT pk_rating            PRIMARY KEY (rating_id),
    CONSTRAINT uq_rating_order      UNIQUE      (order_id),
    CONSTRAINT fk_rating_customer   FOREIGN KEY (customer_id)
        REFERENCES Customer(username)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_rating_order      FOREIGN KEY (order_id)
        REFERENCES `Order`(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_rating_score     CHECK (score BETWEEN 1 AND 5)
);


-- ============================================================
-- INDEXES
-- Created separately for readability; improve performance of
-- the most frequent queries (search, statistics, tracking).
-- ============================================================
CREATE INDEX idx_restaurant_city    ON Restaurant(city);
CREATE INDEX idx_rest_manager       ON Restaurant(manager_id);
CREATE INDEX idx_order_restaurant   ON `Order`(restaurant_id);
CREATE INDEX idx_order_customer     ON `Order`(customer_id);
CREATE INDEX idx_order_created      ON `Order`(created_at);
CREATE INDEX idx_oi_item            ON Order_Item(item_id);
CREATE INDEX idx_rating_customer    ON Rating(customer_id);
CREATE INDEX idx_mi_category        ON Menu_Item(category_id);
CREATE INDEX idx_mc_restaurant      ON Menu_Category(restaurant_id);
CREATE INDEX idx_coupon_restaurant  ON Coupon(restaurant_id);
CREATE INDEX idx_rk_keyword         ON Restaurant_Keyword(keyword);


-- ============================================================
-- TRIGGERS
-- ============================================================

DELIMITER $$

-- ----------------------------------------------------------------
-- TRIGGER 1: trg_order_coupon_restaurant
-- Enforces §5.6: a coupon may only be applied to an order that
-- belongs to the same restaurant that created it.
-- Fires BEFORE INSERT on Order so the bad row is never written.
-- ----------------------------------------------------------------
CREATE TRIGGER trg_order_coupon_restaurant
BEFORE INSERT ON `Order`
FOR EACH ROW
BEGIN
    IF NEW.coupon_id IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM   Coupon
            WHERE  coupon_id    = NEW.coupon_id
              AND  restaurant_id = NEW.restaurant_id
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT =
                'Coupon does not belong to the order restaurant.';
        END IF;
    END IF;
END$$


-- ----------------------------------------------------------------
-- TRIGGER 2: trg_order_item_same_restaurant
-- Enforces §5.7: all menu items in a single order must belong
-- to the same restaurant as the order itself.
-- The item's restaurant is reached via
--   Menu_Item → Menu_Category → Restaurant
-- ----------------------------------------------------------------
CREATE TRIGGER trg_order_item_same_restaurant
BEFORE INSERT ON Order_Item
FOR EACH ROW
BEGIN
    DECLARE v_item_restaurant INT;

    SELECT mc.restaurant_id
    INTO   v_item_restaurant
    FROM   Menu_Item     mi
    JOIN   Menu_Category mc ON mc.category_id = mi.category_id
    WHERE  mi.item_id = NEW.item_id;

    IF v_item_restaurant != (
        SELECT restaurant_id
        FROM   `Order`
        WHERE  order_id = NEW.order_id
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'All items in an order must belong to the same restaurant.';
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- END OF DDL.sql
-- ============================================================
