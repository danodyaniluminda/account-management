package com.biapay.accountmanagement.criteria;

import java.util.List;
import lombok.Data;

@Data
public class LongFilter {

  private Long equals;
  private List<Long> in;
}
