package com.agrocontrol.backend.iam.application.internal.commandservices;
import org.springframework.cloud.stream.function.StreamBridge;
import com.agrocontrol.backend.iam.domain.model.events.UserRegisteredEvent;
import com.agrocontrol.backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.agrocontrol.backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.agrocontrol.backend.iam.domain.model.aggregates.User;
import com.agrocontrol.backend.iam.domain.model.commands.SignInCommand;
import com.agrocontrol.backend.iam.domain.model.commands.SignUpAgriculturalProducerCommand;
import com.agrocontrol.backend.iam.domain.model.commands.SignUpDistributorCommand;
import com.agrocontrol.backend.iam.domain.model.entities.Role;
import com.agrocontrol.backend.iam.domain.model.valueobjects.Roles;
import com.agrocontrol.backend.iam.domain.services.UserCommandService;
import com.agrocontrol.backend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.agrocontrol.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final StreamBridge streamBridge; // <--- Inyectar esto

    public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                                  HashingService hashingService, TokenService tokenService, StreamBridge streamBridge
                                  ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.streamBridge = streamBridge;
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email());
        if (user.isEmpty())
            throw new RuntimeException("User not found");
        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");
        var token = tokenService.generateToken(user.get().getEmail());
        return Optional.of(ImmutablePair.of(user.get(), token));
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpAgriculturalProducerCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already exists");

        // Buscar el rol de desarrollador en el repositorio de roles
        Role agriculturalProducerRole = roleRepository.findByName(Roles.valueOf("ROLE_AGRICULTURAL_PRODUCER"))
                .orElseThrow(() -> new RuntimeException("Agricultural Producer role not found"));

        // Crear una lista con el rol de desarrollador
        List<Role> roles = List.of(agriculturalProducerRole);

        // Crear el usuario con el rol de agricultural producer
        var user = new User(command.email(), hashingService.encode(command.password()), roles);
        userRepository.save(user);


        var event = new UserRegisteredEvent(
                user.getId(),
                command.email(),
                command.fullName(),
                "AGRICULTURAL_PRODUCER",
                command.city(),
                command.country(),
                command.phone(),
                command.dni(),
                null, // RUC
                null  // Company Name
        );

        streamBridge.send("userRegistered-out-0", event);

        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpDistributorCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already exists");

        // Buscar el rol de desarrollador en el repositorio de roles
        Role distributorRole = roleRepository.findByName(Roles.valueOf("ROLE_DISTRIBUTOR"))
                .orElseThrow(() -> new RuntimeException("Distributor role not found"));

        // Crear una lista con el rol de desarrollador
        List<Role> roles = List.of(distributorRole);

        // Crear el usuario con el rol de distribuidor
        var user = new User(command.email(), hashingService.encode(command.password()), roles);
        userRepository.save(user);

        var event = new UserRegisteredEvent(
                user.getId(),
                command.email(),
                command.fullName(),
                "DISTRIBUTOR",
                command.city(),
                command.country(),
                command.phone(),
                null, // DNI
                command.ruc(),
                command.companyName()
        );

        streamBridge.send("userRegistered-out-0", event);

        return Optional.of(user);
    }
}