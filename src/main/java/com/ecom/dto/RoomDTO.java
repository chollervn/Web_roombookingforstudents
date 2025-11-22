package com.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Room information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {

    private Integer id;
    private String roomName;
    private String description;
    private String roomType;
    private Double monthlyRent;
    private String address;
    private String fullAddress;
    private String district;
    private String city;
    private Double area;
    private Integer maxOccupants;
    private String image;

    // Amenities
    private Boolean hasWifi;
    private Boolean hasAirConditioner;
    private Boolean hasParking;
    private Boolean hasElevator;
    private Boolean allowPets;

    // Costs
    private Double deposit;
    private Double electricityCost;
    private Double waterCost;

    // Contact
    private String contactPhone;
    private String contactName;

    // Location
    private Double latitude;
    private Double longitude;

    // Status
    private String status;
    private Boolean isAvailable;
}
