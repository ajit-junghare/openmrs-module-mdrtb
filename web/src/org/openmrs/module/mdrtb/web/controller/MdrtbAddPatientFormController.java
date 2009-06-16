package org.openmrs.module.mdrtb.web.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.IdentifierType;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mdrtb.MdrtbConstants;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PatientIdentifierTypeEditor;
import org.openmrs.propertyeditor.TribeEditor;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.controller.patient.ShortPatientModel;
import org.openmrs.web.controller.user.UserFormController;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class MdrtbAddPatientFormController extends SimpleFormController  {
    
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
            
    /** Parameters passed in view request object **/
    private String name = "";
    private String birthdate = "";
    private String age = "";
    private String gender = "";
    private String personType = "patient";
    private String personId = "";
    private String viewType = "view";
    
    
    Set<PatientIdentifier> newIdentifiers = new HashSet<PatientIdentifier>();
    String pref = "";
    
    @Override
    protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
           Map<String,Object> map = new HashMap<String,Object>();
           if (Context.isAuthenticated()){
                  Program program = Context.getProgramWorkflowService().getProgramByName(MdrtbConstants.MDRTB_PROGRAM_GLOBAL_PROPERTY);
                  map.put("mdrtbProgram", program);
                  MessageSourceAccessor msa = getMessageSourceAccessor();
                  String dateFormat = Context.getDateFormat().toPattern();
                  map.put("dateFormat", dateFormat);
                  map.put("daysOfWeek", "'" + msa.getMessage("mdrtb.sunday")+ "','" + msa.getMessage("mdrtb.monday")+ "','" + msa.getMessage("mdrtb.tuesday") + "','" + msa.getMessage("mdrtb.wednesday")+ "','" + msa.getMessage("mdrtb.thursday")+ "','" + msa.getMessage("mdrtb.friday")+ "','"
                          + msa.getMessage("mdrtb.saturday")+ "','" + msa.getMessage("mdrtb.sun")+ "','" + msa.getMessage("mdrtb.mon")+ "','"+ msa.getMessage("mdrtb.tues")+ "','"+ msa.getMessage("mdrtb.wed")+ "','"+ msa.getMessage("mdrtb.thurs")+ "','"+ msa.getMessage("mdrtb.fri")+ "','" + msa.getMessage("mdrtb.sat") + "'");
                  map.put("monthsOfYear", "'" + msa.getMessage("mdrtb.january")+ "','"+ msa.getMessage("mdrtb.february")+ "','"+ msa.getMessage("mdrtb.march")+ "','"+ msa.getMessage("mdrtb.april")+ "','"+ msa.getMessage("mdrtb.may")+ "','"+ msa.getMessage("mdrtb.june")+ "','"+ msa.getMessage("mdrtb.july")+ "','"+ msa.getMessage("mdrtb.august")+ "','"
                          + msa.getMessage("mdrtb.september")+ "','"+ msa.getMessage("mdrtb.october")+ "','"+ msa.getMessage("mdrtb.november")+ "','"+ msa.getMessage("mdrtb.december")+ "','"+ msa.getMessage("mdrtb.jan")+ "','"+ msa.getMessage("mdrtb.feb")+ "','"+ msa.getMessage("mdrtb.mar")+ "','"+ msa.getMessage("mdrtb.ap")+ "','"+ msa.getMessage("mdrtb.may")+ "','"
                          + msa.getMessage("mdrtb.jun")+ "','"+ msa.getMessage("mdrtb.jul")+ "','"+ msa.getMessage("mdrtb.aug")+ "','"+ msa.getMessage("mdrtb.sept")+ "','"+ msa.getMessage("mdrtb.oct")+ "','"+ msa.getMessage("mdrtb.nov")+ "','"+ msa.getMessage("mdrtb.dec")+ "'");
                  //the global property sets what patientIdTypes you get in the dropdown.  Its all in the jsp.
                  //String mdrtbIdentifierTypeString = Context.getAdministrationService().getGlobalProperty("mdrtb.patient_identifier_type_list");
                  //PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(mdrtbIdentifierTypeString);
                  //map.put("mdrPIdType", pit);
           }
        return map;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        
        NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, 
                new CustomDateEditor(Context.getDateFormat(), true));
        binder.registerCustomEditor(Tribe.class, new TribeEditor());
        binder.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
        binder.registerCustomEditor(Location.class, new LocationEditor());
        binder.registerCustomEditor(Concept.class, "civilStatus", new ConceptEditor());
        binder.registerCustomEditor(Concept.class, "causeOfDeath", new ConceptEditor());
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
        
        ShortPatientModel shortPatient = (ShortPatientModel)obj;
        
        log.debug("\nNOW GOING THROUGH PROCESSFORMSUBMISSION METHOD.......................................\n\n");
        
        if (Context.isAuthenticated()) {
            PatientService ps = Context.getPatientService();
            EncounterService es = Context.getEncounterService();
            MessageSourceAccessor msa = getMessageSourceAccessor();
            
            String action = request.getParameter("action");
            if (action == null || action.equals(msa.getMessage("general.save"))) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name.familyName", "error.name");
                
                String[] identifiers = request.getParameterValues("identifier");
                String[] types = request.getParameterValues("identifierType");
                String[] locs = request.getParameterValues("location");
                pref = request.getParameter("preferred");
                if (pref == null)
                    pref = "";
                
                if (log.isDebugEnabled()) {
                    log.debug("identifiers: " + identifiers);
                    for (String s : identifiers)
                        log.debug(s);
                    log.debug("types: " + types);
                    for (String s : types)
                        log.debug(s);
                    log.debug("locations: " + locs);
                    for (String s : locs)
                        log.debug(s);
                    log.debug("preferred: " + pref);
                }
                
                // loop over the identifiers to create the patient.identifiers set
                if (identifiers != null) {
                    for (int i=0; i<identifiers.length;i++) {
                        // arguments for the spring error messages
                        String[] args = {identifiers[i]};
                        
                        // add the new identifier only if they put in some identifier string
                        if (identifiers[i].length() > 0) {
                            
                            // set up the actual identifier java object
                            PatientIdentifierType pit = null;
                            if (types[i] == null || types[i].equals("")) {
                                String msg = getMessageSourceAccessor().getMessage("PatientIdentifier.identifierType.null", args);
                                errors.reject(msg);
                            }
                            else
                                pit = ps.getPatientIdentifierType(Integer.valueOf(types[i]));
                            
                            Location loc = null;
                            if (locs[i] == null || locs[i].equals("")) {
                                String msg = getMessageSourceAccessor().getMessage("PatientIdentifier.location.null", args);
                                errors.reject(msg);
                            }
                            else
                                loc = es.getLocation(Integer.valueOf(locs[i]));
                            
                            PatientIdentifier pi = new PatientIdentifier(identifiers[i], pit, loc);
                            pi.setPreferred(pref.equals(identifiers[i]+types[i]));
                            newIdentifiers.add(pi);
                            
                            if (log.isDebugEnabled()) {
                                log.debug("Creating patient identifier with identifier: " + identifiers[i]);
                                log.debug("and type: " + types[i]);
                                log.debug("and location: " + locs[i]);
                            }
                        
                            try {
                                if (pit.hasCheckDigit() && !OpenmrsUtil.isValidCheckDigit(identifiers[i])) {
                                    log.error("hasCheckDigit and is not valid: " + pit.getName() + " " + identifiers[i]);
                                    String msg = getMessageSourceAccessor().getMessage("error.checkdigits.verbose", args);
                                    errors.rejectValue("identifier", msg);
                                }
    //                          else if (pit.hasCheckDigit() == false && identifiers[i].contains("-")) {
    //                              log.error("hasn't CheckDigit and contains '-': " + pit.getName() + " " + identifiers[i]);
    //                              String[] args2 = {"-", identifiers[i]}; 
    //                              String msg = getMessageSourceAccessor().getMessage("error.character.invalid", args2);
    //                              errors.rejectValue("identifier", msg);
    //                          }
                            } catch (Exception e) {
                                log.error("exception thrown with: " + pit.getName() + " " + identifiers[i]);
                                log.error("Error while adding patient identifiers to savedIdentifier list", e);
                                String msg = getMessageSourceAccessor().getMessage("error.checkdigits", args);
                                errors.rejectValue("identifier", msg);
                            }
                        }
                    }
                }
            }
            
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "error.null");
            
            // check patients birthdate against future dates and really old dates
            if (shortPatient.getBirthdate() != null) {
                if (shortPatient.getBirthdate().after(new Date()))
                    errors.rejectValue("birthdate", "error.date.future");
                else {
                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
                    if (shortPatient.getBirthdate().before(c.getTime())){
                        errors.rejectValue("birthdate", "error.date.nonsensical");
                    }
                }
            }
            
        }
            
        return super.processFormSubmission(request, response, shortPatient, errors);
    }

    /**
     * 
     * The onSubmit function receives the form/command object that was modified
     *   by the input form and saves it to the db
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
        
        HttpSession httpSession = request.getSession();

        log.debug("\nNOW GOING THROUGH ONSUBMIT METHOD.......................................\n\n");

        if (Context.isAuthenticated()) {
            PatientService ps = Context.getPatientService();
            PersonService personService = Context.getPersonService();
            
            ShortPatientModel shortPatient = (ShortPatientModel)obj;
            String view = getSuccessView();
            boolean isError = false;
            
            String action = request.getParameter("action");
            MessageSourceAccessor msa = getMessageSourceAccessor();
            if (action != null && action.equals(msa.getMessage("general.cancel"))) {
                httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.canceled");
                return new ModelAndView(new RedirectView("addPerson.htm?personType=patient"));
            }
            
            Patient patient = null;
            if (shortPatient.getPatientId() != null) {
                patient = ps.getPatient(shortPatient.getPatientId());
                if (patient == null) {
                    try {
                        Person p = personService.getPerson(shortPatient.getPatientId());
                        //Context.clearSession();
                        //Person p2 = (Person) p;
                        patient = new Patient(p);
                        //patient = (Patient)p2;
                    } 
                    catch (ObjectRetrievalFailureException noUserEx) {
                        // continue;
                    }
                }
            }
            
            if (patient == null)
                patient = new Patient();
                
            boolean duplicate = false;
            PersonName newName = shortPatient.getName();
            
            if (log.isDebugEnabled())
                log.debug("Checking new name: " + newName.toString());
            
            for (PersonName pn : patient.getNames()) {
                if (((pn.getGivenName() == null && newName.getGivenName() == null) || OpenmrsUtil.nullSafeEquals(pn.getGivenName(), newName.getGivenName())) &&
                    ((pn.getMiddleName() == null && newName.getMiddleName() == null) || OpenmrsUtil.nullSafeEquals(pn.getMiddleName(), newName.getMiddleName())) &&
                    ((pn.getFamilyName() == null && newName.getFamilyName() == null) || OpenmrsUtil.nullSafeEquals(pn.getFamilyName(), newName.getFamilyName())))
                    duplicate = true;
            }
            
            // if this is a new name, add it to the patient
            if (!duplicate) {
                // set the current name to "non-prefered"
                if (patient.getPersonName() != null)
                    patient.getPersonName().setPreferred(false);
                
                // add the new name
                newName.setPersonNameId(null);
                newName.setPreferred(true);
                patient.addName(newName);
            }
            
            if (log.isDebugEnabled())
                log.debug("The address to add/check: " + shortPatient.getAddress());
            
            if (shortPatient.getAddress() != null && !shortPatient.getAddress().isBlank()) {
                duplicate = false;
                for (PersonAddress pa : patient.getAddresses()) {
                    if (pa.toString().equals(shortPatient.getAddress().toString()))
                        duplicate = true;
                }
                
                if (log.isDebugEnabled())
                    log.debug("The duplicate address:  " + duplicate);
                
                if (!duplicate) {
                    PersonAddress newAddress = shortPatient.getAddress();
                    newAddress.setPersonAddressId(null);
                    newAddress.setPreferred(true);
                    patient.addAddress(newAddress);
                }
            }
            if (log.isDebugEnabled())
                log.debug("patient addresses: " + patient.getAddresses());
            
            // set or unset the preferred bit for the old identifiers if needed
            if (patient.getIdentifiers() == null)
                patient.setIdentifiers(new TreeSet<PatientIdentifier>());
            
            for (PatientIdentifier pi : patient.getIdentifiers()) {
                pi.setPreferred(pref.equals(pi.getIdentifier()+pi.getIdentifierType().getPatientIdentifierTypeId()));
            }
            
            
            // add the new identifiers
            //patient.getIdentifiers().addAll(newIdentifiers);
            patient.addIdentifiers(newIdentifiers);
            
            
            // find which identifiers they removed and void them
            // must create a new list so that the updated identifiers in
            // the newIdentifiers list are hashed correctly
            List<PatientIdentifier> newIdentifiersList = new Vector<PatientIdentifier>();
            newIdentifiersList.addAll(newIdentifiers);
            for (PatientIdentifier identifier : patient.getIdentifiers()) {
                if (!newIdentifiersList.contains(identifier)) {
                    // mark the "removed" identifiers as voided
                    identifier.setVoided(true);
                }
                
            }
            
            //set preferred = true for mdrtb
            String piList = Context.getAdministrationService().getGlobalProperty("mdrtb.patient_identifier_type");
            if (piList != null && !piList.equals("")){
                try {
                    for (StringTokenizer st = new StringTokenizer(piList, "|"); st.hasMoreTokens(); ) {
                        String s = st.nextToken().trim();
                        PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(s);
                        if (pit != null){
                            boolean found = false;
                            for (PatientIdentifier identifier : patient.getIdentifiers()) {
                               if (identifier.getIdentifierType().getPatientIdentifierTypeId() == pit.getPatientIdentifierTypeId()){
                                   identifier.setPreferred(true);
                                   found = true;
                                   break;
                               } 
                            }
                            if (found)
                                break;
                        }
                    }    
                } catch (Exception ex){
                 
                }
            }
            
            
            
            // set the other patient attributes
            patient.setBirthdate(shortPatient.getBirthdate());
            patient.setBirthdateEstimated(shortPatient.getBirthdateEstimated());
            patient.setGender(shortPatient.getGender());
            if (shortPatient.getTribe() == "" || shortPatient.getTribe() == null)
                patient.setTribe(null);
            else {
                Tribe t = ps.getTribe(Integer.valueOf(shortPatient.getTribe()));
                patient.setTribe(t);
            }
            
            patient.setDead(shortPatient.getDead());
            if (patient.isDead()) {
                patient.setDeathDate(shortPatient.getDeathDate());
                patient.setCauseOfDeath(shortPatient.getCauseOfDeath());
            }
            else {
                patient.setDeathDate(null);
                patient.setCauseOfDeath(null);
            }
            
            // look for person attributes in the request and save to person
            for (PersonAttributeType type : personService.getPersonAttributeTypes("patient", "viewing")) {
                String value = request.getParameter(type.getPersonAttributeTypeId().toString());
                if (value != null)
                patient.addAttribute(new PersonAttribute(type, value));
            }
            
            // save or add the patient
            Patient newPatient = null;
            try {
                newPatient = ps.savePatient(patient);
              //enroll patient in program; attach to encounter, if possible. 
               
            } catch ( InvalidIdentifierFormatException iife ) {
                log.error(iife);
                patient.removeIdentifier(iife.getPatientIdentifier());
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.formatInvalid");
                //errors = new BindException(new InvalidIdentifierFormatException(msa.getMessage("PatientIdentifier.error.formatInvalid")), "givenName");
                isError = true;
            } catch ( InvalidCheckDigitException icde ) {
                log.error(icde);
                patient.removeIdentifier(icde.getPatientIdentifier());
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.checkDigit");
                //errors = new BindException(new InvalidCheckDigitException(msa.getMessage("PatientIdentifier.error.checkDigit")), "givenName");
                isError = true;
            } catch ( IdentifierNotUniqueException inue ) {
                log.error(inue);
                patient.removeIdentifier(inue.getPatientIdentifier());
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.notUnique");
                //errors = new BindException(new IdentifierNotUniqueException(msa.getMessage("PatientIdentifier.error.notUnique")), "givenName");
                isError = true;
            } catch ( DuplicateIdentifierException die ) {
                log.error(die);
                patient.removeIdentifier(die.getPatientIdentifier());
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.duplicate");
                //errors = new BindException(new DuplicateIdentifierException(msa.getMessage("PatientIdentifier.error.duplicate")), "givenName");
                isError = true;
            } catch ( InsufficientIdentifiersException iie ) {
                log.error(iie);
                patient.removeIdentifier(iie.getPatientIdentifier());
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.insufficientIdentifiers");
                //errors = new BindException(new InsufficientIdentifiersException(msa.getMessage("PatientIdentifier.error.insufficientIdentifiers")), "givenName");
                isError = true;
            } catch ( PatientIdentifierException pie ) {
                log.error(pie);
                patient.removeIdentifier(pie.getPatientIdentifier());
                httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.general");
                //errors = new BindException(new PatientIdentifierException(msa.getMessage("PatientIdentifier.error.general")), "givenName");
                isError = true;
            }
                        
            // update patient's relationships and death reason
            if ( !isError ) {
                String[] personAs = request.getParameterValues("personA");
                String[] types = request.getParameterValues("relationshipType");
                Person person = personService.getPerson(patient);
                List<Relationship> relationships;
                List<Person> newPersonAs = new Vector<Person>(); //list of all persons specifically selected in the form
                
                if (person != null) 
                    relationships = personService.getRelationships(person);
                else
                    relationships = new Vector<Relationship>();
                
                if ( personAs != null ) {
                    for (int x = 0 ; x < personAs.length; x++ ) {
                        String personAString = personAs[x];
                        String typeString = types[x];
                        
                        if (personAString != null && personAString.length() > 0 && typeString != null && typeString.length() > 0) {
                            Person personA = personService.getPerson(Integer.valueOf(personAString));
                            RelationshipType type = personService.getRelationshipType(Integer.valueOf(typeString));
                            
                            newPersonAs.add(personA);
                            
                            boolean found = false;
                            // TODO this assumes that a relative can only be related in one way
                            for (Relationship rel : relationships) {
                                //skip the relationships where this patient is the object
                                if (rel.getPersonA().equals(person))
                                    found = true;
                                
                                // just update the type of relationships that have the same relative
                                if (rel.getPersonA().equals(personA)) {
                                    rel.setRelationshipType(type);
                                    found = true;
                                }
                            }
                            if (!found) {
                                Relationship r = new Relationship(personA, person, type);
                                relationships.add(r);
                            }
                        }
                    }
                    
                }
    
                for (Relationship rel : relationships) {
                    if (newPersonAs.contains(rel.getPersonA()) || 
                            person.equals(rel.getPersonA()))
                        personService.updateRelationship(rel);
                    else
                        personService.deleteRelationship(rel);
                }
                
                
                // update the death reason
                if ( patient.getDead() ) {
                    log.debug("Patient is dead, so let's make sure there's an Obs for it");
                    // need to make sure there is an Obs that represents the patient's cause of death, if applicable
    
                    String codProp = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
                    Concept causeOfDeath = Context.getConceptService().getConceptByIdOrName(codProp);
    
                    if ( causeOfDeath != null ) {
                        Set<Obs> obssDeath = Context.getObsService().getObservations(patient, causeOfDeath, false);
                        if ( obssDeath != null ) {
                            if ( obssDeath.size() > 1 ) {
                                log.error("Multiple causes of death (" + obssDeath.size() + ")?  Shouldn't be...");
                            } else {
                                Obs obsDeath = null;
                                if ( obssDeath.size() == 1 ) {
                                    // already has a cause of death - let's edit it.
                                    log.debug("Already has a cause of death, so changing it");
                                    
                                    obsDeath = obssDeath.iterator().next();
                                    
                                } else {
                                    // no cause of death obs yet, so let's make one
                                    log.debug("No cause of death yet, let's create one.");
                                    
                                    obsDeath = new Obs();
                                    obsDeath.setPerson(patient);
                                    obsDeath.setConcept(causeOfDeath);
                                    Location loc = Context.getEncounterService().getLocationByName("Unknown Location");
                                    if ( loc == null ) loc = Context.getEncounterService().getLocation(new Integer(1));
                                    // TODO person healthcenter if ( loc == null ) loc = patient.getHealthCenter();
                                    if ( loc != null ) obsDeath.setLocation(loc);
                                    else log.error("Could not find a suitable location for which to create this new Obs");
                                }
                                
                                // put the right concept and (maybe) text in this obs
                                Concept currCause = patient.getCauseOfDeath();
                                if ( currCause == null ) {
                                    // set to NONE
                                    log.debug("Current cause is null, attempting to set to NONE");
                                    String noneConcept = Context.getAdministrationService().getGlobalProperty("concept.none");
                                    currCause = Context.getConceptService().getConceptByIdOrName(noneConcept);
                                }
                                
                                if ( currCause != null ) {
                                    log.debug("Current cause is not null, setting to value_coded");
                                    obsDeath.setValueCoded(currCause);
                                    
                                    Date dateDeath = patient.getDeathDate();
                                    if ( dateDeath == null ) dateDeath = new Date();
                                    obsDeath.setObsDatetime(dateDeath);
    
                                    // check if this is an "other" concept - if so, then we need to add value_text
                                    String otherConcept = Context.getAdministrationService().getGlobalProperty("concept.otherNonCoded");
                                    Concept conceptOther = Context.getConceptService().getConceptByIdOrName(otherConcept);
                                    if ( conceptOther != null ) {
                                        if ( conceptOther.equals(currCause) ) {
                                            // seems like this is an other concept - let's try to get the "other" field info
                                            String otherInfo = ServletRequestUtils.getStringParameter(request, "causeOfDeath_other", "");
                                            log.debug("Setting value_text as " + otherInfo);
                                            obsDeath.setValueText(otherInfo);
                                        } else {
                                            log.debug("New concept is NOT the OTHER concept, so setting to blank");
                                            obsDeath.setValueText("");
                                        }
                                    } else {
                                        log.debug("Don't seem to know about an OTHER concept, so deleting value_text");
                                        obsDeath.setValueText("");
                                    }
                                    
                                    Context.getObsService().updateObs(obsDeath);
                                } else {
                                    log.debug("Current cause is still null - aborting mission");
                                }
                            }
                        }
                    } else {
                        log.debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
                    }
                    
                }
               
                
                
            }
            
            if ( isError ) {
                log.error("Had an error during processing. Redirecting to " + this.getFormView());
                
                return this.showForm(request, response, errors);
                //return new ModelAndView(new RedirectView(getFormView()));
            }
            else {
                httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
                RedirectView rv = new RedirectView(getSuccessView());
                rv.addStaticAttribute("patientId", patient.getPatientId());
                return new ModelAndView(rv);
            }
        }
        else {
            return new ModelAndView(new RedirectView(getFormView()));
        }
    }
        
       
    

        
   

    /**
     * This class returns the form backing object.  This can be a string, a boolean, or a normal
     * java pojo.
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        
        Patient p = null;
        Integer id = null;
        
        if (Context.isAuthenticated()) {
            PatientService ps = Context.getPatientService();
            String patientId = request.getParameter("patientId");
            if (patientId != null) {
                try {
                    id = Integer.valueOf(patientId);
                    p = ps.getPatient(id);
                }
                catch (NumberFormatException numberError) {
                    log.warn("Invalid userId supplied: '" + patientId + "'", numberError);
                }
                catch (ObjectRetrievalFailureException noUserEx) {
                    // continue
                }
            }
        }
        
        if (p == null) {
            try {
                Person person = Context.getPersonService().getPerson(id);
                if (person != null)
                    p = new Patient(person);
            }
            catch (ObjectRetrievalFailureException noPersonEx) {
                log.warn("There is no patient or person with id: '" + id + "'", noPersonEx);
                throw new ServletException("There is no patient or person with id: '" + id + "'");
            }
        }
        
        ShortPatientModel patient = new ShortPatientModel(p);
        
        String name = request.getParameter("addName");
        if (p == null && name != null) {
            String gender = request.getParameter("addGender");
            String date = request.getParameter("addBirthdate");
            String age = request.getParameter("addAge");
            
            p = new Patient();
            UserFormController.getMiniPerson(p, name, gender, date, age);
            
            patient = new ShortPatientModel(p);
        }
        
        if (patient.getAddress() == null) {
            PersonAddress pa = new PersonAddress();
            pa.setPreferred(true);
            patient.setAddress(pa);
        }
        
        return patient;
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
        
        Map<String, Object> map = new HashMap<String, Object>();
            User me = Context.getAuthenticatedUser();
            map.put("authenticatedUser", me);
            AdministrationService as = Context.getAdministrationService();
            PatientService ps = Context.getPatientService();
            String identifierTypes = as.getGlobalProperty(MdrtbConstants.MDRTB_PATIENT_IDENTIFIER_TYPES);
                List<PatientIdentifierType> idTypeList = new ArrayList<PatientIdentifierType>();
                for (StringTokenizer st = new StringTokenizer(identifierTypes, "|"); st.hasMoreTokens(); ) {
                    String s = st.nextToken();
                    PatientIdentifierType pit = ps.getPatientIdentifierType(s);
                    if (pit != null)
                        idTypeList.add(pit);
                }

            map.put("mdrtbIdentifierTypes", idTypeList);
            map.put("test", identifierTypes);
         
            
        return map;
    } 
    
    

}
