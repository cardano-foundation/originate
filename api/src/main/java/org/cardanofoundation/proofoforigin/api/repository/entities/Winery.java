package org.cardanofoundation.proofoforigin.api.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Winery")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Winery {
    @Id
    @Column(name = "winery_id", nullable = false)
    private String wineryId;

    @Column(name = "keycloak_user_id", nullable = false)
    private String keycloakUserId;

    @Column(name = "winery_name", nullable = false)
    private String wineryName;

    @Column(name = "winery_rs_code")
    private String wineryRsCode;

    @Column(name = "private_key")
    private String privateKey;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "salt")
    private String salt;
}

