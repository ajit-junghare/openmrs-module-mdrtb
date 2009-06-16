<%@ include file="/WEB-INF/template/include.jsp"%>
<style><%@ include file="resources/date_input.css"%></style>
<script src='<%= request.getContextPath() %>/moduleResources/mdrtb/jquery-1.2.3.js'></script>
<script src='<%= request.getContextPath() %>/moduleResources/mdrtb/jquery.dimensions.pack.js'></script>
<script src='<%= request.getContextPath() %>/moduleResources/mdrtb/date_input.js'></script>


<openmrs:require privilege="Add Patients" otherwise="/login.htm" redirect="/module/mdrtb/mdrtbIndex.form" />

<%@ include file="mdrtbHeader.jsp"%>
<%@ taglib prefix="mdrtb" uri="taglibs/mdrtb.tld" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<script type="text/javascript">

	var dateFormat = '${dateFormat}';
	var DAY_NAMES=new Array(${daysOfWeek});
    var MONTH_NAMES=new Array(${monthsOfYear});
    
    
	function addIdentifier(id, type, location, pref, oldIdentifier) {
		var tbody = document.getElementById('identifiersTbody');
		var row = document.getElementById('identifierRow');
		var newrow = row.cloneNode(true);
		newrow.style.display = "";
		newrow.id = tbody.childNodes.length;
		tbody.appendChild(newrow);
		var inputs = newrow.getElementsByTagName("input");
		var selects = newrow.getElementsByTagName("select");
		if (id) {
			for (var i in inputs) {
				if (inputs[i] && inputs[i].name == "identifier") {
					inputs[i].value = id;
					if (oldIdentifier && 1 == 0) {
						inputs[i].parentNode.appendChild(document.createTextNode(id));
						inputs[i].parentNode.removeChild(inputs[i]);
					}
				}
			}	
		}
		if (type) {
			for (var i in selects)
				if (selects[i] && selects[i].name == "identifierType") {
					var selectedOpt;
					for (o in selects[i].options) {
						if (selects[i].options[o].value == type) {
							selectedOpt = selects[i].options[o];
							selectedOpt.selected = true;
						}
						else
							selects[i].options[o].selected = false;
					}
					if (oldIdentifier && 1 == 0) {
						selects[i].parentNode.appendChild(document.createTextNode(selectedOpt.text));
						selects[i].parentNode.removeChild(selects[i]);
					}
				}
		}
		if (location) {
			for (var i in selects)
				if (selects[i] && selects[i].name == "location") {
					var selectedOpt;
					for (o in selects[i].options) {
						if (selects[i].options[o].value == location) {
							selectedOpt = selects[i].options[o];
							selectedOpt.selected = true;
						}
						else
							selects[i].options[o].selected = false;
					}
					if (oldIdentifier && 1 == 0) {
						selects[i].parentNode.appendChild(document.createTextNode(selectedOpt.text));
						selects[i].parentNode.removeChild(selects[i]);
					}
				}	
		}
		
		for (var i in inputs)
			if (inputs[i] && inputs[i].name == "preferred") {
				inputs[i].checked = (pref == true);
				inputs[i].value = id + type;
			}
		
		/*
		if (oldIdentifier) {
			for (var i in inputs) {
				if(inputs[i] && inputs[i].name == "closeButton")
					inputs[i].style.display = "none";
			}
		}
		*/

	}
	
	function updateAge() {
		var birthdateBox = document.getElementById('birthdate');
		var ageBox = document.getElementById('age');
		try {
			var birthdate = new Date(birthdateBox.value);
			var age = getAge(birthdate);
			if (age > 0)
				ageBox.innerHTML = "(" + age + ' <spring:message code="Person.age.years"/>)';
			else if (age == 1)
				ageBox.innerHTML = '(1 <spring:message code="Person.age.year"/>)';
			else if (age == 0)
				ageBox.innerHTML = '( < 1 <spring:message code="Person.age.year"/>)';
			else
				ageBox.innerHTML = '( ? )';
			ageBox.style.display = "";
		} catch (err) {
			ageBox.innerHTML = "";
			ageBox.style.display = "none";
		}
	}
	
	function updateEstimated() {
		var input = document.getElementById("birthdateEstimatedInput");
		if (input) {
			input.checked = false;
			input.parentNode.className = "";
		}
		else
			input.parentNode.className = "listItemChecked";
	}
	
	// age function borrowed from http://anotherdan.com/2006/02/simple-javascript-age-function/
	function getAge(d, now) {
		var age = -1;
		if (typeof(now) == 'undefined') now = new Date();
		while (now >= d) {
			age++;
			d.setFullYear(d.getFullYear() + 1);
		}
		return age;
	}
	
	function removeRow(btn) {
		var parent = btn.parentNode;
		while (parent.tagName.toLowerCase() != "tr")
			parent = parent.parentNode;
		
		parent.style.display = "none";
	}
	
	function removeHiddenRows() {
		var rows = document.getElementsByTagName("TR");
		var i = 0;
		while (i < rows.length) {
			if (rows[i].style.display == "none") {
				rows[i].parentNode.removeChild(rows[i]);
			}
			else {
				i = i + 1;
			}
		}
	}
	
	function identifierOrTypeChanged(input) {
		var parent = input.parentNode;
		while (parent.tagName.toLowerCase() != "tr")
			parent = parent.parentNode;
		
		var inputs = parent.getElementsByTagName("input");
		var prefInput;
		var idInput;
		var typeInput;
		for (var i in inputs) {
			if (inputs[i] && inputs[i].name == "preferred")
				prefInput = inputs[i];
			else if (inputs[i] && inputs[i].name == "identifier")
				idInput = inputs[i];
		}
		inputs = parent.getElementsByTagName("select");
		for (var i in inputs)
			if (inputs[i] && inputs[i].name == "identifierType")
				typeInput = inputs[i];
		
		if (idInput && typeInput)
			prefInput.value = idInput.value + typeInput.value;
	}
	
