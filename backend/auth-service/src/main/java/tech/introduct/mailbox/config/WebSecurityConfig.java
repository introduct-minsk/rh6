package tech.introduct.mailbox.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .requestMatcher(request -> isEmpty(request.getHeader(AUTHORIZATION)))
                .authorizeRequests()
                .antMatchers("/oauth/actuator/health").permitAll()
                .antMatchers("/oauth/actuator/**").denyAll()
                .anyRequest().authenticated()
                .and()
                .logout().logoutUrl("/oauth2/logout").permitAll()
                .and()
                .oauth2Login()
                .defaultSuccessUrl("/", true)
                .redirectionEndpoint()
                .baseUri("/authorize/taara-callback");
    }
}
