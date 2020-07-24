package nl.jerodeveloper.coastarr.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

public enum Constants {

    INSTANCE;

    @Getter private Gson gson = new GsonBuilder()
            .create();

}
