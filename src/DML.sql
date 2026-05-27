-- ============================================================
-- CS 202 – Online Food Ordering System
-- DML.sql  –  Sample Data
-- Group 5: Goktug Gokbulut & Alperen Cimen
-- Spring 2026
-- ============================================================

USE food_order_db;
-- Minimum requirements covered:
--   ✓  3 Restaurant Managers
--   ✓  5 Customers
--   ✓  5 Restaurants  (with keywords)
--   ✓  5 Menu Categories
--   ✓  15 Menu Items
--   ✓  5 Coupons
--   ✓  10 Orders  (placed by different customers, each with items)
--   ✓  10 Ratings (distributed across restaurants)
-- ============================================================

-- ============================================================
-- 1. USER  (supertype rows – 3 managers + 5 customers = 8 rows)
-- Passwords stored as placeholder hashes; real app uses bcrypt.
-- ============================================================
INSERT INTO User (username, password, email, city) VALUES
-- Restaurant Managers
('john_mgr',   'pass123',   'john@foodapp.com',   'Istanbul'),
('sarah_mgr',  'pass123',  'sarah@foodapp.com',  'Istanbul'),
('mehmet_mgr', 'pass123', 'mehmet@foodapp.com', 'Ankara'),
-- Customers
('alice_cust',  'pass123',  'alice@foodapp.com',  'Istanbul'),
('bob_cust',    'pass123',    'bob@foodapp.com',    'Istanbul'),
('ceren_cust',  'pass123',  'ceren@foodapp.com',  'Istanbul'),
('dave_cust',   'pass123',   'dave@foodapp.com',   'Istanbul'),
('elif_cust',   'pass123',   'elif@foodapp.com',   'Ankara');


-- ============================================================
-- 2. USER_ADDRESS  (multi-valued – 1NF decomposition)
--    alice_cust intentionally has two addresses to demonstrate
--    that the multi-valued attribute is correctly normalised.
-- ============================================================
INSERT INTO User_Address (username, address) VALUES
('john_mgr',   'Besiktas Mah. No:12, Istanbul'),
('sarah_mgr',  'Kadikoy Mah. No:45, Istanbul'),
('mehmet_mgr', 'Cankaya Cad. No:8, Ankara'),
('alice_cust',  'Sisli Mah. No:3, Istanbul'),
('alice_cust',  'Levent Cad. No:7, Istanbul'),
('bob_cust',    'Uskudar Mah. No:21, Istanbul'),
('ceren_cust',  'Beyoglu Istiklal Cad. No:55, Istanbul'),
('dave_cust',   'Bakirkoy Cumhuriyet Cad. No:14, Istanbul'),
('elif_cust',   'Yenimahalle Bati Blv. No:9, Ankara');


-- ============================================================
-- 3. USER_PHONE  (multi-valued – 1NF decomposition)
--    alice_cust intentionally has two phone numbers.
-- ============================================================
INSERT INTO User_Phone (username, phone_number) VALUES
('john_mgr',   '+905551234567'),
('sarah_mgr',  '+905559876543'),
('mehmet_mgr', '+905552345678'),
('alice_cust',  '+905553456789'),
('alice_cust',  '+905553456780'),
('bob_cust',    '+905554567890'),
('ceren_cust',  '+905555678901'),
('dave_cust',   '+905556789012'),
('elif_cust',   '+905557890123');


-- ============================================================
-- 4. CUSTOMER  (ISA subtype)
-- ============================================================
INSERT INTO Customer (username) VALUES
('alice_cust'),
('bob_cust'),
('ceren_cust'),
('dave_cust'),
('elif_cust');


-- ============================================================
-- 5. RESTAURANT_MANAGER  (ISA subtype)
-- ============================================================
INSERT INTO Restaurant_Manager (username) VALUES
('john_mgr'),
('sarah_mgr'),
('mehmet_mgr');


-- ============================================================
-- 6. RESTAURANT  (5 restaurants)
--    john_mgr manages 2 Istanbul restaurants (demonstrates 1-N).
--    sarah_mgr manages 2 Istanbul restaurants.
--    mehmet_mgr manages 1 Ankara restaurant.
--    City matches the customers' city so the same-city filter works.
-- ============================================================
INSERT INTO Restaurant (restaurant_id, name, cuisine_type, address, city, manager_id) VALUES
(1, 'Bosphorus Grill',  'Turkish',  'Besiktas Cad. No:12, Istanbul',     'Istanbul', 'john_mgr'),
(2, 'Pasta Palace',     'Italian',  'Kadikoy Mah. No:45, Istanbul',      'Istanbul', 'john_mgr'),
(3, 'Sushi World',      'Japanese', 'Nisantasi Sok. No:8, Istanbul',     'Istanbul', 'sarah_mgr'),
(4, 'Burger Hub',       'American', 'Mecidiyekoy Blv. No:22, Istanbul',  'Istanbul', 'sarah_mgr'),
(5, 'Ankara Kebap Evi', 'Turkish',  'Kizilay Cad. No:3, Ankara',         'Ankara',   'mehmet_mgr');


