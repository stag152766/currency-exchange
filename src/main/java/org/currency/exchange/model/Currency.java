package org.currency.exchange.model;

import lombok.Getter;

@Getter
public class Currency {
    private long id;
    private String code;
    private String full;
    private String sign;

    public Currency(long id, String code, String full, String sign) {
        this.id = id;
        this.code = code;
        this.full = full;
        this.sign = sign;
    }
}
