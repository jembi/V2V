package helpers.builders;

import model.user.User;

public class UserBuilder extends AbstractEntityBuilder<User> {

  private String emailId;
  private String username = "default.username";
  private boolean passwordReset;
  private Integer id;
  private String firstName = "Default";
  private String lastName = "User";

  public static UserBuilder aUser() {
    return new UserBuilder();
  }

  public UserBuilder withId(int id) {
    this.id = id;
    return this;
  }

  public UserBuilder withEmailId(String emailId) {
    this.emailId = emailId;
    return this;
  }

  public UserBuilder withUsername(String username) {
    this.username = username;
    return this;
  }

  public UserBuilder withPasswordReset() {
    passwordReset = true;
    return this;
  }

  public UserBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public UserBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  @Override
  public User build() {
    User user = new User();
    user.setId(id);
    user.setEmailId(emailId);
    user.setUsername(username);
    user.setPasswordReset(passwordReset);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    String password = "$2a$04$iA45ovNGD4hhA1puc/a8J.FN8WCMzKft1vdBAgw6o7oe7KBpVVkRS";
    user.setPassword(password);
    return user;
  }
}
