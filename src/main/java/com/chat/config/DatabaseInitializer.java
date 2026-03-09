package com.chat.config;

import com.chat.model.User;
import com.chat.repository.UserRepository;
import com.chat.util.DefaultAvatarUtil;
import com.chat.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserRepository userRepository;

    @Autowired
    public DatabaseInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        try {
            long userCount = userRepository.count();
            
            if (userCount == 0) {
                log.info("初始化默认管理员账户...");

                User admin = new User.Builder()
                        .userId(IdGenerator.generateNumericId())
                        .username("admin")
                        .password("a1653611988")
                        .isOnline(false)
                        .isAdmin(true)
                        .isMuted(false)
                        .avatar(DefaultAvatarUtil.randomDefaultAvatar())
                        .build();

                userRepository.save(admin);
                log.info("默认管理员账户创建成功 - 用户名: admin, 密码: a1653611988");
            }

            log.info("数据库初始化完成，当前用户数: {}", userRepository.count());
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
        }
    }
}
