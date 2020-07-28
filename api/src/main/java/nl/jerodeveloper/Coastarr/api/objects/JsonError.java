package nl.jerodeveloper.coastarr.api.objects;

public class JsonError extends JsonMessage {

    private final int CODE;

    public JsonError(String message, int code) {
        super(message);
        this.CODE = code;
    }

}
