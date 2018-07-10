delete from shops where npcid = 9030000;
INSERT INTO shops (shopid, npcid) VALUES (9595026, 9030000);
INSERT INTO shopitems (shopid, itemid, price, position) VALUES
(9595026, 5072000, 1000000, 1),
(9595026, 5390000, 2500000, 2),
(9595026, 5390002, 2500000, 3),
(9595026, 5390001, 2500000, 4);