package tech.introduct.mailbox.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

import static tech.introduct.mailbox.config.RequestMatcherProvider.hasBearerMatcher;

@Configuration
@EnableResourceServer
@RequiredArgsConstructor
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .requestMatcher(hasBearerMatcher())
                .authorizeRequests()
                .antMatchers("/admin/data/**").hasRole("DB_READ_WRITE")
                .antMatchers("/websocket/**").authenticated()
                .antMatchers("/private/api/**").authenticated()
                .anyRequest().denyAll();
    }
}
