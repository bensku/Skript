package ch.njol.yggdrasil;

import ch.njol.util.coll.BidiHashMap;
import ch.njol.util.coll.BidiMap;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class SimpleClassResolver implements ClassResolver {

    private final BidiMap<Class<?>, String> classes = new BidiHashMap<Class<?>, String>();

    public void registerClass(final Class<?> c, final String id) {
        final String oldId = classes.put(c, id);
        if (oldId != null && !oldId.equals(id))
            throw new YggdrasilException("Changed id of " + c + " from " + oldId + " to " + id);
    }

    @Override
    @Nullable
    public Class<?> getClass(final String id) {
        return classes.getKey(id);
    }

    @Override
    @Nullable
    public String getID(final Class<?> c) {
        return classes.getValue(c);
    }

}
