/*
 * Copyright (c) 2016, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
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
package com.noctarius.lazylog.impl;

import com.noctarius.lazylog.Logger;
import com.noctarius.lazylog.LoggerFactory;

public class Log4jLoggerFactory
        implements LoggerFactory {

    @Override
    public Logger create(Class<?> binding) {
        return new Log4jLogger(org.apache.log4j.Logger.getLogger(binding));
    }

    @Override
    public Logger create(String binding) {
        return new Log4jLogger(org.apache.log4j.Logger.getLogger(binding));
    }

    @Override
    public Class<?> loggerClass() {
        return org.apache.log4j.Logger.class;
    }
}
