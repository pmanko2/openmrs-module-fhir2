package org.openmrs.module.fhir2.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.openmrs.OpenmrsObject;
import org.openmrs.attribute.AttributeType;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.module.fhir2.api.translators.attributes.FhirAttributeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AttributeHandlers {
	
	private final Lock toFhirLock = new ReentrantLock();
	
	private final Lock toOpenmrsLock = new ReentrantLock();
	
	private final List<FhirAttributeHandler<?, ?>> handlers;
	
	private final Map<Class<? extends Customizable<?>>, List<FhirAttributeHandler<?, ?>>> cachedToFhirHandlers = new HashMap<>();
	
	private final Map<Class<? extends IAnyResource>, List<FhirAttributeHandler<?, ?>>> cachedToOpenmrsHandlers = new HashMap<>();
	
	@Autowired
	public AttributeHandlers(ApplicationContext context) {
		handlers = new ArrayList(context.getBeansOfType(FhirAttributeHandler.class).values());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Customizable<?> & OpenmrsObject, U extends IAnyResource> List<FhirAttributeHandler<T, U>> getHandlersFor(
			Class<T> underlyingType,
			Class<U> resourceType,
			AttributeType<T> attributeType
	) {
		List<FhirAttributeHandler<? extends Customizable<?>, ? extends IAnyResource>> potentialHandlers;
		if (cachedToFhirHandlers.containsKey(underlyingType)) {
			toFhirLock.lock();
			try {
				potentialHandlers = new ArrayList<>(cachedToFhirHandlers.get(underlyingType));
			}
			finally {
				toFhirLock.unlock();
			}
		} else {
			potentialHandlers = handlers.stream().filter(h -> underlyingType.isAssignableFrom(h.getAppliesToOpenmrsType()))
					.collect(
							Collectors.toList());
			
			toFhirLock.lock();
			try {
				cachedToFhirHandlers.put(underlyingType, new ArrayList<>(potentialHandlers));
			}
			finally {
				toFhirLock.unlock();
			}
		}
		
		return potentialHandlers.stream().filter(h -> h.supports((Class) resourceType, (AttributeType) attributeType))
				.map(h -> (FhirAttributeHandler<T, U>) h)
				.collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Customizable<?> & OpenmrsObject, U extends IAnyResource> List<FhirAttributeHandler<T, U>> getHandlersFor(
			Class<U> resourceType,
			Class<T> underlyingType
	) {
		List<FhirAttributeHandler<? extends Customizable<?>, ? extends IAnyResource>> potentialHandlers;
		if (cachedToFhirHandlers.containsKey(underlyingType)) {
			toOpenmrsLock.lock();
			try {
				potentialHandlers = new ArrayList<>(cachedToOpenmrsHandlers.get(underlyingType));
			}
			finally {
				toOpenmrsLock.unlock();
			}
		} else {
			potentialHandlers = handlers.stream().filter(h -> h.getAppliesToFhirType() != null)
					.filter(h -> h.getAppliesToFhirType().isAssignableFrom(resourceType)).collect(
							Collectors.toList());
			
			toOpenmrsLock.lock();
			try {
				cachedToOpenmrsHandlers.put(resourceType, new ArrayList<>(potentialHandlers));
			}
			finally {
				toOpenmrsLock.unlock();
			}
		}
		
		return potentialHandlers.stream()
				.map(h -> (FhirAttributeHandler<T, U>) h)
				.collect(Collectors.toList());
	}
	
}
