//
// ========================================================================
// Copyright (c) 1995-2020 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package embedded.client.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.util.AsyncRequestContent;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.BytesRequestContent;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.client.util.InputStreamRequestContent;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.OutputStreamRequestContent;
import org.eclipse.jetty.client.util.PathRequestContent;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import static java.lang.System.Logger.Level.INFO;

@SuppressWarnings("unused")
public class HTTPClientDocs
{
    public void start() throws Exception
    {
        // tag::start[]
        // Instantiate HttpClient.
        HttpClient httpClient = new HttpClient();

        // Configure HttpClient, for example:
        httpClient.setFollowRedirects(false);

        // Start HttpClient.
        httpClient.start();
        // end::start[]
    }

    public void stop() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();
        // tag::stop[]
        // Stop HttpClient.
        httpClient.stop();
        // end::stop[]
    }

    public void tlsExplicit() throws Exception
    {
        // tag::tlsExplicit[]
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();

        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);

        HttpClient httpClient = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        httpClient.start();
        // end::tlsExplicit[]
    }

    public void tlsNoValidation()
    {
        // tag::tlsNoValidation[]
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        // Disable certificate validation at the TLS level.
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        // end::tlsNoValidation[]
    }

    public void tlsAppValidation()
    {
        // tag::tlsAppValidation[]
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        // Only allow subdomains of domain.com.
        sslContextFactory.setHostnameVerifier((hostName, session) -> hostName.endsWith(".domain.com"));
        // end::tlsAppValidation[]
    }

    public void simpleBlockingGet() throws Exception
    {
        // tag::simpleBlockingGet[]
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // Perform a simple GET and wait for the response.
        ContentResponse response = httpClient.GET("http://domain.com/path?query");
        // end::simpleBlockingGet[]
    }

    public void headFluent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::headFluent[]
        ContentResponse response = httpClient.newRequest("http://domain.com/path?query")
            .method(HttpMethod.HEAD)
            .agent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:17.0) Gecko/20100101 Firefox/17.0")
            .send();
        // end::headFluent[]
    }

    public void headNonFluent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::headNonFluent[]
        Request request = httpClient.newRequest("http://domain.com/path?query");
        request.method(HttpMethod.HEAD);
        request.agent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:17.0) Gecko/20100101 Firefox/17.0");
        ContentResponse response = request.send();
        // end::headNonFluent[]
    }

    public void postFluent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::postFluent[]
        ContentResponse response = httpClient.POST("http://domain.com/entity/1")
            .param("p", "value")
            .send();
        // end::postFluent[]
    }

    public void fileFluent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::fileFluent[]
        ContentResponse response = httpClient.POST("http://domain.com/upload")
            .file(Paths.get("file_to_upload.txt"), "text/plain")
            .send();
        // end::fileFluent[]
    }

    public void totalTimeout() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::totalTimeout[]
        ContentResponse response = httpClient.newRequest("http://domain.com/path?query")
            .timeout(5, TimeUnit.SECONDS)
            .send();
        // end::totalTimeout[]
    }

    public void simpleNonBlocking() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::simpleNonBlocking[]
        httpClient.newRequest("http://domain.com/path")
            .send(result ->
            {
                // Your logic here
            });
        // end::simpleNonBlocking[]
    }

    public void nonBlockingTotalTimeout() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::nonBlockingTotalTimeout[]
        httpClient.newRequest("http://domain.com/path")
            .timeout(3, TimeUnit.SECONDS)
            .send(result ->
            {
                /* Your logic here */
            });
        // end::nonBlockingTotalTimeout[]
    }

    // @checkstyle-disable-check : LeftCurly
    public void listeners() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::listeners[]
        httpClient.newRequest("http://domain.com/path")
            // Add request hooks.
            .onRequestQueued(request -> { /* ... */ })
            .onRequestBegin(request -> { /* ... */ })
            .onRequestHeaders(request -> { /* ... */ })
            .onRequestCommit(request -> { /* ... */ })
            .onRequestContent((request, content) -> { /* ... */ })
            .onRequestFailure((request, failure) -> { /* ... */ })
            .onRequestSuccess(request -> { /* ... */ })
            // Add response hooks.
            .onResponseBegin(response -> { /* ... */ })
            .onResponseHeader((response, field) -> true)
            .onResponseHeaders(response -> { /* ... */ })
            .onResponseContentAsync((response, buffer, callback) -> callback.succeeded())
            .onResponseFailure((response, failure) -> { /* ... */ })
            .onResponseSuccess(response -> { /* ... */ })
            // Result hook.
            .send(result -> { /* ... */ });
        // end::listeners[]
    }

    public void pathRequestContent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::pathRequestContent[]
        ContentResponse response = httpClient.POST("http://domain.com/upload")
            .body(new PathRequestContent("text/plain", Paths.get("file_to_upload.txt")))
            .send();
        // end::pathRequestContent[]
    }

    public void inputStreamRequestContent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::inputStreamRequestContent[]
        ContentResponse response = httpClient.POST("http://domain.com/upload")
            .body(new InputStreamRequestContent("text/plain", new FileInputStream("file_to_upload.txt")))
            .send();
        // end::inputStreamRequestContent[]
    }

    public void bytesStringRequestContent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        byte[] bytes = new byte[1024];
        String string = new String(bytes);
        // tag::bytesStringRequestContent[]
        ContentResponse bytesResponse = httpClient.POST("http://domain.com/upload")
            .body(new BytesRequestContent("text/plain", bytes))
            .send();

        ContentResponse stringResponse = httpClient.POST("http://domain.com/upload")
            .body(new StringRequestContent("text/plain", string))
            .send();
        // end::bytesStringRequestContent[]
    }

    public void asyncRequestContent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::asyncRequestContent[]
        AsyncRequestContent content = new AsyncRequestContent();
        httpClient.POST("http://domain.com/upload")
            .body(content)
            .send(result ->
            {
                // Your logic here
            });

        // Content not available yet here.

        // An event happens in some other class, in some other thread.
        class ContentPublisher
        {
            void publish(ByteBufferPool bufferPool, byte[] bytes, boolean lastContent)
            {
                // Wrap the bytes into a new ByteBuffer.
                ByteBuffer buffer = ByteBuffer.wrap(bytes);

                // Offer the content, and release the ByteBuffer
                // to the pool when the Callback is completed.
                content.offer(buffer, Callback.from(() -> bufferPool.release(buffer)));

                // Close AsyncRequestContent when all the content is arrived.
                if (lastContent)
                    content.close();
            }
        }
        // end::asyncRequestContent[]
    }

    public void outputStreamRequestContent() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::outputStreamRequestContent[]
        OutputStreamRequestContent content = new OutputStreamRequestContent();

        // Use try-with-resources to close the OutputStream when all content is written.
        try (OutputStream output = content.getOutputStream())
        {
            httpClient.POST("http://localhost:8080/")
                .body(content)
                .send(result ->
                {
                    // Your logic here
                });

            // Content not available yet here.

            // Content is now available.
            byte[] bytes = new byte[]{'h', 'e', 'l', 'l', 'o'};
            output.write(bytes);
        }
        // End of try-with-resource, output.close() called automatically to signal end of content.
        // end::outputStreamRequestContent[]
    }

    public void futureResponseListener() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::futureResponseListener[]
        Request request = httpClient.newRequest("http://domain.com/path");

        // Limit response content buffer to 512 KiB.
        FutureResponseListener listener = new FutureResponseListener(request, 512 * 1024);

        request.send(listener);

        // Wait at most 5 seconds for request+response to complete.
        ContentResponse response = listener.get(5, TimeUnit.SECONDS);
        // end::futureResponseListener[]
    }

    public void bufferingResponseListener() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::bufferingResponseListener[]
        httpClient.newRequest("http://domain.com/path")
            // Buffer response content up to 8 MiB
            .send(new BufferingResponseListener(8 * 1024 * 1024)
            {
                @Override
                public void onComplete(Result result)
                {
                    if (!result.isFailed())
                    {
                        byte[] responseContent = getContent();
                        // Your logic here
                    }
                }
            });
        // end::bufferingResponseListener[]
    }

    public void inputStreamResponseListener() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // tag::inputStreamResponseListener[]
        InputStreamResponseListener listener = new InputStreamResponseListener();
        httpClient.newRequest("http://domain.com/path")
            .send(listener);

        // Wait for the response headers to arrive.
        Response response = listener.get(5, TimeUnit.SECONDS);

        // Look at the response before streaming the content.
        if (response.getStatus() == HttpStatus.OK_200)
        {
            // Use try-with-resources to close input stream.
            try (InputStream responseContent = listener.getInputStream())
            {
                // Your logic here
            }
        }
        else
        {
            response.abort(new IOException("Unexpected HTTP response"));
        }
        // end::inputStreamResponseListener[]
    }

    public void demandedContentListener() throws Exception
    {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        String host1 = "localhost";
        String host2 = "localhost";
        int port1 = 8080;
        int port2 = 8080;
        // tag::demandedContentListener[]
        // Prepare a request to server1, the source.
        Request request1 = httpClient.newRequest(host1, port1)
            .path("/source");

        // Prepare a request to server2, the sink.
        AsyncRequestContent content2 = new AsyncRequestContent();
        Request request2 = httpClient.newRequest(host2, port2)
            .path("/sink")
            .body(content2);

        request1.onResponseContentDemanded(new Response.DemandedContentListener()
        {
            @Override
            public void onBeforeContent(Response response, LongConsumer demand)
            {
                request2.onRequestCommit(request ->
                {
                    // Only when the request to server2 has been sent,
                    // then demand response content from server1.
                    demand.accept(1);
                });

                // Send the request to server2.
                request2.send(result -> System.getLogger("forwarder").log(INFO, "Forwarding to server2 complete"));
            }

            @Override
            public void onContent(Response response, LongConsumer demand, ByteBuffer content, Callback callback)
            {
                // When response content is received from server1, forward it to server2.
                content2.offer(content, Callback.from(() ->
                {
                    // When the request content to server2 is sent,
                    // succeed the callback to recycle the buffer.
                    callback.succeeded();
                    // Then demand more response content from server1.
                    demand.accept(1);
                }, callback::failed));
            }
        });

        // When the response content from server1 is complete,
        // complete also the request content to server2.
        request1.onResponseSuccess(response -> content2.close());

        // Send the request to server1.
        request1.send(result -> System.getLogger("forwarder").log(INFO, "Sourcing from server1 complete"));
        // end::demandedContentListener[]
    }
}
