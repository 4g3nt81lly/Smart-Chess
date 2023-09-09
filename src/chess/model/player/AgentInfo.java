package chess.model.player;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used by subclasses of {@link Agent} to specify their custom display names.<p>
 * A display name must be a non-empty string that is distinctive and descriptive.
 * If an {@link Agent} does not explicitly specify a display name, the simple class name will be
 * used by default.
 * */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentInfo {

    /**
     * @return The non-empty display name of the agent.
     * */
    String displayName();

}
