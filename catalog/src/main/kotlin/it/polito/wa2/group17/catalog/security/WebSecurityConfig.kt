package it.polito.wa2.group17.catalog.security

import it.polito.wa2.group17.catalog.security.jwt.JwtAuthenticationTokenFilter
import it.polito.wa2.group17.catalog.service.UserDetailsServiceExtendedImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import kotlin.jvm.Throws

@Configuration
@ConditionalOnSingleCandidate(value = WebSecurityConfigurerAdapter::class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(val passwordEncoder: PasswordEncoder, val userDetailsService: UserDetailsServiceExtendedImpl) :
    WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var authEntryPoint: AuthEntryPoint

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder)
    }

    override fun configure(http: HttpSecurity) {
        http
            .exceptionHandling().authenticationEntryPoint(authEntryPoint).and()
            .authorizeRequests()
            .antMatchers("/swagger-ui/").permitAll()
            .antMatchers("/auth/**").permitAll()
            //.antMatchers("/auth/setAdmin").authenticated()

        http.csrf().disable()

        http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun authenticationTokenFilter(): JwtAuthenticationTokenFilter = JwtAuthenticationTokenFilter()

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager = super.authenticationManagerBean()

}


/**
 * Security Configuration just for "local" profile
 */
@Configuration
@Profile("no-security")
class SecurityConfigLocal : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable()
            .antMatcher("/**").authorizeRequests().anyRequest().permitAll()
    }
}
