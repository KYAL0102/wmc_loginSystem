package at.htlleonding.quarkus.modules;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.UUID;
@Entity
@Table(name = "user")
@NoArgsConstructor
@Getter
public class User {
    private final String Salt = User.generateSecureSalt();

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String telephoneNumber;

    private String otp = "";

    public User(String username, String password, String telephoneNumber) {
        this.username = username;
        this.password = password;
        this.telephoneNumber = telephoneNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public void setOtp(String otp){
        this.otp = otp;
    }

    private static String generateSecureSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
