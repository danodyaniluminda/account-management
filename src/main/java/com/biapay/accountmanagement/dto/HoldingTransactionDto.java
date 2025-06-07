package com.biapay.accountmanagement.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class HoldingTransactionDto {
  private List<String> clientTransactionIds = new ArrayList<>();
}
