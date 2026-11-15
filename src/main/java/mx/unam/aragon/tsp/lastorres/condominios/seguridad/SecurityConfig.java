package mx.unam.aragon.tsp.lastorres.condominios.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Reglas de seguridad: quién puede entrar a qué.
 *
 * Roles:
 *  - ADMIN:   todo (viviendas, residentes, visitantes, caseta, Swagger).
 *  - GUARDIA: solo la caseta (entradas, salidas, bitácora) y consultas de la API.
 *
 * Las pantallas usan el formulario de login; la API (/api/**) acepta además
 * usuario y contraseña por HTTP Basic, para Postman y K6.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/error").permitAll()
                        // API: la caseta la usan ambos roles; consultar, también; modificar, solo ADMIN
                        .requestMatchers("/api/v1/accesos/**").hasAnyRole("ADMIN", "GUARDIA")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "GUARDIA")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").hasRole("ADMIN")
                        // Pantallas de administración
                        .requestMatchers("/viviendas/**", "/residentes/**", "/visitantes/**").hasRole("ADMIN")
                        // Lo demás (inicio, caseta, bitácora) pide haber iniciado sesión
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?salir")
                        .permitAll())
                // Los formularios web llevan token CSRF; la API no lo necesita
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(puntoDeEntrada()));

        return http.build();
    }

    /**
     * Qué pasa cuando alguien sin sesión pide algo protegido:
     * la API responde 401 y las pantallas mandan al login.
     */
    private AuthenticationEntryPoint puntoDeEntrada() {
        return (request, response, excepcion) -> {
            if (request.getRequestURI().startsWith("/api/")) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Las Torres\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión");
            } else {
                response.sendRedirect("/login");
            }
        };
    }

    /** Las contraseñas se guardan cifradas con BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
