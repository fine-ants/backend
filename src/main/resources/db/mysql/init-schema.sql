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
    id                    bigint auto_increment
        primary key,
    create_at             datetime     null,
    modified_at           datetime     null,
    budget                bigint       null,
    maximum_is_active     bit          null,
    maximum_loss          bigint       null,
    name                  varchar(255) null,
    securities_firm       varchar(255) null,
    target_gain           bigint       null,
    target_gain_is_active bit          null,
    member_id             bigint       null,
    constraint FK_PORTFOLIO_ON_MEMBER
        foreign key (member_id) references fineAnts.member (id)
);

create table if not exists fineAnts.portfolio_gain_history
(
    id                bigint auto_increment
        primary key,
    create_at         datetime null,
    modified_at       datetime null,
    current_valuation bigint   null,
    daily_gain        bigint   null,
    total_gain        bigint   null,
    portfolio_id      bigint   null,
    constraint FK_PORTFOLIOGAINHISTORY_PORTFOLIO
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
    constraint FK_PORTFOLIOHOLDING_STOCK
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol),
    constraint FK_PORTFOLIOHOLDING_PORTFOLIO
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
    constraint FK_PURCHASEHISTORY_ON_PORTFOLIOHOLDING
        foreign key (portfolio_holding_id) references fineAnts.portfolio_holding (id)
);

create table if not exists fineAnts.stock_dividend
(
    id               BIGINT AUTO_INCREMENT NOT NULL primary key,
    create_at        datetime              NULL,
    modified_at      datetime              NULL,
    ex_dividend_date date                  NOT NULL,
    record_date      date                  NOT NULL,
    payment_date     date                  NULL,
    dividend         BIGINT                NULL,
    ticker_symbol    VARCHAR(255)          NULL,
    constraint UNIQUE_STOCK_DIVIDEND
        unique (ticker_symbol, record_date),
    constraint FK_STOCKDIVIDEND_ON_STOCK
        foreign key (ticker_symbol) references fineAnts.stock (ticker_symbol)
);
