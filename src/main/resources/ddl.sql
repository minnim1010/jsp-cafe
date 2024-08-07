create
database cafe;

use
cafe;

create table `USER`
(
    `user_id`  varchar(255) NOT NULL PRIMARY KEY,
    `password` varchar(255) NOT NULL,
    `nickname` varchar(255) NOT NULL,
    `email`    varchar(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table QUESTION
(
    `question_id` BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `writer`      varchar(255) NOT NULL,
    `title`       varchar(255) NOT NULL,
    `content`     text         NOT NULL,
    `is_deleted` boolean  NOT NULL DEFAULT FALSE,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY          `idx_question_is_deleted` (`is_deleted`,`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table REPLY
(
    `reply_id`    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `writer`      varchar(255) NOT NULL,
    `content`     text         NOT NULL,
    `is_deleted`  boolean      NOT NULL DEFAULT FALSE,
    `question_id` BIGINT       NOT NULL,
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (question_id) REFERENCES QUESTION (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
