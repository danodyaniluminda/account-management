package com.biapay.accountmanagement.criteria;

import java.util.List;
import lombok.Data;

@Data
public class StringFilter {

  private String equals;
  private String contains;
  private List<String> in;
}
