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

package org.apache.shardingsphere.readwritesplitting.yaml.swapper;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.metadata.converter.ReadwriteSplittingNodeConverter;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * TODO Rename YamlReadwriteSplittingRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * YAML readwrite-splitting rule configuration swapper.
 */
public final class NewYamlReadwriteSplittingRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<ReadwriteSplittingRuleConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final ReadwriteSplittingRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        for (Map.Entry<String, AlgorithmConfiguration> entry : data.getLoadBalancers().entrySet()) {
            result.add(new YamlDataNode(
                    ReadwriteSplittingNodeConverter.getLoadBalancerNodeConverter().getNamePath(entry.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        for (ReadwriteSplittingDataSourceRuleConfiguration each : data.getDataSources()) {
            result.add(new YamlDataNode(ReadwriteSplittingNodeConverter.getDataSourceNodeConvertor().getNamePath(each.getName()), YamlEngine.marshal(swapToYamlConfiguration(each))));
        }
        return result;
    }
    
    private YamlReadwriteSplittingDataSourceRuleConfiguration swapToYamlConfiguration(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlReadwriteSplittingDataSourceRuleConfiguration result = new YamlReadwriteSplittingDataSourceRuleConfiguration();
        result.setWriteDataSourceName(dataSourceRuleConfig.getWriteDataSourceName());
        result.setReadDataSourceNames(dataSourceRuleConfig.getReadDataSourceNames());
        result.setTransactionalReadQueryStrategy(dataSourceRuleConfig.getTransactionalReadQueryStrategy().name());
        result.setLoadBalancerName(dataSourceRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        Map<String, AlgorithmConfiguration> loadBalancerMap = new LinkedHashMap<>();
        for (YamlDataNode each : dataNodes) {
            if (ReadwriteSplittingNodeConverter.getDataSourceNodeConvertor().isPath(each.getKey())) {
                ReadwriteSplittingNodeConverter.getDataSourceNodeConvertor().getName(each.getKey())
                        .ifPresent(groupName -> dataSources.add(swapDataSource(groupName, YamlEngine.unmarshal(each.getValue(), YamlReadwriteSplittingDataSourceRuleConfiguration.class))));
            } else if (ReadwriteSplittingNodeConverter.getLoadBalancerNodeConverter().isPath(each.getKey())) {
                ReadwriteSplittingNodeConverter.getLoadBalancerNodeConverter().getName(each.getKey())
                        .ifPresent(loadBalancerName -> loadBalancerMap.put(loadBalancerName, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            }
        }
        return new ReadwriteSplittingRuleConfiguration(dataSources, loadBalancerMap);
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration swapDataSource(final String name, final YamlReadwriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new ReadwriteSplittingDataSourceRuleConfiguration(name, yamlDataSourceRuleConfig.getWriteDataSourceName(), yamlDataSourceRuleConfig.getReadDataSourceNames(),
                getTransactionalReadQueryStrategy(yamlDataSourceRuleConfig), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    private TransactionalReadQueryStrategy getTransactionalReadQueryStrategy(final YamlReadwriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return Strings.isNullOrEmpty(yamlDataSourceRuleConfig.getTransactionalReadQueryStrategy())
                ? TransactionalReadQueryStrategy.DYNAMIC
                : TransactionalReadQueryStrategy.valueOf(yamlDataSourceRuleConfig.getTransactionalReadQueryStrategy());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getTypeClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "READWRITE_SPLITTING";
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
}
