package com.csye6225.webapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "health_check")
@Getter
@Setter
@NoArgsConstructor
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @Column(name = "datetime", nullable = false)
    private OffsetDateTime datetime = OffsetDateTime.now();
}
