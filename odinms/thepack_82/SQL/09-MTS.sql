SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for mts_cart
-- ----------------------------
DROP TABLE IF EXISTS `mts_cart`;
CREATE TABLE `mts_cart` (
  `id` int(11) NOT NULL auto_increment,
  `cid` int(11) NOT NULL,
  `itemid` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for mts_items
-- ----------------------------
DROP TABLE IF EXISTS `mts_items`;
CREATE TABLE `mts_items` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `tab` int(11) NOT NULL default '0',
  `type` int(11) NOT NULL default '0',
  `itemid` int(10) unsigned NOT NULL default '0',
  `quantity` int(11) NOT NULL default '1',
  `seller` int(11) NOT NULL default '0',
  `price` int(11) NOT NULL default '0',
  `bid_incre` int(11) default '0',
  `buy_now` int(11) default '0',
  `position` int(11) default '0',
  `upgradeslots` int(11) default '0',
  `level` int(11) default '0',
  `str` int(11) default '0',
  `dex` int(11) default '0',
  `int` int(11) default '0',
  `luk` int(11) default '0',
  `hp` int(11) default '0',
  `mp` int(11) default '0',
  `watk` int(11) default '0',
  `matk` int(11) default '0',
  `wdef` int(11) default '0',
  `mdef` int(11) default '0',
  `acc` int(11) default '0',
  `avoid` int(11) default '0',
  `hands` int(11) default '0',
  `speed` int(11) default '0',
  `jump` int(11) default '0',
  `locked` int(11) default '0',
  `isequip` int(1) default '0',
  `owner` varchar(16) default '',
  `sellername` varchar(16) NOT NULL,
  `sell_ends` varchar(16) NOT NULL,
  `transfer` int(2) default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------  