package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_name", length = 50, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
