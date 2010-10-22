/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mdrtb.reporting.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.mdrtb.reporting.ReportSpecification;
import org.openmrs.module.mdrtb.reporting.ReportUtil;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;

/**
 * WHO Form 05 Report
 */
public class WHOForm07 implements ReportSpecification {
	
	/**
	 * @see ReportSpecification#getName()
	 */
	public String getName() {
		return "WHO Form 07";
	}
	
	/**
	 * @see ReportSpecification#getDescription()
	 */
	public String getDescription() {
		return "Annual report of treatment result of confirmed MDR-TB patients starting Category IV treatment";
	}
	
	/**
	 * @see ReportSpecification#getParameters()
	 */
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("location", "Facility", Location.class));
		l.add(new Parameter("year", "Year of treatment start", Integer.class));
		return l;
	}
	
	/**
	 * @see ReportSpecification#getRenderingModes()
	 */
	public List<RenderingMode> getRenderingModes() {
		List<RenderingMode> l = new ArrayList<RenderingMode>();
		l.add(ReportUtil.renderingModeFromResource("HTML Report", "org/openmrs/module/mdrtb/reporting/data/output/WHOForm07.html"));
		return l;
	}
	
	/**
	 * @see ReportSpecification#validateAndCreateContext(Map)
	 */
	public EvaluationContext validateAndCreateContext(Map<String, Object> parameters) {
		
		EvaluationContext context = ReportUtil.constructContext(parameters);
		try {
			Integer year = (Integer) parameters.get("year");
			context.addParameterValue("startDate", DateUtil.getDateTime(year, 1, 1));
			context.addParameterValue("endDate", DateUtil.getDateTime(year, 12, 31));
		}
		catch (Exception e) {
			throw new RuntimeException("Please enter a valid year for the report.");
		}
		return context;
	}
	
	/**
	 * ReportSpecification#evaluateReport(EvaluationContext)
	 */
	@SuppressWarnings("unchecked")
	public ReportData evaluateReport(EvaluationContext context) {
		
		ReportDefinition report = new ReportDefinition();
		
		Location location = (Location) context.getParameterValue("location");
		Date startDate = (Date)context.getParameterValue("startDate");
		Date endDate = (Date)context.getParameterValue("endDate");
		Date today = new Date();
		
		// Base Cohort is confirmed mdr patients who started treatment during year, optionally at location
		Map<String, Mapped<? extends CohortDefinition>> baseCohortDefs = new LinkedHashMap<String, Mapped<? extends CohortDefinition>>();
		baseCohortDefs.put("confirmedMdr", new Mapped(Cohorts.getConfirmedMdrFilter(startDate, endDate), null));
		baseCohortDefs.put("startedTreatment", new Mapped(Cohorts.getStartedTreatmentFilter(startDate, endDate), null));
		if (location != null) {
			CohortDefinition locationFilter = Cohorts.getLocationFilter(location);
			if (locationFilter != null) {
				baseCohortDefs.put("location", new Mapped(locationFilter, null));
			}	
		}
		CohortDefinition baseCohort = ReportUtil.getCompositionCohort(baseCohortDefs, "AND");
		report.setBaseCohortDefinition(baseCohort, null);
		
		CohortCrossTabDataSetDefinition dsd = new CohortCrossTabDataSetDefinition();
		
		dsd.addRow("New", Cohorts.getNewCaseFilter(), null);
		dsd.addRow("PreviousFirstLine", Cohorts.getPrevFirstLineCaseFilter(), null);
		dsd.addRow("PreviousSecondLine", Cohorts.getPrevSecondLineCaseFilter(), null);
		dsd.addRow("Total", ReportUtil.getCompositionCohort(dsd.getRows(), "OR"), null);
		
		@SuppressWarnings("deprecation")
		ProgramWorkflow outcomes = Context.getProgramWorkflowService().getWorkflow(1); // TODO: Refactor
		CohortDefinition cured = Cohorts.getInStateFilter(today, outcomes, "CURED - TB");
		CohortDefinition complete = Cohorts.getInStateFilter(today, outcomes, "TREATMENT COMPLETE");
		CohortDefinition failed = Cohorts.getInStateFilter(today, outcomes, "FAILED - TB");
		CohortDefinition defaulted = Cohorts.getInStateFilter(today, outcomes, "DEFAULTED - TB");
		CohortDefinition died = Cohorts.getInStateFilter(today, outcomes, "DIED - TB");
		CohortDefinition transferred = Cohorts.getInStateFilter(today, outcomes, "PATIENT TRANSFERRED OUT");
		
		dsd.addColumn("Cured", ReportUtil.getCompositionCohort("AND", baseCohort, cured), null);
		dsd.addColumn("TreatmentCompleted", ReportUtil.getCompositionCohort("AND", baseCohort, complete), null);
		dsd.addColumn("Failed", ReportUtil.getCompositionCohort("AND", baseCohort, failed), null);
		dsd.addColumn("Defaulted", ReportUtil.getCompositionCohort("AND", baseCohort, defaulted), null);
		dsd.addColumn("Died", ReportUtil.getCompositionCohort("AND", baseCohort, died), null);
		dsd.addColumn("TransferredOut", ReportUtil.getCompositionCohort("AND", baseCohort, transferred), null);
		dsd.addColumn("StillOnTreatment", ReportUtil.minus(baseCohort, cured, complete, failed, defaulted, died, transferred), null);
		dsd.addColumn("Total", baseCohort, null);
		report.addDataSetDefinition("Treatment results", dsd, null);
		
		ReportData data = Context.getService(ReportDefinitionService.class).evaluate(report, context);
		return data;
	}
}