create database if not exists online_web_chat charset utf8;

use online_web_chat;

drop table if exists user;
create table user (
    user_id int primary key auto_increment,
    user_name varchar(18) unique,
    password varchar(18)
);

insert into user values (null,"zhangsan","123456");
insert into user values (null,"lisi","123456");