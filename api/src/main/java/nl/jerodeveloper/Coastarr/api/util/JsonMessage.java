package nl.jerodeveloper.coastarr.api.util;

import lombok.Data;

@Data
public class JsonMessage {

    private String message;

    public JsonMessage(String message) {
        this.message = message;
    }

}
