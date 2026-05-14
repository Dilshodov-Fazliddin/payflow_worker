package uz.kapitalbank.pg.payflow.myclient.subscription;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalTaskSubscription {
  long lockDuration() default 20000L;
  String topicName();
  String[] variableNames() default {};
}
