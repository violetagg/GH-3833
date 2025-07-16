package org.example;

import io.netty.buffer.Unpooled;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.LoopResources;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

public class Main {
	static final byte[] STATIC_PLAINTEXT = "Hello, World!".getBytes(CharsetUtil.UTF_8);
	static final int STATIC_PLAINTEXT_LEN = STATIC_PLAINTEXT.length;
	static final CharSequence PLAINTEXT_CL_HEADER_VALUE = AsciiString.cached(String.valueOf(STATIC_PLAINTEXT_LEN));

	public static void main(String[] args) {
		HttpServer.create()
				.port(8080)
				.handle((req, res) ->
						res.header(CONTENT_LENGTH, PLAINTEXT_CL_HEADER_VALUE)
								.sendObject(Unpooled.wrappedBuffer(STATIC_PLAINTEXT)))
				.bindNow()
				.onDispose()
				.block();
	}
}