package my.self.test.bean;

import my.self.spring.annotation.Scope;
import my.self.spring.annotation.Service;

@Service
@Scope("prototype")
public class UserService {
}
