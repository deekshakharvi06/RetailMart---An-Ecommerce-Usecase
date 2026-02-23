package com.ecommerce.seller_service.model;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @Enumerated(EnumType.STRING)
    private String role;

    @NotBlank(message="This field is Required")
    private String location;

    // @NotBlank(message="This field is Required")
    @Column(unique = true)
    private String username;

    @NotBlank(message="This field is Required")
    // @NotNull(message="This field is required")
    @Length(min=8)
    @Pattern( regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
         message = "Password must contain at least 8 characters, including uppercase & lowercase, number, and special character")
    private String password;

}
