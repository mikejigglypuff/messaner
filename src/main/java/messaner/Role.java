package messaner;

import lombok.Getter;

@Getter
public enum Role {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER, ROLE_GUEST");

    private final String role;

    private Role(String role) {
        this.role = role;
    }

}
