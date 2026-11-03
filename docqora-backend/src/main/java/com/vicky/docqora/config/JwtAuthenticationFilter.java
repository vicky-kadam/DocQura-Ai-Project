package com.vicky.docqora.config;

import com.vicky.docqora.Service.JwtService;
import com.vicky.docqora.Service.security.CustomUserDetailService;
import com.vicky.docqora.Service.security.CustomUserDetails;
import com.vicky.docqora.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailService userDetailService;
    private final UserRepository userRepository;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(7);

        String userName;

        try {
          userName =  jwtService.extractUserName(token);
          if (userName == null || !jwtService.isValidToken(token,userDetailService.loadUserByUsername(userName))){
              filterChain.doFilter(request,response);
              return;
          }
          var user = userRepository.findByUserName(userName).orElseThrow(() -> new RuntimeException("User not found"));
            var authentication = new UsernamePasswordAuthenticationToken(user,null,new CustomUserDetails(user).getAuthorities());
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

//              filterChain.doFilter(request,response);

        }catch (Exception e){
            filterChain.doFilter(request,response);
            e.getStackTrace();
            return;
        }

        filterChain.doFilter(request,response);

    }
}
