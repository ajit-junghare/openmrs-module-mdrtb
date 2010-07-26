package org.openmrs.module.mdrtb.specimen;

import java.text.DateFormat;
import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.module.mdrtb.MdrtbFactory;


public abstract class TestImpl implements Test {
	
	Obs test;  // the top-level obs that holds all the data for this smear
	
	MdrtbFactory mdrtbFactory;
	
	/** implementing subclasses must override this method to define their type */
	public abstract String getTestType();
	
	public Object getTest() {
		return this.test;
	}
	
	public String getId() {
		return this.test.getId().toString();
	}

	public String getStatus() {
		return calculateStatus();
	}
	
	public String getSpecimenId() {
		return this.test.getEncounter().getEncounterId().toString();
	}
	
	public String getAccessionNumber() {
		return test.getAccessionNumber();
	}
	
	public String getComments() {
	
		return test.getComment();
		
		/** Delete this once we are sure we don't need it
		Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptTestComments());
		
		if (obs == null) {
			return null;
		}
		else {
			return obs.getValueText();
		} */
	}
	
	public Date getDateOrdered() {
		Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptDateOrdered());
	    	
		if (obs == null) {
			return null;
	    }
	    else {
	    	return obs.getValueDatetime();
	    }
	}
	    
	public Date getDateReceived() {
	    Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptDateReceived());
	    	
	    if (obs == null) {
	    	return null;
	    }
	    else {
	    	return obs.getValueDatetime();
	    }
	}

	public Location getLab() {
		return test.getLocation();
	}
	
	public Date getResultDate() {
    	Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptResultDate());
    	
    	if (obs == null) {
    		return null;
    	}
    	else {
    		return obs.getValueDatetime();
    	}
    }
	
	public Date getStartDate() {
		Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptStartDate());
		
		if (obs == null) {
			return null;
		}
		else {
			return obs.getValueDatetime();
		}
	}

	 public void setAccessionNumber(String accessionNumber) {
		 test.setAccessionNumber(accessionNumber);
				
		 // also propagate this location to the all the child obs
		 if(test.getGroupMembers() != null) {
			for(Obs obs : test.getGroupMembers()) {
		    	if(!obs.isVoided()) {
		    		obs.setAccessionNumber(accessionNumber);
		    	}
			}
		 }
	 }
	
	public void setComments(String comments) {
		
		test.setComment(comments);
		
		/** get rid of this old implementation once we know we don't need it
		Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptTestComments());
		
		// if this obs have not been created, and there is no data to add, do nothing
		if (obs == null && StringUtils.isBlank(comments)) {
			return;
		}
		
		// if we are trying to set the obs to null, simply void the obs
		if(StringUtils.isBlank(comments)) {
			obs.setVoided(true);
			obs.setVoidReason("voided by Mdr-tb module specimen tracking UI");
			return;
		}
		
		// otherwise, create the obs if necessary
		if (obs == null) {	
			// now create the new Obs and add it to the encounter
			obs = new Obs (test.getPerson(), mdrtbFactory.getConceptTestComments(), test.getObsDatetime(), test.getLocation());
			obs.setEncounter(test.getEncounter());
			test.addGroupMember(obs);
		}		    
		
		// update the value
		obs.setValueText(comments);
		
		*/
	}
	
	 public void setDateOrdered(Date dateOrdered) {
	    Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptDateOrdered());
	    
	    // if this obs have not been created, and there is no data to add, do nothing
		if (obs == null && dateOrdered == null) {
			return;
		}
	    
		// if we are trying to set the obs to null, simply void the obs
		if(dateOrdered == null) {
			obs.setVoided(true);
			obs.setVoidReason("voided by Mdr-tb module specimen tracking UI");
			return;
		}
		
		// create a new obs if needed
		if (obs == null) {	
			// now create the new Obs and add it to the encounter
			obs = new Obs (test.getPerson(), mdrtbFactory.getConceptDateOrdered(), test.getObsDatetime(), test.getLocation());
			obs.setEncounter(test.getEncounter());
			test.addGroupMember(obs);
		}		    
		
		// update the value
		obs.setValueDatetime(dateOrdered);
	}
	    
	 public void setDateReceived(Date dateReceived) {
	    Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptDateReceived());
		
	    // if this obs have not been created, and there is no data to add, do nothing
		if (obs == null && dateReceived == null) {
			return;
		}
	    
		// if we are trying to set the obs to null, simply void the obs
		if(dateReceived == null) {
			obs.setVoided(true);
			obs.setVoidReason("voided by Mdr-tb module specimen tracking UI");
			return;
		}
		
	    // create a new obs if needed
		if (obs == null) {		
			obs = new Obs (test.getPerson(), mdrtbFactory.getConceptDateReceived(), test.getObsDatetime(), test.getLocation());
			obs.setEncounter(test.getEncounter());
			test.addGroupMember(obs);
		}
		
		// now set the value
		obs.setValueDatetime(dateReceived);
	 }

	 public void setLab(Location location) {
	    test.setLocation(location);
			
		// also propagate this location to the all the child obs
	    if(test.getGroupMembers() != null) {
	    	for(Obs obs : test.getGroupMembers()) {
	    		if(!obs.isVoided()) {
	    			obs.setLocation(location);
	    		}
	    	}
	    }
	 }

	 public void setResultDate(Date resultDate) {
	    Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptResultDate());
	    
	    // if this obs have not been created, and there is no data to add, do nothing
		if (obs == null && resultDate == null) {
			return;
		}
	    
		// if we are trying to set the obs to null, simply void the obs
		if(resultDate == null) {
			obs.setVoided(true);
			obs.setVoidReason("voided by Mdr-tb module specimen tracking UI");
			return;
		}
		
		// create a new obs if needed
		if (obs == null) {			
			obs = new Obs (test.getPerson(), mdrtbFactory.getConceptResultDate(), test.getObsDatetime(), test.getLocation());
			obs.setEncounter(test.getEncounter());
			test.addGroupMember(obs);
		}
		
		// now set the value
		obs.setValueDatetime(resultDate);
	 }
	 
	 public void setStartDate(Date startDate) {
		 Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptStartDate());
		 
		 // if this obs have not been created, and there is no data to add, do nothing
		if (obs == null && startDate == null) {
			return;
		}
		 
		// if we are trying to set the obs to null, simply void the obs
		if(startDate == null) {
			obs.setVoided(true);
			obs.setVoidReason("voided by Mdr-tb module specimen tracking UI");
			return;
		}
		
		// create a new obs if needed
		if (obs == null) {
			obs = new Obs (test.getPerson(), mdrtbFactory.getConceptStartDate(), test.getObsDatetime(), test.getLocation());
			obs.setEncounter(test.getEncounter());
			test.addGroupMember(obs);
		}
		 
		 // now set the value
		 obs.setValueDatetime(startDate);
	 }

	 /**
	  * Comparable interface method and utility methods
	  */
	 
	 public int compareTo(Test test1) {
		 Date recent0 = oldestDate(this);
		 Date recent1 = oldestDate(test1);
		 
		 if (recent0 == null) {
			 // use the actual obs datetime if there are no value dates
			 recent0 = this.getObs().getObsDatetime();
		 }
		 if (recent1 == null) {
			 recent1 = this.getObs().getObsDatetime();
		 }

		return recent0.compareTo(recent1);  
	 }
	 
	 // checks all the dates properties associated with the mdrtb test and returns the date that is most recent
	 private Date oldestDate(Test test) {
		 Date oldest = null;
		 
		 if (test.getDateOrdered() != null) {
			 if (oldest == null || test.getDateOrdered().before(oldest)) {
				 oldest = test.getDateOrdered();
			 }
		 }
		 if (test.getDateReceived() != null) {
			 if (oldest == null || test.getDateReceived().before(oldest)) {
				 oldest = test.getDateReceived();
			 }
		 }
		 if (test.getResultDate() != null) {
			 if (oldest == null || test.getResultDate().before(oldest)) {
				 oldest = test.getResultDate();
			 }
		 }
		 if (test.getStartDate() != null) {
			 if (oldest == null || test.getStartDate().before(oldest)) {
				 oldest = test.getStartDate();
			 }
		 }
		 
		 return oldest;
	 }
	 
	 /**
	  * Protected methods used for interacting with the matching MdrSpecimenImpl
	  */

	 protected Obs getObs() {
		 return test;
	 }
	    
	 /**
	  * Utility methods 
	  */
		
	 /**
	  * Iterates through all the obs in the test obs group and
	  * returns the first one that who concept matches the specified concept
	  * Returns null if obs not found
	  */
	 protected Obs getObsFromObsGroup(Concept concept) {
	    if (test.getGroupMembers() != null) {
	    	for(Obs obs : test.getGroupMembers()) {
	    		if (!obs.isVoided() && obs.getConcept().equals(concept)) {
	    			return obs;
	    		}
	    	}
	    }
	    return null;
	 }
	    
	 /**
	  * Determines the current status of this test by examines the
	  * values of the various date fields
	  * Auto generated method comment
	  * 
	  * @return
	  */
	 private String calculateStatus() {
	    // TODO: determine the best way to localize all the text in this method

	    DateFormat df = DateFormat.getDateInstance();
	    	
	    if (getResultDate() != null) {
	    	return "Completed on " + df.format(getResultDate()) + " at " + getLab();
	    }
	    else if (getStartDate() != null) {
	    	return "Started on " + df.format(getStartDate()) + " at " + getLab();
	    }
	    else if (getDateReceived() != null) {
	    	return "Received by " + getLab() + " on " + df.format(getDateReceived());
	    }
	    else if (getDateOrdered() != null) {
	    	return "Ordered on " + df.format(getDateOrdered()) + " from " + getLab();
	    }
	    else {
	    	return "Unknown";
	    }
	 }
}
