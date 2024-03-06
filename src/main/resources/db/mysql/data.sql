LOAD DATA LOCAL INFILE 'src/main/resources/stocks.tsv'
    INTO TABLE stock
    FIELDS TERMINATED BY '\t'
    IGNORE 1 ROWS
    (@stockCode, @tickerSymbol, @companyName, @companyNameEng, @market)
    set stock_code = @stockCode,
        ticker_symbol = @tickerSymbol,
        company_name = @companyName,
        company_name_eng = @companyNameEng,
        market = @market,
        create_at = now();

LOAD DATA LOCAL INFILE 'src/main/resources/ex-dividend-date.tsv'
    INTO TABLE stock_dividend
    FIELDS TERMINATED BY '\t'
    IGNORE 1 ROWS
    (@ex_dividend_date, @record_date, @payment_date, @ticker_symbol, @dividend_per_share)
    set stock_dividend.ex_dividend_date = @ex_dividend_date,
        stock_dividend.record_date = @record_date,
        stock_dividend.payment_date = @payment_date,
        stock_dividend.ticker_symbol = @ticker_symbol,
        stock_dividend.dividend = @dividend_per_share,
        stock_dividend.create_at = now();

CREATE TEMPORARY TABLE temp_update_table
select ticker_symbol, sector
from stock
limit 0;

LOAD DATA LOCAL INFILE 'src/main/resources/sectors_kospi.tsv'
    INTO TABLE temp_update_table
    FIELDS TERMINATED BY '\t'
    IGNORE 1 ROWS
    (@종목코드, @종목명, @시장구분, @업종명, @종가, @대비, @등락률, @시가총액)
    set ticker_symbol = @종목코드,
        sector = @업종명;

LOAD DATA LOCAL INFILE 'src/main/resources/sectors_kosdaq.tsv'
    INTO TABLE temp_update_table
    FIELDS TERMINATED BY '\t'
    IGNORE 1 ROWS
    (@종목코드, @종목명, @시장구분, @업종명, @종가, @대비, @등락률, @시가총액)
    set ticker_symbol = @종목코드,
        sector = @업종명;

UPDATE stock
    INNER JOIN temp_update_table on temp_update_table.ticker_symbol = stock.ticker_symbol
SET stock.sector = temp_update_table.sector;

DROP TEMPORARY TABLE temp_update_table;


INSERT INTO member(create_at, email, nickname, profile_url, password, provider)
VALUES (now(), 'dragonbead95@naver.com', '네모네모',
        'http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg',
        '$2a$10$Uy0yFi68XP6M.52aRoCQQe6FtnmitlW2mGSO4WdiOU6kzueMBtWda',
        'local');

INSERT INTO fineAnts.portfolio (id, budget, maximum_loss, name, securities_firm, target_gain, target_gain_is_active,
                                maximum_loss_is_active, member_id, create_at)
VALUES (1, 1000000, 900000, '내꿈은 워렌버핏', '토스증권', 1500000, false, false, 1, now());

INSERT INTO fineAnts.portfolio_holding (id, create_at, modified_at, portfolio_id, ticker_symbol)
VALUES (1, '2023-10-26 15:25:39.409612', '2023-10-26 15:25:39.409612', 1, '005930');

INSERT INTO fineAnts.portfolio_holding (id, create_at, modified_at, portfolio_id, ticker_symbol)
VALUES (2, '2023-10-26 15:25:39.409612', '2023-10-26 15:25:39.409612', 1, '000020');

INSERT INTO fineAnts.portfolio_holding (id, create_at, modified_at, portfolio_id, ticker_symbol)
VALUES (3, '2023-10-26 15:25:39.409612', '2023-10-26 15:25:39.409612', 1, '000040');

INSERT INTO fineAnts.portfolio_holding (id, create_at, modified_at, portfolio_id, ticker_symbol)
VALUES (4, '2023-10-26 15:25:39.409612', '2023-10-26 15:25:39.409612', 1, '000050');

INSERT INTO fineAnts.portfolio_holding (id, create_at, modified_at, portfolio_id, ticker_symbol)
VALUES (5, '2023-10-26 15:25:39.409612', '2023-10-26 15:25:39.409612', 1, '000070');

INSERT INTO fineAnts.portfolio_holding (id, create_at, modified_at, portfolio_id, ticker_symbol)
VALUES (6, '2023-10-26 15:25:39.409612', '2023-10-26 15:25:39.409612', 1, '000080');

INSERT INTO fineAnts.portfolio_holding (id, create_at, modified_at, portfolio_id, ticker_symbol)
VALUES (7, '2023-10-26 15:25:39.409612', '2023-10-26 15:25:39.409612', 1, '000087');

INSERT INTO fineAnts.purchase_history (id, create_at, modified_at, memo, num_shares, purchase_date,
                                       purchase_price_per_share, portfolio_holding_id)
VALUES (1, '2023-10-26 15:26:11.219793', '2023-10-26 15:26:11.219793', null, 3, '2023-10-23 13:00:00.000000', 50000, 1);

INSERT INTO fineAnts.notification(id, create_at, title, is_read, type, reference_id, dtype, stock_name, target_price,
                                  member_id)
VALUES (1, '2024-01-22 10:10:10.0', '종목 지정가', true, 'STOCK_TARGET_PRICE', '005930', 'S', '삼성전자보통주', 60000, 1);

INSERT INTO fineAnts.notification(id, create_at, title, is_read, type, reference_id, dtype, portfolio_name, member_id)
VALUES (2, '2024-01-23 10:10:10.0', '포트폴리오', false, 'PORTFOLIO_TARGET_GAIN', '1', 'P', '내꿈은 워렌버핏', 1);

INSERT INTO fineAnts.notification_preference(id, create_at, browser_notify, max_loss_notify, target_gain_notify,
                                             target_price_notify, member_id)
VALUES (1, now(), true, true, true, true, 1);
