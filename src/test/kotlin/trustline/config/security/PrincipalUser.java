package trustline.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import trustline.appuser.model.Role;
import trustline.appuser.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Getter
public class PrincipalUser implements UserDetails {
    private final Set<GrantedAuthority> authorities = new HashSet<>();
    private final UUID id;
    private final AuthProvider authProvider;
    private final String username;
    private final boolean enabled;
    private final String password;


    public PrincipalUser(final User user) {
        id = user.getId();
        authProvider = user.getAuthProvider();
        username = user.getEmail();
        enabled = !user.isDeleted() && user.isAccountVerified();
        password = user.getPassword();
        setAuthorities(user.getRoles());
    }

    void setAuthorities(Set<Role> roles) {
        if (roles == null) {
            return;
        }

        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            role.getPermissions().stream()
                    .map(p -> new SimpleGrantedAuthority(p.getName()))
                    .forEach(authorities::add);
        }
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
