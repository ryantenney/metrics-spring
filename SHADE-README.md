To use metrics-spring in a project which uses the maven-shade-plugin, it is necessary to configure several resource transformers to instruct shade on how to merge resources.

_Without this Spring will not work correctly and reporters will not be detected._

Sample shade plugin configuration:
```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-shade-plugin</artifactId>
	<version>2.1</version>
	<executions>
		<execution>
			<phase>package</phase>
			<goals>
				<goal>shade</goal>
			</goals>
			<configuration>
				<transformers>
					<transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
						<resource>META-INF/spring.handlers</resource>
					</transformer>
					<transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
						<resource>META-INF/spring.schemas</resource>
					</transformer>
					<transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
				</transformers>
			</configuration>
		</execution>
	</executions>
</plugin>
```
