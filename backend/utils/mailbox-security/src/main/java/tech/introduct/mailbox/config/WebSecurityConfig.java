package tech.introduct.mailbox.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static tech.introduct.mailbox.config.RequestMatcherProvider.hasNotBearerMatcher;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().csrfTokenRepository(new MailboxCsrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .and()
                .requestMatcher(hasNotBearerMatcher())
                .authorizeRequests()
                .antMatchers("/websocket/**").hasRole("USER")
                .antMatchers("/api/**/actuator/health").permitAll()
                .antMatchers("/api/**/actuator/**").denyAll()
                .antMatchers("/api/**").hasRole("USER")
                .anyRequest().denyAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @RequiredArgsConstructor
    private static class MailboxCsrfTokenRepository implements CsrfTokenRepository {
        private final CookieCsrfTokenRepository repository;

        @Override
        public CsrfToken generateToken(HttpServletRequest request) {
            return repository.generateToken(request);
        }

        @Override
        public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
            repository.saveToken(token, request, response);
            response.setHeader("Set-Cookie", response.getHeader("Set-Cookie") + "; SameSite=strict");
        }

        @Override
        public CsrfToken loadToken(HttpServletRequest request) {
            return repository.loadToken(request);
        }
    }
}
