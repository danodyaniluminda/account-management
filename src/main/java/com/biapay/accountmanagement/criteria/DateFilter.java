package com.biapay.accountmanagement.criteria;

import java.time.LocalDate;
import lombok.Data;

@Data
public class DateFilter {

  private LocalDate equals;
  private LocalDate lessThan;
  private LocalDate greaterThan;
}
