package io.github.deweyjose.utils;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

	void accept(T t) throws E;

	static <T> Consumer<T> throwingConsumerWrapper(final ThrowingConsumer<T, Exception> throwingConsumer) {

		return i -> {
			try {
				throwingConsumer.accept(i);
			} catch (final Exception ex) {
				throw new RuntimeException(ex);
			}
		};
	}
}
