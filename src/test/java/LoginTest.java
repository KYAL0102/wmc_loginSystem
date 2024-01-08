import at.htlleonding.quarkus.modules.User;
import at.htlleonding.quarkus.services.Globals;
import at.htlleonding.quarkus.services.UserService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.apache.derby.shared.common.sanity.AssertFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LoginTest {

    private final String TELEPHONE_NUMBER = "+87 651 8461874";
    private final String USERNAME = "John Doe";

    @Inject
    UserService service;

    public void createUser(){
        User user = new User(USERNAME, "password1234!", TELEPHONE_NUMBER);
        service.saveUser(user);
    }

    @Test
    @TestTransaction
    public void getExistingUser(){
        createUser();

        try {
            User user = service.checkPasswordForUser(USERNAME, "password1234!");
        }
        catch(NotFoundException e){
            throw new AssertFailure();
        }
    }

    @Test
    @TestTransaction
    public void getNonExistingUser(){
        createUser();

        Assertions.assertThrows(NotFoundException.class, () -> {
            service.checkPasswordForUser("John Rose", "password1234!");
        });
    }

    @Test
    @TestTransaction
    public void getExistingUserWithWrongPwd(){
        createUser();

        Assertions.assertThrows(NotFoundException.class, () -> {
            service.checkPasswordForUser(USERNAME, "passwort123?");
        });
    }

    @Test
    @TestTransaction
    public void changePassword(){
        createUser();

        service.requestPasswordReset(USERNAME);
        service.resetPassword(Globals.LAST_OTP, TELEPHONE_NUMBER, "tree");

        try {
            User user = service.checkPasswordForUser(USERNAME, "tree");
        }
        catch(NotFoundException e){
            throw new AssertFailure();
        }
    }

    @Test
    @TestTransaction
    public void changePasswordButThenOldPassword(){
        createUser();

        service.requestPasswordReset(USERNAME);
        service.resetPassword(Globals.LAST_OTP, TELEPHONE_NUMBER, "tree");

        Assertions.assertThrows(NotFoundException.class, () -> {
            service.checkPasswordForUser(USERNAME, "password1234!");
        });
    }

    @Test
    @TestTransaction
    public void changePasswordButWrongPhoneNumber(){
        createUser();

        service.requestPasswordReset(USERNAME);

        Assertions.assertThrows(NotFoundException.class, () -> {
            service.resetPassword(Globals.LAST_OTP, "+48 451 251487", "tree");
        });

        Assertions.assertThrows(NotFoundException.class, () -> {
            service.checkPasswordForUser(USERNAME, "tree");
        });
    }
}
