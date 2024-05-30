create table if not exists fineAnts_release.exchange_rate
(
    base        bit          not null,
    rate        double       not null,
    create_at   datetime(6)  null,
    modified_at datetime(6)  null,
    code        varchar(255) not null
        primary key
);

create table if not exists fineAnts_release.member
(
    create_at   datetime(6)  null,
    id          bigint auto_increment
        primary key,
    modified_at datetime(6)  null,
    email       varchar(255) null,
    nickname    varchar(255) not null,
    password    varchar(255) null,
    profile_url varchar(255) null,
    provider    varchar(255) null,
    constraint UK_hh9kg6jti4n1eoiertn2k6qsc
        unique (nickname)
);

create table if not exists fineAnts_release.fcm_token
(
    create_at              datetime(6)  null,
    id                     bigint auto_increment
        primary key,
    latest_activation_time datetime(6)  null,
    member_id              bigint       null,
    modified_at            datetime(6)  null,
    token                  varchar(255) null,
    constraint token_member_id_unique
        unique (token, member_id),
    constraint FKf1rbjf8lle4r2in6ovkcgl0w8
        foreign key (member_id) references fineAnts_release.member (id)
);

create table if not exists fineAnts_release.notification
(
    is_read                      bit                                                                        null,
    target_price                 decimal(38, 2)                                                             null,
    create_at                    datetime(6)                                                                null,
    id                           bigint auto_increment
        primary key,
    member_id                    bigint                                                                     null,
    modified_at                  datetime(6)                                                                null,
    target_price_notification_id bigint                                                                     null,
    dtype                        varchar(31)                                                                not null,
    link                         varchar(255)                                                               null,
    name                         varchar(255)                                                               null,
    reference_id                 varchar(255)                                                               null,
    stock_name                   varchar(255)                                                               null,
    title                        varchar(255)                                                               null,
    type                         enum ('PORTFOLIO_MAX_LOSS', 'PORTFOLIO_TARGET_GAIN', 'STOCK_TARGET_PRICE') null,
    constraint FK1xep8o2ge7if6diclyyx53v4q
        foreign key (member_id) references fineAnts_release.member (id)
);

create table if not exists fineAnts_release.notification_preference
(
    browser_notify      bit         not null,
    max_loss_notify     bit         not null,
    target_gain_notify  bit         not null,
    target_price_notify bit         not null,
    create_at           datetime(6) null,
    id                  bigint auto_increment
        primary key,
    member_id           bigint      null,
    modified_at         datetime(6) null,
    constraint UK_sug53kin1ir6qq9790uudjs03
        unique (member_id),
    constraint FKpn714rk5pvp6wjlwd77sngm08
        foreign key (member_id) references fineAnts_release.member (id)
);

create table if not exists fineAnts_release.portfolio
(
    budget                 decimal(19)  not null,
    maximum_loss           decimal(19)  not null,
    maximum_loss_is_active bit          null,
    target_gain            decimal(19)  not null,
    target_gain_is_active  bit          null,
    create_at              datetime(6)  null,
    id                     bigint auto_increment
        primary key,
    member_id              bigint       null,
    modified_at            datetime(6)  null,
    name                   varchar(255) null,
    securities_firm        varchar(255) null,
    constraint UKniiw35vyoiwnxtfhs8im0v2a9
        unique (name, member_id),
    constraint FKhkjiiwx38ctlby4yt4y82tua7
        foreign key (member_id) references fineAnts_release.member (id)
);

create table if not exists fineAnts_release.portfolio_gain_history
(
    cash              decimal(19) not null,
    current_valuation decimal(19) not null,
    daily_gain        decimal(19) not null,
    total_gain        decimal(19) not null,
    create_at         datetime(6) null,
    id                bigint auto_increment
        primary key,
    modified_at       datetime(6) null,
    portfolio_id      bigint      null,
    constraint FKdea3l34fqo27x2xrfdt75otys
        foreign key (portfolio_id) references fineAnts_release.portfolio (id)
);

create table if not exists fineAnts_release.role
(
    role_id          bigint auto_increment
        primary key,
    role_description varchar(255) null,
    role_name        varchar(255) not null
);

create table if not exists fineAnts_release.member_role
(
    member_id      bigint null,
    member_role_id bigint auto_increment
        primary key,
    role_role_id   bigint null,
    constraint FK34g7epqlcxqloewku3aoqhhmg
        foreign key (member_id) references fineAnts_release.member (id),
    constraint FK8ro2tn5n8wkfy1nyjdqxocwpo
        foreign key (role_role_id) references fineAnts_release.role (role_id)
);

