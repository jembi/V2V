package org.jembi.bsis.viewmodel;

import java.util.List;

import org.jembi.bsis.model.user.User;

public class UserViewModel {

  private User user;
  private List<RoleViewModel> roles;

  public UserViewModel() {
  }

  public UserViewModel(User user) {
    this.user = user;
  }

  public Long getId() {
    return user.getId();
  }

  public String getUsername() {
    return user.getUsername();
  }

  public String getFirstName() {
    return user.getFirstName();
  }

  public String getLastName() {
    return user.getLastName();
  }

  public Boolean getIsAdmin() {
    return user.getIsAdmin();
  }

  public String getEmailId() {
    return user.getEmailId();
  }

  public Boolean isPasswordReset() {
    return user.isPasswordReset();
  }

  @Override
  public String toString() {
    return user.getUsername();
  }

  public List<RoleViewModel> getRoles() {
    return roles;
  }

  public void setRoles(List<RoleViewModel> roles) {
    this.roles = roles;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }

    if (!(other instanceof UserViewModel)) {
      return false;
    }

    UserViewModel userViewModel = (UserViewModel) other;

    if (user == null) {
      return userViewModel.user == null;
    }

    return user.equals(userViewModel.user);
  }
}
