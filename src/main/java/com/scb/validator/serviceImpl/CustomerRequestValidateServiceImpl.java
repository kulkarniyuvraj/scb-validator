package com.scb.validator.serviceImpl;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.scb.validator.model.CustomerRequestData;
import com.scb.validator.model.CustomerValidateResponse;
import com.scb.validator.model.MsValidateConfig;
import com.scb.validator.repository.CustomerRequestValidateRepo;
import com.scb.validator.service.CustomerRequestValidateService;

@Component
public class CustomerRequestValidateServiceImpl implements CustomerRequestValidateService {
	@Autowired
	private CustomerRequestValidateRepo customerRequestValidateRepo;

	@Override
	public CustomerValidateResponse validateCustomerRequest(CustomerRequestData customerRequestData) {

		return validateRequest(customerRequestData);
	}

	private CustomerValidateResponse validateRequest(CustomerRequestData customerRequestData) {
		CustomerValidateResponse customerValidateResponse = new CustomerValidateResponse();
		customerValidateResponse.setResponseCode(400);
		customerValidateResponse.setCustomerRequestData(customerRequestData);
		if (null == customerRequestData) {
			customerValidateResponse.setResponseMessage("Customer details empty");
			customerValidateResponse.setValidRequest(false);
			return customerValidateResponse;
		} else {
			return checkFieldsWithMappingTable(customerRequestData);
		}
	}

	private CustomerValidateResponse checkFieldsWithMappingTable(CustomerRequestData customerRequestData) {
		CustomerValidateResponse customerValidateResponse = new CustomerValidateResponse();

		Field[] fields = CustomerRequestData.class.getDeclaredFields();
		List<MsValidateConfig> ssValidateConfigValue = customerRequestValidateRepo.findAll();
		boolean countryCodeValid = false;
		if (ssValidateConfigValue != null) {

			for (MsValidateConfig dbFieldValue : ssValidateConfigValue) {

				if (dbFieldValue.getIsRequireToValidate().equalsIgnoreCase("yes")) {
					/*
					 * if(dbFieldValue.getFieldValue() != null){ String[]
					 * countryRegion = dbFieldValue.getFieldValue().split(",");
					 * for (String country : countryRegion) { if
					 * (country.equalsIgnoreCase(customerRequestData.
					 * getCustomerRegion().trim())) { //
					 * System.out.println("country " + country);
					 * countryCodeValid = true; } } if (!countryCodeValid) {
					 * customerValidateResponse.
					 * setResponseMessage("Validation error from field customerRegion"
					 * ); customerValidateResponse.setResponseCode(400);
					 * customerValidateResponse.setValidRequest(false); return
					 * customerValidateResponse; } }
					 */

					for (Field classField : fields) {
						if (classField.getName().trim().equalsIgnoreCase(dbFieldValue.getFieldName().trim())) {
							try {
								String value = String.valueOf(classField.get(customerRequestData));
								System.out.println("ME: " + value);
								if (dbFieldValue.getFieldValue() != null) {
									String[] countryRegion = dbFieldValue.getFieldValue().split(",");
									for (String country : countryRegion) {
										if (country.equalsIgnoreCase(value)) {
											countryCodeValid = true;
										}
									}
									if (!countryCodeValid) {
										customerValidateResponse
												.setResponseMessage("Validation error from field value " + value);
										customerValidateResponse.setResponseCode(400);
										customerValidateResponse.setValidRequest(false);
										return customerValidateResponse;
									}
								}

								if (value.length() >= dbFieldValue.getMaxLength()
										|| value.length() < dbFieldValue.getMinLength()) {
									customerValidateResponse
											.setResponseMessage("Validation error from field " + classField.getName());
									customerValidateResponse.setResponseCode(400);
									customerValidateResponse.setValidRequest(false);
									return customerValidateResponse;
								}
							} catch (IllegalArgumentException | IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		customerValidateResponse.setResponseCode(200);
		customerValidateResponse.setCustomerRequestData(customerRequestData);
		customerValidateResponse.setResponseMessage("Successfully validated");
		customerValidateResponse.setValidRequest(true);
		return customerValidateResponse;
	}

}
