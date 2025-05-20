package org.cardanofoundation.metabus.services.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.controllers.BaseResponse;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.cardanofoundation.metabus.services.JobProducerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)

public class JobProducerServiceImpl implements JobProducerService {
    WebClient jobProducerWebClient;

    public JobProducerServiceImpl(@Lazy WebClient jobProducerWebClient) {
        this.jobProducerWebClient = jobProducerWebClient;
    }

    @Override
    public BaseResponse<String> createJob(Job job) {
        try {
            return jobProducerWebClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/jobs").build())
                    .body(Mono.just(job), Job.class)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<String>>() {
                    })
                    .block();
        } catch (MetabusException metabusException) {
            throw metabusException;
        } catch (Exception e) {
            throw new MetabusException(MetabusErrors.ERROR_CREATING_JOB, e.getMessage());
        }
    }
}
