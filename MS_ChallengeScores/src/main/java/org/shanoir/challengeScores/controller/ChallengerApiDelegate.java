/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.challengeScores.controller;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.challengeScores.data.access.service.ChallengerService;
import org.shanoir.challengeScores.data.model.Challenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.api.MetricApiController;
import io.swagger.model.Challengers;

/**
 * Implement the logic for the generated Swagger server api : {@link MetricApiController}
 *
 * @author jlouis
 */
@Component
public class ChallengerApiDelegate {

	@Autowired
	private ChallengerService challengerService;

	/**
	 * Constructor
	 */
	public ChallengerApiDelegate() {
	}


	public ResponseEntity<Void> deleteAll() {
		challengerService.deleteAll();
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	public ResponseEntity<Void> saveChallenger(Long id, String name) {
		Challenger challenger = new Challenger();
		challenger.setId(id);
		challenger.setName(name);
		boolean existed = challengerService.find(id) != null;
		challengerService.save(challenger);
		if (existed) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		}
	}


	public ResponseEntity<Void> updateAll(Challengers swaggerChallengers) {
		List<Challenger> challengers = new ArrayList<Challenger>();
		for (io.swagger.model.Challenger swaggerChallenger : swaggerChallengers) {
			Challenger challenger = new Challenger();
			challenger.setId(swaggerChallenger.getId().longValue());
			challenger.setName(swaggerChallenger.getName());
			challengers.add(challenger);
		}
		//challengerService.deleteAll();
		challengerService.saveAll(challengers);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


}
