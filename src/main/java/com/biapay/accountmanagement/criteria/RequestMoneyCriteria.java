package com.biapay.accountmanagement.criteria;


import com.biapay.core.model.UserType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestMoneyCriteria {

  private RequestorUserTypeFilter requestorUserType;
  private StringFilter requestorUserId;
  private RequesteeUserTypeFilter requesteeUserType;
  private StringFilter requesteeUserId;
  private StringFilter requestCode;

  @Data
  public static class RequestorUserTypeFilter {

    private UserType equals;
    private List<UserType> in;
  }

  @Data
  public static class RequesteeUserTypeFilter {

    private UserType equals;
    private List<UserType> in;
  }
}
