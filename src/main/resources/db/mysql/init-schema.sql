CREATE TABLE member
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    create_at   datetime NULL,
    modified_at datetime NULL,
    email       VARCHAR(255) NULL,
    nickname    VARCHAR(255) NULL,
    provider    VARCHAR(255) NULL,
    password    VARCHAR(255) NULL,
    profile_url VARCHAR(255) NULL,
    CONSTRAINT pk_member PRIMARY KEY (id)
);


CREATE TABLE portfolio
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    name                  VARCHAR(255) NULL,
    securities_firm       VARCHAR(255) NULL,
    budget                BIGINT NULL,
    target_gain           BIGINT NULL,
    maximum_loss          BIGINT NULL,
    target_gain_is_active BIT(1) NULL,
    maximum_is_active     BIT(1) NULL,
    member_id             BIGINT NULL,
    CONSTRAINT pk_portfolio PRIMARY KEY (id)
);

CREATE TABLE portfolio_gain_history
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    create_at         datetime NULL,
    modified_at       datetime NULL,
    total_gain        BIGINT NULL,
    daily_gain        BIGINT NULL,
    current_valuation BIGINT NULL,
    portfolio_id      BIGINT NULL,
    CONSTRAINT pk_portfoliogainhistory PRIMARY KEY (id)
);

CREATE TABLE portfolio_holding
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    create_at     datetime NULL,
    modified_at   datetime NULL,
    portfolio_id  BIGINT NULL,
    ticker_symbol VARCHAR(255) NULL,
    CONSTRAINT pk_portfolioholding PRIMARY KEY (id)
);

CREATE TABLE purchase_history
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    create_at            datetime NULL,
    modified_at          datetime NULL,
    purchase_date        datetime NULL,
    purchase_price_per_share DOUBLE NULL,
    num_shares           BIGINT NULL,
    memo                 VARCHAR(255) NULL,
    portfolio_holding_id BIGINT NULL,
    CONSTRAINT pk_purchasehistory PRIMARY KEY (id)
);

CREATE TABLE stock
(
    ticker_symbol    VARCHAR(255) NOT NULL,
    create_at        datetime NULL,
    modified_at      datetime NULL,
    company_name     VARCHAR(255) NULL,
    company_name_eng VARCHAR(255) NULL,
    stock_code       VARCHAR(255) NULL,
    market           VARCHAR(255) NULL,
    CONSTRAINT pk_stock PRIMARY KEY (ticker_symbol)
);

CREATE TABLE stock_dividend
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    create_at        datetime NULL,
    modified_at      datetime NULL,
    ex_dividend_date date NOT NULL,
    record_date      date NOT NULL,
    payment_date     date NULL,
    dividend         BIGINT NULL,
    ticker_symbol    VARCHAR(255) NULL,
    CONSTRAINT pk_stock_dividend PRIMARY KEY (id)
);

ALTER TABLE stock_dividend
    ADD CONSTRAINT UNIQUE_STOCK_DIVIDEND UNIQUE (ticker_symbol, record_date);

ALTER TABLE portfolio_gain_history
    ADD CONSTRAINT FK_PORTFOLIOGAINHISTORY_ON_PORTFOLIO FOREIGN KEY (portfolio_id) REFERENCES portfolio (id);

ALTER TABLE portfolio_holding
    ADD CONSTRAINT FK_PORTFOLIOHOLDING_ON_PORTFOLIO FOREIGN KEY (portfolio_id) REFERENCES portfolio (id);

ALTER TABLE portfolio_holding
    ADD CONSTRAINT FK_PORTFOLIOHOLDING_ON_TICKER_SYMBOL FOREIGN KEY (ticker_symbol) REFERENCES stock (ticker_symbol);

ALTER TABLE port_folio_stock
    ADD CONSTRAINT FK_PORTFOLIOSTOCK_ON_PORTFOLIO FOREIGN KEY (portfolio_id) REFERENCES portfolio (id);

ALTER TABLE port_folio_stock
    ADD CONSTRAINT FK_PORTFOLIOSTOCK_ON_STOCK FOREIGN KEY (stock_id) REFERENCES stock (ticker_symbol);

ALTER TABLE portfolio
    ADD CONSTRAINT FK_PORTFOLIO_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE purchase_history
    ADD CONSTRAINT FK_PURCHASEHISTORY_ON_PORTFOLIO_HOLDING FOREIGN KEY (portfolio_holding_id) REFERENCES portfolio_holding (id);

ALTER TABLE stock_dividend
    ADD CONSTRAINT FK_STOCK_DIVIDEND_ON_TICKER_SYMBOL FOREIGN KEY (ticker_symbol) REFERENCES stock (ticker_symbol);