package com.biapay.accountmanagement.mapper;

import com.biapay.core.dto.AccountTransactionDTO;
import com.biapay.core.model.AccountTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface AccountTransactionMapper {

  AccountTransactionMapper INSTANCE = Mappers.getMapper(AccountTransactionMapper.class);

  AccountTransactionDTO toAccountTransactionDto(AccountTransaction accountTransaction);
}
