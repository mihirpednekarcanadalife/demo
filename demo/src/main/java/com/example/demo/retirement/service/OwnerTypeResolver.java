package com.example.demo.retirement.service;

import com.example.demo.retirement.models.OwnerType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves whether a case owner belongs to the CLE book of business.
 *
 * <p>Owners are CLE when they are listed in {@code retirement.cle.owners} or when the owner
 * value is namespaced with a {@code CLE} prefix (e.g. {@code CLE-TEAM-1}). Everything else,
 * including a missing owner, is treated as NON-CLE.</p>
 */
@Component
public class OwnerTypeResolver {

    private final Set<String> cleOwners;

    public OwnerTypeResolver(@Value("${retirement.cle.owners:CLE}") String configuredCleOwners) {
        this.cleOwners = Arrays.stream(configuredCleOwners.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public OwnerType resolve(String owner) {
        if (owner == null || owner.isBlank()) {
            return OwnerType.NON_CLE;
        }

        String normalized = owner.trim().toUpperCase(Locale.ROOT);

        if (cleOwners.contains(normalized)) {
            return OwnerType.CLE;
        }
        if (normalized.startsWith("CLE-") || normalized.startsWith("CLE_") || normalized.startsWith("CLE ")) {
            return OwnerType.CLE;
        }
        return OwnerType.NON_CLE;
    }
}