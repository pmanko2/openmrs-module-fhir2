package org.openmrs.module.fhir2.api.search.param;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

import java.util.HashSet;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LocationSearchParams extends BaseResourceSearchParams {
	
	/**
	 * StringAndListParam name, StringAndListParam city, StringAndListParam country, StringAndListParam
	 * postalCode, StringAndListParam state, TokenAndListParam tag, ReferenceAndListParam parent,
	 * TokenAndListParam id, DateRangeParam lastUpdated, HashSet<Include> includes, HashSet<Include>
	 * revIncludes, SortSpec sort
	 * 
	 * @return
	 */
	
	private StringAndListParam name;
	
	private StringAndListParam city;
	
	private StringAndListParam country;
	
	private StringAndListParam postalCode;
	
	private StringAndListParam state;
	
	private TokenAndListParam tag;
	
	private ReferenceAndListParam parent;
	
	public LocationSearchParams(StringAndListParam name, StringAndListParam city, StringAndListParam country,
	    StringAndListParam postalCode, StringAndListParam state, TokenAndListParam tag, ReferenceAndListParam parent,
	    TokenAndListParam id, DateRangeParam lastUpdated, SortSpec sort, HashSet<Include> includes,
	    HashSet<Include> revIncludes) {
		
		super(id, lastUpdated, sort, includes, revIncludes);
		
		this.name = name;
		this.city = city;
		this.country = country;
		this.postalCode = postalCode;
		this.state = state;
		this.tag = tag;
		this.parent = parent;
	}
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		return baseSearchParameterMap().addParameter(FhirConstants.NAME_SEARCH_HANDLER, name)
		        .addParameter(FhirConstants.CITY_SEARCH_HANDLER, city)
		        .addParameter(FhirConstants.STATE_SEARCH_HANDLER, state)
		        .addParameter(FhirConstants.COUNTRY_SEARCH_HANDLER, country)
		        .addParameter(FhirConstants.POSTALCODE_SEARCH_HANDLER, postalCode)
		        .addParameter(FhirConstants.LOCATION_REFERENCE_SEARCH_HANDLER, parent)
		        .addParameter(FhirConstants.TAG_SEARCH_HANDLER, tag);
	}
}
