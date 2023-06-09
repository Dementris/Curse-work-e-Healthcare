package com.cursework.ehelthcare.registration.token;


import com.cursework.ehelthcare.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The type Confirmation token.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "confirmaition_token")
public class ConfirmationToken {

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false)
    private LocalDateTime createAt;
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;

    @ManyToOne
    @JoinColumn(nullable = false, name = "users_id")
    private User user;

    /**
     * Instantiates a new Confirmation token.
     *
     * @param token     the token
     * @param createAt  the create at
     * @param expiresAt the expires at
     * @param user      the user
     */
    public ConfirmationToken(String token, LocalDateTime createAt, LocalDateTime expiresAt,
                             User user) {
        this.token = token;
        this.createAt = createAt;
        this.expiresAt = expiresAt;
        this.user = user;
    }
}
