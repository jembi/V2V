package org.jembi.bsis.controller;

import static org.jembi.bsis.helpers.builders.PasswordResetBackingFormBuilder.aPasswordResetBackingForm;
import static org.jembi.bsis.helpers.builders.SimpleMailMessageBuilder.aSimpleMailMessage;
import static org.jembi.bsis.helpers.builders.UserBuilder.aUser;
import static org.jembi.bsis.helpers.matchers.UserMatcher.hasSameStateAsUser;
import static org.jembi.bsis.helpers.matchers.UserPasswordMatcher.hasPassword;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import junit.framework.Assert;

import org.jembi.bsis.backingform.PasswordResetBackingForm;
import org.jembi.bsis.controller.PasswordResetController;
import org.jembi.bsis.model.user.User;
import org.jembi.bsis.repository.UserRepository;
import org.jembi.bsis.service.PasswordGenerationService;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class PasswordResetControllerTests {

  private PasswordResetController controller;
  private UserRepository userRepository;
  private JavaMailSender mailSender;
  private PasswordGenerationService passwordGenerationService;

  @Test
  public void testResetPasswordWithNoExsitingUser_shouldReturnResponseEntityWithNotFoundStatus() {
    // Set up fixture
    setUpFixture();

    String expectedUsername = "expected.username";

    // Set up expectations
    when(userRepository.findUser(anyString())).thenReturn(null);

    // Exercise SUT
    PasswordResetBackingForm form = aPasswordResetBackingForm().withUsername(expectedUsername).build();
    ResponseEntity<Map<String, Object>> response = controller.resetPassword(form);

    // Verify
    verify(userRepository).findUser(expectedUsername);
    verifyNoMoreInteractions(userRepository);
    verifyZeroInteractions(mailSender);
    verifyZeroInteractions(passwordGenerationService);
    Assert.assertEquals("Response status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void testResetPassword_shouldReturnResponseEntityWithCreatedStatus() {
    // Set up fixture
    setUpFixture();

    String expectedUsername = "expected.username";
    String expectedEmailId = "expected.emailId@jembi.org";
    String expectedPassword = "Jxa4cgV2d3OoyRfL";

    User existingUser = aUser().withEmailId(expectedEmailId).build();

    SimpleMailMessage expectedMessage = aSimpleMailMessage()
        .withTo(expectedEmailId)
        .withSubject("BSIS Password reset")
        .withText("Your password has been reset to \"" + expectedPassword +
            "\". You will be required to change it next time you log in.")
        .build();

    // Set up expectations
    User expectedUser = aUser()
        .withEmailId(expectedEmailId)
        .withPasswordReset()
        .build();

    when(userRepository.findUser(expectedUsername)).thenReturn(existingUser);
    when(passwordGenerationService.generatePassword()).thenReturn(expectedPassword);
    when(userRepository.updateUser(existingUser, true)).thenReturn(existingUser);
    doNothing().when(mailSender).send(any(SimpleMailMessage.class));

    // Exercise SUT
    PasswordResetBackingForm form = aPasswordResetBackingForm().withUsername(expectedUsername).build();
    ResponseEntity<Map<String, Object>> response = controller.resetPassword(form);

    // Verify
    verify(userRepository).findUser(expectedUsername);
    verify(passwordGenerationService).generatePassword();
    verify(userRepository).updateUser(and(argThat(hasSameStateAsUser(expectedUser)), argThat(hasPassword(expectedPassword))), eq(true));
    verify(mailSender).send(expectedMessage);
    verifyNoMoreInteractions(userRepository);
    verifyNoMoreInteractions(mailSender);
    verifyNoMoreInteractions(passwordGenerationService);
    Assert.assertEquals("Response status should be CREATED", HttpStatus.CREATED, response.getStatusCode());
  }

  private void setUpFixture() {
    userRepository = mock(UserRepository.class);
    mailSender = mock(JavaMailSender.class);
    passwordGenerationService = mock(PasswordGenerationService.class);

    controller = new PasswordResetController();
    controller.setUserRepository(userRepository);
    controller.setMailSender(mailSender);
    controller.setPasswordGenerationService(passwordGenerationService);
    controller.setPasswordResetSubject("BSIS Password reset");
    controller.setPasswordResetMessage("Your password has been reset to \"%s\". You will be required to change it next time you log in.");
  }

}