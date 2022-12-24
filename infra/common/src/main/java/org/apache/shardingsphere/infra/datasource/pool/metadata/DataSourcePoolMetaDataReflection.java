/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.datasource.pool.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtil;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.Properties;

/**
 * Data source pool meta data reflection.
 */
@RequiredArgsConstructor
public final class DataSourcePoolMetaDataReflection {
    
    private final DataSource targetDataSource;
    
    private final DataSourcePoolFieldMetaData dataSourcePoolFieldMetaData;
    
    /**
     * Get JDBC URL.
     *
     * @return JDBC URL
     */
    public Optional<String> getJdbcUrl() {
        return ReflectionUtil.getFieldValue(targetDataSource, dataSourcePoolFieldMetaData.getJdbcUrlFieldName());
    }
    
    /**
     * Get username.
     * 
     * @return username
     */
    public Optional<String> getUsername() {
        return ReflectionUtil.getFieldValue(targetDataSource, dataSourcePoolFieldMetaData.getUsernameFieldName());
    }
    
    /**
     * Get password.
     *
     * @return password
     */
    public Optional<String> getPassword() {
        return ReflectionUtil.getFieldValue(targetDataSource, dataSourcePoolFieldMetaData.getPasswordFieldName());
    }
    
    /**
     * Get JDBC connection properties.
     * 
     * @return JDBC connection properties
     */
    public Optional<Properties> getJdbcConnectionProperties() {
        return ReflectionUtil.getFieldValue(targetDataSource, dataSourcePoolFieldMetaData.getJdbcUrlPropertiesFieldName());
    }
}
