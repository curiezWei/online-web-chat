create database if not exists online_web_chat charset utf8mb4;

use online_web_chat;


drop table user;
drop table if exists user;
create table user (
                      user_id int primary key auto_increment,
                      user_name varchar(18) unique,
                      password varchar(18)
);

insert into user values (null,'zhangsan','123456');
insert into user values (null,'lisi','123456');
insert into user values (null,'wangwu','123456');
insert into user values (null,'zhaoliu','123456');

insert into user values (null,'🧑‍💻curiez我','114514');

show tables ;

desc user;

drop table if exists friend;
create table friend (
                        user_id int,
                        friend_id int
);

insert into friend values(6,1);
insert into friend values(6,2);
insert into friend values(6,3);
insert into friend values(1,2);
insert into friend values(3,2);
insert into friend values(4,2);
insert into friend values(5,3);







drop table if exists message_session;
create table message_session (
                                 session_id int primary key auto_increment,
                                 last_time DATETIME
);

insert into message_session values(1,'2006-03-27 00:00:00');
insert into message_session values(2,'2007-03-27 00:00:00');

drop table if exists message_session_user;
create table message_session_user (
                                      session_id int,
                                      user_id int
);

insert into message_session_user values (1,6),(1,1);
insert into message_session_user values(2,1),(2,2);

drop table if exists message;
create table message(
                        message_id int primary key auto_increment,
                        from_id int,
                        session_id int,
                        content varchar(2048),
                        post_time datetime
);

insert into message values (1,6,1,'吃什么','2006-03-27 18:00:00');
insert into message values (2,1,1,'不知道啊','2006-03-27 18:01:00');
insert into message values (3,6,1,'吃螺蛳粉怎么样','2006-03-27 18:03:00');
insert into message values (4,1,1,'彳亍','2006-03-27 18:00:00');
insert into message values (5,1,1,'不对啊，今天吃过螺蛳粉了，腻了','2006-03-27 18:06:00');
insert into message values (6,6,1,'。。。','2006-03-27 18:07:00');
insert into message values (7,6,1,'那你说吃什么','2006-03-27 18:08:00');
insert into message values (8,1,1,'吃泡面得了','2006-03-27 18:09:00');

drop table if exists friend_request;
create table friend_request (
    request_id   int primary key auto_increment,
    from_id      int not null,
    to_id        int not null,
    request_time datetime not null default current_timestamp,
    unique key uk_from_to (from_id, to_id)
);





