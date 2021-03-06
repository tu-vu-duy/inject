/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.extras.injection.forum;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.extras.injection.utils.LoremIpsum4J;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class PostInjector extends AbstractForumInjector {
  
  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String POST_PREFIX = "postPrefix";
  
  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";

  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String TO_TOPIC = "toTopic";

  /** . */
  private static final String TOPIC_PREFIX = "topicPrefix";

  private List<String> supportBBcodes = new ArrayList<String>();

  public PostInjector() {
    super();
    try {
      supportBBcodes.clear();
      List<String> supportBBcodes = bbcodeService.getActive();
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

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    String postPrefix = params.get(POST_PREFIX);
    
    //
    int fromUser = param(params, FROM_USER);
    int toUser = param(params, TO_USER);
    String userPrefix = params.get(USER_PREFIX);
    
    //
    int toTopic = param(params, TO_TOPIC);
    String topicPrefix = params.get(TOPIC_PREFIX);
    init(userPrefix, null, null, topicPrefix, postPrefix, 0);

    //
    String topicName = topicBase + toTopic;
    Topic topic = getTopicByName(topicName);
    if (topic == null) {
      getLog().info("topic name is '" + topicName + "' wrong. Please set it exactly. Aborting injection ..." );
      return;
    }
    Forum forum = forumService.getForum(topic.getCategoryId(), topic.getForumId());
    Category cat = forumService.getCategory(topic.getCategoryId());
    
    //
    for (int i = fromUser; i <= toUser; ++i) {

      //
      String owner = userBase + i;

      for (int j = 0; j < number; ++j) {

        String postName = postName();

        Post post = new Post();
        post.setOwner(owner);
        post.setCreatedDate(new Date());
        post.setModifiedBy(owner);
        post.setModifiedDate(new Date());
        post.setName(postName);
        post.setMessage(getParagraphs());
        post.setRemoteAddr("127.0.0.1");
        post.setIcon("uiIconForumTopic");
        post.setIsApproved(true);
        post.setIsActiveByTopic(true);
        post.setIsHidden(false);
        post.setIsWaiting(false);

        //
        forumService.savePost(cat.getId(),
                              forum.getId(),
                              topic.getId(),
                              post,
                              true,
                              new MessageBuilder());
        ++postNumber;
        //
        getLog().info("Post '" + postName + "' into '" + topicName + "' topic of '"
            + forum.getForumName() + "' forum created by " + owner);

      }

    }

  }
  
  private String getParagraphs() {
    lorem = new LoremIpsum4J();
    String p = lorem.getParagraphs();
    if(supportBBcodes.isEmpty() == false) {
      p = lorem.getCharacters(4, supportBBcodes);
    }
    return p;
  }

 
}
