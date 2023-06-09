package com.cursework.ehelthcare.registration;

import com.cursework.ehelthcare.auth.AuthenticationResponse;
import com.cursework.ehelthcare.config.JwtService;
import com.cursework.ehelthcare.email.EmailSender;
import com.cursework.ehelthcare.token.Token;
import com.cursework.ehelthcare.token.TokenRepository;
import com.cursework.ehelthcare.token.TokenType;
import com.cursework.ehelthcare.user.User;
import com.cursework.ehelthcare.user.UserRole;
import com.cursework.ehelthcare.user.UserService;
import com.cursework.ehelthcare.registration.token.ConfirmationToken;
import com.cursework.ehelthcare.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * The type Registration service.
 */
@Service
@AllArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    private final TokenRepository tokenRepository;

    private final JwtService jwtService;

    /**
     * Register string.
     *
     * @param request the request
     * @return the string
     */
    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }
        var user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                UserRole.ROLE_USER);
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        String token = userService.singupUser(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user,jwtToken);
        AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
        String link = "http://localhost:8080/registration/confirm?token="+token;
        emailSender.send(request.getEmail(),buildEmail(request.getFirstName(),link));
        return token;
    }

    /**
     * Confirm token string.
     *
     * @param token the token
     * @return the string
     */
    @Transactional
    public String confirmToken(String token){
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(()->new IllegalStateException("Token not found"));
        if (confirmationToken.getConfirmedAt() != null){
            throw new IllegalStateException("Email already confirmed");
        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Token Expired");
        }
        confirmationTokenService.setConfirmedAt(token);
        userService.enableAppUser(confirmationToken.getUser().getEmail());
        return "confirmed";
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family: Arial, sans-serif; font-size: 16px; line-height: 1.5; margin: 0;\">\n" +
                "    <div style=\"background-color: #fff; padding: 40px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.2);\">\n" +
                "        <h2 style=\"font-size: 28px; margin-bottom: 20px; text-align: center;\">Email Confirmation</h2>\n" +
                "        <p style=\"font-size: 18px; margin-bottom: 30px; text-align: center;\">Dear " + name + ",</p>\n" +
                "        <p style=\"font-size: 18px; margin-bottom: 20px;\">Thank you for registering with our e-Healthcare website. To activate your account, please click the\n" +
                "            link below:</p>\n" +
                "        <p style=\"text-align: center;\">\n" +
                "            <a style=\"background-color: #418520; color: #fff; font-size: 18px; padding: 10px 20px; border-radius: 5px; text-decoration: none;\" href=\"" + link + "\">Activate Account</a>\n" +
                "        </p>\n" +
                "        <p style=\"font-size: 18px; margin-top: 20px;\">This link will expire in 15 minutes.</p>\n" +
                "    </div>\n" +
                "</div>";

    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}

