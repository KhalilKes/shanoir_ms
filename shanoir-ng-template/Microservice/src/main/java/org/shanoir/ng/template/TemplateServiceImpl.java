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

package org.shanoir.ng.template;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirTemplateException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Template service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class TemplateServiceImpl implements TemplateService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TemplateServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private TemplateRepository templateRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirTemplateException {
		templateRepository.delete(id);
	}

	@Override
	public List<Template> findAll() {
		return Utils.toList(templateRepository.findAll());
	}

	@Override
	public List<Template> findBy(final String fieldName, final Object value) {
		return templateRepository.findBy(fieldName, value);
	}

	@Override
	public Optional<Template> findByData(final String data) {
		return templateRepository.findByData(data);
	}

	@Override
	public Template findById(final Long id) {
		return templateRepository.findOne(id);
	}

	@Override
	public Template save(final Template template) throws ShanoirTemplateException {
		Template savedTemplate = null;
		try {
			savedTemplate = templateRepository.save(template);
		} catch (DataIntegrityViolationException e) {
			LOG.error("Error while creating template", e);
			throw new ShanoirTemplateException("Error while creating template");
		}
		updateShanoirOld(savedTemplate);
		return savedTemplate;
	}

	@Override
	public Template update(final Template template) throws ShanoirTemplateException {
		final Template templateDb = templateRepository.findOne(template.getId());
		updateTemplateValues(templateDb, template);
		try {
			templateRepository.save(templateDb);
		} catch (Exception e) {
			LOG.error("Error while updating template", e);
			throw new ShanoirTemplateException("Error while updating template");
		}
		updateShanoirOld(templateDb);
		return templateDb;
	}

	@Override
	public void updateFromShanoirOld(final Template template) throws ShanoirTemplateException {
		if (template.getId() == null) {
			throw new IllegalArgumentException("Template id cannot be null");
		} else {
			final Template templateDb = templateRepository.findOne(template.getId());
			if (templateDb != null) {
				try {
					templateDb.setData(template.getData());
					templateRepository.save(templateDb);
				} catch (Exception e) {
					LOG.error("Error while updating template from Shanoir Old", e);
					throw new ShanoirTemplateException("Error while updating template from Shanoir Old");
				}
			}
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param template template.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final Template template) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(),
					new ObjectMapper().writeValueAsString(template));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send template " + template.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send template " + template.getId() + " save/update because of an error while serializing template.",
					e);
		}
		return false;
	}

	/*
	 * Update some values of template to save them in database.
	 * 
	 * @param templateDb template found in database.
	 * 
	 * @param template template with new values.
	 * 
	 * @return database template with new values.
	 */
	private Template updateTemplateValues(final Template templateDb, final Template template) {
		templateDb.setData(template.getData());
		return templateDb;
	}

}
