/*
 * Copyright (c) 2011-2018 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.ipc.netty.http.client;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import reactor.ipc.netty.Connection;
import reactor.ipc.netty.tcp.TcpClient;

/**
 * @author Stephane Maldini
 */
final class HttpClientLifecycle extends HttpClientOperator
		implements Consumer<Connection> {

	final Consumer<? super HttpClientRequest>  onRequest;
	final Consumer<? super HttpClientRequest>  afterRequest;
	final Consumer<? super HttpClientResponse> onResponse;
	final Consumer<? super HttpClientResponse> afterResponse;

	HttpClientLifecycle(HttpClient client,
			@Nullable Consumer<? super HttpClientRequest> onRequest,
			@Nullable Consumer<? super HttpClientRequest> afterRequest,
			@Nullable Consumer<? super HttpClientResponse> onResponse,
			@Nullable Consumer<? super HttpClientResponse> afterResponse) {
		super(client);
		this.onRequest = onRequest;
		this.afterRequest = afterRequest;
		this.onResponse = onResponse;
		this.afterResponse = afterResponse;
	}

	static final Consumer<? super Throwable> EMPTY_ERROR = e -> {};

	@Override
	protected TcpClient tcpConfiguration() {
		TcpClient client = super.tcpConfiguration();
		if (onRequest != null || afterRequest != null) {
			client = client.bootstrap(b -> HttpClientConfiguration.aroundBody(b, onRequest, afterRequest));
		}
		if (onResponse != null || afterResponse != null) {
			return client.doOnConnected(this);
		}
		return client;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void accept(Connection o) {
		HttpClientOperations ops = (HttpClientOperations)o;

		if (onResponse != null) {
			onResponse.accept(ops);
		}

		if (afterResponse != null) {
//			ops.responseEnd.subscribe(null, EMPTY_ERROR, () -> afterResponse.accept(ops));
		}
	}

}
