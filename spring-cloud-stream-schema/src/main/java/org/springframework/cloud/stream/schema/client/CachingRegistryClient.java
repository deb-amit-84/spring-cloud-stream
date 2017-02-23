/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.schema.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.schema.SchemaReference;
import org.springframework.cloud.stream.schema.SchemaRegistrationResponse;

/**
 * @author Vinicius Carvalho
 */
public class CachingRegistryClient implements SchemaRegistryClient {

	protected static final String CACHE_PREFIX = "org.springframework.cloud.stream.schema.client";

	protected static final String ID_CACHE = CACHE_PREFIX + ".schemaByIdCache";

	protected static final String REF_CACHE = CACHE_PREFIX + ".schemaByReferenceCache";

	private SchemaRegistryClient delegate;

	@Autowired
	private CacheManager cacheManager;

	public CachingRegistryClient(SchemaRegistryClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public SchemaRegistrationResponse register(String subject, String format, String schema) {
		SchemaRegistrationResponse response = delegate.register(subject,format,schema);
		cacheManager.getCache(ID_CACHE).put(response.getSchemaReference(),schema);
		cacheManager.getCache(REF_CACHE).put(response.getId(),schema);
		return response;
	}

	@Override
	@Cacheable(cacheNames = REF_CACHE)
	public String fetch(SchemaReference schemaReference) {
		return delegate.fetch(schemaReference);
	}

	@Override
	@Cacheable(cacheNames = ID_CACHE)
	public String fetch(int id) {
		return delegate.fetch(id);
	}

	public void setDelegate(SchemaRegistryClient delegate) {
		this.delegate = delegate;
	}
}
