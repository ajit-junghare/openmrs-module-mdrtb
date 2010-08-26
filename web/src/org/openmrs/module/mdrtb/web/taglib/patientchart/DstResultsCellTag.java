package org.openmrs.module.mdrtb.web.taglib.patientchart;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.mdrtb.MdrtbConcepts;
import org.openmrs.module.mdrtb.MdrtbService;
import org.openmrs.module.mdrtb.regimen.Regimen;
import org.openmrs.module.mdrtb.regimen.RegimenComponent;
import org.openmrs.module.mdrtb.specimen.DstResult;


public class DstResultsCellTag extends TagSupport {
	
    private static final long serialVersionUID = -1694368107155244376L;
    
	private final Log log = LogFactory.getLog(getClass());
	
    private List<DstResult> dstResults;
    
    private List<Regimen> regimens;
    
    private Concept drug;
    
    public int doStartTag() {
   
    	String ret = null;
    	
    	String drugColor = "white";
    	   	
    	// determine if we need to color the overall cell to reflect an active drug order
    	if(regimens != null) {
    		for(Regimen regimen : regimens) {
    			RegimenComponent component = regimen.getRegimenComponentByDrugConcept(drug);
    			if(component != null) {
    				drugColor="skyblue";  // TODO: make this a global prop, make sure it's in sync with color in dst results cell
    				break;
    			}
    		}
    	}
    	
    	// now figure out what DST result to display
    	if(dstResults != null && !dstResults.isEmpty()) {
    		String title = "";
    		Concept result = null;
    		
    		// first we need to build the title string (for the tooltip) and determine the result
    		for(DstResult dstResult : dstResults) {
    			
    			// need to ignore "none" results here
    			if(dstResult.getResult() != null && dstResult.getResult() != Context.getService(MdrtbService.class).getConcept(MdrtbConcepts.NONE)) {
    			
    			String concentration = "";
    		
    				if(dstResult.getConcentration() != null) {
    					concentration = dstResult.getConcentration().toString() + " - ";
    				}
    		
    				title = title + concentration + dstResult.getResult().getBestShortName(Context.getLocale()) + "<br>";
    
    				// TODO: figure out if this is the right rule if there are multiple results for the same drug
    				// right now I'm setting the result to intermediate (so it will pick up that color)
    				if(result == null) {
    					result = dstResult.getResult();
    				}
    				else if (!result.equals(dstResult.getResult())) {
    				    result = Context.getService(MdrtbService.class).getConcept(MdrtbConcepts.INTERMEDIATE_TO_TB_DRUG);
    				    break;
    				}
    			}
    		}
    		
    		String color = Context.getService(MdrtbService.class).getColorForConcept(result);
    		
    		ret = "<td class=\"chartCell\" style=\"background-color:" + drugColor + "\"><table style=\"padding:0px; border:0px; margin0px; width:100%;\"><tr><td class=\"chartCell\" title=\"" + title +
    				"\" style=\"width:30px;padding:0px;border:0px;margin:0px;text-align:center;background-color:" + color + ";\">" + 
    				result.getBestShortName(Context.getLocale()) + "</td></tr></table></td>";
    	}
    	else {
    		ret = "<td class=\"chartCell\" style=\"background-color:" + drugColor + "\"><table style=\"padding:0px; border:0px; margin0px; width:100%;\"><tr><td class=\"chartCell\"/></tr></table></td>";
    	}
    		
    	try {
			JspWriter w = pageContext.getOut();
			w.println(ret);
		}
		catch (IOException ex) {
			log.error("Error while starting dst result tag", ex);
		}
     
    	return SKIP_BODY;
    }
    
    
    public int doEndTag() {
    	dstResults = null;
    	return EVAL_PAGE;
    }
    
	public void setDstResults(List<DstResult> dstResults) {
	    this.dstResults = dstResults;
    }

	public List<DstResult> getDstResults() {
	    return dstResults;
    }


	public void setRegimens(List<Regimen> regimens) {
	    this.regimens = regimens;
    }


	public List<Regimen> getRegimens() {
	    return regimens;
    }
	
    public Concept getDrug() {
    	return drug;
    }
	
    public void setDrug(Concept drug) {
    	this.drug = drug;
    }

}