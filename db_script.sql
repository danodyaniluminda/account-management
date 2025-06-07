
create table public.accounts
(
    account_id          bigint primary key not null,
    account_number      varchar(20),
    account_user_id     bigint,
    account_user_type   varchar(20),
    currency_code      varchar(20),
    balance            numeric(19, 2),
    on_hold            numeric(19, 2),
    account_type        varchar(20),
    account_status      varchar(20),
    created_by         varchar(20),
    created_date       timestamp without time zone,
    last_modified_by   varchar(20),
    last_modified_date timestamp without time zone
);
grant delete, insert, references, select, trigger, truncate, update on public.accounts to bia_app with grant option;

create sequence public.accounts_account_id_seq;
grant select, update, usage on sequence public.accounts_account_id_seq to bia_app with grant option;

create table public.account_events
(
    account_event_id            bigint primary key not null,
    account_id                  bigint,
    account_transaction_id      bigint,
    amount                     numeric(19, 2),
    balance_before_transaction numeric(19, 2),
    balance_after_transaction  numeric(19, 2),
    event_type                 varchar(20),
    account_type                varchar(20),

    created_by                 varchar(20),
    created_date               timestamp without time zone,
    last_modified_by           varchar(20),
    last_modified_date         timestamp without time zone
--     foreign key (account_transaction_id) references public.account_transactions (account_transaction_id)
--         match simple on update no action on delete no action,
--     foreign key (account_id) references public.accounts (account_id)
--         match simple on update no action on delete no action
);
grant delete, insert, references, select, trigger, truncate, update on public.account_events to bia_app with grant option;

create sequence public.account_event_account_event_id_seq;
grant select, update, usage on sequence public.account_event_account_event_id_seq to bia_app;


create table public.account_transactions
(
    account_transaction_id      bigint primary key not null,
    user_type                  varchar(20),
    account_user_id             bigint,
    from_account_id             bigint,
    to_account_id               bigint,
    amount                     numeric(19, 2),
    fee                        numeric(19, 2),
    commission                 numeric(19, 2),
    currency_code              varchar(20),
    account_transaction_type    varchar(20),
    transaction_reference      varchar(50),
    remarks                    varchar(255),
    account_transaction_status  varchar(20),

    from_account_user_name      varchar(50),
    from_account_balance_before numeric(19, 2),
    from_account_mobile_number  varchar(20),
    from_account_type           varchar(20),
    from_account_event          varchar(20),

    to_account_user_name        varchar(50),
    to_account_balance_before   numeric(19, 2),
    to_account_mobile_number    varchar(20),
    to_account_type             varchar(20),
    to_account_event            varchar(20),

    is_processed               integer            not null,

    created_by                 varchar(20),
    created_date               timestamp without time zone,
    last_modified_by           varchar(20),
    last_modified_date         timestamp without time zone,
    foreign key (from_account_id) references public.accounts (account_id)
        match simple on update no action on delete no action,
    foreign key (to_account_id) references public.accounts (account_id)
        match simple on update no action on delete no action
);
create sequence public.account_transaction_account_transaction_id_seq;
grant delete, insert, references, select, trigger, truncate, update on public.account_transactions to bia_app with grant option;
grant select, update, usage on sequence public.account_transaction_account_transaction_id_seq to bia_app;