-- ============================================================
-- 7. RESTAURANT_KEYWORD  (multi-valued – 1NF decomposition)
--    Used by the search query to rank restaurants by keyword match.
-- ============================================================
INSERT INTO Restaurant_Keyword (restaurant_id, keyword) VALUES
-- Bosphorus Grill
(1, 'turkish'), (1, 'grill'), (1, 'kebap'), (1, 'meze'),
-- Pasta Palace
(2, 'italian'), (2, 'pasta'), (2, 'pizza'), (2, 'mediterranean'),
-- Sushi World
(3, 'japanese'), (3, 'sushi'), (3, 'asian'), (3, 'seafood'),
-- Burger Hub
(4, 'american'), (4, 'burger'), (4, 'fast-food'), (4, 'sandwich'),
-- Ankara Kebap Evi
(5, 'turkish'), (5, 'kebap'), (5, 'ankara'), (5, 'traditional');


-- ============================================================
-- 8. MENU_CATEGORY  (5 categories, one per restaurant)
-- ============================================================
INSERT INTO Menu_Category (category_id, name, restaurant_id) VALUES
(1, 'Meze & Starters',  1),
(2, 'Pasta & Pizza',    2),
(3, 'Sushi Rolls',      3),
(4, 'Burgers & Sides',  4),
(5, 'Kebap & Pide',     5);


-- ============================================================
-- 9. MENU_ITEM  (15 items, 3 per restaurant)
--    restaurant_id is NOT stored here; it is derived via
--    Menu_Item → Menu_Category → Restaurant  (see §4.3 / 3NF).
-- ============================================================
INSERT INTO Menu_Item (item_id, name, description, price, image_url, category_id) VALUES
-- Bosphorus Grill  (category 1)
(1,  'Hummus',       'Creamy chickpea dip served with warm pita bread',           45.00, 'img/hummus.jpg',       1),
(2,  'Adana Kebap',  'Spiced minced lamb on skewer, served with rice and salad',  120.00,'img/adana.jpg',        1),
(3,  'Lamb Ribs',    'Slow-roasted lamb ribs with herbs and seasonal vegetables', 150.00,'img/lambribs.jpg',     1),
-- Pasta Palace  (category 2)
(4,  'Spaghetti Carbonara', 'Classic Roman pasta with egg, pecorino and pancetta',  95.00, 'img/carbonara.jpg',  2),
(5,  'Margherita Pizza',    'San Marzano tomato, fresh mozzarella, basil',          85.00, 'img/margherita.jpg', 2),
(6,  'Penne Arrabbiata',    'Penne in spicy tomato and garlic sauce',               80.00, 'img/arrabbiata.jpg', 2),
-- Sushi World  (category 3)
(7,  'California Roll',  '8-piece crab, avocado and cucumber inside-out roll',    110.00, 'img/california.jpg', 3),
(8,  'Salmon Nigiri',    '2-piece fresh Atlantic salmon over seasoned rice',       130.00, 'img/nigiri.jpg',     3),
(9,  'Dragon Roll',      '8-piece shrimp tempura topped with avocado',             140.00, 'img/dragon.jpg',     3),
-- Burger Hub  (category 4)
(10, 'Classic Burger',  'Beef patty, lettuce, tomato, pickles, house sauce',       75.00, 'img/classic.jpg',    4),
(11, 'BBQ Burger',      'Smoked beef patty, cheddar, crispy onion, BBQ sauce',     90.00, 'img/bbq.jpg',        4),
(12, 'Veggie Burger',   'Plant-based patty, avocado spread, mixed greens',         70.00, 'img/veggie.jpg',     4),
-- Ankara Kebap Evi  (category 5)
(13, 'Iskender Kebap',  'Thinly sliced lamb over bread, tomato sauce and butter', 135.00, 'img/iskender.jpg',   5),
(14, 'Lahmacun',        'Thin crispy flatbread topped with spiced minced meat',    45.00, 'img/lahmacun.jpg',   5),
(15, 'Kofte',           'Grilled minced lamb meatballs, served with bulgur pilaf', 90.00, 'img/kofte.jpg',      5);


-- ============================================================
-- 10. COUPON  (5 coupons, one per restaurant)
--     Validity: all active during the sample order period.
-- ============================================================
INSERT INTO Coupon (coupon_id, code, discount_type, discount_value,
                    valid_from, valid_until, is_active, restaurant_id) VALUES
