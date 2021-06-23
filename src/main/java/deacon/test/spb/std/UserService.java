package deacon.test.spb.std;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    public String addUser(User user) {
        // 直接编写业务逻辑
        return "success";
    }

}
