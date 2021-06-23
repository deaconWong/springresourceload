package deacon.test.spb.std;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostMapping("/addUser")
    public String addUser(@RequestBody @Valid User user ) {
        return userService.addUser(user);
    }
    
    @GetMapping("/getUser")
    public ResultVO<User> getUser() {
        User user = new User();
        user.setId(1L);
        user.setAccount("12345678");
        user.setPassword("12345678");
        user.setEmail("123@qq.com");
        
        return new ResultVO<>(user);
    }

}
