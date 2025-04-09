package org.currency.exchange.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeRateDto {
    private String baseCurrencyCode;
    private String targetCurrencyCode;
    private double rate;

    public ExchangeRateDto() {
    }

    public ExchangeRateDto(String baseCurrencyCode, String targetCurrencyCode, double rate) {
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        this.rate = rate;
    }
}
