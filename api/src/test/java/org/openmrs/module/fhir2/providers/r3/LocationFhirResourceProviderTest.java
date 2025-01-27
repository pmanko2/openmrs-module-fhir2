/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hamcrest.Matchers;
import org.hl7.fhir.convertors.conv30_40.Location30_40;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.providers.r4.MockIBundleProvider;

@RunWith(MockitoJUnitRunner.class)
public class LocationFhirResourceProviderTest extends BaseFhirR3ProvenanceResourceTest<org.hl7.fhir.r4.model.Location> {
	
	private static final String LOCATION_UUID = "123xx34-623hh34-22hj89-23hjy5";
	
	private static final String WRONG_LOCATION_UUID = "c3467w-hi4jer83-56hj34-23hjy5";
	
	private static final String LOCATION_NAME = "chulaimbo";
	
	private static final String CITY = "kakamega";
	
	private static final String COUNTRY = "Kenya";
	
	private static final String STATE = "Pan villa";
	
	private static final String POSTAL_CODE = "234-30100";
	
	private static final String LOGIN_LOCATION_TAG_NAME = "login";
	
	private static final String LOGIN_LOCATION_TAG_DESCRIPTION = "Identify login locations";
	
	private static final String LAST_UPDATED_DATE = "2020-09-03";
	
	private static final int PREFERRED_PAGE_SIZE = 10;
	
	private static final int COUNT = 1;
	
	private static final int START_INDEX = 0;
	
	private static final int END_INDEX = 10;
	
	@Mock
	private FhirLocationService locationService;
	
	private LocationFhirResourceProvider resourceProvider;
	
	private org.hl7.fhir.r4.model.Location location;
	
	@Before
	public void setup() {
		resourceProvider = new LocationFhirResourceProvider();
		resourceProvider.setLocationService(locationService);
	}
	
	@Before
	public void initLocation() {
		Address address = new Address();
		address.setCity(CITY);
		address.setCountry(COUNTRY);
		address.setState(STATE);
		address.setPostalCode(POSTAL_CODE);
		
		location = new org.hl7.fhir.r4.model.Location();
		location.setId(LOCATION_UUID);
		location.setName(LOCATION_NAME);
		location.setAddress(address);
		location.getMeta().addTag(new Coding(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME,
		        LOGIN_LOCATION_TAG_DESCRIPTION));
		setProvenanceResources(location);
	}
	
	@Test
	public void getResourceType_shouldReturnResourceType() {
		assertThat(resourceProvider.getResourceType(), equalTo(org.hl7.fhir.dstu3.model.Location.class));
		assertThat(resourceProvider.getResourceType().getName(), equalTo(org.hl7.fhir.dstu3.model.Location.class.getName()));
	}
	
