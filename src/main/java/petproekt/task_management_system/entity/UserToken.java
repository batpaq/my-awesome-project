package petproekt.task_management_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import petproekt.task_management_system.enm.TokenType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserApp user;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private boolean revoked = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public UserToken(String token, UserApp user, TokenType tokenType) {
        this.token = token;
        this.user = user;
        this.tokenType = tokenType;
        this.revoked = false;
    }
}
