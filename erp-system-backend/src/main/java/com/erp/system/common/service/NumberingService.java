package com.erp.system.common.service;

import com.erp.system.common.entity.NumberingSequence;
import com.erp.system.common.repository.NumberingSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NumberingService {

    private final NumberingSequenceRepository numberingSequenceRepository;

    @Transactional
    public String generateNextNumber(String sequenceName) {
        NumberingSequence sequence = numberingSequenceRepository.findBySequenceNameForUpdate(sequenceName)
                .orElseThrow(() -> new IllegalArgumentException("Sequence not found: " + sequenceName));

        String nextNumber = sequence.generateNextNumber();
        numberingSequenceRepository.save(sequence);
        return nextNumber;
    }

    public String peekNextNumber(String sequenceName) {
        NumberingSequence sequence = numberingSequenceRepository.findBySequenceName(sequenceName)
                .orElseThrow(() -> new IllegalArgumentException("Sequence not found: " + sequenceName));

        Long nextNumber = sequence.getCurrentNumber() + 1;
        String numberStr = String.format("%0" + sequence.getPaddingLength() + "d", nextNumber);
        return (sequence.getPrefix() != null ? sequence.getPrefix() : "") + numberStr;
    }
}