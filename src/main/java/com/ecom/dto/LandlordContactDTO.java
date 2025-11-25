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
    private String bankId; // Bank ID for VietQR (e.g., MB, VCB, TCB)
    private String accountNo; // Bank account number for VietQR

    /**
     * Get display name (fallback to email if name is null)
     */
    public String getDisplayName() {
        return name != null && !name.isEmpty() ? name : email;
    }

    /**
     * Check if bank information is available for VietQR payment
     */
    public boolean hasBankInfo() {
        return bankId != null && !bankId.isEmpty() &&
               accountNo != null && !accountNo.isEmpty();
    }
}
