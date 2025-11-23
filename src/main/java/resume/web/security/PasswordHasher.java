package resume.web.security;

public interface PasswordHasher {

    String hash(String raw);
    boolean matches(String raw, String hash);
}
