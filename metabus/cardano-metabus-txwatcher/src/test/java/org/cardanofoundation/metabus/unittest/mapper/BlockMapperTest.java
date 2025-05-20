package org.cardanofoundation.metabus.unittest.mapper;

import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.model.BlockHeader;
import com.bloxbean.cardano.yaci.core.model.HeaderBody;
import com.bloxbean.cardano.yaci.core.model.TransactionBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.cardanofoundation.metabus.mapper.BlockMapper;
import org.cardanofoundation.metabus.mapper.BlockMapperImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockMapperTest {

    BlockMapper blockMapper = new BlockMapperImpl();

    @Test
    void test_map_block() throws JsonProcessingException {
        Block block = Block.builder()
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
        BlockJPA entity = blockMapper.toEntity(block);

        assertEquals(block.getHeader().getHeaderBody().getBlockHash(), entity.getHash());
        assertEquals(block.getHeader().getHeaderBody().getSlot(), entity.getSlotNo());
        assertEquals(block.getHeader().getHeaderBody().getBlockNumber(), entity.getBlockNo());
        assertEquals(block.getHeader().getHeaderBody().getPrevHash(), entity.getPrevious());

        String txOnChainHashes = entity.getTxOnChainHashes();
        ObjectMapper mapper = new ObjectMapper();
        List<String> list = mapper.readValue(txOnChainHashes, List.class);

        assertEquals(2, list.size());
        assertEquals("Hash1", list.get(0));
        assertEquals("Hash2", list.get(1));

    }
}
