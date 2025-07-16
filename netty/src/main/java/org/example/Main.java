package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.uring.IoUringIoHandler;
//import io.netty.channel.uring.IoUringServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Main {
	static final byte[] STATIC_PLAINTEXT = "Hello, World!".getBytes(CharsetUtil.UTF_8);
	static final int STATIC_PLAINTEXT_LEN = STATIC_PLAINTEXT.length;
	static final CharSequence PLAINTEXT_CL_HEADER_VALUE = AsciiString.cached(String.valueOf(STATIC_PLAINTEXT_LEN));

	public static void main(String[] args) throws Exception {
		// Epoll
		EventLoopGroup group = new MultiThreadIoEventLoopGroup(EpollIoHandler.newFactory());
		//IO_Uring
		//EventLoopGroup group = new MultiThreadIoEventLoopGroup(IoUringIoHandler.newFactory());
		try {
			ServerBootstrap b = new ServerBootstrap();

			// Epoll
			b.group(group)
					.channel(EpollServerSocketChannel.class);

			//IO_Uring
			//b.group(new MultiThreadIoEventLoopGroup(IoUringIoHandler.newFactory()))
			//		.channel(IoUringServerSocketChannel.class);

			b.childHandler(new HelloWorldServerInitializer());

			Channel ch = b.bind(new InetSocketAddress(8080)).sync().channel();
			ch.closeFuture().sync();
		}
		finally {
			group.shutdownGracefully().sync();
		}
	}

	static final class HelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel socketChannel) {
			socketChannel.pipeline().addLast(new HttpServerCodec(), new HelloWorldServerHandler());
		}
	}

	static final class HelloWorldServerHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			try {
				if (msg instanceof HttpRequest) {
					FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(STATIC_PLAINTEXT));
					response.headers().set(CONTENT_LENGTH, PLAINTEXT_CL_HEADER_VALUE);
					ctx.write(response);
				}
			}
			finally {
				ReferenceCountUtil.release(msg);
			}
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) {
			ctx.flush();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			ctx.close();
		}
	}
}