create table if not exists fineAnts_release.stock
(
    create_at        datetime(6)  null,
    modified_at      datetime(6)  null,
    company_name     varchar(255) null,
    company_name_eng varchar(255) null,
    market           varchar(255) null,
    sector           varchar(255) null,
    stock_code       varchar(255) null,
    ticker_symbol    varchar(255) not null
        primary key
);

create table if not exists fineAnts_release.portfolio_holding
(
    create_at     datetime(6)  null,
    id            bigint auto_increment
        primary key,
    modified_at   datetime(6)  null,
    portfolio_id  bigint       null,
    ticker_symbol varchar(255) null,
    constraint FK3ixur6cv3eqixv9kc01tihm4i
        foreign key (ticker_symbol) references fineAnts_release.stock (ticker_symbol),
    constraint FK99yckortu2r0bxjltxfvabcbo
        foreign key (portfolio_id) references fineAnts_release.portfolio (id)
);

create table if not exists fineAnts_release.purchase_history
(
    num_shares               decimal(38)  null,
    purchase_price_per_share decimal(19)  not null,
    create_at                datetime(6)  null,
    id                       bigint auto_increment
        primary key,
    modified_at              datetime(6)  null,
    portfolio_holding_id     bigint       null,
    purchase_date            datetime(6)  null,
    memo                     varchar(255) null,
    constraint FKtmqhjq2ng9k66gw9s3qbnx0op
        foreign key (portfolio_holding_id) references fineAnts_release.portfolio_holding (id)
);

create table if not exists fineAnts_release.stock_dividend
(
    dividend         decimal(19)  not null,
    ex_dividend_date date         not null,
    payment_date     date         null,
    record_date      date         not null,
    create_at        datetime(6)  null,
    id               bigint auto_increment
        primary key,
    modified_at      datetime(6)  null,
    ticker_symbol    varchar(255) null,
    constraint UKs7kxldvrap8rcpi7emyaq28y7
        unique (ticker_symbol, record_date),
    constraint FK6tww3epiobccxnj5rgjdu4ab0
        foreign key (ticker_symbol) references fineAnts_release.stock (ticker_symbol)
);

create table if not exists fineAnts_release.stock_target_price
(
    is_active     bit          null,
    create_at     datetime(6)  null,
    id            bigint auto_increment
        primary key,
    member_id     bigint       null,
    modified_at   datetime(6)  null,
    ticker_symbol varchar(255) null,
    constraint UKhwlfu5x3iqpei19soxhmjcfs3
        unique (member_id, ticker_symbol),
    constraint FK2r0grp1n205hnw3ysp179f5l3
        foreign key (member_id) references fineAnts_release.member (id),
    constraint FKcup8hchscft8jniri3wkk72kx
        foreign key (ticker_symbol) references fineAnts_release.stock (ticker_symbol)
);

create table if not exists fineAnts_release.target_price_notification
(
    target_price          decimal(19) not null,
    create_at             datetime(6) null,
    id                    bigint auto_increment
        primary key,
    modified_at           datetime(6) null,
    stock_target_price_id bigint      null,
    constraint FKnds69ucw684g4c2a09g0fa5bq
        foreign key (stock_target_price_id) references fineAnts_release.stock_target_price (id)
);

create table if not exists fineAnts_release.watch_list
(
    create_at   datetime(6)  null,
    id          bigint auto_increment
        primary key,
    member_id   bigint       null,
    modified_at datetime(6)  null,
    name        varchar(255) null,
    constraint FK913gb7s3b8il5emg0489jhibc
        foreign key (member_id) references fineAnts_release.member (id)
);

create table if not exists fineAnts_release.watch_stock
(
    create_at     datetime(6)  null,
    id            bigint auto_increment
        primary key,
    modified_at   datetime(6)  null,
    watch_list_id bigint       null,
    ticker_symbol varchar(255) null,
    constraint FK3eu9b3aw8tnk1lyao7vielolj
        foreign key (watch_list_id) references fineAnts_release.watch_list (id),
    constraint FKk1yabpeilnrrys4og9yid2cw1
        foreign key (ticker_symbol) references fineAnts_release.stock (ticker_symbol)
);

