package io.github.deweyjose.paramcodegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate")
public class ParamCodegen extends AbstractMojo {

  @Parameter(
      property = "paramcodegen.outputDirectory",
      defaultValue = "${project.build.directory}/generated-sources/paramcodegen")
  private String outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Parameter Code Generator Plugin");
    getLog().info("Output Directory: " + outputDirectory);
  }
}
