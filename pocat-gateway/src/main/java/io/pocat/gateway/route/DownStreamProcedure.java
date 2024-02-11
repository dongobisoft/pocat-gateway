package io.pocat.gateway.route;

import io.pocat.message.Message;

public interface DownStreamProcedure {
    void call(Message message);
}
