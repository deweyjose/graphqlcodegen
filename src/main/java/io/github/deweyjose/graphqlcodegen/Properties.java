package io.github.deweyjose.graphqlcodegen;

import java.util.Map;

import org.apache.maven.plugins.annotations.Parameter;

public class Properties {
	@Parameter
	private Map<String, String> properties;

	@Override
	public String toString() {
		return properties.toString();
	} // to test

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, String> properties) {
		this.properties = properties;
	}
}
