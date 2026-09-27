package com.onlinemarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.onlinemarket.security.JwtAccessDeniedHandler;
import com.onlinemarket.security.JwtAuthenticationEntryPoint;
import com.onlinemarket.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint authenticationEntryPoint, JwtAccessDeniedHandler accessDeniedHandler) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
				.requestMatchers("/h2-console/**").permitAll()
				.requestMatchers("/stakeholder/login").permitAll()
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
				.requestMatchers("/actuator/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
			.exceptionHandling(handling -> handling
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
