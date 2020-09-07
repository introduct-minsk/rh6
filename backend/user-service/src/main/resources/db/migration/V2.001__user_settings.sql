create schema if not exists user_schema;

create table user_schema.mailbox_user_settings
(
    user_id varchar(13) primary key,
    locale  varchar(8) not null
);
