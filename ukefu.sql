/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50554
Source Host           : localhost:3306
Source Database       : ukefu

Target Server Type    : MYSQL
Target Server Version : 50554
File Encoding         : 65001

Date: 2017-03-15 22:05:04
*/

CREATE SCHEMA `ukefu` DEFAULT CHARACTER SET utf8 ;


SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `uk_fans`
-- ----------------------------
DROP TABLE IF EXISTS `uk_fans`;
CREATE TABLE `uk_fans` (
  `id` varchar(32) NOT NULL,
  `creater` varchar(32) DEFAULT NULL,
  `createtime` date DEFAULT NULL,
  `updatetime` date DEFAULT NULL,
  `user` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of uk_fans
-- ----------------------------

-- ----------------------------
-- Table structure for `uk_user`
-- ----------------------------
DROP TABLE IF EXISTS `uk_user`;
CREATE TABLE `uk_user` (
  `ID` varchar(32) NOT NULL,
  `LANGUAGE` varchar(255) DEFAULT NULL,
  `USERNAME` varchar(255) DEFAULT NULL,
  `PASSWORD` varchar(255) DEFAULT NULL,
  `SECURECONF` varchar(255) DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `FIRSTNAME` varchar(255) DEFAULT NULL,
  `MIDNAME` varchar(255) DEFAULT NULL,
  `LASTNAME` varchar(255) DEFAULT NULL,
  `JOBTITLE` varchar(255) DEFAULT NULL,
  `DEPARTMENT` varchar(255) DEFAULT NULL,
  `GENDER` varchar(255) DEFAULT NULL,
  `BIRTHDAY` varchar(255) DEFAULT NULL,
  `NICKNAME` varchar(255) DEFAULT NULL,
  `USERTYPE` varchar(255) DEFAULT NULL,
  `RULENAME` varchar(255) DEFAULT NULL,
  `SEARCHPROJECTID` varchar(255) DEFAULT NULL,
  `ORGI` varchar(32) DEFAULT NULL,
  `CREATER` varchar(32) DEFAULT NULL,
  `CREATETIME` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `MEMO` varchar(255) DEFAULT NULL,
  `UPDATETIME` timestamp NULL DEFAULT NULL,
  `ORGAN` varchar(32) DEFAULT NULL,
  `MOBILE` varchar(32) DEFAULT NULL,
  `passupdatetime` timestamp NULL DEFAULT NULL,
  `sign` text,
  `del` tinyint(4) DEFAULT '0',
  `uname` varchar(100) DEFAULT NULL,
  `musteditpassword` tinyint(4) DEFAULT NULL,
  `AGENT` tinyint(4) DEFAULT NULL,
  `SKILL` varchar(32) DEFAULT NULL,
  `province` varchar(50) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `fans` int(11) DEFAULT NULL,
  `follows` int(11) DEFAULT NULL,
  `integral` int(11) DEFAULT NULL,
  `lastlogintime` datetime DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  `deactivetime` datetime DEFAULT NULL,
  `title` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of uk_user
-- ----------------------------
INSERT INTO `uk_user` VALUES ('297e8c7b455798280145579c73e501c1', null, 'admin', '14e1b600b1fd579f47433b88e8d85291', '5', 'admin@ukewo.com', null, null, null, null, null, '0', null, null, '0', null, null, 'ukewo', null, '2017-02-14 11:19:46', '北京', '2017-03-12 18:06:29', '4028838b5acb901f015acba98b70000b', '18510129577', null, null, '0', '系统管理员', '0', '1', null, '北京', '北京', '2', '1', '0', '2017-03-15 20:33:18', null, null, null);

-- ----------------------------
-- Table structure for `uk_userevent`
-- ----------------------------
DROP TABLE IF EXISTS `uk_userevent`;
CREATE TABLE `uk_userevent` (
  `id` varchar(32) NOT NULL DEFAULT '',
  `username` varchar(32) DEFAULT NULL,
  `creater` varchar(32) DEFAULT NULL,
  `orgi` varchar(50) DEFAULT NULL,
  `maintype` varchar(32) DEFAULT NULL,
  `subtype` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `admin` tinyint(32) DEFAULT NULL,
  `access` tinyint(32) DEFAULT NULL,
  `ip` varchar(20) DEFAULT NULL,
  `hostname` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `region` varchar(50) DEFAULT NULL,
  `city` varchar(32) DEFAULT NULL,
  `isp` varchar(32) DEFAULT NULL,
  `province` varchar(32) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `sessionid` varchar(32) DEFAULT NULL,
  `param` text,
  `times` int(11) DEFAULT NULL,
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `ostype` varchar(50) DEFAULT NULL,
  `browser` varchar(50) DEFAULT NULL,
  `mobile` varchar(10) DEFAULT NULL,
  `model` varchar(10) DEFAULT NULL,
  `appid` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of uk_userevent
-- ----------------------------
