ALTER TABLE characters ADD petid int(10) DEFAULT 0;

DROP TABLE IF EXISTS `petsaves`;
CREATE TABLE `petsaves` (
  `id` int(11) NOT NULL auto_increment,
  `characterid` int(11) NOT NULL,
  `petid1` int(10) NOT NULL default '-1',
  `petid2` int(10) NOT NULL default '-1',
  `petid3` int(10) NOT NULL default '-1',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=latin1;
