package com.synkra.crm.config;

import com.synkra.crm.model.AppUser;
import com.synkra.crm.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppUserBootstrapper implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean bootstrapEnabled;
    private final String username;
    private final String password;

    public AppUserBootstrapper(AppUserRepository appUserRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.security.bootstrap-enabled:true}") boolean bootstrapEnabled,
                               @Value("${app.security.username}") String username,
                               @Value("${app.security.password}") String password) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEnabled = bootstrapEnabled;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled || username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }

        AppUser appUser = appUserRepository.findByUsername(username).orElseGet(AppUser::new);
        appUser.setUsername(username);
        appUser.setPasswordHash(passwordEncoder.encode(password));
        appUser.setRoles("CRM_USER");
        appUser.setEnabled(true);
        appUserRepository.save(appUser);
    }
}
