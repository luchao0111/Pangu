package com.joindata.inf.common.support.fastdfs;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.joindata.inf.common.basic.annotation.BindConfigHub;
import com.joindata.inf.common.basic.annotation.WebConfig;
import com.joindata.inf.common.support.fastdfs.bootconfig.WebMvcConfig;

/**
 * 启用 FastDFS
 * 
 * @author <a href="mailto:songxiang@joindata.com">宋翔</a>
 * @date 2016年12月2日 下午12:41:10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ConfigHub.class)
@BindConfigHub(ConfigHub.class)
@WebConfig(WebMvcConfig.class)
public @interface EnableFastDfs
{
}