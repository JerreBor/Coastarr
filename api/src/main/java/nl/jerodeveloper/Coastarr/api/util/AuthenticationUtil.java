package nl.jerodeveloper.coastarr.api.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import nl.jerodeveloper.coastarr.api.Coastarr;
import nl.jerodeveloper.coastarr.api.objects.users.User;

import java.time.Instant;
import java.util.Date;

public class AuthenticationUtil {

    public void generateAuthenticationSecret() {
        if (Coastarr.getSettingsUtil().getSettings().getAUTH_SECRET().isEmpty()) {
            String secretKey = Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
            Coastarr.getSettingsUtil().getSettings().setAUTH_SECRET(secretKey);
        }
    }

    public boolean verifyToken(String jwt) {
        try {
            Jwts.parserBuilder().setSigningKey(Coastarr.getSettingsUtil().getSettings().getAUTH_SECRET()).build().parseClaimsJws(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userGroup", user.getUserGroup())
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(Coastarr.getSettingsUtil().getSettings().getAUTH_SECRET())))
                .setExpiration(Date.from(Instant.now().plusSeconds(Coastarr.getSettingsUtil().getSettings().getTOKEN_EXPIRATION())))
                .compact();
    }



}
