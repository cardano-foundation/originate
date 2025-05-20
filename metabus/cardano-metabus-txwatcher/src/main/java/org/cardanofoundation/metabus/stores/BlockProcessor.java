package org.cardanofoundation.metabus.stores;

import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.store.events.BlockEvent;
import com.bloxbean.cardano.yaci.store.events.RollbackEvent;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.common.entities.BlockJPA;
import org.cardanofoundation.metabus.mapper.BlockMapper;
import org.cardanofoundation.metabus.repos.BlockRepository;
import org.cardanofoundation.metabus.service.TxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockProcessor {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;
    private final TxService txService;

    @Value("${rollback.threshold}")
    private int rollbackThreshold;

    @EventListener
    @Order(1)
    @Transactional
    public void handleBlockHeaderEvent(@NonNull BlockEvent blockEvent) {
        Block block = blockEvent.getBlock();

        BlockJPA entity = blockMapper.toEntity(block);
        boolean isExists = blockRepository.existsBlockByHash(entity.getHash());
        if(isExists) {
            log.debug("Skip existed block : number {}, hash {}", entity.getBlockNo(), entity.getHash());
            return;
        }
        blockRepository.save(entity);

        // Process business logic in the block target
        Optional<BlockJPA> targergetBlock = blockRepository.findByBlockNo(entity.getBlockNo() - rollbackThreshold);
        targergetBlock.ifPresent(blockJPA -> txService.updateTxStates(blockJPA.getTxOnChainHashes()));
    }

    @EventListener
    @Order(1)
    @Transactional
    public void handleRollbackEvent(@NotNull RollbackEvent rollbackEvent) {
        int count = blockRepository.deleteBySlotNoGreaterThan(rollbackEvent.getRollbackTo().getSlot());

        log.info("Rollback -- {} block records", count);
    }
}
