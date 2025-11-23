package resume.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import resume.repository.PersonRepository;
import resume.web.security.JwtService;
import resume.web.security.PasswordHasher;
import resume.service.exceptions.ForbiddenException;
import resume.web.dto.auth.LoginRequestDto;
import resume.web.dto.auth.LoginResponseDto;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PersonRepository personRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto req) {
        var p = personRepository.findByEmail(req.email())
                .orElseThrow(() -> new ForbiddenException("Bad credentials"));

        if (!passwordHasher.matches(req.password(), p.getPasswordHash())) {
            throw new ForbiddenException("Bad credentials");
        }

        var token = jwtService.generateToken(p.getId(), p.getEmail());
        return new LoginResponseDto(token);
    }
}
