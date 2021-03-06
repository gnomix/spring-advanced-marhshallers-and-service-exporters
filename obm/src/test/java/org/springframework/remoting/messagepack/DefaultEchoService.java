/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.remoting.messagepack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Josh Long
 */
public class DefaultEchoService implements EchoService, CatService {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Log log = LogFactory.getLog(getClass());

    @Override
    public String alarm(String msg) {
        return (msg == null ? "" : msg).toUpperCase() + "!!";
    }

    public DefaultEchoService() {
        if (executor instanceof InitializingBean) {
            try {
                ((InitializingBean) executor).afterPropertiesSet();
            } catch (Exception e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Cat fetch() {
        Cat garfield = new Cat("Garfield", 53);
        Cat nermel = new Cat("Nermel", 12);
        Cat george = new Cat("George", 42);
        garfield.addFriend(nermel);
        garfield.addFriend(george);

        Human john = new Human("John");
        Human mary = new Human("Mary");

        garfield.addHuman(john);
        garfield.addHuman(mary);

        return garfield;
    }

    /**
     * nb: this is not part of the interface, but MessagePack will attempt to call it if it's available.
     * <p/>
     * The {@link org.msgpack.rpc.Request} parameter's used to give the callee a chance to handle the request asynchronously or to leverage the error handling mechanism.
     *
     * @param request object required to facilitate communication
     * @param in      the string to send back
     */
    public void hello(final org.msgpack.rpc.Request request, final String in) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    long time = 2;
                    Thread.sleep(time * 1000);
                    if (log.isDebugEnabled()) {
                        log.debug("sleeping for " + time + "s");
                    }
                    request.sendResult("Hello, " + in);
                } catch (Throwable thro) {
                    if (log.isErrorEnabled()) {
                        log.error(thro);
                    }
                }
            }
        });
    }

    public void fetchCat(final org.msgpack.rpc.Request request) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3 * 1000);
                    Cat cat = fetch();
                    request.sendResult(cat);
                } catch (Throwable thro) {
                    if (log.isErrorEnabled()) {
                        log.error(thro);
                    }
                }
            }
        });
    }

    @Override
    public String echo(String in) {
        return "No, " + in;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
}