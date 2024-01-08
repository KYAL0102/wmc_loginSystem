package at.htlleonding.quarkus.services;

import at.htlleonding.quarkus.modules.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
@Transactional
public class UserService {

    private final String PEPPER = "tFxzsGeeTqXCnp9dJSoezM6OlgdwtQ";
    @Inject
    EntityManager entityManager;

    @Transactional
    public void saveUser(User user){
        user.setPassword(HashFunctions.hashPassword(user.getPassword(), user.getSalt(), PEPPER));
        this.entityManager.persist(user);
    }

    public void requestPasswordReset(String username){
        try {
            User user = this.getUserByUsername(username);
            String otp = ResetService.generateOTP();

            String smsText = String.format("Hey %s! \uD83D\uDC4B\n" +
                    "\n" +
                    "Hope you're doing well! It looks like you need to reset your password. No worries, we've got you covered. \uD83D\uDEE1\uFE0F\n" +
                    "\n" +
                    "To reset your password, you need this OTP:\n" +
                    "%s\n" +
                    "\n" +
                    "If you didn't request this, don't worry, your account is safe with us. Just ignore this message.\n" +
                    "\n" +
                    "Best regards,\n" +
                    "Test Company", username, otp);

            user.setOtp(otp);
            Globals.LAST_OTP = otp;
            saveUser(user);
            ResetService.writeToFile(smsText);
        }
        catch(NotFoundException e){
            return;
        }
    }

    private User checkOTP(String otp, String telephoneNumber){
        User user;
        try {
            var query = this.entityManager.createQuery("select u from User u where u.telephoneNumber = ?1 and u.otp = ?2", User.class);
            query.setParameter(1, telephoneNumber);
            query.setParameter(2, otp);
            user = query.getSingleResult();
        }
        catch(NoResultException e) {
            throw new NotFoundException("OTP/TelephoneNumber not valid.");
        }
        return user;
    }

    public boolean resetPassword(String otp, String telephoneNumber, String password){
        try{
            User user = checkOTP(otp, telephoneNumber);
            user.setPassword(password);
            saveUser(user);
            return true;
        }
        catch(NotFoundException e){
            throw e;
        }
    }

    public User checkPasswordForUser(String username, String password){
        User user;
        try {
            var query = this.entityManager.createQuery("select u from User u where u.username = ?1", User.class);
            query.setParameter(1, username);
            user = query.getSingleResult();
        }
        catch(NoResultException e){
            throw new NotFoundException("User was not found");
        }

        if (HashFunctions.checkPassword(password, user.getSalt(), PEPPER, user.getPassword())) {
            return user;
        }
        throw new NotFoundException("Password incorrect");
    }

    public User getUserByUsername(String username){

        var query = this.entityManager.createQuery("select u from User u where u.username = ?1", User.class);
        query.setParameter(1, username);
        User user =  query.getSingleResult();
        if(user != null){
            return user;
        }
        throw new NotFoundException("User was not found");
    }
}
