package model.reporting;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import model.location.Location;
import utils.DateTimeSerialiser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Indicator {

  private Date startDate;
  private Date endDate;
  private Object value;
  private Location venue;
  private List<Cohort> cohorts;

  @JsonSerialize(using = DateTimeSerialiser.class)
  private Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  @JsonSerialize(using = DateTimeSerialiser.class)
  private Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  private Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  private Location getVenue() {
    return venue;
  }

  public void setVenue(Location venue) {
    this.venue = venue;
  }

  private List<Cohort> getCohorts() {
    return cohorts;
  }

  public void setCohorts(List<Cohort> cohorts) {
    this.cohorts = cohorts;
  }

  public void addCohort(Cohort cohort) {
    if (cohorts == null) {
      cohorts = new ArrayList<>();
    }
    cohorts.add(cohort);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Indicator)) {
      return false;
    }

    Indicator other = (Indicator) obj;

    return Objects.equals(getStartDate(), other.getStartDate()) &&
            Objects.equals(getEndDate(), other.getEndDate()) &&
            Objects.equals(getValue(), other.getValue()) &&
            Objects.equals(getVenue(), other.getVenue()) &&
            Objects.equals(getCohorts(), other.getCohorts());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getStartDate(), getEndDate(), getValue(), getVenue(), getCohorts());
  }

}
