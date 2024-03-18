create table if not exists fineAnts.hibernate_sequence
(
    next_val bigint null
);

create table if not exists fineAnts.member
(
    id          bigint auto_increment
        primary key,
    create_at   datetime     null,
    modified_at datetime     null,
    email       varchar(255) null,
    nickname    varchar(255) null,
    password    varchar(255) null,
    profile_url varchar(255) null,
    provider    varchar(255) null
);

create table if not exists fineAnts.portfolio
(
    id                     bigint auto_increment
        primary key,
    create_at              datetime(6)  null,
    modified_at            datetime(6)  null,
    budget                 bigint       null,
    maximum_loss           bigint       null,
    maximum_loss_is_active bit          null,
    name                   varchar(255) null,
    securities_firm        varchar(255) null,
    target_gain            bigint       null,
    target_gain_is_active  bit          null,
    member_id              bigint       null,
    constraint FKhkjiiwx38ctlby4yt4y82tua7
        foreign key (member_id) references fineAnts.member (id)
);

create table if not exists fineAnts.portfolio_gain_history
(
    id                bigint auto_increment
        primary key,
    create_at         datetime null,
    modified_at       datetime null,
    cash bigint null,
    current_valuation bigint   null,
    daily_gain        bigint   null,
    total_gain        bigint   null,
    portfolio_id      bigint   null,
    constraint FKdea3l34fqo27x2xrfdt75otys
        foreign key (portfolio_id) references fineAnts.portfolio (id)
);

create table if not exists fineAnts.stock
(
    ticker_symbol    varchar(255) not null
        primary key,
    create_at        datetime     null,
    modified_at      datetime     null,
    company_name     varchar(255) null,
    company_name_eng varchar(255) null,
    market           varchar(255) null,
    sector           varchar(255) null,
    stock_code       varchar(255) null
);

create table if not exists fineAnts.portfolio_holding
(
    id            bigint auto_increment
        primary key,
    create_at     datetime     null,
    modified_at   datetime     null,
    portfolio_id  bigint       null,
    ticker_symbol varchar(255) null,
    constraint FK3ixur6cv3eqixv9kc01tihm4i
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol),
    constraint FK99yckortu2r0bxjltxfvabcbo
        foreign key (portfolio_id) references fineAnts.portfolio (id)
);

create table if not exists fineAnts.purchase_history
(
    id                       bigint auto_increment
        primary key,
    create_at                datetime     null,
    modified_at              datetime     null,
    memo                     varchar(255) null,
    num_shares               bigint       null,
    purchase_date            datetime     null,
    purchase_price_per_share double       null,
    portfolio_holding_id     bigint       null,
    constraint FKtmqhjq2ng9k66gw9s3qbnx0op
        foreign key (portfolio_holding_id) references fineAnts.portfolio_holding (id)
);

create table if not exists fineAnts.stock_dividend
(
    id               bigint auto_increment
        primary key,
    create_at        datetime     null,
    modified_at      datetime     null,
    dividend         bigint       null,
    ex_dividend_date date         not null,
    payment_date     date         null,
    record_date      date         not null,
    ticker_symbol    varchar(255) null,
    constraint UK9arb4d9tfcndvi89oppsv3wsa
        unique (ticker_symbol, record_date),
    constraint FK6tww3epiobccxnj5rgjdu4ab0
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol)
);

create table if not exists fineAnts.watch_list
(
    id          bigint       not null
        primary key,
    create_at   datetime     null,
    modified_at datetime     null,
    name        varchar(255) null,
    member_id   bigint       null,
    constraint FK913gb7s3b8il5emg0489jhibc
        foreign key (member_id) references fineAnts.member (id)
);

create table if not exists fineAnts.watch_stock
(
    id            bigint       not null
        primary key,
    create_at     datetime     null,
    modified_at   datetime     null,
    ticker_symbol varchar(255) null,
    watch_list_id bigint       null,
    constraint FK3eu9b3aw8tnk1lyao7vielolj
        foreign key (watch_list_id) references fineAnts.watch_list (id),
    constraint FKk1yabpeilnrrys4og9yid2cw1
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol)
);


create table if not exists fineAnts.fcm_token
(
    id bigint auto_increment
        primary key,
    create_at              datetime(6)  null,
    modified_at            datetime(6)  null,
    latest_activation_time datetime(6)  null,
    token                  varchar(255) null,
    member_id              bigint       null,
    constraint token_member_id_unique
        unique (token, member_id),
    constraint FKf1rbjf8lle4r2in6ovkcgl0w8
        foreign key (member_id) references fineAnts.member (id)
);

create table if not exists fineAnts.notification_preference
(
    id                 bigint NOT NULL primary key,
    create_at          datetime(6) DEFAULT NULL,
    modified_at        datetime(6) DEFAULT NULL,
    browser_notify     bit(1) NOT NULL,
    max_loss_notify    bit(1) NOT NULL,
    target_gain_notify bit(1) NOT NULL,
    target_price_notify bit(1) NOT NULL,
    member_id          bigint      DEFAULT NULL,
    CONSTRAINT FKpn714rk5pvp6wjlwd77sngm08 FOREIGN KEY (member_id) REFERENCES fineAnts.member (id)
);



create table if not exists fineAnts.stock_target_price
(
    id            bigint auto_increment
        primary key,
    create_at     datetime(6)  null,
    modified_at   datetime(6)  null,
    is_active bit null,
    member_id     bigint       null,
    ticker_symbol varchar(255) null,
    constraint UKhwlfu5x3iqpei19soxhmjcfs3
        unique (member_id, ticker_symbol),
    constraint FK2r0grp1n205hnw3ysp179f5l3
        foreign key (member_id) references fineAnts.member (id),
    constraint FKcup8hchscft8jniri3wkk72kx
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol)
);

create table if not exists fineAnts.target_price_notification
(
    id                    bigint auto_increment
        primary key,
    create_at             datetime(6) null,
    modified_at           datetime(6) null,
    target_price          bigint      null,
    stock_target_price_id bigint      null,
    constraint FKnds69ucw684g4c2a09g0fa5bq
        foreign key (stock_target_price_id) references fineAnts.stock_target_price (id)
);

create table fineAnts.notification
(
    dtype                        varchar(31)  not null,
    id                           bigint auto_increment
        primary key,
    create_at                    datetime(6)  null,
    modified_at                  datetime(6)  null,
    is_read                      bit          null,
    link                         varchar(255) null,
    reference_id                 varchar(255) null,
    title                        varchar(255) null,
    type                         varchar(255) null,
    name                         varchar(255) null,
    stock_name                   varchar(255) null,
    target_price                 bigint       null,
    target_price_notification_id bigint       null,
    member_id                    bigint       null,
    constraint FK1xep8o2ge7if6diclyyx53v4q
        foreign key (member_id) references fineAnts.member (id)
);

create table if not exists fineAnts.notification_preference
(
    id                  bigint auto_increment
        primary key,
    create_at           datetime(6) null,
    modified_at         datetime(6) null,
    browser_notify      bit         not null,
    max_loss_notify     bit         not null,
    target_gain_notify  bit         not null,
    target_price_notify bit         not null,
    member_id           bigint      null,
    constraint FKpn714rk5pvp6wjlwd77sngm08
        foreign key (member_id) references fineAnts.member (id)
);

