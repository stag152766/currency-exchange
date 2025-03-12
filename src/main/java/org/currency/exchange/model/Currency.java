package org.currency.exchange.model;

public class Currency {
    private long id;
    private String code;
    private String name;
    private String sign;

    public Currency(long id, String code, String name, String sign) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.sign = sign;
    }
}
