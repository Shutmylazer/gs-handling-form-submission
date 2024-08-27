package com.example.securelogin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.sql.Connection;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/index", "/register", "/components/**", "/styles/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login?logout"));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static class CustomUserDetailsService implements UserDetailsService {

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            try (Connection connection = DbUtility.createConnection()) {
                if (!DbUtility.checkIfUserIsPresent(connection, username)) {
                    throw new UsernameNotFoundException("Non è stato trovato alcun utente con username: " + username);
                }

                User dbUser = DbUtility.getUser(connection, username);

                return new UserDB(dbUser.getUsername(), dbUser.getPassword(), dbUser.getSalt(),
                        dbUser.getName(), dbUser.getSurname(), dbUser.getTelephone());
            } catch (Exception e) {
                throw new UsernameNotFoundException("Database error occurred", e);
            }
        }
    }


}
