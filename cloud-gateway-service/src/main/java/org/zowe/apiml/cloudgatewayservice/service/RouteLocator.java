/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.cloudgatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.zowe.apiml.eurekaservice.client.util.EurekaMetadataParser;
import org.zowe.apiml.product.routing.RoutedService;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class RouteLocator implements RouteDefinitionLocator {

    private final DiscoveryLocatorProperties properties;

    private final String routeIdPrefix;

    private final SimpleEvaluationContext evalCtxt;

    private Flux<List<ServiceInstance>> serviceInstances;

    public RouteLocator(ReactiveDiscoveryClient discoveryClient,
                        DiscoveryLocatorProperties properties) {
        this(properties);
        serviceInstances = discoveryClient.getServices()
            .flatMap(service -> discoveryClient.getInstances(service).collectList());
    }

    private RouteLocator(DiscoveryLocatorProperties properties) {
        this.properties = properties;
        routeIdPrefix = this.getClass().getSimpleName() + "_";
        evalCtxt = SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build();
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {

        SpelExpressionParser parser = new SpelExpressionParser();
        Expression urlExpr = parser.parseExpression(properties.getUrlExpression());

        EurekaMetadataParser metadataParser = new EurekaMetadataParser();
        return serviceInstances.filter(instances -> !instances.isEmpty()).flatMap(Flux::fromIterable)
            .collectMap(ServiceInstance::getInstanceId)
            // remove duplicates
            .flatMapMany(map -> Flux.fromIterable(map.values())).map(instance -> {

                List<RoutedService> routedServices = metadataParser.parseToListRoute(instance.getMetadata());
                List<RouteDefinition> definitionsForInstance = new ArrayList<>();
                for (RoutedService service : routedServices) {
                    RouteDefinition routeDefinition = buildRouteDefinition(urlExpr, instance, service.getSubServiceId());

                    setProperties(routeDefinition, instance, service);

                    definitionsForInstance.add(routeDefinition);
                }
                return definitionsForInstance;
            }).flatMapIterable(list -> list);
    }

    protected void setProperties(RouteDefinition routeDefinition, ServiceInstance instance, RoutedService service) {
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        String predicateValue = "/" + instance.getServiceId().toLowerCase() + "/" + service.getGatewayUrl() + "/**";
        predicate.addArg("pattern", predicateValue);
        routeDefinition.getPredicates().add(predicate);

        FilterDefinition filter = new FilterDefinition();
        filter.setName("RewritePath");

        filter.addArg("regexp", predicateValue.replace("/**", "/?(?<remaining>.*)"));
        filter.addArg("replacement", service.getServiceUrl() + "/${remaining}");
        routeDefinition.getFilters().add(filter);

    }

    protected RouteDefinition buildRouteDefinition(Expression urlExpr, ServiceInstance serviceInstance, String routeId) {
        String serviceId = serviceInstance.getServiceId();
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(this.routeIdPrefix + serviceId + routeId);
        String uri = urlExpr.getValue(this.evalCtxt, serviceInstance, String.class);
        routeDefinition.setUri(URI.create(uri));
        // add instance metadata
        routeDefinition.setMetadata(new LinkedHashMap<>(serviceInstance.getMetadata()));
        return routeDefinition;
    }

    public String getRouteIdPrefix() {
        return routeIdPrefix;
    }

}
