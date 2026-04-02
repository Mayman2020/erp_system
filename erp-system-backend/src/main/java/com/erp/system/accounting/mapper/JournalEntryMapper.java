package com.erp.system.accounting.mapper;

import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.dto.display.JournalEntryDisplayDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = JournalEntryLineMapper.class
)
public interface JournalEntryMapper {

    JournalEntryDisplayDto toDisplay(JournalEntry journalEntry);
}
