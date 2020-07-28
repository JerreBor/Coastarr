package nl.jerodeveloper.coastarr.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

public enum Constants {

    INSTANCE;

    @Getter private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

}
