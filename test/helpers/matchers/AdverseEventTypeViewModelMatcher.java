package helpers.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import viewmodel.AdverseEventTypeViewModel;

import java.util.Objects;

public class AdverseEventTypeViewModelMatcher extends TypeSafeMatcher<AdverseEventTypeViewModel> {

  private AdverseEventTypeViewModel expected;

  private AdverseEventTypeViewModelMatcher(AdverseEventTypeViewModel expected) {
    this.expected = expected;
  }

  public static AdverseEventTypeViewModelMatcher hasSameStateAsAdverseEventTypeViewModel(
          AdverseEventTypeViewModel expected) {

    return new AdverseEventTypeViewModelMatcher(expected);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("An adverse event type view model with the following state:")
            .appendText("\nId: ").appendValue(expected.getId())
            .appendText("\nName: ").appendValue(expected.getName())
            .appendText("\nDescription: ").appendValue(expected.getDescription())
            .appendText("\nDeleted: ").appendValue(expected.getIsDeleted());
  }

  @Override
  public boolean matchesSafely(AdverseEventTypeViewModel actual) {
    return Objects.equals(actual.getId(), expected.getId()) &&
            Objects.equals(actual.getName(), expected.getName()) &&
            Objects.equals(actual.getDescription(), expected.getDescription()) &&
            Objects.equals(actual.getIsDeleted(), expected.getIsDeleted());
  }

}
