package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean active;

    // For admin user listings
}