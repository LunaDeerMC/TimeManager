package cn.lunadeer.mc.timeEssence.utils.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Heads {
    String[] value();
}