	@Test
	public void getLocationByUuid_shouldReturnMatchingLocation() {
		when(locationService.get(LOCATION_UUID)).thenReturn(location);
		IdType id = new IdType();
		id.setValue(LOCATION_UUID);
		org.hl7.fhir.dstu3.model.Location result = resourceProvider.getLocationById(id);
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
		assertThat(result.getId(), equalTo(LOCATION_UUID));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getLocationWithWrongUuid_shouldThrowResourceNotFoundException() {
		IdType id = new IdType();
		id.setValue(WRONG_LOCATION_UUID);
		org.hl7.fhir.dstu3.model.Location result = resourceProvider.getLocationById(id);
		assertThat(result, nullValue());
	}
	
	@Test
	public void findLocationsByName_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam nameParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		
		when(locationService.searchForLocations(argThat(Matchers.is(nameParam)), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(nameParam, null, null, null, null, null, null, null, null,
		    null, null, null);
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(0).getName(), equalTo(LOCATION_NAME));
	}
	
	@Test
	public void findLocationsByCity_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam cityParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(CITY)));
		when(locationService.searchForLocations(isNull(), argThat(Matchers.is(cityParam)), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, cityParam, null, null, null, null, null, null, null,
		    null, null, null);
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(0).getAddress().getCity(), equalTo(CITY));
	}
	
	@Test
	public void findLocationsByCountry_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam countryParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(COUNTRY)));
		when(locationService.searchForLocations(isNull(), isNull(), argThat(Matchers.is(countryParam)), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, countryParam, null, null, null, null, null,
		    null, null, null, null);
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(0).getAddress().getCountry(), equalTo(COUNTRY));
	}
	
	@Test
	public void findLocationsByState_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam stateParam = new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(STATE)));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), argThat(Matchers.is(stateParam)),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, stateParam, null, null, null,
		    null, null, null, null);
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(0).getAddress().getState(), equalTo(STATE));
	}
	
	@Test
	public void findLocationsByPostalCode_shouldReturnMatchingBundleOfLocations() {
		StringAndListParam postalCodeParam = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(POSTAL_CODE)));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), argThat(Matchers.is(postalCodeParam)),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, postalCodeParam, null, null, null, null,
		    null, null, null, null);
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(0).getAddress().getPostalCode(), equalTo(POSTAL_CODE));
	}
	
	@Test
	public void findLocationsByTags_shouldReturnLocationsContainingGivenTag() {
		TokenAndListParam tag = new TokenAndListParam()
		        .addAnd(new TokenOrListParam(FhirConstants.OPENMRS_FHIR_EXT_LOCATION_TAG, LOGIN_LOCATION_TAG_NAME));
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), argThat(Matchers.is(tag)),
		    isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, tag, null, null, null, null,
		    null, null);
		
		assertThat(results, notNullValue());
		
		List<Location> resultList = get(results);
		
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(0).getMeta().getTag().iterator().next().getCode(), equalTo(LOGIN_LOCATION_TAG_NAME));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentName() {
		ReferenceAndListParam locationParentName = new ReferenceAndListParam();
		locationParentName.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("chulaimbo").setChain(Location.SP_NAME)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentName)), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentName,
		    null, null, null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentCity() {
		ReferenceAndListParam locationParentCity = new ReferenceAndListParam();
		locationParentCity.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("kampala").setChain(Location.SP_ADDRESS_CITY)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentCity)), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentCity,
		    null, null, null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentCountry() {
		ReferenceAndListParam locationParentCountry = new ReferenceAndListParam();
		locationParentCountry.addValue(
		    new ReferenceOrListParam().add(new ReferenceParam().setValue("uganda").setChain(Location.SP_ADDRESS_COUNTRY)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentCountry)), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentCountry,
		    null, null, null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentPostalCode() {
		ReferenceAndListParam locationParentPostalCode = new ReferenceAndListParam();
		locationParentPostalCode.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("234-30100").setChain(Location.SP_ADDRESS_POSTALCODE)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentPostalCode)), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null,
		    locationParentPostalCode, null, null, null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfChainedLocationsByParentState() {
		ReferenceAndListParam locationParentState = new ReferenceAndListParam();
		locationParentState.addValue(new ReferenceOrListParam()
		        .add(new ReferenceParam().setValue("najjanankumbi").setChain(Location.SP_ADDRESS_STATE)));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(Matchers.is(locationParentState)), isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, locationParentState,
		    null, null, null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfLocationsByUUID() {
		TokenAndListParam uuid = new TokenAndListParam().addAnd(new TokenParam(LOCATION_UUID));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    argThat(is(uuid)), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, null, uuid, null,
		    null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfLocationsByLastUpdated() {
		DateRangeParam lastUpdated = new DateRangeParam().setUpperBound(LAST_UPDATED_DATE).setLowerBound(LAST_UPDATED_DATE);
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), argThat(is(lastUpdated)), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, null, null,
		    lastUpdated, null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchLocations_shouldAddRelatedResourcesWhenIncluded() {
		HashSet<Include> includeSet = new HashSet<>();
		includeSet.add(new Include("Location:partof"));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), argThat(is(includeSet)), isNull(), isNull())).thenReturn(
		        new MockIBundleProvider<>(Arrays.asList(location, new Location()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, null, null, null,
		    includeSet, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(1).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.size(), equalTo(2));
		assertThat(resultList.get(0).getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchLocations_shouldNotAddRelatedResourcesForEmptyInclude() {
		HashSet<Include> includeSet = new HashSet<>();
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, null, null, null,
		    includeSet, null, null);
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.size(), equalTo(1));
		assertThat(((Location) resultList.get(0)).getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchLocations_shouldAddRelatedResourcesWhenReverseIncluded() {
		HashSet<Include> revIncludeSet = new HashSet<>();
		revIncludeSet.add(new Include("Encounter:location"));
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), argThat(is(revIncludeSet)), isNull())).thenReturn(
		        new MockIBundleProvider<>(Arrays.asList(location, new Encounter()), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, null, null, null,
		    null, revIncludeSet, null);
		
		List<IBaseResource> resultList = results.getResources(START_INDEX, END_INDEX);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.get(1).fhirType(), is(FhirConstants.ENCOUNTER));
		assertThat(resultList.size(), equalTo(2));
		assertThat(((Location) resultList.get(0)).getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchLocations_shouldNotAddRelatedResourcesForEmptyReverseInclude() {
		HashSet<Include> revIncludeSet = new HashSet<>();
		
		when(locationService.searchForLocations(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
		    isNull(), isNull(), isNull(), isNull(), isNull()))
		            .thenReturn(new MockIBundleProvider<>(Collections.singletonList(location), PREFERRED_PAGE_SIZE, COUNT));
		
		IBundleProvider results = resourceProvider.searchLocations(null, null, null, null, null, null, null, null, null,
		    null, revIncludeSet, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList.size(), equalTo(1));
		assertThat(resultList.get(0).getId(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void searchLocations_shouldReturnMatchingBundleOfLocations() {
		List<org.hl7.fhir.r4.model.Location> locations = new ArrayList<>();
		locations.add(location);
		
		when(locationService.searchForLocations(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
		    any())).thenReturn(new MockIBundleProvider<>(locations, PREFERRED_PAGE_SIZE, COUNT));
		
		StringAndListParam location = new StringAndListParam()
		        .addAnd(new StringOrListParam().add(new StringParam(LOCATION_NAME)));
		
		IBundleProvider results = resourceProvider.searchLocations(location, null, null, null, null, null, null, null, null,
		    null, null, null);
		
		List<Location> resultList = get(results);
		
		assertThat(results, notNullValue());
		assertThat(resultList.get(0).fhirType(), is(FhirConstants.LOCATION));
		assertThat(resultList, hasSize(greaterThanOrEqualTo(1)));
		assertThat(resultList.get(0).getId(), equalTo(LOCATION_UUID));
	}
	
	private List<Location> get(IBundleProvider results) {
		return results.getResources(START_INDEX, END_INDEX).stream().filter(it -> it instanceof Location)
		        .map(it -> (Location) it).collect(Collectors.toList());
	}
	
	@Test
	public void createLocation_shouldCreateNewLocation() {
		when(locationService.create(any(org.hl7.fhir.r4.model.Location.class))).thenReturn(location);
		
		MethodOutcome result = resourceProvider.createLocation(Location30_40.convertLocation(location));
		assertThat(result, notNullValue());
		assertThat(result.getCreated(), is(true));
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test
	public void deleteLocation_shouldDeleteLocation() {
		OperationOutcome result = resourceProvider.deleteLocation(new IdType().setValue(LOCATION_UUID));
		assertThat(result, notNullValue());
		assertThat(result.getIssue(), notNullValue());
		assertThat(result.getIssueFirstRep().getSeverity(), equalTo(OperationOutcome.IssueSeverity.INFORMATION));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getCode(), equalTo("MSG_DELETED"));
		assertThat(result.getIssueFirstRep().getDetails().getCodingFirstRep().getDisplay(),
		    equalTo("This resource has been deleted"));
	}
	
	@Test
	public void updateLocation_shouldUpdateLocation() {
		when(locationService.update(eq(LOCATION_UUID), any(org.hl7.fhir.r4.model.Location.class))).thenReturn(location);
		
		MethodOutcome result = resourceProvider.updateLocation(new IdType().setValue(LOCATION_UUID),
		    Location30_40.convertLocation(location));
		assertThat(result, notNullValue());
		assertThat(result.getResource(), notNullValue());
		assertThat(result.getResource().getIdElement().getIdPart(), equalTo(LOCATION_UUID));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateLocation_shouldThrowInvalidRequestForUuidMismatch() {
		when(locationService.update(eq(WRONG_LOCATION_UUID), any(org.hl7.fhir.r4.model.Location.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateLocation(new IdType().setValue(WRONG_LOCATION_UUID), Location30_40.convertLocation(location));
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateLocation_shouldThrowInvalidRequestForMissingId() {
		Location noIdLocation = new Location();
		
		when(locationService.update(eq(LOCATION_UUID), any(org.hl7.fhir.r4.model.Location.class)))
		        .thenThrow(InvalidRequestException.class);
		
		resourceProvider.updateLocation(new IdType().setValue(LOCATION_UUID), noIdLocation);
	}
	
	@Test(expected = MethodNotAllowedException.class)
	public void updateLocation_shouldThrowMethodNotAllowedIfDoesNotExist() {
		Location wrongLocation = new Location();
		wrongLocation.setId(WRONG_LOCATION_UUID);
		
		when(locationService.update(eq(WRONG_LOCATION_UUID), any(org.hl7.fhir.r4.model.Location.class)))
		        .thenThrow(MethodNotAllowedException.class);
		
		resourceProvider.updateLocation(new IdType().setValue(WRONG_LOCATION_UUID), wrongLocation);
	}
	
}