</script>

<style>
	th { text-align: left } 
	th.headerCell {
		border-top: 1px lightgray solid; 
		xborder-right: 1px lightgray solid
	}
	td.inputCell {
		border-top: 1px lightgray solid;
		}
		td.inputCell th {
			font-weight: normal;
		}
	.lastCell {
		border-bottom: 1px lightgray solid;
	}
</style>

<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>

<spring:hasBindErrors name="patient">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${fn:replace(error, '--', '\\-\\-')} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post" onSubmit="removeHiddenRows()">
	<c:if test="${patient.patientId == null}"><h2><spring:message code="Patient.create"/></h2></c:if>


	
	<br/>
	
	<table cellspacing="0" cellpadding="7">
	<tr>
		<th class="headerCell"><spring:message code="Person.name"/></th>
		<td class="inputCell">
			<table cellspacing="2">
				<thead>
					<openmrs:portlet url="nameLayout" id="namePortlet" size="columnHeaders" parameters="layoutShowTable=false|layoutShowExtended=false" />
				</thead>
				<spring:nestedPath path="patient.name">
					<openmrs:portlet url="nameLayout" id="namePortlet" size="inOneRow" parameters="layoutMode=edit|layoutShowTable=false|layoutShowExtended=false" />
				</spring:nestedPath>
			</table>
		</td>
	</tr>
	<tr>
		<th class="headerCell"><spring:message code="PatientIdentifier.title.endUser"/></th>
		<td class="inputCell">
			<table id="identifiers" cellspacing="2">
				<tr>
					<td><spring:message code="PatientIdentifier.identifier"/></td>
					<td><spring:message code="PatientIdentifier.identifierType"/></td>
					<td><spring:message code="PatientIdentifier.location.identifier"/></td>
					<td><spring:message code="general.preferred"/></td>
					<td></td>
				</tr>
				<tbody id="identifiersTbody">
					<tr id="identifierRow">
						<td valign="top">
							<input type="text" size="30" name="identifier" onmouseup="identifierOrTypeChanged(this)" />
						</td>
						<td valign="top">
							<select name="identifierType" onclick="identifierOrTypeChanged(this)">
							<openmrs:globalProperty key="mdrtb.patient_identifier_type_list" var="filterList"/>
								<mdrtb:forEachRecord name="patientIdentifierType" filterList="${filterList}">
									<option value="${record.patientIdentifierTypeId}">
										${record.name}
									</option>
								</mdrtb:forEachRecord>
							</select>
						</td>
						<td valign="top">
							<select name="location">
								<option value=""></option>
								<openmrs:globalProperty key="mdrtb.location_list" var="locFilterList"/>
								<mdrtb:forEachRecord name="location" filterList="${locFilterList}">
									<option value="${record.locationId}">
										${record.name}
									</option>
								</mdrtb:forEachRecord>
							</select>
						</td>
						<td valign="middle" align="center">
							<input type="radio" name="preferred" value="1" checked selected onclick="identifierOrTypeChanged(this)" />
						</td>
						<td valign="middle" align="center">
							<input type="button" name="closeButton" onClick="return removeRow(this);" class="closeButton" value='<spring:message code="general.remove"/>'/>
						</td>
					</tr>
				</tbody>
			</table>
			<script type="text/javascript">
				<c:forEach items="${identifiers}" var="id">
					addIdentifier("${id.identifier}", "${id.identifierType.patientIdentifierTypeId}", "${id.location.locationId}", ${id.preferred}, ${id.dateCreated != null});
				</c:forEach>
			</script>
			<input type="button" class="smallButton" onclick="addIdentifier()" value="<spring:message code="PatientIdentifier.add" />" hidefocus />
		</td>
	</tr>
	<tr>
		<th class="headerCell"><spring:message code="patientDashboard.demographics"/></th>
		<td class="inputCell">
			<table>
				<tr>
					<td><spring:message code="Person.gender"/></td>
					<td><spring:message code="Person.age"/></td>
					<td><spring:message code="Person.birthdate"/> <i style="font-weight: normal; font-size: 0.8em;">(<spring:message code="general.format"/>: <openmrs:datePattern />)</i></td>

				</tr>
				<tr>
					<td style="padding-right: 3em">
						<spring:bind path="patient.gender">
								<openmrs:forEachRecord name="gender">
									<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
										<label for="${record.key}"> <spring:message code="Person.gender.${record.value}"/> </label>
								</openmrs:forEachRecord>
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
					<td style="padding-right: 3em">
						<span id="age"></span>
					</td>
					<td style="padding-right: 3em">
						<script type="text/javascript">
							function updateEstimated(txtbox) {
								var input = document.getElementById("birthdateEstimatedInput");
								if (input) {
									input.checked = false;
									input.parentNode.className = "";
								}
								else if (txtbox)
									txtbox.parentNode.className = "listItemChecked";
							}
						</script>
						<spring:bind path="patient.birthdate">			
							<input  type="text" 
									name="birthdate" size="10" id="birthdate"
									value="${status.value}"
									onChange="updateAge(); updateEstimated(this);"
									onmousedown="javascript:$(this).date_input();" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
						</spring:bind>
						
						<span id="birthdateEstimatedCheckbox" class="listItemChecked" style="padding: 5px;">
							<spring:bind path="patient.birthdateEstimated">
								<label for="birthdateEstimatedInput"><spring:message code="Person.birthdateEstimated"/></label>
								<input type="hidden" name="_birthdateEstimated">
								<input type="checkbox" name="birthdateEstimated" value="true" 
									   <c:if test="${status.value == true}">checked</c:if> 
									   id="birthdateEstimatedInput" 
									   onclick="if (!this.checked) updateEstimated()" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</span>
						
						<script type="text/javascript">
							if (document.getElementById("birthdateEstimatedInput").checked == false)
								updateEstimated();
							updateAge();
						</script>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th class="headerCell"><spring:message code="Person.address"/></th>
		<td class="inputCell">
			<spring:nestedPath path="patient.address">
				<openmrs:portlet url="addressLayout" id="addressPortlet" size="full" parameters="layoutShowTable=true|layoutShowExtended=false" />
			</spring:nestedPath>
		</td>
	</tr>
	
	</table>
	
	<input type="hidden" name="patientId" value="${param.patientId}" />
	
	<br />
	<input type="submit" value="<spring:message code="general.save" />" name="action" id="addButton"> &nbsp; &nbsp; 
	<input type="button" value="<spring:message code="general.back" />" onclick="history.go(-1);">
</form>

<script type="text/javascript">
	document.forms[0].elements[0].focus();
	document.getElementById("identifierRow").style.display = "none";
	addIdentifier();
	updateAge();
</script>

<%@ include file="mdrtbFooter.jsp"%>