// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include "string.h"
#include "stream.h"
#include <vespa/vespalib/data/simple_buffer.h>
#include <vespa/vespalib/net/socket_handle.h>
#include <vespa/vespalib/net/server_socket.h>
#include <memory>

namespace vbench {

using Input = vespalib::Input;
using Memory = vespalib::Memory;
using Output = vespalib::Output;
using SimpleBuffer = vespalib::SimpleBuffer;
using WritableMemory = vespalib::WritableMemory;

class Socket : public Stream
{
private:
    vespalib::SocketHandle _socket;
    SimpleBuffer           _input;
    SimpleBuffer           _output;
    Taint                  _taint;
    bool                   _eof;

public:
    Socket(vespalib::SocketHandle socket);
    Socket(const string &host, int port);
    ~Socket();
    bool eof() const override { return _eof; }
    Memory obtain() override;
    Input &evict(size_t bytes) override;
    WritableMemory reserve(size_t bytes) override;
    Output &commit(size_t bytes) override;
    const Taint &tainted() const override { return _taint; }
};

struct ServerSocket {
    vespalib::ServerSocket server_socket;
    ServerSocket() : server_socket(0) {}
    int port() const { return server_socket.address().port(); }
    Stream::UP accept() {
        vespalib::SocketHandle handle = server_socket.accept();
        if (handle.valid()) {
            return std::make_unique<Socket>(std::move(handle));
        } else {
            return Stream::UP();
        }
    }
    void close() { server_socket.shutdown(); }
};

} // namespace vbench
