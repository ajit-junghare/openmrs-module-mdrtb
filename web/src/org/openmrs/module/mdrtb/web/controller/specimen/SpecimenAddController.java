package org.openmrs.module.mdrtb.web.controller.specimen;

import org.openmrs.api.context.Context;
import org.openmrs.module.mdrtb.MdrtbService;
import org.openmrs.module.mdrtb.specimen.Specimen;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/module/mdrtb/specimen/add.form")
public class SpecimenAddController extends AbstractSpecimenController {

	@ModelAttribute("specimen")
	public Specimen getSpecimen(@RequestParam(required = true, value = "patientId") Integer patientId) {
		Specimen specimen = Context.getService(MdrtbService.class).createSpecimen(Context.getPatientService().getPatient(patientId));
		
		// set the default type to "sputum"
		specimen.setType(Context.getService(MdrtbService.class).getConceptSputum());
		
		return specimen;
	}
		
	
	@SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET) 
	public ModelAndView showSpecimenAdd(@RequestParam(required = true, value = "patientId") Integer patientId, ModelMap map) {
		map.put("patientId", patientId);
		return new ModelAndView("/module/mdrtb/specimen/specimenAdd", map);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView processSubmit(@ModelAttribute("specimen") Specimen specimen, BindingResult result, SessionStatus status) {
		
		// TODO: add validation
		
		if (result.hasErrors()) {
			return new ModelAndView("/module/mdrtb/specimen/specimen");
		}
		
		// save the specimen
		Context.getService(MdrtbService.class).saveSpecimen(specimen);
		
		// clears the command object from the session
		status.setComplete();
		
		// redirect to the new detail patient for this specimen
		return new ModelAndView("redirect:specimen.form?specimenId=" + specimen.getId());
		
	}
}
