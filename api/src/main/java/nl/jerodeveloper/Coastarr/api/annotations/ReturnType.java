package nl.jerodeveloper.coastarr.api.annotations;

import lombok.Getter;

public enum ReturnType {

    JSON("application/json"),
    TEXT("text/plain; charset=utf-8"),
    ;

    @Getter private final String header;

    ReturnType(String header) {
        this.header = header;
    }

}
