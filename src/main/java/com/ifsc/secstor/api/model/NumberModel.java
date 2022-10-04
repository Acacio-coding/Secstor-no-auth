package com.ifsc.secstor.api.model;

import lombok.*;
import org.hibernate.annotations.SQLInsert;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;

@Entity
@Table(name = "tb_pvss_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NumberModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupPrimeOrder;

    @Column(nullable = false, unique = true)
    private String g1;

    @Column(nullable = false, unique = true)
    private String g2;

    @Column(nullable = false, unique = true)
    private String secret;
}
