package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "registrar")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Registrar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrarId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
