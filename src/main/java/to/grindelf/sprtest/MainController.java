package to.grindelf.sprtest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import to.grindelf.sprtest.auth.UserDaoImpl;
import to.grindelf.sprtest.domain.User;
import to.grindelf.sprtest.exceptions.NoSuchUserException;
import to.grindelf.sprtest.exceptions.UserAlreadyExistsException;
import to.grindelf.sprtest.utils.database.SQLOperator;

import java.io.IOException;

@Controller
public class MainController {

    @RequestMapping("/")
    public RedirectView redirectToLogin() {
        return new RedirectView("/login");
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String name,
            @RequestParam String password
    ) {

        User loginUser = new User(name, password);
        SQLOperator<User, String> operator = new SQLOperator<>();
        UserDaoImpl userDao = new UserDaoImpl(operator);

        try {
            User storedUser = userDao.getUserByName(name);

            if (storedUser.equals(loginUser) && storedUser.isJustUser()) {
                return "home";
            } else if (storedUser.equals(loginUser) && storedUser.isAdmin()) {
                return "home-admin";
            }
        } catch (NoSuchUserException e) {
            throw new RuntimeException(e);
        }

        return "login";
    }

    @GetMapping("/signup")
    public String showSignup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String name,
            @RequestParam String password
    ) {
        User newUser = new User(name, password);
        SQLOperator<User, String> operator = new SQLOperator<>();
        UserDaoImpl userDao = new UserDaoImpl(operator);

        try {
            userDao.save(newUser);
            return "login";
        } catch (UserAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }
}

