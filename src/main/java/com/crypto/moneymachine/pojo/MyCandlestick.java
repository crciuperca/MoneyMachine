package com.crypto.moneymachine.pojo;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class MyCandlestick {
    CandlestickInterval interval;
    Date openTime;
    double open;
    double close;
    double high;
    double low;
    double ema;
    double macd;
    double signal;

    public MyCandlestick() {
    }

    public MyCandlestick(Candlestick candlestick, CandlestickInterval interval) {
        this.interval = interval;
        this.openTime = new Date(candlestick.getOpenTime());
        this.open = Double.parseDouble(candlestick.getOpen());
        this.close = Double.parseDouble(candlestick.getClose());
        this.high = Double.parseDouble(candlestick.getHigh());
        this.low = Double.parseDouble(candlestick.getLow());
    }



    public MyCandlestick clone() {
        return new MyCandlestick(interval, openTime, open, close, high, low, ema, macd, signal);
    }

}
