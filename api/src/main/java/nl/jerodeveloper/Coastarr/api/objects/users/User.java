package nl.jerodeveloper.coastarr.api.objects.users;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {

    int id;
    String username, key;
    Group userGroup;

}
