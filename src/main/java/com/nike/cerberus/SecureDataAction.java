/*
 * Copyright (c) 2017 Nike, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nike.cerberus;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Arrays;
import java.util.Set;

import static com.nike.cerberus.record.RoleRecord.ROLE_OWNER;
import static com.nike.cerberus.record.RoleRecord.ROLE_READ;
import static com.nike.cerberus.record.RoleRecord.ROLE_WRITE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;

public enum SecureDataAction {

    READ(ImmutableSet.of(GET), ImmutableSet.of(ROLE_OWNER, ROLE_WRITE, ROLE_READ)),
    WRITE(ImmutableSet.of(POST, PUT), ImmutableSet.of(ROLE_OWNER, ROLE_WRITE)),
    DELETE(ImmutableSet.of(HttpMethod.DELETE), ImmutableSet.of(ROLE_OWNER, ROLE_WRITE));

    private final Set<HttpMethod> methods;

    private final ImmutableSet<String> allowedRoles;

    public Set<HttpMethod> getMethods() {
        return methods;
    }

    public ImmutableSet<String> getAllowedRoles() {
        return allowedRoles;
    }

    SecureDataAction(Set<HttpMethod> methods, ImmutableSet<String> allowedRoles) {
        this.methods = methods;
        this.allowedRoles = allowedRoles;
    }

    public static SecureDataAction fromMethod(HttpMethod method) {
        SecureDataAction action = Arrays.stream(values())
                .filter(e -> e.methods.contains(method))
                .findFirst()
                .orElse(null);

        Preconditions.checkNotNull(action, String.format("Method: %s is not associated with an secure data action", method.name()));
        return action;
    }
}
