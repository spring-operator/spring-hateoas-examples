/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.examples;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Turnquist
 */
@RestController
class EmployeeController {

	private final EmployeeRepository repository;
	private final EmployeeResourceAssembler assembler;
	private final EmployeeWithManagerResourceAssembler employeeWithManagerResourceAssembler;

	EmployeeController(EmployeeRepository repository, EmployeeResourceAssembler assembler,
					   EmployeeWithManagerResourceAssembler employeeWithManagerResourceAssembler) {

		this.repository = repository;
		this.assembler = assembler;
		this.employeeWithManagerResourceAssembler = employeeWithManagerResourceAssembler;
	}

	/**
	 * Look up all employees, and transform them into a REST collection resource using
	 * {@link EmployeeResourceAssembler#toResources(Iterable)}. Then return them through
	 * Spring Web's {@link ResponseEntity} fluent API.
	 *
	 * NOTE: cURL will fetch things as HAL JSON directly, but browsers issue a different
	 * default accept header, which allows XML to get requested first, so "produces"
	 * forces it to HAL JSON for all clients.
	 */
	@GetMapping(value = "/employees", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<Resources<Resource<Employee>>> findAll() {
		return ResponseEntity.ok(
			assembler.toResources(repository.findAll()));

	}

	/**
	 * Look up a single {@link Employee} and transform it into a REST resource using
	 * {@link EmployeeResourceAssembler#toResource(Object)}. Then return it through
	 * Spring Web's {@link ResponseEntity} fluent API.
	 *
	 * See {@link #findAll()} to explain {@link GetMapping}'s "produces" argument.
	 *
	 * @param id
	 */
	@GetMapping(value = "/employees/{id}", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<Resource<Employee>> findOne(@PathVariable long id) {
		return ResponseEntity.ok(
			assembler.toResource(repository.findOne(id)));
	}

	/**
	 * Find an {@link Employee}'s {@link Manager} based upon employee id. Turn it into a context-based link.
	 *
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/managers/{id}/employees", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<Resources<Resource<Employee>>> findEmployees(@PathVariable long id) {
		return ResponseEntity.ok(
			assembler.toResources(repository.findByManagerId(id)));
	}

	@GetMapping(value = "/employees/detailed", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<Resources<Resource<EmployeeWithManager>>> findAllDetailedEmployees() {

		return ResponseEntity.ok(
			employeeWithManagerResourceAssembler.toResources(
				StreamSupport.stream(repository.findAll().spliterator(), false)
					.map(EmployeeWithManager::new)
					.collect(Collectors.toList())));
	}

	@GetMapping(value = "/employees/{id}/detailed", produces = MediaTypes.HAL_JSON_VALUE)
	public ResponseEntity<Resource<EmployeeWithManager>> findDetailedEmployee(@PathVariable Long id) {

		Employee employee = repository.findOne(id);

		return ResponseEntity.ok(
			employeeWithManagerResourceAssembler.toResource(
				new EmployeeWithManager(employee)));
	}
}
