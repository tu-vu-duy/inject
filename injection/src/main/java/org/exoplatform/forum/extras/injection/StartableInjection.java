package org.exoplatform.forum.extras.injection;

import java.util.HashMap;

import org.exoplatform.forum.extras.injection.forum.ProfileInjector;
import org.exoplatform.forum.extras.injection.user.UserInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.picocontainer.Startable;

public class StartableInjection implements Startable {
  private static final Log LOG = ExoLogger.getLogger(StartableInjection.class);
  private UserInjector userInjector;
  private ProfileInjector profileInjector;
  protected final OrganizationService organizationService;
  public StartableInjection(OrganizationService organizationService,
                            UserInjector injector, ProfileInjector profileInjector) {
    this.organizationService = organizationService;
    this.userInjector = injector;
    this.profileInjector = profileInjector;
  }

  @Override 
  public void start() {
    HashMap<String, String> params = new HashMap<String, String>();
    try {
      User user = organizationService.getUserHandler().findUserByName("demo");
      if (user != null) {
        return;
      }
      params.put("users", "demo,john,mary,ghost");
      params.put("type", "n");
      userInjector.inject(params);
      //
      params.clear();
      params.put("number", "10");
      params.put("prefix", "user");
      profileInjector.inject(params);
      //
      params.clear();
      params.put("number", "10");
      params.put("prefix", "user");
      
    } catch (Exception e) {
      LOG.debug("Failed to Inject by startable");
    }
  }

  @Override
  public void stop() {

  }

}
