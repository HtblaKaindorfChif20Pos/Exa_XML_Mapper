package at.kaindorf.mapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Project: plf_xmlMapper
 * Created by: SF
 * Date: 13.03.2024
 * Time: 12:35
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlFormat {
  String pattern() default "dd-MM-yyyy";
}
