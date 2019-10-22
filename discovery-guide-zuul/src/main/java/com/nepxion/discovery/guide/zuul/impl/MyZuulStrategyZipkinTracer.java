package com.nepxion.discovery.guide.zuul.impl;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.strategy.zuul.tracer.DefaultZuulStrategyTracer;
import com.netflix.zuul.context.RequestContext;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.LinkedHashMap;
import java.util.Map;

// 自定义调用链和灰度调用链输出到Zipkin
public class MyZuulStrategyZipkinTracer extends DefaultZuulStrategyTracer {
    private static final Logger LOG = LoggerFactory.getLogger(MyZuulStrategyZipkinTracer.class);

    @Autowired
    private Tracer tracer;


    @Override
    public void trace(RequestContext context) {
        super.trace(context);


        Tracer.SpanBuilder spanBuilder = tracer.buildSpan("opentracingSpan");
        spanBuilder.withTag(Tags.COMPONENT.getKey(),"discovery");
        Span span = spanBuilder.start();
        //添加链路信息
        span.setTag(DiscoveryConstant.N_D_SERVICE_GROUP, "服务组名=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_GROUP));
        span.setTag(DiscoveryConstant.N_D_SERVICE_TYPE, "服务类型=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_TYPE));
        span.setTag(DiscoveryConstant.N_D_SERVICE_ID, "服务名=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_ID));
        span.setTag(DiscoveryConstant.N_D_SERVICE_ADDRESS, "地址=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_ADDRESS));
        span.setTag(DiscoveryConstant.N_D_SERVICE_VERSION, "版本=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_VERSION));
        span.setTag(DiscoveryConstant.N_D_SERVICE_REGION, "区域=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_REGION));
        //TODO 这里最好能够捕捉方法调用异常，否则链路中异常无法捕获
        if (span != null && tracer != null){
            span.finish();
        }

        // 自定义调用链
        MDC.put("traceid", "traceid=" + strategyContextHolder.getHeader("traceid"));
        MDC.put("spanid", "spanid=" + strategyContextHolder.getHeader("spanid"));
        MDC.put("mobile", "mobile=" + strategyContextHolder.getHeader("mobile"));
        MDC.put("user", "user=" + strategyContextHolder.getHeader("user"));

        // 灰度路由调用链
        MDC.put(DiscoveryConstant.N_D_SERVICE_GROUP, "服务组名=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_GROUP));
        MDC.put(DiscoveryConstant.N_D_SERVICE_TYPE, "服务类型=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_TYPE));
        MDC.put(DiscoveryConstant.N_D_SERVICE_ID, "服务名=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_ID));
        MDC.put(DiscoveryConstant.N_D_SERVICE_ADDRESS, "地址=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_ADDRESS));
        MDC.put(DiscoveryConstant.N_D_SERVICE_VERSION, "版本=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_VERSION));
        MDC.put(DiscoveryConstant.N_D_SERVICE_REGION, "区域=" + strategyContextHolder.getHeader(DiscoveryConstant.N_D_SERVICE_REGION));

        LOG.info("全链路灰度调用链输出");

        LOG.info("request={}", context.getRequest());
    }

    @Override
    public void release(RequestContext context) {
        MDC.clear();

        LOG.info("全链路灰度调用链清除");
    }

    // Debug用
    @Override
    public Map<String, String> getDebugTraceMap() {
        Map<String, String> debugTraceMap = new LinkedHashMap<String, String>();
        debugTraceMap.put("traceid", strategyContextHolder.getHeader("traceid"));
        debugTraceMap.put("spanid", strategyContextHolder.getHeader("spanid"));
        debugTraceMap.put("mobile", strategyContextHolder.getHeader("mobile"));
        debugTraceMap.put("user", strategyContextHolder.getHeader("user"));

        return debugTraceMap;
    }
}