# MoneyMachine


# Init DB
DROP TABLE IF EXISTS currency;
DROP TABLE IF EXISTS currency_pair;
DROP TABLE IF EXISTS balance_history;
DROP TABLE IF EXISTS trade;
DROP TABLE IF EXISTS orders;

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
    pair                VARCHAR(999) NOT NULL,
    type                VARCHAR(999) NOT NULL,
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

