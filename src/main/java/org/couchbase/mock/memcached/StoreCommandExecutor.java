/*
 *  Copyright 2011 Couchbase, Inc..
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.couchbase.mock.memcached;

import org.couchbase.mock.memcached.protocol.BinaryResponse;
import org.couchbase.mock.memcached.protocol.ErrorCode;
import org.couchbase.mock.memcached.protocol.BinaryCommand;
import org.couchbase.mock.memcached.protocol.BinaryStoreCommand;
import org.couchbase.mock.memcached.protocol.BinaryStoreResponse;
import org.couchbase.mock.memcached.protocol.ComCode;

/**
 *
 * @author Trond Norbye <trond.norbye@gmail.com>
 */
public class StoreCommandExecutor implements CommandExecutor {

    @Override
    public void execute(BinaryCommand cmd, MemcachedServer server, MemcachedConnection client) {
        BinaryStoreCommand command = (BinaryStoreCommand) cmd;

        ErrorCode err = ErrorCode.SUCCESS;
        Item item = command.getItem();
        ComCode cc = cmd.getComCode();

        switch (cc) {
            case ADD:
            case ADDQ:
                err = server.getDatastore().add(server, cmd.getVBucketId(), item);
                break;
            case REPLACE:
            case REPLACEQ:
                err = server.getDatastore().replace(server, cmd.getVBucketId(), item);
                break;
            case SET:
            case SETQ:
                err = server.getDatastore().set(server, cmd.getVBucketId(), item);
                break;
            default:
                client.sendResponse(new BinaryResponse(cmd, ErrorCode.EINTERNAL));
                return;
        }

        // Quiet commands doesn't send a reply for success.
        if (err == ErrorCode.SUCCESS && (cc == ComCode.ADDQ || cc == ComCode.SETQ || cc == ComCode.REPLACEQ)) {
            return;
        }
        client.sendResponse(new BinaryStoreResponse(command, err, item.getCas()));
    }
}
