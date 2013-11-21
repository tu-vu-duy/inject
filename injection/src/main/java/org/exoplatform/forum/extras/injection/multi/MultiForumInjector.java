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
package org.exoplatform.forum.extras.injection.multi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.forum.bbcode.api.BBCodeService;
import org.exoplatform.forum.extras.injection.SessionProviderServiceImpl;
import org.exoplatform.forum.extras.injection.lifecycle.LifeCycleCompletionService;
import org.exoplatform.forum.extras.injection.utils.ExoNameGenerator;
import org.exoplatform.forum.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class MultiForumInjector extends DataInjector {
  private static Log LOG = ExoLogger.getLogger(MultiForumInjector.class);
  /** . */
  private static final String CATEGORY     = "category";
  /** . */
  private static final String FORUM        = "forum";
  /** . */
  private static final String TOPIC        = "topic";
  /** . */
  private static final String POST         = "post";
  /** . */
  private static final String CATEGORY_PREFIX = "categoryPrefix";
  /** . */
  private static final String FORUM_PREFIX = "forumPrefix";
  /** . */
  private static final String TOPIC_PREFIX = "topicPrefix";
  /** . */
  private static final String FROM_USER    = "fromUser";
  /** . */
  private static final String TO_USER      = "toUser";
  /** . */
  private static final String USER_PREFIX  = "userPrefix";
  /** . */
  private static final String POST_PREFIX  = "postPrefix";
  
  private List<String> supportBBcodes = new ArrayList<String>();
  
  int cate = 0;
  int forum = 0;
  int topic = 0;
  int post = 0;
  int from = 1;
  int to = 1;
  int coutCat = 0, coutFor = 0, coutTop = 0, coutPos = 0, maxUser = 0;
  
  String cate_ = "";
  String forum_ = "";
  String topic_ = "";
  String post_ = "";
  String user_ = "";
  String owner = "";
  String [] users = new String[] {};

  private LifeCycleCompletionService completionService;

  protected final ForumService forumService;
  protected LoremIpsum4J lorem;
  protected ExoNameGenerator exoNameGenerator;

  public MultiForumInjector() {
    forumService = CommonsUtils.getService(ForumService.class);
    completionService = CommonsUtils.getService(LifeCycleCompletionService.class);
    
    this.exoNameGenerator = new ExoNameGenerator();
    this.lorem = new LoremIpsum4J();
    initBBcode();
  }
  
  private void initBBcode() {
    try {
      List<String> supportBBcodes = CommonsUtils.getService(BBCodeService.class).getActive();
      String bbcodeIgnore = "wiki url email img";
      for (String bbcode : supportBBcodes) {
        if (bbcode.indexOf("=") < 0 && bbcodeIgnore.indexOf(bbcode.toLowerCase()) < 0) {
          this.supportBBcodes.add(bbcode);
        }
      }
    } catch (Exception e) {
      supportBBcodes = new ArrayList<String>();
    }
  }
  
  public ForumService getForumService() {
    return forumService;
  }
  
  @Override
  public Log getLog() {
    return ExoLogger.getExoLogger(getClass());
  }

  @Override
  public Object execute(HashMap<String, String> params) throws Exception {
    return null;
  }

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    cate = intParam(params, CATEGORY, 0);
    forum = intParam(params, FORUM, 0);
    topic = intParam(params, TOPIC, 0);
    post = intParam(params, POST, 0);

    from = intParam(params, FROM_USER, 1);
    to = intParam(params, TO_USER, 1);

    cate_ = strParam(params, CATEGORY_PREFIX, "Cate_");
    forum_ = strParam(params, FORUM_PREFIX, "Forum_");
    topic_ = strParam(params, TOPIC_PREFIX, "Topic_");
    post_ = strParam(params, POST_PREFIX, "Post_");
    user_ = strParam(params, USER_PREFIX, "user");
    
    owner = user_ + from;
    
    //
    LOG.info("Initial category number: " + cate + " prefix: " + cate_);
    LOG.info("Initial forum number : " + forum + " prefix: " + forum_);
    LOG.info("Initial topic number : " + topic + " prefix: " + topic_);
    LOG.info("Initial post number : " + post + " prefix: " + post_);
    LOG.info("From user : " + from);
    LOG.info("To user : " + to);
    
    //
    buildUser();
    //
    makeCategories();
  }

  @Override
  public void reject(HashMap<String, String> params) throws Exception {

  }
  
  void buildUser() {
    List<String> users = new ArrayList<String>();
    for (int i = from; i <= to; i++) {
      users.add(user_ + String.valueOf(i));
    }
    this.maxUser = users.size();
    this.users = users.toArray(new String[maxUser]);
  }
  
  protected int intParam(HashMap<String, String> params, String name, int df) {
    try {
      return Integer.valueOf(strParam(params, name, null));
    } catch (NumberFormatException e) {
      getLog().warn("Integer number expected for property " + name);
    }
    return df;
  }

  protected String strParam(HashMap<String, String> params, String name, String defaultVl) {
    //
    if (params == null) {
      throw new NullPointerException();
    }
    //
    try {
      String value = params.get(name);
      if (value != null) {
        return value;
      }
    } catch (Exception e) {
      getLog().warn("Can not get value of param" + name, e);
    }
    return defaultVl;
  }
  
  private String getParagraphs() {
    lorem = new LoremIpsum4J();
    String p = lorem.getParagraphs();
    if(supportBBcodes.isEmpty() == false) {
      p = lorem.getCharacters(4, supportBBcodes);
    }
    return p;
  }
  
  private void makeCategories() {
    List<Future> futures = new ArrayList<Future>();
    for (int i = 0; i < cate; i++) {
      BuildCategory buildCategory = new BuildCategory();
      buildCategory.setName(cate_ + String.valueOf(i));
//      futures.add(completionService.addTaskNotConf(buildCategory));
      completionService.addTask(buildCategory);
    }
    SessionProviderService providerService = CommonsUtils.getService(SessionProviderService.class);
    if (providerService instanceof SessionProviderServiceImpl) {
      boolean isDone = false;

      while (isDone == false) {
        isDone = true;
        for (Future future : futures) {
          if (future.isDone() == false) {
            isDone = false;
            break;
          }
        }
        if (isDone == false) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }

  }
  
  class BuildCategory extends BuildObject<Category>{

    @Override
    protected Category build(String name, String path) throws Exception {
      Category cat = new Category();
      cat.setOwner(owner);
      cat.setCategoryName(name);
      cat.setCategoryOrder(coutCat++);
      cat.setCreatedDate(new Date());
      cat.setDescription(name + " desciption");
      cat.setModifiedBy(owner);
      cat.setModifiedDate(new Date());
      
      getForumService().saveCategory(cat, true);
      return cat;
    }

    @Override
    protected void callback(Category e) throws Exception {
      for (int i = 0; i < forum; i++) {
        ForumBuild forumBuild = new ForumBuild();
        forumBuild.setName(forum_ + String.valueOf(i));
        forumBuild.setPath(e.getId());
        completionService.addTask(forumBuild);
      }
    }
    
  }
  
  class ForumBuild extends BuildObject<Forum> {

    @Override
    protected void callback(Forum e) throws Exception {
      for (int i = 0; i < topic; i++) {
        TopicBuild topicBuild = new TopicBuild();
        topicBuild.setName(topic_ + String.valueOf(i));
        topicBuild.setPath(e.getPath());
        completionService.addTask(topicBuild);
      }
    }

    @Override
    protected Forum build(String name, String path) throws Exception {
      Forum forum = new Forum();
      forum.setOwner(owner);
      forum.setForumName(name);
      forum.setForumOrder(coutFor++);
      forum.setCreatedDate(new Date());
      forum.setModifiedBy(owner);
      forum.setModifiedDate(new Date());
      forum.setLastTopicPath("");
      forum.setDescription(owner + " description");
      forum.setPostCount(0);
      forum.setTopicCount(0);

      forum.setNotifyWhenAddTopic(new String[] {});
      forum.setNotifyWhenAddPost(new String[] {});
      forum.setIsModeratePost(false);
      forum.setIsModerateTopic(false);
      forum.setIsClosed(false);
      forum.setIsLock(false);

      forum.setViewer(new String[] {});
      forum.setCreateTopicRole(new String[] {});
      forum.setModerators(new String[] {});

      forumService.saveForum(path, forum, true);
      return forum;
    }

  }

  class TopicBuild extends BuildObject<Topic> {

    @Override
    protected void callback(Topic e) throws Exception {
      for (int i = 0; i < post; i++) {
        PostBuild postBuild = new PostBuild();
        postBuild.setName(post_ + String.valueOf(i));
        postBuild.setPath(e.getPath());
        //
        postBuild.build(post_ + String.valueOf(i), e.getPath());
      }

    }

    @Override
    protected Topic build(String name, String path) throws Exception {
      Topic topicNew = new Topic();
      
      String owner = users[coutTop % maxUser];
      
      topicNew.setOwner(owner);
      topicNew.setTopicName(name);
      topicNew.setCreatedDate(new Date());
      topicNew.setModifiedBy(owner);
      topicNew.setModifiedDate(new Date());
      topicNew.setLastPostBy(owner);
      topicNew.setLastPostDate(new Date());
      topicNew.setDescription(lorem.getParagraphs());
      topicNew.setPostCount(0);
      topicNew.setViewCount(0);
      topicNew.setIsNotifyWhenAddPost("");
      topicNew.setIsModeratePost(false);
      topicNew.setIsClosed(false);
      topicNew.setIsLock(false);
      topicNew.setIsWaiting(false);
      topicNew.setIsActive(true);
      topicNew.setIcon("classNameIcon");
      topicNew.setIsApproved(true);
      topicNew.setCanView(new String[] {});
      topicNew.setCanPost(new String[] {});

      //
      forumService.saveTopic(Utils.getCategoryId(path), Utils.getForumId(path), 
                             topicNew, true, false, new MessageBuilder());
      coutTop++;
      return topicNew;
    }

  }

  class PostBuild extends BuildObject<Post> {

    @Override
    protected void callback(Post e) throws Exception {
      getLog().info("Created done post: " + e.getName());
    }

    @Override
    protected Post build(String name, String path) throws Exception {
      Post post = new Post();
      
      String owner = users[coutPos % maxUser];
      
      post.setOwner(owner);
      post.setCreatedDate(new Date());
      post.setModifiedBy(owner);
      post.setModifiedDate(new Date());
      post.setName(name);
      post.setMessage(getParagraphs());
      post.setRemoteAddr("127.0.0.1");
      post.setIcon("uiIconForumTopic");
      post.setIsApproved(true);
      post.setIsActiveByTopic(true);
      post.setIsHidden(false);
      post.setIsWaiting(false);

      //
      forumService.savePost(Utils.getCategoryId(path), Utils.getForumId(path), Utils.getTopicId(path),
                            post, true, new MessageBuilder());
      coutPos++;
      return post;
    }

  }
  
  abstract class BuildObject<E> implements Callable<E>{
    private E cate;
    private String name;
    private String path;
    
    public void setPath(String path) {
      this.path = path;
    }
    public void setName(String name) {
      this.name = name;
    }
    
    @Override
    public E call() throws Exception {
      LOG.info("Building " + name + " of the path: " + path);
      E e = build(name, path);
      SessionProviderService providerService = CommonsUtils.getService(SessionProviderService.class);
      if(providerService instanceof SessionProviderServiceImpl) {
        ((SessionProviderServiceImpl)providerService).closeSessionProvider();
      }
      callback(e);
      return e;
    }
    
    public E getObject() {
      return cate;
    }
    
    protected abstract void callback(E e) throws Exception;
    
    protected abstract E build(String name, String path) throws Exception;
  }

}
