package org.avyla.security.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.security.api.dto.AuthCreateUserRequest;
import org.avyla.security.api.dto.AuthLoginRequest;
import org.avyla.security.api.dto.AuthResponse;
import org.avyla.security.domain.model.RoleEntity;
import org.avyla.security.domain.model.UserEntity;
import org.avyla.security.domain.repo.RoleRepository;
import org.avyla.security.domain.repo.UserRepository;
import org.avyla.security.infraestructure.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        UserEntity userEntity = userRepository.findUserEntitiesByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found with username: " + username)
                );

        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();

        userEntity.getRoles()
                .forEach(
                        role -> authorityList.add(
                                new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))
                        )
                );

        userEntity.getRoles()
                .stream()
                .flatMap(
                        role -> role.getPermissionList()
                                .stream())
                .forEach(
                        permission -> authorityList
                                .add(new SimpleGrantedAuthority(permission.getName())));

        return new User
                (
                        userEntity.getUsername(),
                        userEntity.getPassword(),
                        userEntity.isEnabled(),
                        userEntity.isAccountNonExpired(),
                        userEntity.isCredentialsNonExpired(),
                        userEntity.isAccountNonLocked(),
                        authorityList
                );

    }

    public AuthResponse createUser(AuthCreateUserRequest createUserRequest)
    {
        String username = createUserRequest.username();
        String password = createUserRequest.password();
        List<String> rolesRequest = createUserRequest.roleRequest().roleListName();

        Set<RoleEntity> roleEntitySet = roleRepository.findByRoleEnumIn(rolesRequest)
                .stream()
                .collect(Collectors.toSet());

        if(roleEntitySet.isEmpty()){
            throw new IllegalArgumentException("No valid roles found for the user");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(createUserRequest.email())
                .firstName(createUserRequest.firstName())
                .lastName(createUserRequest.lastName())
                .documentType(createUserRequest.documentIdentityType())
                .documentNumber(createUserRequest.documentNumber())
                .phoneNumber(createUserRequest.phoneNumber())
                .roles(roleEntitySet)
                .isEnable(true)
                .isAccountNoLocked(true)
                .isAccountNonExpired(true)
                .isCredentialsNonExpired(true)
                .build();

        UserEntity userSaved = userRepository.save(userEntity);

        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();

         userSaved.getRoles()
                 .forEach(
                         role -> authorities.add(
                                 new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))
                         )
                 );

         userSaved.getRoles()
                 .stream()
                 .flatMap(
                         role -> role.getPermissionList()
                                 .stream())
                 .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userSaved, null, authorities);

        String accessToken = jwtUtils.createToken(authentication);

        return new AuthResponse(
                username,
                "User created successfully",
                accessToken,
                true
        );

    }

    public AuthResponse loginUser(AuthLoginRequest loginRequest)
    {
        String username = loginRequest.username();
        String password = loginRequest.password();

        Authentication authentication = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.createToken(authentication);
        return new AuthResponse(
                username,
                "User logged in successfully",
                accessToken,
                true
        );
    }

    private Authentication authenticate(String username, String password)
    {
        UserDetails userDetails = this.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException(String.format("Invalid username or password: %s", username));
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException(String.format("Invalid password: %s", username));
        }

        return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
    }

}
