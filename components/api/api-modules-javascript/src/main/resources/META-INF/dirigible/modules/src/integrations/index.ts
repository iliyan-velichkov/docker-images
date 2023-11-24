const SpringBeanProvider = Java.type("org.eclipse.dirigible.components.spring.SpringBeanProvider");
const Invoker = Java.type('org.eclipse.dirigible.components.engine.camel.invoke.Invoker');
const invoker = SpringBeanProvider.getBean(Invoker.class);

export function invokeRoute(routeId, payload, headers) {
    return invoker.invokeRoute(routeId, payload, headers);
}

export function getInvokingRouteMessage() {
    return __context.camelMessage;
}
