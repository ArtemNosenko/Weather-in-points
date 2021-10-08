#include "backendnetworking.h"


//#include "thirdparty/boost/beast/core.hpp"
//#include "thirdparty/boost/beast/http.hpp"
//#include "thirdparty/boost/beast/version.hpp"
//#include "thirdparty/boost/asio/connect.hpp"
//#include "thirdparty/boost/asio/ip/tcp.hpp"


#include <boost/beast/core.hpp>
#include <boost/beast/http.hpp>
#include <boost/beast/version.hpp>
#include <boost/asio/connect.hpp>
#include <boost/asio/ip/tcp.hpp>

#include <cstdlib>
#include <iostream>
#include <string>


namespace beast = boost::beast;
namespace http = beast::http;
namespace net = boost::asio;
using tcp = net::ip::tcp;


BackEndNetworking::BackEndNetworking(QObject *parent) : QObject(parent)
{

}

QString BackEndNetworking::run()
{

    // Declare a container to hold the response
    http::response<http::dynamic_body> res;
    try
    {
        // Check command line arguments.

        auto const port = "80";
        int version = 11;

        // The io_context is required for all I/O
        net::io_context ioc;

        // These objects perform our I/O
        tcp::resolver resolver(ioc);
        beast::tcp_stream stream(ioc);

        // Look up the domain name
        auto const results = resolver.resolve(host().toStdString(), port);

        // Make the connection on the IP address we get from a lookup
        stream.connect(results);

        // Set up an HTTP GET request message
        http::request<http::string_body> req{http::verb::get, request().toStdString(), version};
        req.set(http::field::host,host().toStdString());
        req.set(http::field::user_agent, BOOST_BEAST_VERSION_STRING);

        // Send the HTTP request to the remote host
        http::write(stream, req);

        // This buffer is used for reading and must be persisted
        beast::flat_buffer buffer;

        // Receive the HTTP response
        http::read(stream, buffer, res);

        // Gracefully close the socket
        beast::error_code ec;
        stream.socket().shutdown(tcp::socket::shutdown_both, ec);

        // not_connected happens sometimes
        // so don't bother reporting it.
        //
        if(ec && ec != beast::errc::not_connected)
            throw beast::system_error{ec};

        // If we get here then the connection is closed gracefully
    }
    catch(std::exception const& e)
    {
        return QString("Error: ") + QString(e.what());
    }

    return QString::fromStdString(boost::beast::buffers_to_string(res.body().data()));
}
