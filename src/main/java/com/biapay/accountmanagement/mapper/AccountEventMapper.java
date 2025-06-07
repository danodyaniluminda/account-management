package com.biapay.accountmanagement.mapper;

import com.biapay.core.dto.AccountDTO;
import com.biapay.core.dto.AccountEventDTO;
import com.biapay.core.dto.AccountTransactionDTO;
import com.biapay.core.model.Account;
import com.biapay.core.model.AccountEvent;
import com.biapay.core.model.AccountTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface AccountEventMapper {

  AccountEventMapper INSTANCE = Mappers.getMapper(AccountEventMapper.class);

  AccountEventDTO toAccountEventDto(AccountEvent accountEvent);

  AccountDTO toAccountDto(Account account);

  AccountTransactionDTO toAccountTransactionDto(AccountTransaction accountTransaction);
}
