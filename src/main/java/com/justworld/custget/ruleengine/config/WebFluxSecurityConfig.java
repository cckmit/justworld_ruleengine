package com.justworld.custget.ruleengine.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.service.auth.flux.FluxJwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.cookie.CookieHeaderNames.MAX_AGE;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class WebFluxSecurityConfig {

    @Autowired
    private FluxJwtTokenUtil jwtTokenUtil;
    @Autowired
    private ObjectMapper mapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .httpBasic().and().securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/messageReceive/**"
                        , "/click/**"
                        , "/auth/**"
                        , "/sysconfig/**"
                        , "/aismsjob/weibocode")
                .permitAll()
                .and()
                .formLogin().loginPage("/auth/login")
                .authenticationSuccessHandler(authenticationSuccessHandler())
                .authenticationFailureHandler(authenticationFailureHandler())
                .and().logout().logoutUrl("/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(serverAccessDeniedHandler())
                .and().authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .addFilterAt(tokenAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
        ;
        return http.build();
    }

    /**
     * 登录成功处理
     *
     * @return
     */
    protected ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        return (exchange, authentication) -> {
            //生成TOKEY
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(userDetails.getUsername());
            //放入缓存中 TODO 目前临时在内存里
            jwtTokenUtil.validTokenStore.put(token, userDetails);
            if (jwtTokenUtil.loginedUserStore.get(userDetails.getUsername()) != null) {
                jwtTokenUtil.loginedUserStore.get(userDetails.getUsername()).add(token);
            } else {
                Set tokenSet = new HashSet();
                tokenSet.add(token);
                jwtTokenUtil.loginedUserStore.put(userDetails.getUsername(), tokenSet);
            }

            try {
                ServerHttpResponse response = exchange.getExchange().getResponse();
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
                response.getHeaders().setAccessControlExposeHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION));
                response.getHeaders().set(HttpHeaders.AUTHORIZATION, jwtTokenUtil.TOKEN_PREFIX + token);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(mapper.writeValueAsBytes(BaseResult.buildSuccess()))));
            } catch (JsonProcessingException e) {
                log.error("生成token转换JOSN失败", e);
                return Mono.error(e);
            }

        };
    }

    /**
     * 登录失败处理
     *
     * @return
     */
    protected ServerAuthenticationFailureHandler authenticationFailureHandler() {
        return (exchange, authentication) -> {

            try {
                ServerHttpResponse response = exchange.getExchange().getResponse();
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(mapper.writeValueAsBytes(BaseResult.buildFail("400", "登录失败，请检查账号/密码是否正确")))));
            } catch (JsonProcessingException e) {
                log.error("生成token转换JOSN失败", e);
                return Mono.error(e);
            }
        };
    }

    /**
     * 注销处理
     *
     * @return
     */
    protected ServerLogoutSuccessHandler logoutSuccessHandler() {
        return (exchange, authentication) -> {
            ServerHttpResponse response = exchange.getExchange().getResponse();
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
            String token = jwtTokenUtil.getToken(exchange.getExchange().getRequest());
            UserDetails userDetails = jwtTokenUtil.validTokenStore.get(token);
            if (userDetails != null) {
                jwtTokenUtil.validTokenStore.remove(token);
                Set<String> tokenList = jwtTokenUtil.loginedUserStore.get(userDetails.getUsername());
                if (tokenList != null && tokenList.size() > 0) {
                    tokenList.remove(token);
                    if (tokenList.isEmpty()) {
                        jwtTokenUtil.loginedUserStore.remove(userDetails.getUsername());
                    }
                }
            }
            response.setStatusCode(HttpStatus.OK);
            try {
                DataBuffer d = response.bufferFactory().wrap(mapper.writeValueAsBytes(BaseResult.buildSuccess()));
                return response.writeWith(Mono.just(d));
            } catch (JsonProcessingException e) {
                log.error("注销登录失败", e);
                return Mono.error(e);
            }
        };
    }

    /**
     * 拒绝访问处理
     * @return
     */
    protected ServerAccessDeniedHandler serverAccessDeniedHandler() {
        return (exchange, exception) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            try {
                return response.writeWith(Mono.just(response.bufferFactory().wrap(mapper.writeValueAsBytes(BaseResult.buildFail("403", "权限不足")))));
            } catch (JsonProcessingException e) {
                log.error("拒绝访问处理失败", e);
                return Mono.error(e);
            }
        };
    }

    protected ServerAuthenticationEntryPoint authenticationEntryPoint(){
        return (exchange, exception) -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
            DataBuffer d;
            try {
                d = exchange.getResponse().bufferFactory().wrap(mapper.writeValueAsString(BaseResult.buildFail("401", "未登录，请先登录系统")).getBytes("UTF-8"));
                return exchange.getResponse().writeWith(Mono.just(d));
            } catch (Exception e) {
                return Mono.error(e);
            }

        };
    }

    protected AuthenticationWebFilter tokenAuthenticationFilter() {

        AuthenticationWebFilter filter = new AuthenticationWebFilter((authentication)->Mono.just(authentication));
        filter.setAuthenticationConverter(tokenAuthenticationConverter());

        return filter;
    }

    protected Function<ServerWebExchange, Mono<Authentication>> tokenAuthenticationConverter() {

        return serverWebExchange -> {
            String authorization = jwtTokenUtil.getToken(serverWebExchange.getRequest());
            if(StringUtils.isBlank(authorization))
                return Mono.empty();

            String username = jwtTokenUtil.getUsername(authorization);
            log.trace("username={}",username);
            UserDetails userDetails = jwtTokenUtil.validTokenStore.get(authorization);
            if(userDetails!=null) {
                return Mono.just(new UsernamePasswordAuthenticationToken(userDetails, authorization, userDetails.getAuthorities()));
            }else{
                return Mono.empty();
            }
        };
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("*"));
        corsConfig.setMaxAge(8000L);
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
