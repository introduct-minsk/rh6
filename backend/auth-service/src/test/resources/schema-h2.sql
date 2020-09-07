drop table if exists auth_schema.mailbox_client_role;
drop table if exists auth_schema.mailbox_client;

create table auth_schema.mailbox_client
(
    client_id varchar(32) PRIMARY KEY,
    secret    varchar(60) not null
);

create table auth_schema.mailbox_client_role
(
    id        integer auto_increment PRIMARY KEY,
    client_id varchar(32) REFERENCES auth_schema.mailbox_client ON UPDATE CASCADE ON DELETE CASCADE,
    role      varchar(32) not null
);
