create schema if not exists auth_schema;

create table auth_schema.mailbox_client
(
    client_id varchar(32) PRIMARY KEY,
    secret    varchar(60) not null
);

create table auth_schema.mailbox_client_role
(
    id        SERIAL PRIMARY KEY,
    client_id varchar(32) REFERENCES auth_schema.mailbox_client ON UPDATE CASCADE ON DELETE CASCADE,
    role      varchar(32) not null
);


-- secret = https://bcrypt-generator.com/ rounds 12

insert into auth_schema.mailbox_client (client_id, secret)
values ('user-service-client',
        '$2y$12$oPFU.wHvCJJUUStkOkzHxeSSt668bDOWRllZc9RgFYIKCYv1KYC8K'); -- 1e279f48-7995-11ea-bc55-0242ac130003


insert into auth_schema.mailbox_client (client_id, secret)
values ('mailbox-service-client',
        '$2y$12$NzO6KoHkE7Qyo2.s1b9kH.5Qg/tvl.lGSRllJx1r6dyymyR0F8nNG'); -- 0db0b8ac-7995-11ea-bc55-0242ac130003

insert into auth_schema.mailbox_client_role (client_id, role)
values ('mailbox-service-client', 'GET_USER_DETAILS');


insert into auth_schema.mailbox_client (client_id, secret)
values ('admin-client',
        '$2y$12$PAi04Tr9BMDwUwKwsYHeSOA55hEZU41T5gzn4wLrKItnvsBmL8aKK'); -- mailboxpassword

insert into auth_schema.mailbox_client_role (client_id, role)
values ('admin-client', 'DB_READ_WRITE');