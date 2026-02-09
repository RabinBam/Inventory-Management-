package org.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data // From Lombok in your pom.xml
public class InventoryItem {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private Double price;
    private String warehouse;
    private Integer stock;
}