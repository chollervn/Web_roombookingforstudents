package com.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Landlord Contact information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandlordContactDTO {

    private Integer ownerId;
    private String name;
    private String email;
    private String mobileNumber;
    private String profileImage;

    /**
     * Get display name (fallback to email if name is null)
     */
    public String getDisplayName() {
        return name != null && !name.isEmpty() ? name : email;
    }
}
