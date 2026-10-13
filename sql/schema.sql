-- KOB 建表脚本（MySQL 8）
-- 用法：mysql -u root -p < sql/schema.sql

CREATE DATABASE IF NOT EXISTS kob DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE kob;

CREATE TABLE IF NOT EXISTS `user` (
    `id`       INT           NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(100)  NOT NULL,
    `password` VARCHAR(100)  NOT NULL COMMENT 'BCrypt 密文',
    `photo`    VARCHAR(1000) DEFAULT NULL,
    `rating`   INT           NOT NULL DEFAULT 1500,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `bot` (
    `id`          INT            NOT NULL AUTO_INCREMENT,
    `user_id`     INT            NOT NULL,
    `title`       VARCHAR(100)   NOT NULL,
    `description` VARCHAR(300)   DEFAULT NULL,
    `content`     VARCHAR(10000) NOT NULL COMMENT 'Bot 的 Java 源码',
    `createtime`  DATETIME       DEFAULT NULL,
    `modifytime`  DATETIME       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_bot_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `record` (
    `id`         INT           NOT NULL AUTO_INCREMENT,
    `a_id`       INT           NOT NULL,
    `a_sx`       INT           NOT NULL,
    `a_sy`       INT           NOT NULL,
    `b_id`       INT           NOT NULL,
    `b_sx`       INT           NOT NULL,
    `b_sy`       INT           NOT NULL,
    `a_steps`    VARCHAR(1000) DEFAULT NULL,
    `b_steps`    VARCHAR(1000) DEFAULT NULL,
    `map`        VARCHAR(1000) DEFAULT NULL,
    `loser`      VARCHAR(10)   DEFAULT NULL COMMENT 'A / B / all',
    `createtime` DATETIME      DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
