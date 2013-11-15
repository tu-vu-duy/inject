/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.extras.injection;

import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class SessionProviderServiceImpl implements SessionProviderService {
  SessionProvider sProvider = null;
  private static final ThreadLocal<SessionProvider> currentSession = new ThreadLocal<SessionProvider>();
  @Override
  public void setSessionProvider(Object key, SessionProvider sessionProvider) {
  }

  @Override
  public SessionProvider getSessionProvider(Object key) {
    return null;
  }

  @Override
  public void removeSessionProvider(Object key) {
    closeSessionProvider();
  }

  public void closeSessionProvider() {
    SessionProvider sProvider = currentSession.get();
    if (sProvider != null) {
      sProvider.close();
      currentSession.set(null);
    }
  }

  @Override
  public SessionProvider getSystemSessionProvider(Object key) {
    SessionProvider sProvider = currentSession.get();
    if (sProvider == null) {
      sProvider = SessionProvider.createSystemProvider();
      currentSession.set(sProvider);
    }
    return sProvider;
  }

}
