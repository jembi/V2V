package org.jembi.bsis.controllerservice;

import java.util.List;

import javax.transaction.Transactional;

import org.jembi.bsis.backingform.ComponentTypeBackingForm;
import org.jembi.bsis.factory.ComponentTypeFactory;
import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.repository.ComponentTypeRepository;
import org.jembi.bsis.viewmodel.ComponentTypeFullViewModel;
import org.jembi.bsis.viewmodel.ComponentTypeViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ComponentTypeControllerService {

  @Autowired
  private ComponentTypeRepository componentTypeRepository;
  
  @Autowired
  private ComponentTypeFactory componentTypeFactory;
  
  public List<ComponentTypeViewModel> getComponentTypes(boolean includeDeleted) {
    List<ComponentType> componentTypes;

    if (includeDeleted) {
      componentTypes = componentTypeRepository.getAllComponentTypesIncludeDeleted();
    } else {
      componentTypes = componentTypeRepository.getAllComponentTypes();
    }

    return componentTypeFactory.createViewModels(componentTypes);
  }
  
  public ComponentTypeFullViewModel getComponentType(long id) {
    return componentTypeFactory.createFullViewModel(componentTypeRepository.getComponentTypeById(id));
  }
  
  public ComponentTypeFullViewModel addComponentType(ComponentTypeBackingForm form) {
    ComponentType componentType = componentTypeRepository.saveComponentType(form.getComponentType());
    return componentTypeFactory.createFullViewModel(componentType);
  }
  
  public ComponentTypeFullViewModel updateComponentType(ComponentTypeBackingForm form) {
    ComponentType componentType = form.getComponentType();
    
    componentType = componentTypeRepository.updateComponentType(componentType);
    return componentTypeFactory.createFullViewModel(componentType);
  }
}