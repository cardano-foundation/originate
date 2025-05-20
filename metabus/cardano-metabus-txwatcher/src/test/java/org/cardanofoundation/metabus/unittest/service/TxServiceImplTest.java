package org.cardanofoundation.metabus.unittest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.json.JsonParser;
import org.cardanofoundation.metabus.mapper.JobMapper;
import org.cardanofoundation.metabus.mapper.JobMapperImpl;
import org.cardanofoundation.metabus.repos.JobRepository;
import org.cardanofoundation.metabus.repos.UnconfirmedTxRepository;
import org.cardanofoundation.metabus.service.JobService;
import org.cardanofoundation.metabus.service.impl.JobServiceImpl;
import org.cardanofoundation.metabus.service.impl.TxServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class TxServiceImplTest {
    UnconfirmedTxRepository unconfirmedTxRepository;
    JobService jobService;
    JobMapper jobMapper;
    JobRepository jobRepository;
    JsonParser jsonParser;
    TxServiceImpl txService;

    @BeforeEach
    public void init() {
        unconfirmedTxRepository = mock(UnconfirmedTxRepository.class);
        jobService = mock(JobServiceImpl.class);
        jobMapper = mock(JobMapperImpl.class);
        jobRepository = mock(JobRepository.class);
        jsonParser = mock(JsonParser.class);
        txService = new TxServiceImpl(unconfirmedTxRepository, jobService, jobMapper, jobRepository, jsonParser);
    }
    @Test
    void test_updateTxStates_in_state_storage_and_redis() {
        String onChainTransactionsInput = "[hash1,hash2]";
        List<String> onChainTranList = List.of("hash1", "hash2");


        Long unconfirmedTxJPA1Id = 1L;
        Long unconfirmedTxJPA2Id = 2L;
        UnconfirmedTxJPA unconfirmedTxJPA1 = UnconfirmedTxJPA.builder().id(unconfirmedTxJPA1Id).txHash("txHash1").build();
        UnconfirmedTxJPA unconfirmedTxJPA2 = UnconfirmedTxJPA.builder().id(unconfirmedTxJPA2Id).txHash("txHash2").build();
        List<UnconfirmedTxJPA> unconfirmedTxJPAs = List.of(unconfirmedTxJPA1, unconfirmedTxJPA2);

        JobJPA jobJPA1 = new JobJPA().toBuilder().id(1L).build();
        JobJPA jobJPA2 = new JobJPA().toBuilder().id(2L).build();
        Job job1 = Job.builder().id(1L).build();
        Job job2 = Job.builder().id(2L).build();
        List<JobJPA> jobJPAList = List.of(jobJPA1, jobJPA2);

        when(jobRepository.findAllByUnconfirmedTxId(eq(unconfirmedTxJPA1Id))).thenReturn(List.of(jobJPA1));
        when(jobRepository.findAllByUnconfirmedTxId(eq(unconfirmedTxJPA2Id))).thenReturn(List.of(jobJPA2));
        when(jobMapper.toJob(eq(jobJPA1))).thenReturn(job1);
        when(jobMapper.toJob(eq(jobJPA2))).thenReturn(job2);
        doNothing().when(jobService).pushJobToRabbit(any(Job.class));

        when(jsonParser.parseJsonStringToObject(eq(onChainTransactionsInput), any(TypeReference.class))).thenReturn(onChainTranList);
        when(unconfirmedTxRepository.findAllByTxHashIn(anyList())).thenAnswer(invocationOnMock -> {
            List<String> onChainTxHashesArgument = invocationOnMock.getArgument(0);
            Assertions.assertEquals(onChainTxHashesArgument.get(0), onChainTranList.get(0));
            Assertions.assertEquals(onChainTxHashesArgument.get(1), onChainTranList.get(1));
            return unconfirmedTxJPAs;
        });
        when(jobRepository.saveAll(anyList())).thenAnswer(invocationOnMock -> {
            List<JobJPA> jobJPASsaved = invocationOnMock.getArgument(0);
            Assertions.assertEquals(jobJPA1, jobJPASsaved.get(0));
            Assertions.assertEquals(jobJPA2, jobJPASsaved.get(1));
            return jobJPAList;
        });

        txService.updateTxStates(onChainTransactionsInput);

        // Assert
        verify(jobService, times(2)).pushJobToRabbit(any(Job.class));
    }

    @Test
    void test_updateTxStates_in_state_storage_only() {

        String onChainTransactionsInput = "[hash1,hash2]";
        List<String> onChainTranList = List.of("hash1", "hash2");

        Long unconfirmedTxJPA1Id = 1L;
        Long unconfirmedTxJPA2Id = 2L;
        UnconfirmedTxJPA unconfirmedTxJPA1 = UnconfirmedTxJPA.builder().id(unconfirmedTxJPA1Id).txHash("txHash1").build();
        UnconfirmedTxJPA unconfirmedTxJPA2 = UnconfirmedTxJPA.builder().id(unconfirmedTxJPA2Id).txHash("txHash2").build();
        List<UnconfirmedTxJPA> unconfirmedTxJPAs = List.of(unconfirmedTxJPA1, unconfirmedTxJPA2);

        JobJPA jobJPA1 = new JobJPA().toBuilder().id(1L).build();
        JobJPA jobJPA2 = new JobJPA().toBuilder().id(2L).build();
        Job job1 = Job.builder().id(1L).build();
        Job job2 = Job.builder().id(2L).build();
        List<JobJPA> jobJPAList = List.of(jobJPA1, jobJPA2);

        when(jobRepository.findAllByUnconfirmedTxId(eq(unconfirmedTxJPA1Id))).thenReturn(List.of(jobJPA1));
        when(jobRepository.findAllByUnconfirmedTxId(eq(unconfirmedTxJPA2Id))).thenReturn(List.of(jobJPA2));
        when(jobMapper.toJob(eq(jobJPA1))).thenReturn(job1);
        when(jobMapper.toJob(eq(jobJPA2))).thenReturn(job2);
        doNothing().when(jobService).pushJobToRabbit(any(Job.class));

        when(jsonParser.parseJsonStringToObject(eq(onChainTransactionsInput), any(TypeReference.class))).thenReturn(onChainTranList);
        when(unconfirmedTxRepository.findAllByTxHashIn(anyList())).thenAnswer(invocationOnMock -> {
            List<String> onChainTxHashesArgument = invocationOnMock.getArgument(0);
            Assertions.assertEquals(onChainTxHashesArgument.get(0), onChainTranList.get(0));
            Assertions.assertEquals(onChainTxHashesArgument.get(1), onChainTranList.get(1));
            return unconfirmedTxJPAs;
        });
        when(jobRepository.saveAll(anyList())).thenAnswer(invocationOnMock -> {
            List<JobJPA> jobJPASsaved = invocationOnMock.getArgument(0);
            Assertions.assertEquals(jobJPA1, jobJPASsaved.get(0));
            Assertions.assertEquals(jobJPA2, jobJPASsaved.get(1));
            return jobJPAList;
        });
        txService.updateTxStates(onChainTransactionsInput);

        // Assert
        verify(jobService, times(2)).pushJobToRabbit(any(Job.class));
    }

    @Test
    void test_updateTxStates_failed_cause_input_empty() {
        String onChainTransactionsInput = "";

        txService.updateTxStates(onChainTransactionsInput);

        verify(jobService, times(0)).pushJobToRabbit(any(Job.class));
        verify(jobRepository,times(0)).saveAll(any());
        verify(unconfirmedTxRepository, times(0)).saveAll(anyList());
    }
}
