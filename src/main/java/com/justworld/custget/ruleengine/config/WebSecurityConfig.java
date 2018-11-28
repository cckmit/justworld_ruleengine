package com.justworld.custget.ruleengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.service.auth.JwtAuthenticationTokenFilter;
import com.justworld.custget.ruleengine.service.auth.JwtTokenUtil;
import com.justworld.custget.ruleengine.service.auth.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserLoginService userDetailService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    private ObjectMapper mapper;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint((httpServletRequest, httpServletResponse, e) -> {
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.getWriter().write(mapper.writeValueAsString(BaseResult.buildFail("401", "请先登录")));
                })
                .and()
                .authorizeRequests()
                .antMatchers("/messageReceive/**"
                        , "/click/**"
                        , "/aismsjob/weibocode")
                .permitAll()
                .anyRequest().authenticated()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()

                //登录处理
                .and()
                .formLogin().loginPage("/auth/login")
                .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    //生成TOKEY
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    String token = jwtTokenUtil.generateToken(userDetails.getUsername());
                    //放入缓存中 TODO 目前临时在内存里
                    JwtTokenUtil.validTokenStore.put(token, userDetails);
                    if (JwtTokenUtil.loginedUserStore.get(userDetails.getUsername()) != null) {
                        JwtTokenUtil.loginedUserStore.get(userDetails.getUsername()).add(token);
                    } else {
                        Set tokenSet = new HashSet();
                        tokenSet.add(token);
                        JwtTokenUtil.loginedUserStore.put(userDetails.getUsername(), tokenSet);
                    }
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.getWriter().write(mapper.writeValueAsString(BaseResult.buildSuccess(JwtTokenUtil.TOKEN_PREFIX + token)));
                })
                .failureHandler((httpServletRequest, httpServletResponse, e) -> {
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.getWriter().write(mapper.writeValueAsString(BaseResult.buildFail("400", "登录失败，请检查账号/密码是否正确")));
                })
                .permitAll()

                //登出返回处理
                .and()
                .logout().logoutUrl("/auth/logout")
                .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    String token = jwtTokenUtil.getToken(httpServletRequest);
                    UserDetails userDetails = JwtTokenUtil.validTokenStore.get(token);
                    if (userDetails != null) {
                        JwtTokenUtil.validTokenStore.remove(token);
                        Set<String> tokenList = JwtTokenUtil.loginedUserStore.get(userDetails.getUsername());
                        if (tokenList != null && tokenList.size() > 0) {
                            tokenList.remove(token);
                            if (tokenList.isEmpty()) {
                                JwtTokenUtil.loginedUserStore.remove(userDetails.getUsername());
                            }
                        }
                    }
                    httpServletResponse.getWriter().write(mapper.writeValueAsString(BaseResult.buildSuccess()));
                })
                .permitAll()

                //从TOKEN中取认证信息
                .and()
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)

                //权限不足返回
                .exceptionHandling().accessDeniedHandler((httpServletRequest, httpServletResponse, e) -> {
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.getWriter().write(mapper.writeValueAsString(BaseResult.buildFail("403", "权限不足")));
                })

//                .antMatchers("/aismsjob/**").hasAuthority("1")
        ;

        //解决中文乱码问题
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        http.addFilterBefore(filter, CsrfFilter.class);
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

}
