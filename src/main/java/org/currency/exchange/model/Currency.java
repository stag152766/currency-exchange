package org.currency.exchange.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Currency {
    private long id;
    private String code;
    private String fullName;
    private String sign;

    public Currency() {
    }

    public Currency(String code, String fullName, String sign) {
        this.code = code;
        this.fullName = fullName;
        this.sign = sign;
    }

    public Currency(long id, String code, String fullName, String sign) {
        this.id = id;
        this.code = code;
        this.fullName = fullName;
        this.sign = sign;
    }
}