(1, 'BOSPHORUS10', 'percentage', 10.00, '2026-01-01', '2026-12-31', TRUE, 1),
(2, 'PASTA15',     'percentage', 15.00, '2026-01-01', '2026-12-31', TRUE, 2),
(3, 'SUSHI20',     'fixed',      20.00, '2026-01-01', '2026-12-31', TRUE, 3),
(4, 'BURGER5',     'fixed',       5.00, '2026-01-01', '2026-12-31', TRUE, 4),
(5, 'KEBAP10',     'percentage', 10.00, '2026-01-01', '2026-12-31', TRUE, 5);


-- ============================================================
-- 11. ORDER  (10 orders, all Accepted so ratings can be left)
--     Dates set to May 2026 so they fall within the 1-month
--     statistics window (queries use DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)).
--
--  Discount calculations:
--    Order 2 : 180.00 × 15%  = 27.00  → total 153.00
--    Order 3 : 220.00 − 20   = 200.00 (fixed)
--    Order 5 : 300.00 × 10%  = 30.00  → total 270.00
--    Order 8 : 180.00 − 5    = 175.00 (fixed)
--    Order 10: 315.00 × 10%  = 31.50  → total 283.50
-- ============================================================
INSERT INTO `Order` (order_id, customer_id, restaurant_id, coupon_id,
                     status, created_at, total_amount, discount_applied) VALUES
(1,  'alice_cust', 1, NULL, 'Accepted', '2026-05-01 12:00:00', 210.00,   0.00),
(2,  'alice_cust', 2,    2, 'Accepted', '2026-05-01 13:00:00', 153.00,  27.00),
(3,  'bob_cust',   3,    3, 'Accepted', '2026-05-02 11:00:00', 200.00,  20.00),
(4,  'bob_cust',   4, NULL, 'Accepted', '2026-05-02 18:00:00', 165.00,   0.00),
(5,  'ceren_cust', 1,    1, 'Accepted', '2026-05-03 12:30:00', 270.00,  30.00),
(6,  'ceren_cust', 2, NULL, 'Accepted', '2026-05-03 19:00:00', 245.00,   0.00),
(7,  'dave_cust',  3, NULL, 'Accepted', '2026-05-04 13:00:00', 410.00,   0.00),
(8,  'dave_cust',  4,    4, 'Accepted', '2026-05-04 19:30:00', 175.00,   5.00),
(9,  'elif_cust',  5, NULL, 'Accepted', '2026-05-05 12:00:00', 225.00,   0.00),
(10, 'elif_cust',  5,    5, 'Accepted', '2026-05-06 12:00:00', 283.50,  31.50);


-- ============================================================
-- 12. ORDER_ITEM  (CONTAINS relationship – M:N with attributes)
--     unit_price is the snapshot price at order time.
--     Trigger trg_order_item_same_restaurant enforces that all
--     items in an order come from the same restaurant.
-- ============================================================
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
-- Order 1  alice → Bosphorus Grill
(1,  1, 2,  45.00),   -- Hummus ×2
(1,  2, 1, 120.00),   -- Adana Kebap ×1
-- Order 2  alice → Pasta Palace  (coupon PASTA15 applied)
(2,  4, 1,  95.00),   -- Spaghetti Carbonara ×1
(2,  5, 1,  85.00),   -- Margherita Pizza ×1
-- Order 3  bob → Sushi World  (coupon SUSHI20 applied)
(3,  7, 2, 110.00),   -- California Roll ×2
-- Order 4  bob → Burger Hub
(4, 10, 1,  75.00),   -- Classic Burger ×1
(4, 11, 1,  90.00),   -- BBQ Burger ×1
-- Order 5  ceren → Bosphorus Grill  (coupon BOSPHORUS10 applied)
(5,  3, 2, 150.00),   -- Lamb Ribs ×2
-- Order 6  ceren → Pasta Palace
(6,  5, 1,  85.00),   -- Margherita Pizza ×1
(6,  6, 2,  80.00),   -- Penne Arrabbiata ×2
-- Order 7  dave → Sushi World
(7,  8, 1, 130.00),   -- Salmon Nigiri ×1
(7,  9, 2, 140.00),   -- Dragon Roll ×2
-- Order 8  dave → Burger Hub  (coupon BURGER5 applied)
(8, 11, 2,  90.00),   -- BBQ Burger ×2
-- Order 9  elif → Ankara Kebap Evi
(9, 13, 1, 135.00),   -- Iskender Kebap ×1
(9, 14, 2,  45.00),   -- Lahmacun ×2
-- Order 10 elif → Ankara Kebap Evi  (coupon KEBAP10 applied)
(10, 15, 2,  90.00),  -- Kofte ×2
(10, 13, 1, 135.00);  -- Iskender Kebap ×1


