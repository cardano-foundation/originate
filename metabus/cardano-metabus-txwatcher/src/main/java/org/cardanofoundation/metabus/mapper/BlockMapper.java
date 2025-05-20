package org.cardanofoundation.metabus.mapper;

import com.bloxbean.cardano.client.util.JsonUtil;
import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.model.TransactionBody;
import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;


@Mapper(config = BaseMapper.class)
public interface BlockMapper {

    @Mapping(target = "hash", source = "header.headerBody.blockHash")
    @Mapping(target = "blockNo", source = "header.headerBody.blockNumber")
    @Mapping(target = "slotNo", source = "header.headerBody.slot")
    @Mapping(target = "previous", source = "header.headerBody.prevHash")
    @Mapping(target = "txOnChainHashes", source = "transactionBodies", qualifiedByName = "transactionBodyHashesToString")
    BlockJPA toEntity(Block block);

    @Named("transactionBodyHashesToString")
    default String transactionBodyHashesToString(List<TransactionBody> transactionBodies) {
        List<String> onChainTxHashes = transactionBodies.stream().map(TransactionBody::getTxHash).toList();
        String txOnChainHashes;
        if(onChainTxHashes == null || onChainTxHashes.isEmpty()) {
            txOnChainHashes = "";
        } else {
            txOnChainHashes = JsonUtil.getPrettyJson(onChainTxHashes);
        }
        return txOnChainHashes;
    }

}
