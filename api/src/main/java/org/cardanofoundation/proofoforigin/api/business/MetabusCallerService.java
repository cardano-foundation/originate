package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.JobResponse;

public interface MetabusCallerService {
    JobResponse createJob(Object job, Unit.MetabusJobType type, String signature, String pubKey, String group);
    JobResponse createJob(Object job, Unit.MetabusJobType type, String signature);

    /**
     * <p>
     * Create job and push to Metabus
     * </p>
     *
     * @param job       The job info
     * @param type      The metabus's job type
     * @param signature The signature
     * @param pubKey    The public key
     * @return The job response
     */
    JobResponse createJob(final Object job, final Unit.MetabusJobType type, final String signature,
            final String pubKey);
}
