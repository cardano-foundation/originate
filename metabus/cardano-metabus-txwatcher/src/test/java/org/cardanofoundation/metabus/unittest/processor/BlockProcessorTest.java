package org.cardanofoundation.metabus.unittest.processor;

import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.model.BlockHeader;
import com.bloxbean.cardano.yaci.core.model.HeaderBody;
import com.bloxbean.cardano.yaci.core.model.TransactionBody;
import com.bloxbean.cardano.yaci.store.events.BlockEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.cardanofoundation.metabus.mapper.BlockMapper;
import org.cardanofoundation.metabus.mapper.BlockMapperImpl;
import org.cardanofoundation.metabus.repos.BlockRepository;
import org.cardanofoundation.metabus.service.TxService;
import org.cardanofoundation.metabus.stores.BlockProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class BlockProcessorTest {

    BlockRepository blockRepository;
    TxService txService;

    BlockMapper blockMapper = new BlockMapperImpl();

    BlockProcessor blockProcessor;

    @BeforeEach
    public void init() {
        blockRepository = mock(BlockRepository.class);
        txService = mock(TxService.class);
        blockProcessor = new BlockProcessor(blockRepository, blockMapper, txService);
    }

    @Test
    void test_blockProcessor() {
        Block block = createTestBlock();
        BlockEvent blockEvent = BlockEvent.builder()
                .block(block)
                .build();
        when(blockRepository.findByBlockNo(ArgumentMatchers.any(Long.class))).thenReturn(Optional.of(BlockJPA.builder().txOnChainHashes("").build()));
        blockProcessor.handleBlockHeaderEvent(blockEvent);

        verify(blockRepository, times(1)).save(ArgumentMatchers.any(BlockJPA.class));
        verify(txService, times(1)).updateTxStates(ArgumentMatchers.any(String.class));
    }

    private Block createTestBlock() {
        return Block.builder()
                .header(BlockHeader.builder()
                        .headerBody(HeaderBody.builder()
                                .blockBodyHash("BlockHash")
                                .blockNumber(0L)
                                .slot(1L)
                                .prevHash("PrevHash")
                                .build())
                        .build())
                .transactionBodies(
                        List.of(
                                TransactionBody.builder()
                                        .txHash("Hash1")
                                        .build(),
                                TransactionBody.builder()
                                        .txHash("Hash2")
                                        .build()
                        )
                )
                .build();
    }

}
