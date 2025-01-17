/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.security.user.query;

import org.apache.jackrabbit.guava.common.base.Predicate;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.apache.jackrabbit.oak.security.user.query.QueryUtil.getID;

/**
 * Predicate used to filter authorizables based on their group membership.
 */
class GroupPredicate implements Predicate<Authorizable> {

    private final Iterator<Authorizable> membersIterator;
    private final Set<String> memberIds = new HashSet<>();

    GroupPredicate(@NotNull UserManager userManager, @NotNull String groupId, boolean declaredMembersOnly) throws RepositoryException {
        Authorizable authorizable = userManager.getAuthorizable(groupId);
        Group group = (authorizable == null || !authorizable.isGroup()) ? null : (Group) authorizable;
        if (group != null) {
            membersIterator = (declaredMembersOnly) ? group.getDeclaredMembers() : group.getMembers();
        } else {
            membersIterator = Collections.emptyIterator();
        }
    }

    @Override
    public boolean apply(@Nullable Authorizable authorizable) {
        String id = getID(authorizable);
        if (id != null) {
            if (memberIds.contains(id)) {
                return true;
            } else {
                // not contained in ids that have already been processed => look
                // for occurrence in the remaining iterator entries.
                while (membersIterator.hasNext()) {
                    String memberId = getID(membersIterator.next());
                    if (memberId != null) {
                        memberIds.add(memberId);
                        if (memberId.equals(id)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
