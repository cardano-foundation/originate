package org.cardanofoundation.proofoforigin.api.repository;

import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WineryRepository extends JpaRepository<Winery, String> {

    Optional<Winery> findByWineryId(String wineryId);

    @Query("SELECT w from Winery w ORDER BY LPAD(wineryId, 4, '0') DESC LIMIT 1")
    Optional<Winery> findTopByOrderByWineryIdLPadDesc();

    @Query("SELECT w from Winery w ORDER BY LPAD(wineryId, 4, '0')")
    List<Winery> findByOrderByWineryIdLPad();

    Optional<Winery> findFirstByKeycloakUserId(String keycloakUserId);
}
