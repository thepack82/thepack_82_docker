DROP TABLE IF EXISTS `trocklocations`;
CREATE TABLE `trocklocations` (  
	`trockid` int(11) NOT NULL auto_increment,
	`characterid` int(11) NOT NULL,
	`mapid` int(11) NOT NULL,
	PRIMARY KEY  (`trockid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;