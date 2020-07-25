package nl.jerodeveloper.coastarr.api.util;

import nl.jerodeveloper.coastarr.api.Coastarr;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class PasswordUtil {

    public CompletableFuture<Optional<String>> getSalt() {
        return CompletableFuture.supplyAsync(() -> {
            if (Coastarr.getSettingsUtil().getSettings().getPASS_SECRET().isEmpty()) {
                byte[] salt = new byte[512];
                new SecureRandom().nextBytes(salt);

                String saltEncoded = Base64.getEncoder().encodeToString(salt);
                Coastarr.getSettingsUtil().getSettings().setPASS_SECRET(saltEncoded);

                return Optional.of(saltEncoded);
            }
            return Optional.of(Coastarr.getSettingsUtil().getSettings().getPASS_SECRET());
        });
    }

    public CompletableFuture<Optional<String>> hashPassword(String password, String salt) {
        return CompletableFuture.supplyAsync(() -> {
            char[] chars = password.toCharArray();
            byte[] bytes = salt.getBytes();

            PBEKeySpec spec = new PBEKeySpec(chars, bytes, 65536, 512);

            Arrays.fill(chars, Character.MIN_VALUE);

            try {
                SecretKeyFactory fac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
                byte[] securePassword = fac.generateSecret(spec).getEncoded();
                return Optional.of(Base64.getEncoder().encodeToString(securePassword));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        });
    }

    public CompletableFuture<Boolean> verifyPassword(String password, String key, String salt) {
        return hashPassword(password, salt).thenApply((optEncrypted) -> {
            if(optEncrypted.isEmpty()) return false;
            return optEncrypted.get().equals(key);
        });
    }

}
