drop table if exists user_schema.mailbox_user_settings;

create table user_schema.mailbox_user_settings
(
    user_id varchar(13) primary key,
    locale  varchar(8) not null
);
