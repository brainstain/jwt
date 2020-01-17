package com.devglan.controller;

import com.devglan.config.TokenProvider;
import com.devglan.model.AuthToken;
import com.devglan.model.LoginUser;
import com.devglan.model.Refresh;
import com.devglan.model.User;
import com.devglan.service.RefreshService;
import com.devglan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static com.devglan.model.Constants.HEADER_STRING;
import static com.devglan.model.Constants.TOKEN_PREFIX;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider jwtTokenUtil;

    @Autowired
    private RefreshService refreshService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestBody LoginUser loginUser) throws AuthenticationException {

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUser.getUsername(),
                        loginUser.getPassword()
                )
        );
        //cleanup any existing refresh tokens
        refreshService.deleteByUsername(loginUser.getUsername());
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = jwtTokenUtil.generateToken(authentication);
        final String refresh = jwtTokenUtil.generateRefresh();

        Refresh newRefresh = new Refresh();
        newRefresh.SetNewRefresh(refresh, jwtTokenUtil.getUsernameFromToken(token));
        refreshService.save(newRefresh);    
        return ResponseEntity.ok(new AuthToken(token, refresh));
    }

    //Sorry for the throws Exception...
    @RequestMapping(value="/token", method=RequestMethod.POST)
    public ResponseEntity<?> refreshToken(@RequestHeader(HEADER_STRING) String authorization, @RequestBody String refreshToken) throws Exception {
        // Check Database for Refresh Token, invalidate
        Refresh refresh = refreshService.getRefreshByToken(refreshToken);
        if(refresh == null || !refresh.isValidRefresh()){
            return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED);
        }
        //check timing of refresh token

        refreshService.invalidate(refresh);

        //generate new auth token
        if (authorization != null && authorization.startsWith(TOKEN_PREFIX)) {
            authorization = authorization.replace(TOKEN_PREFIX,"");
        } else{
            //Update this
            throw new Exception("No auth token to update");
        }
        authorization = jwtTokenUtil.updateToken(authorization);
        //Update Refresh Token
        final String refreshUpdated = jwtTokenUtil.generateRefresh();
        Refresh newRefresh = new Refresh();
        newRefresh.SetNewRefresh(refreshUpdated, jwtTokenUtil.getUsernameFromToken(authorization));
        refreshService.save(newRefresh);    
        
        return ResponseEntity.ok(new AuthToken(authorization, refreshUpdated));
    }

    //TODO
    // public ResponseEntity<?> logout
    // delete refresh tokens for user

}
