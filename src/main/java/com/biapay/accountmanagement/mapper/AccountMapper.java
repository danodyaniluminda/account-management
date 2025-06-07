package com.biapay.accountmanagement.mapper;

import com.biapay.accountmanagement.dto.AccountRes;
import com.biapay.core.dto.AccountDTO;
import com.biapay.core.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface AccountMapper {

  AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

  AccountDTO toDto(Account account);

  Account toEntity(AccountDTO accountDTO);

  AccountRes toAccountRes(Account account);
}
