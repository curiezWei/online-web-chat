create database if not exists online_web_chat charset utf8mb4;

use online_web_chat;

drop table if exists user;
create table user (
                      user_id int primary key auto_increment,
                      user_name varchar(18) unique,
                      password varchar(18)
);

drop table if exists friend;
create table friend (
                        user_id int,
                        friend_id int
);

drop table if exists message_session;
create table message_session (
                                 session_id int primary key auto_increment,
                                 last_time DATETIME
);

drop table if exists message_session_user;
create table message_session_user (
                                      session_id int,
                                      user_id int
);

drop table if exists message;
create table message(
                        message_id int primary key auto_increment,
                        from_id int,
                        session_id int,
                        content varchar(2048),
                        post_time datetime
);

drop table if exists friend_request;
create table friend_request (
    request_id   int primary key auto_increment,
    from_id      int not null,
    to_id        int not null,
    request_time datetime not null default current_timestamp,
    unique key uk_from_to (from_id, to_id)
);





