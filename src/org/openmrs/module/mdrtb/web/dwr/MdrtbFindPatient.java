package org.openmrs.module.mdrtb.web.dwr;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.web.dwr.PatientListItem;

public class MdrtbFindPatient {
    protected final Log log = LogFactory.getLog(getClass());
    
    public Collection findPatients(String searchValue, boolean includeVoided) {
        
        Collection<Object> patientList = new Vector<Object>();

        Integer userId = -1;
        if (Context.isAuthenticated())
            userId = Context.getAuthenticatedUser().getUserId();
        log.info(userId + "|" + searchValue);
        
        PatientService ps = Context.getPatientService();
        List<Patient> patients;
        

            patients = ps.getPatients(searchValue);
            patientList = new Vector<Object>(patients.size());
            for (Patient p : patients)
                    patientList.add(new PatientListItem(p));

       
        
        // I'm taking out the 'minimal patients returned'
        // If this needs to be smarter, there should be a better findPatients(...)
                
        return patientList;
    }
    
    public Collection findPeople(String searchValue, String dateString, boolean includeVoided) {
        
        Collection<Object> patientList = new Vector<Object>();

        Integer userId = -1;
        if (Context.isAuthenticated())
            userId = Context.getAuthenticatedUser().getUserId();
        PersonService ps = Context.getPersonService();
        List<Person> patients;
        
        patients = ps.getPeople(searchValue, false);
        patientList = new Vector<Object>(patients.size());
        String persAttTypeString = Context.getAdministrationService().getGlobalProperty("mdrtb.treatment_supporter_person_attribute_type");
        PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByName(persAttTypeString);
        for (Person p : patients){
            if (p.getAttributes(pat.getPersonAttributeTypeId()) != null && !p.getAttributes(pat.getPersonAttributeTypeId()).isEmpty())
                patientList.add(new MdrtbPersonListItem(p, dateString));
        }        
        return patientList;
    }
    
 public Collection findAllPeople(String searchValue, String dateString, boolean includeVoided) {
        
        Collection<Object> patientList = new Vector<Object>();

        Integer userId = -1;
        if (Context.isAuthenticated())
            userId = Context.getAuthenticatedUser().getUserId();
        PersonService ps = Context.getPersonService();
        List<Person> patients;
        
        patients = ps.getPeople(searchValue, false);
        patientList = new Vector<Object>(patients.size());

        for (Person p : patients){
                patientList.add(new MdrtbPersonListItem(p, dateString));
        }        
        return patientList;
    }
}
