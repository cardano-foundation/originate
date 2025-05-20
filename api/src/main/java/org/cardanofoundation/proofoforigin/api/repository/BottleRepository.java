package org.cardanofoundation.proofoforigin.api.repository;

import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BottleRepository extends JpaRepository<Bottle, String> {

    List<Bottle> getBottlesByWineryId(String wineryId);

    List<Bottle> getBottlesByWineryIdAndLotId(String wineryId, String lotId);

    List<Bottle> findByLotId(@Param("lotId") String lotId);

    Optional<Bottle> findByWineryIdAndId(String wineryId, String id);

    List<Bottle> findByLotIdAndIdIn(String lotId, Set<String> ids);

    List<Bottle> findByCertificateIdAndIdIn(String certId, Set<String> ids);

    List<Bottle> findByIdInAndCertificateIdNotNull(Set<String> bottleIds);

    /**
     * <p>
     * get list of the bottle by wineryId, lotId and certificateId
     * </p>
     *
     * @param wineryId         The wineryId
     * @param lotId            The lotId
     * @param certificateId    The certificate Id
     * @param certUpdateStatus The cert update status
     * @return The list of bottle
     */
    List<Bottle> getBottlesByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatus(final String wineryId,
            final String lotId, final String certificateId, final CertUpdateStatus certUpdateStatus);

    /**
     * <p>
     * get list of the bottle by wineryId, lotId and certificateId
     * </p>
     *
     * @param wineryId         The wineryId
     * @param lotId            The lotId
     * @param certificateId    The certificate Id
     * @param certUpdateStatus The cert update status to exclude
     * @return The list of bottle
     */
    List<Bottle> findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot(final String wineryId, final String lotId, final String certificateId, final CertUpdateStatus certUpdateStatus);

     /**
     * <p>
     * get list of the bottle by certificateId
     * </p>
     *
     * @param certificateId    The certificate Id
     * @return The list of bottle that linked to certificateId
     */
    List<Bottle> findAllByCertificateId(final String certificateId);

    /**
     * <p>
     * Update cert update status of the bottles.
     * </p>
     *
     * @param status The update status
     * @param idList The id list
     * @return Number of record update.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true) 
    @Transactional
    @Query("update Bottle bottle set bottle.certUpdateStatus = :status where bottle.id in :ids")
    public int updateCertUpdateStatusOfBottles(@Param("status") CertUpdateStatus status,
            @Param("ids") List<String> idList);

    @Modifying
    @Query("UPDATE Bottle b SET b.lotUpdateStatus = :lotUpdateStatus WHERE b.id = :id")
    void updateLotUpdateStatusById(@Param("id") String id, @Param("lotUpdateStatus") Integer lotUpdateStatus);

    boolean existsByLotIdAndWineryId(String lotId, String wineryId);

    List<Bottle> findBySequentialNumberBetween(Integer startSN, Integer endSN);

    List<Bottle> findByIdIn(List<String> ids);

}
