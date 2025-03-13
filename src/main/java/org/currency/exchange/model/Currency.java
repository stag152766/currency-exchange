package org.currency.exchange.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Currency {
    public Currency() {
    }

    public Currency(String code, String full, String sign) {
        this.code = code;
        this.full = full;
        this.sign = sign;
    }

    public Currency(long id, String code, String full, String sign) {
        this.id = id;
        this.code = code;
        this.full = full;
        this.sign = sign;
    }

    private long id;
    private String code;
    private String full;
    private String sign;
}
