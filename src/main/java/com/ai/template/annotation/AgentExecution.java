package com.ai.template.annotation;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentExecution {
    
    
    String value();
    
    
    String description() default "";
}