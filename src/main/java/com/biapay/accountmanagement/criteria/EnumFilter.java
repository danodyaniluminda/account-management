package com.biapay.accountmanagement.criteria;

import java.util.List;
import lombok.Data;

@Data
public class EnumFilter<T> {

  private T equals;
  private List<T> in;
}
