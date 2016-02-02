package controller;

import factory.AuditRevisionViewModelFactory;
import model.audit.AuditRevision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repository.AuditRevisionRepository;
import utils.PermissionConstants;
import viewmodel.AuditRevisionViewModel;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("auditrevisions")
public class AuditRevisionController {

  @Autowired
  private AuditRevisionRepository auditRevisionRepository;
  @Autowired
  private AuditRevisionViewModelFactory auditRevisionViewModelFactory;

  public void setAuditRevisionRepository(AuditRevisionRepository auditRevisionRepository) {
    this.auditRevisionRepository = auditRevisionRepository;
  }

  public void setAuditRevisionViewModelFactory(AuditRevisionViewModelFactory auditRevisionViewModelFactory) {
    this.auditRevisionViewModelFactory = auditRevisionViewModelFactory;
  }

  @RequestMapping(method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_AUDIT_LOG + "')")
  public List<AuditRevisionViewModel> getAuditRevisions(
      @RequestParam(required = false) String search,
      @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
      @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {

    List<AuditRevision> auditRevisions;
    if (search == null) {
      auditRevisions = auditRevisionRepository.findAuditRevisions(startDate, endDate);
    } else {
      auditRevisions = auditRevisionRepository.findAuditRevisionsByUser(search, startDate, endDate);
    }
    return auditRevisionViewModelFactory.createAuditRevisionViewModels(auditRevisions);
  }
}
