insert into auth_schema.mailbox_client (client_id, secret)
values ('search-service-client',
        '$2y$12$ZJp0cDYfX5FZnQQlNujPBO2uSomJYzFAamVM7CfJJk8pMq3R13vt6'); -- 4e64bfbe-7b44-11ea-bc55-0242ac130003

insert into auth_schema.mailbox_client_role (client_id, role)
values ('mailbox-service-client', 'ROLE_SEARCH_SOURCE');