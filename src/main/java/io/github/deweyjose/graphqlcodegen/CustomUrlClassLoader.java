package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

//  Don't use ClassRealm as the Javadoc explicitly says not to depend on it
//  //ClassRealm classRealm = pluginDescriptor.getClassRealm();
//  https://maven.apache.org/ref/3.2.3/apidocs/org/apache/maven/project/MavenProject.html#getClassRealm()
//  Instead create a new classloader to read files from classpath
public class CustomUrlClassLoader extends URLClassLoader {
    public CustomUrlClassLoader(List<String> classpath) {
        super(new URL[0]);
        if (classpath == null || classpath.isEmpty()) {
            throw new IllegalArgumentException("Invalid classpath provided");
        }
        classpath.stream()
                .map(File::new)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .forEach(this::addURL);
    }
}
