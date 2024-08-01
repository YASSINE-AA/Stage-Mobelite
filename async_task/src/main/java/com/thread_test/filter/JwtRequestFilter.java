package com.thread_test.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thread_test.service.CustomUserDetailsService;
import com.thread_test.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService myUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

  @Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
    // Extract JWT from cookies
    String jwt = null;
    Cookie[] cookies = request.getCookies();
  
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            System.out.println("Cookie: " + cookie.getName() + "=" + cookie.getValue());
            if ("JWT".equals(cookie.getName())) {
                jwt = cookie.getValue();
                break;
            }
        }
    }

    if (jwt != null && !"/register".equals(request.getRequestURI()) && !"/login".equals(request.getRequestURI()) && !"/authenticate".equals(request.getRequestURI())) {
        String username = jwtUtil.extractUsername(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    }

    chain.doFilter(request, response);
}

}
