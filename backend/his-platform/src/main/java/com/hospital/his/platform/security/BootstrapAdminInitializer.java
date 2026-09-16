package com.hospital.his.platform.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "his.bootstrap-admin.enabled", havingValue = "true", matchIfMissing = true)
public class BootstrapAdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

    private final AuthUserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String displayName;
    private final String password;

    public BootstrapAdminInitializer(
            AuthUserMapper mapper,
            PasswordEncoder passwordEncoder,
            @Value("${his.bootstrap-admin.username}") String username,
            @Value("${his.bootstrap-admin.display-name}") String displayName,
            @Value("${his.bootstrap-admin.password:}") String password) {
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.displayName = displayName;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (mapper.countUsers() > 0) {
            return;
        }
        if (password == null || password.isBlank()) {
            log.warn("No users exist. Set HIS_BOOTSTRAP_ADMIN_PASSWORD before startup to create the initial administrator.");
            return;
        }
        NewUser user = new NewUser(username, passwordEncoder.encode(password), displayName);
        mapper.insertUser(user);
        Long rootRoleId = mapper.findRootRoleId();
        if (rootRoleId == null) {
            throw new IllegalStateException("ROOT role is missing from the database baseline");
        }
        mapper.insertUserRole(user.getId(), rootRoleId);
        log.info("Created initial administrator account: {}", username);
    }
}
