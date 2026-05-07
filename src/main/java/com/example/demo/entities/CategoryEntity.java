package com.example.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mst_categories")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity extends BaseEntity {
    private String name;
}
