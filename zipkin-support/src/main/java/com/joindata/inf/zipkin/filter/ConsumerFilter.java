package com.joindata.inf.zipkin.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.joindata.inf.common.basic.cst.RequestLogCst;
import com.joindata.inf.common.util.basic.CodecUtil;
import com.joindata.inf.common.util.basic.JsonUtil;
import com.joindata.inf.zipkin.TraceContext;
import com.joindata.inf.zipkin.anno.Hide;
import com.joindata.inf.zipkin.cst.TraceConstants;
import com.joindata.inf.zipkin.util.Ids;
import com.joindata.inf.zipkin.util.JsonUtils;
import com.joindata.inf.zipkin.util.Networks;
import com.joindata.inf.zipkin.util.Times;
import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rayee on 2017/10/23.
 */
@Activate(group = {Constants.CONSUMER})
public class ConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (TraceContext.getTraceId() == null || "com.alibaba.dubbo.monitor.MonitorService".equals(invoker.getInterface().getName())) {
            return invoker.invoke(invocation);
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        long csTimestamp = Times.currentMicros();
        Span consumerSpan = startTrace(invoker, invocation, csTimestamp);

        Result result = invoker.invoke(invocation);
        String serviceName = new StringBuilder().append(result.getAttachment(TraceConstants.APP_ID)).append(".").append(invoker.getInterface().getSimpleName()).append(".").append(invocation.getMethodName()).toString();
        consumerSpan.setName(serviceName);
        consumerSpan.addToAnnotations(Annotation.create(csTimestamp, TraceConstants.ANNO_CS, Endpoint.create(serviceName, Networks.ip2Num(invoker.getUrl().getHost()), invoker.getUrl().getPort())));

        RpcResult rpcResult = (RpcResult) result;

        endTrace(invoker, rpcResult, consumerSpan, stopwatch);
        return rpcResult;
    }


    private Span startTrace(Invoker<?> invoker, Invocation invocation, long csTimestamp) {
        //consumer span data
        Span consumerSpan = new Span();
        consumerSpan.setId(Ids.get());
        consumerSpan.setTrace_id(TraceContext.getTraceId());
        consumerSpan.setParent_id(TraceContext.getSpanId());

        consumerSpan.setTimestamp(csTimestamp);

        consumerSpan.addToBinary_annotations(BinaryAnnotation.create(TraceConstants.ARGS, hide(invocation), null));
        Map<String, String> attches = invocation.getAttachments();
        attches.put(TraceConstants.TRACE_ID, String.valueOf(consumerSpan.getTrace_id()));
        attches.put(TraceConstants.SPAN_ID, String.valueOf(consumerSpan.getId()));
        attches.put(RequestLogCst.REQUEST_ID, Long.toHexString(consumerSpan.getTrace_id()));
        MDC.put(RequestLogCst.REQUEST_ID, Long.toHexString(consumerSpan.getTrace_id()));
        return consumerSpan;
    }

    private void endTrace(Invoker invoker, Result result, Span consumerSpan, Stopwatch watch) {
        consumerSpan.setDuration(watch.stop().elapsed(TimeUnit.MICROSECONDS));
        // cr annotation
        consumerSpan.addToAnnotations(Annotation.create(Times.currentMicros(), TraceConstants.ANNO_CR, Endpoint.create(consumerSpan.getName(), Networks.ip2Num(invoker.getUrl().getHost()), invoker.getUrl().getPort())));
        // exception catch
        Throwable throwable = result.getException();
        if (throwable != null) {
            // attach exception
            consumerSpan.addToBinary_annotations(BinaryAnnotation.create("Exception", Throwables.getStackTraceAsString(throwable), null));
        }
        // collect the span
        TraceContext.addSpan(consumerSpan);
    }

    static String hide(Invocation invocation) {
        List<String> args = Lists.newArrayList();
        List<Class<?>> classes = Lists.newArrayList(invocation.getParameterTypes());
        Object[] arguments = invocation.getArguments();
        Method method;
        try {
            method = invocation.getInvoker().getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (classes.size() != 0) {
            for (int i = 0; i < classes.size(); i++) {
                Hide hide = method.getParameters()[i].getAnnotation(Hide.class);
                if (hide == null) {
                    args.add(JsonUtil.toJSON(arguments[i]));
                } else {
                    try {
                        args.add(CodecUtil.encryptDES(JsonUtils.toJson(arguments[i]), TraceConstants.DES_KEY) + "（加密）");
                    } catch (GeneralSecurityException e) {
                        args.add(TraceConstants.DEFAULT_ENCODE_PARAM);
                    }
                }
            }
        }
        return JsonUtil.toJSON(args);
    }
}
