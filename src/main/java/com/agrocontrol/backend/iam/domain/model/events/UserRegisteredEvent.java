package com.agrocontrol.backend.iam.domain.model.events;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String fullName,
        String role, // "AGRICULTURAL_PRODUCER" o "DISTRIBUTOR"
        // Datos específicos del perfil:
        String city,
        String country,
        String phone,
        String dni, // Puede ser null si es distribuidor
        String ruc, // Puede ser null si es productor
        String companyName // Puede ser null si es productor
) {}