package com.nexoralabs.navalhahub.customer.entity;

import com.nexoralabs.navalhahub.barbershop.entity.embeddables.PhoneNumber;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Customer {

    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private PhoneNumber phoneNumber;
    private boolean active;
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastModifiedDate;
}
