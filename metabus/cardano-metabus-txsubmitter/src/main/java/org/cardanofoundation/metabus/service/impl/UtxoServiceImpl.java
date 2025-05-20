package org.cardanofoundation.metabus.service.impl;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.common.CardanoConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.metabus.common.entities.UnconfirmedTxJPA;
import org.cardanofoundation.metabus.common.entities.UtxoJPA;
import org.cardanofoundation.metabus.repos.UtxoRepository;
import org.cardanofoundation.metabus.service.LocalNodeService;
import org.cardanofoundation.metabus.service.UtxoService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UtxoServiceImpl implements UtxoService {
    LocalNodeService localNodeService;
    UtxoRepository utxoRepository;

    @Override
    public List<Utxo> getUnusedUtxosSortByAmount(Address address) {
        List<Utxo> utxosFromNode = localNodeService.queryUTXOs(address);
        // Filter used utxos of the previous transaction that hasn't been update on-chain yet
        List<String> txHashes = utxosFromNode.stream()
                .map(Utxo::getTxHash)
                .collect(Collectors.toList());

        List<UtxoJPA> usedUtxosFromDb = utxoRepository.findAllByTxHashIn(txHashes);
        List<Utxo> unusedUtxos = filterUnusedUtxos(utxosFromNode, usedUtxosFromDb);

        return unusedUtxos;
    }

    @Override
    public Optional<Utxo> getGreatestUtxo(List<Utxo> utxos) {
        return utxos.stream().max((utxo, utxo2) -> {
            var utxoAmount = utxo.getAmount().stream().map(Amount::getQuantity)
                    .reduce(BigInteger.ZERO, BigInteger::add);
            var utxoAmount2 = utxo2.getAmount().stream().map(Amount::getQuantity)
                    .reduce(BigInteger.ZERO, BigInteger::add);
            return utxoAmount.compareTo(utxoAmount2);
        });
    }

    @Override
    public void saveUsedUtxo(List<Utxo> usedUtxos, Address address, UnconfirmedTxJPA unconfirmedTxJPA) {
        List<UtxoJPA> listUtxoJPAS = usedUtxos.stream().map(usedUtxo->UtxoJPA.builder()
                .txHash(usedUtxo.getTxHash())
                .outputIndex((long) usedUtxo.getOutputIndex())
                .lovelace(usedUtxo.getAmount().get(0).getQuantity())
                .unconfirmedTx(unconfirmedTxJPA)
                .address(address.getAddress())
                .build()).collect(Collectors.toList());
        utxoRepository.saveAll(listUtxoJPAS);
    }

    private List<Utxo> filterUnusedUtxos(List<Utxo> utxosFromNode, List<UtxoJPA> usedUTxosFromDB) {
        return utxosFromNode.stream().filter(utxo ->
                        usedUTxosFromDB.stream()
                                .noneMatch(utxoJPA -> utxoJPA.getOutputIndex() == utxo.getOutputIndex() &&
                                        utxoJPA.getTxHash().equals(utxo.getTxHash())
                                )
                )
                .sorted((utxo, utxo2) -> {
                    var utxoAmount = utxo.getAmount().stream()
                            .filter(amount -> amount.getUnit().equals(CardanoConstants.LOVELACE))
                            .map(Amount::getQuantity)
                            .reduce(BigInteger.ZERO, BigInteger::add);
                    var utxoAmount2 = utxo2.getAmount().stream()
                            .filter(amount -> amount.getUnit().equals(CardanoConstants.LOVELACE))
                            .map(Amount::getQuantity)
                            .reduce(BigInteger.ZERO, BigInteger::add);
                    return utxoAmount2.compareTo(utxoAmount);
                })
                .collect(Collectors.toList());
    }
}
