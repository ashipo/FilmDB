package com.demo.filmdb.security;

import com.demo.filmdb.security.dtos.LoginRequestDto;
import com.demo.filmdb.security.dtos.LoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.demo.filmdb.utils.Path.API_PREFIX;
import static com.demo.filmdb.utils.Path.LOGIN;
import static com.demo.filmdb.utils.SpringDocConfig.TAG_LOGIN;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = API_PREFIX + LOGIN, produces = APPLICATION_JSON_VALUE)
@SecurityRequirements
public class JwtAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(tags = TAG_LOGIN)
    @PostMapping
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password());
        Authentication authenticationResponse;
        try {
            authenticationResponse = authenticationManager.authenticate(authenticationRequest);
            UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
            LoginResponseDto response = new LoginResponseDto(jwtUtil.generateToken(userDetails));
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(WWW_AUTHENTICATE, "Basic realm=\"Realm\"").body(null);
        }
    }
}