-- ============================================================
-- 13. ORDER_STATUS_HISTORY  (weak entity HAS_STATUS)
--     Every order passes through all three stages.
--     Timestamps are spaced realistically.
--     The Accepted timestamp is used by the 24-hour rating
--     window check in the application.
-- ============================================================
INSERT INTO Order_Status_History (order_id, status, time_stamp) VALUES
-- Order 1
(1,  'Preparing', '2026-05-01 12:00:00'),
(1,  'Sent',      '2026-05-01 12:15:00'),
(1,  'Accepted',  '2026-05-01 12:45:00'),
-- Order 2
(2,  'Preparing', '2026-05-01 13:00:00'),
(2,  'Sent',      '2026-05-01 13:20:00'),
(2,  'Accepted',  '2026-05-01 14:00:00'),
-- Order 3
(3,  'Preparing', '2026-05-02 11:00:00'),
(3,  'Sent',      '2026-05-02 11:20:00'),
(3,  'Accepted',  '2026-05-02 12:00:00'),
-- Order 4
(4,  'Preparing', '2026-05-02 18:00:00'),
(4,  'Sent',      '2026-05-02 18:15:00'),
(4,  'Accepted',  '2026-05-02 18:50:00'),
-- Order 5
(5,  'Preparing', '2026-05-03 12:30:00'),
(5,  'Sent',      '2026-05-03 12:50:00'),
(5,  'Accepted',  '2026-05-03 13:20:00'),
-- Order 6
(6,  'Preparing', '2026-05-03 19:00:00'),
(6,  'Sent',      '2026-05-03 19:15:00'),
(6,  'Accepted',  '2026-05-03 19:45:00'),
-- Order 7
(7,  'Preparing', '2026-05-04 13:00:00'),
(7,  'Sent',      '2026-05-04 13:20:00'),
(7,  'Accepted',  '2026-05-04 14:00:00'),
-- Order 8
(8,  'Preparing', '2026-05-04 19:30:00'),
(8,  'Sent',      '2026-05-04 19:45:00'),
(8,  'Accepted',  '2026-05-04 20:15:00'),
-- Order 9
(9,  'Preparing', '2026-05-05 12:00:00'),
(9,  'Sent',      '2026-05-05 12:30:00'),
(9,  'Accepted',  '2026-05-05 13:00:00'),
-- Order 10
(10, 'Preparing', '2026-05-06 12:00:00'),
(10, 'Sent',      '2026-05-06 12:20:00'),
(10, 'Accepted',  '2026-05-06 13:00:00');


-- ============================================================
-- 14. RATING  (10 ratings, one per order)
--     restaurant_id is intentionally OMITTED (derived via
--     Rating → Order → Restaurant, preserving 3NF – §4.3).
--     Ratings are distributed across all 5 restaurants:
--       Bosphorus Grill   : orders 1, 5   → 2 ratings
--       Pasta Palace      : orders 2, 6   → 2 ratings
--       Sushi World       : orders 3, 7   → 2 ratings
--       Burger Hub        : orders 4, 8   → 2 ratings
--       Ankara Kebap Evi  : orders 9, 10  → 2 ratings
--     Total: 10 ratings.
--     NOTE: each restaurant only has 2 ratings here, so the
--     "New" label will appear in search results until the
--     application accumulates 10+ ratings per restaurant —
--     this correctly demonstrates the threshold rule.
-- ============================================================
INSERT INTO Rating (rating_id, score, comment, created_at, customer_id, order_id) VALUES
(1,  5, 'Excellent meze and perfectly cooked kebap!',       '2026-05-01 14:30:00', 'alice_cust', 1),
(2,  4, 'Great pasta, very generous portions.',              '2026-05-01 15:00:00', 'alice_cust', 2),
(3,  5, 'Best California Roll I have had in Istanbul.',      '2026-05-02 13:00:00', 'bob_cust',   3),
(4,  4, 'Tasty burgers, fries were crispy.',                 '2026-05-02 20:00:00', 'bob_cust',   4),
(5,  5, 'Lamb ribs were fall-off-the-bone perfect!',         '2026-05-03 14:30:00', 'ceren_cust', 5),
(6,  3, 'Pasta was good but took longer than expected.',     '2026-05-03 21:00:00', 'ceren_cust', 6),
(7,  5, 'Dragon roll was outstanding, very fresh fish.',     '2026-05-04 15:30:00', 'dave_cust',  7),
(8,  4, 'Solid BBQ burgers, sauce was delicious.',           '2026-05-04 21:30:00', 'dave_cust',  8),
(9,  5, 'Best Iskender in Ankara, exactly like home!',       '2026-05-05 14:00:00', 'elif_cust',  9),
(10, 4, 'Kofte was juicy and the bulgur pilaf was perfect.', '2026-05-06 14:00:00', 'elif_cust',  10);

-- ============================================================
-- END OF DML.sql
-- ============================================================
