create
database cafe;

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
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;