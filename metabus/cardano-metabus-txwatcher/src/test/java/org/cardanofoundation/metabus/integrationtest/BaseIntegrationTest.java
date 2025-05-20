package org.cardanofoundation.metabus.integrationtest;

import org.cardanofoundation.metabus.repos.BlockRepository;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("it")
public abstract class BaseIntegrationTest {
    @Autowired
    protected UnconfirmedTxRepository unconfirmedTxRepository;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected BlockRepository blockRepository;
    @Autowired
    protected UtxoRepository utxoRepository;
    @BeforeEach
    public void cleanUp(){
        clearDatabase();
    }

    private void clearDatabase() {
        jobRepository.deleteAll();
        utxoRepository.deleteAll();
        unconfirmedTxRepository.deleteAll();
        blockRepository.deleteAll();
    }
}
