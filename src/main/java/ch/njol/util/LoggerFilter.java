/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.util;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class LoggerFilter implements Filter, Closeable {
    private final Logger l;
    private final Collection<Filter> filters = new ArrayList<Filter>(5);
    @Nullable
    private final Filter oldFilter;

    public LoggerFilter(final Logger l) {
        this.l = l;
        oldFilter = l.getFilter();
        l.setFilter(this);
    }

    @Override
    public boolean isLoggable(final @Nullable LogRecord record) {
        if (oldFilter != null && !oldFilter.isLoggable(record))
            return false;
        for (final Filter f : filters)
            if (!f.isLoggable(record))
                return false;
        return true;
    }

    public final void addFilter(final Filter f) {
        filters.add(f);
    }

    public final boolean removeFilter(final Filter f) {
        return filters.remove(f);
    }

    @Override
    public void close() {
        l.setFilter(oldFilter);
    }
}
