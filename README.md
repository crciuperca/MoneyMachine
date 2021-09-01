# MoneyMachine


# Init DB
DROP TABLE IF EXISTS currency cascade;
DROP TABLE IF EXISTS currency_pair cascade;
DROP TABLE IF EXISTS balance_history cascade;
DROP TABLE IF EXISTS trade cascade;
DROP TABLE IF EXISTS orders cascade;

CREATE TABLE currency (
    symbol       VARCHAR(999) PRIMARY KEY,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE currency_pair (
    symbol      VARCHAR(999) PRIMARY KEY,
    first       VARCHAR(999),
    second      VARCHAR(999),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT  fk_cp_c_a
        FOREIGN KEY(first) REFERENCES currency(symbol),
    CONSTRAINT  fk_cp_c_b
        FOREIGN KEY(second) REFERENCES currency(symbol)
);

CREATE TABLE balance_history (
    id uuid      DEFAULT gen_random_uuid() PRIMARY KEY,
    currency     VARCHAR(999) NOT NULL,
    amount       DOUBLE PRECISION NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT  fk_bh_c
        FOREIGN KEY(currency) REFERENCES currency(symbol)
);

CREATE TABLE trade (
    id uuid             DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id            VARCHAR(999) UNIQUE NOT NULL,
    pair                VARCHAR(999) NOT NULL,
    type                VARCHAR(999) NOT NULL,
    side                VARCHAR(999) NOT NULL,
    status              VARCHAR(999) NOT NULL,
    from_currency       VARCHAR(999) NOT NULL,
    to_currency         VARCHAR(999) NOT NULL,
    price               DOUBLE PRECISION NOT NULL,
    stop_price          DOUBLE PRECISION,
    stop_limit          DOUBLE PRECISION,
    quantity            DOUBLE PRECISION NOT NULL,
    executed_quantity   DOUBLE PRECISION,
    profit              DOUBLE PRECISION,
    percent_profit      DOUBLE PRECISION,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT  fk_t_p
        FOREIGN KEY(pair) REFERENCES currency_pair(symbol),
    CONSTRAINT  fk_t_c_a
        FOREIGN KEY(from_currency) REFERENCES currency(symbol),
    CONSTRAINT  fk_t_c_b
        FOREIGN KEY(to_currency) REFERENCES currency(symbol)
);

CREATE TABLE orders (
    id uuid             DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id            VARCHAR(999) UNIQUE NOT NULL,
    trade_id            UUID NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT  fk_o_t
        FOREIGN KEY(trade_id) REFERENCES trade(id)
);


[WAIT] Wed Sep 01 07:39:00 EEST 2021 [
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:37:00 EEST 2021, open=179.12, close=179.04, high=179.28, low=179.02, ema=179.25391503166105, macd=0.07021436825914407, signal=0.21427667611714338) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:38:00 EEST 2021, open=179.14, close=178.94, high=179.23, low=178.86, ema=179.2056204114055, macd=0.03997164899632821, signal=0.17941567069298034) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:39:00 EEST 2021, open=178.85, close=179.85, high=179.99, low=178.85, ema=179.30475573272773, macd=0.08841428605256851, signal=0.16121539376489796)]
[WAIT] Wed Sep 01 07:40:00 EEST 2021 [
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:38:00 EEST 2021, open=179.14, close=178.94, high=179.23, low=178.86, ema=179.2056204114055, macd=0.03997164899632821, signal=0.17941567069298034) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:39:00 EEST 2021, open=178.85, close=179.85, high=179.99, low=178.85, ema=179.30475573272773, macd=0.08841428605256851, signal=0.16121539376489796) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:40:00 EEST 2021, open=179.83, close=180.75, high=180.75, low=179.79, ema=179.52710100461576, macd=0.19715522065726532, signal=0.16840335914337143)]
[WAIT] Wed Sep 01 07:41:00 EEST 2021 [
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:39:00 EEST 2021, open=178.85, close=179.85, high=179.99, low=178.85, ema=179.30475573272773, macd=0.08841428605256851, signal=0.16121539376489796) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:40:00 EEST 2021, open=179.83, close=180.75, high=180.75, low=179.79, ema=179.52710100461576, macd=0.19715522065726532, signal=0.16840335914337143) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:41:00 EEST 2021, open=180.75, close=180.56, high=180.75, low=180.36, ema=179.68600854236718, macd=0.26494763129448984, signal=0.1877122135735951)]
[WAIT] Wed Sep 01 07:42:00 EEST 2021 [
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:40:00 EEST 2021, open=179.83, close=180.75, high=180.75, low=179.79, ema=179.52710100461576, macd=0.19715522065726532, signal=0.16840335914337143) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:41:00 EEST 2021, open=180.75, close=180.56, high=180.75, low=180.36, ema=179.68600854236718, macd=0.26494763129448984, signal=0.1877122135735951) 
MyCandlestick(interval=ONE_MINUTE, openTime=Wed Sep 01 07:42:00 EEST 2021, open=180.64, close=180.74, high=180.75, low=180.55, ema=179.8481610743107, macd=0.329400971465617, signal=0.21604996515199948)]

